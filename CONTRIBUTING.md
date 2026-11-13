# Contributing to Neon Hop: Star Circuit

Thank you for your interest in contributing to **Neon Hop: Star Circuit**!

## Development Workflow

1. **Fork the repository** and create your feature branch from `main`:
   ```bash
   git checkout -b feature/my-new-feature
   ```
2. **Code Style & Architecture**:
   - Write all code in **Kotlin** following official Android & Jetpack Compose conventions.
   - Keep physics and collision logic inside `com.example.game.GameEngine` decoupled from Compose UI state.
   - Keep Canvas drawing operations inside `com.example.game.GameRenderer`.
   - Maintain 60/120 FPS performance by avoiding object allocations inside per-frame `render()` and `update()` loops where possible.
3. **Run Unit Tests**:
   Ensure all Robolectric unit and physics tests pass before submitting a Pull Request:
   ```bash
   gradle :app:testDebugUnitTest
   ```
4. **Verify Debug APK Compilation**:
   ```bash
   gradle assembleDebug
   ```
5. **Open a Pull Request** with a clear summary of your changes.
