package com.example.eatstoday.feature.auth.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eatstoday.core.components.*
import com.example.eatstoday.core.utils.AppViewModelProvider
import com.example.eatstoday.core.utils.GoogleSignIn
import com.example.eatstoday.core.utils.GoogleSignInResult
import com.example.eatstoday.data.model.UserPreferences
import com.example.eatstoday.feature.auth.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
private fun Subtitle(text: String) {
    Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

/** Hiện lỗi chung (mất mạng, hủy Google...) bằng snackbar rồi xóa để không hiện lại khi xoay màn hình. */
@Composable
private fun AuthMessages(viewModel: AuthViewModel, message: String?) {
    val snackbar = LocalSnackbar.current
    LaunchedEffect(message) {
        if (message != null) {
            snackbar.showSnackbar(message)
            viewModel.clearMessage()
        }
    }
}

/** Nút Google (Firebase thật) và Apple (chưa hỗ trợ) dùng chung cho Đăng nhập và Đăng ký. */
@Composable
private fun SocialAuthButtons(
    action: String,
    label: String,
    viewModel: AuthViewModel,
    enabled: Boolean,
    onSuccess: () -> Unit,
) {
    val context = LocalContext.current
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    Text("hoặc", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    SecondaryButton("$label Google", {
        scope.launch {
            when (val result = GoogleSignIn.requestIdToken(context)) {
                is GoogleSignInResult.Success -> viewModel.signInWithGoogle(result.idToken, onSuccess)
                is GoogleSignInResult.Failure -> viewModel.showMessage(result.message)
                GoogleSignInResult.Cancelled -> Unit
            }
        }
    }, enabled = enabled)
    SecondaryButton("$label Apple", {
        scope.launch { snackbar.showSnackbar("$action bằng Apple chưa hỗ trợ trên Android trong bản này") }
    }, enabled = enabled)
}

@Composable
fun WelcomeScreen(onStart: () -> Unit, onSignIn: () -> Unit, onRegister: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.45f)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("EatsToday", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onPrimary)
        }
        Column(
            modifier = Modifier.fillMaxWidth().weight(0.55f).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Tự tay nấu, vui mỗi ngày", style = MaterialTheme.typography.headlineLarge)
            Subtitle("Công thức dễ làm, hướng dẫn từng bước, chia sẻ món với bạn bè.")
            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("Đã có tài khoản?", style = MaterialTheme.typography.bodySmall)
                LinkButton("Đăng nhập", onSignIn)
            }
            PrimaryButton("Bắt đầu", onStart)
            SecondaryButton("Đăng ký tài khoản", onRegister)
        }
    }
}

@Composable
fun SignInScreen(
    onBack: () -> Unit,
    onSignedIn: () -> Unit,
    onForgot: () -> Unit,
    onRegister: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    AuthMessages(viewModel, state.message)
    ScreenColumn {
        Text("Chào mừng trở lại", style = MaterialTheme.typography.headlineMedium)
        Subtitle("Đăng nhập để tiếp tục nấu")
        CustomTextField(email, { email = it }, "Email", error = state.emailError, keyboardType = KeyboardType.Email)
        PasswordField(password, { password = it }, "Mật khẩu", error = state.passwordError)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            LinkButton("Quên mật khẩu?", onForgot)
        }
        PrimaryButton(if (state.isBusy) "Đang đăng nhập..." else "Đăng nhập", { viewModel.signIn(email, password, onSignedIn) }, enabled = !state.isBusy)
        SocialAuthButtons("Đăng nhập", "Tiếp tục với", viewModel, !state.isBusy, onSignedIn)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("Chưa có tài khoản?", style = MaterialTheme.typography.bodySmall)
            LinkButton("Đăng ký", onRegister)
        }
    }
}

@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onSignIn: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var avatarPath by rememberSaveable { mutableStateOf<String?>(null) }
    AuthMessages(viewModel, state.message)
    ScreenColumn(
        bottomBar = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Đã có tài khoản?", style = MaterialTheme.typography.bodySmall)
                LinkButton("Đăng nhập", onSignIn)
            }
            PrimaryButton(
                if (state.isBusy) "Đang tạo tài khoản..." else "Đăng ký",
                { viewModel.register(name, email, password, confirm, avatarPath, onRegistered) },
                enabled = !state.isBusy,
            )
        },
    ) {
        Text("Tạo tài khoản", style = MaterialTheme.typography.headlineMedium)
        Subtitle("Đăng ký một lần, đăng nhập được trên mọi máy")
        AvatarEditor(photoPath = avatarPath, text = name.trim().take(2).ifEmpty { "?" }, onChange = { avatarPath = it })
        CustomTextField(name, { name = it }, "Tên hiển thị", error = state.nameError)
        CustomTextField(email, { email = it }, "Email", error = state.emailError, keyboardType = KeyboardType.Email)
        PasswordField(password, { password = it }, "Mật khẩu", error = state.passwordError)
        PasswordField(confirm, { confirm = it }, "Nhập lại mật khẩu", error = state.confirmError)
        SocialAuthButtons("Đăng ký", "Đăng ký bằng", viewModel, !state.isBusy, onRegistered)
    }
}

@Composable
fun PreferencesScreen(
    onDone: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val cuisineOptions = listOf("Món Việt", "Món Á", "Món Âu", "Chay", "Ít dầu mỡ", "Ăn vặt")
    var cuisines by rememberSaveable { mutableStateOf(listOf("Món Việt", "Món Âu")) }
    var skill by rememberSaveable { mutableStateOf("Mới học") }
    var household by rememberSaveable { mutableStateOf("2-3") }
    ScreenColumn(
        bottomBar = {
            PrimaryButton("Tiếp tục", {
                viewModel.startLocalAccount(UserPreferences(cuisines.toSet(), skill, household), onDone)
            })
        },
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            LinkButton("Bỏ qua", { viewModel.startLocalAccount(UserPreferences(), onDone) })
        }
        Text("Bạn thích ăn gì?", style = MaterialTheme.typography.headlineMedium)
        Subtitle("Để mình gợi ý món hợp khẩu vị")
        ChipFlow {
            cuisineOptions.forEach { option ->
                EatsChip(option, option in cuisines, {
                    cuisines = if (option in cuisines) cuisines - option else cuisines + option
                })
            }
        }
        Text("Trình độ nấu", style = MaterialTheme.typography.titleMedium)
        ChipRow {
            listOf("Mới học", "Trung bình", "Giỏi").forEach { EatsChip(it, it == skill, { skill = it }) }
        }
        Text("Số người ăn", style = MaterialTheme.typography.titleMedium)
        ChipRow {
            listOf("1", "2-3", "4+").forEach { EatsChip(it, it == household, { household = it }) }
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onEmailSent: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    AuthMessages(viewModel, state.message)
    ScreenColumn(
        bottomBar = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Nhớ ra rồi?", style = MaterialTheme.typography.bodySmall)
                LinkButton("Đăng nhập", onBack)
            }
            PrimaryButton(
                if (state.isBusy) "Đang gửi..." else "Gửi link đặt lại",
                { viewModel.sendPasswordReset(email, onEmailSent) },
                enabled = !state.isBusy,
            )
        },
    ) {
        Text("Quên mật khẩu?", style = MaterialTheme.typography.headlineMedium)
        Subtitle("Nhập email đã đăng ký, mình sẽ gửi link để bạn đặt mật khẩu mới.")
        CustomTextField(email, { email = it }, "Email của bạn", error = state.emailError, keyboardType = KeyboardType.Email)
    }
}

/** Màn hình kết quả dùng chung: đổi mật khẩu xong, nâng cấp Premium xong. */
@Composable
fun ResultScreen(title: String, message: String, actionLabel: String, onAction: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier.size(96.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(14.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(1f))
        PrimaryButton(actionLabel, onAction)
    }
}
