package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.GamePreferences
import com.example.game.GameEngine
import com.example.game.Levels
import com.example.game.model.Hazard
import com.example.game.model.HazardType
import com.example.game.model.Platform
import com.example.game.model.PlatformType
import com.example.game.model.PowerUpType
import com.example.game.model.RobotSkins
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Neon Hop", appName)
    }

    @Test
    fun `verify all 5 levels exist with proper geometry`() {
        assertEquals(5, Levels.ALL.size)
        for (i in 1..5) {
            val level = Levels.getLevel(i)
            assertEquals(i, level.id)
            assertTrue("Level $i has platforms", level.platforms.isNotEmpty())
            assertTrue("Level $i has crystals", level.crystals.isNotEmpty())
            assertNotNull("Level $i has portal", level.portal)
        }
    }

    @Test
    fun `verify game preferences save and restore`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = GamePreferences(context)
        prefs.resetAllProgress()

        assertEquals(1, prefs.maxUnlockedLevel)
        assertTrue(prefs.isLevelUnlocked(1))
        assertFalse(prefs.isLevelUnlocked(2))

        prefs.unlockNextLevel(1)
        assertEquals(2, prefs.maxUnlockedLevel)
        assertTrue(prefs.isLevelUnlocked(2))

        prefs.addCrystals(500)
        assertEquals(500, prefs.totalCrystals)

        val spent = prefs.spendCrystals(300)
        assertTrue(spent)
        assertEquals(200, prefs.totalCrystals)
    }

    @Test
    fun `verify jumping and double jumping physics`() {
        val level = Levels.getLevel(1)
        val engine = GameEngine(level)

        // Settle on ground
        for (i in 0 until 25) {
            engine.update(0.016f, 0f, false, false, 420f, 700f)
        }
        assertTrue(engine.player.isGrounded)
        assertEquals(0, engine.player.jumpCount)

        // First jump
        engine.update(0.016f, 0f, jumpPressed = true, dashPressed = false, 420f, 700f)
        assertFalse(engine.player.isGrounded)
        assertTrue(engine.player.vy < 0f)
        assertEquals(1, engine.player.jumpCount)

        // Release jump button in air
        engine.update(0.016f, 0f, jumpPressed = false, dashPressed = false, 420f, 700f)

        // Double jump in air
        engine.update(0.016f, 0f, jumpPressed = true, dashPressed = false, 420f, 700f)
        assertEquals(2, engine.player.jumpCount)
        assertTrue(engine.player.vy < 0f)
    }

    @Test
    fun `verify air jump recovery after walking off ledge`() {
        val level = Levels.getLevel(1)
        val engine = GameEngine(level)

        // Force player in air without jumping
        engine.player.isGrounded = false
        engine.player.jumpCount = 0

        // Air jump
        engine.update(0.016f, 0f, jumpPressed = true, dashPressed = false, 420f, 700f)
        assertEquals(2, engine.player.jumpCount)
        assertTrue(engine.player.vy < 0f)
    }

    @Test
    fun `verify safe landing shield absorbs hazard damage`() {
        val level = Levels.getLevel(1)
        val engine = GameEngine(level)

        // Equip Safe Landing shield
        engine.player.activePowerUp = PowerUpType.SAFE_LANDING
        engine.player.hasShield = true

        // Place a spike hazard directly on the player
        val spike = Hazard(type = HazardType.SPIKE, x = engine.player.x, y = engine.player.y, width = 30f, height = 30f)
        (engine.hazards as MutableList).add(spike)

        // Run update frame
        engine.update(0.016f, 0f, false, false, 420f, 700f)

        // Player should NOT have died; shield absorbed it!
        assertFalse("Player survived via shield", engine.isGameOver)
        assertFalse("Shield was consumed", engine.player.hasShield)
        assertTrue("Invincibility timer activated", engine.player.invincibilityTimer > 0f)
    }

    @Test
    fun `verify crystal magnet pulls nearby crystals`() {
        val level = Levels.getLevel(1)
        val engine = GameEngine(level)

        val crystal = engine.crystals.first()
        crystal.x = engine.player.centerX + 50f
        crystal.y = engine.player.centerY + 50f
        val initialDist = Math.hypot((crystal.x - engine.player.centerX).toDouble(), (crystal.y - engine.player.centerY).toDouble())

        // Activate magnet
        engine.player.activePowerUp = PowerUpType.CRYSTAL_MAGNET
        engine.player.powerUpTimer = 8f

        // Update frame
        engine.update(0.03f, 0f, false, false, 420f, 700f)

        val newDist = Math.hypot((crystal.x - engine.player.centerX).toDouble(), (crystal.y - engine.player.centerY).toDouble())
        assertTrue("Crystal moved closer to player", newDist < initialDist)
    }

    @Test
    fun `verify moving platform carries player`() {
        val level = Levels.getLevel(2)
        val engine = GameEngine(level)

        // Find a moving X platform
        val movingPlatform = engine.platforms.first { it.type == PlatformType.MOVING_X }
        // Place player on top of it
        engine.player.x = movingPlatform.x + 20f
        engine.player.y = movingPlatform.top - engine.player.height
        engine.player.isGrounded = true
        engine.player.standingOnPlatform = movingPlatform

        val initialPlayerX = engine.player.x
        val initialPlatformX = movingPlatform.x

        // Run frames
        for (i in 0 until 5) {
            engine.update(0.016f, 0f, false, false, 420f, 700f)
        }

        val platformDeltaX = movingPlatform.x - initialPlatformX
        val playerDeltaX = engine.player.x - initialPlayerX

        assertTrue("Platform moved", platformDeltaX != 0f)
        assertEquals("Player carried along with moving platform", platformDeltaX, playerDeltaX, 1.0f)
    }

    @Test
    fun `verify robot skins catalog`() {
        assertEquals(5, RobotSkins.ALL.size)
        val defaultSkin = RobotSkins.getSkin("cyan_pulse")
        assertNotNull(defaultSkin)
        assertEquals(0, defaultSkin.cost)
    }

    @Test
    fun `verify coyote time allows first jump within grace window`() {
        val level = Levels.getLevel(1)
        val engine = GameEngine(level)

        // Settle on ground to activate coyote grace window
        for (i in 0 until 25) {
            engine.update(0.016f, 0f, false, false, 420f, 700f)
        }
        assertTrue(engine.player.isGrounded)

        // Step off platform into air for 2 frames (~0.032s < 0.14s coyote window)
        engine.player.isGrounded = false
        engine.update(0.016f, 0f, false, false, 420f, 700f)
        assertTrue("Coyote window still active", engine.player.coyoteTimer > 0f)

        // Jump during coyote grace window: should award ground jump (jumpCount = 1)
        engine.update(0.016f, 0f, jumpPressed = true, dashPressed = false, 420f, 700f)
        assertEquals(1, engine.player.jumpCount)
        assertTrue(engine.player.vy < 0f)
    }

    @Test
    fun `verify jump buffer executes jump on landing`() {
        val level = Levels.getLevel(1)
        val engine = GameEngine(level)

        // Place player slightly above platform (airborne)
        val plat = engine.platforms.first()
        engine.player.x = plat.x + 20f
        engine.player.y = plat.top - engine.player.height - 20f
        engine.player.vy = 200f
        engine.player.isGrounded = false
        engine.player.jumpCount = 2

        // Tap jump button early while still 20px above ground (air jumps already consumed)
        engine.update(0.016f, 0f, jumpPressed = true, dashPressed = false, 420f, 700f)
        assertTrue("Jump buffer registered", engine.player.jumpBufferTimer > 0f)

        // Advance frames until player touches ground
        for (i in 0 until 10) {
            engine.update(0.016f, 0f, jumpPressed = false, dashPressed = false, 420f, 700f)
        }

        // Jump buffer should have fired upon landing, launching NOVA upward
        assertTrue("Player jumped via buffer", engine.player.vy < 0f)
    }

    @Test
    fun `verify crystal combo and floating text generation`() {
        val level = Levels.getLevel(1)
        val engine = GameEngine(level)

        // Place player directly on first crystal
        val c1 = engine.crystals[0]
        engine.player.x = c1.x - engine.player.width / 2f
        engine.player.y = c1.y - engine.player.height / 2f

        engine.update(0.016f, 0f, false, false, 420f, 700f)

        assertTrue("First crystal collected", c1.collected)
        assertEquals(1, engine.comboCount)
        assertEquals(1, engine.floatingTexts.size)
        assertTrue(engine.currentScore >= 100)

        // Collect second crystal while combo timer active
        val c2 = engine.crystals[1]
        c2.x = engine.player.centerX
        c2.y = engine.player.centerY

        engine.update(0.016f, 0f, false, false, 420f, 700f)

        assertTrue("Second crystal collected", c2.collected)
        assertEquals(2, engine.comboCount)
        assertEquals(2, engine.floatingTexts.size)
        // Bonus awarded for combo
        assertTrue("Combo text generated", engine.floatingTexts.any { it.text.contains("2x") })
    }

    @Test
    fun `verify smooth horizontal movement on ground platform without getting stuck`() {
        val level = Levels.getLevel(1)
        val engine = GameEngine(level)

        // Settle on ground
        for (i in 0 until 15) {
            engine.update(0.016f, 0f, false, false, 420f, 700f)
        }
        assertTrue(engine.player.isGrounded)

        val startX = engine.player.x
        // Move right continuously across platform for 20 frames
        for (i in 0 until 20) {
            engine.update(0.016f, moveInput = 1.0f, jumpPressed = false, dashPressed = false, 420f, 700f)
        }

        assertTrue("Player moved smoothly rightward without stuck collision", engine.player.x > startX + 30f)
        assertTrue("Player is still grounded while walking", engine.player.isGrounded)
        assertTrue("Player horizontal velocity is positive", engine.player.vx > 0f)
    }

    @Test
    fun `verify screen shake triggers on dash and hazard impact`() {
        val level = Levels.getLevel(1)
        val engine = GameEngine(level)

        // Dash trigger
        engine.update(0.016f, 0f, jumpPressed = false, dashPressed = true, 420f, 700f)
        assertTrue("Dash triggers screen shake", engine.screenShakeTimer > 0f)
        assertTrue("Shake intensity is positive", engine.screenShakeIntensity > 0f)

        // Shield hazard impact trigger
        engine.player.activePowerUp = PowerUpType.SAFE_LANDING
        engine.player.hasShield = true
        val spike = Hazard(type = HazardType.SPIKE, x = engine.player.x, y = engine.player.y, width = 30f, height = 30f)
        engine.hazards.add(spike)

        engine.update(0.016f, 0f, false, false, 420f, 700f)
        assertTrue("Shield absorb triggers strong screen shake", engine.screenShakeTimer > 0f)
    }

    @Test
    fun `verify endless mode procedurally generates new platform chunks`() {
        val endlessLevel = Levels.getLevel(Levels.ENDLESS_LEVEL_ID)
        assertEquals("HYPER CIRCUIT", endlessLevel.title)
        val engine = GameEngine(endlessLevel)
        assertTrue(engine.isEndlessMode)

        val initialPlatformCount = engine.platforms.size
        val initialDistance = engine.endlessDistanceGenerated

        // Teleport player near the end of the initial chunk to trigger procedural generation
        engine.player.x = initialDistance - 600f
        engine.update(0.016f, 0f, false, false, 420f, 700f)

        assertTrue("More platforms generated", engine.platforms.size > initialPlatformCount)
        assertTrue("World distance expanded", engine.endlessDistanceGenerated > initialDistance)
        assertTrue("World width extended", engine.currentWorldWidth > initialDistance)
    }

    @Test
    fun `verify endless high score persistence in preferences`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = GamePreferences(context)
        prefs.resetAllProgress()

        assertEquals(0, prefs.getEndlessHighScore())
        val saved = prefs.saveEndlessHighScore(1250)
        assertTrue(saved)
        assertEquals(1250, prefs.getEndlessHighScore())

        // Lower score does not overwrite
        val notSaved = prefs.saveEndlessHighScore(800)
        assertFalse(notSaved)
        assertEquals(1250, prefs.getEndlessHighScore())
    }

    @Test
    fun `verify level restart creates independent engine with fresh player and platforms`() {
        val level1 = Levels.getLevel(1)
        val engine1 = GameEngine(level1)

        // Player moves and collects crystal
        engine1.update(0.1f, 1f, false, false, 420f, 700f)
        engine1.crystals[0].collected = true
        engine1.currentScore = 500

        // Restart level
        val engine2 = GameEngine(Levels.getLevel(1))

        assertEquals("New engine starts at initial player start X", level1.playerStartX, engine2.player.x, 0.01f)
        assertEquals("New engine starts at initial player start Y", level1.playerStartY, engine2.player.y, 0.01f)
        assertEquals("New engine score is 0", 0, engine2.currentScore)
        assertFalse("New engine crystals are uncollected", engine2.crystals[0].collected)
        assertFalse("New engine is not game over", engine2.isGameOver)
        assertFalse("New engine is not level complete", engine2.isLevelComplete)
    }

    @Test
    fun `verify advancing from level 1 to level 2 creates clean engine with next level data`() {
        val level1 = Levels.getLevel(1)
        val engine1 = GameEngine(level1)
        assertEquals(1, engine1.level.id)

        val level2 = Levels.getLevel(2)
        val engine2 = GameEngine(level2)
        assertEquals(2, engine2.level.id)
        assertEquals("BROKEN ORBIT", engine2.level.title)
        assertEquals(0, engine2.currentScore)
        assertFalse(engine2.isGameOver)
        assertFalse(engine2.isLevelComplete)
    }

    @Test
    fun `verify game input state resets all directions and buttons cleanly`() {
        val input = com.example.ui.GameInputState()
        input.moveLeft = true
        input.moveRight = true
        input.jump = true
        input.dash = true

        input.reset()
        assertFalse(input.moveLeft)
        assertFalse(input.moveRight)
        assertFalse(input.jump)
        assertFalse(input.dash)
    }
}
