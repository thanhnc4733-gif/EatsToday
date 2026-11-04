package com.example.eatstoday.feature.recipe.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eatstoday.core.components.*
import com.example.eatstoday.core.utils.AppViewModelProvider
import com.example.eatstoday.data.model.DietTag
import com.example.eatstoday.data.model.Difficulty
import com.example.eatstoday.feature.recipe.presentation.viewmodel.SearchViewModel
import com.example.eatstoday.feature.recipe.presentation.viewmodel.TimeFilter

@Composable
fun SearchScreen(
    onRecipeClick: (Int) -> Unit,
    viewModel: SearchViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showResults by rememberSaveable { mutableStateOf(false) }
    ScreenColumn(
        bottomBar = if (showResults) {
            null
        } else {
            { PrimaryButton("Xem ${state.results.size} món", { showResults = true }) }
        },
    ) {
        Text("Tìm món", style = MaterialTheme.typography.headlineMedium)
        EatsSearchBar(state.query, viewModel::setQuery, "Bún bò, thịt bằm, canh...")
        if (showResults) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${state.results.size} món", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                LinkButton("Sửa bộ lọc", { showResults = false })
            }
            if (state.results.isEmpty()) {
                EmptyState("Không thấy món", "Không tìm thấy món phù hợp", "Thử từ khóa khác hoặc bỏ bớt bộ lọc.", actionLabel = "Xóa bộ lọc", onAction = viewModel::clearFilters)
            } else {
                RecipeGrid(state.results, { onRecipeClick(it.id) })
            }
        } else {
            Text("Lọc", style = MaterialTheme.typography.titleMedium)
            FilterLabel("Thời gian nấu")
            ChipRow {
                TimeFilter.entries.forEach { EatsChip(it.label, it == state.time, { viewModel.toggleTime(it) }) }
            }
            FilterLabel("Độ khó")
            ChipRow {
                Difficulty.entries.forEach { EatsChip(it.label, it == state.difficulty, { viewModel.toggleDifficulty(it) }) }
            }
            FilterLabel("Chế độ ăn")
            ChipFlow {
                DietTag.entries.forEach { EatsChip(it.label, it in state.diets, { viewModel.toggleDiet(it) }) }
            }
        }
    }
}

@Composable
private fun FilterLabel(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
