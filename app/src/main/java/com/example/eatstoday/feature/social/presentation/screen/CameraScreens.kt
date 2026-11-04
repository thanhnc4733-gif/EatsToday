package com.example.eatstoday.feature.social.presentation.screen

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eatstoday.core.components.*
import com.example.eatstoday.core.permission.CameraPermission
import com.example.eatstoday.core.utils.AppViewModelProvider
import com.example.eatstoday.core.utils.ImageFiles
import com.example.eatstoday.feature.social.presentation.viewmodel.PostViewModel
import kotlinx.coroutines.launch

/** Gồm hai trạng thái theo thiết kế: xin quyền camera và màn hình chụp. */
@Composable
fun CameraScreen(
    onClose: () -> Unit,
    onPhotoReady: (String) -> Unit,
    viewModel: PostViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(CameraPermission.isGranted(context)) }
    var deniedPermanently by rememberSaveable { mutableStateOf(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        granted = CameraPermission.isGranted(context)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
        granted = result
        deniedPermanently = !result && !CameraPermission.canAskAgain(context)
    }
    if (granted) {
        CameraContent(viewModel.shareRecipeName, onPhotoReady)
    } else {
        PermissionContent(
            deniedPermanently = deniedPermanently,
            onAllow = {
                if (deniedPermanently) CameraPermission.openAppSettings(context) else permissionLauncher.launch(CameraPermission.NAME)
            },
            onLater = onClose,
        )
    }
}

@Composable
private fun PermissionContent(deniedPermanently: Boolean, onAllow: () -> Unit, onLater: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(1f))
        FoodImage("Camera", Modifier.size(120.dp), radius = 60.dp)
        Spacer(Modifier.height(14.dp))
        Text("Cho phép dùng camera", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(14.dp))
        Text(
            if (deniedPermanently) {
                "Quyền camera đang bị tắt. Hãy mở Cài đặt để bật lại, hoặc chọn ảnh có sẵn sau."
            } else {
                "Để chụp món bạn vừa nấu và chia sẻ với bạn bè. Ảnh chỉ gửi khi bạn bấm Gửi."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(1f))
        LinkButton("Để sau", onLater)
        PrimaryButton(if (deniedPermanently) "Mở Cài đặt" else "Cho phép", onAllow)
    }
}

@Composable
private fun CameraContent(recipeName: String, onPhotoReady: (String) -> Unit) {
    val context = LocalContext.current
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    var pendingPath by rememberSaveable { mutableStateOf<String?>(null) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val path = pendingPath
        if (saved && path != null) onPhotoReady(path)
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = ImageFiles.copyToApp(context, uri)
                if (path != null) onPhotoReady(path) else snackbar.showSnackbar("Không đọc được ảnh này")
            }
        }
    }
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF1F1B1A)).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(recipeName, color = Color.White, style = MaterialTheme.typography.labelMedium)
        }
        Text("Khoe món vừa nấu xong", color = Color.White, style = MaterialTheme.typography.titleMedium)
        FoodImage(
            "Bấm nút chụp để mở camera, hướng vào đĩa món",
            Modifier.fillMaxWidth().height(350.dp),
            radius = 28.dp,
        )
        Text(
            "Ảnh chỉ gửi khi bạn bấm Gửi ở bước sau",
            color = Color(0xFFE5CFC7),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(
                onClick = {
                    gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                border = BorderStroke(1.dp, Color.White),
            ) { Text("Thư viện", color = Color.White) }
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .border(5.dp, Color.White, CircleShape)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClickLabel = "Chụp ảnh") {
                        val path = ImageFiles.newPhotoPath(context)
                        pendingPath = path
                        try {
                            camera.launch(ImageFiles.uriForPath(context, path))
                        } catch (e: ActivityNotFoundException) {
                            scope.launch { snackbar.showSnackbar("Thiết bị không có ứng dụng camera") }
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(56.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
            }
            Spacer(Modifier.width(96.dp))
        }
    }
}

@Composable
fun ShareScreen(
    photoPath: String?,
    onRetake: () -> Unit,
    onPublished: () -> Unit,
    viewModel: PostViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val friends by viewModel.friends.collectAsStateWithLifecycle()
    var message by rememberSaveable { mutableStateOf("") }
    var selected by rememberSaveable { mutableStateOf(listOf<Long>()) }
    var allowRating by rememberSaveable { mutableStateOf(true) }
    ScreenColumn(
        bottomBar = {
            PrimaryButton(
                if (selected.isEmpty()) "Đăng lên bảng tin" else "Gửi cho ${selected.size} người",
                { viewModel.publish(photoPath, message, allowRating, onPublished) },
            )
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LinkButton("Chụp lại", onRetake)
            Spacer(Modifier.weight(1f))
            Text("Bước cuối", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("Gửi cho bạn bè", style = MaterialTheme.typography.headlineMedium)
        FoodImage(viewModel.shareRecipeName, Modifier.fillMaxWidth().height(240.dp), photoPath = photoPath, radius = 24.dp)
        CustomTextField(message, { message = it }, "Thêm lời nhắn... ví dụ: \"Nước dùng ngọt lắm!\"", singleLine = false)
        Text("Gửi tới", style = MaterialTheme.typography.titleMedium)
        ChipRow {
            friends.forEach { friend ->
                UserAvatar(
                    friend.avatarText,
                    highlighted = friend.id in selected,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(role = Role.Checkbox, onClickLabel = "Chọn ${friend.name}") {
                            selected = if (friend.id in selected) selected - friend.id else selected + friend.id
                        },
                )
            }
        }
        EatsCard(Modifier.fillMaxWidth(), contentPadding = 6.dp) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp)) {
                Text("Cho bạn bè chấm sao và bình luận", modifier = Modifier.weight(1f))
                EatsCheckbox(allowRating, { allowRating = it }, "Cho bạn bè chấm sao và bình luận")
            }
        }
    }
}
