package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AppThemeMode
import com.example.ui.screens.EditParkingScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SaveParkingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ParkedTheme
import com.example.viewmodel.ParkingViewModel
import com.example.viewmodel.ParkingViewModelFactory

sealed class Screen {
    data object Home : Screen()
    data object SaveParking : Screen()
    data object EditParking : Screen()
    data object Settings : Screen()
    data object History : Screen()
}

class MainActivity : ComponentActivity() {

    private val viewModel: ParkingViewModel by viewModels {
        ParkingViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val preferences by viewModel.preferences.collectAsStateWithLifecycle()
            val activeParking by viewModel.activeParking.collectAsStateWithLifecycle()

            val isDarkTheme = when (preferences.themeMode) {
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            ParkedTheme(darkTheme = isDarkTheme) {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

                BackHandler(enabled = currentScreen != Screen.Home) {
                    currentScreen = Screen.Home
                }

                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        (fadeIn() + slideInHorizontally { width -> width / 5 } + scaleIn(initialScale = 0.98f)) togetherWith
                            (fadeOut() + slideOutHorizontally { width -> -width / 5 } + scaleOut(targetScale = 0.98f))
                    },
                    modifier = Modifier.fillMaxSize(),
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        is Screen.Home -> {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToSave = { currentScreen = Screen.SaveParking },
                                onNavigateToEdit = { currentScreen = Screen.EditParking },
                                onNavigateToSettings = { currentScreen = Screen.Settings },
                                onNavigateToHistory = { currentScreen = Screen.History }
                            )
                        }

                        is Screen.SaveParking -> {
                            SaveParkingScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = Screen.Home },
                                onSaved = { currentScreen = Screen.Home }
                            )
                        }

                        is Screen.EditParking -> {
                            activeParking?.let { parking ->
                                EditParkingScreen(
                                    parking = parking,
                                    viewModel = viewModel,
                                    onNavigateBack = { currentScreen = Screen.Home },
                                    onSaved = { currentScreen = Screen.Home }
                                )
                            } ?: run {
                                currentScreen = Screen.Home
                            }
                        }

                        is Screen.Settings -> {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = Screen.Home }
                            )
                        }

                        is Screen.History -> {
                            HistoryScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = Screen.Home }
                            )
                        }
                    }
                }
            }
        }
    }
}
