package com.example.eatstoday.core.utils

import java.security.MessageDigest
import java.text.Normalizer
import java.util.Locale

private val CombiningMarks = "\\p{Mn}+".toRegex()
private val EmailRegex = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$".toRegex()

/** Bỏ dấu tiếng Việt để tìm "bun bo" vẫn ra "Bún bò". */
fun String.normalizedForSearch(): String =
    CombiningMarks.replace(Normalizer.normalize(trim().lowercase(Locale.ROOT), Normalizer.Form.NFD), "")
        .replace('đ', 'd')

fun isValidEmail(value: String): Boolean = EmailRegex.matches(value.trim())

/** Mật khẩu hợp lệ: ít nhất 8 ký tự, có cả chữ và số. */
fun isValidPassword(value: String): Boolean =
    value.length >= 8 && value.any(Char::isLetter) && value.any(Char::isDigit)

fun maskEmail(email: String): String {
    val at = email.indexOf('@')
    if (at <= 0) return email
    return email.take(minOf(2, at)) + "****" + email.substring(at)
}

fun sha256(value: String): String =
    MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        .joinToString("") { "%02x".format(it) }

fun formatTimer(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    return "%02d:%02d".format(safe / 60, safe % 60)
}

fun relativeTime(timeMillis: Long, now: Long = System.currentTimeMillis()): String {
    val minutes = ((now - timeMillis) / 60_000L).coerceAtLeast(0)
    return when {
        minutes < 1 -> "Vừa xong"
        minutes < 60 -> "$minutes phút trước"
        minutes < 24 * 60 -> "${minutes / 60} giờ trước"
        minutes < 48 * 60 -> "Hôm qua"
        else -> "${minutes / (24 * 60)} ngày trước"
    }
}

/** Định dạng số lượng: 2.0 hiển thị là 2, 1.5 hiển thị là 1.5. */
fun formatQuantity(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
