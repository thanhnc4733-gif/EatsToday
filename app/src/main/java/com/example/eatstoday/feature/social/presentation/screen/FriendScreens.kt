package com.example.eatstoday.feature.social.presentation.screen

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eatstoday.core.components.*
import com.example.eatstoday.core.utils.AppViewModelProvider
import com.example.eatstoday.core.utils.FriendQr
import com.example.eatstoday.core.utils.normalizedForSearch
import com.example.eatstoday.core.utils.relativeTime
import com.example.eatstoday.data.model.FriendProfile
import com.example.eatstoday.data.model.Relation
import com.example.eatstoday.feature.social.presentation.viewmodel.FriendsUiState
import com.example.eatstoday.feature.social.presentation.viewmodel.FriendsViewModel
import kotlinx.coroutines.launch

/** Hiện thông báo của ViewModel bằng snackbar rồi xóa để không hiện lại khi xoay màn hình. */
@Composable
private fun FriendMessages(viewModel: FriendsViewModel) {
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbar.current
    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }
}

/**
 * Phần chung của mọi màn bạn bè: chưa đăng nhập, đang tải, lỗi Firestore.
 * Trả về true nếu đã vẽ một trạng thái thay cho nội dung chính.
 */
@Composable
private fun ColumnScopeGate(state: FriendsUiState, viewModel: FriendsViewModel, onSignIn: () -> Unit): Boolean {
    when {
        !state.signedIn -> EmptyState(
            "Cần tài khoản",
            "Đăng nhập để kết bạn",
            "Kết bạn cần một tài khoản để bạn bè tìm thấy bạn. Bạn đang dùng ứng dụng không có tài khoản.",
            actionLabel = "Đăng nhập",
            onAction = onSignIn,
        )
        state.error != null && state.me == null -> EmptyState(
            "Lỗi",
            "Chưa tải được bạn bè",
            state.error,
            actionLabel = "Thử lại",
            onAction = viewModel::retry,
        )
        state.loading -> EatsLoading(Modifier.height(240.dp))
        else -> return false
    }
    return true
}

@Composable
private fun PersonRow(
    profile: FriendProfile,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    action: @Composable (() -> Unit)? = null,
) {
    EatsCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            UserAvatar(profile.avatarText)
            Column(Modifier.weight(1f)) {
                Text(profile.name, style = MaterialTheme.typography.titleSmall)
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
    onFriendClick: (String) -> Unit,
    onAdd: () -> Unit,
    onRequests: () -> Unit,
    onSignIn: () -> Unit,
    viewModel: FriendsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    FriendMessages(viewModel)
    ScreenColumn {
        if (state.signedIn) TopActions { EatsChip("Thêm", true, onAdd) }
        Text("Bạn bè (${state.friends.size})", style = MaterialTheme.typography.headlineMedium)
        if (ColumnScopeGate(state, viewModel, onSignIn)) return@ScreenColumn
        if (state.incoming.isNotEmpty() || state.outgoing.isNotEmpty()) {
            EatsCard(onClick = onRequests, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Lời mời kết bạn: ${state.incoming.size} nhận được, ${state.outgoing.size} đã gửi",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        if (state.friends.isEmpty()) {
            EmptyState(
                "Chưa có bạn",
                "Bạn chưa có bạn bè nào",
                "Gửi mã bạn bè của bạn cho người quen, hoặc tìm họ bằng mã, email hay mã QR.",
                actionLabel = "Thêm bạn bè",
                onAction = onAdd,
            )
            return@ScreenColumn
        }
        EatsSearchBar(query, { query = it }, "Tìm bạn...")
        val shown = state.friends.filter { it.first.name.normalizedForSearch().contains(query.normalizedForSearch()) }
        if (shown.isEmpty()) Text("Không tìm thấy bạn nào.", style = MaterialTheme.typography.bodySmall)
        shown.forEach { (profile, request) ->
            PersonRow(profile, "Bạn bè từ ${relativeTime(request.createdAt)}", { onFriendClick(profile.uid) })
        }
    }
}

@Composable
fun AddFriendScreen(
    onSignIn: () -> Unit,
    viewModel: FriendsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val search by viewModel.search.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    var query by rememberSaveable { mutableStateOf("") }
    FriendMessages(viewModel)
    ScreenColumn {
        Text("Thêm bạn bè", style = MaterialTheme.typography.headlineMedium)
        if (ColumnScopeGate(state, viewModel, onSignIn)) return@ScreenColumn
        val me = state.me ?: return@ScreenColumn

        // Mã của tôi
        EatsCard(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Mã bạn bè của bạn", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(me.friendCode, fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp, color = MaterialTheme.colorScheme.primary)
                val qr = remember(me.friendCode) { FriendQr.bitmap(FriendQr.content(me.friendCode)).asImageBitmap() }
                Image(qr, contentDescription = "Mã QR kết bạn của bạn", modifier = Modifier.size(180.dp).clip(RoundedCornerShape(12.dp)))
                Text("Bạn bè quét mã này trong mục Thêm bạn bè", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                SecondaryButton("Chia sẻ mã", {
                    val text = "Kết bạn với mình trên EatsToday nhé! Mã bạn bè của mình: ${me.friendCode}"
                    val intent = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
                    try {
                        context.startActivity(Intent.createChooser(intent, "Chia sẻ mã bạn bè"))
                    } catch (e: ActivityNotFoundException) {
                        scope.launch { snackbar.showSnackbar("Không có ứng dụng nào để chia sẻ") }
                    }
                })
            }
        }

        // Tìm người khác
        Text("Tìm bạn", style = MaterialTheme.typography.titleMedium)
        EatsSearchBar(query, { query = it; viewModel.clearSearch() }, "Nhập mã bạn bè hoặc email")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PrimaryButton(
                if (search.busy) "Đang tìm..." else "Tìm",
                { viewModel.search(query) },
                Modifier.weight(1f),
                enabled = query.isNotBlank() && !search.busy,
                height = 48.dp,
            )
            SecondaryButton("Quét mã QR", {
                scope.launch {
                    try {
                        FriendQr.scan(context)?.let { raw ->
                            query = FriendQr.parse(raw) ?: raw
                            viewModel.onScanned(raw)
                        }
                    } catch (e: Exception) {
                        snackbar.showSnackbar("Máy này chưa quét mã được. Hãy nhập mã bằng tay")
                    }
                }
            }, Modifier.weight(1f), height = 48.dp)
        }
        if (search.notFound) {
            Text("Không tìm thấy ai với mã hoặc email này.", style = MaterialTheme.typography.bodySmall)
        }
        search.result?.let { found ->
            PersonRow(found, "Mã ${found.friendCode}") {
                when (state.relationOf(found.uid)) {
                    Relation.SELF -> Text("Bạn", style = MaterialTheme.typography.labelMedium)
                    Relation.FRIEND -> Text("Đã là bạn", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Relation.SENT -> Text("Đã gửi", style = MaterialTheme.typography.labelMedium)
                    Relation.RECEIVED -> PrimaryButton("Đồng ý", { viewModel.acceptFrom(found.uid) }, Modifier.width(96.dp), height = 44.dp)
                    Relation.NONE -> PrimaryButton("Kết bạn", { viewModel.sendRequest(found) }, Modifier.width(104.dp), height = 44.dp)
                }
            }
        }
    }
}

@Composable
fun RequestsScreen(
    onSignIn: () -> Unit,
    viewModel: FriendsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showSent by rememberSaveable { mutableStateOf(false) }
    FriendMessages(viewModel)
    ScreenColumn {
        Text("Lời mời kết bạn", style = MaterialTheme.typography.headlineMedium)
        if (ColumnScopeGate(state, viewModel, onSignIn)) return@ScreenColumn
        ChipRow {
            EatsChip("Nhận được (${state.incoming.size})", !showSent, { showSent = false })
            EatsChip("Đã gửi (${state.outgoing.size})", showSent, { showSent = true })
        }
        if (showSent) {
            if (state.outgoing.isEmpty()) Text("Chưa gửi lời mời nào.", style = MaterialTheme.typography.bodySmall)
            state.outgoing.forEach { request ->
                PersonRow(FriendProfile(request.to, request.toName, request.toCode), "Đang chờ đồng ý") {
                    SecondaryButton("Hủy", { viewModel.cancel(request) }, Modifier.width(88.dp), height = 44.dp)
                }
            }
        } else {
            if (state.incoming.isEmpty()) Text("Không có lời mời mới.", style = MaterialTheme.typography.bodySmall)
            state.incoming.forEach { request ->
                PersonRow(FriendProfile(request.from, request.fromName, request.fromCode), "Gửi ${relativeTime(request.createdAt)}") {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        LinkButton("Từ chối", { viewModel.decline(request) })
                        PrimaryButton("Đồng ý", { viewModel.accept(request) }, Modifier.width(88.dp), height = 44.dp)
                    }
                }
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
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmRemove by rememberSaveable { mutableStateOf(false) }
    FriendMessages(viewModel)
    val friend = state.friends.firstOrNull { it.first.uid == viewModel.friendUid }
    ScreenColumn {
        if (friend == null) {
            if (state.loading) {
                EatsLoading(Modifier.height(200.dp))
            } else {
                EmptyState("Không thấy", "Không còn là bạn bè", "Người này không còn trong danh sách bạn bè của bạn.", actionLabel = "Quay lại", onAction = onBack)
            }
            return@ScreenColumn
        }
        val (profile, request) = friend
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            UserAvatar(profile.avatarText, size = 88.dp)
            Text(profile.name, style = MaterialTheme.typography.headlineMedium)
            Text(
                "Mã ${profile.friendCode} · Bạn bè từ ${relativeTime(request.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        PrimaryButton("Nấu và đăng món", onSendDish)
        EatsCard(Modifier.fillMaxWidth()) {
            Text(
                "Món của bạn bè sẽ hiện ở đây khi ứng dụng đồng bộ bài đăng (đang phát triển).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            LinkButton("Hủy kết bạn", { confirmRemove = true })
        }
    }
    if (confirmRemove && friend != null) {
        ConfirmationDialog(
            title = "Hủy kết bạn?",
            message = "${friend.first.name} sẽ bị xóa khỏi danh sách bạn bè của bạn, và bạn cũng biến mất khỏi danh sách của họ.",
            confirmLabel = "Hủy kết bạn",
            onConfirm = {
                confirmRemove = false
                viewModel.removeFriend(friend.first.uid, onBack)
            },
            onDismiss = { confirmRemove = false },
        )
    }
}
