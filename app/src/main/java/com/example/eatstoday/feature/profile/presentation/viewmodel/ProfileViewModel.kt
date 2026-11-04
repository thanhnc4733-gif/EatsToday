package com.example.eatstoday.feature.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eatstoday.data.model.AppNotification
import com.example.eatstoday.data.model.AppSettings
import com.example.eatstoday.data.model.AppThemeMode
import com.example.eatstoday.data.model.CookHistory
import com.example.eatstoday.data.model.FriendStatus
import com.example.eatstoday.data.model.TextSizeOption
import com.example.eatstoday.data.model.User
import com.example.eatstoday.data.repository.RecipeRepository
import com.example.eatstoday.data.repository.SocialRepository
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
    private val socialRepository: SocialRepository,
    recipeRepository: RecipeRepository,
    private val applicationScope: CoroutineScope,
) : ViewModel() {
    val user: StateFlow<User> = userRepository.user.asState(this, User())
    val settings: StateFlow<AppSettings> = userRepository.settings.asState(this, AppSettings())
    val history: StateFlow<List<CookHistory>> = recipeRepository.history.asState(this, emptyList())
    val friendCount: StateFlow<Int> = socialRepository.friends
        .map { list -> list.count { it.status == FriendStatus.FRIEND } }
        .asState(this, 0)
    val notifications: StateFlow<List<AppNotification>> = socialRepository.notifications.asState(this, emptyList())

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
        applicationScope.launch { socialRepository.markNotificationsRead() }
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
