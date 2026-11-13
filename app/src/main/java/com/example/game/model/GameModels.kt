package com.example.game.model

import androidx.compose.ui.graphics.Color

enum class PowerUpType(val displayName: String, val durationSec: Float, val badgeColor: Color) {
    NONE("None", 0f, Color.Transparent),
    LIGHT_BURST("Light Burst", 8.0f, Color(0xFF00F0FF)),
    SAFE_LANDING("Safe Landing", 10.0f, Color(0xFF00E676)),
    CRYSTAL_MAGNET("Crystal Magnet", 8.0f, Color(0xFFFF007F))
}

data class PowerUpItem(
    val id: Int,
    var x: Float,
    var y: Float,
    val type: PowerUpType,
    val radius: Float = 14f,
    var collected: Boolean = false,
    var bobPhase: Float = 0f
)

enum class PlatformType {
    SOLID,
    ONE_WAY,
    MOVING_X,
    MOVING_Y,
    TIMED_ENERGY
}

data class Platform(
    var x: Float,
    var y: Float,
    val width: Float,
    val height: Float,
    val type: PlatformType = PlatformType.SOLID,
    val moveMin: Float = 0f,
    val moveMax: Float = 0f,
    val moveSpeed: Float = 60f,
    var moveDir: Float = 1f,
    val activeDuration: Float = 2.0f,
    val inactiveDuration: Float = 1.5f,
    var timer: Float = 0f,
    var isCycleActive: Boolean = true,
    val color: Color = Color(0xFF00F0FF)
) {
    // Platform bounds
    val left: Float get() = x
    val right: Float get() = x + width
    val top: Float get() = y
    val bottom: Float get() = y + height
}

enum class HazardType {
    SPIKE,
    LASER_VERTICAL,
    LASER_HORIZONTAL,
    ROTATING_SPINNER,
    VOID_ZONE
}

data class Hazard(
    val type: HazardType,
    var x: Float = 0f,
    var y: Float = 0f,
    val width: Float = 0f,
    val height: Float = 0f,
    val centerX: Float = 0f,
    val centerY: Float = 0f,
    val armLength: Float = 48f,
    var angle: Float = 0f,
    val angularSpeed: Float = 2.0f,
    val cyclePeriod: Float = 3.0f,
    var cycleTimer: Float = 0f,
    var isPulsingActive: Boolean = true,
    val color: Color = Color(0xFFFF007F)
) {
    val left: Float get() = x
    val right: Float get() = x + width
    val top: Float get() = y
    val bottom: Float get() = y + height
}

data class Crystal(
    val id: Int,
    var x: Float,
    var y: Float,
    val isSecret: Boolean = false,
    var collected: Boolean = false,
    var bobPhase: Float = 0f,
    val radius: Float = 10f
)

data class ExitPortal(
    val x: Float,
    val y: Float,
    val radius: Float = 28f,
    var rotation: Float = 0f,
    var isOpen: Boolean = true
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    var life: Float,
    val maxLife: Float,
    val size: Float
)

data class FloatingText(
    var x: Float,
    var y: Float,
    val text: String,
    val color: Color,
    var life: Float = 1.0f,
    val maxLife: Float = 1.0f
)

data class Player(
    var x: Float = 0f,
    var y: Float = 0f,
    val width: Float = 28f,
    val height: Float = 34f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var isGrounded: Boolean = false,
    var jumpCount: Int = 0,
    var facingRight: Boolean = true,
    var isDashing: Boolean = false,
    var dashTimer: Float = 0f,
    var dashCooldown: Float = 0f,
    var activePowerUp: PowerUpType = PowerUpType.NONE,
    var powerUpTimer: Float = 0f,
    var hasShield: Boolean = false,
    var invincibilityTimer: Float = 0f,
    var eyeBlinkTimer: Float = 2f,
    var isEyeClosed: Boolean = false,
    var squashStretch: Float = 1.0f,
    var standingOnPlatform: Platform? = null,
    var coyoteTimer: Float = 0f,
    var jumpBufferTimer: Float = 0f,
    var tiltAngle: Float = 0f,
    var happyEyeTimer: Float = 0f
) {
    val left: Float get() = x
    val right: Float get() = x + width
    val top: Float get() = y
    val bottom: Float get() = y + height
    val centerX: Float get() = x + width / 2f
    val centerY: Float get() = y + height / 2f
}

data class LevelDef(
    val id: Int,
    val title: String,
    val subtitle: String,
    val description: String,
    val worldWidth: Float,
    val worldHeight: Float,
    val playerStartX: Float,
    val playerStartY: Float,
    val targetTimeSeconds: Float,
    val platforms: List<Platform>,
    val hazards: List<Hazard>,
    val crystals: List<Crystal>,
    val powerUps: List<PowerUpItem>,
    val portal: ExitPortal,
    val themeColor: Color
)
