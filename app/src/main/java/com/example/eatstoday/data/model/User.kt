package com.example.eatstoday.data.model

enum class AppThemeMode(val label: String) { LIGHT("Sáng"), DARK("Tối"), SYSTEM("Theo hệ thống") }

enum class TextSizeOption(val label: String, val scale: Float) {
    SMALL("Nhỏ", 0.9f), MEDIUM("Vừa", 1f), LARGE("Lớn", 1.15f)
}

data class User(
    val name: String = "Nguyễn An",
    val email: String = "",
    val isPremium: Boolean = false,
    val avatarPath: String? = null,
) {
    val avatarText: String get() = name.trim().split(" ").lastOrNull().orEmpty().take(2).ifEmpty { "Tôi" }
}

data class UserPreferences(
    val cuisines: Set<String> = emptySet(),
    val skill: String = "Mới học",
    val householdSize: String = "2-3",
)

data class AppSettings(
    val theme: AppThemeMode = AppThemeMode.LIGHT,
    val textSize: TextSizeOption = TextSizeOption.MEDIUM,
    val notificationsEnabled: Boolean = true,
    val keepScreenOn: Boolean = true,
)
