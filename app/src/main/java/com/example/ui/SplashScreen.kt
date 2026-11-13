package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.SpaceDarkBg
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(
    viewModel: GameViewModel,
    onStartGame: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = androidx.compose.animation.core.LinearEasing)
        ),
        label = "rotation"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    LaunchedEffect(Unit) {
        if (viewModel.musicEnabled.value) {
            viewModel.soundSystem.startMusic()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceDarkBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onStartGame
            )
            .testTag("splash_screen_root"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Animated Star Circuit Crest
            Canvas(modifier = Modifier.size(160.dp)) {
                val cx = size.width / 2f
                val cy = size.height / 2f

                // Deep glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonCyan.copy(alpha = 0.35f * glowAlpha), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = size.width * 0.7f
                    ),
                    radius = size.width * 0.7f,
                    center = Offset(cx, cy)
                )

                // Rotating circuit rings
                rotate(rotation, pivot = Offset(cx, cy)) {
                    drawCircle(
                        color = NeonCyan.copy(alpha = 0.7f),
                        radius = 65f * pulse,
                        center = Offset(cx, cy),
                        style = Stroke(width = 3f)
                    )
                    drawCircle(
                        color = NeonMagenta.copy(alpha = 0.8f),
                        radius = 45f,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.5f)
                    )

                    // Orbiting star nodes
                    for (i in 0 until 4) {
                        val rad = (i * 90f) * (PI.toFloat() / 180f)
                        val nx = cx + cos(rad) * 65f * pulse
                        val ny = cy + sin(rad) * 65f * pulse
                        drawCircle(color = Color.White, radius = 4f, center = Offset(nx, ny))
                    }
                }

                // Inner NOVA mascot face
                drawCircle(
                    color = Color(0xFF161E38),
                    radius = 30f,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = NeonCyan,
                    radius = 30f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2.5f)
                )
                // Visor & neon eyes
                drawRoundRect(
                    color = Color(0xFF090D1C),
                    topLeft = Offset(cx - 20f, cy - 10f),
                    size = androidx.compose.ui.geometry.Size(40f, 20f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
                drawCircle(color = NeonCyan, radius = 3.5f, center = Offset(cx - 8f, cy))
                drawCircle(color = NeonCyan, radius = 3.5f, center = Offset(cx + 8f, cy))
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Game Title
            Text(
                text = "NEON HOP",
                color = NeonCyan,
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "STAR CIRCUIT",
                color = NeonMagenta,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 6.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "RETRO-FUTURISTIC 2D PLATFORMER",
                color = Color(0xFF8C98C2),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(72.dp))

            // Pulsing tap prompt
            Text(
                text = "TAP SCREEN TO BEGIN",
                color = Color.White.copy(alpha = glowAlpha),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
