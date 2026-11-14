package com.example.eatstoday.core.utils

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.eatstoday.AppContainer
import com.example.eatstoday.EatsTodayApp
import com.example.eatstoday.feature.auth.presentation.viewmodel.AuthViewModel
import com.example.eatstoday.feature.pantry.presentation.viewmodel.IngredientFormViewModel
import com.example.eatstoday.feature.pantry.presentation.viewmodel.PantryViewModel
import com.example.eatstoday.feature.profile.presentation.viewmodel.ProfileViewModel
import com.example.eatstoday.feature.recipe.presentation.viewmodel.CookingViewModel
import com.example.eatstoday.feature.recipe.presentation.viewmodel.FavoritesViewModel
import com.example.eatstoday.feature.recipe.presentation.viewmodel.HomeViewModel
import com.example.eatstoday.feature.recipe.presentation.viewmodel.RecipeDetailViewModel
import com.example.eatstoday.feature.recipe.presentation.viewmodel.SearchViewModel
import com.example.eatstoday.feature.shopping.presentation.viewmodel.ShoppingViewModel
import com.example.eatstoday.feature.social.presentation.viewmodel.FriendsViewModel
import com.example.eatstoday.feature.social.presentation.viewmodel.PostViewModel

private fun CreationExtras.container(): AppContainer =
    (this[APPLICATION_KEY] as EatsTodayApp).container

object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer { AuthViewModel(container().userRepository) }
        initializer { HomeViewModel(container().recipeRepository, container().userRepository, container().feedRepository) }
        initializer { SearchViewModel(container().recipeRepository) }
        initializer { FavoritesViewModel(container().recipeRepository) }
        initializer {
            RecipeDetailViewModel(
                createSavedStateHandle(),
                container().recipeRepository,
                container().pantryRepository,
                container().userRepository,
            )
        }
        initializer { CookingViewModel(createSavedStateHandle(), container().recipeRepository, container().userRepository) }
        initializer { PantryViewModel(container().pantryRepository, container().recipeRepository) }
        initializer { IngredientFormViewModel(createSavedStateHandle(), container().pantryRepository) }
        initializer { ShoppingViewModel(container().pantryRepository) }
        initializer { PostViewModel(createSavedStateHandle(), container().feedRepository, container().userRepository, container().recipeRepository) }
        initializer { FriendsViewModel(createSavedStateHandle(), container().friendRepository, container().userRepository) }
        initializer {
            ProfileViewModel(
                container().userRepository,
                container().feedRepository,
                container().friendRepository,
                container().recipeRepository,
                container().applicationScope,
            )
        }
    }
}
