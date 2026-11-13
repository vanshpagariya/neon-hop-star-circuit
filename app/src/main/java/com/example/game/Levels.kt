package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.game.model.Crystal
import com.example.game.model.ExitPortal
import com.example.game.model.Hazard
import com.example.game.model.HazardType
import com.example.game.model.LevelDef
import com.example.game.model.Platform
import com.example.game.model.PlatformType
import com.example.game.model.PowerUpItem
import com.example.game.model.PowerUpType

/**
 * Handcrafted deterministic levels for Neon Hop: Star Circuit.
 * Each level features distinct platforming mechanics, environmental hazards,
 * collectible energy crystals, and power-ups.
 */
object Levels {

    const val ENDLESS_LEVEL_ID = 99

    val ALL: List<LevelDef> by lazy {
        listOf(
            createLevel1(),
            createLevel2(),
            createLevel3(),
            createLevel4(),
            createLevel5()
        )
    }

    fun getLevel(id: Int): LevelDef {
        if (id == ENDLESS_LEVEL_ID) return createEndlessLevel()
        return ALL.find { it.id == id } ?: ALL.first()
    }

    // ==========================================
    // LEVEL 1: FIRST LIGHT
    // ==========================================
    private fun createLevel1(): LevelDef {
        val theme = Color(0xFF00F0FF) // Neon Cyan
        val platforms = listOf(
            // Intro floor
            Platform(x = 0f, y = 480f, width = 320f, height = 40f, type = PlatformType.SOLID, color = theme),
            // Low step jump
            Platform(x = 360f, y = 430f, width = 120f, height = 24f, type = PlatformType.SOLID, color = theme),
            // High ledge
            Platform(x = 520f, y = 370f, width = 130f, height = 24f, type = PlatformType.ONE_WAY, color = theme),
            // Power-up perch
            Platform(x = 690f, y = 310f, width = 100f, height = 22f, type = PlatformType.ONE_WAY, color = theme),
            // Chasm bridge
            Platform(x = 840f, y = 390f, width = 160f, height = 24f, type = PlatformType.SOLID, color = theme),
            // Double jump step
            Platform(x = 1050f, y = 330f, width = 120f, height = 24f, type = PlatformType.ONE_WAY, color = theme),
            // Archway platform
            Platform(x = 1210f, y = 260f, width = 140f, height = 24f, type = PlatformType.ONE_WAY, color = theme),
            // Descending steps
            Platform(x = 1390f, y = 340f, width = 110f, height = 24f, type = PlatformType.SOLID, color = theme),
            // Exit portal landing pad
            Platform(x = 1540f, y = 420f, width = 240f, height = 40f, type = PlatformType.SOLID, color = theme)
        )

        val hazards = listOf(
            // Low plasma hazard spikes in gap 1
            Hazard(type = HazardType.SPIKE, x = 320f, y = 520f, width = 40f, height = 20f, color = Color(0xFFFF007F)),
            // Hazard spikes under middle gap
            Hazard(type = HazardType.SPIKE, x = 650f, y = 520f, width = 190f, height = 20f, color = Color(0xFFFF007F)),
            // Bottom void hazard
            Hazard(type = HazardType.VOID_ZONE, x = 0f, y = 590f, width = 1800f, height = 30f)
        )

        val crystals = listOf(
            Crystal(id = 1, x = 180f, y = 440f),
            Crystal(id = 2, x = 250f, y = 420f),
            Crystal(id = 3, x = 420f, y = 380f),
            Crystal(id = 4, x = 580f, y = 320f),
            Crystal(id = 5, x = 740f, y = 260f),
            Crystal(id = 6, x = 910f, y = 340f),
            Crystal(id = 7, x = 1110f, y = 280f),
            Crystal(id = 8, x = 1280f, y = 210f),
            // Secret bonus high crystals
            Crystal(id = 9, x = 1210f, y = 160f, isSecret = true),
            Crystal(id = 10, x = 1280f, y = 140f, isSecret = true)
        )

        val powerUps = listOf(
            PowerUpItem(id = 1, x = 740f, y = 275f, type = PowerUpType.CRYSTAL_MAGNET)
        )

        return LevelDef(
            id = 1,
            title = "FIRST LIGHT",
            subtitle = "Sector 1: Orbit Outpost",
            description = "Welcome NOVA! Master the basics of horizontal propulsion, jumping, crystal collection, and reach the exit portal.",
            worldWidth = 1800f,
            worldHeight = 620f,
            playerStartX = 60f,
            playerStartY = 430f,
            targetTimeSeconds = 25f,
            platforms = platforms,
            hazards = hazards,
            crystals = crystals,
            powerUps = powerUps,
            portal = ExitPortal(x = 1660f, y = 370f),
            themeColor = theme
        )
    }

    // ==========================================
    // LEVEL 2: BROKEN ORBIT
    // ==========================================
    private fun createLevel2(): LevelDef {
        val theme = Color(0xFF2979FF) // Electric Blue
        val platforms = listOf(
            // Start station
            Platform(x = 0f, y = 520f, width = 200f, height = 35f, type = PlatformType.SOLID, color = theme),
            // Moving platform 1 (horizontal patrol)
            Platform(
                x = 240f, y = 470f, width = 100f, height = 22f,
                type = PlatformType.MOVING_X, moveMin = 240f, moveMax = 440f, moveSpeed = 65f,
                color = theme
            ),
            // Safe Landing island
            Platform(x = 480f, y = 440f, width = 120f, height = 24f, type = PlatformType.SOLID, color = theme),
            // Moving platform 2
            Platform(
                x = 640f, y = 400f, width = 95f, height = 22f,
                type = PlatformType.MOVING_X, moveMin = 640f, moveMax = 860f, moveSpeed = 75f,
                color = theme
            ),
            // Middle resting asteroid
            Platform(x = 900f, y = 370f, width = 130f, height = 24f, type = PlatformType.SOLID, color = theme),
            // Vertical elevator
            Platform(
                x = 1070f, y = 360f, width = 95f, height = 22f,
                type = PlatformType.MOVING_Y, moveMin = 210f, moveMax = 390f, moveSpeed = 60f,
                color = theme
            ),
            // High celestial ridge
            Platform(x = 1200f, y = 210f, width = 200f, height = 24f, type = PlatformType.ONE_WAY, color = theme),
            // Fast horizontal glider
            Platform(
                x = 1440f, y = 290f, width = 105f, height = 22f,
                type = PlatformType.MOVING_X, moveMin = 1440f, moveMax = 1700f, moveSpeed = 85f,
                color = theme
            ),
            // Stepping asteroids
            Platform(x = 1750f, y = 360f, width = 90f, height = 22f, type = PlatformType.SOLID, color = theme),
            Platform(x = 1880f, y = 420f, width = 90f, height = 22f, type = PlatformType.SOLID, color = theme),
            // Final station
            Platform(x = 2010f, y = 470f, width = 240f, height = 40f, type = PlatformType.SOLID, color = theme)
        )

        val hazards = listOf(
            Hazard(type = HazardType.SPIKE, x = 200f, y = 570f, width = 280f, height = 20f, color = Color(0xFFFF007F)),
            Hazard(type = HazardType.SPIKE, x = 600f, y = 570f, width = 300f, height = 20f, color = Color(0xFFFF007F)),
            Hazard(type = HazardType.SPIKE, x = 1030f, y = 570f, width = 720f, height = 20f, color = Color(0xFFFF007F)),
            Hazard(type = HazardType.VOID_ZONE, x = 0f, y = 640f, width = 2300f, height = 30f)
        )

        val crystals = listOf(
            Crystal(id = 11, x = 120f, y = 470f),
            Crystal(id = 12, x = 340f, y = 410f),
            Crystal(id = 13, x = 540f, y = 390f),
            Crystal(id = 14, x = 750f, y = 340f),
            Crystal(id = 15, x = 960f, y = 310f),
            Crystal(id = 16, x = 1120f, y = 170f),
            Crystal(id = 17, x = 1280f, y = 160f),
            Crystal(id = 18, x = 1360f, y = 160f),
            Crystal(id = 19, x = 1560f, y = 230f),
            Crystal(id = 20, x = 1790f, y = 300f),
            Crystal(id = 21, x = 1920f, y = 360f),
            // Secret crystal in high orbit
            Crystal(id = 22, x = 1280f, y = 90f, isSecret = true)
        )

        val powerUps = listOf(
            PowerUpItem(id = 2, x = 540f, y = 395f, type = PowerUpType.SAFE_LANDING),
            PowerUpItem(id = 3, x = 1280f, y = 170f, type = PowerUpType.LIGHT_BURST)
        )

        return LevelDef(
            id = 2,
            title = "BROKEN ORBIT",
            subtitle = "Sector 2: Asteroid Drift",
            description = "Traverse drifting space platforms and wide atmospheric chasms. Use Safe Landing to cushion risky leaps.",
            worldWidth = 2300f,
            worldHeight = 680f,
            playerStartX = 60f,
            playerStartY = 470f,
            targetTimeSeconds = 35f,
            platforms = platforms,
            hazards = hazards,
            crystals = crystals,
            powerUps = powerUps,
            portal = ExitPortal(x = 2130f, y = 420f),
            themeColor = theme
        )
    }

    // ==========================================
    // LEVEL 3: NEON FACTORY
    // ==========================================
    private fun createLevel3(): LevelDef {
        val theme = Color(0xFFFF007F) // Neon Magenta
        val platforms = listOf(
            // Start zone
            Platform(x = 0f, y = 540f, width = 220f, height = 35f, type = PlatformType.SOLID, color = theme),
            // Timed energy barrier 1
            Platform(
                x = 260f, y = 490f, width = 110f, height = 22f,
                type = PlatformType.TIMED_ENERGY, activeDuration = 2.2f, inactiveDuration = 1.6f,
                color = theme
            ),
            // Solid staging platform
            Platform(x = 410f, y = 450f, width = 130f, height = 24f, type = PlatformType.SOLID, color = theme),
            // Timed energy barrier 2
            Platform(
                x = 580f, y = 410f, width = 110f, height = 22f,
                type = PlatformType.TIMED_ENERGY, activeDuration = 2.0f, inactiveDuration = 1.4f,
                color = theme
            ),
            // Central factory hub
            Platform(x = 730f, y = 380f, width = 170f, height = 24f, type = PlatformType.SOLID, color = theme),
            // Low conveyor ledge
            Platform(x = 940f, y = 420f, width = 120f, height = 22f, type = PlatformType.SOLID, color = theme),
            // Moving conveyor platform
            Platform(
                x = 1100f, y = 370f, width = 100f, height = 22f,
                type = PlatformType.MOVING_X, moveMin = 1100f, moveMax = 1320f, moveSpeed = 80f,
                color = theme
            ),
            // Factory upper mezzanine
            Platform(x = 1360f, y = 280f, width = 180f, height = 24f, type = PlatformType.ONE_WAY, color = theme),
            // Timed energy barrier 3
            Platform(
                x = 1580f, y = 320f, width = 110f, height = 22f,
                type = PlatformType.TIMED_ENERGY, activeDuration = 1.8f, inactiveDuration = 1.2f,
                color = theme
            ),
            // Stepping nodes
            Platform(x = 1730f, y = 370f, width = 90f, height = 22f, type = PlatformType.SOLID, color = theme),
            Platform(x = 1860f, y = 420f, width = 90f, height = 22f, type = PlatformType.SOLID, color = theme),
            // Exit dock
            Platform(x = 2000f, y = 470f, width = 260f, height = 40f, type = PlatformType.SOLID, color = theme)
        )

        val hazards = listOf(
            // Floor plasma pools
            Hazard(type = HazardType.SPIKE, x = 220f, y = 580f, width = 1780f, height = 20f, color = Color(0xFFFF007F)),
            // Rotating laser spinner 1 in hub - adjusted for safe platform clearance while challenging jump timings
            Hazard(type = HazardType.ROTATING_SPINNER, centerX = 500f, centerY = 345f, armLength = 46f, angularSpeed = 1.8f, color = Color(0xFFFF007F)),
            // Rotating laser spinner 2 in factory upper
            Hazard(type = HazardType.ROTATING_SPINNER, centerX = 1250f, centerY = 260f, armLength = 50f, angularSpeed = 2.2f, color = Color(0xFFFF007F)),
            // Pulsing vertical laser beam near exit
            Hazard(type = HazardType.LASER_VERTICAL, x = 1830f, y = 260f, width = 8f, height = 150f, cyclePeriod = 2.5f, color = Color(0xFFFF007F)),
            Hazard(type = HazardType.VOID_ZONE, x = 0f, y = 650f, width = 2300f, height = 30f)
        )

        val crystals = listOf(
            Crystal(id = 23, x = 140f, y = 490f),
            Crystal(id = 24, x = 310f, y = 430f),
            Crystal(id = 25, x = 470f, y = 390f),
            Crystal(id = 26, x = 630f, y = 350f),
            Crystal(id = 27, x = 810f, y = 320f),
            Crystal(id = 28, x = 1000f, y = 360f),
            Crystal(id = 29, x = 1210f, y = 310f),
            Crystal(id = 30, x = 1440f, y = 220f),
            Crystal(id = 31, x = 1510f, y = 220f),
            Crystal(id = 32, x = 1630f, y = 260f),
            Crystal(id = 33, x = 1770f, y = 310f),
            Crystal(id = 34, x = 1900f, y = 360f),
            // Secret crystals inside spinner danger zone
            Crystal(id = 35, x = 500f, y = 320f, isSecret = true),
            Crystal(id = 36, x = 1250f, y = 230f, isSecret = true)
        )

        val powerUps = listOf(
            PowerUpItem(id = 4, x = 810f, y = 340f, type = PowerUpType.LIGHT_BURST),
            PowerUpItem(id = 5, x = 1440f, y = 240f, type = PowerUpType.CRYSTAL_MAGNET)
        )

        return LevelDef(
            id = 3,
            title = "NEON FACTORY",
            subtitle = "Sector 3: Cyber Foundry",
            description = "Navigate through pulsing energy barriers and spinning laser hazard cores. Time your hops with machine precision.",
            worldWidth = 2300f,
            worldHeight = 700f,
            playerStartX = 60f,
            playerStartY = 490f,
            targetTimeSeconds = 40f,
            platforms = platforms,
            hazards = hazards,
            crystals = crystals,
            powerUps = powerUps,
            portal = ExitPortal(x = 2130f, y = 420f),
            themeColor = theme
        )
    }

    // ==========================================
    // LEVEL 4: VOID GARDENS
    // ==========================================
    private fun createLevel4(): LevelDef {
        val theme = Color(0xFF9D00FF) // Neon Violet
        val platforms = listOf(
            // Base garden starting ground
            Platform(x = 0f, y = 620f, width = 200f, height = 35f, type = PlatformType.SOLID, color = theme),
            // Low route platforms
            Platform(x = 240f, y = 570f, width = 110f, height = 22f, type = PlatformType.SOLID, color = theme),
            Platform(x = 390f, y = 530f, width = 120f, height = 22f, type = PlatformType.SOLID, color = theme),
            // Branch split: High crystal pathway
            Platform(x = 420f, y = 410f, width = 100f, height = 20f, type = PlatformType.ONE_WAY, color = theme),
            Platform(x = 560f, y = 330f, width = 110f, height = 20f, type = PlatformType.ONE_WAY, color = theme),
            Platform(x = 710f, y = 260f, width = 130f, height = 20f, type = PlatformType.ONE_WAY, color = theme),
            // High secret spire
            Platform(x = 880f, y = 190f, width = 140f, height = 22f, type = PlatformType.ONE_WAY, color = theme),
            // Low main route continued
            Platform(x = 550f, y = 500f, width = 130f, height = 22f, type = PlatformType.SOLID, color = theme),
            Platform(
                x = 720f, y = 460f, width = 100f, height = 22f,
                type = PlatformType.MOVING_X, moveMin = 720f, moveMax = 940f, moveSpeed = 70f,
                color = theme
            ),
            // Convergence plateau
            Platform(x = 1080f, y = 360f, width = 180f, height = 24f, type = PlatformType.SOLID, color = theme),
            // Garden canopy stepping stones
            Platform(
                x = 1300f, y = 320f, width = 95f, height = 22f,
                type = PlatformType.MOVING_Y, moveMin = 200f, moveMax = 380f, moveSpeed = 65f,
                color = theme
            ),
            Platform(x = 1440f, y = 230f, width = 130f, height = 22f, type = PlatformType.ONE_WAY, color = theme),
            // Timed energy bridge
            Platform(
                x = 1610f, y = 270f, width = 120f, height = 22f,
                type = PlatformType.TIMED_ENERGY, activeDuration = 2.0f, inactiveDuration = 1.4f,
                color = theme
            ),
            Platform(x = 1770f, y = 330f, width = 110f, height = 22f, type = PlatformType.SOLID, color = theme),
            Platform(x = 1920f, y = 390f, width = 110f, height = 22f, type = PlatformType.SOLID, color = theme),
            // Portal sanctuary
            Platform(x = 2070f, y = 450f, width = 250f, height = 40f, type = PlatformType.SOLID, color = theme)
        )

        val hazards = listOf(
            Hazard(type = HazardType.SPIKE, x = 200f, y = 670f, width = 1900f, height = 20f, color = Color(0xFFFF007F)),
            // Rotating hazard defending the convergence plateau
            Hazard(type = HazardType.ROTATING_SPINNER, centerX = 1000f, centerY = 410f, armLength = 55f, angularSpeed = 1.9f, color = Color(0xFFFF007F)),
            // Hazard laser guarding the high secret route
            Hazard(type = HazardType.LASER_VERTICAL, x = 860f, y = 140f, width = 8f, height = 120f, cyclePeriod = 2.8f, color = Color(0xFFFF007F)),
            Hazard(type = HazardType.VOID_ZONE, x = 0f, y = 740f, width = 2400f, height = 30f)
        )

        val crystals = listOf(
            Crystal(id = 37, x = 120f, y = 570f),
            Crystal(id = 38, x = 290f, y = 520f),
            Crystal(id = 39, x = 440f, y = 480f),
            Crystal(id = 40, x = 610f, y = 450f),
            Crystal(id = 41, x = 830f, y = 410f),
            Crystal(id = 42, x = 1170f, y = 310f),
            Crystal(id = 43, x = 1350f, y = 260f),
            Crystal(id = 44, x = 1500f, y = 180f),
            Crystal(id = 45, x = 1670f, y = 220f),
            Crystal(id = 46, x = 1820f, y = 280f),
            Crystal(id = 47, x = 1970f, y = 340f),
            // Secret high branch crystals (rich reward for skilled platforming!)
            Crystal(id = 48, x = 470f, y = 360f, isSecret = true),
            Crystal(id = 49, x = 610f, y = 280f, isSecret = true),
            Crystal(id = 50, x = 770f, y = 210f, isSecret = true),
            Crystal(id = 51, x = 930f, y = 140f, isSecret = true),
            Crystal(id = 52, x = 970f, y = 140f, isSecret = true)
        )

        val powerUps = listOf(
            PowerUpItem(id = 6, x = 420f, y = 540f, type = PowerUpType.CRYSTAL_MAGNET),
            PowerUpItem(id = 7, x = 950f, y = 150f, type = PowerUpType.LIGHT_BURST),
            PowerUpItem(id = 8, x = 1170f, y = 320f, type = PowerUpType.SAFE_LANDING)
        )

        return LevelDef(
            id = 4,
            title = "VOID GARDENS",
            subtitle = "Sector 4: Ethereal Spires",
            description = "Explore branching pathways among luminescent floating gardens. Discover the secret high-altitude crystal route.",
            worldWidth = 2400f,
            worldHeight = 780f,
            playerStartX = 60f,
            playerStartY = 570f,
            targetTimeSeconds = 45f,
            platforms = platforms,
            hazards = hazards,
            crystals = crystals,
            powerUps = powerUps,
            portal = ExitPortal(x = 2190f, y = 400f),
            themeColor = theme
        )
    }

    // ==========================================
    // LEVEL 5: STAR CORE
    // ==========================================
    private fun createLevel5(): LevelDef {
        val theme = Color(0xFFFFD600) // Solar Gold
        val platforms = listOf(
            // Reactor entry pad
            Platform(x = 0f, y = 600f, width = 200f, height = 35f, type = PlatformType.SOLID, color = theme),
            // Moving energy ferry 1
            Platform(
                x = 240f, y = 540f, width = 95f, height = 22f,
                type = PlatformType.MOVING_X, moveMin = 240f, moveMax = 440f, moveSpeed = 85f,
                color = theme
            ),
            // Plasma reactor perch
            Platform(x = 480f, y = 490f, width = 110f, height = 22f, type = PlatformType.SOLID, color = theme),
            // Timed energy bridge 1
            Platform(
                x = 630f, y = 440f, width = 100f, height = 22f,
                type = PlatformType.TIMED_ENERGY, activeDuration = 1.7f, inactiveDuration = 1.3f,
                color = theme
            ),
            // Reactor column 1
            Platform(x = 770f, y = 400f, width = 130f, height = 24f, type = PlatformType.SOLID, color = theme),
            // Vertical plasma elevator
            Platform(
                x = 940f, y = 380f, width = 95f, height = 22f,
                type = PlatformType.MOVING_Y, moveMin = 180f, moveMax = 420f, moveSpeed = 80f,
                color = theme
            ),
            // Upper supercharged conductor
            Platform(x = 1070f, y = 180f, width = 200f, height = 24f, type = PlatformType.ONE_WAY, color = theme),
            // Fast gliding shuttle
            Platform(
                x = 1310f, y = 260f, width = 95f, height = 22f,
                type = PlatformType.MOVING_X, moveMin = 1310f, moveMax = 1570f, moveSpeed = 100f,
                color = theme
            ),
            // Core central pedestal
            Platform(x = 1620f, y = 320f, width = 150f, height = 24f, type = PlatformType.SOLID, color = theme),
            // Dual timed energy run
            Platform(
                x = 1810f, y = 300f, width = 90f, height = 22f,
                type = PlatformType.TIMED_ENERGY, activeDuration = 1.6f, inactiveDuration = 1.2f,
                color = theme
            ),
            Platform(
                x = 1940f, y = 350f, width = 90f, height = 22f,
                type = PlatformType.TIMED_ENERGY, activeDuration = 1.6f, inactiveDuration = 1.2f,
                color = theme
            ),
            // Final reactor gateway
            Platform(x = 2070f, y = 410f, width = 100f, height = 22f, type = PlatformType.SOLID, color = theme),
            // Grand Star Core Apex
            Platform(x = 2210f, y = 460f, width = 280f, height = 45f, type = PlatformType.SOLID, color = theme)
        )

        val hazards = listOf(
            Hazard(type = HazardType.SPIKE, x = 200f, y = 650f, width = 2100f, height = 20f, color = Color(0xFFFF007F)),
            // Reactor Core Dual Rotating Spinners with balanced clearance
            Hazard(type = HazardType.ROTATING_SPINNER, centerX = 630f, centerY = 350f, armLength = 48f, angularSpeed = 2.4f, color = Color(0xFFFF007F)),
            Hazard(type = HazardType.ROTATING_SPINNER, centerX = 1450f, centerY = 190f, armLength = 48f, angularSpeed = 2.5f, color = Color(0xFFFF007F)),
            Hazard(type = HazardType.ROTATING_SPINNER, centerX = 1750f, centerY = 340f, armLength = 50f, angularSpeed = -2.2f, color = Color(0xFFFF007F)),
            // Pulsing Core Laser
            Hazard(type = HazardType.LASER_VERTICAL, x = 1200f, y = 90f, width = 8f, height = 150f, cyclePeriod = 2.2f, color = Color(0xFFFF007F)),
            Hazard(type = HazardType.VOID_ZONE, x = 0f, y = 720f, width = 2500f, height = 30f)
        )

        val crystals = listOf(
            Crystal(id = 53, x = 110f, y = 550f),
            Crystal(id = 54, x = 330f, y = 490f),
            Crystal(id = 55, x = 530f, y = 440f),
            Crystal(id = 56, x = 680f, y = 390f),
            Crystal(id = 57, x = 830f, y = 350f),
            Crystal(id = 58, x = 990f, y = 250f),
            Crystal(id = 59, x = 1140f, y = 130f),
            Crystal(id = 60, x = 1230f, y = 130f),
            Crystal(id = 61, x = 1430f, y = 200f),
            Crystal(id = 62, x = 1690f, y = 260f),
            Crystal(id = 63, x = 1850f, y = 240f),
            Crystal(id = 64, x = 1980f, y = 290f),
            Crystal(id = 65, x = 2120f, y = 350f),
            // Secret core crystals
            Crystal(id = 66, x = 630f, y = 310f, isSecret = true),
            Crystal(id = 67, x = 1450f, y = 170f, isSecret = true),
            Crystal(id = 68, x = 1750f, y = 270f, isSecret = true)
        )

        val powerUps = listOf(
            PowerUpItem(id = 9, x = 530f, y = 450f, type = PowerUpType.SAFE_LANDING),
            PowerUpItem(id = 10, x = 1150f, y = 140f, type = PowerUpType.LIGHT_BURST),
            PowerUpItem(id = 11, x = 1690f, y = 280f, type = PowerUpType.CRYSTAL_MAGNET)
        )

        return LevelDef(
            id = 5,
            title = "STAR CORE",
            subtitle = "Sector 5: Hyper-Reactor Apex",
            description = "The ultimate star circuit test! Brave synchronized plasma spinners, rapid moving shuttles, and claim the Master Circuit Portal.",
            worldWidth = 2500f,
            worldHeight = 750f,
            playerStartX = 60f,
            playerStartY = 550f,
            targetTimeSeconds = 50f,
            platforms = platforms,
            hazards = hazards,
            crystals = crystals,
            powerUps = powerUps,
            portal = ExitPortal(x = 2340f, y = 410f),
            themeColor = theme
        )
    }

    // ==========================================
    // ENDLESS MODE: HYPER CIRCUIT
    // ==========================================
    fun createEndlessLevel(): LevelDef {
        val theme = Color(0xFF00E5FF) // Cyber Neon Cyan
        val platforms = listOf(
            Platform(x = 0f, y = 500f, width = 360f, height = 40f, type = PlatformType.SOLID, color = theme),
            Platform(x = 400f, y = 450f, width = 130f, height = 24f, type = PlatformType.SOLID, color = theme),
            Platform(x = 570f, y = 400f, width = 120f, height = 24f, type = PlatformType.ONE_WAY, color = theme),
            Platform(x = 730f, y = 350f, width = 110f, height = 24f, type = PlatformType.SOLID, color = theme),
            Platform(
                x = 880f, y = 400f, width = 140f, height = 24f,
                type = PlatformType.MOVING_X, moveMin = 880f, moveMax = 1060f, moveSpeed = 70f,
                color = theme
            ),
            Platform(x = 1120f, y = 350f, width = 120f, height = 24f, type = PlatformType.SOLID, color = theme),
            Platform(x = 1280f, y = 300f, width = 130f, height = 24f, type = PlatformType.ONE_WAY, color = theme),
            Platform(x = 1450f, y = 360f, width = 140f, height = 24f, type = PlatformType.SOLID, color = theme)
        )

        val hazards = listOf(
            Hazard(type = HazardType.SPIKE, x = 360f, y = 540f, width = 40f, height = 20f, color = Color(0xFFFF007F)),
            Hazard(type = HazardType.VOID_ZONE, x = 0f, y = 620f, width = 999999f, height = 30f)
        )

        val crystals = listOf(
            Crystal(id = 101, x = 180f, y = 460f),
            Crystal(id = 102, x = 460f, y = 400f),
            Crystal(id = 103, x = 630f, y = 350f),
            Crystal(id = 104, x = 790f, y = 300f),
            Crystal(id = 105, x = 970f, y = 340f),
            Crystal(id = 106, x = 1180f, y = 300f),
            Crystal(id = 107, x = 1340f, y = 250f)
        )

        val powerUps = listOf(
            PowerUpItem(id = 101, x = 790f, y = 310f, type = PowerUpType.CRYSTAL_MAGNET)
        )

        return LevelDef(
            id = ENDLESS_LEVEL_ID,
            title = "HYPER CIRCUIT",
            subtitle = "Infinite Space Corridor",
            description = "Test your reflexes in an endless procedurally generated cyber circuit! Collect crystals, sustain combos, and survive as long as you can.",
            worldWidth = 2400f,
            worldHeight = 670f,
            playerStartX = 60f,
            playerStartY = 450f,
            targetTimeSeconds = 999f,
            platforms = platforms,
            hazards = hazards,
            crystals = crystals,
            powerUps = powerUps,
            portal = ExitPortal(x = 999999f, y = 999999f), // Endless run; no exit portal
            themeColor = theme
        )
    }
}
