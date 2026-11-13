# ⚡ NEON HOP: STAR CIRCUIT

![Platform](https://img.shields.io/badge/Platform-Android-00F0FF?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-FF007F?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Canvas%202D-FFD700?style=for-the-badge&logo=jetpackcompose&logoColor=black)
![Min SDK](https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0)-00FF9D?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)

**Neon Hop: Star Circuit** is a high-octane 2D cyberpunk sci-fi platformer built natively for Android using **Kotlin** and **Jetpack Compose Canvas**. Pilot **NOVA**, an agile cyber-bot, across glowing orbital sectors, dodge rotating laser hazards, ride moving platforms, chain crystal combos, and survive the infinite **Hyper Circuit** endless mode.

---

## ✨ Key Features

- **🚀 60/120 FPS Custom 2D Physics Engine (`GameEngine.kt`)**
  - Frame-independent delta-time stepping with sub-stepping to prevent high-velocity collision tunneling.
  - Responsive platforming mechanics including **Coyote Time**, **Jump Buffering**, **Variable Jump Height**, **Double Jump**, and directional **Cyber Dash** with cooldown management.
  - Dynamic platform types: **Solid**, **One-Way Jump-Through**, **Moving Platforms** (with automatic player carrier attachment), **Crumbling Platforms**, and **Bounce Jump Pads**.
- **🌌 5 Handcrafted Campaign Sectors + Endless Mode (`Levels.kt`)**
  - **Sector 1 — Neon Awakening:** Orbital Launchpad introductory circuit.
  - **Sector 2 — Broken Orbit:** Asteroid Drift with moving platforms and wide chasms.
  - **Sector 3 — Laser Grid:** Defense Matrix with sweeping and rotating laser hazards.
  - **Sector 4 — Zero-G Spire:** Vertical Ascent with precision bounce pads and crumbling bridges.
  - **Sector 5 — Star Core:** Singularity Reactor finale combining all hazards and high-speed platforming.
  - **Endless Mode — Hyper Circuit:** Procedurally generated infinite corridors with escalating difficulty and persistent high-score tracking.
- **🎨 Custom Hardware-Accelerated Canvas Renderer (`GameRenderer.kt`)**
  - Multi-layered cosmic parallax starfields, glowing neon geometry, dynamic screen-shake feedback, thruster particle trails, landing dust puffs, and animated exit portals.
- **🤖 Customizable Robot Hangar (`RobotCollectionScreen.kt`)**
  - Unlock and equip distinct NOVA robot chassis skins using collected Star Crystals, each with custom primary, secondary, visor, and particle trail colors.
- **🔊 Zero-Asset Procedural Audio & Haptics (`SoundSystem.kt`, `HapticSystem.kt`)**
  - Real-time synthesized PCM sound effects via Android `AudioTrack` (jump sweeps, double-jump chimes, crystal pings, power-up chords, hazard alarms, and victory fanfares) paired with crisp tactile vibration feedback.
- **🎮 Multi-Touch & Hardware Keyboard Controls (`GameScreen.kt`)**
  - Persistent pointer-ID tracking on touch buttons so sliding thumbs never drop input during intense platforming, plus full keyboard support for emulators, foldables, and ChromeOS devices.

---

## 🕹️ Controls

### 📱 On-Screen Touch Controls
| Button | Action |
| :--- | :--- |
| **◀ / ▶ Directional Pad** | Move NOVA Left / Right |
| **⏫ Jump Button** | Jump (Tap in mid-air for **Double Jump**) |
| **⚡ Dash Button** | High-speed horizontal **Cyber Dash** (0.9s cooldown) |
| **⏸ Pause Button** | Pause / Resume current sector |

### ⌨️ Hardware Keyboard Controls (Emulator / Desktop / Chromebook)
| Key | Action |
| :--- | :--- |
| `A` / `D` or `Left` / `Right` Arrow | Move Left / Right |
| `W` / `Up` Arrow / `Spacebar` | Jump / Double Jump |
| `Shift` / `K` / `X` | Cyber Dash |
| `Esc` / `P` | Pause / Resume Game |

---

## 📂 Project Architecture & Folder Structure

```text
NeonHop-StarCircuit/
├── .github/
│   └── workflows/
│       └── android-ci.yml                  # GitHub Actions CI for unit tests & APK build
├── app/
│   ├── build.gradle.kts                    # App module Gradle configuration
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml         # App manifest & hardware-accelerated Activity
│       │   ├── java/com/example/
│       │   │   ├── MainActivity.kt         # Entry point & Compose screen router
│       │   │   ├── audio/
│       │   │   │   ├── HapticSystem.kt     # Tactile vibration controller
│       │   │   │   └── SoundSystem.kt      # Real-time PCM AudioTrack synthesizer
│       │   │   ├── data/
│       │   │   │   └── GamePreferences.kt  # Persistent storage (stars, scores, skins, settings)
│       │   │   ├── game/
│       │   │   │   ├── GameEngine.kt       # Core 2D physics, collision, & procedural generator
│       │   │   │   ├── GameRenderer.kt     # 2D Canvas rendering & parallax visual effects
│       │   │   │   ├── Levels.kt           # 5 campaign level definitions + Endless Mode setup
│       │   │   │   └── model/
│       │   │   │       └── GameModels.kt   # Entities: Player, Platform, Hazard, Crystal, Skin
│       │   │   └── ui/
│       │   │       ├── GameScreen.kt       # Gameplay loop, HUD, dialogs, & touch controller
│       │   │       ├── GameViewModel.kt    # MVVM state management & progression coordinator
│       │   │       ├── HowToPlayScreen.kt  # Flight manual & mechanics guide
│       │   │       ├── LevelSelectScreen.kt# Sector selection & Endless Mode launcher
│       │   │       ├── MainMenuScreen.kt   # Animated main menu & personal best stats
│       │   │       ├── RobotCollectionScreen.kt # NOVA chassis customization shop
│       │   │       ├── SettingsScreen.kt   # Audio, haptics, and data reset controls
│       │   │       ├── SplashScreen.kt     # Animated cyber-boot splash screen
│       │   │       └── theme/              # Neon cyberpunk color palette & typography
│       │   └── res/                        # Adaptive launcher icons, strings, & XML resources
│       └── test/
│           └── java/com/example/
│               └── ExampleRobolectricTest.kt # 20 Robolectric JVM unit & physics tests
├── gradle/
│   └── libs.versions.toml                  # Centralized Gradle Version Catalog
├── NeonHop-StarCircuit.apk                 # Pre-built installable Debug APK
├── build.gradle.kts                        # Root Gradle configuration
├── settings.gradle.kts                     # Project settings
├── CONTRIBUTING.md                         # Contribution guidelines
├── CHANGELOG.md                            # Release notes & version history
├── LICENSE                                 # MIT License
└── README.md                               # Project documentation
```

---

## 🛠️ Getting Started & Building from Source

### Prerequisites
- **Android Studio** Ladybug (2024.2.1) or newer
- **JDK 11** or **JDK 17+**
- **Android SDK 36** (Min SDK 24)

### 1. Clone the Repository
```bash
git clone https://github.com/vanshpagariya/neon-hop-star-circuit.git
cd neon-hop-star-circuit
```

### 2. Build the Debug APK
Using Gradle (or the Gradle wrapper if initialized locally):
```bash
gradle assembleDebug
```
The compiled APK will be generated at:
```text
app/build/outputs/apk/debug/app-debug.apk
```
*(A pre-built copy is also available directly in the repository root as `NeonHop-StarCircuit.apk`.)*

### 3. Run the Automated Unit & Physics Tests
The project includes a suite of **20 Robolectric unit tests** verifying movement physics, coyote time, jump buffering, platform collisions, shield absorption, endless procedural generation, and level progression:
```bash
gradle :app:testDebugUnitTest
```

---

## 📲 Installing the APK on Android

1. Download **`NeonHop-StarCircuit.apk`** from the repository root or the **Releases** page on GitHub onto your Android phone.
2. Open your device's **Files** or **Downloads** app and tap `NeonHop-StarCircuit.apk`.
3. If prompted with *"Install unknown apps"*, enable **Allow from this source**.
4. Tap **Install** and launch **Neon Hop**!

---

## 📄 License

Distributed under the **MIT License**. See [`LICENSE`](LICENSE) for full details.
