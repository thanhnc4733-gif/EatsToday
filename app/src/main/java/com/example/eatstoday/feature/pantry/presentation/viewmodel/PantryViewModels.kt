package com.example.eatstoday.feature.pantry.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eatstoday.core.utils.formatQuantity
import com.example.eatstoday.core.utils.normalizedForSearch
import com.example.eatstoday.data.model.PantryItem
import com.example.eatstoday.data.model.Recipe
import com.example.eatstoday.data.repository.PantryRepository
import com.example.eatstoday.data.repository.RecipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecipeMatch(val recipe: Recipe, val missing: Int)

data class PantryUiState(
    val items: List<PantryItem> = emptyList(),
    val matches: List<RecipeMatch> = emptyList(),
) {
    val readyCount: Int get() = matches.count { it.missing == 0 }
}

class PantryViewModel(
    private val pantryRepository: PantryRepository,
    private val recipeRepository: RecipeRepository,
) : ViewModel() {
    val state: StateFlow<PantryUiState> = pantryRepository.pantryItems
        .map { items ->
            val owned = items.filter { it.isAvailable }.map { it.name.normalizedForSearch() }.toSet()
            val matches = recipeRepository.recipes
                .mapNotNull { recipe ->
                    val required = recipe.ingredients.filterNot { it.isStaple }
                    val have = required.count { it.name.normalizedForSearch() in owned }
                    // Chỉ gợi ý món có dùng ít nhất một nguyên liệu đang có và thiếu tối đa hai món.
                    if (have > 0 && required.size - have <= 2) RecipeMatch(recipe, required.size - have) else null
                }
                .sortedBy { it.missing }
            PantryUiState(items, matches)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PantryUiState())

    fun toggleAvailable(item: PantryItem) {
        viewModelScope.launch { pantryRepository.setAvailable(item, !item.isAvailable) }
    }

    fun delete(item: PantryItem) {
        viewModelScope.launch { pantryRepository.deletePantryItem(item) }
    }
}

class IngredientFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: PantryRepository,
) : ViewModel() {
    private val itemId: Long = savedStateHandle.get<Long>("itemId") ?: -1L
    val isEditing: Boolean = itemId > 0

    var name by mutableStateOf("")
    var quantity by mutableStateOf("")
    var unit by mutableStateOf("")
    var nameError by mutableStateOf<String?>(null)
        private set
    var quantityError by mutableStateOf<String?>(null)
        private set

    private var original: PantryItem? = null
    private var saving = false

    init {
        if (isEditing) {
            viewModelScope.launch {
                repository.pantryItem(itemId)?.let { item ->
                    original = item
                    name = item.name
                    quantity = item.quantity?.let(::formatQuantity).orEmpty()
                    unit = item.unit
                }
            }
        }
    }

    fun save(onSaved: () -> Unit) {
        if (saving) return
        val trimmedName = name.trim()
        val rawQuantity = quantity.trim().replace(',', '.')
        val parsed = rawQuantity.toDoubleOrNull()
        nameError = if (trimmedName.isEmpty()) "Vui lòng nhập tên nguyên liệu" else null
        quantityError = when {
            rawQuantity.isEmpty() -> null
            parsed == null -> "Số lượng phải là một số"
            parsed <= 0 -> "Số lượng phải lớn hơn 0"
            else -> null
        }
        if (nameError != null || quantityError != null) return
        saving = true
        viewModelScope.launch {
            val item = (original ?: PantryItem(name = trimmedName)).copy(
                name = trimmedName,
                quantity = parsed,
                unit = unit.trim(),
            )
            if (repository.savePantryItem(item)) {
                onSaved()
            } else {
                nameError = "Nguyên liệu này đã có trong tủ lạnh"
            }
            saving = false
        }
    }
}
