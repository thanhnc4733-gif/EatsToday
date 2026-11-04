package com.example.eatstoday.feature.auth.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eatstoday.core.components.*
import com.example.eatstoday.core.utils.AppViewModelProvider
import com.example.eatstoday.core.utils.formatTimer
import com.example.eatstoday.core.utils.maskEmail
import com.example.eatstoday.data.model.UserPreferences
import com.example.eatstoday.data.repository.UserRepository
import com.example.eatstoday.feature.auth.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
private fun Subtitle(text: String) {
    Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

/** Nút Google và Apple dùng chung cho Đăng nhập và Đăng ký. Chưa kết nối dịch vụ thật nên chỉ báo cho người dùng biết. */
@Composable
private fun SocialAuthButtons(action: String, label: String) {
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    fun notAvailable(provider: String) {
        scope.launch { snackbar.showSnackbar("$action bằng $provider chưa được kết nối trong bản này") }
    }
    Text("hoặc", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    SecondaryButton("$label Google", { notAvailable("Google") })
    SecondaryButton("$label Apple", { notAvailable("Apple") })
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
    ScreenColumn {
        Text("Chào mừng trở lại", style = MaterialTheme.typography.headlineMedium)
        Subtitle("Đăng nhập để tiếp tục nấu")
        CustomTextField(email, { email = it }, "Email", error = state.emailError, keyboardType = KeyboardType.Email)
        PasswordField(password, { password = it }, "Mật khẩu", error = state.passwordError)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            LinkButton("Quên mật khẩu?", onForgot)
        }
        PrimaryButton("Đăng nhập", { viewModel.signIn(email, password, onSignedIn) }, enabled = !state.isBusy)
        Text(
            "Tài khoản mẫu: ${UserRepository.DEMO_EMAIL} / ${UserRepository.DEMO_PASSWORD}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        SocialAuthButtons(action = "Đăng nhập", label = "Tiếp tục với")
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
    ScreenColumn(
        bottomBar = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Đã có tài khoản?", style = MaterialTheme.typography.bodySmall)
                LinkButton("Đăng nhập", onSignIn)
            }
            PrimaryButton(
                "Đăng ký",
                { viewModel.register(name, email, password, confirm, avatarPath, onRegistered) },
                enabled = !state.isBusy,
            )
        },
    ) {
        Text("Tạo tài khoản", style = MaterialTheme.typography.headlineMedium)
        Subtitle("Tài khoản được lưu trên thiết bị này")
        AvatarEditor(photoPath = avatarPath, text = name.trim().take(2).ifEmpty { "?" }, onChange = { avatarPath = it })
        CustomTextField(name, { name = it }, "Tên hiển thị", error = state.nameError)
        CustomTextField(email, { email = it }, "Email", error = state.emailError, keyboardType = KeyboardType.Email)
        PasswordField(password, { password = it }, "Mật khẩu", error = state.passwordError)
        PasswordField(confirm, { confirm = it }, "Nhập lại mật khẩu", error = state.confirmError)
        SocialAuthButtons(action = "Đăng ký", label = "Đăng ký bằng")
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
    onCodeSent: (String) -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    ScreenColumn(
        bottomBar = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Nhớ ra rồi?", style = MaterialTheme.typography.bodySmall)
                LinkButton("Đăng nhập", onBack)
            }
            PrimaryButton("Gửi mã", { viewModel.requestReset(email) { onCodeSent(email.trim()) } })
        },
    ) {
        Text("Quên mật khẩu?", style = MaterialTheme.typography.headlineMedium)
        Subtitle("Nhập email đã đăng ký, mình sẽ gửi mã xác nhận gồm 4 số.")
        CustomTextField(email, { email = it }, "Email của bạn", error = state.emailError, keyboardType = KeyboardType.Email)
    }
}

@Composable
fun VerifyCodeScreen(
    email: String,
    onBack: () -> Unit,
    onVerified: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var code by rememberSaveable { mutableStateOf("") }
    var countdown by rememberSaveable { mutableIntStateOf(45) }
    var resendCount by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(resendCount) {
        while (countdown > 0) {
            delay(1_000)
            countdown -= 1
        }
    }
    ScreenColumn(
        bottomBar = { PrimaryButton("Xác nhận", { viewModel.verifyCode(code, onVerified) }, enabled = code.length == 4) },
    ) {
        Text("Nhập mã xác nhận", style = MaterialTheme.typography.headlineMedium)
        Subtitle("Mã đã gửi tới ${maskEmail(email)}")
        Text(
            "Bản mô phỏng chưa gửi email thật. Mã thử nghiệm: ${UserRepository.DEMO_RESET_CODE}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        BasicTextField(
            value = code,
            onValueChange = { value -> code = value.filter(Char::isDigit).take(4) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                // Ô nhập thật được ẩn nhưng vẫn phải đưa vào cây giao diện để nhận bàn phím.
                Box(Modifier.size(1.dp).alpha(0f)) { innerTextField() }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                    repeat(4) { index ->
                        val active = index == code.length || (index == 3 && code.length == 4)
                        Box(
                            modifier = Modifier
                                .size(width = 48.dp, height = 56.dp)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                .border(
                                    2.dp,
                                    if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(12.dp),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(code.getOrNull(index)?.toString().orEmpty(), style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            },
        )
        state.codeError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            if (countdown > 0) {
                Text("Gửi lại mã sau ${formatTimer(countdown)}", style = MaterialTheme.typography.bodySmall)
            } else {
                LinkButton("Gửi lại mã", {
                    countdown = 45
                    resendCount += 1
                })
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("Sai email?", style = MaterialTheme.typography.bodySmall)
            LinkButton("Đổi email", onBack)
        }
    }
}

@Composable
fun NewPasswordScreen(
    email: String,
    onDone: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    ScreenColumn(
        bottomBar = { PrimaryButton("Đổi mật khẩu", { viewModel.resetPassword(email, password, confirm, onDone) }) },
    ) {
        Text("Tạo mật khẩu mới", style = MaterialTheme.typography.headlineMedium)
        Subtitle("Mật khẩu mới nên khác mật khẩu cũ.")
        PasswordField(password, { password = it }, "Mật khẩu mới", error = state.passwordError)
        PasswordField(confirm, { confirm = it }, "Nhập lại mật khẩu", error = state.confirmError)
        EatsCard {
            Text("Ít nhất 8 ký tự", style = MaterialTheme.typography.bodySmall)
            Text("Có cả chữ và số", style = MaterialTheme.typography.bodySmall)
        }
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
