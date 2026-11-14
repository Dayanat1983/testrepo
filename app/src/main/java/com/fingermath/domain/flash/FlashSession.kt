package com.fingermath.domain.flash

import com.fingermath.domain.gesture.GestureMapper
import com.fingermath.domain.input.OpMode
import kotlin.random.Random

/** Что показывать на экране: жесты рук, цифры или цифры вместе с жестами. */
enum class FlashDisplay { HANDS, DIGITS, BOTH }

data class FlashSettings(
    val count: Int = 5,
    val intervalMs: Long = 1000,
    val maxValue: Int = 99,
    val display: FlashDisplay = FlashDisplay.HANDS,
    /** PLUS — только сложение, MINUS — только вычитание (от начального числа), BOTH — вперемешку. */
    val ops: OpMode = OpMode.BOTH,
) {
    init {
        require(count >= 1)
        require(intervalMs > 0)
        require(maxValue in 1..GestureMapper.MAX_VALUE)
    }
}

/**
 * Один шаг: прибавить или вычесть [value]. [isStart] — начальное число в режиме «только вычитание»
 * (его не прибавляют, с него начинают). Перед показом шага пользователю называется действие.
 */
data class FlashStep(val value: Int, val isPlus: Boolean, val isStart: Boolean = false) {
    val signed: Int get() = if (isPlus) value else -value
}

fun List<FlashStep>.total(): Int = sumOf { it.signed }

/** «12 + 5 − 3»: первый шаг без знака, дальше со знаком. */
fun List<FlashStep>.asExpression(): String = withIndex().joinToString(" ") { (i, s) ->
    if (i == 0) "${s.value}" else "${if (s.isPlus) "+" else "−"} ${s.value}"
}

/**
 * Генерирует шаги со значениями 1..maxValue. Текущий итог никогда не уходит ниже нуля, два одинаковых
 * числа подряд не выдаётся (иначе смену не заметить).
 * - PLUS/BOTH: первый шаг — сложение; в BOTH вычитать можно не больше, чем накоплено.
 * - MINUS: первый шаг — начальное число (не меньше числа шагов и maxValue/2, не больше min(99, 3*maxValue)), дальше только вычитание;
 *   вычитаемое ограничено так, чтобы на каждый оставшийся шаг хватило хотя бы единицы.
 */
class NumberSequenceGenerator(private val random: Random = Random.Default) {
    fun generate(settings: FlashSettings): List<FlashStep> {
        val result = ArrayList<FlashStep>(settings.count)
        var running = 0
        repeat(settings.count) { i ->
            val prev = result.lastOrNull()?.value
            fun candidates(hi: Int): List<Int> =
                (1..hi).filter { it != prev || settings.maxValue == 1 }

            if (settings.ops == OpMode.MINUS) {
                if (i == 0) {
                    val lo = maxOf(settings.count, settings.maxValue / 2)
                    val hi = minOf(GestureMapper.MAX_VALUE, maxOf(lo, settings.maxValue * 3))
                    val start = random.nextInt(lo, hi + 1)
                    result += FlashStep(start, isPlus = true, isStart = true)
                    running = start
                } else {
                    val remainingAfter = settings.count - 1 - i
                    val hi = minOf(settings.maxValue, running - remainingAfter) // >= 1 по построению
                    val pool = candidates(hi).ifEmpty { (1..hi).toList() }
                    val value = pool[random.nextInt(pool.size)]
                    result += FlashStep(value, isPlus = false)
                    running -= value
                }
                return@repeat
            }

            var minus = settings.ops == OpMode.BOTH && running > 0 && random.nextBoolean()
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
