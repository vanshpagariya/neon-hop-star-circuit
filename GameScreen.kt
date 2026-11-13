package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.game.GameEngine
import com.example.game.GameRenderer
import com.example.game.model.PowerUpType
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.SpaceCardBg
import com.example.ui.theme.SpaceCardBorder
import com.example.ui.theme.SpaceDarkBg
import com.example.ui.theme.SpaceSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.min

class GameInputState {
    var moveLeft: Boolean = false
    var moveRight: Boolean = false
    var jump: Boolean = false
    var dash: Boolean = false

    fun reset() {
        moveLeft = false
        moveRight = false
        jump = false
        dash = false
    }
}

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    levelId: Int,
    onBackToMenu: () -> Unit,
    onLevelSelect: () -> Unit
) {
    val engine by viewModel.engine.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val equippedSkin by viewModel.equippedSkin.collectAsState()

    BackHandler {
        if (!isPaused && engine != null && !engine!!.isGameOver && !engine!!.isLevelComplete) {
            viewModel.pauseGame()
        } else {
            onBackToMenu()
        }
    }

    // Lifecycle observer to automatically pause the game on background
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.pauseGame()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (engine == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(SpaceDarkBg),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Initializing Star Circuit...", color = NeonCyan, fontFamily = FontFamily.Monospace)
        }
        return
    }

    val activeEngine = engine!!
    val renderer = remember { GameRenderer() }
    val sfxEnabled by viewModel.sfxEnabled.collectAsState()
    val inputState = remember { GameInputState() }
    val focusRequester = remember { FocusRequester() }

    // Clear controls when restarting, pausing, or changing levels
    LaunchedEffect(activeEngine) {
        inputState.reset()
        focusRequester.requestFocus()
    }

    LaunchedEffect(isPaused) {
        if (isPaused) {
            inputState.reset()
        }
    }

    // Critical render invalidation ticker to guarantee continuous 60/120 FPS drawing on Canvas
    var renderTick by remember(activeEngine) { mutableLongStateOf(0L) }

    // HUD reactive states (tied to activeEngine instance so restarting is clean)
    var hudScore by remember(activeEngine) { mutableIntStateOf(0) }
    var hudCrystals by remember(activeEngine) { mutableIntStateOf(0) }
    var hudCombo by remember(activeEngine) { mutableIntStateOf(0) }
    var hudTime by remember(activeEngine) { mutableFloatStateOf(0f) }
    var hudPowerUp by remember(activeEngine) { mutableStateOf(PowerUpType.NONE) }
    var hudPowerUpRemaining by remember(activeEngine) { mutableFloatStateOf(0f) }
    var hudGameOver by remember(activeEngine) { mutableStateOf(false) }
    var hudGameOverReason by remember(activeEngine) { mutableStateOf("") }
    var hudLevelComplete by remember(activeEngine) { mutableStateOf(false) }
    var hasTriggeredCompletion by remember(activeEngine) { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceDarkBg)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown || keyEvent.type == KeyEventType.KeyUp) {
                    val isDown = keyEvent.type == KeyEventType.KeyDown
                    when (keyEvent.key) {
                        Key.DirectionLeft, Key.A -> {
                            inputState.moveLeft = isDown
                            true
                        }
                        Key.DirectionRight, Key.D -> {
                            inputState.moveRight = isDown
                            true
                        }
                        Key.DirectionUp, Key.W, Key.Spacebar -> {
                            inputState.jump = isDown
                            true
                        }
                        Key.ShiftLeft, Key.ShiftRight, Key.K, Key.X -> {
                            inputState.dash = isDown
                            true
                        }
                        Key.Escape, Key.P -> {
                            if (isDown) {
                                if (isPaused) viewModel.resumeGame() else viewModel.pauseGame()
                            }
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
            .testTag("gameplay_screen")
    ) {
        // Virtual coordinate sizing for pixel-perfect scaling across all Android device displays
        val screenWidth = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val screenHeight = constraints.maxHeight.toFloat().coerceAtLeast(1f)
        val virtualWidth = 420f
        val gameScale = screenWidth / virtualWidth
        val virtualHeight = screenHeight / gameScale

        // High performance frame-driven game loop
        LaunchedEffect(activeEngine, isPaused, virtualWidth, virtualHeight) {
            var lastFrameNanos = 0L
            while (!isPaused && !activeEngine.isGameOver && !activeEngine.isLevelComplete) {
                withFrameNanos { now ->
                    if (lastFrameNanos != 0L) {
                        val rawDt = (now - lastFrameNanos) / 1_000_000_000f
                        val dt = min(rawDt, 0.04f)

                        val moveInput = when {
                            inputState.moveLeft && !inputState.moveRight -> -1f
                            inputState.moveRight && !inputState.moveLeft -> 1f
                            else -> 0f
                        }

                        activeEngine.update(
                            rawDt = dt,
                            moveInput = moveInput,
                            jumpPressed = inputState.jump,
                            dashPressed = inputState.dash,
                            viewportWidth = virtualWidth,
                            viewportHeight = virtualHeight
                        )

                        // Trigger Canvas draw invalidation every frame
                        renderTick = now

                        // Sync HUD
                        hudScore = activeEngine.currentScore
                        hudCrystals = activeEngine.crystalsCollected
                        hudCombo = activeEngine.comboCount
                        hudTime = activeEngine.elapsedTime
                        hudPowerUp = activeEngine.player.activePowerUp
                        hudPowerUpRemaining = activeEngine.player.powerUpTimer
                        hudGameOver = activeEngine.isGameOver
                        hudGameOverReason = activeEngine.gameOverReason
                        hudLevelComplete = activeEngine.isLevelComplete

                        if (activeEngine.isLevelComplete && !hasTriggeredCompletion) {
                            hasTriggeredCompletion = true
                            viewModel.onLevelVictory(
                                levelId = activeEngine.level.id,
                                finalScore = activeEngine.currentScore,
                                crystals = activeEngine.crystalsCollected,
                                timeTaken = activeEngine.elapsedTime
                            )
                        } else if (activeEngine.isGameOver && !hasTriggeredCompletion) {
                            hasTriggeredCompletion = true
                            viewModel.onLevelFailed(
                                crystals = activeEngine.crystalsCollected,
                                timeTaken = activeEngine.elapsedTime
                            )
                        }
                    }
                    lastFrameNanos = now
                }
            }
        }

        // 1. Android Canvas 2D Game Rendering
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val _frame = renderTick // State read invalidates Canvas every frame for silky-smooth motion!
            renderer.render(
                scope = this,
                engine = activeEngine,
                skin = equippedSkin,
                virtualWidth = virtualWidth,
                virtualHeight = virtualHeight,
                scale = gameScale
            )
        }

        // 2. Heads-Up Display (HUD)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top HUD Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Score & Sector Title
                    Column {
                        Text(
                            text = activeEngine.level.title,
                            color = activeEngine.level.themeColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "SCORE: $hudScore",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        if (hudCombo > 1) {
                            Text(
                                text = "★ ${hudCombo}x COMBO!",
                                color = NeonGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Crystals Counter, Audio Toggle & Pause Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Crystal badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(SpaceSurface.copy(alpha = 0.85f))
                                .border(1.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Diamond,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$hudCrystals / ${activeEngine.level.crystals.size}",
                                color = NeonCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Audio Toggle Quick Button
                        IconButton(
                            onClick = { viewModel.toggleSfx() },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SpaceSurface.copy(alpha = 0.85f))
                                .border(1.dp, SpaceCardBorder, CircleShape)
                                .testTag("quick_audio_toggle")
                        ) {
                            Icon(
                                imageVector = if (sfxEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = "Toggle Audio",
                                tint = if (sfxEnabled) NeonCyan else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Pause Button
                        IconButton(
                            onClick = { viewModel.pauseGame() },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SpaceSurface.copy(alpha = 0.85f))
                                .border(1.dp, SpaceCardBorder, CircleShape)
                                .testTag("pause_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Pause Game",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Power-up status badge (if active)
                AnimatedVisibility(
                    visible = hudPowerUp != PowerUpType.NONE && hudPowerUpRemaining > 0f,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SpaceSurface.copy(alpha = 0.9f))
                            .border(1.dp, hudPowerUp.badgeColor, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .width(180.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = hudPowerUp.displayName.uppercase(),
                                color = hudPowerUp.badgeColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${"%.1f".format(hudPowerUpRemaining)}s",
                                color = TextPrimary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (hudPowerUpRemaining / hudPowerUp.durationSec).coerceIn(0f, 1f) },
                            color = hudPowerUp.badgeColor,
                            trackColor = Color(0x33FFFFFF),
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                        )
                    }
                }
            }

            // Bottom On-Screen Controls
            key(activeEngine) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Directional Pad (Left & Right)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GameTouchButton(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Move Left",
                            color = NeonCyan,
                            size = 66.dp,
                            touchSize = 84.dp,
                            tag = "touch_left",
                            onPressChange = { inputState.moveLeft = it }
                        )
                        GameTouchButton(
                            icon = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Move Right",
                            color = NeonCyan,
                            size = 66.dp,
                            touchSize = 84.dp,
                            tag = "touch_right",
                            onPressChange = { inputState.moveRight = it }
                        )
                    }

                    // Action Buttons (Dash & Jump)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Dash Button
                        GameTouchButton(
                            icon = Icons.Default.Bolt,
                            contentDescription = "Dash",
                            color = NeonMagenta,
                            size = 60.dp,
                            touchSize = 80.dp,
                            tag = "touch_dash",
                            rechargeProgress = if (activeEngine.player.dashCooldown > 0f) (activeEngine.player.dashCooldown / 0.9f).coerceIn(0f, 1f) else 0f,
                            onPressChange = { inputState.dash = it }
                        )

                        // Jump Button (Large, prominent)
                        GameTouchButton(
                            icon = Icons.Default.KeyboardDoubleArrowUp,
                            contentDescription = "Jump",
                            color = NeonCyan,
                            size = 74.dp,
                            touchSize = 92.dp,
                            tag = "touch_jump",
                            onPressChange = { inputState.jump = it }
                        )
                    }
                }
            }
        }

        // 3. Pause Overlay Modal
        if (isPaused) {
            PauseDialog(
                onResume = { viewModel.resumeGame() },
                onRestart = { viewModel.startLevel(activeEngine.level.id) },
                onLevelSelect = onLevelSelect,
                onMainMenu = onBackToMenu
            )
        }

        // 4. Game Over Dialog
        if (hudGameOver) {
            GameOverDialog(
                reason = hudGameOverReason,
                score = hudScore,
                crystals = hudCrystals,
                onRestart = { viewModel.startLevel(activeEngine.level.id) },
                onLevelSelect = onLevelSelect,
                onMainMenu = onBackToMenu
            )
        }

        // 5. Level Complete Dialog
        if (hudLevelComplete) {
            val currentLvlId = activeEngine.level.id
            LevelCompleteDialog(
                levelId = currentLvlId,
                score = hudScore,
                crystals = hudCrystals,
                totalCrystals = activeEngine.level.crystals.size,
                time = hudTime,
                targetTime = activeEngine.level.targetTimeSeconds,
                onNextLevel = {
                    if (currentLvlId < 5) {
                        viewModel.startLevel(currentLvlId + 1)
                    } else {
                        onLevelSelect()
                    }
                },
                onReplay = { viewModel.startLevel(currentLvlId) },
                onLevelSelect = onLevelSelect
            )
        }
    }
}

/**
 * Ultra-responsive touch controller button with pointer-ID tracking.
 * Maintains press state continuously while thumb is held down, even if the thumb slides
 * slightly during fast-paced platforming, with zero dropped inputs and multitouch support.
 */
@Composable
fun GameTouchButton(
    icon: ImageVector,
    contentDescription: String,
    color: Color = NeonCyan,
    size: androidx.compose.ui.unit.Dp = 66.dp,
    touchSize: androidx.compose.ui.unit.Dp = 82.dp,
    tag: String,
    rechargeProgress: Float = 0f,
    onPressChange: (Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val currentOnPressChange by rememberUpdatedState(onPressChange)

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        label = "btn_scale"
    )
    val bgAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.55f else 0.20f,
        label = "btn_bg_alpha"
    )
    val borderAlpha by animateFloatAsState(
        targetValue = if (isPressed) 1.0f else 0.65f,
        label = "btn_border_alpha"
    )

    DisposableEffect(Unit) {
        onDispose {
            isPressed = false
            currentOnPressChange(false)
        }
    }

    Box(
        modifier = Modifier
            .size(touchSize)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val pointerId = down.id
                    down.consume()
                    isPressed = true
                    currentOnPressChange(true)

                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId }
                            if (change == null || !change.pressed) {
                                break
                            }
                            change.consume()
                        }
                    } finally {
                        isPressed = false
                        currentOnPressChange(false)
                    }
                }
            }
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        // Visual circular button
        Box(
            modifier = Modifier
                .size(size)
                .scale(scale)
                .clip(CircleShape)
                .background(color.copy(alpha = if (rechargeProgress > 0f) bgAlpha * 0.5f else bgAlpha))
                .border(2.dp, color.copy(alpha = if (rechargeProgress > 0f) borderAlpha * 0.4f else borderAlpha), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (isPressed) Color.White else if (rechargeProgress > 0f) color.copy(alpha = 0.45f) else color,
                modifier = Modifier.size(size * 0.48f)
            )
        }
    }
}

@Composable
fun PauseDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onLevelSelect: () -> Unit,
    onMainMenu: () -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SpaceCardBg)
                .border(1.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .padding(24.dp)
                .testTag("pause_dialog"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CIRCUIT PAUSED",
                color = NeonCyan,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onResume,
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("resume_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = SpaceDarkBg)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("RESUME CIRCUIT", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("restart_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("RESTART LEVEL", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onLevelSelect,
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("dialog_level_select_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ViewList, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("LEVEL SELECT", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onMainMenu,
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("dialog_main_menu_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Home, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("MAIN MENU", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun GameOverDialog(
    reason: String,
    score: Int,
    crystals: Int,
    onRestart: () -> Unit,
    onLevelSelect: () -> Unit,
    onMainMenu: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SpaceCardBg)
                .border(1.dp, NeonMagenta, RoundedCornerShape(20.dp))
                .padding(24.dp)
                .testTag("game_over_dialog"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CIRCUIT OVERLOAD",
                color = NeonMagenta,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = reason.ifEmpty { "NOVA lost circuit stabilization." },
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Score & Crystals Summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SpaceSurface)
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "SCORE", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "$score", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "CRYSTALS SAVED", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "+$crystals", color = NeonCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("game_over_retry_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta, contentColor = Color.White)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("RETRY MISSION", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onLevelSelect,
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("game_over_select_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SECTOR SELECT", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onMainMenu,
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("game_over_menu_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("MAIN MENU", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun LevelCompleteDialog(
    levelId: Int,
    score: Int,
    crystals: Int,
    totalCrystals: Int,
    time: Float,
    targetTime: Float,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onLevelSelect: () -> Unit
) {
    // Star Calculation
    var stars = 1
    if (crystals >= (totalCrystals * 0.75f).toInt()) stars = 2
    if (crystals >= totalCrystals && time <= targetTime) stars = 3

    Dialog(onDismissRequest = {}) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SpaceCardBg)
                .border(1.dp, NeonGold, RoundedCornerShape(20.dp))
                .padding(24.dp)
                .testTag("level_complete_dialog"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SECTOR COMPLETE!",
                color = NeonGold,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Star Rating Fanfare
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (s in 1..3) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star $s",
                        tint = if (s <= stars) NeonGold else Color(0xFF2A3154),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Score & Stats breakdown
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SpaceSurface)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "TOTAL SCORE", color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "$score", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "CRYSTALS COLLECTED", color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "$crystals / $totalCrystals", color = NeonCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "CLEAR TIME", color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "${"%.1f".format(time)}s (PAR ${targetTime.toInt()}s)", color = NeonGold, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (levelId < 5) {
                Button(
                    onClick = onNextLevel,
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("next_level_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGold, contentColor = SpaceDarkBg)
                ) {
                    Text("PROCEED TO SECTOR ${levelId + 1}", fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            OutlinedButton(
                onClick = onReplay,
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("victory_replay_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("REPLAY SECTOR", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onLevelSelect,
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("victory_level_select_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SECTOR SELECT", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
    }
}
