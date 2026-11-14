package com.example.eatstoday.data.repository

import com.example.eatstoday.data.local.LocalDataStore
import com.example.eatstoday.data.model.AppSettings
import com.example.eatstoday.data.model.AppThemeMode
import com.example.eatstoday.data.model.TextSizeOption
import com.example.eatstoday.data.model.User
import com.example.eatstoday.data.model.UserPreferences
import com.example.eatstoday.data.remote.AuthAccount
import com.example.eatstoday.data.remote.FirebaseAuthSource
import kotlinx.coroutines.flow.Flow

/**
 * Tài khoản do Firebase Authentication quản lý. Hồ sơ, cài đặt và trạng thái "đã vào app"
 * vẫn lưu trong DataStore để app mở được cả khi người dùng chọn "Bắt đầu" mà không đăng nhập.
 */
class UserRepository(
    private val store: LocalDataStore,
    private val authSource: FirebaseAuthSource,
    private val friendRepository: FriendRepository,
) {
    val isLoggedIn: Flow<Boolean> = store.isLoggedIn
    val user: Flow<User> = store.user
    val preferences: Flow<UserPreferences> = store.preferences
    val settings: Flow<AppSettings> = store.settings

    /** true nếu đang đăng nhập bằng tài khoản Firebase, false nếu đang dùng không có tài khoản. */
    val hasAccount: Boolean get() = authSource.currentAccount != null

    suspend fun signIn(email: String, password: String) = onSignedIn(authSource.signIn(email, password))

    suspend fun register(name: String, email: String, password: String, avatarPath: String?) {
        onSignedIn(authSource.register(name, email, password))
        store.setAvatar(avatarPath)
    }

    suspend fun signInWithGoogle(idToken: String) = onSignedIn(authSource.signInWithGoogle(idToken))

    suspend fun sendPasswordReset(email: String) = authSource.sendPasswordReset(email)

    private suspend fun onSignedIn(account: AuthAccount) {
        store.updateProfile(account.name?.takeIf { it.isNotBlank() } ?: account.email?.substringBefore('@') ?: "Bạn", account.email.orEmpty())
        store.setLoggedIn(account.email)
        // Tạo hồ sơ kết bạn trên Firestore. Lỗi mạng hay Firestore chưa bật không được chặn đăng nhập;
        // màn hình Bạn bè sẽ thử lại khi mở.
        runCatching { friendRepository.ensureProfile(account.name) }
    }

    suspend fun setAvatar(path: String?) = store.setAvatar(path)
    suspend fun permissionsSeen(): Boolean = store.permissionsSeen()
    suspend fun markPermissionsSeen() = store.markPermissionsSeen()

    /** "Bắt đầu" không cần tài khoản: chỉ lưu sở thích và vào app. */
    suspend fun startLocalAccount(preferences: UserPreferences) {
        store.savePreferences(preferences)
        store.setLoggedIn(null)
    }

    suspend fun logout() {
        authSource.signOut()
        store.logout()
    }

    suspend fun updateProfile(name: String, email: String) {
        store.updateProfile(name, email)
        if (hasAccount) runCatching { friendRepository.updateName(name) }
    }
    suspend fun setPremium(value: Boolean) = store.setPremium(value)
    suspend fun setTheme(value: AppThemeMode) = store.setTheme(value)
    suspend fun setTextSize(value: TextSizeOption) = store.setTextSize(value)
    suspend fun setNotifications(value: Boolean) = store.setNotifications(value)
    suspend fun setKeepScreenOn(value: Boolean) = store.setKeepScreenOn(value)
}
