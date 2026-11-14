package com.example.eatstoday.feature.recipe.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eatstoday.core.utils.normalizedForSearch
import com.example.eatstoday.data.model.CollectionWithCount
import com.example.eatstoday.data.model.DietTag
import com.example.eatstoday.data.model.Difficulty
import com.example.eatstoday.data.model.Ingredient
import com.example.eatstoday.data.model.MealType
import com.example.eatstoday.data.model.Recipe
import com.example.eatstoday.data.model.User
import com.example.eatstoday.data.repository.PantryRepository
import com.example.eatstoday.data.repository.RecipeRepository
import com.example.eatstoday.data.repository.FeedRepository
import com.example.eatstoday.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private fun <T> kotlinx.coroutines.flow.Flow<T>.asState(vm: ViewModel, initial: T): StateFlow<T> =
    stateIn(vm.viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)

enum class QuickFilter(val label: String) { ALL("Tất cả"), QUICK("Nhanh 15p"), EASY("Dễ"), VEGETARIAN("Chay") }

data class HomeUiState(
    val recipes: List<Recipe> = emptyList(),
    val quickFilter: QuickFilter = QuickFilter.ALL,
    val meal: MealType? = null,
    val unreadNotifications: Int = 0,
    val user: User = User(),
)

class HomeViewModel(
    private val recipeRepository: RecipeRepository,
    userRepository: UserRepository,
    feedRepository: FeedRepository,
) : ViewModel() {
    private val quickFilter = MutableStateFlow(QuickFilter.ALL)
    private val meal = MutableStateFlow<MealType?>(null)

    val state: StateFlow<HomeUiState> = combine(
        quickFilter,
        meal,
        userRepository.preferences,
        feedRepository.notifications,
        userRepository.user,
    ) { filter, selectedMeal, preferences, notifications, user ->
        val recipes = recipeRepository.recipes
            .filter { recipe ->
                when (filter) {
                    QuickFilter.ALL -> true
                    QuickFilter.QUICK -> recipe.cookingTime <= 15
                    QuickFilter.EASY -> recipe.difficulty == Difficulty.EASY
                    QuickFilter.VEGETARIAN -> DietTag.VEGETARIAN in recipe.diets
                }
            }
            .filter { selectedMeal == null || selectedMeal in it.meals }
            // Món hợp khẩu vị đã chọn được đưa lên trước.
            .sortedByDescending { it.cuisine in preferences.cuisines }
        HomeUiState(recipes, filter, selectedMeal, notifications.count { !it.read }, user)
    }.asState(this, HomeUiState())

    fun selectQuickFilter(filter: QuickFilter) {
        quickFilter.value = filter
    }

    fun selectMeal(value: MealType) {
        meal.update { if (it == value) null else value }
    }
}

enum class TimeFilter(val label: String) {
    UNDER_15("Dưới 15p"), UNDER_30("30p"), OVER_60("60p+");

    fun matches(minutes: Int): Boolean = when (this) {
        UNDER_15 -> minutes <= 15
        UNDER_30 -> minutes <= 30
        OVER_60 -> minutes >= 60
    }
}

data class SearchUiState(
    val query: String = "",
    val time: TimeFilter? = null,
    val difficulty: Difficulty? = null,
    val diets: Set<DietTag> = emptySet(),
    val results: List<Recipe> = emptyList(),
)

class SearchViewModel(private val recipeRepository: RecipeRepository) : ViewModel() {
    private val _state = MutableStateFlow(applyFilters(SearchUiState()))
    val state: StateFlow<SearchUiState> = _state

    private fun applyFilters(input: SearchUiState): SearchUiState {
        val query = input.query.normalizedForSearch()
        val results = recipeRepository.recipes.filter { recipe ->
            (
                query.isEmpty() ||
                    recipe.name.normalizedForSearch().contains(query) ||
                    recipe.ingredients.any { it.name.normalizedForSearch().contains(query) }
                ) &&
                (input.time?.matches(recipe.cookingTime) ?: true) &&
                (input.difficulty == null || recipe.difficulty == input.difficulty) &&
                recipe.diets.containsAll(input.diets)
        }
        return input.copy(results = results)
    }

    private fun change(transform: (SearchUiState) -> SearchUiState) {
        _state.update { applyFilters(transform(it)) }
    }

    fun setQuery(value: String) = change { it.copy(query = value) }
    fun toggleTime(value: TimeFilter) = change { it.copy(time = if (it.time == value) null else value) }
    fun toggleDifficulty(value: Difficulty) = change { it.copy(difficulty = if (it.difficulty == value) null else value) }
    fun toggleDiet(value: DietTag) = change {
        it.copy(diets = if (value in it.diets) it.diets - value else it.diets + value)
    }
    fun clearFilters() = change { it.copy(time = null, difficulty = null, diets = emptySet()) }
}

enum class SavedFilter(val label: String) { ALL("Tất cả"), COOKED("Đã nấu"), WANT("Muốn thử") }

data class FavoritesUiState(
    val recipes: List<Recipe> = emptyList(),
    val totalCount: Int = 0,
    val filter: SavedFilter = SavedFilter.ALL,
    val collections: List<CollectionWithCount> = emptyList(),
    val selectedCollection: Long? = null,
)

class FavoritesViewModel(private val repository: RecipeRepository) : ViewModel() {
    private val filter = MutableStateFlow(SavedFilter.ALL)
    private val selectedCollection = MutableStateFlow<Long?>(null)
    private val selection = combine(filter, selectedCollection) { saved, collection -> saved to collection }

    val state: StateFlow<FavoritesUiState> = combine(
        repository.favoriteIds,
        repository.history,
        repository.collections,
        repository.collectionRecipes,
        selection,
    ) { favorites, history, collections, links, (saved, collection) ->
        val cooked = history.map { it.recipeId }.toSet()
        val inCollection = links.filter { it.collectionId == collection }.map { it.recipeId }.toSet()
        val recipes = favorites.mapNotNull(repository::recipe)
            .filter { collection == null || it.id in inCollection }
            .filter {
                when (saved) {
                    SavedFilter.ALL -> true
                    SavedFilter.COOKED -> it.id in cooked
                    SavedFilter.WANT -> it.id !in cooked
                }
            }
        FavoritesUiState(recipes, favorites.size, saved, collections, collection)
    }.asState(this, FavoritesUiState())

    fun selectFilter(value: SavedFilter) {
        filter.value = value
    }

    fun selectCollection(id: Long) {
        selectedCollection.update { if (it == id) null else id }
    }

    fun addCollection(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.addCollection(name) }
    }
}

data class CollectionOption(val id: Long, val name: String, val contains: Boolean)

data class RecipeDetailUiState(
    val recipe: Recipe? = null,
    val isFavorite: Boolean = false,
    val isLocked: Boolean = false,
    val checkedIngredientIds: Set<Int> = emptySet(),
    val collections: List<CollectionOption> = emptyList(),
)

class RecipeDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val recipeRepository: RecipeRepository,
    private val pantryRepository: PantryRepository,
    userRepository: UserRepository,
) : ViewModel() {
    private val recipeId: Int = savedStateHandle.get<Int>("recipeId") ?: -1
    private val recipe: Recipe? = recipeRepository.recipe(recipeId)
    private val overrides = MutableStateFlow<Map<Int, Boolean>>(emptyMap())

    private val collectionOptions = combine(
        recipeRepository.collections,
        recipeRepository.collectionRecipes,
    ) { collections, links ->
        collections.map { item ->
            CollectionOption(
                item.collection.id,
                item.collection.name,
                links.any { it.collectionId == item.collection.id && it.recipeId == recipeId },
            )
        }
    }

    val state: StateFlow<RecipeDetailUiState> = combine(
        recipeRepository.favoriteIds,
        pantryRepository.pantryItems,
        userRepository.user,
        overrides,
        collectionOptions,
    ) { favorites, pantry, user, manual, collections ->
        val owned = pantry.filter { it.isAvailable }.map { it.name.normalizedForSearch() }.toSet()
        val checked = recipe?.ingredients.orEmpty()
            .filter { manual[it.id] ?: (it.name.normalizedForSearch() in owned) }
            .map { it.id }
            .toSet()
        RecipeDetailUiState(
            recipe = recipe,
            isFavorite = recipeId in favorites,
            isLocked = recipe?.isPremium == true && !user.isPremium,
            checkedIngredientIds = checked,
            collections = collections,
        )
    }.asState(this, RecipeDetailUiState(recipe = recipe))

    fun toggleFavorite() {
        viewModelScope.launch {
            recipeRepository.setFavorite(recipeId, recipeId !in recipeRepository.favoriteIds.first())
        }
    }

    fun setIngredientChecked(id: Int, checked: Boolean) {
        overrides.update { it + (id to checked) }
    }

    fun setInCollection(collectionId: Long, inCollection: Boolean) {
        viewModelScope.launch { recipeRepository.setInCollection(collectionId, recipeId, inCollection) }
    }

    /** Thêm các nguyên liệu chưa được đánh dấu "đã có" vào danh sách đi chợ. */
    fun addMissingToShopping(onResult: (Int) -> Unit) {
        val target = recipe ?: return
        val checked = state.value.checkedIngredientIds
        val missing: List<Ingredient> = target.ingredients.filter { it.id !in checked }
        viewModelScope.launch { onResult(pantryRepository.addToShopping(missing, target.name)) }
    }
}
