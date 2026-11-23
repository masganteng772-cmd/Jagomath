# Contributing to EQ-Math

Thank you for helping make **EQ-Math — Belajar Jadi Mudah!** even better for elementary school learners!

## Development Workflow

1. **Fork & Clone** the repository:
   ```bash
   git clone https://github.com/<your-username>/EQ-Math.git
   cd EQ-Math
   ```
2. **Create a feature branch** from `main`:
   ```bash
   git checkout -b feat/your-feature-name
   ```
3. **Prepare local signing & environment**:
   - Copy `.env.example` to `.env` if needed.
   - Decode `debug.keystore.base64` to `debug.keystore` for local debug builds:
     ```bash
     base64 --decode debug.keystore.base64 > debug.keystore
     ```
4. **Build & Run Tests**:
   ```bash
   gradle :app:testDebugUnitTest
   gradle :app:lintDebug
   gradle :app:assembleDebug
   ```
5. **Commit using Conventional Commits**:
   - `feat(quiz): add timed bonus combo indicator`
   - `fix(accessibility): increase touch target to 48dp`
   - `docs(readme): refresh 60fps showcase gifs`
   - `test(rewards): verify galaxy star milestone`

## Design & Accessibility Standards

- **Material 3 & EQ-Math Design Tokens**: Use `MaterialTheme.colorScheme` and `EqTheme.colors` from `Color.kt` rather than hardcoded hex values.
- **WCAG 2.1 AA/AAA**: Verify light and dark mode contrast ratios on all new UI components.
- **Touch Targets & Semantics**: Minimum `48.dp` touch target size, meaningful `contentDescription` / `semantics`, and `Modifier.testTag(...)` on interactive elements.
- **Keep `debug.keystore` and `.env` uncommitted**: Never commit secrets or private keystores.

## Regenerating Showcase GIFs

If you update the UI and want to refresh the 2×2 showcase GIFs used in `README.md` and GitHub Pages:

```bash
python3 scripts/generate_showcase_gifs.py
```

## Pull Request Checklist

- [ ] Unit & Robolectric tests pass (`gradle :app:testDebugUnitTest`)
- [ ] Android Lint reports no new errors (`gradle :app:lintDebug`)
- [ ] UI verified in both Light & Dark themes and responsive widths (phone & tablet)
- [ ] `CHANGELOG.md` updated under `[Unreleased]` for user-visible changes
