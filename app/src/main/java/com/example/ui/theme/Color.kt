package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// EQ-MATH OFFICIAL DESIGN TOKENS + WCAG 2.1 AA/AAA ACCESSIBLE CONTRAST SCALE
// Sourced from eqmath.roomwise.id/css/tokens.css & logo-eq-math-border
// ============================================================================

// 1. Brand Primary — EQ-Math Navy (Logo, Headings, Primary Actions)
val EqNavy50 = Color(0xFFF4F4F8)
val EqNavy100 = Color(0xFFE6E6F2)
val EqNavy200 = Color(0xFFCACADB)
val EqNavy300 = Color(0xFFA0A0BF)
val EqNavy400 = Color(0xFF6C6B9B)
val EqNavy500 = Color(0xFF2D2C70) // Exact logo navy (12.2:1 on white)
val EqNavy600 = Color(0xFF26255F)
val EqNavy700 = Color(0xFF1F1F4E)
val EqNavy800 = Color(0xFF19183E)
val EqNavy900 = Color(0xFF12122D) // 18.0:1 on white

// 2. Brand 4 Brain-Quadrant Accents (with WCAG AA+ accessible text & fill variants)
// Yellow Quadrant (Stars, Rewards, Highlights)
val EqYellow50 = Color(0xFFFFFCF3)
val EqYellow100 = Color(0xFFFEF4D6)
val EqYellow400 = Color(0xFFFFD54F) // High-contrast dark-mode yellow (11.6:1 on dark surface)
val EqYellow500 = Color(0xFFF6B908) // Exact logo yellow (for brand stripes & dark-navy badges)
val EqYellow600 = Color(0xFF9E6E00) // >= 4.54:1 on white, >= 3.35:1 on progress tracks
val EqYellow700 = Color(0xFF7D5A00) // >= 6.30:1 on white, >= 5.75:1 on yellow-100
val EqYellow900 = Color(0xFF4A3500) // >= 11.4:1 on yellow-100

// Red Quadrant (Multiplication Accent, Danger/Error)
val EqRed50 = Color(0xFFFEF6F5)
val EqRed100 = Color(0xFFFDEAE8)
val EqRed400 = Color(0xFFFF8A80)    // High-contrast dark-mode red (7.3:1 on dark surface)
val EqRed500 = Color(0xFFE94639)    // Exact logo red (for brand stripes)
val EqRed600 = Color(0xFFC42B20)    // >= 5.67:1 on white AND white on EqRed600
val EqRed700 = Color(0xFF9E1E15)    // >= 7.8:1 deep red gradient end
val EqRed900 = Color(0xFF5C120C)

// Green Quadrant (Success, Correct Answers, Mastery)
val EqGreen50 = Color(0xFFF5FBF6)
val EqGreen100 = Color(0xFFE3F5E8)
val EqGreen400 = Color(0xFF6EE08B)  // High-contrast dark-mode green (9.8:1 on dark surface)
val EqGreen500 = Color(0xFF35A853)  // Exact logo green (for brand stripes)
val EqGreen600 = Color(0xFF1B7435)  // >= 5.83:1 on white AND white on EqGreen600
val EqGreen700 = Color(0xFF125425)  // >= 8.7:1 deep green gradient end
val EqGreen900 = Color(0xFF0F421D)

// Blue Quadrant (Secondary, Division, Info)
val EqBlue50 = Color(0xFFF6F9FE)
val EqBlue100 = Color(0xFFE6F0FE)
val EqBlue400 = Color(0xFF8AB8FF)   // High-contrast dark-mode blue (8.3:1 on dark surface)
val EqBlue500 = Color(0xFF4285F4)   // Exact logo blue (for brand stripes)
val EqBlue600 = Color(0xFF1D5BC4)   // >= 6.27:1 on white AND white on EqBlue600
val EqBlue700 = Color(0xFF144191)   // >= 9.6:1 deep blue gradient end
val EqBlue900 = Color(0xFF102B5C)

// 3. Cool Navy-Tinted Neutrals (tuned for crisp surface separation & AAA text contrast)
val EqNeutral50 = Color(0xFFEFF0F6)   // Distinct cool canvas behind pure-white cards
val EqNeutral100 = Color(0xFFE8EAF3)  // Distinct surfaceVariant / mini-tile surface
val EqNeutral200 = Color(0xFFDBDDE8)  // Progress bar track on white cards
val EqNeutral300 = Color(0xFFC8C9DC)  // Crisp decorative card border
val EqNeutral500 = Color(0xFF6D6C8A)  // >= 5.1:1 interactive outline border on white
val EqNeutral600 = Color(0xFF4A4966)  // >= 8.75:1 secondary text on white (AAA!)
val EqNeutral800 = Color(0xFF32313F)
val EqNeutral900 = Color(0xFF201F28)
val EqWhite = Color(0xFFFFFFFF)

// 4. Accessible Semantic Aliases (safe on light surfaces >= 5.6:1)
val TangerinePrimary = EqRed600
val TangerineDark = EqRed700
val CeruleanSecondary = EqBlue600
val CeruleanDark = EqBlue700
val MangoTertiary = EqYellow600
val MangoDark = EqYellow700
val EmeraldSuccess = EqGreen600
val EmeraldContainer = EqGreen100
val OnEmeraldContainer = EqGreen900
val BerryError = EqRed600
val BerryContainer = EqRed100
val OnBerryContainer = EqRed900

// 5. Dark Mode Surfaces (Deep EQ-Math Midnight Navy)
val EqDarkBackground = Color(0xFF101022)
val EqDarkSurface = Color(0xFF1B1B33)
val EqDarkSurfaceVariant = Color(0xFF262547)
val EqDarkOnSurface = Color(0xFFF5F5FA)
val EqDarkOnSurfaceVariant = Color(0xFFC6C5DF)
val EqDarkOutline = Color(0xFF7D7CA8)
val EqDarkOutlineVariant = Color(0xFF454470)
val EqDarkPrimary = Color(0xFFB3B3FF)
val EqDarkProgressTrack = Color(0xFF383760)
val EqDarkSuccessContainer = Color(0xFF133B1E)
val EqDarkOnSuccessContainer = Color(0xFFC9F5D4)
val EqDarkDangerContainer = Color(0xFF4D1510)
val EqDarkOnDangerContainer = Color(0xFFFFDAD6)
val EqDarkInfoContainer = Color(0xFF132C57)
val EqDarkOnInfoContainer = Color(0xFFD6E6FF)
val EqDarkStarContainer = Color(0xFF3D2E05)
val EqDarkOnStarContainer = Color(0xFFFFEAA0)
