package com.fingermath.domain.gesture

/** Пара рук: левая = десятки, правая = единицы. */
data class NumberGesture(val tens: HandGesture, val ones: HandGesture) {
    val value: Int get() = tens.digit * 10 + ones.digit
}

/** Единственная точка, где число 0..99 превращается в жесты (и обратно). */
object GestureMapper {
    const val MAX_VALUE = 99

    // Таблица из 100 готовых значений: смена жеста в UI не аллоцирует и ничего не считает.
    private val table: Array<NumberGesture> = Array(MAX_VALUE + 1) {
        NumberGesture(HandGesture.ofDigit(it / 10), HandGesture.ofDigit(it % 10))
    }

    fun toGesture(value: Int): NumberGesture {
        require(value in 0..MAX_VALUE) { "Value must be in 0..$MAX_VALUE, was $value" }
        return table[value]
    }

    fun toValue(tens: Set<Finger>, ones: Set<Finger>): Int? {
        val t = HandGesture.fromFingers(tens) ?: return null
        val o = HandGesture.fromFingers(ones) ?: return null
        return t.digit * 10 + o.digit
    }
}
