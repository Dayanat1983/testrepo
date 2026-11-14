package com.fingermath.domain.input

import kotlin.random.Random

enum class OpMode { PLUS, MINUS, BOTH }

data class ArithmeticProblem(val a: Int, val b: Int, val isPlus: Boolean) {
    val answer: Int get() = if (isPlus) a + b else a - b
    val text: String get() = "$a ${if (isPlus) "+" else "−"} $b"
}

data class InputSettings(
    val rounds: Int = 10,
    val maxValue: Int = 99,
    val ops: OpMode = OpMode.BOTH,
) {
    init {
        require(rounds >= 1)
        require(maxValue in 9..99) // ответ обязан помещаться в 0..99 — столько показывают две руки
    }
}

/** Все операнды и ответ лежат в 1..maxValue, так что ответ всегда можно выложить пальцами. */
class ProblemGenerator(private val random: Random = Random.Default) {
    fun next(settings: InputSettings): ArithmeticProblem {
        val plus = when (settings.ops) {
            OpMode.PLUS -> true
            OpMode.MINUS -> false
            OpMode.BOTH -> random.nextBoolean()
        }
        val max = settings.maxValue
        return if (plus) {
            val a = random.nextInt(1, max)          // 1..max-1
            val b = random.nextInt(1, max - a + 1)  // сумма <= max
            ArithmeticProblem(a, b, true)
        } else {
            val a = random.nextInt(2, max + 1)      // 2..max
            val b = random.nextInt(1, a)            // разность >= 1
            ArithmeticProblem(a, b, false)
        }
    }
}
