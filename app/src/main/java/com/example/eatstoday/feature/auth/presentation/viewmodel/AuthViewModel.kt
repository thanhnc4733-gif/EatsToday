package com.example.eatstoday.feature.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eatstoday.core.utils.isValidEmail
import com.example.eatstoday.core.utils.isValidPassword
import com.example.eatstoday.data.model.UserPreferences
import com.example.eatstoday.data.remote.AuthException
import com.example.eatstoday.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isBusy: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmError: String? = null,
    /** Lỗi không gắn với ô nhập nào (mất mạng, hủy đăng nhập Google...). */
    val message: String? = null,
)

class AuthViewModel(private val repository: UserRepository) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    /** Chạy một thao tác với Firebase, chặn bấm liên tục và đưa lỗi về đúng ô nhập. */
    private fun runAuth(action: suspend () -> Unit, onSuccess: () -> Unit) {
        if (_state.value.isBusy) return
        _state.value = AuthUiState(isBusy = true)
        viewModelScope.launch {
            try {
                action()
                _state.value = AuthUiState()
                onSuccess()
            } catch (e: AuthException) {
                _state.value = when (e.field) {
                    AuthException.Field.EMAIL -> AuthUiState(emailError = e.message)
                    AuthException.Field.PASSWORD -> AuthUiState(passwordError = e.message)
                    AuthException.Field.GENERAL -> AuthUiState(message = e.message)
                }
            }
        }
    }

    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        val emailError = if (isValidEmail(email)) null else "Email chưa đúng định dạng"
        val passwordError = if (password.isEmpty()) "Vui lòng nhập mật khẩu" else null
        if (emailError != null || passwordError != null) {
            _state.value = AuthUiState(emailError = emailError, passwordError = passwordError)
            return
        }
        runAuth({ repository.signIn(email, password) }, onSuccess)
    }

    fun register(
        name: String,
        email: String,
        password: String,
        confirm: String,
        avatarPath: String?,
        onSuccess: () -> Unit,
    ) {
        val errors = AuthUiState(
            nameError = if (name.isBlank()) "Vui lòng nhập tên" else null,
            emailError = if (isValidEmail(email)) null else "Email chưa đúng định dạng",
            passwordError = if (isValidPassword(password)) null else "Mật khẩu cần ít nhất 8 ký tự, có cả chữ và số",
            confirmError = if (password == confirm) null else "Hai mật khẩu chưa khớp",
        )
        if (errors != AuthUiState()) {
            _state.value = errors
            return
        }
        runAuth({ repository.register(name, email, password, avatarPath) }, onSuccess)
    }

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit) {
        runAuth({ repository.signInWithGoogle(idToken) }, onSuccess)
    }

    fun sendPasswordReset(email: String, onSuccess: () -> Unit) {
        if (!isValidEmail(email)) {
            _state.value = AuthUiState(emailError = "Email chưa đúng định dạng")
            return
        }
        runAuth({ repository.sendPasswordReset(email) }, onSuccess)
    }

    fun showMessage(message: String) {
        _state.value = AuthUiState(message = message)
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    /** onResult(true) nghĩa là chưa từng hiện màn hình xin quyền. */
    fun checkPermissionIntro(onResult: (Boolean) -> Unit) {
        viewModelScope.launch { onResult(!repository.permissionsSeen()) }
    }

    fun markPermissionsSeen(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.markPermissionsSeen()
            onDone()
        }
    }

    fun startLocalAccount(preferences: UserPreferences, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.startLocalAccount(preferences)
            onDone()
        }
    }
}
