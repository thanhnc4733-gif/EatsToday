package com.example.eatstoday.core.permission

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

/** Các quyền hệ thống ứng dụng xin. Vị trí chỉ dùng khi đang mở ứng dụng, không theo dõi ở nền. */
enum class AppPermission(val title: String, val reason: String, val names: List<String>) {
    LOCATION(
        "Vị trí",
        "Dành cho tính năng gợi ý món theo khu vực (sắp có). Chỉ dùng khi bạn đang mở ứng dụng.",
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
    ),
    CAMERA(
        "Camera",
        "Để chụp món bạn vừa nấu và ảnh đại diện. Ảnh chỉ gửi khi bạn bấm Gửi.",
        listOf(Manifest.permission.CAMERA),
    ),
    NOTIFICATIONS(
        "Thông báo",
        "Dành cho nhắc hết giờ nấu và tin từ bạn bè (sắp có).",
        if (Build.VERSION.SDK_INT >= 33) listOf(Manifest.permission.POST_NOTIFICATIONS) else emptyList(),
    ),
    MICROPHONE(
        "Micro",
        "Dành cho điều khiển bằng giọng nói khi tay đang bận nấu (sắp có).",
        listOf(Manifest.permission.RECORD_AUDIO),
    );

    /** Android dưới 13 không cần xin quyền thông báo nên danh sách rỗng được coi là đã cho phép. */
    fun isGranted(context: Context): Boolean =
        names.isEmpty() || names.any {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
}
