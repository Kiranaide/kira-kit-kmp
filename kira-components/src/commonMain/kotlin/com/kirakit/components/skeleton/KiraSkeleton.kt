package com.kirakit.components.skeleton

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import kotlinx.coroutines.delay

enum class KiraSkeletonVariant {
    Default,
    Circle,
    Text,
    Shimmer,
    Wave,
}

enum class KiraSkeletonPreset {
    Text,
    Card,
    List,
    Table,
}

enum class KiraSkeletonAvatarSize(val size: Dp) {
    Sm(32.dp),
    Md(40.dp),
    Lg(48.dp),
    Xl(64.dp),
}

enum class KiraSkeletonButtonSize(val height: Dp) {
    Xs(28.dp),
    Sm(32.dp),
    Default(36.dp),
    Lg(40.dp),
    Xl(44.dp),
}

@Immutable
data class KiraSkeletonConfig(
    val loading: Boolean = true,
    val animated: Boolean = true,
    val paused: Boolean = false,
    val durationMillis: Int = KiraMotion.ShimmerMs,
    val variant: KiraSkeletonVariant? = null,
)

private val LocalKiraSkeletonConfig = compositionLocalOf { KiraSkeletonConfig() }
private val LocalKiraSkeletonClock = staticCompositionLocalOf<InfiniteTransition?> { null }

/**
 * Clamps repeated skeleton element counts to a safe bounded range (`0..100`).
 */
fun clampSkeletonCount(count: Int, fallback: Int = 3): Int =
    if (count < 0) 0 else count.coerceAtMost(100)

/**
 * Coordinates animation timing, pause state, and accessibility live-region announcements
 * across a group of skeleton bones (aligned with kira-kit-v2's `SkeletonRoot` / `SkeletonGroup`).
 *
 * Shares a single [InfiniteTransition] clock across all descendant bones so a screen with
 * dozens of skeleton placeholders drives only one animation clock.
 */
@Composable
fun KiraSkeletonGroup(
    modifier: Modifier = Modifier,
    loading: Boolean = true,
    animated: Boolean = true,
    paused: Boolean = false,
    durationMillis: Int = KiraMotion.ShimmerMs,
    variant: KiraSkeletonVariant? = null,
    loadingLabel: String = "Loading content",
    loadedLabel: String = "Content loaded",
    announce: Boolean = true,
    content: @Composable () -> Unit,
) {
    val parent = LocalKiraSkeletonConfig.current
    val parentClock = LocalKiraSkeletonClock.current
    val effectiveConfig = remember(loading, animated, paused, durationMillis, variant, parent) {
        KiraSkeletonConfig(
            loading = loading,
            animated = animated && parent.animated,
            paused = paused || parent.paused,
            durationMillis = durationMillis.coerceAtLeast(0),
            variant = variant ?: parent.variant,
        )
    }
    val needsClock = effectiveConfig.loading &&
        effectiveConfig.animated &&
        !effectiveConfig.paused &&
        effectiveConfig.durationMillis > 0
    val sharedClock = when {
        parentClock != null -> parentClock
        needsClock -> rememberInfiniteTransition(label = "KiraSkeletonGroupClock")
        else -> null
    }

    CompositionLocalProvider(
        LocalKiraSkeletonConfig provides effectiveConfig,
        LocalKiraSkeletonClock provides sharedClock,
    ) {
        Box(
            modifier = modifier.then(
                if (announce) {
                    Modifier.semantics {
                        liveRegion = LiveRegionMode.Polite
                        stateDescription = if (loading) loadingLabel else loadedLabel
                    }
                } else {
                    Modifier
                },
            ),
        ) {
            content()
        }
    }
}

/**
 * Single animated placeholder bone aligned with kira-kit-v2's `Skeleton` / `SkeletonBone`.
 *
 * Supports `Default` (pulse), `Circle`, `Text`, `Shimmer` (gradient sweep), and `Wave`
 * motion presets. Animation frames execute exclusively in the Draw / GraphicsLayer phase
 * with zero per-frame recompositions or Brush allocations.
 */
@Composable
fun KiraSkeleton(
    modifier: Modifier = Modifier,
    variant: KiraSkeletonVariant = KiraSkeletonVariant.Default,
    animated: Boolean? = null,
    paused: Boolean? = null,
    durationMillis: Int? = null,
    shape: Shape? = null,
) {
    val group = LocalKiraSkeletonConfig.current
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius

    val effectiveVariant = if (variant == KiraSkeletonVariant.Default && group.variant != null) {
        group.variant
    } else {
        variant
    }
    val effectiveDuration = (durationMillis ?: group.durationMillis).coerceAtLeast(0)
    val shouldAnimate = group.loading &&
        group.animated &&
        (animated ?: true) &&
        !(paused ?: group.paused) &&
        effectiveDuration > 0

    val resolvedShape = shape ?: when (effectiveVariant) {
        KiraSkeletonVariant.Circle -> CircleShape
        else -> radius.shapeMd
    }

    val baseColor = colors.muted
    val highlightColor = colors.background.copy(alpha = 0.55f)

    if (!shouldAnimate) {
        Box(
            modifier = modifier
                .clip(resolvedShape)
                .background(baseColor)
                .clearAndSetSemantics {},
        )
        return
    }

    val infinite = LocalKiraSkeletonClock.current ?: rememberInfiniteTransition(label = "KiraSkeletonClock")
    when (effectiveVariant) {
        KiraSkeletonVariant.Shimmer -> {
            val progressState = infinite.animateFloat(
                initialValue = -1f,
                targetValue = 2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = effectiveDuration, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "SkeletonShimmer",
            )
            Box(
                modifier = modifier
                    .clip(resolvedShape)
                    .drawWithCache {
                        val bandWidth = (size.width * 0.8f).coerceAtLeast(120f)
                        val shimmerBrush = Brush.linearGradient(
                            colors = listOf(baseColor, highlightColor, baseColor),
                            start = Offset(0f, 0f),
                            end = Offset(bandWidth, size.height.coerceAtLeast(1f)),
                        )
                        val bandSize = Size(bandWidth, size.height)
                        onDrawBehind {
                            drawRect(baseColor)
                            val travel = size.width + bandWidth
                            val left = -bandWidth + ((progressState.value + 1f) / 3f) * travel
                            withTransform({ translate(left = left) }) {
                                drawRect(brush = shimmerBrush, size = bandSize)
                            }
                        }
                    }
                    .clearAndSetSemantics {},
            )
        }
        KiraSkeletonVariant.Wave -> {
            val phaseState = infinite.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = effectiveDuration, easing = KiraMotion.EaseOutStrong),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "SkeletonWave",
            )
            Box(
                modifier = modifier
                    .clip(resolvedShape)
                    .drawWithCache {
                        val edgeBrush = Brush.horizontalGradient(
                            colors = listOf(baseColor, baseColor.copy(alpha = 0.55f), baseColor),
                        )
                        val centerBrush = Brush.horizontalGradient(
                            colors = listOf(baseColor.copy(alpha = 0.55f), baseColor, baseColor.copy(alpha = 0.55f)),
                        )
                        onDrawBehind {
                            val phase = phaseState.value
                            drawRect(brush = edgeBrush, alpha = 1f - phase * 0.45f)
                            drawRect(brush = centerBrush, alpha = phase * 0.65f)
                        }
                    }
                    .clearAndSetSemantics {},
            )
        }
        KiraSkeletonVariant.Default, KiraSkeletonVariant.Circle, KiraSkeletonVariant.Text -> {
            val alphaState = infinite.animateFloat(
                initialValue = 1f,
                targetValue = 0.45f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = (effectiveDuration / 2).coerceAtLeast(300), easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "SkeletonPulse",
            )
            Box(
                modifier = modifier
                    .graphicsLayer { alpha = alphaState.value }
                    .clip(resolvedShape)
                    .background(baseColor)
                    .clearAndSetSemantics {},
            )
        }
    }
}

@Composable
fun KiraSkeletonText(
    modifier: Modifier = Modifier,
    variant: KiraSkeletonVariant = KiraSkeletonVariant.Text,
    animated: Boolean? = null,
    paused: Boolean? = null,
) {
    KiraSkeleton(
        modifier = modifier.fillMaxWidth().height(16.dp),
        variant = variant,
        animated = animated,
        paused = paused,
    )
}

@Composable
fun KiraSkeletonHeading(
    modifier: Modifier = Modifier,
    variant: KiraSkeletonVariant = KiraSkeletonVariant.Text,
    animated: Boolean? = null,
    paused: Boolean? = null,
) {
    KiraSkeleton(
        modifier = modifier.fillMaxWidth(0.75f).height(24.dp),
        variant = variant,
        animated = animated,
        paused = paused,
    )
}

@Composable
fun KiraSkeletonButton(
    modifier: Modifier = Modifier,
    size: KiraSkeletonButtonSize = KiraSkeletonButtonSize.Default,
    width: Dp = 96.dp,
    variant: KiraSkeletonVariant = KiraSkeletonVariant.Default,
    animated: Boolean? = null,
    paused: Boolean? = null,
) {
    KiraSkeleton(
        modifier = modifier.width(width).height(size.height),
        variant = variant,
        animated = animated,
        paused = paused,
        shape = KiraTheme.radius.shapeLg,
    )
}

@Composable
fun KiraSkeletonAvatar(
    modifier: Modifier = Modifier,
    size: KiraSkeletonAvatarSize = KiraSkeletonAvatarSize.Md,
    animated: Boolean? = null,
    paused: Boolean? = null,
) {
    KiraSkeleton(
        modifier = modifier.size(size.size),
        variant = KiraSkeletonVariant.Circle,
        animated = animated,
        paused = paused,
        shape = CircleShape,
    )
}

@Composable
fun KiraSkeletonImage(
    modifier: Modifier = Modifier,
    aspectRatio: Float = 16f / 9f,
    variant: KiraSkeletonVariant = KiraSkeletonVariant.Default,
    animated: Boolean? = null,
    paused: Boolean? = null,
) {
    KiraSkeleton(
        modifier = modifier.fillMaxWidth().aspectRatio(aspectRatio.coerceAtLeast(0.1f)),
        variant = variant,
        animated = animated,
        paused = paused,
        shape = KiraTheme.radius.shapeLg,
    )
}

@Composable
fun KiraSkeletonLines(
    modifier: Modifier = Modifier,
    count: Int = 3,
    gap: Dp = 8.dp,
    lastLineWidthFraction: Float = 0.75f,
    variant: KiraSkeletonVariant = KiraSkeletonVariant.Text,
    animated: Boolean? = null,
    paused: Boolean? = null,
) {
    val lines = clampSkeletonCount(count, 3)
    Column(
        modifier = modifier.fillMaxWidth().clearAndSetSemantics {},
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        repeat(lines) { index ->
            val fraction = if (index == lines - 1) lastLineWidthFraction.coerceIn(0.1f, 1f) else 1f
            KiraSkeleton(
                modifier = Modifier.fillMaxWidth(fraction).height(16.dp),
                variant = variant,
                animated = animated,
                paused = paused,
            )
        }
    }
}

@Composable
fun KiraSkeletonCard(
    modifier: Modifier = Modifier,
    variant: KiraSkeletonVariant = KiraSkeletonVariant.Default,
    animated: Boolean? = null,
    paused: Boolean? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().clearAndSetSemantics {},
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        KiraSkeletonImage(variant = variant, animated = animated, paused = paused)
        KiraSkeletonHeading(variant = variant, animated = animated, paused = paused)
        KiraSkeletonLines(
            count = 2,
            lastLineWidthFraction = 0.5f,
            variant = variant,
            animated = animated,
            paused = paused,
        )
    }
}

@Composable
fun KiraSkeletonTableRow(
    modifier: Modifier = Modifier,
    columns: Int = 4,
    variant: KiraSkeletonVariant = KiraSkeletonVariant.Default,
    animated: Boolean? = null,
    paused: Boolean? = null,
) {
    val cols = clampSkeletonCount(columns, 4)
    Row(
        modifier = modifier.fillMaxWidth().clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        repeat(cols) {
            KiraSkeleton(
                modifier = Modifier.weight(1f).height(16.dp),
                variant = variant,
                animated = animated,
                paused = paused,
            )
        }
    }
}

@Composable
fun KiraSkeletonListItem(
    modifier: Modifier = Modifier,
    variant: KiraSkeletonVariant = KiraSkeletonVariant.Default,
    animated: Boolean? = null,
    paused: Boolean? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().clearAndSetSemantics {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        KiraSkeletonAvatar(size = KiraSkeletonAvatarSize.Sm, animated = animated, paused = paused)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KiraSkeleton(
                modifier = Modifier.fillMaxWidth(0.75f).height(16.dp),
                variant = variant,
                animated = animated,
                paused = paused,
            )
            KiraSkeleton(
                modifier = Modifier.fillMaxWidth(0.50f).height(12.dp),
                variant = variant,
                animated = animated,
                paused = paused,
            )
        }
    }
}

private enum class SkeletonDisplayPhase {
    Waiting,
    Visible,
    Ready,
}

/**
 * Smart loading container aligned with kira-kit-v2's `KiraSkeleton`.
 *
 * - `delayMs` prevents jarring skeleton flashes on fast responses (default `150ms`).
 * - `minDurationMs` keeps a shown skeleton visible long enough to avoid flicker.
 * - `retainContent = true` preserves previously loaded content while refreshing.
 */
@Composable
fun KiraSkeletonContainer(
    loading: Boolean,
    modifier: Modifier = Modifier,
    preset: KiraSkeletonPreset = KiraSkeletonPreset.Text,
    count: Int = 3,
    delayMs: Long = 150L,
    minDurationMs: Long = 0L,
    retainContent: Boolean = false,
    minHeight: Dp = Dp.Unspecified,
    variant: KiraSkeletonVariant? = null,
    loadingLabel: String = "Loading content",
    loadedLabel: String = "Content loaded",
    fallback: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    var phase by remember { mutableStateOf(if (loading) SkeletonDisplayPhase.Waiting else SkeletonDisplayPhase.Ready) }
    var revealed by remember { mutableStateOf(!loading) }

    LaunchedEffect(loading, delayMs, minDurationMs) {
        if (loading) {
            if (phase == SkeletonDisplayPhase.Ready) {
                phase = SkeletonDisplayPhase.Waiting
            }
            if (phase == SkeletonDisplayPhase.Waiting) {
                if (delayMs > 0L) delay(delayMs)
                phase = SkeletonDisplayPhase.Visible
            }
        } else {
            if (phase == SkeletonDisplayPhase.Visible && minDurationMs > 0L) {
                delay(minDurationMs)
            }
            phase = SkeletonDisplayPhase.Ready
            revealed = true
        }
    }

    val retaining = revealed && retainContent
    val masking = !retaining && phase != SkeletonDisplayPhase.Ready
    val pending = loading || masking

    KiraSkeletonGroup(
        loading = pending,
        variant = variant,
        loadingLabel = if (retaining && loading) "Refreshing content" else loadingLabel,
        loadedLabel = loadedLabel,
        modifier = modifier
            .fillMaxWidth()
            .then(if (minHeight != Dp.Unspecified) Modifier.heightIn(min = minHeight) else Modifier),
    ) {
        AnimatedContent(
            targetState = if (masking) phase else SkeletonDisplayPhase.Ready,
            transitionSpec = {
                fadeIn(tween(KiraMotion.RouteFadeMs, easing = KiraMotion.EaseOutStrong)) togetherWith
                    fadeOut(tween(120))
            },
            label = "KiraSkeletonContainer",
        ) { target ->
            when (target) {
                SkeletonDisplayPhase.Waiting -> {
                    Box(Modifier.fillMaxWidth().semantics { contentDescription = loadingLabel })
                }
                SkeletonDisplayPhase.Visible -> {
                    if (fallback != null) {
                        fallback()
                    } else {
                        when (preset) {
                            KiraSkeletonPreset.Text -> KiraSkeletonLines(count = count)
                            KiraSkeletonPreset.Card -> KiraSkeletonCard()
                            KiraSkeletonPreset.List -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                repeat(clampSkeletonCount(count, 3)) { KiraSkeletonListItem() }
                            }
                            KiraSkeletonPreset.Table -> Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                repeat(clampSkeletonCount(count, 3)) { KiraSkeletonTableRow() }
                            }
                        }
                    }
                }
                SkeletonDisplayPhase.Ready -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (retaining && loading) Modifier.graphicsLayer { alpha = 0.6f } else Modifier),
                    ) {
                        content()
                    }
                }
            }
        }
    }
}
