package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class EqSemanticColors(
    val brandNavyStart: Color,
    val brandNavyEnd: Color,
    val onBrandNavy: Color,
    val onBrandNavyMuted: Color,
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val danger: Color,
    val onDanger: Color,
    val dangerContainer: Color,
    val onDangerContainer: Color,
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,
    val starAccent: Color,
    val starFill: Color,
    val starContainer: Color,
    val onStarContainer: Color,
    val progressTrack: Color,
    val cardBorder: Color,
    val subtleSurfaceTint: Color,
    val modeMulStart: Color,
    val modeMulEnd: Color,
    val modeDivStart: Color,
    val modeDivEnd: Color,
    val modeMixStart: Color,
    val modeMixEnd: Color,
    val logoPlateBackground: Color,
    val logoPlateBorder: Color
)

private val LightEqSemanticColors = EqSemanticColors(
    brandNavyStart = EqNavy500,
    brandNavyEnd = EqNavy700,
    onBrandNavy = EqWhite,
    onBrandNavyMuted = Color(0xFFEBECF8),
    success = EqGreen600,
    onSuccess = EqWhite,
    successContainer = EqGreen100,
    onSuccessContainer = EqGreen900,
    danger = EqRed600,
    onDanger = EqWhite,
    dangerContainer = EqRed100,
    onDangerContainer = EqRed900,
    info = EqBlue600,
    onInfo = EqWhite,
    infoContainer = EqBlue100,
    onInfoContainer = EqBlue900,
    starAccent = EqYellow700,
    starFill = EqYellow600,
    starContainer = EqYellow100,
    onStarContainer = EqYellow900,
    progressTrack = EqNeutral200,
    cardBorder = EqNeutral300,
    subtleSurfaceTint = EqNeutral100,
    modeMulStart = EqRed600,
    modeMulEnd = EqRed700,
    modeDivStart = EqBlue600,
    modeDivEnd = EqBlue700,
    modeMixStart = EqGreen600,
    modeMixEnd = EqGreen700,
    logoPlateBackground = EqWhite,
    logoPlateBorder = EqNeutral300
)

private val DarkEqSemanticColors = EqSemanticColors(
    brandNavyStart = EqNavy500,
    brandNavyEnd = EqNavy800,
    onBrandNavy = EqWhite,
    onBrandNavyMuted = Color(0xFFE6E6F5),
    success = EqGreen400,
    onSuccess = Color(0xFF072911),
    successContainer = EqDarkSuccessContainer,
    onSuccessContainer = EqDarkOnSuccessContainer,
    danger = EqRed400,
    onDanger = Color(0xFF4A0904),
    dangerContainer = EqDarkDangerContainer,
    onDangerContainer = EqDarkOnDangerContainer,
    info = EqBlue400,
    onInfo = Color(0xFF09204A),
    infoContainer = EqDarkInfoContainer,
    onInfoContainer = EqDarkOnInfoContainer,
    starAccent = EqYellow400,
    starFill = EqYellow400,
    starContainer = EqDarkStarContainer,
    onStarContainer = EqDarkOnStarContainer,
    progressTrack = EqDarkProgressTrack,
    cardBorder = EqDarkOutlineVariant,
    subtleSurfaceTint = EqDarkSurfaceVariant,
    modeMulStart = EqRed600,
    modeMulEnd = EqRed700,
    modeDivStart = EqBlue600,
    modeDivEnd = EqBlue700,
    modeMixStart = EqGreen600,
    modeMixEnd = EqGreen700,
    logoPlateBackground = EqWhite,
    logoPlateBorder = EqDarkOutline
)

val LocalEqSemanticColors = staticCompositionLocalOf { LightEqSemanticColors }

object EqTheme {
    val colors: EqSemanticColors
        @Composable
        @ReadOnlyComposable
        get() = LocalEqSemanticColors.current
}

private val EqLightColorScheme = lightColorScheme(
    primary = EqNavy500,
    onPrimary = EqWhite,
    primaryContainer = EqNavy100,
    onPrimaryContainer = EqNavy900,
    secondary = EqBlue600,
    onSecondary = EqWhite,
    secondaryContainer = EqBlue100,
    onSecondaryContainer = EqBlue900,
    tertiary = EqYellow600,
    onTertiary = EqWhite,
    tertiaryContainer = EqYellow100,
    onTertiaryContainer = EqYellow900,
    error = EqRed600,
    onError = EqWhite,
    errorContainer = EqRed100,
    onErrorContainer = EqRed900,
    background = EqNeutral50,
    onBackground = EqNavy900,
    surface = EqWhite,
    onSurface = EqNavy900,
    surfaceVariant = EqNeutral100,
    onSurfaceVariant = EqNeutral600,
    outline = EqNeutral500,
    outlineVariant = EqNeutral300
)

private val EqDarkColorScheme = darkColorScheme(
    primary = EqDarkPrimary,
    onPrimary = EqNavy900,
    primaryContainer = EqNavy600,
    onPrimaryContainer = EqNavy50,
    secondary = EqBlue400,
    onSecondary = EqBlue900,
    secondaryContainer = EqDarkInfoContainer,
    onSecondaryContainer = EqDarkOnInfoContainer,
    tertiary = EqYellow400,
    onTertiary = EqYellow900,
    tertiaryContainer = EqDarkStarContainer,
    onTertiaryContainer = EqDarkOnStarContainer,
    error = EqRed400,
    onError = EqRed900,
    errorContainer = EqDarkDangerContainer,
    onErrorContainer = EqDarkOnDangerContainer,
    background = EqDarkBackground,
    onBackground = EqDarkOnSurface,
    surface = EqDarkSurface,
    onSurface = EqDarkOnSurface,
    surfaceVariant = EqDarkSurfaceVariant,
    onSurfaceVariant = EqDarkOnSurfaceVariant,
    outline = EqDarkOutline,
    outlineVariant = EqDarkOutlineVariant
)

// Matches EQ-Math design tokens (--radius-button: 12px, --radius-card: 16px)
val EqMathShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun EqMathTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) EqDarkColorScheme else EqLightColorScheme
    val semanticColors = if (darkTheme) DarkEqSemanticColors else LightEqSemanticColors

    CompositionLocalProvider(LocalEqSemanticColors provides semanticColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = EqMathTypography,
            shapes = EqMathShapes,
            content = content
        )
    }
}
