package com.fingermath.domain.gesture

/** Пальцы одной руки. Порядок = порядок слева направо при взгляде на правую руку (большой слева). */
enum class Finger(val unitWeight: Int) {
    THUMB(5), INDEX(1), MIDDLE(1), RING(1), PINKY(1)
}

/**
 * Жест одной руки для цифры 0..9 по методу Чисанбоп.
 * Таблица соответствий явная (а не вычисляемая), чтобы её можно было прочитать и проверить глазами;
 * инвариант «сумма весов == цифра» проверяется в init и в тестах.
 */
enum class HandGesture(val digit: Int, vararg counted: Finger) {
    ZERO(0),
    ONE(1, Finger.INDEX),
    TWO(2, Finger.INDEX, Finger.MIDDLE),
    THREE(3, Finger.INDEX, Finger.MIDDLE, Finger.RING),
    FOUR(4, Finger.INDEX, Finger.MIDDLE, Finger.RING, Finger.PINKY),
    FIVE(5, Finger.THUMB),
    SIX(6, Finger.THUMB, Finger.INDEX),
    SEVEN(7, Finger.THUMB, Finger.INDEX, Finger.MIDDLE),
    EIGHT(8, Finger.THUMB, Finger.INDEX, Finger.MIDDLE, Finger.RING),
    NINE(9, Finger.THUMB, Finger.INDEX, Finger.MIDDLE, Finger.RING, Finger.PINKY);

    /** Пальцы, участвующие в счёте (в рисунке — «выставленные»). */
    val pressed: Set<Finger> = counted.toSet()

    init {
        check(pressed.sumOf { it.unitWeight } == digit) { "Bad gesture table for digit $digit" }
    }

    companion object {
        private val byDigit: Map<Int, HandGesture> = entries.associateBy { it.digit }

        fun ofDigit(digit: Int): HandGesture =
            requireNotNull(byDigit[digit]) { "Digit must be in 0..9, was $digit" }

        /** Обратное отображение для Режима 2 (ввод пальцами): null — такой комбинации нет. */
        fun fromFingers(fingers: Set<Finger>): HandGesture? = entries.firstOrNull { it.pressed == fingers }
    }
}
