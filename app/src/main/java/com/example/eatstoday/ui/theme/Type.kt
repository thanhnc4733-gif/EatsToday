package com.example.eatstoday.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Thiết kế dùng Be Vietnam Pro. Sau khi chép file .ttf vào res/font, chỉ cần đổi giá trị này
 * (xem README); mọi màn hình sẽ nhận font mới.
 */
val AppFontFamily: FontFamily = FontFamily.Default

private fun style(size: Int, weight: FontWeight = FontWeight.Normal, line: Int = size + 6) = TextStyle(
    fontFamily = AppFontFamily,
    fontSize = size.sp,
    fontWeight = weight,
    lineHeight = line.sp,
)

val AppTypography = Typography(
    displaySmall = style(32, FontWeight.Bold, 40),
    headlineLarge = style(30, FontWeight.Bold, 36),
    headlineMedium = style(26, FontWeight.Bold, 32),
    titleLarge = style(22, FontWeight.Bold, 28),
    titleMedium = style(18, FontWeight.Bold, 24),
    titleSmall = style(15, FontWeight.Bold, 20),
    bodyLarge = style(15),
    bodyMedium = style(14),
    bodySmall = style(13),
    labelLarge = style(16, FontWeight.SemiBold),
    labelMedium = style(14, FontWeight.SemiBold),
    labelSmall = style(12),
)
