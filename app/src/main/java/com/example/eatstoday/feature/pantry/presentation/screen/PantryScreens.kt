package com.example.eatstoday.feature.pantry.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eatstoday.core.components.*
import com.example.eatstoday.core.utils.AppViewModelProvider
import com.example.eatstoday.core.utils.formatQuantity
import com.example.eatstoday.data.model.PantryItem
import com.example.eatstoday.feature.pantry.presentation.viewmodel.IngredientFormViewModel
import com.example.eatstoday.feature.pantry.presentation.viewmodel.PantryViewModel

@Composable
fun FridgeScreen(
    onRecipeClick: (Int) -> Unit,
    onManageIngredients: () -> Unit,
    onAddIngredient: () -> Unit,
    onShopping: () -> Unit,
    onSeeAllRecipes: () -> Unit,
    viewModel: PantryViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ScreenColumn(bottomBar = { PrimaryButton("Xem tất cả món", onSeeAllRecipes) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Tủ lạnh nhà tôi", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            IconButton(onClick = onShopping) { Icon(Icons.Outlined.ShoppingCart, contentDescription = "Danh sách đi chợ") }
            IconButton(onClick = onManageIngredients) { Icon(Icons.Outlined.Edit, contentDescription = "Quản lý nguyên liệu") }
        }
        Text("Chọn nguyên liệu bạn đang có", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ChipFlow {
            state.items.forEach { item ->
                EatsChip(item.name, item.isAvailable, { viewModel.toggleAvailable(item) })
            }
            EatsChip("+ Thêm", false, onAddIngredient)
        }
        Text("Nấu được ngay (${state.readyCount})", style = MaterialTheme.typography.titleMedium)
        if (state.matches.isEmpty()) {
            EatsCard(Modifier.fillMaxWidth()) {
                Text(
                    "Chọn thêm nguyên liệu để nhận gợi ý món.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        state.matches.forEach { match ->
            EatsCard(onClick = { onRecipeClick(match.recipe.id) }, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FoodImage(match.recipe.name, Modifier.size(64.dp), imageName = match.recipe.image)
                    Column(Modifier.weight(1f)) {
                        Text(match.recipe.name, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${match.recipe.cookingTime} phút · ${match.recipe.difficulty.label} · " +
                                if (match.missing == 0) "đủ nguyên liệu" else "thiếu ${match.missing} món",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private fun PantryItem.amountText(): String = listOfNotNull(quantity?.let(::formatQuantity), unit.ifBlank { null }).joinToString(" ")

@Composable
fun IngredientListScreen(
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: PantryViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<PantryItem?>(null) }
    ScreenColumn(bottomBar = { PrimaryButton("Thêm nguyên liệu", onAdd) }) {
        Text("Nguyên liệu của tôi", style = MaterialTheme.typography.headlineMedium)
        if (state.items.isEmpty()) {
            EmptyState("Tủ trống", "Tủ lạnh đang trống", "Thêm nguyên liệu đầu tiên để nhận gợi ý món.")
        }
        state.items.forEach { item ->
            EatsCard(Modifier.fillMaxWidth(), contentPadding = 6.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EatsCheckbox(item.isAvailable, { viewModel.toggleAvailable(item) }, "Đang có ${item.name}")
                    Column(Modifier.weight(1f)) {
                        Text(item.name, style = MaterialTheme.typography.bodyMedium)
                        val amount = item.amountText()
                        if (amount.isNotEmpty()) {
                            Text(amount, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    IconButton(onClick = { onEdit(item.id) }) { Icon(Icons.Outlined.Edit, contentDescription = "Sửa ${item.name}") }
                    IconButton(onClick = { pendingDelete = item }) { Icon(Icons.Outlined.Delete, contentDescription = "Xóa ${item.name}") }
                }
            }
        }
    }
    pendingDelete?.let { item ->
        ConfirmationDialog(
            title = "Xóa nguyên liệu?",
            message = "${item.name} sẽ bị xóa khỏi tủ lạnh.",
            confirmLabel = "Xóa",
            onConfirm = {
                viewModel.delete(item)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
fun IngredientFormScreen(
    onBack: () -> Unit,
    viewModel: IngredientFormViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    ScreenColumn(bottomBar = { PrimaryButton("Lưu", { viewModel.save(onBack) }) }) {
        Text(if (viewModel.isEditing) "Sửa nguyên liệu" else "Thêm nguyên liệu", style = MaterialTheme.typography.headlineMedium)
        CustomTextField(viewModel.name, { viewModel.name = it }, "Tên nguyên liệu", error = viewModel.nameError)
        CustomTextField(
            viewModel.quantity,
            { viewModel.quantity = it },
            "Số lượng (không bắt buộc)",
            error = viewModel.quantityError,
            keyboardType = KeyboardType.Decimal,
        )
        CustomTextField(viewModel.unit, { viewModel.unit = it }, "Đơn vị: quả, g, bó...")
    }
}
