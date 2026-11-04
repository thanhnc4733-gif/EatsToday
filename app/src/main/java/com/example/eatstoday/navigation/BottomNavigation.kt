package com.example.eatstoday.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomTab(val route: String, val label: String, val icon: ImageVector) {
    HOME(AppRoute.HOME, "Trang chủ", Icons.Filled.Home),
    SEARCH(AppRoute.SEARCH, "Khám phá", Icons.Filled.Search),
    PANTRY(AppRoute.PANTRY, "Tủ lạnh", Icons.Filled.Kitchen),
    FEED(AppRoute.FEED, "Bạn bè", Icons.Filled.People),
    PROFILE(AppRoute.PROFILE, "Tôi", Icons.Filled.Person),
}

/** Trả về tab đang được chọn; null nghĩa là màn hình này không hiện thanh điều hướng. */
fun tabForRoute(route: String?): BottomTab? = when (route) {
    AppRoute.HOME, AppRoute.SAVED, AppRoute.NOTIFICATIONS -> BottomTab.HOME
    AppRoute.SEARCH -> BottomTab.SEARCH
    AppRoute.PANTRY, AppRoute.SHOPPING -> BottomTab.PANTRY
    AppRoute.FEED, AppRoute.FRIENDS, AppRoute.ADD_FRIEND, AppRoute.REQUESTS -> BottomTab.FEED
    AppRoute.PROFILE, AppRoute.FAVORITES -> BottomTab.PROFILE
    else -> null
}

@Composable
fun EatsBottomBar(current: BottomTab, onSelect: (BottomTab) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        BottomTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == current,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
            )
        }
    }
}
