package com.fingermath.ui.components

import androidx.compose.runtime.State
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.fingermath.domain.gesture.Finger
import com.fingermath.domain.gesture.HandGesture
import com.fingermath.domain.gesture.NumberGesture

/**
 * Рука, нарисованная векторно (Canvas) в виртуальной системе координат 100x130.
 * Ресурсов-картинок нет: масштабируется на любой экран без потерь, а состояние каждого пальца —
 * независимый параметр, поэтому тот же компонент подойдёт для интерактивного Режима 2.
 *
 * [mirrored] = true рисует левую руку (большой палец справа).
 */
@Composable
fun HandView(
    counted: Set<Finger>,
    mirrored: Boolean,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    idleColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    // Короткая анимация (80 мс) — смена выглядит плавной, но не «размазывает» быстрые интервалы 0.5 с.
    val progress: List<State<Float>> = Finger.entries.map { finger ->
        animateFloatAsState(
            targetValue = if (finger in counted) 1f else 0f,
            animationSpec = tween(durationMillis = 80),
            label = "finger-$finger",
        )
    }
    val palmColor = lerp(idleColor, Color.Black, 0.08f)

    Canvas(modifier.aspectRatio(W / H)) {
        val s = size.width / W
        withTransform({
            if (mirrored) {
                translate(left = size.width)
                scale(-s, s, pivot = Offset.Zero)
            } else {
                scale(s, s, pivot = Offset.Zero)
            }
        }) {
            // Четыре пальца: сложенный = короткий бугорок над ладонью, выставленный = длинный и цветной.
            for (finger in FOUR_FINGERS) {
                val t = progress[finger.ordinal].value
                val x = FINGER_X.getValue(finger)
                val length = FOLDED_LEN + (FINGER_LEN.getValue(finger) - FOLDED_LEN) * t
                drawLine(
                    color = lerp(idleColor, activeColor, t),
                    start = Offset(x, FINGER_BASE_Y),
                    end = Offset(x, FINGER_BASE_Y - length),
                    strokeWidth = FINGER_W,
                    cap = StrokeCap.Round,
                )
            }
            drawRoundRect(
                color = palmColor,
                topLeft = Offset(18f, 66f),
                size = Size(72f, 60f),
                cornerRadius = CornerRadius(16f, 16f),
            )
            // Большой палец рисуем поверх ладони: сложенный лежит на ней.
            val tt = progress[Finger.THUMB.ordinal].value
            drawLine(
                color = lerp(idleColor, activeColor, tt),
                start = THUMB_BASE,
                end = Offset(
                    THUMB_FOLDED.x + (THUMB_RAISED.x - THUMB_FOLDED.x) * tt,
                    THUMB_FOLDED.y + (THUMB_RAISED.y - THUMB_FOLDED.y) * tt,
                ),
                strokeWidth = THUMB_W,
                cap = StrokeCap.Round,
            )
        }
    }
}

/** Обе руки числа: слева десятки, справа единицы (большие пальцы смотрят к центру). */
@Composable
fun NumberHands(gesture: NumberGesture, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Число ${gesture.value}" },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HandView(gesture.tens.pressed, mirrored = true, modifier = Modifier.weight(1f))
        HandView(gesture.ones.pressed, mirrored = false, modifier = Modifier.weight(1f))
    }
}

@Composable
fun HandView(gesture: HandGesture, mirrored: Boolean, modifier: Modifier = Modifier) =
    HandView(gesture.pressed, mirrored, modifier)

private const val W = 100f
private const val H = 130f
private const val FINGER_W = 13f
private const val THUMB_W = 14f
private const val FINGER_BASE_Y = 72f
private const val FOLDED_LEN = 10f
private val FOUR_FINGERS = listOf(Finger.INDEX, Finger.MIDDLE, Finger.RING, Finger.PINKY)
private val FINGER_X = mapOf(Finger.INDEX to 30f, Finger.MIDDLE to 45f, Finger.RING to 60f, Finger.PINKY to 75f)
private val FINGER_LEN = mapOf(Finger.INDEX to 56f, Finger.MIDDLE to 62f, Finger.RING to 56f, Finger.PINKY to 44f)
private val THUMB_BASE = Offset(26f, 104f)
private val THUMB_FOLDED = Offset(38f, 98f)
private val THUMB_RAISED = Offset(10f, 74f)
