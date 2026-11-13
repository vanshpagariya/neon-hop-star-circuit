package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.HapticSystem
import com.example.audio.SoundSystem
import com.example.data.GamePreferences
import com.example.game.GameEngine
import com.example.game.Levels
import com.example.game.model.RobotSkin
import com.example.game.model.RobotSkins
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class Screen {
    data object Splash : Screen()
    data object MainMenu : Screen()
    data object LevelSelect : Screen()
    data class Gameplay(val levelId: Int) : Screen()
    data object RobotCollection : Screen()
    data object Settings : Screen()
    data object HowToPlay : Screen()
}

/**
 * Main state coordinator and game lifecycle manager.
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    val prefs = GamePreferences(application)
    val soundSystem = SoundSystem()
    val hapticSystem = HapticSystem(application)

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _engine = MutableStateFlow<GameEngine?>(null)
    val engine: StateFlow<GameEngine?> = _engine.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _totalCrystals = MutableStateFlow(prefs.totalCrystals)
    val totalCrystals: StateFlow<Int> = _totalCrystals.asStateFlow()

    private val _maxUnlockedLevel = MutableStateFlow(prefs.maxUnlockedLevel)
    val maxUnlockedLevel: StateFlow<Int> = _maxUnlockedLevel.asStateFlow()

    private val _equippedSkin = MutableStateFlow(RobotSkins.getSkin(prefs.equippedSkinId))
    val equippedSkin: StateFlow<RobotSkin> = _equippedSkin.asStateFlow()

    private val _unlockedSkins = MutableStateFlow(prefs.getUnlockedSkins())
    val unlockedSkins: StateFlow<Set<String>> = _unlockedSkins.asStateFlow()

    private val _sfxEnabled = MutableStateFlow(prefs.sfxEnabled)
    val sfxEnabled: StateFlow<Boolean> = _sfxEnabled.asStateFlow()

    private val _musicEnabled = MutableStateFlow(prefs.musicEnabled)
    val musicEnabled: StateFlow<Boolean> = _musicEnabled.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(prefs.hapticsEnabled)
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    init {
        soundSystem.sfxEnabled = prefs.sfxEnabled
        soundSystem.musicEnabled = prefs.musicEnabled
        hapticSystem.hapticsEnabled = prefs.hapticsEnabled
    }

    fun navigateTo(screen: Screen) {
        soundSystem.playClick()
        hapticSystem.click()

        if (screen !is Screen.Gameplay) {
            _engine.value = null
            _isPaused.value = false
        }
        _currentScreen.value = screen
    }

    fun startLevel(levelId: Int) {
        soundSystem.playClick()
        hapticSystem.click()

        val levelDef = Levels.getLevel(levelId)
        val newEngine = GameEngine(levelDef).apply {
            onSoundJump = { soundSystem.playJump() }
            onSoundDoubleJump = { soundSystem.playDoubleJump() }
            onSoundCrystal = { soundSystem.playCrystal() }
            onSoundPowerUp = { soundSystem.playPowerUp() }
            onSoundHazard = { soundSystem.playHazard() }
            onSoundDash = { soundSystem.playDash() }
            onSoundVictory = { soundSystem.playLevelComplete() }

            onHapticJump = { hapticSystem.jump() }
            onHapticCollect = { hapticSystem.collect() }
            onHapticHazard = { hapticSystem.hazard() }
            onHapticVictory = { hapticSystem.victory() }
        }

        _engine.value = newEngine
        _isPaused.value = false
        _currentScreen.value = Screen.Gameplay(levelId)
    }

    fun restartCurrentLevel() {
        soundSystem.playClick()
        hapticSystem.click()
        val current = _currentScreen.value
        val levelIdToRestart = if (current is Screen.Gameplay) {
            current.levelId
        } else {
            _engine.value?.level?.id ?: 1
        }
        startLevel(levelIdToRestart)
    }

    fun pauseGame() {
        _isPaused.value = true
        soundSystem.pause()
    }

    fun resumeGame() {
        _isPaused.value = false
        soundSystem.resume()
    }

    fun onLevelVictory(levelId: Int, finalScore: Int, crystals: Int, timeTaken: Float) {
        viewModelScope.launch {
            // Save completion & unlock next level
            prefs.unlockNextLevel(levelId)
            _maxUnlockedLevel.value = prefs.maxUnlockedLevel

            // Calculate Stars (1-3)
            val levelDef = Levels.getLevel(levelId)
            var stars = 1
            if (crystals >= (levelDef.crystals.size * 0.75f).toInt()) {
                stars = 2
            }
            if (crystals >= levelDef.crystals.size && timeTaken <= levelDef.targetTimeSeconds) {
                stars = 3
            }

            prefs.saveLevelStars(levelId, stars)
            prefs.saveLevelScore(levelId, finalScore)

            // Bank crystals
            prefs.addCrystals(crystals)
            _totalCrystals.value = prefs.totalCrystals

            // Accumulate play time
            prefs.addPlayTime(timeTaken.toLong())
        }
    }

    fun onLevelFailed(crystals: Int, timeTaken: Float) {
        viewModelScope.launch {
            val eng = _engine.value
            if (eng != null && eng.isEndlessMode) {
                prefs.saveEndlessHighScore(eng.currentScore)
            }
            // Player keeps crystals collected before falling
            prefs.addCrystals(crystals)
            _totalCrystals.value = prefs.totalCrystals
            prefs.addPlayTime(timeTaken.toLong())
        }
    }

    fun buySkin(skin: RobotSkin): Boolean {
        soundSystem.playClick()
        hapticSystem.click()

        if (prefs.spendCrystals(skin.cost)) {
            prefs.unlockSkin(skin.id)
            prefs.equippedSkinId = skin.id
            _totalCrystals.value = prefs.totalCrystals
            _unlockedSkins.value = prefs.getUnlockedSkins()
            _equippedSkin.value = skin
            soundSystem.playPowerUp()
            hapticSystem.victory()
            return true
        }
        return false
    }

    fun equipSkin(skin: RobotSkin) {
        soundSystem.playClick()
        hapticSystem.click()
        if (prefs.isSkinUnlocked(skin.id)) {
            prefs.equippedSkinId = skin.id
            _equippedSkin.value = skin
        }
    }

    fun toggleSfx() {
        val newVal = !prefs.sfxEnabled
        prefs.sfxEnabled = newVal
        _sfxEnabled.value = newVal
        soundSystem.sfxEnabled = newVal
        if (newVal) soundSystem.playJump()
    }

    fun toggleMusic() {
        val newVal = !prefs.musicEnabled
        prefs.musicEnabled = newVal
        _musicEnabled.value = newVal
        soundSystem.musicEnabled = newVal
    }

    fun toggleHaptics() {
        val newVal = !prefs.hapticsEnabled
        prefs.hapticsEnabled = newVal
        _hapticsEnabled.value = newVal
        hapticSystem.hapticsEnabled = newVal
        if (newVal) hapticSystem.jump()
    }

    fun resetAllProgress() {
        prefs.resetAllProgress()
        _totalCrystals.value = prefs.totalCrystals
        _maxUnlockedLevel.value = prefs.maxUnlockedLevel
        _equippedSkin.value = RobotSkins.getSkin(RobotSkins.ALL.first().id)
        _unlockedSkins.value = prefs.getUnlockedSkins()
        soundSystem.playClick()
        hapticSystem.hazard()
    }

    override fun onCleared() {
        super.onCleared()
        soundSystem.release()
    }
}
