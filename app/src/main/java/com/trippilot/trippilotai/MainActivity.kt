package com.trippilot.trippilotai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.trippilot.trippilotai.ui.TripPilotApp
import com.trippilot.trippilotai.ui.theme.TripPilotAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TripPilotAITheme {
                TripPilotApp()
            }
        }
    }
}
