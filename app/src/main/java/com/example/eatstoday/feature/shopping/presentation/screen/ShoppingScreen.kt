package com.example.eatstoday.feature.shopping.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.dp
import com.example.eatstoday.core.components.*
import com.example.eatstoday.core.utils.AppViewModelProvider
import com.example.eatstoday.data.model.IngredientCategory
import com.example.eatstoday.feature.shopping.presentation.viewmodel.ShoppingViewModel
import kotlinx.coroutines.launch

@Composable
fun ShoppingScreen(
    onFindRecipes: () -> Unit,
    viewModel: ShoppingViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    var showAdd by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var quantity by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(IngredientCategory.OTHER) }

    ScreenColumn(
        bottomBar = if (state.bought > 0) {
            {
                PrimaryButton("Cập nhật ${state.bought} món đã mua vào tủ lạnh", {
                    viewModel.moveBoughtToPantry { moved ->
                        scope.launch { snackbar.showSnackbar("Đã chuyển $moved món vào tủ lạnh") }
                    }
                })
            }
        } else {
            null
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Danh sách đi chợ", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            IconButton(onClick = { showAdd = true }) { Icon(Icons.Outlined.Add, contentDescription = "Thêm món cần mua") }
        }
        if (state.isLoaded && state.total == 0) {
            EmptyState(
                "Giỏ trống",
                "Chưa có gì cần mua",
                "Mở một món và bấm \"Thêm vào danh sách đi chợ\" để tạo danh sách.",
                actionLabel = "Tìm món",
                onAction = onFindRecipes,
            )
        } else if (state.total > 0) {
            val source = if (state.sourceRecipes.isEmpty()) "" else "Từ món: ${state.sourceRecipes.joinToString()} · "
            Text(
                "$source${state.bought}/${state.total} đã mua",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            state.groups.forEach { (group, items) ->
                Text(group, style = MaterialTheme.typography.titleMedium)
                EatsCard(Modifier.fillMaxWidth(), contentPadding = 6.dp) {
                    items.forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            EatsCheckbox(item.isBought, { viewModel.toggleBought(item) }, "Đã mua ${item.name}")
                            Text(
                                item.name,
                                modifier = Modifier.weight(1f),
                                color = if (item.isBought) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                textDecoration = if (item.isBought) TextDecoration.LineThrough else null,
                            )
                            Text(item.quantity, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            IconButton(onClick = { viewModel.delete(item) }) {
                                Icon(Icons.Outlined.Close, contentDescription = "Xóa ${item.name}")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Thêm món cần mua") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CustomTextField(name, { name = it }, "Tên món")
                    CustomTextField(quantity, { quantity = it }, "Số lượng, ví dụ 500 g")
                    ChipFlow {
                        IngredientCategory.all.forEach { EatsChip(it, it == category, { category = it }) }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = name.isNotBlank(),
                    onClick = {
                        viewModel.add(name, quantity, category) { added ->
                            if (!added) scope.launch { snackbar.showSnackbar("Món này đã có trong danh sách") }
                        }
                        name = ""
                        quantity = ""
                        showAdd = false
                    },
                ) { Text("Thêm") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Hủy") } },
        )
    }
}
