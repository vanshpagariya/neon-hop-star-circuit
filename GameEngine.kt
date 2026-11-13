package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.game.model.Crystal
import com.example.game.model.ExitPortal
import com.example.game.model.FloatingText
import com.example.game.model.Hazard
import com.example.game.model.HazardType
import com.example.game.model.LevelDef
import com.example.game.model.Particle
import com.example.game.model.Platform
import com.example.game.model.PlatformType
import com.example.game.model.Player
import com.example.game.model.PowerUpItem
import com.example.game.model.PowerUpType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Core 2D physics and gameplay engine for Neon Hop: Star Circuit.
 * Features frame-independent physics, smooth movement, double-jumping,
 * one-way & moving platforms, timed laser hazards, magnetic crystal attraction,
 * coyote time, jump buffering, and reactive particle systems.
 */
class GameEngine(val level: LevelDef) {

    companion object {
        private const val GRAVITY = 1000f
        private const val MAX_FALL_SPEED = 720f
        private const val MOVE_ACCEL = 1700f
        private const val MOVE_DECEL = 1400f
        private const val BASE_MAX_SPEED = 240f
        private const val BURST_MAX_SPEED = 370f
        private const val JUMP_VELOCITY = -420f
        private const val DOUBLE_JUMP_VELOCITY = -380f
        private const val DASH_SPEED = 520f
        private const val DASH_DURATION = 0.16f
        private const val DASH_COOLDOWN = 0.9f
    }

    val player: Player = Player(
        x = level.playerStartX,
        y = level.playerStartY
    )

    // Deep-copied level entities for state isolation
    val platforms: MutableList<Platform> = level.platforms.map { it.copy() }.toMutableList()
    val hazards: MutableList<Hazard> = level.hazards.map { it.copy() }.toMutableList()
    val crystals: MutableList<Crystal> = level.crystals.map { it.copy() }.toMutableList()
    val powerUps: MutableList<PowerUpItem> = level.powerUps.map { it.copy() }.toMutableList()
    val portal: ExitPortal = level.portal.copy()

    val particles: MutableList<Particle> = ArrayList(128)
    val floatingTexts: MutableList<FloatingText> = ArrayList(32)

    var currentWorldWidth: Float = level.worldWidth
    val isEndlessMode: Boolean = level.id == Levels.ENDLESS_LEVEL_ID
    var endlessDistanceGenerated: Float = 1600f
    var nextEntityId: Int = 300

    var screenShakeTimer: Float = 0f
    var screenShakeIntensity: Float = 0f
    var shakeOffsetX: Float = 0f
    var shakeOffsetY: Float = 0f

    fun triggerScreenShake(intensity: Float, duration: Float) {
        if (intensity >= screenShakeIntensity || screenShakeTimer <= 0f) {
            screenShakeIntensity = intensity
            screenShakeTimer = duration
        }
    }

    var currentScore: Int = 0
    var crystalsCollected: Int = 0
    var comboCount: Int = 0
    var comboTimer: Float = 0f
    var elapsedTime: Float = 0f
    var isGameOver: Boolean = false
    var gameOverReason: String = ""
    var isLevelComplete: Boolean = false

    var cameraX: Float = 0f
    var cameraY: Float = 0f

    // Callbacks for audio and tactile haptics
    var onSoundJump: (() -> Unit)? = null
    var onSoundDoubleJump: (() -> Unit)? = null
    var onSoundCrystal: (() -> Unit)? = null
    var onSoundPowerUp: (() -> Unit)? = null
    var onSoundHazard: (() -> Unit)? = null
    var onSoundDash: (() -> Unit)? = null
    var onSoundVictory: (() -> Unit)? = null

    var onHapticJump: (() -> Unit)? = null
    var onHapticCollect: (() -> Unit)? = null
    var onHapticHazard: (() -> Unit)? = null
    var onHapticVictory: (() -> Unit)? = null

    // Track previous input state to require fresh taps for jumping
    private var wasJumpDown = false
    private var wasDashDown = false

    fun update(
        rawDt: Float,
        moveInput: Float,
        jumpPressed: Boolean,
        dashPressed: Boolean,
        viewportWidth: Float,
        viewportHeight: Float
    ) {
        if (isGameOver || isLevelComplete) {
            // Update particles only
            updateParticles(min(rawDt, 0.04f))
            return
        }

        // Clamp frame time to protect against physics instability when resuming
        val dt = min(rawDt, 0.04f)
        elapsedTime += dt

        // Screen Shake calculation
        if (screenShakeTimer > 0f) {
            screenShakeTimer -= dt
            val currentIntensity = screenShakeIntensity * (screenShakeTimer / 0.35f).coerceIn(0f, 1f)
            shakeOffsetX = (Math.random().toFloat() * 2f - 1f) * currentIntensity
            shakeOffsetY = (Math.random().toFloat() * 2f - 1f) * currentIntensity
            if (screenShakeTimer <= 0f) {
                screenShakeIntensity = 0f
                shakeOffsetX = 0f
                shakeOffsetY = 0f
            }
        }

        // Endless mode world chunk generation
        if (isEndlessMode) {
            if (player.x > endlessDistanceGenerated - 1200f) {
                generateNextEndlessChunk()
            }
            val distanceScore = (player.x / 10f).toInt()
            val totalEarned = distanceScore + crystalsCollected * 100
            if (totalEarned > currentScore) {
                currentScore = totalEarned
            }
        }

        // 1. Update Platform & Hazard Timers & Movements
        updatePlatforms(dt)
        updateHazards(dt)

        // 2. Power-Up & Invincibility Timers
        if (player.powerUpTimer > 0f) {
            player.powerUpTimer -= dt
            if (player.powerUpTimer <= 0f) {
                player.activePowerUp = PowerUpType.NONE
                player.powerUpTimer = 0f
            }
        }
        if (player.invincibilityTimer > 0f) {
            player.invincibilityTimer -= dt
        }
        if (player.dashCooldown > 0f) {
            player.dashCooldown -= dt
        }
        if (player.happyEyeTimer > 0f) {
            player.happyEyeTimer -= dt
        }

        // Combo timer countdown
        if (comboTimer > 0f) {
            comboTimer -= dt
            if (comboTimer <= 0f) {
                comboCount = 0
            }
        }

        // 3. Jump Handling with Coyote Time & Jump Buffering
        if (player.isGrounded) {
            player.coyoteTimer = 0.14f
        } else if (player.coyoteTimer > 0f) {
            player.coyoteTimer -= dt
        }

        val isJumpTap = jumpPressed && !wasJumpDown
        wasJumpDown = jumpPressed

        if (isJumpTap) {
            player.jumpBufferTimer = 0.16f
        } else if (player.jumpBufferTimer > 0f) {
            player.jumpBufferTimer -= dt
        }

        // Jump Execution
        if (player.jumpBufferTimer > 0f && (player.isGrounded || player.coyoteTimer > 0f)) {
            // Ground / Coyote Jump
            player.vy = JUMP_VELOCITY
            player.isGrounded = false
            player.coyoteTimer = 0f
            player.jumpBufferTimer = 0f
            player.standingOnPlatform = null
            player.jumpCount = 1
            player.squashStretch = 1.35f
            spawnJumpSparks(player.centerX, player.bottom, Color(0xFF00F0FF))
            onSoundJump?.invoke()
            onHapticJump?.invoke()
        } else if (isJumpTap && player.jumpCount < 2) {
            // Mid-air Double Jump
            player.vy = DOUBLE_JUMP_VELOCITY
            player.jumpCount = 2
            player.jumpBufferTimer = 0f
            player.standingOnPlatform = null
            player.squashStretch = 1.35f
            spawnJumpSparks(player.centerX, player.centerY + 8f, Color(0xFFFF007F))
            onSoundDoubleJump?.invoke()
            onHapticJump?.invoke()
        }

        // Variable Jump Height: releasing jump button cuts ascent velocity
        if (!jumpPressed && player.vy < -160f) {
            player.vy += 700f * dt
        }

        // 4. Handle Dash Input
        val isDashTap = dashPressed && !wasDashDown
        wasDashDown = dashPressed

        if (isDashTap && player.dashCooldown <= 0f && !player.isDashing) {
            player.isDashing = true
            player.dashTimer = DASH_DURATION
            player.dashCooldown = DASH_COOLDOWN
            val dir = if (player.facingRight) 1f else -1f
            player.vx = dir * DASH_SPEED
            player.vy = 0f
            triggerScreenShake(3.5f, 0.14f)
            spawnDashTrail(player.centerX, player.centerY)
            onSoundDash?.invoke()
            onHapticJump?.invoke()
        }

        // 5. Horizontal Physics
        if (player.isDashing) {
            player.dashTimer -= dt
            if (player.dashTimer <= 0f) {
                player.isDashing = false
            }
        } else {
            val maxSpeed = if (player.activePowerUp == PowerUpType.LIGHT_BURST) BURST_MAX_SPEED else BASE_MAX_SPEED
            if (moveInput != 0f) {
                player.facingRight = moveInput > 0f
                player.vx += moveInput * MOVE_ACCEL * dt
                player.vx = player.vx.coerceIn(-maxSpeed, maxSpeed)
                if (player.isGrounded && Math.random() < 0.25) {
                    spawnFootstepParticle(player.centerX, player.bottom)
                }
            } else {
                // Decelerate smoothly
                if (player.vx > 0f) {
                    player.vx = max(0f, player.vx - MOVE_DECEL * dt)
                } else if (player.vx < 0f) {
                    player.vx = min(0f, player.vx + MOVE_DECEL * dt)
                }
            }
        }

        // Dynamic banking tilt based on horizontal velocity
        val targetTilt = (player.vx / BASE_MAX_SPEED) * 12f
        player.tiltAngle += (targetTilt - player.tiltAngle) * 12f * dt

        // 6. Vertical Physics (Gravity)
        if (!player.isDashing) {
            player.vy = min(player.vy + GRAVITY * dt, MAX_FALL_SPEED)
        }

        // Smooth squash/stretch return to 1.0
        player.squashStretch += (1.0f - player.squashStretch) * 10f * dt

        // 7. Move & Collide
        moveAndCollide(dt)

        // 8. Magnetic Crystal Attraction & Pickups
        updateCrystalsAndPowerUps(dt)

        // 9. Hazard Collision Detection
        checkHazardCollisions()

        // 10. Exit Portal Check
        checkPortalCollision()

        // 11. Particles & Visual animations
        updateParticles(dt)
        updatePlayerBlink(dt)
        portal.rotation += 65f * dt

        // 12. Smooth Camera Follow
        updateCamera(viewportWidth, viewportHeight, dt)
    }

    private fun updatePlatforms(dt: Float) {
        for (platform in platforms) {
            var deltaX = 0f
            var deltaY = 0f
            when (platform.type) {
                PlatformType.MOVING_X -> {
                    val delta = platform.moveDir * platform.moveSpeed * dt
                    platform.x += delta
                    if (platform.x > platform.moveMax) {
                        platform.x = platform.moveMax
                        platform.moveDir = -1f
                    } else if (platform.x < platform.moveMin) {
                        platform.x = platform.moveMin
                        platform.moveDir = 1f
                    }
                    deltaX = delta
                }
                PlatformType.MOVING_Y -> {
                    val delta = platform.moveDir * platform.moveSpeed * dt
                    platform.y += delta
                    if (platform.y > platform.moveMax) {
                        platform.y = platform.moveMax
                        platform.moveDir = -1f
                    } else if (platform.y < platform.moveMin) {
                        platform.y = platform.moveMin
                        platform.moveDir = 1f
                    }
                    deltaY = delta
                }
                PlatformType.TIMED_ENERGY -> {
                    platform.timer += dt
                    val cycleDuration = platform.activeDuration + platform.inactiveDuration
                    val phase = platform.timer % cycleDuration
                    platform.isCycleActive = phase < platform.activeDuration
                }
                else -> {}
            }
            // Carry player if standing on this moving platform
            if (player.isGrounded && player.standingOnPlatform === platform) {
                player.x += deltaX
                player.y += deltaY
            }
        }
    }

    private fun updateHazards(dt: Float) {
        for (hazard in hazards) {
            when (hazard.type) {
                HazardType.ROTATING_SPINNER -> {
                    hazard.angle += hazard.angularSpeed * dt
                }
                HazardType.LASER_VERTICAL, HazardType.LASER_HORIZONTAL -> {
                    hazard.cycleTimer += dt
                    val phase = (hazard.cycleTimer % hazard.cyclePeriod) / hazard.cyclePeriod
                    hazard.isPulsingActive = phase < 0.65f // active for 65% of the cycle
                }
                else -> {}
            }
        }
    }

    private fun moveAndCollide(dt: Float) {
        // Horizontal Movement
        val prevX = player.x
        player.x += player.vx * dt

        // Level boundary clamping
        if (player.x < 0f) {
            player.x = 0f
            player.vx = 0f
        } else if (player.x + player.width > currentWorldWidth) {
            player.x = currentWorldWidth - player.width
            player.vx = 0f
        }

        // Horizontal Solid Collisions (including moving platforms)
        for (platform in platforms) {
            val isSolidHoriz = when (platform.type) {
                PlatformType.SOLID, PlatformType.MOVING_X, PlatformType.MOVING_Y -> true
                PlatformType.TIMED_ENERGY -> platform.isCycleActive
                PlatformType.ONE_WAY -> false
            }
            if (isSolidHoriz) {
                // Inset feet and head for horizontal wall checks so the floor platform
                // NOVA is standing on is NEVER mistaken for a vertical obstacle!
                val footInset = 6f
                val headInset = 4f
                if (checkAABB(
                        player.left, player.top + headInset, player.right, player.bottom - footInset,
                        platform.left, platform.top, platform.right, platform.bottom
                    )
                ) {
                    if (player.vx > 0f) {
                        player.x = platform.left - player.width
                    } else if (player.vx < 0f) {
                        player.x = platform.right
                    }
                    player.vx = 0f
                }
            }
        }

        // Vertical Movement
        val prevY = player.y
        player.y += player.vy * dt
        val wasGrounded = player.isGrounded
        player.isGrounded = false
        player.standingOnPlatform = null

        for (platform in platforms) {
            val isCollidable = when (platform.type) {
                PlatformType.SOLID, PlatformType.MOVING_X, PlatformType.MOVING_Y -> true
                PlatformType.TIMED_ENERGY -> platform.isCycleActive
                PlatformType.ONE_WAY -> true
            }
            if (!isCollidable) continue

            if (platform.type == PlatformType.ONE_WAY) {
                // One-way: Only collide from above when falling
                val prevBottom = prevY + player.height
                val currBottom = player.y + player.height
                if (player.vy >= 0f && prevBottom <= platform.top + 8f && currBottom >= platform.top) {
                    if (player.right > platform.left + 2f && player.left < platform.right - 2f) {
                        if (!wasGrounded && player.vy > 100f) {
                            spawnLandingPuff(player.centerX, platform.top)
                        }
                        player.y = platform.top - player.height
                        player.vy = 0f
                        player.isGrounded = true
                        player.standingOnPlatform = platform
                        player.jumpCount = 0
                    }
                }
            } else {
                // Solid / Moving / Active Energy
                if (checkAABB(player.left, player.top, player.right, player.bottom,
                        platform.left, platform.top, platform.right, platform.bottom)) {
                    if (player.vy > 0f) {
                        // Landed on top
                        if (!wasGrounded && player.vy > 100f) {
                            spawnLandingPuff(player.centerX, platform.top)
                        }
                        player.y = platform.top - player.height
                        player.vy = 0f
                        player.isGrounded = true
                        player.standingOnPlatform = platform
                        player.jumpCount = 0
                    } else if (player.vy < 0f) {
                        // Hit ceiling
                        player.y = platform.bottom
                        player.vy = 0f
                    }
                }
            }
        }
    }

    private fun updateCrystalsAndPowerUps(dt: Float) {
        val magnetActive = player.activePowerUp == PowerUpType.CRYSTAL_MAGNET
        val magnetRadius = 180f

        for (crystal in crystals) {
            if (crystal.collected) continue
            crystal.bobPhase += 4f * dt

            // Magnet attraction
            if (magnetActive) {
                val dx = player.centerX - crystal.x
                val dy = player.centerY - crystal.y
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < magnetRadius && dist > 1f) {
                    val pullSpeed = 340f
                    crystal.x += (dx / dist) * pullSpeed * dt
                    crystal.y += (dy / dist) * pullSpeed * dt
                    if (Math.random() < 0.2) {
                        spawnSparkle(crystal.x, crystal.y, Color(0xFFFF007F))
                    }
                }
            }

            // Pickup check
            val dx = player.centerX - crystal.x
            val dy = player.centerY - crystal.y
            val dist = sqrt(dx * dx + dy * dy)
            if (dist < (player.width / 2f + crystal.radius + 6f)) {
                crystal.collected = true
                crystalsCollected++
                comboCount++
                comboTimer = 2.5f
                player.happyEyeTimer = 0.8f

                val basePoints = if (crystal.isSecret) 250 else 100
                val comboBonus = (comboCount - 1) * 25
                val points = basePoints + comboBonus
                currentScore += points

                val popupColor = if (crystal.isSecret) Color(0xFFFFD600) else Color(0xFF00F0FF)
                val popupText = if (comboCount > 1) "+$points (${comboCount}x)" else "+$points"
                floatingTexts.add(FloatingText(x = crystal.x, y = crystal.y - 12f, text = popupText, color = popupColor))

                spawnCrystalBurst(crystal.x, crystal.y, popupColor)
                onSoundCrystal?.invoke()
                onHapticCollect?.invoke()
            }
        }

        for (powerUp in powerUps) {
            if (powerUp.collected) continue
            powerUp.bobPhase += 3.5f * dt

            val dx = player.centerX - powerUp.x
            val dy = player.centerY - powerUp.y
            val dist = sqrt(dx * dx + dy * dy)
            if (dist < (player.width / 2f + powerUp.radius + 4f)) {
                powerUp.collected = true
                player.activePowerUp = powerUp.type
                player.powerUpTimer = powerUp.type.durationSec
                if (powerUp.type == PowerUpType.SAFE_LANDING) {
                    player.hasShield = true
                }
                currentScore += 150
                floatingTexts.add(FloatingText(x = powerUp.x, y = powerUp.y - 14f, text = powerUp.type.displayName.uppercase(), color = powerUp.type.badgeColor))
                spawnPowerUpBurst(powerUp.x, powerUp.y, powerUp.type.badgeColor)
                onSoundPowerUp?.invoke()
                onHapticCollect?.invoke()
            }
        }
    }

    private fun checkHazardCollisions() {
        if (player.invincibilityTimer > 0f) return

        var hitHazard = false
        var reason = ""

        // Void drop check
        if (player.y > level.worldHeight - 20f) {
            hitHazard = true
            reason = "NOVA fell into deep galactic space void."
        }

        // Obstacle hazards
        if (!hitHazard) {
            for (hazard in hazards) {
                when (hazard.type) {
                    HazardType.SPIKE -> {
                        if (checkAABB(player.left + 4f, player.top + 4f, player.right - 4f, player.bottom,
                                hazard.left, hazard.top + 4f, hazard.right, hazard.bottom)) {
                            hitHazard = true
                            reason = "NOVA came into contact with sharp plasma spikes."
                            break
                        }
                    }
                    HazardType.ROTATING_SPINNER -> {
                        // Check distance from player center to both sides of the rotating dual laser blade
                        val rad = hazard.angle * (PI.toFloat() / 180f)
                        for (side in listOf(1f, -1f)) {
                            val endX = hazard.centerX + cos(rad) * hazard.armLength * side
                            val endY = hazard.centerY + sin(rad) * hazard.armLength * side
                            if (distToSegment(player.centerX, player.centerY, hazard.centerX, hazard.centerY, endX, endY) < 14f) {
                                hitHazard = true
                                reason = "NOVA was struck by a rotating laser energy blade."
                                break
                            }
                        }
                    }
                    HazardType.LASER_VERTICAL, HazardType.LASER_HORIZONTAL -> {
                        if (hazard.isPulsingActive && checkAABB(
                                player.left + 2f, player.top + 2f, player.right - 2f, player.bottom - 2f,
                                hazard.left, hazard.top, hazard.right, hazard.bottom
                            )) {
                            hitHazard = true
                            reason = "NOVA was intercepted by an active energy security laser."
                            break
                        }
                    }
                    HazardType.VOID_ZONE -> {
                        if (checkAABB(player.left, player.top, player.right, player.bottom,
                                hazard.left, hazard.top, hazard.right, hazard.bottom)) {
                            hitHazard = true
                            reason = "NOVA fell out of the circuit perimeter."
                            break
                        }
                    }
                }
            }
        }

        if (hitHazard) {
            comboCount = 0
            comboTimer = 0f
            if (player.hasShield && player.y < level.worldHeight - 20f) {
                // Safe Landing shield absorbs damage!
                player.hasShield = false
                player.invincibilityTimer = 1.4f
                player.vy = -340f // emergency bounce
                triggerScreenShake(6.5f, 0.25f)
                spawnShieldAbsorb(player.centerX, player.centerY)
                onSoundHazard?.invoke()
                onHapticHazard?.invoke()
            } else {
                // Game Over
                isGameOver = true
                gameOverReason = reason
                triggerScreenShake(9.0f, 0.40f)
                spawnExplosion(player.centerX, player.centerY)
                onSoundHazard?.invoke()
                onHapticHazard?.invoke()
            }
        }
    }

    private fun checkPortalCollision() {
        val dx = player.centerX - portal.x
        val dy = player.centerY - portal.y
        val dist = sqrt(dx * dx + dy * dy)
        if (dist < portal.radius + 12f) {
            isLevelComplete = true
            // Calculate final completion bonus
            val baseBonus = 1000
            val speedBonus = max(0, ((level.targetTimeSeconds - elapsedTime) * 40f).toInt())
            currentScore += baseBonus + speedBonus
            triggerScreenShake(5.0f, 0.35f)
            spawnPortalVictory(portal.x, portal.y)
            onSoundVictory?.invoke()
            onHapticVictory?.invoke()
        }
    }

    private fun updateCamera(viewportWidth: Float, viewportHeight: Float, dt: Float) {
        val targetX = player.centerX - viewportWidth * 0.42f
        val targetY = player.centerY - viewportHeight * 0.55f

        val maxX = max(0f, currentWorldWidth - viewportWidth)
        val maxY = max(0f, level.worldHeight - viewportHeight)

        val clampedTargetX = targetX.coerceIn(0f, maxX)
        val clampedTargetY = targetY.coerceIn(0f, maxY)

        cameraX += (clampedTargetX - cameraX) * 8f * dt
        cameraY += (clampedTargetY - cameraY) * 6f * dt
    }

    private fun updatePlayerBlink(dt: Float) {
        player.eyeBlinkTimer -= dt
        if (player.eyeBlinkTimer <= 0f) {
            player.isEyeClosed = !player.isEyeClosed
            player.eyeBlinkTimer = if (player.isEyeClosed) 0.12f else (2.0f + Math.random().toFloat() * 2.5f)
        }
    }

    private fun updateParticles(dt: Float) {
        val iter = particles.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.life -= dt
            if (p.life <= 0f) {
                iter.remove()
            }
        }

        val textIter = floatingTexts.iterator()
        while (textIter.hasNext()) {
            val f = textIter.next()
            f.y -= 38f * dt
            f.life -= dt
            if (f.life <= 0f) {
                textIter.remove()
            }
        }
    }

    // Particle Spawners
    private fun spawnJumpSparks(x: Float, y: Float, color: Color) {
        for (i in 0 until 10) {
            val angle = Math.random().toFloat() * PI.toFloat()
            val speed = 80f + Math.random().toFloat() * 120f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed * (if (Math.random() > 0.5) 1f else -1f),
                    vy = abs(sin(angle)) * speed * 0.4f,
                    color = color,
                    life = 0.25f + Math.random().toFloat() * 0.2f,
                    maxLife = 0.45f,
                    size = 3.5f
                )
            )
        }
    }

    private fun spawnFootstepParticle(x: Float, y: Float) {
        particles.add(
            Particle(
                x = x + (Math.random().toFloat() * 10f - 5f),
                y = y,
                vx = (Math.random().toFloat() * 30f - 15f),
                vy = -20f - Math.random().toFloat() * 25f,
                color = Color(0x8800F0FF),
                life = 0.2f,
                maxLife = 0.2f,
                size = 2.5f
            )
        )
    }

    private fun spawnDashTrail(x: Float, y: Float) {
        for (i in 0 until 14) {
            particles.add(
                Particle(
                    x = x + (Math.random().toFloat() * 20f - 10f),
                    y = y + (Math.random().toFloat() * 20f - 10f),
                    vx = (Math.random().toFloat() * 60f - 30f),
                    vy = (Math.random().toFloat() * 60f - 30f),
                    color = Color(0xFF00E5FF),
                    life = 0.35f,
                    maxLife = 0.35f,
                    size = 4.5f
                )
            )
        }
    }

    private fun spawnCrystalBurst(x: Float, y: Float, color: Color) {
        for (i in 0 until 16) {
            val angle = Math.random().toFloat() * 2f * PI.toFloat()
            val speed = 60f + Math.random().toFloat() * 140f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = color,
                    life = 0.35f + Math.random().toFloat() * 0.25f,
                    maxLife = 0.6f,
                    size = 4f
                )
            )
        }
    }

    private fun spawnPowerUpBurst(x: Float, y: Float, color: Color) {
        for (i in 0 until 22) {
            val angle = Math.random().toFloat() * 2f * PI.toFloat()
            val speed = 90f + Math.random().toFloat() * 180f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = color,
                    life = 0.5f + Math.random().toFloat() * 0.3f,
                    maxLife = 0.8f,
                    size = 5.5f
                )
            )
        }
    }

    private fun spawnShieldAbsorb(x: Float, y: Float) {
        for (i in 0 until 24) {
            val angle = Math.random().toFloat() * 2f * PI.toFloat()
            val speed = 80f + Math.random().toFloat() * 160f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = Color(0xFF00E676),
                    life = 0.45f,
                    maxLife = 0.45f,
                    size = 5f
                )
            )
        }
    }

    private fun spawnExplosion(x: Float, y: Float) {
        for (i in 0 until 35) {
            val angle = Math.random().toFloat() * 2f * PI.toFloat()
            val speed = 100f + Math.random().toFloat() * 260f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = if (i % 2 == 0) Color(0xFFFF007F) else Color(0xFFFFD600),
                    life = 0.6f + Math.random().toFloat() * 0.4f,
                    maxLife = 1.0f,
                    size = 6f
                )
            )
        }
    }

    private fun spawnPortalVictory(x: Float, y: Float) {
        for (i in 0 until 40) {
            val angle = Math.random().toFloat() * 2f * PI.toFloat()
            val speed = 80f + Math.random().toFloat() * 200f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = if (i % 3 == 0) Color(0xFF00F0FF) else if (i % 3 == 1) Color(0xFFFF007F) else Color(0xFFFFD600),
                    life = 0.7f + Math.random().toFloat() * 0.5f,
                    maxLife = 1.2f,
                    size = 5f
                )
            )
        }
    }

    private fun spawnSparkle(x: Float, y: Float, color: Color) {
        particles.add(
            Particle(
                x = x,
                y = y,
                vx = (Math.random().toFloat() * 20f - 10f),
                vy = (Math.random().toFloat() * 20f - 10f),
                color = color,
                life = 0.25f,
                maxLife = 0.25f,
                size = 3f
            )
        )
    }

    private fun spawnLandingPuff(x: Float, y: Float) {
        for (i in 0 until 8) {
            val speed = 25f + Math.random().toFloat() * 65f
            val dir = if (i % 2 == 0) 1f else -1f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = dir * speed,
                    vy = -Math.random().toFloat() * 25f,
                    color = Color(0x9900F0FF),
                    life = 0.22f,
                    maxLife = 0.22f,
                    size = 3.2f
                )
            )
        }
    }

    private fun generateNextEndlessChunk() {
        val theme = level.themeColor
        var curX = endlessDistanceGenerated
        val groundY = 620f

        val platformCount = 5
        for (i in 0 until platformCount) {
            val platWidth = 100f + (Math.random().toFloat() * 60f)
            val gap = 45f + (Math.random().toFloat() * 45f)
            val platY = 280f + (Math.random().toFloat() * 180f)

            val randType = Math.random()
            val platType = when {
                randType < 0.40 -> PlatformType.SOLID
                randType < 0.65 -> PlatformType.ONE_WAY
                randType < 0.85 -> PlatformType.MOVING_X
                else -> PlatformType.TIMED_ENERGY
            }

            val p = if (platType == PlatformType.MOVING_X) {
                Platform(
                    x = curX,
                    y = platY,
                    width = platWidth,
                    height = 24f,
                    type = PlatformType.MOVING_X,
                    moveMin = curX - 25f,
                    moveMax = curX + 90f,
                    moveSpeed = 65f + (Math.random().toFloat() * 30f),
                    color = theme
                )
            } else if (platType == PlatformType.TIMED_ENERGY) {
                Platform(
                    x = curX,
                    y = platY,
                    width = platWidth,
                    height = 24f,
                    type = PlatformType.TIMED_ENERGY,
                    activeDuration = 2.0f,
                    inactiveDuration = 1.3f,
                    color = theme
                )
            } else {
                Platform(
                    x = curX,
                    y = platY,
                    width = platWidth,
                    height = 24f,
                    type = platType,
                    color = theme
                )
            }
            platforms.add(p)

            val crystalX = curX + platWidth / 2f
            val crystalY = platY - 45f
            crystals.add(Crystal(id = nextEntityId++, x = crystalX, y = crystalY, isSecret = Math.random() < 0.2))

            if (Math.random() < 0.35) {
                crystals.add(Crystal(id = nextEntityId++, x = crystalX + 24f, y = crystalY - 20f))
            }

            if (Math.random() < 0.16) {
                val powerType = when {
                    Math.random() < 0.35 -> PowerUpType.CRYSTAL_MAGNET
                    Math.random() < 0.70 -> PowerUpType.LIGHT_BURST
                    else -> PowerUpType.SAFE_LANDING
                }
                powerUps.add(PowerUpItem(id = nextEntityId++, x = curX + platWidth / 2f, y = platY - 35f, type = powerType))
            }

            if (gap > 50f && Math.random() < 0.6) {
                hazards.add(
                    Hazard(
                        type = HazardType.SPIKE,
                        x = curX + platWidth,
                        y = groundY - 80f,
                        width = gap,
                        height = 20f,
                        color = Color(0xFFFF007F)
                    )
                )
            }

            curX += platWidth + gap
        }

        endlessDistanceGenerated = curX
        currentWorldWidth = endlessDistanceGenerated + 600f
    }

    private fun checkAABB(
        al: Float, at: Float, ar: Float, ab: Float,
        bl: Float, bt: Float, br: Float, bb: Float
    ): Boolean {
        return al < br && ar > bl && at < bb && ab > bt
    }

    private fun distToSegment(px: Float, py: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val l2 = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)
        if (l2 == 0f) {
            val dx = px - x1
            val dy = py - y1
            return sqrt(dx * dx + dy * dy)
        }
        var t = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / l2
        t = t.coerceIn(0f, 1f)
        val projX = x1 + t * (x2 - x1)
        val projY = y1 + t * (y2 - y1)
        val dx = px - projX
        val dy = py - projY
        return sqrt(dx * dx + dy * dy)
    }

    private fun abs(v: Float): Float = if (v < 0f) -v else v
}
