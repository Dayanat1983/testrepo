package com.fingermath.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.fingermath.domain.gesture.Finger
import com.fingermath.domain.gesture.NumberGesture
import com.fingermath.ui.components.HandGeometry as G

/**
 * Рука, нарисованная векторно (Canvas). Ресурсов-картинок нет: масштабируется на любой экран без потерь,
 * а состояние каждого пальца независимо — поэтому тот же компонент работает и для показа (Режим 1),
 * и для ввода (Режим 2, [onFingerClick] != null).
 *
 * [mirrored] = true рисует левую руку (большой палец справа).
 */
@Composable
fun HandView(
    counted: Set<Finger>,
    mirrored: Boolean,
    modifier: Modifier = Modifier,
    onFingerClick: ((Finger) -> Unit)? = null,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    idleColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    // Короткая анимация (80 мс): смена выглядит плавной, но не «размазывает» интервалы от 0.5 с.
    val progress: List<State<Float>> = Finger.entries.map { finger ->
        animateFloatAsState(
            targetValue = if (finger in counted) 1f else 0f,
            animationSpec = tween(durationMillis = 80),
            label = "finger-$finger",
        )
    }
    val palmColor = lerp(idleColor, Color.Black, 0.08f)
    val currentClick by rememberUpdatedState(onFingerClick)
    val tapModifier = if (onFingerClick != null) {
        Modifier.pointerInput(mirrored) {
            detectTapGestures { tap ->
                val s = size.width / G.W
                val x = if (mirrored) G.W - tap.x / s else tap.x / s
                G.fingerAt(x, tap.y / s)?.let { currentClick?.invoke(it) }
            }
        }
    } else {
        Modifier
    }

    Canvas(modifier.aspectRatio(G.W / G.H).then(tapModifier)) {
        val s = size.width / G.W
        withTransform({
            if (mirrored) {
                translate(left = size.width)
                scale(-s, s, pivot = Offset.Zero)
            } else {
                scale(s, s, pivot = Offset.Zero)
            }
        }) {
            // Четыре пальца: сложенный = короткий бугорок над ладонью, выставленный = длинный и цветной.
            for (finger in G.FOUR_FINGERS) {
                val t = progress[finger.ordinal].value
                val x = G.FINGER_X.getValue(finger)
                val length = G.FOLDED_LEN + (G.FINGER_LEN.getValue(finger) - G.FOLDED_LEN) * t
                drawLine(
                    color = lerp(idleColor, activeColor, t),
                    start = Offset(x, G.FINGER_BASE_Y),
                    end = Offset(x, G.FINGER_BASE_Y - length),
                    strokeWidth = G.FINGER_W,
                    cap = StrokeCap.Round,
                )
            }
            drawRoundRect(
                color = palmColor,
                topLeft = Offset(18f, 66f),
                size = Size(72f, 60f),
                cornerRadius = CornerRadius(16f, 16f),
            )
            // Большой палец поверх ладони: сложенный лежит на ней.
            val tt = progress[Finger.THUMB.ordinal].value
            drawLine(
                color = lerp(idleColor, activeColor, tt),
                start = Offset(G.THUMB_BASE_X, G.THUMB_BASE_Y),
                end = Offset(
                    G.THUMB_FOLDED_X + (G.THUMB_RAISED_X - G.THUMB_FOLDED_X) * tt,
                    G.THUMB_FOLDED_Y + (G.THUMB_RAISED_Y - G.THUMB_FOLDED_Y) * tt,
                ),
                strokeWidth = G.THUMB_W,
                cap = StrokeCap.Round,
            )
        }
    }
}

/** Режим 1: показ числа. Слева десятки, справа единицы (большие пальцы смотрят к центру). */
@Composable
fun NumberHands(gesture: NumberGesture, modifier: Modifier = Modifier) {
    HandsRow(
        tens = gesture.tens.pressed,
        ones = gesture.ones.pressed,
        modifier = modifier.semantics { contentDescription = "Число ${gesture.value}" },
    )
}

/** Режим 2: те же руки, но каждый палец нажимается. */
@Composable
fun InteractiveHands(
    tens: Set<Finger>,
    ones: Set<Finger>,
    enabled: Boolean,
    onTensClick: (Finger) -> Unit,
    onOnesClick: (Finger) -> Unit,
    modifier: Modifier = Modifier,
) {
    HandsRow(
        tens = tens,
        ones = ones,
        modifier = modifier,
        onTensClick = if (enabled) onTensClick else null,
        onOnesClick = if (enabled) onOnesClick else null,
    )
}

@Composable
private fun HandsRow(
    tens: Set<Finger>,
    ones: Set<Finger>,
    modifier: Modifier = Modifier,
    onTensClick: ((Finger) -> Unit)? = null,
    onOnesClick: ((Finger) -> Unit)? = null,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        HandView(tens, mirrored = true, modifier = Modifier.weight(1f), onFingerClick = onTensClick)
        HandView(ones, mirrored = false, modifier = Modifier.weight(1f), onFingerClick = onOnesClick)
    }
}
