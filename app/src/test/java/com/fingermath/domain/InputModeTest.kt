package com.fingermath.domain

import com.fingermath.domain.gesture.Finger
import com.fingermath.domain.gesture.GestureMapper
import com.fingermath.domain.input.InputSettings
import com.fingermath.domain.input.OpMode
import com.fingermath.domain.input.ProblemGenerator
import com.fingermath.ui.components.HandGeometry
import com.fingermath.ui.input.HandSide
import com.fingermath.ui.input.InputUiState
import com.fingermath.ui.input.InputViewModel
import com.fingermath.ui.input.Verdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class InputModeTest {
    @Test
    fun problemsAlwaysHaveAnswerInRange() {
        val gen = ProblemGenerator(Random(7))
        for (max in listOf(9, 50, 99)) for (ops in OpMode.entries) {
            repeat(3000) {
                val p = gen.next(InputSettings(maxValue = max, ops = ops))
                assertTrue("$p", p.a in 1..max && p.b in 1..max && p.answer in 1..max)
            }
        }
    }

    @Test
    fun opModeIsRespected() {
        val gen = ProblemGenerator(Random(1))
        repeat(200) { assertTrue(gen.next(InputSettings(ops = OpMode.PLUS)).isPlus) }
        repeat(200) { assertTrue(!gen.next(InputSettings(ops = OpMode.MINUS)).isPlus) }
    }

    @Test
    fun hitTestFindsEveryFingerInBothStates() {
        val g = HandGeometry
        assertEquals(Finger.THUMB, g.fingerAt(10f, 74f))   // большой поднят
        assertEquals(Finger.THUMB, g.fingerAt(14f, 100f))  // большой сложен/у основания
        for (f in g.FOUR_FINGERS) {
            val x = g.FINGER_X.getValue(f)
            assertEquals(f, g.fingerAt(x, g.FINGER_BASE_Y - g.FINGER_LEN.getValue(f)))  // кончик поднятого
            assertEquals(f, g.fingerAt(x, g.FINGER_BASE_Y - g.FOLDED_LEN))              // сложенный
        }
        assertNull(g.fingerAt(50f, 110f)) // ладонь
        assertNull(g.fingerAt(95f, 20f))  // мимо
    }

    @Test
    fun correctAnswerFlow() {
        val vm = InputViewModel(ProblemGenerator(Random(3)))
        vm.start()
        val p = (vm.state.value as InputUiState.Playing).problem
        val target = GestureMapper.toGesture(p.answer)
        target.tens.pressed.forEach { vm.toggle(HandSide.TENS, it) }
        target.ones.pressed.forEach { vm.toggle(HandSide.ONES, it) }
        vm.check()
        val s = vm.state.value as InputUiState.Playing
        assertEquals(Verdict.Correct, s.verdict)
        assertEquals(1, s.score)
        vm.toggle(HandSide.ONES, Finger.THUMB) // руки заблокированы после проверки
        assertEquals(s.ones, (vm.state.value as InputUiState.Playing).ones)
    }

    @Test
    fun wrongAndInvalidAnswers() {
        val vm = InputViewModel(ProblemGenerator(Random(3)))
        vm.start()
        vm.toggle(HandSide.ONES, Finger.PINKY) // одиночный мизинец — не жест
        vm.check()
        val v = (vm.state.value as InputUiState.Playing).verdict as Verdict.Wrong
        assertTrue(v.invalid)
    }

    @Test
    fun sessionFinishesAfterAllRounds() {
        val vm = InputViewModel(ProblemGenerator(Random(5)))
        vm.updateSettings { it.copy(rounds = 5) }
        vm.start()
        repeat(5) { vm.check(); vm.next() }
        val f = vm.state.value as InputUiState.Finished
        assertEquals(5, f.total)
    }
}
