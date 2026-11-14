package com.fingermath.ui.flash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fingermath.domain.flash.FlashDisplay
import com.fingermath.domain.flash.FlashSettings
import com.fingermath.domain.flash.asExpression
import com.fingermath.domain.input.OpMode
import com.fingermath.domain.gesture.GestureMapper
import com.fingermath.ui.components.NumberHands

@Composable
fun FlashScreen(viewModel: FlashViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Контент ограничен по ширине: на планшетах руки не растягиваются на весь экран.
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (val s = state) {
                is FlashUiState.Setup -> SetupContent(s.settings, viewModel)
                is FlashUiState.Countdown -> BigText("${s.seconds}")
                is FlashUiState.Showing -> {
                    Text("${s.index} / ${s.total}", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(24.dp))
                    // Пауза между показами: содержимое скрыто, но занимает то же место — макет не «прыгает».
                    val hidden = Modifier.alpha(if (s.step == null) 0f else 1f)
                    // Действие называется перед каждым шагом: пользователь знает, прибавлять или вычитать.
                    val step = s.step
                    val isMinus = step != null && !step.isPlus
                    Text(
                        text = when {
                            step?.isStart == true -> "Начальное число"
                            isMinus -> "Вычитание  −"
                            else -> "Сложение  +"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (isMinus) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = hidden,
                    )
                    Spacer(Modifier.height(16.dp))
                    val value = s.step?.value ?: 0
                    Column(hidden, horizontalAlignment = Alignment.CenterHorizontally) {
                        if (s.display != FlashDisplay.HANDS) {
                            Text(
                                text = value.toString(),
                                fontSize = if (s.display == FlashDisplay.BOTH) 72.sp else 120.sp,
                                style = MaterialTheme.typography.displayLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        if (s.display != FlashDisplay.DIGITS) {
                            NumberHands(GestureMapper.toGesture(value))
                        }
                    }
                }
                is FlashUiState.Answer -> AnswerContent(s, viewModel)
                is FlashUiState.Result -> ResultContent(s, viewModel)
            }
        }
    }
}

@Composable
private fun SetupContent(settings: FlashSettings, vm: FlashViewModel) {
    Text("Флеш-анзан", style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(24.dp))

    Text("Что показывать")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for ((mode, label) in listOf(FlashDisplay.HANDS to "Жесты", FlashDisplay.DIGITS to "Цифры", FlashDisplay.BOTH to "Оба")) {
            FilterChip(
                selected = settings.display == mode,
                onClick = { vm.updateSettings { it.copy(display = mode) } },
                label = { Text(label) },
            )
        }
    }
    Spacer(Modifier.height(12.dp))

    Text("Количество жестов: ${settings.count}")
    Slider(
        value = settings.count.toFloat(),
        onValueChange = { v -> vm.updateSettings { it.copy(count = v.toInt()) } },
        valueRange = 3f..20f,
        steps = 16,
    )
    Text("Время на жест: %.1f с".format(settings.intervalMs / 1000f))
    Slider(
        value = settings.intervalMs.toFloat(),
        onValueChange = { v -> vm.updateSettings { it.copy(intervalMs = (v / 100).toInt() * 100L) } },
        valueRange = 500f..2000f,
        steps = 14,
    )
    Text("Действия")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for ((sub, label) in listOf(OpMode.PLUS to "Только +", OpMode.BOTH to "+ и −", OpMode.MINUS to "Только −")) {
            FilterChip(
                selected = settings.ops == sub,
                onClick = { vm.updateSettings { it.copy(ops = sub) } },
                label = { Text(label) },
            )
        }
    }
    Spacer(Modifier.height(12.dp))
    Text("Числа")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for ((max, label) in listOf(9 to "1–9", 50 to "1–50", 99 to "1–99")) {
            FilterChip(
                selected = settings.maxValue == max,
                onClick = { vm.updateSettings { it.copy(maxValue = max) } },
                label = { Text(label) },
            )
        }
    }
    Spacer(Modifier.height(32.dp))
    Button(onClick = vm::start, modifier = Modifier.fillMaxWidth()) { Text("Старт") }
}

@Composable
private fun AnswerContent(s: FlashUiState.Answer, vm: FlashViewModel) {
    Text("Чему равна сумма?", style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(16.dp))
    OutlinedTextField(
        value = s.input,
        onValueChange = vm::onInputChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { vm.submit() }),
    )
    Spacer(Modifier.height(16.dp))
    Button(onClick = vm::submit, enabled = s.input.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
        Text("Проверить")
    }
}

@Composable
private fun ResultContent(s: FlashUiState.Result, vm: FlashViewModel) {
    Text(
        if (s.isCorrect) "Верно!" else "Неверно",
        style = MaterialTheme.typography.headlineLarge,
        color = if (s.isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
    )
    Spacer(Modifier.height(12.dp))
    Text("Ваш ответ: ${s.given ?: "—"}")
    Text("Правильный ответ: ${s.expected}")
    Spacer(Modifier.height(8.dp))
    Text(s.steps.asExpression(), textAlign = TextAlign.Center)
    Spacer(Modifier.height(24.dp))
    Button(onClick = vm::start, modifier = Modifier.fillMaxWidth()) { Text("Ещё раз") }
    Spacer(Modifier.height(8.dp))
    OutlinedButton(onClick = vm::reset, modifier = Modifier.fillMaxWidth()) { Text("Настройки") }
}

@Composable
private fun BigText(text: String) =
    Text(text, fontSize = 96.sp, style = MaterialTheme.typography.displayLarge)
