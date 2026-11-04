package com.example.eatstoday.feature.profile.presentation.screen

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.eatstoday.core.components.*
import com.example.eatstoday.core.permission.AppPermission
import com.example.eatstoday.core.permission.CameraPermission

private fun AppPermission.icon(): ImageVector = when (this) {
    AppPermission.LOCATION -> Icons.Outlined.LocationOn
    AppPermission.CAMERA -> Icons.Outlined.CameraAlt
    AppPermission.NOTIFICATIONS -> Icons.Outlined.Notifications
    AppPermission.MICROPHONE -> Icons.Outlined.Mic
}

private fun AppPermission.isBlocked(context: Context): Boolean =
    !isGranted(context) && names.none { CameraPermission.canAskAgain(context, it) }

/** Mỗi quyền có thể bật riêng; từ chối quyền nào thì chức năng còn lại vẫn dùng được. */
@Composable
fun PermissionsScreen(isOnboarding: Boolean, onDone: () -> Unit) {
    val context = LocalContext.current
    var refresh by remember { mutableIntStateOf(0) }
    var blocked by rememberSaveable { mutableStateOf(listOf<String>()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { refresh += 1 }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        // Sau khi bị từ chối hẳn, hệ thống không hiện hộp thoại nữa nên phải chuyển sang mở Cài đặt.
        blocked = AppPermission.entries.filter { it.isBlocked(context) }.map { it.name }
        refresh += 1
    }
    val granted = remember(refresh) { AppPermission.entries.filter { it.isGranted(context) }.toSet() }
    val missing = AppPermission.entries.filter { it !in granted && it.name !in blocked }

    ScreenColumn(
        bottomBar = if (isOnboarding) {
            {
                if (missing.isNotEmpty()) {
                    PrimaryButton("Cho phép tất cả", { launcher.launch(missing.flatMap { it.names }.toTypedArray()) })
                }
                SecondaryButton(if (missing.isEmpty()) "Tiếp tục" else "Để sau", onDone)
            }
        } else {
            null
        },
    ) {
        Text("Quyền truy cập", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Bạn có thể đổi lựa chọn bất cứ lúc nào trong Cài đặt.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AppPermission.entries.forEach { permission ->
            EatsCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(permission.icon(), contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(permission.title, style = MaterialTheme.typography.titleSmall)
                        Text(
                            permission.reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    when {
                        permission in granted -> Text(
                            "Đã cho phép",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 12.dp),
                        )
                        permission.name in blocked -> LinkButton("Mở Cài đặt", { CameraPermission.openAppSettings(context) })
                        else -> LinkButton("Cho phép", { launcher.launch(permission.names.toTypedArray()) })
                    }
                }
            }
        }
    }
}
