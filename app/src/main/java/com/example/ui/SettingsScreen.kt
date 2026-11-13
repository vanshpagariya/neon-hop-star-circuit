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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.SpaceCardBg
import com.example.ui.theme.SpaceCardBorder
import com.example.ui.theme.SpaceDarkBg
import com.example.ui.theme.SpaceSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val sfxEnabled by viewModel.sfxEnabled.collectAsState()
    val musicEnabled by viewModel.musicEnabled.collectAsState()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()
    val maxUnlocked by viewModel.maxUnlockedLevel.collectAsState()
    val totalCrystals by viewModel.totalCrystals.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceDarkBg)
            .testTag("settings_screen"),
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
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NeonCyan
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SYSTEM SETTINGS",
                    color = NeonCyan,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Audio & Haptics Section
            Text(
                text = "AUDIO & FEEDBACK",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SpaceCardBg)
                    .border(1.dp, SpaceCardBorder, RoundedCornerShape(16.dp))
            ) {
                SettingToggleItem(
                    title = "Sound Effects",
                    subtitle = "Procedural arcade jumps, crystals, hazard alerts",
                    icon = Icons.Default.GraphicEq,
                    checked = sfxEnabled,
                    tag = "toggle_sfx",
                    onCheckedChange = { viewModel.toggleSfx() }
                )
                SettingToggleItem(
                    title = "Synthesizer Music",
                    subtitle = "Ambient cosmic arpeggiator soundtrack",
                    icon = Icons.Default.MusicNote,
                    checked = musicEnabled,
                    tag = "toggle_music",
                    onCheckedChange = { viewModel.toggleMusic() }
                )
                SettingToggleItem(
                    title = "Tactile Haptics",
                    subtitle = "Physical vibration pulses during action",
                    icon = Icons.Default.Vibration,
                    checked = hapticsEnabled,
                    tag = "toggle_haptics",
                    onCheckedChange = { viewModel.toggleHaptics() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Player Stats Section
            Text(
                text = "PILOT TELEMETRY",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SpaceCardBg)
                    .border(1.dp, SpaceCardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatRow("MAX SECTOR UNLOCKED", "Sector $maxUnlocked / 5")
                StatRow("BANKED CRYSTALS", "$totalCrystals")
                StatRow("TOTAL PLAY TIME", "${viewModel.prefs.totalPlayTimeSeconds / 60} min ${viewModel.prefs.totalPlayTimeSeconds % 60} sec")
                StatRow("DATA STORAGE", "Local Device SharedPreferences")
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Danger Zone: Reset Progress
            Text(
                text = "DATA RESET",
                color = NeonMagenta,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("reset_progress_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonMagenta)
            ) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = NeonMagenta)
                Spacer(modifier = Modifier.width(8.dp))
                Text("RESET ALL PROGRESS", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }

        // Reset Confirmation Dialog
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = {
                    Text(
                        text = "ERASE SAVE DATA?",
                        color = NeonMagenta,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                },
                text = {
                    Text(
                        text = "This will permanently reset all unlocked sectors, high scores, collected crystals, and cosmetic robot skins back to default. This action cannot be undone.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetAllProgress()
                            showResetDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta, contentColor = Color.White),
                        modifier = Modifier.testTag("confirm_reset_button")
                    ) {
                        Text("RESET EVERYTHING", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("CANCEL", color = TextPrimary)
                    }
                },
                containerColor = SpaceCardBg
            )
        }
    }
}

@Composable
fun SettingToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    tag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) NeonCyan else TextSecondary,
                modifier = Modifier.size(22.dp)
            )
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
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SpaceDarkBg,
                checkedTrackColor = NeonCyan,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = SpaceSurface
            ),
            modifier = Modifier.testTag(tag)
        )
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Text(text = value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}
