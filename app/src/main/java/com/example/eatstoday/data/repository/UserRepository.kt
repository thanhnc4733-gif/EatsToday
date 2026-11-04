package com.example.eatstoday.data.repository

import com.example.eatstoday.core.utils.sha256
import com.example.eatstoday.data.local.LocalDataStore
import com.example.eatstoday.data.model.AppSettings
import com.example.eatstoday.data.model.AppThemeMode
import com.example.eatstoday.data.model.TextSizeOption
import com.example.eatstoday.data.model.User
import com.example.eatstoday.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Xác thực mô phỏng cho phiên bản chưa có backend, KHÔNG phải xác thực tài khoản thật.
 */
class UserRepository(private val store: LocalDataStore) {
    val isLoggedIn: Flow<Boolean> = store.isLoggedIn
    val user: Flow<User> = store.user
    val preferences: Flow<UserPreferences> = store.preferences
    val settings: Flow<AppSettings> = store.settings

    private fun isDemo(email: String) = email.trim().equals(DEMO_EMAIL, ignoreCase = true)

    private suspend fun isRegistered(email: String) =
        store.registeredEmail()?.equals(email.trim(), ignoreCase = true) == true

    private suspend fun currentHash(email: String): String? = when {
        isDemo(email) -> store.passwordHash() ?: sha256(DEMO_PASSWORD)
        isRegistered(email) -> store.registeredHash()
        else -> null
    }

    suspend fun signIn(email: String, password: String): Boolean {
        if (sha256(password) != currentHash(email)) return false
        store.setLoggedIn(email.trim())
        return true
    }

    suspend fun accountExists(email: String): Boolean = isDemo(email) || isRegistered(email)

    /** Tạo tài khoản lưu trên máy. Trả về false nếu email đã được dùng. */
    suspend fun register(name: String, email: String, password: String, avatarPath: String?): Boolean {
        if (accountExists(email)) return false
        store.setRegisteredAccount(email.trim(), sha256(password))
        store.updateProfile(name.trim(), email.trim())
        store.setAvatar(avatarPath)
        store.setLoggedIn(email.trim())
        return true
    }

    fun isValidResetCode(code: String): Boolean = code == DEMO_RESET_CODE
    suspend fun resetPassword(email: String, password: String) {
        if (isDemo(email)) store.setPasswordHash(sha256(password)) else store.setRegisteredHash(sha256(password))
    }

    suspend fun isCurrentPassword(email: String, password: String): Boolean = sha256(password) == currentHash(email)

    suspend fun setAvatar(path: String?) = store.setAvatar(path)
    suspend fun permissionsSeen(): Boolean = store.permissionsSeen()
    suspend fun markPermissionsSeen() = store.markPermissionsSeen()

    suspend fun startLocalAccount(preferences: UserPreferences) {
        store.savePreferences(preferences)
        store.setLoggedIn(null)
    }

    suspend fun logout() = store.logout()
    suspend fun updateProfile(name: String, email: String) = store.updateProfile(name, email)
    suspend fun setPremium(value: Boolean) = store.setPremium(value)
    suspend fun setTheme(value: AppThemeMode) = store.setTheme(value)
    suspend fun setTextSize(value: TextSizeOption) = store.setTextSize(value)
    suspend fun setNotifications(value: Boolean) = store.setNotifications(value)
    suspend fun setKeepScreenOn(value: Boolean) = store.setKeepScreenOn(value)

    companion object {
        const val DEMO_EMAIL = "demo@eatstoday.vn"
        const val DEMO_PASSWORD = "Eats1234"
        const val DEMO_RESET_CODE = "1234"
    }
}
