# Changelog

All notable changes to **EQ-Math** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Automated GitHub Actions CI/CD workflows for unit/Robolectric tests, Lint, Debug/Release APK builds, GitHub Pages deployment, and repository metadata syncing.
- Interactive GitHub Pages showcase site (`docs/`) with live browser mini-quiz demo and 2×2 animated GIF gallery.
- Community governance files (`LICENSE`, `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`, `SECURITY.md`, issue/PR templates, Dependabot).

## [1.0.0] - 2026-11-23

### Added
- **EQ-Math Android App** ("Belajar Jadi Mudah!") built with Kotlin, Jetpack Compose, and Material Design 3.
- **Adventure Modes**: Multiplication (`×`), Division (`÷`), Mixed Challenge (`×÷`), and Daily Mission (`Misi Harian`) with `+30 BONUS XP`.
- **Interactive Quiz Engine**: Relaxed mode with **Bantuan Visual** (dot-group visualizers), Timed mode (`Kilat Waktu`), dual input modes (Multiple Choice 2×2 grid & Numeric Keypad), live combo counter, and encouraging Indonesian feedback.
- **Gamification & Rewards**: 8 learner levels (`Pemula Ceria` to `Legenda EQ-Math`), 5 star milestones (`Bintang Perunggu` to `Mahkota Galaksi`), 12 unlockable achievement badges, and daily streak tracking.
- **Smart Table Guide & Mastery Map**: Interactive 1–12 multiplication/division reference tables with per-table 3-star mastery tracking.
- **Cloud Sync & Guest Mode**: Google Sign-In via Jetpack Credential Manager + Firebase Auth & Firestore (`MathProgressRepository`) plus zero-friction offline Guest Mode (`LocalGuestMathRepository`).
- **Accessibility & Adaptive Layout**: Official EQ-Math 4-quadrant design tokens tuned to WCAG 2.1 AA/AAA in Light & Dark modes, TalkBack live regions, 48dp minimum touch targets, and adaptive `NavigationBar` / `NavigationRail`.
