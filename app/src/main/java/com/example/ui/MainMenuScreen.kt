package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.SpaceCardBg
import com.example.ui.theme.SpaceCardBorder
import com.example.ui.theme.SpaceDarkBg
import com.example.ui.theme.SpaceSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MainMenuScreen(
    viewModel: GameViewModel,
    onPlay: () -> Unit,
    onLevelSelect: () -> Unit,
    onRobotCollection: () -> Unit,
    onSettings: () -> Unit,
    onHowToPlay: () -> Unit
) {
    val totalCrystals by viewModel.totalCrystals.collectAsState()
    val maxUnlocked by viewModel.maxUnlockedLevel.collectAsState()
    val equippedSkin by viewModel.equippedSkin.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceDarkBg)
            .testTag("main_menu_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .widthIn(max = 500.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Crystal Bank & Equipped Skin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skin badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SpaceSurface)
                        .border(1.dp, equippedSkin.accentColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .clickable { onRobotCollection() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("menu_skin_chip")
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(equippedSkin.accentColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = equippedSkin.name,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Crystal counter chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SpaceSurface)
                        .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("menu_crystals_chip")
                ) {
                    Icon(
                        imageVector = Icons.Default.Diamond,
                        contentDescription = "Crystals",
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$totalCrystals",
                        color = NeonCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Game Logo and Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "NEON HOP",
                    color = NeonCyan,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "★ STAR CIRCUIT ★",
                    color = NeonMagenta,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "SECTOR $maxUnlocked OF 5 UNLOCKED",
                    color = NeonGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Menu Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Primary Play Button
                Button(
                    onClick = onPlay,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .testTag("play_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = SpaceDarkBg
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (maxUnlocked > 1) "CONTINUE SECTOR $maxUnlocked" else "START MISSION",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Endless Mode Quick Launch
                val endlessBest = viewModel.prefs.getEndlessHighScore()
                MenuSecondaryButton(
                    title = "HYPER CIRCUIT (ENDLESS)",
                    subtitle = if (endlessBest > 0) "Best: $endlessBest pts • Infinite space run" else "Infinite procedural space corridor",
                    icon = Icons.Default.Bolt,
                    accentColor = NeonMagenta,
                    tag = "endless_quick_launch_button",
                    onClick = { viewModel.startLevel(com.example.game.Levels.ENDLESS_LEVEL_ID) }
                )

                // Level Selection
                MenuSecondaryButton(
                    title = "SECTOR SELECT",
                    subtitle = "Browse all 5 circuits & high scores",
                    icon = Icons.Default.ViewModule,
                    accentColor = NeonMagenta,
                    tag = "level_select_button",
                    onClick = onLevelSelect
                )

                // Robot Collection
                MenuSecondaryButton(
                    title = "ROBOT COLLECTION",
                    subtitle = "Customize NOVA chassis & glow",
                    icon = Icons.Default.Palette,
                    accentColor = NeonGold,
                    tag = "robot_collection_button",
                    onClick = onRobotCollection
                )

                // How to Play
                MenuSecondaryButton(
                    title = "FLIGHT MANUAL",
                    subtitle = "Controls, mechanics & power-ups",
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    accentColor = NeonCyan,
                    tag = "how_to_play_button",
                    onClick = onHowToPlay
                )

                // Settings
                MenuSecondaryButton(
                    title = "SYSTEM SETTINGS",
                    subtitle = "Audio, haptics & progress",
                    icon = Icons.Default.Settings,
                    accentColor = TextSecondary,
                    tag = "settings_button",
                    onClick = onSettings
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Footer version
            Text(
                text = "NEON HOP v1.0 • STAR CIRCUIT ENGINE",
                color = TextSecondary.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MenuSecondaryButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SpaceCardBg)
            .border(1.dp, SpaceCardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
        Text(
            text = "▶",
            color = accentColor.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
    }
}
