package com.fingermath.ui.components

import com.fingermath.domain.gesture.Finger

/**
 * Геометрия руки в виртуальных координатах 100x130 (правая рука, большой палец слева).
 * Без зависимостей от Compose, поэтому hit-test проверяется обычными юнит-тестами.
 */
internal object HandGeometry {
    const val W = 100f
    const val H = 130f
    const val FINGER_W = 13f
    const val THUMB_W = 14f
    const val FINGER_BASE_Y = 72f
    const val FOLDED_LEN = 10f

    val FOUR_FINGERS = listOf(Finger.INDEX, Finger.MIDDLE, Finger.RING, Finger.PINKY)
    val FINGER_X = mapOf(Finger.INDEX to 30f, Finger.MIDDLE to 45f, Finger.RING to 60f, Finger.PINKY to 75f)
    val FINGER_LEN = mapOf(Finger.INDEX to 56f, Finger.MIDDLE to 62f, Finger.RING to 56f, Finger.PINKY to 44f)

    const val THUMB_BASE_X = 26f
    const val THUMB_BASE_Y = 104f
    const val THUMB_FOLDED_X = 38f
    const val THUMB_FOLDED_Y = 98f
    const val THUMB_RAISED_X = 10f
    const val THUMB_RAISED_Y = 74f

    private const val FINGER_HIT_HALF_WIDTH = 7.5f // зоны соседних пальцев (шаг 15) не перекрываются
    private const val THUMB_ZONE_MAX_X = 24f

    /**
     * Какой палец под точкой (x, y) в координатах правой руки. Зоны не зависят от того,
     * поднят палец или сложен, иначе сложенный палец нельзя было бы нажать.
     */
    fun fingerAt(x: Float, y: Float): Finger? {
        if (y in 60f..114f && x in 0f..THUMB_ZONE_MAX_X) return Finger.THUMB
        for (f in FOUR_FINGERS) {
            val cx = FINGER_X.getValue(f)
            val top = FINGER_BASE_Y - FINGER_LEN.getValue(f) - FINGER_W / 2
            if (x in (cx - FINGER_HIT_HALF_WIDTH)..(cx + FINGER_HIT_HALF_WIDTH) && y in top..(FINGER_BASE_Y + 8f)) return f
        }
        return null
    }
}
