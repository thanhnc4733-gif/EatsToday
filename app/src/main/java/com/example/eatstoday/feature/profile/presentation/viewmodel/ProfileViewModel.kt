package com.example.eatstoday.feature.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eatstoday.data.model.FeedNotification
import com.example.eatstoday.data.repository.FeedRepository
import com.example.eatstoday.data.model.AppSettings
import com.example.eatstoday.data.model.AppThemeMode
import com.example.eatstoday.data.model.CookHistory
import com.example.eatstoday.data.model.RequestStatus
import com.example.eatstoday.data.repository.FriendRepository
import kotlinx.coroutines.flow.catch
import com.example.eatstoday.data.model.TextSizeOption
import com.example.eatstoday.data.model.User
import com.example.eatstoday.data.repository.RecipeRepository
import com.example.eatstoday.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private fun <T> Flow<T>.asState(vm: ViewModel, initial: T): StateFlow<T> =
    stateIn(vm.viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val feedRepository: FeedRepository,
    private val friendRepository: FriendRepository,
    recipeRepository: RecipeRepository,
    private val applicationScope: CoroutineScope,
) : ViewModel() {
    val user: StateFlow<User> = userRepository.user.asState(this, User())
    val settings: StateFlow<AppSettings> = userRepository.settings.asState(this, AppSettings())
    val history: StateFlow<List<CookHistory>> = recipeRepository.history.asState(this, emptyList())
    val friendCount: StateFlow<Int> = friendRepository.requests
        .map { list -> list.count { it.status == RequestStatus.ACCEPTED } }
        .catch { emit(0) }
        .asState(this, 0)
    val notifications: StateFlow<List<FeedNotification>> = feedRepository.notifications.asState(this, emptyList())
    val signedIn: StateFlow<Boolean> = feedRepository.uid.map { it != null }.asState(this, true)

    fun updateProfile(name: String, email: String) {
        viewModelScope.launch { userRepository.updateProfile(name.trim(), email.trim()) }
    }

    fun setAvatar(path: String?) {
        viewModelScope.launch { userRepository.setAvatar(path) }
    }

    fun setTheme(value: AppThemeMode) {
        viewModelScope.launch { userRepository.setTheme(value) }
    }

    fun setTextSize(value: TextSizeOption) {
        viewModelScope.launch { userRepository.setTextSize(value) }
    }

    fun setNotifications(value: Boolean) {
        viewModelScope.launch { userRepository.setNotifications(value) }
    }

    fun setKeepScreenOn(value: Boolean) {
        viewModelScope.launch { userRepository.setKeepScreenOn(value) }
    }

    /** Chạy ở scope của ứng dụng để vẫn hoàn tất khi màn hình đã đóng. */
    fun markNotificationsRead() {
        applicationScope.launch { runCatching { feedRepository.markAllRead() } }
    }

    fun activatePremium(onDone: () -> Unit) {
        viewModelScope.launch {
            userRepository.setPremium(true)
            onDone()
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            userRepository.logout()
            onDone()
        }
    }
}
