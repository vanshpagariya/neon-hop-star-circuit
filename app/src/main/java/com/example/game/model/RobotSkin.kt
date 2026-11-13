package com.example.game.model

import androidx.compose.ui.graphics.Color

/**
 * Robot cosmetic skin options for NOVA.
 * Purchased using banked energy crystals collected across levels.
 */
data class RobotSkin(
    val id: String,
    val name: String,
    val description: String,
    val cost: Int,
    val bodyColor: Color,
    val visorColor: Color,
    val eyeColor: Color,
    val accentColor: Color,
    val glowColor: Color,
    val trailColor: Color
)

object RobotSkins {
    val ALL = listOf(
        RobotSkin(
            id = "cyan_pulse",
            name = "Cyan Pulse",
            description = "The classic star circuit explorer. Crisp metallic alloy with glowing cyan optics.",
            cost = 0,
            bodyColor = Color(0xFF1E2640),
            visorColor = Color(0xFF0A0F1D),
            eyeColor = Color(0xFF00F0FF),
            accentColor = Color(0xFF00E5FF),
            glowColor = Color(0x6600F0FF),
            trailColor = Color(0xFF00E5FF)
        ),
        RobotSkin(
            id = "magenta_flare",
            name = "Magenta Flare",
            description = "Infused with raw plasma energy. High-contrast neon pink and obsidian chassis.",
            cost = 300,
            bodyColor = Color(0xFF2D162B),
            visorColor = Color(0xFF150814),
            eyeColor = Color(0xFFFF007F),
            accentColor = Color(0xFFFF4081),
            glowColor = Color(0x66FF007F),
            trailColor = Color(0xFFFF007F)
        ),
        RobotSkin(
            id = "electric_gold",
            name = "Electric Gold",
            description = "High-voltage reactor armor. Radiates brilliant solar energy and cybernetic sparks.",
            cost = 600,
            bodyColor = Color(0xFF332A15),
            visorColor = Color(0xFF1A1408),
            eyeColor = Color(0xFFFFD600),
            accentColor = Color(0xFFFFAB00),
            glowColor = Color(0x66FFD600),
            trailColor = Color(0xFFFFD600)
        ),
        RobotSkin(
            id = "void_phantom",
            name = "Void Phantom",
            description = "Tuned to deep-space dark matter. Luminous spectral violet with an eerie ethereal aura.",
            cost = 1000,
            bodyColor = Color(0xFF23143B),
            visorColor = Color(0xFF0F081C),
            eyeColor = Color(0xFFB388FF),
            accentColor = Color(0xFF7C4DFF),
            glowColor = Color(0x669D00FF),
            trailColor = Color(0xFFB388FF)
        ),
        RobotSkin(
            id = "solar_matrix",
            name = "Solar Matrix",
            description = "Overclocked hyper-core frame. Fiery neon orange and emerald green reactor conduits.",
            cost = 1500,
            bodyColor = Color(0xFF381F14),
            visorColor = Color(0xFF1A0E08),
            eyeColor = Color(0xFF00E676),
            accentColor = Color(0xFFFF6D00),
            glowColor = Color(0x66FF6D00),
            trailColor = Color(0xFF00E676)
        )
    )

    fun getSkin(id: String): RobotSkin {
        return ALL.find { it.id == id } ?: ALL.first()
    }
}
