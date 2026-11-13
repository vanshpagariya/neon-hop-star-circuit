package com.example.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Local persistent storage for Neon Hop: Star Circuit.
 * Manages player progression, level stars/scores, banked crystals,
 * unlocked skins, and audio/haptic preferences across sessions.
 */
class GamePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("neon_hop_save_data", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_MAX_UNLOCKED_LEVEL = "max_unlocked_level"
        private const val KEY_COMPLETED_LEVELS = "completed_levels"
        private const val KEY_TOTAL_CRYSTALS = "total_crystals"
        private const val KEY_EQUIPPED_SKIN = "equipped_skin"
        private const val KEY_UNLOCKED_SKINS = "unlocked_skins"
        private const val KEY_DASH_UNLOCKED = "dash_unlocked"
        private const val KEY_TOTAL_PLAY_TIME = "total_play_time"

        private const val KEY_SFX_ENABLED = "sfx_enabled"
        private const val KEY_MUSIC_ENABLED = "music_enabled"
        private const val KEY_HAPTICS_ENABLED = "haptics_enabled"

        const val DEFAULT_SKIN_ID = "cyan_pulse"
    }

    var maxUnlockedLevel: Int
        get() = prefs.getInt(KEY_MAX_UNLOCKED_LEVEL, 1)
        set(value) {
            prefs.edit().putInt(KEY_MAX_UNLOCKED_LEVEL, value).commit()
        }

    fun isLevelUnlocked(levelId: Int): Boolean {
        return levelId <= maxUnlockedLevel
    }

    fun unlockNextLevel(completedLevelId: Int) {
        val nextLevel = completedLevelId + 1
        if (nextLevel > maxUnlockedLevel && nextLevel <= 5) {
            maxUnlockedLevel = nextLevel
        }
        // Unlock dash ability after sector 1
        if (completedLevelId >= 1) {
            isDashUnlocked = true
        }
        markLevelCompleted(completedLevelId)
    }

    fun isLevelCompleted(levelId: Int): Boolean {
        val set = prefs.getStringSet(KEY_COMPLETED_LEVELS, emptySet()) ?: emptySet()
        return set.contains(levelId.toString())
    }

    fun markLevelCompleted(levelId: Int) {
        val set = (prefs.getStringSet(KEY_COMPLETED_LEVELS, emptySet()) ?: emptySet()).toMutableSet()
        set.add(levelId.toString())
        prefs.edit().putStringSet(KEY_COMPLETED_LEVELS, set).commit()
    }

    fun getLevelHighScore(levelId: Int): Int {
        return prefs.getInt("high_score_lvl_$levelId", 0)
    }

    fun saveLevelScore(levelId: Int, score: Int): Boolean {
        val currentBest = getLevelHighScore(levelId)
        if (score > currentBest) {
            prefs.edit().putInt("high_score_lvl_$levelId", score).commit()
            return true // New high score
        }
        return false
    }

    fun getLevelStars(levelId: Int): Int {
        return prefs.getInt("stars_lvl_$levelId", 0)
    }

    fun saveLevelStars(levelId: Int, stars: Int) {
        val currentBest = getLevelStars(levelId)
        if (stars > currentBest) {
            prefs.edit().putInt("stars_lvl_$levelId", stars).commit()
        }
    }

    fun getEndlessHighScore(): Int {
        return prefs.getInt("endless_high_score", 0)
    }

    fun saveEndlessHighScore(score: Int): Boolean {
        val currentBest = getEndlessHighScore()
        if (score > currentBest) {
            prefs.edit().putInt("endless_high_score", score).commit()
            return true
        }
        return false
    }

    var totalCrystals: Int
        get() = prefs.getInt(KEY_TOTAL_CRYSTALS, 0)
        set(value) {
            prefs.edit().putInt(KEY_TOTAL_CRYSTALS, value.coerceAtLeast(0)).commit()
        }

    fun addCrystals(amount: Int) {
        if (amount > 0) {
            totalCrystals += amount
        }
    }

    fun spendCrystals(cost: Int): Boolean {
        if (totalCrystals >= cost) {
            totalCrystals -= cost
            return true
        }
        return false
    }

    var equippedSkinId: String
        get() = prefs.getString(KEY_EQUIPPED_SKIN, DEFAULT_SKIN_ID) ?: DEFAULT_SKIN_ID
        set(value) {
            prefs.edit().putString(KEY_EQUIPPED_SKIN, value).commit()
        }

    fun getUnlockedSkins(): Set<String> {
        val set = prefs.getStringSet(KEY_UNLOCKED_SKINS, null)
        return set ?: setOf(DEFAULT_SKIN_ID)
    }

    fun unlockSkin(skinId: String) {
        val set = getUnlockedSkins().toMutableSet()
        set.add(skinId)
        prefs.edit().putStringSet(KEY_UNLOCKED_SKINS, set).commit()
    }

    fun isSkinUnlocked(skinId: String): Boolean {
        return skinId == DEFAULT_SKIN_ID || getUnlockedSkins().contains(skinId)
    }

    var isDashUnlocked: Boolean
        get() = prefs.getBoolean(KEY_DASH_UNLOCKED, true) // Default true or unlocked for rich gameplay
        set(value) {
            prefs.edit().putBoolean(KEY_DASH_UNLOCKED, value).commit()
        }

    var totalPlayTimeSeconds: Long
        get() = prefs.getLong(KEY_TOTAL_PLAY_TIME, 0L)
        set(value) {
            prefs.edit().putLong(KEY_TOTAL_PLAY_TIME, value).commit()
        }

    fun addPlayTime(seconds: Long) {
        totalPlayTimeSeconds += seconds
    }

    var sfxEnabled: Boolean
        get() = prefs.getBoolean(KEY_SFX_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_SFX_ENABLED, value).commit()
        }

    var musicEnabled: Boolean
        get() = prefs.getBoolean(KEY_MUSIC_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_MUSIC_ENABLED, value).commit()
        }

    var hapticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, value).commit()
        }

    fun resetAllProgress() {
        prefs.edit().clear().commit()
        unlockSkin(DEFAULT_SKIN_ID)
        equippedSkinId = DEFAULT_SKIN_ID
        maxUnlockedLevel = 1
        isDashUnlocked = true
    }
}
