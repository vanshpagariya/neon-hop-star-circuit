package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.RobotSkin
import com.example.game.model.RobotSkins
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
fun RobotCollectionScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val totalCrystals by viewModel.totalCrystals.collectAsState()
    val equippedSkin by viewModel.equippedSkin.collectAsState()
    val unlockedSkins by viewModel.unlockedSkins.collectAsState()

    var previewSkin by remember { mutableStateOf(equippedSkin) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceDarkBg)
            .testTag("robot_collection_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("collection_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NeonCyan
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ROBOT HANGAR",
                        color = NeonCyan,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                }

                // Crystal Bank Balance
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(SpaceSurface)
                        .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Diamond,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$totalCrystals",
                        color = NeonCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Large NOVA Preview Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SpaceCardBg)
                    .border(1.5.dp, previewSkin.accentColor, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(modifier = Modifier.size(90.dp)) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f

                        // Outer glow
                        drawCircle(
                            color = previewSkin.glowColor,
                            radius = 42f,
                            center = Offset(cx, cy)
                        )

                        // Antenna
                        drawLine(
                            color = previewSkin.accentColor,
                            start = Offset(cx, cy - 25f),
                            end = Offset(cx, cy - 38f),
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                        drawCircle(color = previewSkin.eyeColor, radius = 5f, center = Offset(cx, cy - 40f))

                        // Head Body
                        drawRoundRect(
                            color = previewSkin.bodyColor,
                            topLeft = Offset(cx - 28f, cy - 25f),
                            size = Size(56f, 50f),
                            cornerRadius = CornerRadius(12f, 12f)
                        )
                        drawRoundRect(
                            color = previewSkin.accentColor,
                            topLeft = Offset(cx - 28f, cy - 25f),
                            size = Size(56f, 50f),
                            cornerRadius = CornerRadius(12f, 12f),
                            style = Stroke(width = 2.5f)
                        )

                        // Visor
                        drawRoundRect(
                            color = previewSkin.visorColor,
                            topLeft = Offset(cx - 22f, cy - 15f),
                            size = Size(44f, 22f),
                            cornerRadius = CornerRadius(6f, 6f)
                        )

                        // Glowing Eyes
                        drawRoundRect(
                            color = previewSkin.eyeColor,
                            topLeft = Offset(cx - 15f, cy - 10f),
                            size = Size(8f, 12f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                        drawRoundRect(
                            color = previewSkin.eyeColor,
                            topLeft = Offset(cx + 7f, cy - 10f),
                            size = Size(8f, 12f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )

                        // Chest badge
                        drawCircle(color = previewSkin.accentColor, radius = 3.5f, center = Offset(cx, cy + 16f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = previewSkin.name.uppercase(),
                        color = previewSkin.accentColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Skins list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(RobotSkins.ALL) { skin ->
                    val isUnlocked = unlockedSkins.contains(skin.id) || skin.cost == 0
                    val isEquipped = equippedSkin.id == skin.id
                    val isSelected = previewSkin.id == skin.id

                    SkinItemCard(
                        skin = skin,
                        isUnlocked = isUnlocked,
                        isEquipped = isEquipped,
                        isSelected = isSelected,
                        canAfford = totalCrystals >= skin.cost,
                        onSelect = { previewSkin = skin },
                        onEquip = {
                            viewModel.equipSkin(skin)
                            previewSkin = skin
                        },
                        onUnlock = {
                            if (viewModel.buySkin(skin)) {
                                previewSkin = skin
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SkinItemCard(
    skin: RobotSkin,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    isSelected: Boolean,
    canAfford: Boolean,
    onSelect: () -> Unit,
    onEquip: () -> Unit,
    onUnlock: () -> Unit
) {
    val borderColor = when {
        isEquipped -> NeonCyan
        isSelected -> skin.accentColor
        else -> SpaceCardBorder
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SpaceCardBg)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onSelect)
            .padding(14.dp)
            .testTag("skin_item_${skin.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Color Swatch
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(skin.bodyColor)
                    .border(2.dp, skin.accentColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(skin.eyeColor)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = skin.name,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    if (isEquipped) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "[EQUIPPED]",
                            color = NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Text(
                    text = skin.description,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    maxLines = 2
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Action button
        if (isEquipped) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(NeonCyan.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = NeonCyan)
            }
        } else if (isUnlocked) {
            Button(
                onClick = onEquip,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SpaceSurface, contentColor = NeonCyan),
                modifier = Modifier.testTag("equip_skin_${skin.id}")
            ) {
                Text("EQUIP", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        } else {
            Button(
                onClick = onUnlock,
                enabled = canAfford,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) NeonGold else SpaceSurface,
                    contentColor = if (canAfford) SpaceDarkBg else TextSecondary
                ),
                modifier = Modifier.testTag("unlock_skin_${skin.id}")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Diamond, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${skin.cost}", fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
