package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.GameScreen
import com.example.ui.GameViewModel
import com.example.ui.HowToPlayScreen
import com.example.ui.LevelSelectScreen
import com.example.ui.MainMenuScreen
import com.example.ui.RobotCollectionScreen
import com.example.ui.Screen
import com.example.ui.SettingsScreen
import com.example.ui.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SpaceDarkBg

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SpaceDarkBg
                ) {
                    NeonHopApp(viewModel = viewModel)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.soundSystem.pause()
    }

    override fun onResume() {
        super.onResume()
        viewModel.soundSystem.resume()
    }
}

@Composable
fun NeonHopApp(viewModel: GameViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    when (val screen = currentScreen) {
        is Screen.Splash -> {
            SplashScreen(
                viewModel = viewModel,
                onStartGame = { viewModel.navigateTo(Screen.MainMenu) }
            )
        }
        is Screen.MainMenu -> {
            MainMenuScreen(
                viewModel = viewModel,
                onPlay = {
                    val maxUnlocked = viewModel.maxUnlockedLevel.value
                    viewModel.startLevel(maxUnlocked)
                },
                onLevelSelect = { viewModel.navigateTo(Screen.LevelSelect) },
                onRobotCollection = { viewModel.navigateTo(Screen.RobotCollection) },
                onSettings = { viewModel.navigateTo(Screen.Settings) },
                onHowToPlay = { viewModel.navigateTo(Screen.HowToPlay) }
            )
        }
        is Screen.LevelSelect -> {
            LevelSelectScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo(Screen.MainMenu) },
                onSelectLevel = { levelId -> viewModel.startLevel(levelId) }
            )
        }
        is Screen.Gameplay -> {
            val engine by viewModel.engine.collectAsState()
            androidx.compose.runtime.key(engine) {
                GameScreen(
                    viewModel = viewModel,
                    levelId = screen.levelId,
                    onBackToMenu = { viewModel.navigateTo(Screen.MainMenu) },
                    onLevelSelect = { viewModel.navigateTo(Screen.LevelSelect) }
                )
            }
        }
        is Screen.RobotCollection -> {
            RobotCollectionScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo(Screen.MainMenu) }
            )
        }
        is Screen.Settings -> {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo(Screen.MainMenu) }
            )
        }
        is Screen.HowToPlay -> {
            HowToPlayScreen(
                onBack = { viewModel.navigateTo(Screen.MainMenu) }
            )
        }
    }
}
