package com.fingermath.ui.input

import androidx.lifecycle.ViewModel
import com.fingermath.domain.gesture.Finger
import com.fingermath.domain.gesture.GestureMapper
import com.fingermath.domain.input.ArithmeticProblem
import com.fingermath.domain.input.InputSettings
import com.fingermath.domain.input.ProblemGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class HandSide { TENS, ONES }

sealed interface Verdict {
    data object Correct : Verdict

    /** [invalid] — выложена комбинация, которой нет в таблице жестов (например, один мизинец). */
    data class Wrong(val expected: Int, val invalid: Boolean) : Verdict
}

sealed interface InputUiState {
    data class Setup(val settings: InputSettings) : InputUiState
    data class Playing(
        val round: Int,
        val total: Int,
        val problem: ArithmeticProblem,
        val tens: Set<Finger> = emptySet(),
        val ones: Set<Finger> = emptySet(),
        val verdict: Verdict? = null,
        val score: Int = 0,
    ) : InputUiState

    data class Finished(val score: Int, val total: Int) : InputUiState
}

class InputViewModel(
    private val generator: ProblemGenerator = ProblemGenerator(),
) : ViewModel() {

    private val _state = MutableStateFlow<InputUiState>(InputUiState.Setup(InputSettings()))
    val state: StateFlow<InputUiState> = _state.asStateFlow()

    private var settings = InputSettings()

    fun updateSettings(transform: (InputSettings) -> InputSettings) {
        settings = transform(settings)
        _state.value = InputUiState.Setup(settings)
    }

    fun start() {
        _state.value = InputUiState.Playing(1, settings.rounds, generator.next(settings))
    }

    fun toggle(side: HandSide, finger: Finger) {
        val s = _state.value as? InputUiState.Playing ?: return
        if (s.verdict != null) return // после проверки руки заблокированы
        fun Set<Finger>.flip() = if (finger in this) this - finger else this + finger
        _state.value = when (side) {
            HandSide.TENS -> s.copy(tens = s.tens.flip())
            HandSide.ONES -> s.copy(ones = s.ones.flip())
        }
    }

    fun clear() {
        val s = _state.value as? InputUiState.Playing ?: return
        if (s.verdict == null) _state.value = s.copy(tens = emptySet(), ones = emptySet())
    }

    fun check() {
        val s = _state.value as? InputUiState.Playing ?: return
        if (s.verdict != null) return
        val given = GestureMapper.toValue(s.tens, s.ones)
        val expected = s.problem.answer
        _state.value = if (given == expected) {
            s.copy(verdict = Verdict.Correct, score = s.score + 1)
        } else {
            s.copy(verdict = Verdict.Wrong(expected, invalid = given == null))
        }
    }

    fun next() {
        val s = _state.value as? InputUiState.Playing ?: return
        if (s.verdict == null) return
        _state.value = if (s.round >= s.total) {
            InputUiState.Finished(s.score, s.total)
        } else {
            InputUiState.Playing(s.round + 1, s.total, generator.next(settings), score = s.score)
        }
    }

    fun reset() {
        _state.value = InputUiState.Setup(settings)
    }
}
