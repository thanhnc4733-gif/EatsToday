package com.example.eatstoday.feature.recipe.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eatstoday.core.components.*
import com.example.eatstoday.core.utils.AppViewModelProvider
import com.example.eatstoday.data.model.MealType
import com.example.eatstoday.feature.recipe.presentation.viewmodel.HomeViewModel
import com.example.eatstoday.feature.recipe.presentation.viewmodel.QuickFilter
import java.util.Calendar

private fun greeting(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..10 -> "Chào buổi sáng"
    in 11..13 -> "Chào buổi trưa"
    in 14..17 -> "Chào buổi chiều"
    else -> "Chào buổi tối"
}

@Composable
fun HomeScreen(
    onRecipeClick: (Int) -> Unit,
    onSearch: () -> Unit,
    onFridge: () -> Unit,
    onShopping: () -> Unit,
    onSaved: () -> Unit,
    onNotifications: () -> Unit,
    onProfile: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ScreenColumn {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            UserAvatar(
                state.user.avatarText,
                photoPath = state.user.avatarPath,
                modifier = Modifier.clip(CircleShape).clickable(onClickLabel = "Mở hồ sơ", onClick = onProfile),
            )
            Spacer(Modifier.width(6.dp))
            Column(Modifier.weight(1f)) {
                Text(greeting(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Hôm nay nấu gì?", style = MaterialTheme.typography.titleLarge)
            }
            IconButton(onClick = onSaved) { Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Món đã lưu") }
            IconButton(onClick = onShopping) { Icon(Icons.Outlined.ShoppingCart, contentDescription = "Danh sách đi chợ") }
            IconButton(onClick = onNotifications) {
                Box {
                    Icon(Icons.Outlined.Notifications, contentDescription = "Thông báo, ${state.unreadNotifications} chưa đọc")
                    if (state.unreadNotifications > 0) {
                        Box(
                            Modifier
                                .align(Alignment.TopEnd)
                                .size(9.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                        )
                    }
                }
            }
        }
        EatsCard(onClick = onSearch, modifier = Modifier.fillMaxWidth()) {
            Text("Tìm món, nguyên liệu...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        ChipRow {
            QuickFilter.entries.forEach { filter ->
                EatsChip(filter.label, filter == state.quickFilter, { viewModel.selectQuickFilter(filter) })
            }
        }
        Surface(
            onClick = onFridge,
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Tủ lạnh nhà tôi", style = MaterialTheme.typography.titleMedium)
                    Text("Chọn nguyên liệu đang có, app gợi ý món nấu được", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.width(10.dp))
                Text("Thử ngay", style = MaterialTheme.typography.labelMedium)
            }
        }
        Text("Theo bữa", style = MaterialTheme.typography.titleMedium)
        ChipRow {
            MealType.entries.forEach { meal ->
                EatsChip(meal.label, meal == state.meal, { viewModel.selectMeal(meal) })
            }
        }
        if (state.recipes.isEmpty()) {
            EmptyState("Chưa có món", "Không có món phù hợp", "Thử đổi bộ lọc để xem thêm món.")
        } else {
            RecipeGrid(state.recipes, { onRecipeClick(it.id) })
        }
    }
}
