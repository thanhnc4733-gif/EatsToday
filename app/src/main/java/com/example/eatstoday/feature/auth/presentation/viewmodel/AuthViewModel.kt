package com.example.eatstoday.feature.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eatstoday.core.utils.isValidEmail
import com.example.eatstoday.core.utils.isValidPassword
import com.example.eatstoday.data.model.UserPreferences
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
    val codeError: String? = null,
)

class AuthViewModel(private val repository: UserRepository) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        if (_state.value.isBusy) return
        val emailError = if (isValidEmail(email)) null else "Email chưa đúng định dạng"
        val passwordError = if (password.isEmpty()) "Vui lòng nhập mật khẩu" else null
        if (emailError != null || passwordError != null) {
            _state.value = AuthUiState(emailError = emailError, passwordError = passwordError)
            return
        }
        _state.value = AuthUiState(isBusy = true)
        viewModelScope.launch {
            if (repository.signIn(email, password)) {
                _state.value = AuthUiState()
                onSuccess()
            } else {
                _state.value = AuthUiState(passwordError = "Email hoặc mật khẩu không đúng")
            }
        }
    }

    fun requestReset(email: String, onSuccess: () -> Unit) {
        if (!isValidEmail(email)) {
            _state.value = AuthUiState(emailError = "Email chưa đúng định dạng")
            return
        }
        viewModelScope.launch {
            if (repository.accountExists(email)) {
                _state.value = AuthUiState()
                onSuccess()
            } else {
                _state.value = AuthUiState(emailError = "Không tìm thấy tài khoản với email này")
            }
        }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        confirm: String,
        avatarPath: String?,
        onSuccess: () -> Unit,
    ) {
        if (_state.value.isBusy) return
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
        _state.value = AuthUiState(isBusy = true)
        viewModelScope.launch {
            if (repository.register(name, email, password, avatarPath)) {
                _state.value = AuthUiState()
                onSuccess()
            } else {
                _state.value = AuthUiState(emailError = "Email này đã có tài khoản")
            }
        }
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

    fun verifyCode(code: String, onSuccess: () -> Unit) {
        if (repository.isValidResetCode(code)) {
            _state.value = AuthUiState()
            onSuccess()
        } else {
            _state.value = AuthUiState(codeError = "Mã xác nhận không đúng")
        }
    }

    fun resetPassword(email: String, password: String, confirm: String, onSuccess: () -> Unit) {
        if (!isValidPassword(password)) {
            _state.value = AuthUiState(passwordError = "Mật khẩu cần ít nhất 8 ký tự, có cả chữ và số")
            return
        }
        if (password != confirm) {
            _state.value = AuthUiState(confirmError = "Hai mật khẩu chưa khớp")
            return
        }
        viewModelScope.launch {
            if (repository.isCurrentPassword(email, password)) {
                _state.value = AuthUiState(passwordError = "Mật khẩu mới nên khác mật khẩu cũ")
            } else {
                repository.resetPassword(email, password)
                _state.value = AuthUiState()
                onSuccess()
            }
        }
    }

    fun startLocalAccount(preferences: UserPreferences, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.startLocalAccount(preferences)
            onDone()
        }
    }
}
