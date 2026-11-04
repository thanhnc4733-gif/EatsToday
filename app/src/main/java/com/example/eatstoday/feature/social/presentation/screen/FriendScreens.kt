package com.example.eatstoday.feature.social.presentation.screen

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eatstoday.core.components.*
import com.example.eatstoday.core.utils.AppViewModelProvider
import com.example.eatstoday.core.utils.normalizedForSearch
import com.example.eatstoday.data.model.Friend
import com.example.eatstoday.data.model.FriendStatus
import com.example.eatstoday.feature.social.presentation.viewmodel.FriendsViewModel
import kotlinx.coroutines.launch

@Composable
private fun FriendRow(
    friend: Friend,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    highlighted: Boolean = false,
    action: @Composable (() -> Unit)? = null,
) {
    EatsCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            UserAvatar(friend.avatarText, highlighted = highlighted)
            Column(Modifier.weight(1f)) {
                Text(friend.name, style = MaterialTheme.typography.titleSmall)
                if (subtitle.isNotEmpty()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            action?.invoke()
        }
    }
}

@Composable
fun FriendListScreen(
    onBack: () -> Unit,
    onFriendClick: (Long) -> Unit,
    onAdd: () -> Unit,
    onRequests: () -> Unit,
    viewModel: FriendsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val all by viewModel.all.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    val friends = all.filter { it.status == FriendStatus.FRIEND }
    val shown = friends.filter { it.name.normalizedForSearch().contains(query.normalizedForSearch()) }
    val requestCount = all.count { it.status == FriendStatus.REQUEST_RECEIVED }
    ScreenColumn {
        TopActions { EatsChip("Thêm", true, onAdd) }
        Text("Bạn bè (${friends.size})", style = MaterialTheme.typography.headlineMedium)
        EatsSearchBar(query, { query = it }, "Tìm bạn...")
        if (requestCount > 0) {
            EatsCard(onClick = onRequests, modifier = Modifier.fillMaxWidth()) {
                Text("Lời mời kết bạn ($requestCount)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
        val cooking = shown.filter { it.cookingNow != null }
        if (cooking.isNotEmpty()) {
            Text("Đang nấu", style = MaterialTheme.typography.titleMedium)
            cooking.forEach { friend ->
                FriendRow(friend, "Đang nấu ${friend.cookingNow}", { onFriendClick(friend.id) }, highlighted = true)
            }
        }
        Text("Tất cả bạn bè", style = MaterialTheme.typography.titleMedium)
        if (shown.isEmpty()) {
            Text("Không tìm thấy bạn nào.", style = MaterialTheme.typography.bodySmall)
        }
        shown.forEach { friend ->
            FriendRow(friend, "Nấu ${friend.cookedCount} món", { onFriendClick(friend.id) })
        }
    }
}

@Composable
fun AddFriendScreen(
    onBack: () -> Unit,
    viewModel: FriendsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val all by viewModel.all.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    var query by rememberSaveable { mutableStateOf("") }
    fun needsBackend(feature: String) {
        scope.launch { snackbar.showSnackbar("$feature cần có backend, chưa có trong bản này") }
    }
    val candidates = all.filter {
        it.status != FriendStatus.FRIEND && it.status != FriendStatus.REQUEST_RECEIVED &&
            it.name.normalizedForSearch().contains(query.normalizedForSearch())
    }
    ScreenColumn {
        Text("Thêm bạn bè", style = MaterialTheme.typography.headlineMedium)
        Text("Nấu cùng nhau vui hơn", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        EatsSearchBar(query, { query = it }, "Tìm theo tên hoặc ID")
        AddOption("QR", "Mã QR của tôi", "Bạn bè quét để kết bạn") { needsBackend("Mã QR") }
        AddOption("Link", "Chia sẻ lời mời", "Gửi qua Zalo, Messenger...") {
            val intent = Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, "Cùng nấu ăn với mình trên EatsToday nhé!")
            try {
                context.startActivity(Intent.createChooser(intent, "Chia sẻ lời mời"))
            } catch (e: ActivityNotFoundException) {
                needsBackend("Chia sẻ")
            }
        }
        AddOption("DB", "Tìm trong danh bạ", "Cần cho phép truy cập danh bạ") { needsBackend("Tìm trong danh bạ") }
        Text("Gợi ý cho bạn", style = MaterialTheme.typography.titleMedium)
        if (candidates.isEmpty()) {
            Text("Không có gợi ý phù hợp.", style = MaterialTheme.typography.bodySmall)
        }
        candidates.forEach { friend ->
            FriendRow(friend, friend.note) {
                if (friend.status == FriendStatus.REQUEST_SENT) {
                    SecondaryButton("Hủy", { viewModel.cancelRequest(friend.id) }, Modifier.width(88.dp), height = 44.dp)
                } else {
                    PrimaryButton("Thêm", { viewModel.sendRequest(friend.id) }, Modifier.width(88.dp), height = 44.dp)
                }
            }
        }
    }
}

@Composable
private fun AddOption(icon: String, title: String, subtitle: String, onClick: () -> Unit) {
    EatsCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FoodImage(icon, Modifier.size(56.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun RequestsScreen(
    onBack: () -> Unit,
    onFriendClick: (Long) -> Unit,
    viewModel: FriendsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val all by viewModel.all.collectAsStateWithLifecycle()
    var showSent by rememberSaveable { mutableStateOf(false) }
    val received = all.filter { it.status == FriendStatus.REQUEST_RECEIVED }
    val sent = all.filter { it.status == FriendStatus.REQUEST_SENT }
    ScreenColumn {
        Text("Lời mời kết bạn", style = MaterialTheme.typography.headlineMedium)
        ChipRow {
            EatsChip("Nhận được (${received.size})", !showSent, { showSent = false })
            EatsChip("Đã gửi (${sent.size})", showSent, { showSent = true })
        }
        if (showSent) {
            sent.forEach { friend ->
                FriendRow(friend, "Đang chờ đồng ý") {
                    SecondaryButton("Hủy", { viewModel.cancelRequest(friend.id) }, Modifier.width(88.dp), height = 44.dp)
                }
            }
            if (sent.isEmpty()) Text("Chưa gửi lời mời nào.", style = MaterialTheme.typography.bodySmall)
        } else {
            received.forEach { friend ->
                FriendRow(friend, friend.note, { onFriendClick(friend.id) }) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        LinkButton("Từ chối", { viewModel.decline(friend.id) })
                        PrimaryButton("Đồng ý", { viewModel.accept(friend.id) }, Modifier.width(88.dp), height = 44.dp)
                    }
                }
            }
            if (received.isEmpty()) {
                Text("Không có lời mời mới.", style = MaterialTheme.typography.bodySmall)
            } else {
                Text(
                    "Chạm vào tên để xem hồ sơ trước khi đồng ý",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
fun FriendProfileScreen(
    onBack: () -> Unit,
    onSendDish: () -> Unit,
    viewModel: FriendsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val friend by viewModel.friend.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    var confirmRemove by rememberSaveable { mutableStateOf(false) }
    val current = friend
    ScreenColumn {
        if (current == null) {
            EatsLoading(Modifier.height(200.dp))
            return@ScreenColumn
        }
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            UserAvatar(current.avatarText, size = 88.dp)
            Text(current.name, style = MaterialTheme.typography.headlineMedium)
            Text(
                "Nấu ${current.cookedCount} món · Món sở trường: ${current.specialty}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PrimaryButton("Gửi món", onSendDish, Modifier.weight(1f), height = 48.dp)
            SecondaryButton(
                "Nhắn tin",
                { scope.launch { snackbar.showSnackbar("Nhắn tin cần có backend, chưa có trong bản này") } },
                Modifier.weight(1f),
                height = 48.dp,
            )
        }
        Text("Món gần đây của ${current.name}", style = MaterialTheme.typography.titleMedium)
        repeat(2) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FoodImage("Ảnh món", Modifier.weight(1f).height(120.dp))
                FoodImage("Ảnh món", Modifier.weight(1f).height(120.dp))
            }
        }
        if (current.status == FriendStatus.FRIEND) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                LinkButton("Xóa bạn", { confirmRemove = true })
            }
        }
        if (confirmRemove) {
            ConfirmationDialog(
                title = "Xóa bạn?",
                message = "${current.name} sẽ bị xóa khỏi danh sách bạn bè.",
                confirmLabel = "Xóa",
                onConfirm = {
                    viewModel.remove(current.id)
                    confirmRemove = false
                    onBack()
                },
                onDismiss = { confirmRemove = false },
            )
        }
    }
}
