package com.kirakit.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp

/**
 * Radius presets exported from `kira-kit-v2` (`tokens/kira-tokens.json`).
 * Kept outside `KiraTheme.kt` so re-running `bun run export:kmp` never clobbers it.
 */
@Immutable
data class KiraRadiusPreset(
    val id: String,
    val title: String,
    val description: String,
    val scale: KiraRadiusScale,
)

object KiraRadiusPresets {
    val Sharp = KiraRadiusPreset(
        id = "sharp",
        title = "Sharp",
        description = "Architectural 0dp corners for dense consoles and editorial grids.",
        scale = KiraRadiusScale(
            sm = 0.dp,
            md = 0.dp,
            lg = 0.dp,
            xl = 4.dp,
            xxl = 8.dp,
            xxxl = 12.dp,
            xxxxl = 16.dp,
        ),
    )

    val Crisp = KiraRadiusPreset(
        id = "crisp",
        title = "Crisp",
        description = "Tight 6dp corners tuned for developer tools and precision workspaces.",
        scale = KiraRadiusScale(
            sm = 2.dp,
            md = 4.dp,
            lg = 6.dp,
            xl = 10.dp,
            xxl = 14.dp,
            xxxl = 18.dp,
            xxxxl = 22.dp,
        ),
    )

    val Balanced = KiraRadiusPreset(
        id = "balanced",
        title = "Balanced",
        description = "Versatile 10dp corners balancing clarity and approachability.",
        scale = KiraRadiusScale(
            sm = 6.dp,
            md = 8.dp,
            lg = 10.dp,
            xl = 14.dp,
            xxl = 18.dp,
            xxxl = 22.dp,
            xxxxl = 26.dp,
        ),
    )

    val Soft = KiraRadiusPreset(
        id = "soft",
        title = "Soft",
        description = "Generous 14dp corners for calm consumer and studio products.",
        scale = KiraRadiusScale(
            sm = 10.dp,
            md = 12.dp,
            lg = 14.dp,
            xl = 18.dp,
            xxl = 22.dp,
            xxxl = 26.dp,
            xxxxl = 30.dp,
        ),
    )

    val Pill = KiraRadiusPreset(
        id = "pill",
        title = "Pill",
        description = "Expressive 20dp geometry for tactile, friendly interfaces.",
        scale = KiraRadiusScale(
            sm = 16.dp,
            md = 18.dp,
            lg = 20.dp,
            xl = 24.dp,
            xxl = 28.dp,
            xxxl = 32.dp,
            xxxxl = 36.dp,
        ),
    )

    val All: List<KiraRadiusPreset> = listOf(Sharp, Crisp, Balanced, Soft, Pill)
}

val KiraRadiusScale.shapeFull get() = CircleShape

/**
 * Overload of [KiraTheme] supporting an optional [radiusOverride] (matching kira-kit-v2's
 * Theme Studio radius selector) without modifying generated `KiraTheme.kt`.
 */
@Composable
fun KiraTheme(
    theme: KiraThemeDefinition = KiraThemes.Default,
    darkTheme: Boolean = isSystemInDarkTheme(),
    radiusOverride: KiraRadiusScale?,
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) theme.dark else theme.light
    CompositionLocalProvider(
        LocalKiraColors provides colors,
        LocalKiraRadius provides (radiusOverride ?: theme.radius),
        content = content,
    )
}
