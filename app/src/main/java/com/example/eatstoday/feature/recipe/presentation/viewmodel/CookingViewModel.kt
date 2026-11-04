package com.example.eatstoday.feature.recipe.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eatstoday.data.model.CookingStep
import com.example.eatstoday.data.model.Recipe
import com.example.eatstoday.data.repository.RecipeRepository
import com.example.eatstoday.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CookingUiState(
    val recipe: Recipe? = null,
    val stepIndex: Int = 0,
    val remainingSeconds: Int? = null,
    val isRunning: Boolean = false,
    val timerFinished: Boolean = false,
) {
    val step: CookingStep? get() = recipe?.steps?.getOrNull(stepIndex)
    val stepCount: Int get() = recipe?.steps?.size ?: 0
    val isLastStep: Boolean get() = stepIndex >= stepCount - 1
}

/**
 * Đồng hồ được tính theo thời điểm kết thúc nên vẫn đúng sau khi xoay màn hình,
 * chuyển ứng dụng xuống nền, hoặc hệ thống dừng tiến trình rồi khôi phục.
 */
class CookingViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val recipeRepository: RecipeRepository,
    userRepository: UserRepository,
) : ViewModel() {
    private val recipe = recipeRepository.recipe(savedStateHandle.get<Int>("recipeId") ?: -1)
    private val _state = MutableStateFlow(CookingUiState(recipe = recipe))
    val state: StateFlow<CookingUiState> = _state
    val keepScreenOn: Flow<Boolean> = userRepository.settings.map { it.keepScreenOn }

    private var ticker: Job? = null
    private var recorded = savedStateHandle.get<Boolean>(KEY_RECORDED) ?: false

    init {
        val step = savedStateHandle.get<Int>(KEY_STEP) ?: 0
        val endAt = savedStateHandle.get<Long>(KEY_END_AT)
        val paused = savedStateHandle.get<Int>(KEY_REMAINING)
        goTo(step, resetTimer = false)
        when {
            endAt != null -> runUntil(endAt)
            paused != null -> _state.update { it.copy(remainingSeconds = paused) }
        }
    }

    private fun goTo(index: Int, resetTimer: Boolean = true) {
        val last = (recipe?.steps?.size ?: 1) - 1
        val target = index.coerceIn(0, last.coerceAtLeast(0))
        ticker?.cancel()
        savedStateHandle[KEY_STEP] = target
        if (resetTimer) {
            savedStateHandle.remove<Long>(KEY_END_AT)
            savedStateHandle.remove<Int>(KEY_REMAINING)
        }
        _state.update {
            it.copy(
                stepIndex = target,
                remainingSeconds = recipe?.steps?.getOrNull(target)?.duration,
                isRunning = false,
                timerFinished = false,
            )
        }
    }

    fun nextStep() = goTo(_state.value.stepIndex + 1)
    fun previousStep() = goTo(_state.value.stepIndex - 1)

    fun toggleTimer() {
        val current = _state.value
        val remaining = current.remainingSeconds ?: return
        if (current.isRunning) {
            ticker?.cancel()
            savedStateHandle.remove<Long>(KEY_END_AT)
            savedStateHandle[KEY_REMAINING] = remaining
            _state.update { it.copy(isRunning = false) }
        } else {
            val seconds = if (remaining > 0) remaining else current.step?.duration ?: return
            runUntil(System.currentTimeMillis() + seconds * 1_000L)
        }
    }

    private fun runUntil(endAt: Long) {
        ticker?.cancel()
        savedStateHandle[KEY_END_AT] = endAt
        savedStateHandle.remove<Int>(KEY_REMAINING)
        _state.update { it.copy(isRunning = true, timerFinished = false) }
        ticker = viewModelScope.launch {
            while (true) {
                val left = ((endAt - System.currentTimeMillis() + 999) / 1_000L).toInt().coerceAtLeast(0)
                _state.update { it.copy(remainingSeconds = left) }
                if (left == 0) {
                    savedStateHandle.remove<Long>(KEY_END_AT)
                    _state.update { it.copy(isRunning = false, timerFinished = true) }
                    break
                }
                delay(250)
            }
        }
    }

    fun acknowledgeTimerFinished() {
        _state.update { it.copy(timerFinished = false) }
    }

    /** Ghi lịch sử đúng một lần, kể cả khi người dùng bấm liên tục. */
    fun complete(onDone: () -> Unit) {
        val target = recipe
        if (target == null || recorded) {
            onDone()
            return
        }
        recorded = true
        savedStateHandle[KEY_RECORDED] = true
        viewModelScope.launch {
            recipeRepository.recordCooked(target)
            onDone()
        }
    }

    private companion object {
        const val KEY_STEP = "cooking_step"
        const val KEY_END_AT = "timer_end_at"
        const val KEY_REMAINING = "timer_remaining"
        const val KEY_RECORDED = "cooking_recorded"
    }
}
