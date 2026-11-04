package com.example.eatstoday.core.components

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.eatstoday.core.permission.CameraPermission
import com.example.eatstoday.core.utils.ImageFiles
import kotlinx.coroutines.launch

/** Ảnh đại diện kèm lựa chọn: thư viện, chụp mới hoặc xóa. Ảnh được lưu trong bộ nhớ riêng của ứng dụng. */
@Composable
fun AvatarEditor(photoPath: String?, text: String, onChange: (String?) -> Unit) {
    val context = LocalContext.current
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    var showOptions by rememberSaveable { mutableStateOf(false) }
    var pendingPath by rememberSaveable { mutableStateOf<String?>(null) }

    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = ImageFiles.copyToApp(context, uri)
                if (path != null) onChange(path) else snackbar.showSnackbar("Không đọc được ảnh này")
            }
        }
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        if (saved) pendingPath?.let(onChange)
    }
    fun openCamera() {
        val path = ImageFiles.newPhotoPath(context)
        pendingPath = path
        try {
            camera.launch(ImageFiles.uriForPath(context, path))
        } catch (e: ActivityNotFoundException) {
            scope.launch { snackbar.showSnackbar("Thiết bị không có ứng dụng camera") }
        }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            openCamera()
        } else {
            scope.launch { snackbar.showSnackbar("Cần quyền camera để chụp ảnh. Bạn vẫn có thể chọn từ thư viện.") }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        UserAvatar(
            text,
            size = 96.dp,
            photoPath = photoPath,
            modifier = Modifier.clip(CircleShape).clickable(onClickLabel = "Đổi ảnh đại diện") { showOptions = true },
        )
        LinkButton(if (photoPath == null) "Thêm ảnh đại diện" else "Đổi ảnh đại diện", { showOptions = true })
    }

    if (showOptions) {
        AlertDialog(
            onDismissRequest = { showOptions = false },
            title = { Text("Ảnh đại diện") },
            text = {
                Column {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showOptions = false
                            gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    ) { Text("Chọn từ thư viện") }
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showOptions = false
                            if (CameraPermission.isGranted(context)) openCamera() else permission.launch(CameraPermission.NAME)
                        },
                    ) { Text("Chụp ảnh mới") }
                    if (photoPath != null) {
                        TextButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                showOptions = false
                                onChange(null)
                            },
                        ) { Text("Xóa ảnh") }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showOptions = false }) { Text("Đóng") } },
        )
    }
}
