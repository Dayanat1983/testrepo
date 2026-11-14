package com.fingermath.ui.flash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fingermath.domain.flash.FlashDisplay
import com.fingermath.domain.flash.FlashSettings
import com.fingermath.domain.flash.FlashStep
import com.fingermath.domain.flash.NumberSequenceGenerator
import com.fingermath.domain.flash.total
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface FlashUiState {
    data class Setup(val settings: FlashSettings) : FlashUiState
    data class Countdown(val seconds: Int) : FlashUiState

    /** [step] == null — короткая пауза между показами (всё скрыто), чтобы одинаковые подряд числа не сливались. */
    data class Showing(val index: Int, val total: Int, val step: FlashStep?, val display: FlashDisplay) : FlashUiState
    data class Answer(val input: String) : FlashUiState
    data class Result(val steps: List<FlashStep>, val expected: Int, val given: Int?) : FlashUiState {
        val isCorrect: Boolean get() = given == expected
    }
}

class FlashViewModel(
    private val generator: NumberSequenceGenerator = NumberSequenceGenerator(),
) : ViewModel() {

    private val _state = MutableStateFlow<FlashUiState>(FlashUiState.Setup(FlashSettings()))
    val state: StateFlow<FlashUiState> = _state.asStateFlow()

    private var settings = FlashSettings()
    private var steps: List<FlashStep> = emptyList()
    private var job: Job? = null

    fun updateSettings(transform: (FlashSettings) -> FlashSettings) {
        settings = transform(settings)
        _state.value = FlashUiState.Setup(settings)
    }

    fun start() {
        job?.cancel()
        steps = generator.generate(settings)
        val total = steps.size
        val display = settings.display
        val gapMs = minOf(GAP_MS, settings.intervalMs / 5)
        val showMs = settings.intervalMs - gapMs // показ + пауза == интервал, темп не «плывёт»
        job = viewModelScope.launch {
            for (sec in 3 downTo 1) {
                _state.value = FlashUiState.Countdown(sec)
                delay(700)
            }
            steps.forEachIndexed { i, step ->
                _state.value = FlashUiState.Showing(i + 1, total, step, display)
                delay(showMs)
                _state.value = FlashUiState.Showing(i + 1, total, null, display)
                delay(gapMs)
            }
            _state.value = FlashUiState.Answer("")
        }
    }

    fun onInputChange(text: String) {
        if (_state.value is FlashUiState.Answer) {
            _state.value = FlashUiState.Answer(text.filter(Char::isDigit).take(MAX_INPUT_LEN))
        }
    }

    fun submit() {
        val current = _state.value as? FlashUiState.Answer ?: return
        _state.value = FlashUiState.Result(steps, steps.total(), current.input.toIntOrNull())
    }

    /** Назад к настройкам (также останавливает идущую сессию). */
    fun reset() {
        job?.cancel()
        _state.value = FlashUiState.Setup(settings)
    }

    private companion object {
        const val GAP_MS = 120L
        const val MAX_INPUT_LEN = 5
    }
}
