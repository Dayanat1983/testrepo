package com.fingermath.domain

import com.fingermath.domain.flash.FlashSettings
import com.fingermath.domain.flash.NumberSequenceGenerator
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
        assertTrue(seq.all { it in 1..3 })
        assertTrue(seq.zipWithNext().all { (a, b) -> a != b })
    }
}
