package com.example.eatstoday.data.repository

import com.example.eatstoday.data.local.RecipeDao
import com.example.eatstoday.data.local.RecipeSampleData
import com.example.eatstoday.data.model.CollectionRecipe
import com.example.eatstoday.data.model.CollectionWithCount
import com.example.eatstoday.data.model.CookHistory
import com.example.eatstoday.data.model.Favorite
import com.example.eatstoday.data.model.Recipe
import com.example.eatstoday.data.model.RecipeCollection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecipeRepository(private val dao: RecipeDao) {
    val recipes: List<Recipe> = RecipeSampleData.recipes

    fun recipe(id: Int): Recipe? = recipes.firstOrNull { it.id == id }

    /** Danh sách ID yêu thích, mới nhất trước. */
    val favoriteIds: Flow<List<Int>> = dao.favorites().map { list -> list.map(Favorite::recipeId) }
    val history: Flow<List<CookHistory>> = dao.history()
    val collections: Flow<List<CollectionWithCount>> = dao.collections()
    val collectionRecipes: Flow<List<CollectionRecipe>> = dao.collectionRecipes()

    suspend fun setFavorite(recipeId: Int, favorite: Boolean) {
        if (favorite) dao.addFavorite(Favorite(recipeId, System.currentTimeMillis())) else dao.removeFavorite(recipeId)
    }

    suspend fun recordCooked(recipe: Recipe) {
        dao.addHistory(CookHistory(recipeId = recipe.id, recipeName = recipe.name, cookedAt = System.currentTimeMillis()))
    }

    suspend fun addCollection(name: String) {
        dao.addCollection(RecipeCollection(name = name.trim()))
    }

    suspend fun setInCollection(collectionId: Long, recipeId: Int, inCollection: Boolean) {
        if (inCollection) {
            dao.addToCollection(CollectionRecipe(collectionId, recipeId))
        } else {
            dao.removeFromCollection(collectionId, recipeId)
        }
    }
}
