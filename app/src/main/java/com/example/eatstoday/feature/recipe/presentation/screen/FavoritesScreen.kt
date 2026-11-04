package com.example.eatstoday.feature.recipe.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eatstoday.core.components.*
import com.example.eatstoday.core.utils.AppViewModelProvider
import com.example.eatstoday.feature.recipe.presentation.viewmodel.FavoritesViewModel
import com.example.eatstoday.feature.recipe.presentation.viewmodel.SavedFilter

/** Món đã lưu: chế độ xem gọn, không có bộ sưu tập. */
@Composable
fun SavedScreen(
    onRecipeClick: (Int) -> Unit,
    viewModel: FavoritesViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ScreenColumn {
        Text("Món đã lưu", style = MaterialTheme.typography.headlineMedium)
        ChipRow {
            SavedFilter.entries.forEach { EatsChip(it.label, it == state.filter, { viewModel.selectFilter(it) }) }
        }
        RecipeGrid(state.recipes, { onRecipeClick(it.id) }, imageHeight = 120.dp)
        EatsCard(Modifier.fillMaxWidth()) {
            Text(
                "Chạm vào biểu tượng trái tim ở mỗi món để thêm vào đây",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun FavoritesScreen(
    onRecipeClick: (Int) -> Unit,
    viewModel: FavoritesViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showNewCollection by rememberSaveable { mutableStateOf(false) }
    var newName by rememberSaveable { mutableStateOf("") }
    ScreenColumn {
        Text("Món yêu thích", style = MaterialTheme.typography.headlineMedium)
        ChipRow {
            SavedFilter.entries.forEach { filter ->
                val label = if (filter == SavedFilter.ALL) "${filter.label} (${state.totalCount})" else filter.label
                EatsChip(label, filter == state.filter, { viewModel.selectFilter(filter) })
            }
        }
        Text("Bộ sưu tập", style = MaterialTheme.typography.titleMedium)
        ChipFlow {
            state.collections.forEach { item ->
                EatsChip(
                    "${item.collection.name} · ${item.recipeCount} món",
                    item.collection.id == state.selectedCollection,
                    { viewModel.selectCollection(item.collection.id) },
                )
            }
            EatsChip("+ Thêm", false, { showNewCollection = true })
        }
        Text("Gần đây", style = MaterialTheme.typography.titleMedium)
        if (state.recipes.isEmpty()) {
            EmptyState("Chưa có món", "Chưa có món nào ở đây", "Chạm trái tim ở một món để lưu lại.")
        } else {
            RecipeGrid(state.recipes, { onRecipeClick(it.id) }, imageHeight = 104.dp)
        }
    }
    if (showNewCollection) {
        AlertDialog(
            onDismissRequest = { showNewCollection = false },
            title = { Text("Bộ sưu tập mới") },
            text = { CustomTextField(newName, { newName = it }, "Tên bộ sưu tập") },
            confirmButton = {
                TextButton(
                    enabled = newName.isNotBlank(),
                    onClick = {
                        viewModel.addCollection(newName)
                        newName = ""
                        showNewCollection = false
                    },
                ) { Text("Tạo") }
            },
            dismissButton = { TextButton(onClick = { showNewCollection = false }) { Text("Hủy") } },
        )
    }
}
