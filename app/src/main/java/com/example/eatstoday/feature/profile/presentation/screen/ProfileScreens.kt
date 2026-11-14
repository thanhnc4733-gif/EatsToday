package com.example.eatstoday.feature.profile.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eatstoday.core.components.*
import com.example.eatstoday.core.utils.AppViewModelProvider
import com.example.eatstoday.core.utils.isValidEmail
import com.example.eatstoday.core.utils.relativeTime
import com.example.eatstoday.data.model.AppThemeMode
import com.example.eatstoday.data.model.TextSizeOption
import com.example.eatstoday.feature.profile.presentation.viewmodel.ProfileViewModel

@Composable
private fun MenuRow(title: String, value: String = "", onClick: (() -> Unit)? = null) {
    EatsCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, modifier = Modifier.weight(1f))
            Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SwitchRow(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    EatsCard(modifier = Modifier.fillMaxWidth(), contentPadding = 8.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 6.dp)) {
            Text(title, modifier = Modifier.weight(1f))
            Switch(checked = checked, onCheckedChange = onChange)
        }
    }
}

@Composable
fun ProfileScreen(
    onUpgrade: () -> Unit,
    onEdit: () -> Unit,
    onFriends: () -> Unit,
    onFavorites: () -> Unit,
    onShopping: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    var confirmLogout by rememberSaveable { mutableStateOf(false) }
    val user by viewModel.user.collectAsStateWithLifecycle()
    val friendCount by viewModel.friendCount.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    ScreenColumn {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            UserAvatar(
                user.avatarText,
                size = 72.dp,
                photoPath = user.avatarPath,
                modifier = Modifier.clip(CircleShape).clickable(onClickLabel = "Sửa hồ sơ", onClick = onEdit),
            )
            Column(Modifier.weight(1f)) {
                Text(user.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    "Đã nấu ${history.size} món · $friendCount bạn bè",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (!user.isPremium) {
            Surface(
                onClick = onUpgrade,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Nâng cấp Premium", style = MaterialTheme.typography.titleMedium)
                        Text("Không quảng cáo, công thức đặc biệt", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("Xem", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        MenuRow("Sửa hồ sơ", onClick = onEdit)
        MenuRow("Bạn bè", friendCount.toString(), onFriends)
        MenuRow("Món yêu thích", onClick = onFavorites)
        MenuRow("Danh sách đi chợ", onClick = onShopping)
        MenuRow("Lịch sử nấu", history.size.toString(), onHistory)
        MenuRow("Gói đăng ký", if (user.isPremium) "Premium (mô phỏng)" else "Miễn phí", onUpgrade)
        MenuRow("Cài đặt", onClick = onSettings)
        SecondaryButton("Đăng xuất", { confirmLogout = true })
    }
    if (confirmLogout) {
        LogoutDialog(
            onConfirm = {
                confirmLogout = false
                viewModel.logout(onLoggedOut)
            },
            onDismiss = { confirmLogout = false },
        )
    }
}

@Composable
private fun LogoutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    ConfirmationDialog(
        title = "Đăng xuất?",
        message = "Dữ liệu đã lưu vẫn còn trên thiết bị này.",
        confirmLabel = "Đăng xuất",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    var name by rememberSaveable(user.name) { mutableStateOf(user.name) }
    var email by rememberSaveable(user.email) { mutableStateOf(user.email) }
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    ScreenColumn(
        bottomBar = {
            PrimaryButton("Lưu", {
                nameError = if (name.isBlank()) "Vui lòng nhập tên" else null
                emailError = if (email.isNotBlank() && !isValidEmail(email)) "Email chưa đúng định dạng" else null
                if (nameError == null && emailError == null) {
                    viewModel.updateProfile(name, email)
                    onBack()
                }
            })
        },
    ) {
        Text("Sửa hồ sơ", style = MaterialTheme.typography.headlineMedium)
        AvatarEditor(photoPath = user.avatarPath, text = user.avatarText, onChange = viewModel::setAvatar)
        CustomTextField(name, { name = it }, "Tên hiển thị", error = nameError)
        CustomTextField(email, { email = it }, "Email", error = emailError, keyboardType = KeyboardType.Email)
    }
}

@Composable
fun CookHistoryScreen(
    onBack: () -> Unit,
    onRecipeClick: (Int) -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val history by viewModel.history.collectAsStateWithLifecycle()
    ScreenColumn {
        Text("Lịch sử nấu", style = MaterialTheme.typography.headlineMedium)
        if (history.isEmpty()) {
            EmptyState("Chưa nấu món nào", "Chưa có lịch sử", "Nấu xong một món, nó sẽ xuất hiện ở đây.")
        }
        history.forEach { item ->
            MenuRow(item.recipeName, relativeTime(item.cookedAt), { onRecipeClick(item.recipeId) })
        }
    }
}

@Composable
fun SettingsScreen(
    onPermissions: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var showTheme by rememberSaveable { mutableStateOf(false) }
    var showTextSize by rememberSaveable { mutableStateOf(false) }
    var showTerms by rememberSaveable { mutableStateOf(false) }
    var confirmLogout by rememberSaveable { mutableStateOf(false) }
    ScreenColumn(bottomBar = { SecondaryButton("Đăng xuất", { confirmLogout = true }) }) {
        Text("Cài đặt", style = MaterialTheme.typography.headlineMedium)
        MenuRow("Ngôn ngữ", "Tiếng Việt")
        MenuRow("Cỡ chữ", settings.textSize.label) { showTextSize = true }
        MenuRow("Giao diện", settings.theme.label) { showTheme = true }
        SwitchRow("Thông báo", settings.notificationsEnabled, viewModel::setNotifications)
        SwitchRow("Giữ màn hình sáng khi nấu", settings.keepScreenOn, viewModel::setKeepScreenOn)
        MenuRow("Quyền truy cập", onClick = onPermissions)
        MenuRow("Điều khoản và bảo mật") { showTerms = true }
    }
    if (showTheme) {
        OptionDialog("Giao diện", AppThemeMode.entries.map { it.label }, { index ->
            viewModel.setTheme(AppThemeMode.entries[index])
            showTheme = false
        }, { showTheme = false })
    }
    if (showTextSize) {
        OptionDialog("Cỡ chữ", TextSizeOption.entries.map { it.label }, { index ->
            viewModel.setTextSize(TextSizeOption.entries[index])
            showTextSize = false
        }, { showTextSize = false })
    }
    if (showTerms) {
        AlertDialog(
            onDismissRequest = { showTerms = false },
            title = { Text("Điều khoản và bảo mật") },
            text = { Text("Phiên bản này lưu toàn bộ dữ liệu trên thiết bị của bạn và không gửi đi đâu. [Thêm nội dung điều khoản chính thức trước khi phát hành]") },
            confirmButton = { TextButton(onClick = { showTerms = false }) { Text("Đóng") } },
        )
    }
    if (confirmLogout) {
        LogoutDialog(
            onConfirm = {
                confirmLogout = false
                viewModel.logout(onLoggedOut)
            },
            onDismiss = { confirmLogout = false },
        )
    }
}

@Composable
private fun OptionDialog(title: String, options: List<String>, onSelect: (Int) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEachIndexed { index, label ->
                    TextButton(onClick = { onSelect(index) }, modifier = Modifier.fillMaxWidth()) { Text(label) }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Đóng") } },
    )
}

@Composable
fun NotificationsScreen(
    onOpenPost: (String) -> Unit,
    onSignIn: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val signedIn by viewModel.signedIn.collectAsStateWithLifecycle()
    DisposableEffect(Unit) {
        onDispose { viewModel.markNotificationsRead() }
    }
    ScreenColumn {
        Text("Thông báo", style = MaterialTheme.typography.headlineMedium)
        if (!signedIn) {
            EmptyState(
                "Cần tài khoản",
                "Đăng nhập để nhận thông báo",
                "Khi bạn bè đăng món, bình luận hay chấm sao món của bạn, thông báo sẽ hiện ở đây.",
                actionLabel = "Đăng nhập",
                onAction = onSignIn,
            )
            return@ScreenColumn
        }
        if (!settings.notificationsEnabled) {
            EatsCard(Modifier.fillMaxWidth()) {
                Text("Thông báo trên điện thoại đang tắt trong Cài đặt. Danh sách bên dưới vẫn cập nhật.", style = MaterialTheme.typography.bodySmall)
            }
        }
        notifications.forEach { item ->
            EatsCard(onClick = { onOpenPost(item.postId) }, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    UserAvatar(item.avatarText, highlighted = !item.read)
                    Column(Modifier.weight(1f)) {
                        Text("${item.actorName} ${item.message}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            relativeTime(item.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        if (notifications.isEmpty()) {
            EmptyState("Chưa có gì", "Chưa có thông báo", "Khi bạn bè đăng món, bình luận hay chấm sao món của bạn, thông báo sẽ hiện ở đây.")
        } else {
            EatsCard(Modifier.fillMaxWidth()) {
                Text("Hết thông báo mới", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
