# Changelog

All notable changes to **Neon Hop: Star Circuit** are documented in this file.

## [1.0.0] - 2026-11-13

### Added
- **Core 2D Physics Engine (`GameEngine`)**:
  - 60/120 FPS frame-driven game loop with sub-stepping collision detection.
  - Coyote time, jump buffering, variable jump height, double jump, and directional cyber dash.
  - Solid, one-way, moving, crumbling, and bounce pad platform types.
  - Spike hazards, rotating laser spinners, and timed laser beams.
  - Collectible star crystals with combo multiplier chains, magnetic attraction, and secret crystals.
  - Power-up items: **Safe Landing** (hazard shield), **Magnet Pulse**, and **Light Burst** speed boost.
- **Game Modes & Sectors (`Levels`)**:
  - 5 campaign sectors: *Neon Awakening*, *Broken Orbit*, *Laser Grid*, *Zero-G Spire*, and *Star Core*.
  - *Hyper Circuit* Endless Survival mode with procedural chunk generation and high-score tracking.
- **Visuals & Audio (`GameRenderer`, `SoundSystem`, `HapticSystem`)**:
  - Hardware-accelerated 2D Canvas rendering with cosmic parallax starfields, particle bursts, and screen shake.
  - Real-time synthesized PCM sound effects via Android `AudioTrack` and haptic feedback.
  - Robot Hangar with unlockable NOVA skins and custom color schemes.
- **Controls & QA Polish (`GameScreen`, `MainActivity`)**:
  - Multi-touch on-screen controller with persistent pointer-ID tracking and `rememberUpdatedState` callback binding across level restarts and transitions.
  - Full hardware keyboard support (`WASD` / Arrow keys, `Space`, `Shift`, `Esc`).
  - 20 automated Robolectric unit & physics tests.
