package com.fingermath.ui.input

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fingermath.domain.input.InputSettings
import com.fingermath.domain.input.OpMode
import com.fingermath.ui.components.InteractiveHands

@Composable
fun InputScreen(viewModel: InputViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
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
                is InputUiState.Setup -> SetupContent(s.settings, viewModel)
                is InputUiState.Playing -> PlayingContent(s, viewModel)
                is InputUiState.Finished -> FinishedContent(s, viewModel)
            }
        }
    }
}

@Composable
private fun SetupContent(settings: InputSettings, vm: InputViewModel) {
    Text("Ввод пальцами", style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(24.dp))

    Text("Примеров: ${settings.rounds}")
    Slider(
        value = settings.rounds.toFloat(),
        onValueChange = { v -> vm.updateSettings { it.copy(rounds = v.toInt()) } },
        valueRange = 5f..20f,
        steps = 14,
    )
    Text("Числа")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for ((max, label) in listOf(9 to "до 9", 50 to "до 50", 99 to "до 99")) {
            FilterChip(
                selected = settings.maxValue == max,
                onClick = { vm.updateSettings { it.copy(maxValue = max) } },
                label = { Text(label) },
            )
        }
    }
    Spacer(Modifier.height(12.dp))
    Text("Действия")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for ((mode, label) in listOf(OpMode.PLUS to "+", OpMode.MINUS to "−", OpMode.BOTH to "+ и −")) {
            FilterChip(
                selected = settings.ops == mode,
                onClick = { vm.updateSettings { it.copy(ops = mode) } },
                label = { Text(label) },
            )
        }
    }
    Spacer(Modifier.height(32.dp))
    Button(onClick = vm::start, modifier = Modifier.fillMaxWidth()) { Text("Старт") }
}

@Composable
private fun PlayingContent(s: InputUiState.Playing, vm: InputViewModel) {
    Text("Пример ${s.round} / ${s.total}", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(12.dp))
    Text("${s.problem.text} = ?", style = MaterialTheme.typography.displayMedium)
    Spacer(Modifier.height(24.dp))

    InteractiveHands(
        tens = s.tens,
        ones = s.ones,
        enabled = s.verdict == null,
        onTensClick = { vm.toggle(HandSide.TENS, it) },
        onOnesClick = { vm.toggle(HandSide.ONES, it) },
    )
    Spacer(Modifier.height(8.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Text("Десятки", style = MaterialTheme.typography.labelLarge)
        Text("Единицы", style = MaterialTheme.typography.labelLarge)
    }
    Spacer(Modifier.height(24.dp))

    when (val v = s.verdict) {
        null -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = vm::clear, modifier = Modifier.weight(1f)) { Text("Сбросить") }
            Button(onClick = vm::check, modifier = Modifier.weight(1f)) { Text("Проверить") }
        }
        is Verdict.Correct -> {
            Text("Верно!", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            NextButton(s, vm)
        }
        is Verdict.Wrong -> {
            Text("Неверно", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.error)
            Text(
                if (v.invalid) "Такого жеста нет. Правильный ответ: ${v.expected}" else "Правильный ответ: ${v.expected}",
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            NextButton(s, vm)
        }
    }
}

@Composable
private fun NextButton(s: InputUiState.Playing, vm: InputViewModel) {
    Button(onClick = vm::next, modifier = Modifier.fillMaxWidth()) {
        Text(if (s.round >= s.total) "Завершить" else "Дальше")
    }
}

@Composable
private fun FinishedContent(s: InputUiState.Finished, vm: InputViewModel) {
    Text("Готово!", style = MaterialTheme.typography.headlineLarge)
    Spacer(Modifier.height(12.dp))
    Text("Верно: ${s.score} из ${s.total}", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(24.dp))
    Button(onClick = vm::start, modifier = Modifier.fillMaxWidth()) { Text("Ещё раз") }
    Spacer(Modifier.height(8.dp))
    OutlinedButton(onClick = vm::reset, modifier = Modifier.fillMaxWidth()) { Text("Настройки") }
}
