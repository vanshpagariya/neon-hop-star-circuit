package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.SpaceCardBg
import com.example.ui.theme.SpaceCardBorder
import com.example.ui.theme.SpaceDarkBg
import com.example.ui.theme.SpaceSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HowToPlayScreen(
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceDarkBg)
            .testTag("how_to_play_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("how_to_play_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NeonCyan
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "FLIGHT MANUAL",
                        color = NeonCyan,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "NOVA PILOT TRAINING",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Objective
            SectionCard(title = "MISSION OBJECTIVE", accentColor = NeonCyan) {
                Text(
                    text = "Guide NOVA through abandoned space stations and alien spires. Collect shimmering energy crystals, bypass hazardous defense systems, and reach each sector's Exit Portal.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Controls
            SectionCard(title = "FLIGHT CONTROLS", accentColor = NeonMagenta) {
                GuideRow(
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    title = "Horizontal Propulsion",
                    desc = "Press and hold the Left or Right buttons to steer NOVA with smooth acceleration and braking."
                )
                GuideRow(
                    icon = Icons.Default.KeyboardDoubleArrowUp,
                    title = "Jump & Double Jump",
                    desc = "Tap Jump once from the ground, then tap again while airborne to trigger NOVA's secondary rocket booster."
                )
                GuideRow(
                    icon = Icons.Default.Bolt,
                    title = "Hyper Dash",
                    desc = "Tap the Dash button for an instantaneous high-speed burst across treacherous gaps."
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Collectibles & Power-Ups
            SectionCard(title = "POWER-UPS & ENERGY", accentColor = NeonGold) {
                GuideRow(
                    icon = Icons.Default.Diamond,
                    iconTint = NeonCyan,
                    title = "Energy Crystals",
                    desc = "Standard crystals give +100 pts. Golden secret crystals hidden on daring routes award +250 pts and cosmetic currency."
                )
                GuideRow(
                    icon = Icons.Default.Speed,
                    iconTint = NeonCyan,
                    title = "Light Burst (Cyan Orb)",
                    desc = "Supercharges horizontal movement speed by +50% for 8 seconds with neon speed trails."
                )
                GuideRow(
                    icon = Icons.Default.Shield,
                    iconTint = NeonGreen,
                    title = "Safe Landing (Green Orb)",
                    desc = "Envelops NOVA in an energy forcefield that absorbs a lethal hazard hit and softens falls."
                )
                GuideRow(
                    icon = Icons.Default.Diamond,
                    iconTint = NeonMagenta,
                    title = "Crystal Magnet (Magenta Orb)",
                    desc = "Emits a localized gravity well pulling all nearby crystals directly toward NOVA."
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hazards
            SectionCard(title = "SECTOR HAZARDS", accentColor = Color(0xFFFF007F)) {
                GuideRow(
                    icon = Icons.Default.Warning,
                    iconTint = Color(0xFFFF007F),
                    title = "Plasma Spikes & Spinners",
                    desc = "Avoid floor spike fields and rotating laser blades. Without a shield, contact causes immediate circuit overload."
                )
                GuideRow(
                    icon = Icons.Default.Bolt,
                    iconTint = Color(0xFFFF007F),
                    title = "Timed Energy Barriers",
                    desc = "Pulsing laser gates phase on and off. Study the rhythmic cycle and cross only during inactive windows."
                )
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SpaceCardBg)
            .border(1.dp, SpaceCardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            color = accentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

@Composable
fun GuideRow(
    icon: ImageVector,
    iconTint: Color = NeonCyan,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = desc,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
