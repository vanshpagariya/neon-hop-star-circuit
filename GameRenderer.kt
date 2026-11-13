package com.example.game

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.example.game.model.HazardType
import com.example.game.model.PlatformType
import com.example.game.model.PowerUpType
import com.example.game.model.RobotSkin
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 2D Canvas renderer for Neon Hop: Star Circuit.
 * Draws multilayered cosmic parallax backgrounds, glowing neon platforms,
 * rotating laser hazards, animated collectible crystals, exit portals,
 * and the customizable robot protagonist NOVA.
 */
class GameRenderer {

    // Pre-calculated deterministic stars for parallax
    private val distantStars = List(45) { i ->
        val x = ((i * 137.5f) % 2400f)
        val y = ((i * 83.2f) % 700f)
        val size = if (i % 3 == 0) 2.2f else 1.2f
        Triple(x, y, size)
    }

    private val nearStars = List(35) { i ->
        val x = ((i * 219.1f) % 2400f)
        val y = ((i * 127.3f) % 700f)
        val color = if (i % 2 == 0) Color(0xFF00F0FF) else Color(0xFFFF007F)
        Triple(x, y, color)
    }

    fun render(
        scope: DrawScope,
        engine: GameEngine,
        skin: RobotSkin,
        virtualWidth: Float,
        virtualHeight: Float,
        scale: Float = 1f
    ) {
        val camX = engine.cameraX
        val camY = engine.cameraY

        scope.withTransform({
            scale(scale, scale, pivot = Offset.Zero)
        }) {
            // 1. Draw Space Cosmic Background & Parallax
            drawParallaxBackground(this, camX, camY, virtualWidth, virtualHeight, engine.level.themeColor)

            // 2. Apply Camera & Screen Shake Transform for World Objects
            withTransform({
                translate(
                    left = -camX + engine.shakeOffsetX,
                    top = -camY + engine.shakeOffsetY
                )
            }) {
                // Draw Platforms
                drawPlatforms(this, engine)

                // Draw Hazards
                drawHazards(this, engine)

                // Draw Power-Up items
                drawPowerUps(this, engine)

                // Draw Crystals
                drawCrystals(this, engine)

                // Draw Exit Portal
                drawPortal(this, engine)

                // Draw Particles
                drawParticles(this, engine)

                // Draw Robot NOVA
                if (!engine.isGameOver) {
                    drawNova(this, engine, skin)
                }

                // Draw Floating Score & Combo Texts
                drawFloatingTexts(this, engine)
            }
        }
    }

    private fun drawParallaxBackground(
        scope: DrawScope,
        camX: Float,
        camY: Float,
        width: Float,
        height: Float,
        themeColor: Color
    ) {
        // Deep space gradient
        scope.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF060714),
                    Color(0xFF0D1026),
                    Color(0xFF140D26)
                ),
                startY = 0f,
                endY = height
            ),
            size = Size(width, height)
        )

        // Subtle theme ambient nebula glow
        scope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(themeColor.copy(alpha = 0.15f), Color.Transparent),
                center = Offset(width * 0.5f, height * 0.35f),
                radius = width * 0.7f
            ),
            radius = width * 0.7f,
            center = Offset(width * 0.5f, height * 0.35f)
        )

        // Distant Parallax Stars (Factor 0.15)
        for (star in distantStars) {
            val sx = (star.first - camX * 0.15f) % (width + 100f)
            val sy = (star.second - camY * 0.15f) % (height + 100f)
            val finalX = if (sx < 0) sx + width + 100f else sx
            val finalY = if (sy < 0) sy + height + 100f else sy
            scope.drawCircle(
                color = Color.White.copy(alpha = 0.55f),
                radius = star.third,
                center = Offset(finalX, finalY)
            )
        }

        // Mid Parallax Neon Stars (Factor 0.35)
        for (star in nearStars) {
            val sx = (star.first - camX * 0.35f) % (width + 100f)
            val sy = (star.second - camY * 0.35f) % (height + 100f)
            val finalX = if (sx < 0) sx + width + 100f else sx
            val finalY = if (sy < 0) sy + height + 100f else sy
            scope.drawCircle(
                color = star.third.copy(alpha = 0.75f),
                radius = 2.0f,
                center = Offset(finalX, finalY)
            )
        }

        // Horizon futuristic grid lines (Factor 0.45)
        val horizonY = height * 0.85f
        for (i in 0 until 6) {
            val ly = horizonY + i * 22f
            val alpha = (0.04f + i * 0.03f).coerceIn(0f, 0.2f)
            scope.drawLine(
                color = themeColor.copy(alpha = alpha),
                start = Offset(0f, ly),
                end = Offset(width, ly),
                strokeWidth = 1.2f
            )
        }
    }

    private fun drawPlatforms(scope: DrawScope, engine: GameEngine) {
        for (p in engine.platforms) {
            val baseColor = p.color

            when (p.type) {
                PlatformType.SOLID, PlatformType.MOVING_X, PlatformType.MOVING_Y -> {
                    // Dark body
                    scope.drawRoundRect(
                        color = Color(0xFF10152B),
                        topLeft = Offset(p.x, p.y),
                        size = Size(p.width, p.height),
                        cornerRadius = CornerRadius(6f, 6f)
                    )

                    // Glowing top border line
                    scope.drawLine(
                        color = baseColor,
                        start = Offset(p.x, p.y),
                        end = Offset(p.x + p.width, p.y),
                        strokeWidth = 3.5f,
                        cap = StrokeCap.Round
                    )

                    // Outer perimeter outline
                    scope.drawRoundRect(
                        color = baseColor.copy(alpha = 0.45f),
                        topLeft = Offset(p.x, p.y),
                        size = Size(p.width, p.height),
                        cornerRadius = CornerRadius(6f, 6f),
                        style = Stroke(width = 1.2f)
                    )

                    // Moving platform guide arrows
                    if (p.type == PlatformType.MOVING_X || p.type == PlatformType.MOVING_Y) {
                        val midY = p.y + p.height / 2f
                        val midX = p.x + p.width / 2f
                        scope.drawCircle(
                            color = baseColor,
                            radius = 3.5f,
                            center = Offset(midX, midY)
                        )
                    }
                }
                PlatformType.ONE_WAY -> {
                    // One-way floating glass rail
                    scope.drawRoundRect(
                        color = Color(0x66182245),
                        topLeft = Offset(p.x, p.y),
                        size = Size(p.width, p.height),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    // High-contrast dashed top ledge
                    scope.drawLine(
                        color = baseColor,
                        start = Offset(p.x, p.y),
                        end = Offset(p.x + p.width, p.y),
                        strokeWidth = 3.0f,
                        cap = StrokeCap.Round
                    )
                    // Small support nodes
                    scope.drawCircle(color = baseColor, radius = 2.5f, center = Offset(p.x + 8f, p.y + 4f))
                    scope.drawCircle(color = baseColor, radius = 2.5f, center = Offset(p.x + p.width - 8f, p.y + 4f))
                }
                PlatformType.TIMED_ENERGY -> {
                    val isActive = p.isCycleActive
                    val alpha = if (isActive) 0.85f else 0.2f
                    val energyColor = if (isActive) Color(0xFFFF007F) else Color(0x55FF007F)

                    scope.drawRoundRect(
                        color = energyColor.copy(alpha = if (isActive) 0.35f else 0.08f),
                        topLeft = Offset(p.x, p.y),
                        size = Size(p.width, p.height),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    scope.drawRoundRect(
                        color = energyColor.copy(alpha = alpha),
                        topLeft = Offset(p.x, p.y),
                        size = Size(p.width, p.height),
                        cornerRadius = CornerRadius(4f, 4f),
                        style = Stroke(width = if (isActive) 2.5f else 1.0f)
                    )

                    // Energy core pulse
                    if (isActive) {
                        scope.drawLine(
                            color = Color.White.copy(alpha = 0.9f),
                            start = Offset(p.x + 8f, p.y + p.height / 2f),
                            end = Offset(p.x + p.width - 8f, p.y + p.height / 2f),
                            strokeWidth = 2.0f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }

    private fun drawHazards(scope: DrawScope, engine: GameEngine) {
        for (h in engine.hazards) {
            when (h.type) {
                HazardType.SPIKE -> {
                    // Plasma spikes
                    val spikeCount = (h.width / 16f).toInt().coerceAtLeast(1)
                    val step = h.width / spikeCount
                    for (i in 0 until spikeCount) {
                        val sx = h.x + i * step
                        val path = Path().apply {
                            moveTo(sx, h.bottom)
                            lineTo(sx + step / 2f, h.top)
                            lineTo(sx + step, h.bottom)
                            close()
                        }
                        scope.drawPath(path, color = Color(0x66FF007F), style = Fill)
                        scope.drawPath(path, color = Color(0xFFFF007F), style = Stroke(width = 1.8f))
                        // Glowing tip
                        scope.drawCircle(
                            color = Color.White,
                            radius = 2.0f,
                            center = Offset(sx + step / 2f, h.top)
                        )
                    }
                }
                HazardType.ROTATING_SPINNER -> {
                    // Center Hub
                    scope.drawCircle(
                        color = Color(0xFF1A1A2E),
                        radius = 8f,
                        center = Offset(h.centerX, h.centerY)
                    )
                    scope.drawCircle(
                        color = Color(0xFFFF007F),
                        radius = 8f,
                        center = Offset(h.centerX, h.centerY),
                        style = Stroke(width = 2f)
                    )
                    // Rotating Dual Blades
                    val rad = h.angle * (PI.toFloat() / 180f)
                    for (side in listOf(1f, -1f)) {
                        val endX = h.centerX + cos(rad) * h.armLength * side
                        val endY = h.centerY + sin(rad) * h.armLength * side

                        // Outer laser beam glow
                        scope.drawLine(
                            color = Color(0x66FF007F),
                            start = Offset(h.centerX, h.centerY),
                            end = Offset(endX, endY),
                            strokeWidth = 7f,
                            cap = StrokeCap.Round
                        )
                        // Intense laser core
                        scope.drawLine(
                            color = Color.White,
                            start = Offset(h.centerX, h.centerY),
                            end = Offset(endX, endY),
                            strokeWidth = 2.5f,
                            cap = StrokeCap.Round
                        )
                        // Blade tip node
                        scope.drawCircle(
                            color = Color(0xFFFF007F),
                            radius = 3.5f,
                            center = Offset(endX, endY)
                        )
                    }
                }
                HazardType.LASER_VERTICAL -> {
                    if (h.isPulsingActive) {
                        val midX = h.x + h.width / 2f
                        scope.drawLine(
                            color = Color(0x66FF007F),
                            start = Offset(midX, h.top),
                            end = Offset(midX, h.bottom),
                            strokeWidth = 8f
                        )
                        scope.drawLine(
                            color = Color.White,
                            start = Offset(midX, h.top),
                            end = Offset(midX, h.bottom),
                            strokeWidth = 2.5f
                        )
                    }
                    // Emitter nodes
                    scope.drawCircle(color = Color(0xFFFF007F), radius = 4f, center = Offset(h.x + h.width / 2f, h.top))
                    scope.drawCircle(color = Color(0xFFFF007F), radius = 4f, center = Offset(h.x + h.width / 2f, h.bottom))
                }
                HazardType.LASER_HORIZONTAL -> {
                    if (h.isPulsingActive) {
                        val midY = h.y + h.height / 2f
                        scope.drawLine(
                            color = Color(0x66FF007F),
                            start = Offset(h.left, midY),
                            end = Offset(h.right, midY),
                            strokeWidth = 8f
                        )
                        scope.drawLine(
                            color = Color.White,
                            start = Offset(h.left, midY),
                            end = Offset(h.right, midY),
                            strokeWidth = 2.5f
                        )
                    }
                }
                HazardType.VOID_ZONE -> {}
            }
        }
    }

    private fun drawCrystals(scope: DrawScope, engine: GameEngine) {
        for (c in engine.crystals) {
            if (c.collected) continue
            val bobY = c.y + sin(c.bobPhase) * 5f
            val color = if (c.isSecret) Color(0xFFFFD600) else Color(0xFF00F0FF)

            val r = c.radius
            val path = Path().apply {
                moveTo(c.x, bobY - r)
                lineTo(c.x + r * 0.75f, bobY)
                lineTo(c.x, bobY + r)
                lineTo(c.x - r * 0.75f, bobY)
                close()
            }

            // Glow backing
            scope.drawPath(path, color = color.copy(alpha = 0.35f), style = Fill)
            // Vibrant diamond border
            scope.drawPath(path, color = color, style = Stroke(width = 2.0f))
            // Inner shimmering facet
            scope.drawCircle(color = Color.White, radius = 2.5f, center = Offset(c.x, bobY))
        }
    }

    private fun drawPowerUps(scope: DrawScope, engine: GameEngine) {
        for (item in engine.powerUps) {
            if (item.collected) continue
            val bobY = item.y + sin(item.bobPhase) * 4f
            val color = item.type.badgeColor

            // Outer pulse ring
            scope.drawCircle(
                color = color.copy(alpha = 0.3f),
                radius = item.radius * 1.5f,
                center = Offset(item.x, bobY)
            )

            // Inner sphere
            scope.drawCircle(
                color = Color(0xFF10152B),
                radius = item.radius,
                center = Offset(item.x, bobY)
            )
            scope.drawCircle(
                color = color,
                radius = item.radius,
                center = Offset(item.x, bobY),
                style = Stroke(width = 2.2f)
            )

            // Inner icon motif
            when (item.type) {
                PowerUpType.LIGHT_BURST -> {
                    // Lightning bolt
                    val bolt = Path().apply {
                        moveTo(item.x - 2f, bobY - 7f)
                        lineTo(item.x + 4f, bobY - 1f)
                        lineTo(item.x - 1f, bobY - 1f)
                        lineTo(item.x + 2f, bobY + 7f)
                        lineTo(item.x - 4f, bobY + 1f)
                        lineTo(item.x + 1f, bobY + 1f)
                        close()
                    }
                    scope.drawPath(bolt, color = Color.White, style = Fill)
                }
                PowerUpType.SAFE_LANDING -> {
                    // Shield
                    val shield = Path().apply {
                        moveTo(item.x, bobY - 6f)
                        lineTo(item.x + 5f, bobY - 4f)
                        lineTo(item.x + 4f, bobY + 3f)
                        lineTo(item.x, bobY + 7f)
                        lineTo(item.x - 4f, bobY + 3f)
                        lineTo(item.x - 5f, bobY - 4f)
                        close()
                    }
                    scope.drawPath(shield, color = Color.White, style = Fill)
                }
                PowerUpType.CRYSTAL_MAGNET -> {
                    // Horseshoe magnet
                    scope.drawCircle(
                        color = Color.White,
                        radius = 5.5f,
                        center = Offset(item.x, bobY),
                        style = Stroke(width = 2.0f)
                    )
                    scope.drawRect(
                        color = Color(0xFF10152B),
                        topLeft = Offset(item.x - 7f, bobY),
                        size = Size(14f, 7f)
                    )
                }
                PowerUpType.NONE -> {}
            }
        }
    }

    private fun drawPortal(scope: DrawScope, engine: GameEngine) {
        val portal = engine.portal
        val r = portal.radius

        scope.rotate(portal.rotation, pivot = Offset(portal.x, portal.y)) {
            // Outermost rotating star circuit ring
            scope.drawCircle(
                color = Color(0x4400F0FF),
                radius = r * 1.35f,
                center = Offset(portal.x, portal.y),
                style = Stroke(width = 2f)
            )

            // Mid neon ring with notches
            scope.drawCircle(
                color = Color(0xFFD500F9),
                radius = r,
                center = Offset(portal.x, portal.y),
                style = Stroke(width = 3.5f)
            )

            // Orbiting circuit dots
            for (i in 0 until 4) {
                val ang = (i * 90f) * (PI.toFloat() / 180f)
                val dotX = portal.x + cos(ang) * r
                val dotY = portal.y + sin(ang) * r
                scope.drawCircle(
                    color = Color.White,
                    radius = 3.5f,
                    center = Offset(dotX, dotY)
                )
            }
        }

        // Deep vortex center
        scope.drawCircle(
            color = Color(0xFF070B1A),
            radius = r * 0.7f,
            center = Offset(portal.x, portal.y)
        )
        scope.drawCircle(
            color = Color(0xFF00F0FF),
            radius = r * 0.45f,
            center = Offset(portal.x, portal.y)
        )
        scope.drawCircle(
            color = Color.White,
            radius = r * 0.22f,
            center = Offset(portal.x, portal.y)
        )
    }

    private fun drawNova(scope: DrawScope, engine: GameEngine, skin: RobotSkin) {
        val p = engine.player

        val cx = p.centerX
        val cy = p.centerY
        val scaleY = p.squashStretch
        val scaleX = 1f / scaleY

        scope.withTransform({
            rotate(p.tiltAngle, pivot = Offset(cx, cy))
            scale(scaleX, scaleY, pivot = Offset(cx, cy))
        }) {
            // Dash speed streaks
            if (p.isDashing) {
                val trailDir = if (p.facingRight) -1f else 1f
                for (i in 0 until 4) {
                    val streakY = p.y + 6f + i * 7f
                    val startX = if (p.facingRight) p.left else p.right
                    val length = 32f + (i * 6f)
                    scope.drawLine(
                        color = Color(0xCC00F0FF),
                        start = Offset(startX, streakY),
                        end = Offset(startX + trailDir * length, streakY),
                        strokeWidth = 2.5f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // Power-up active auras
            when (p.activePowerUp) {
                PowerUpType.SAFE_LANDING -> {
                    // Protective shield bubble
                    scope.drawCircle(
                        color = Color(0x3300E676),
                        radius = 24f,
                        center = Offset(cx, cy)
                    )
                    scope.drawCircle(
                        color = Color(0xFF00E676),
                        radius = 24f,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2f)
                    )
                }
                PowerUpType.CRYSTAL_MAGNET -> {
                    // Pulsing magnetic rings
                    scope.drawCircle(
                        color = Color(0x44FF007F),
                        radius = 22f,
                        center = Offset(cx, cy),
                        style = Stroke(width = 1.5f)
                    )
                }
                PowerUpType.LIGHT_BURST -> {
                    // Cyan speed trail
                    val offsetTrail = if (p.facingRight) -10f else 10f
                    scope.drawRoundRect(
                        color = Color(0x4400F0FF),
                        topLeft = Offset(p.x + offsetTrail, p.y),
                        size = Size(p.width, p.height),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                }
                PowerUpType.NONE -> {}
            }

            // Thruster fire when jumping or falling
            if (!p.isGrounded) {
                val flameHeight = 12f + (Math.random().toFloat() * 6f)
                val flamePath = Path().apply {
                    moveTo(cx - 6f, p.bottom)
                    lineTo(cx, p.bottom + flameHeight)
                    lineTo(cx + 6f, p.bottom)
                    close()
                }
                scope.drawPath(flamePath, color = Color(0xFFFF6D00), style = Fill)
                scope.drawCircle(color = Color(0xFFFFD600), radius = 3f, center = Offset(cx, p.bottom + 3f))
            }

            // Antenna
            scope.drawLine(
                color = skin.accentColor,
                start = Offset(cx, p.top),
                end = Offset(cx, p.top - 8f),
                strokeWidth = 2.0f,
                cap = StrokeCap.Round
            )
            // Glowing antenna orb
            scope.drawCircle(
                color = skin.eyeColor,
                radius = 3.5f,
                center = Offset(cx, p.top - 9f)
            )

            // Robot Body & Head (Chassis)
            scope.drawRoundRect(
                color = skin.bodyColor,
                topLeft = Offset(p.x, p.top),
                size = Size(p.width, p.height),
                cornerRadius = CornerRadius(8f, 8f)
            )
            // Accent border
            scope.drawRoundRect(
                color = skin.accentColor,
                topLeft = Offset(p.x, p.top),
                size = Size(p.width, p.height),
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(width = 2.0f)
            )

            // Visor Screen
            val visorLeft = p.x + 3f
            val visorTop = p.top + 7f
            val visorWidth = p.width - 6f
            val visorHeight = 15f

            scope.drawRoundRect(
                color = skin.visorColor,
                topLeft = Offset(visorLeft, visorTop),
                size = Size(visorWidth, visorHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Eyes: Happy anime eyes (when collecting crystals/combo) or blinking/normal eyes
            if (p.happyEyeTimer > 0f) {
                // Curved happy anime eyes "^ ^"
                val eyeY = visorTop + 7f
                val leftEyeX = cx - 5f
                val rightEyeX = cx + 5f

                val happyLeft = Path().apply {
                    moveTo(leftEyeX - 3.5f, eyeY + 1.5f)
                    lineTo(leftEyeX, eyeY - 2.5f)
                    lineTo(leftEyeX + 3.5f, eyeY + 1.5f)
                }
                val happyRight = Path().apply {
                    moveTo(rightEyeX - 3.5f, eyeY + 1.5f)
                    lineTo(rightEyeX, eyeY - 2.5f)
                    lineTo(rightEyeX + 3.5f, eyeY + 1.5f)
                }
                scope.drawPath(happyLeft, color = skin.eyeColor, style = Stroke(width = 2.0f, cap = StrokeCap.Round))
                scope.drawPath(happyRight, color = skin.eyeColor, style = Stroke(width = 2.0f, cap = StrokeCap.Round))
            } else if (!p.isEyeClosed) {
                val eyeY = visorTop + 6f
                val eyeLookOffset = if (p.facingRight) 2.5f else -2.5f
                val leftEyeX = cx - 5f + eyeLookOffset
                val rightEyeX = cx + 5f + eyeLookOffset

                // Left Eye
                scope.drawRoundRect(
                    color = skin.eyeColor,
                    topLeft = Offset(leftEyeX - 2.5f, eyeY - 3f),
                    size = Size(5f, 7f),
                    cornerRadius = CornerRadius(2.5f, 2.5f)
                )
                // Right Eye
                scope.drawRoundRect(
                    color = skin.eyeColor,
                    topLeft = Offset(rightEyeX - 2.5f, eyeY - 3f),
                    size = Size(5f, 7f),
                    cornerRadius = CornerRadius(2.5f, 2.5f)
                )
                // Sparkle reflections
                scope.drawCircle(color = Color.White, radius = 1.0f, center = Offset(leftEyeX - 0.5f, eyeY - 1f))
                scope.drawCircle(color = Color.White, radius = 1.0f, center = Offset(rightEyeX - 0.5f, eyeY - 1f))
            } else {
                // Closed eye lines
                val eyeY = visorTop + 7f
                scope.drawLine(
                    color = skin.eyeColor,
                    start = Offset(cx - 7f, eyeY),
                    end = Offset(cx - 3f, eyeY),
                    strokeWidth = 1.8f
                )
                scope.drawLine(
                    color = skin.eyeColor,
                    start = Offset(cx + 3f, eyeY),
                    end = Offset(cx + 7f, eyeY),
                    strokeWidth = 1.8f
                )
            }

            // Chest circuit badge
            scope.drawCircle(
                color = skin.accentColor,
                radius = 2.5f,
                center = Offset(cx, p.top + 26f)
            )
        }
    }

    private fun drawParticles(scope: DrawScope, engine: GameEngine) {
        for (p in engine.particles) {
            val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
            scope.drawCircle(
                color = p.color.copy(alpha = alpha),
                radius = p.size * alpha,
                center = Offset(p.x, p.y)
            )
        }
    }

    private val textPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
        textSize = 13f
        textAlign = android.graphics.Paint.Align.CENTER
        setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
    }

    private fun drawFloatingTexts(scope: DrawScope, engine: GameEngine) {
        val nativeCanvas = scope.drawContext.canvas.nativeCanvas
        for (f in engine.floatingTexts) {
            val alpha = (f.life / f.maxLife).coerceIn(0f, 1f)
            textPaint.color = f.color.toArgb()
            textPaint.alpha = (alpha * 255).toInt()
            nativeCanvas.drawText(f.text, f.x, f.y, textPaint)
        }
    }
}
