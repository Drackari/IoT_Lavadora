package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.WashingMachineScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.WashingViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val washingViewModel: WashingViewModel = viewModel()
                WashingMachineScreen(viewModel = washingViewModel)
            }
        }
    }
}
