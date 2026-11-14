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
    /** false — только сложение; true — сложение и вычитание. */
    val allowSubtraction: Boolean = true,
) {
    init {
        require(count >= 1)
        require(intervalMs > 0)
        require(maxValue in 1..GestureMapper.MAX_VALUE)
    }
}

/** Один шаг: прибавить или вычесть [value]. Перед показом шага пользователю называется действие. */
data class FlashStep(val value: Int, val isPlus: Boolean) {
    val signed: Int get() = if (isPlus) value else -value
}

fun List<FlashStep>.total(): Int = sumOf { it.signed }

/** «12 + 5 − 3»: первый шаг без знака, дальше со знаком. */
fun List<FlashStep>.asExpression(): String = withIndex().joinToString(" ") { (i, s) ->
    if (i == 0) "${s.value}" else "${if (s.isPlus) "+" else "−"} ${s.value}"
}

/**
 * Генерирует шаги со значениями 1..maxValue. Первый шаг всегда сложение, текущий итог никогда не уходит ниже нуля
 * (вычитать можно не больше, чем накоплено), два одинаковых числа подряд не выдаёт (иначе смену не заметить).
 */
class NumberSequenceGenerator(private val random: Random = Random.Default) {
    fun generate(settings: FlashSettings): List<FlashStep> {
        val result = ArrayList<FlashStep>(settings.count)
        var running = 0
        repeat(settings.count) {
            val prev = result.lastOrNull()?.value
            fun candidates(hi: Int): List<Int> =
                (1..hi).filter { it != prev || settings.maxValue == 1 }

            var minus = settings.allowSubtraction && running > 0 && random.nextBoolean()
            var pool = if (minus) candidates(minOf(settings.maxValue, running)) else emptyList()
            if (pool.isEmpty()) {
                minus = false
                pool = candidates(settings.maxValue)
            }
            val value = pool[random.nextInt(pool.size)]
            result += FlashStep(value, isPlus = !minus)
            running += if (minus) -value else value
        }
        return result
    }
}
