package com.example.eatstoday.feature.recipe.presentation.screen

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eatstoday.core.components.*
import com.example.eatstoday.core.utils.AppViewModelProvider
import com.example.eatstoday.core.utils.formatTimer
import com.example.eatstoday.feature.recipe.presentation.viewmodel.CookingViewModel
import kotlinx.coroutines.delay

@Composable
fun CookingScreen(
    onExit: () -> Unit,
    onCompleted: (Int) -> Unit,
    viewModel: CookingViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val keepScreenOn by viewModel.keepScreenOn.collectAsStateWithLifecycle(initialValue = true)
    var confirmExit by rememberSaveable { mutableStateOf(false) }
    val snackbar = LocalSnackbar.current
    val view = LocalView.current

    DisposableEffect(keepScreenOn) {
        view.keepScreenOn = keepScreenOn
        onDispose { view.keepScreenOn = false }
    }
    LaunchedEffect(state.timerFinished) {
        if (state.timerFinished) {
            val tone = runCatching { ToneGenerator(AudioManager.STREAM_ALARM, 100) }.getOrNull()
            tone?.startTone(ToneGenerator.TONE_PROP_BEEP2, 800)
            snackbar.showSnackbar("Hết giờ cho bước này")
            delay(800)
            tone?.release()
            viewModel.acknowledgeTimerFinished()
        }
    }
    BackHandler { confirmExit = true }

    val recipe = state.recipe
    val step = state.step
    if (recipe == null || step == null) {
        EmptyState("Không có món", "Không có hướng dẫn cho món này", "Hãy chọn một món khác.", actionLabel = "Thoát", onAction = onExit)
        return
    }

    ScreenColumn(
        bottomBar = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SecondaryButton("Lùi", viewModel::previousStep, Modifier.width(110.dp), enabled = state.stepIndex > 0, height = 64.dp)
                PrimaryButton(
                    text = if (state.isLastStep) "Hoàn thành" else "Bước tiếp",
                    onClick = {
                        if (state.isLastStep) viewModel.complete { onCompleted(recipe.id) } else viewModel.nextStep()
                    },
                    modifier = Modifier.weight(1f),
                    height = 64.dp,
                )
            }
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f))
            Text(
                "Bước ${state.stepIndex + 1}/${state.stepCount}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LinearProgressIndicator(
            progress = { (state.stepIndex + 1).toFloat() / state.stepCount },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant,
        )
        FoodImage("Ảnh minh họa bước", Modifier.fillMaxWidth().height(200.dp))
        Text(step.title, style = MaterialTheme.typography.displaySmall)
        Text(step.description, style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Normal))
        state.remainingSeconds?.let { remaining ->
            EatsCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        formatTimer(remaining),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    SecondaryButton(
                        text = when {
                            state.isRunning -> "Tạm dừng"
                            remaining == 0 -> "Đếm lại"
                            else -> "Bắt đầu"
                        },
                        onClick = viewModel::toggleTimer,
                        modifier = Modifier.width(130.dp),
                        height = 48.dp,
                    )
                }
            }
        }
    }

    if (confirmExit) {
        ConfirmationDialog(
            title = "Thoát chế độ nấu?",
            message = "Tiến trình nấu món này sẽ không được lưu.",
            confirmLabel = "Thoát",
            onConfirm = {
                confirmExit = false
                onExit()
            },
            onDismiss = { confirmExit = false },
        )
    }
}

@Composable
fun CookingCompletedScreen(recipeName: String, onTakePhoto: () -> Unit, onHome: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(1f))
        FoodImage("Xong rồi!", Modifier.size(140.dp), radius = 70.dp)
        Spacer(Modifier.height(14.dp))
        Text("Nấu xong rồi!", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Món $recipeName đã được thêm vào lịch sử nấu.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        PrimaryButton("Chụp ảnh khoe món", onTakePhoto)
        Spacer(Modifier.height(8.dp))
        SecondaryButton("Về trang chủ", onHome)
    }
}
