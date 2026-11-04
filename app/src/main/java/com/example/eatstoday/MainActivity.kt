package com.example.eatstoday

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eatstoday.data.model.AppSettings
import com.example.eatstoday.navigation.AppNavigation
import com.example.eatstoday.ui.theme.EatsTodayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val userRepository = (application as EatsTodayApp).container.userRepository
        setContent {
            val settings by userRepository.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
            EatsTodayTheme(themeMode = settings.theme, textSize = settings.textSize) {
                AppNavigation(userRepository = userRepository)
            }
        }
    }
}
