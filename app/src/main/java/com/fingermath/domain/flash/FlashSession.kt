package com.fingermath.domain.flash

import com.fingermath.domain.gesture.GestureMapper
import kotlin.random.Random

/** Что показывать на экране: жесты рук или просто цифры. */
enum class FlashDisplay { HANDS, DIGITS }

data class FlashSettings(
    val count: Int = 5,
    val intervalMs: Long = 1000,
    val maxValue: Int = 99,
    val display: FlashDisplay = FlashDisplay.HANDS,
) {
    init {
        require(count >= 1)
        require(intervalMs > 0)
        require(maxValue in 1..GestureMapper.MAX_VALUE)
    }
}

/** Генерирует последовательность чисел 1..maxValue; два одинаковых числа подряд не выдаёт (иначе смену не заметить). */
class NumberSequenceGenerator(private val random: Random = Random.Default) {
    fun generate(settings: FlashSettings): List<Int> {
        val result = ArrayList<Int>(settings.count)
        repeat(settings.count) {
            var n: Int
            do {
                n = random.nextInt(1, settings.maxValue + 1)
            } while (settings.maxValue > 1 && n == result.lastOrNull())
            result += n
        }
        return result
    }
}
