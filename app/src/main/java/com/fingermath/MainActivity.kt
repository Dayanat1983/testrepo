package com.fingermath

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fingermath.ui.flash.FlashScreen
import com.fingermath.ui.flash.FlashViewModel
import com.fingermath.ui.input.InputScreen
import com.fingermath.ui.input.InputViewModel
import com.fingermath.ui.theme.FingerMathTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FingerMathTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppRoot()
                }
            }
        }
    }
}

private enum class Screen { HOME, FLASH, INPUT }

@Composable
private fun AppRoot() {
    var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
    when (screen) {
        Screen.HOME -> HomeScreen(
            onFlash = { screen = Screen.FLASH },
            onInput = { screen = Screen.INPUT },
        )
        Screen.FLASH -> {
            val vm: FlashViewModel = viewModel()
            // Back останавливает сессию и возвращает на главный экран.
            BackHandler {
                vm.reset()
                screen = Screen.HOME
            }
            FlashScreen(vm)
        }
        Screen.INPUT -> {
            val vm: InputViewModel = viewModel()
            BackHandler {
                vm.reset()
                screen = Screen.HOME
            }
            InputScreen(vm)
        }
    }
}

@Composable
private fun HomeScreen(onFlash: () -> Unit, onInput: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Finger Math", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onFlash, modifier = Modifier.fillMaxWidth()) { Text("Режим 1: жесты → сумма") }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onInput, modifier = Modifier.fillMaxWidth()) {
            Text("Режим 2: пример → пальцы")
        }
    }
}
