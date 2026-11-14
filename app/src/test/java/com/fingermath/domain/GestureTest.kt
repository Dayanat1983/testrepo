package com.fingermath.domain

import com.fingermath.domain.flash.FlashSettings
import com.fingermath.domain.flash.FlashStep
import com.fingermath.domain.flash.NumberSequenceGenerator
import com.fingermath.domain.flash.asExpression
import com.fingermath.domain.flash.total
import com.fingermath.domain.gesture.Finger
import com.fingermath.domain.gesture.GestureMapper
import com.fingermath.domain.gesture.HandGesture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GestureTest {
    @Test
    fun everyNumberRoundTrips() {
        for (n in 0..99) {
            val g = GestureMapper.toGesture(n)
            assertEquals(n, g.value)
            assertEquals(n, GestureMapper.toValue(g.tens.pressed, g.ones.pressed))
        }
    }

    @Test
    fun allDigitGesturesAreDistinct() {
        assertEquals(10, HandGesture.entries.map { it.pressed }.toSet().size)
    }

    @Test
    fun knownExamples() {
        val g = GestureMapper.toGesture(76)
        assertEquals(setOf(Finger.THUMB, Finger.INDEX, Finger.MIDDLE), g.tens.pressed) // 70
        assertEquals(setOf(Finger.THUMB, Finger.INDEX), g.ones.pressed) // 6
    }

    @Test
    fun unknownCombinationIsNull() {
        assertNull(HandGesture.fromFingers(setOf(Finger.PINKY)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun outOfRangeRejected() {
        GestureMapper.toGesture(100)
    }

    @Test
    fun generatorRespectsRangeAndNoRepeats() {
        val s = FlashSettings(count = 200, maxValue = 3)
        val seq = NumberSequenceGenerator(Random(42)).generate(s)
        assertEquals(200, seq.size)
        assertTrue(seq.all { it.value in 1..3 })
        assertTrue(seq.zipWithNext().all { (a, b) -> a.value != b.value })
    }

    @Test
    fun subtractionNeverGoesNegativeAndFirstStepAdds() {
        for (max in listOf(1, 2, 9, 99)) {
            repeat(300) { seed ->
                val seq = NumberSequenceGenerator(Random(seed)).generate(FlashSettings(count = 20, maxValue = max))
                assertTrue(seq.first().isPlus)
                var run = 0
                for (st in seq) { run += st.signed; assertTrue("$seq", run >= 0) }
                assertEquals(run, seq.total())
            }
        }
    }

    @Test
    fun additionOnlyAndMixedModes() {
        val gen = NumberSequenceGenerator(Random(9))
        assertTrue(gen.generate(FlashSettings(count = 200, allowSubtraction = false)).all { it.isPlus })
        val mixed = gen.generate(FlashSettings(count = 200, allowSubtraction = true))
        assertTrue(mixed.any { !it.isPlus } && mixed.any { it.isPlus })
    }

    @Test
    fun expressionIsReadable() {
        val steps = listOf(FlashStep(12, true), FlashStep(5, true), FlashStep(3, false))
        assertEquals("12 + 5 − 3", steps.asExpression())
        assertEquals(14, steps.total())
    }
}
