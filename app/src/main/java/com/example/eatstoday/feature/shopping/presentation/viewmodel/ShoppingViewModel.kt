package com.example.eatstoday.feature.shopping.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eatstoday.data.model.ShoppingItem
import com.example.eatstoday.data.repository.PantryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ShoppingUiState(
    val isLoaded: Boolean = false,
    val groups: Map<String, List<ShoppingItem>> = emptyMap(),
    val total: Int = 0,
    val bought: Int = 0,
    val sourceRecipes: List<String> = emptyList(),
)

class ShoppingViewModel(private val repository: PantryRepository) : ViewModel() {
    val state: StateFlow<ShoppingUiState> = repository.shoppingItems
        .map { items ->
            ShoppingUiState(
                isLoaded = true,
                groups = items.groupBy { it.category },
                total = items.size,
                bought = items.count { it.isBought },
                sourceRecipes = items.mapNotNull { it.recipeName }.distinct(),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShoppingUiState())

    fun toggleBought(item: ShoppingItem) {
        viewModelScope.launch { repository.setBought(item, !item.isBought) }
    }

    fun delete(item: ShoppingItem) {
        viewModelScope.launch { repository.deleteShoppingItem(item) }
    }

    fun add(name: String, quantity: String, category: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch { onResult(repository.addShoppingItem(name.trim(), quantity.trim(), category)) }
    }

    fun moveBoughtToPantry(onResult: (Int) -> Unit) {
        viewModelScope.launch { onResult(repository.moveBoughtToPantry()) }
    }
}
