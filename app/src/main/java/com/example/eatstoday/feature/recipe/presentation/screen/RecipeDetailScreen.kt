package com.example.eatstoday.feature.recipe.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eatstoday.core.components.*
import com.example.eatstoday.core.utils.AppViewModelProvider
import com.example.eatstoday.feature.recipe.presentation.viewmodel.RecipeDetailViewModel
import kotlinx.coroutines.launch

@Composable
fun RecipeDetailScreen(
    onBack: () -> Unit,
    onStartCooking: (Int) -> Unit,
    onUpgrade: () -> Unit,
    viewModel: RecipeDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val recipe = state.recipe
    if (recipe == null) {
        EmptyState("Không có món", "Không tìm thấy công thức", "Món này không còn tồn tại.", actionLabel = "Quay lại", onAction = onBack)
        return
    }
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    var showSteps by rememberSaveable { mutableStateOf(false) }
    var showCollections by rememberSaveable { mutableStateOf(false) }

    ScreenColumn(
        bottomBar = {
            if (state.isLocked) {
                PrimaryButton("Mở khóa bằng Premium", onUpgrade)
            } else {
                PrimaryButton("Bắt đầu nấu", { onStartCooking(recipe.id) })
            }
        },
    ) {
        TopActions {
            IconButton(onClick = { showCollections = true }) {
                Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Thêm vào bộ sưu tập")
            }
            IconButton(onClick = viewModel::toggleFavorite) {
                Icon(
                    imageVector = if (state.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (state.isFavorite) "Bỏ yêu thích" else "Thêm yêu thích",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        FoodImage("Ảnh món lớn", Modifier.fillMaxWidth().height(220.dp), radius = 24.dp)
        Text(recipe.name, style = MaterialTheme.typography.headlineMedium)
        Text(
            "${recipe.cookingTime} phút · ${recipe.difficulty.label} · ${recipe.servings} người · ${recipe.rating} sao",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(recipe.description, style = MaterialTheme.typography.bodyMedium)
        if (state.isLocked) {
            EatsCard(Modifier.fillMaxWidth()) {
                Text("Công thức Premium", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Nâng cấp để xem nguyên liệu và các bước của món này.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@ScreenColumn
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            EatsChip("Nguyên liệu", !showSteps, { showSteps = false }, Modifier.weight(1f))
            EatsChip("Các bước", showSteps, { showSteps = true }, Modifier.weight(1f))
        }
        EatsCard(Modifier.fillMaxWidth()) {
            if (showSteps) {
                recipe.steps.forEachIndexed { index, step ->
                    Text("${index + 1}. ${step.title}", style = MaterialTheme.typography.titleSmall)
                    Text(step.description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 10.dp))
                }
            } else {
                recipe.ingredients.forEach { ingredient ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        EatsCheckbox(
                            checked = ingredient.id in state.checkedIngredientIds,
                            onCheckedChange = { viewModel.setIngredientChecked(ingredient.id, it) },
                            contentDescription = "Đã có ${ingredient.name}",
                        )
                        Text(ingredient.name, modifier = Modifier.weight(1f))
                        Text(ingredient.quantity, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        if (!showSteps) {
            SecondaryButton("Thêm vào danh sách đi chợ", {
                viewModel.addMissingToShopping { added ->
                    scope.launch {
                        snackbar.showSnackbar(
                            if (added > 0) "Đã thêm $added món vào danh sách đi chợ" else "Không có món mới cần thêm",
                        )
                    }
                }
            })
        }
    }

    if (showCollections) {
        AlertDialog(
            onDismissRequest = { showCollections = false },
            title = { Text("Thêm vào bộ sưu tập") },
            text = {
                Column {
                    if (state.collections.isEmpty()) Text("Hãy tạo bộ sưu tập ở màn hình Món yêu thích.")
                    state.collections.forEach { option ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            EatsCheckbox(
                                checked = option.contains,
                                onCheckedChange = { viewModel.setInCollection(option.id, it) },
                                contentDescription = option.name,
                            )
                            Text(option.name)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showCollections = false }) { Text("Xong") } },
        )
    }
}
