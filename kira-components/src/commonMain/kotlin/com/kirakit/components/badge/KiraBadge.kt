package com.kirakit.components.badge

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.shapeFull
import com.kirakit.theme.typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.roundToLong

enum class KiraBadgeVariant {
    Default,
    Secondary,
    Destructive,
    Success,
    Warning,
    Info,
    Outline,
    Ghost,
    Link,
    PrimarySoft,
    SecondarySoft,
    DestructiveSoft,
    SuccessSoft,
    WarningSoft,
    InfoSoft,
}

enum class KiraBadgeSize(
    val height: Dp,
    internal val horizontalPadding: Dp,
    internal val fontSize: Int,
    val iconSize: Dp,
    internal val dotSize: Dp,
    internal val closeButtonSize: Dp,
    internal val closeIconSize: Dp,
) {
    Xs(16.dp, 6.dp, 10, 10.dp, 6.dp, 12.dp, 8.dp),
    Sm(20.dp, 8.dp, 12, 12.dp, 8.dp, 14.dp, 10.dp),
    Default(24.dp, 10.dp, 12, 14.dp, 10.dp, 16.dp, 12.dp),
    Lg(28.dp, 12.dp, 14, 16.dp, 12.dp, 16.dp, 12.dp),
    Xl(32.dp, 16.dp, 14, 16.dp, 14.dp, 20.dp, 16.dp),
}

enum class KiraBadgeDot {
    Default,
    Secondary,
    Destructive,
    Success,
    Warning,
    Info,
    Outline,
    Muted,
}

enum class KiraBadgePulse {
    None,
    Continuous,
    Once,
}

enum class KiraBadgeTrend {
    Up,
    Down,
}

enum class KiraBadgePreset {
    Beta,
    New,
    Version,
}

enum class KiraBadgeCountFormat {
    Default,
    Short,
    Commas,
}

enum class KiraBadgeStatus {
    Success,
    Warning,
    Error,
    Info,
    Default,
}

enum class KiraBadgeIconPosition {
    Left,
    Right,
}

enum class KiraBadgeGroupGap(val spacing: Dp) {
    Tight(4.dp),
    Normal(8.dp),
    Loose(12.dp),
}

enum class KiraBadgeGroupOrientation {
    Horizontal,
    Vertical,
}

enum class KiraBadgeMode {
    Badge,
    Status,
    Count,
    Tag,
    Dot,
}

typealias KiraBadgeIcon = @Composable (color: Color, size: Dp) -> Unit

/**
 * Formats a numeric count identically to kira-kit-v2's `formatCount`.
 */
fun formatBadgeCount(
    count: Int,
    format: KiraBadgeCountFormat = KiraBadgeCountFormat.Default,
    max: Int = 99,
): String = when (format) {
    KiraBadgeCountFormat.Short -> when {
        count >= 1_000_000 -> formatCompactCount(count, 1_000_000, "M")
        count >= 1_000 -> formatCompactCount(count, 1_000, "k")
        else -> if (count > max) "$max+" else count.toString()
    }
    KiraBadgeCountFormat.Commas -> {
        if (count > max) "${formatWithCommas(max)}+" else formatWithCommas(count)
    }
    KiraBadgeCountFormat.Default -> if (count > max) "$max+" else count.toString()
}

private fun formatCompactCount(count: Int, unit: Int, suffix: String): String {
    val tenths = (count.toDouble() / unit * 10.0).roundToLong()
    val whole = tenths / 10
    val decimal = tenths % 10
    return if (decimal == 0L) "$whole$suffix+" else "$whole.$decimal$suffix+"
}

private fun formatWithCommas(value: Int): String {
    val negative = value < 0
    val digits = value.toLong().let { if (negative) -it else it }.toString()
    val grouped = digits.reversed().chunked(3).joinToString(",").reversed()
    return if (negative) "-$grouped" else grouped
}

@Composable
fun KiraBadge(
    text: String? = null,
    modifier: Modifier = Modifier,
    variant: KiraBadgeVariant = KiraBadgeVariant.Default,
    size: KiraBadgeSize = KiraBadgeSize.Default,
    icon: KiraBadgeIcon? = null,
    iconPosition: KiraBadgeIconPosition = KiraBadgeIconPosition.Left,
    dot: KiraBadgeDot? = null,
    dotPulse: KiraBadgePulse = KiraBadgePulse.None,
    dismissible: Boolean = false,
    onClose: (() -> Unit)? = null,
    closeLabel: String = "Remove",
    loading: Boolean = false,
    enabled: Boolean = true,
    shimmer: Boolean = false,
    trend: KiraBadgeTrend? = null,
    preset: KiraBadgePreset? = null,
    pressed: Boolean? = null,
    shape: Shape = KiraTheme.radius.shapeFull,
    onClick: (() -> Unit)? = null,
) {
    val colors = KiraTheme.colors
    val isDark = colors.background.luminance() < 0.5f

    val resolvedVariant = when (preset) {
        KiraBadgePreset.Beta -> KiraBadgeVariant.WarningSoft
        KiraBadgePreset.New -> KiraBadgeVariant.SuccessSoft
        KiraBadgePreset.Version -> KiraBadgeVariant.Secondary
        null -> variant
    }
    val resolvedText = text ?: when (preset) {
        KiraBadgePreset.Beta -> "Beta"
        KiraBadgePreset.New -> "New"
        KiraBadgePreset.Version, null -> ""
    }

    var isVisible by remember { mutableStateOf(true) }
    var isClosing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (!isVisible) return

    val showDismiss = (dismissible || onClose != null) && !loading

    val (baseBackground, foreground) = resolveBadgeColors(resolvedVariant, isDark)
    val rawDotColor = when (dot) {
        KiraBadgeDot.Default -> colors.primary
        KiraBadgeDot.Secondary -> colors.secondary
        KiraBadgeDot.Destructive -> colors.destructive
        KiraBadgeDot.Success -> colors.success
        KiraBadgeDot.Warning -> colors.warning
        KiraBadgeDot.Info -> colors.info
        KiraBadgeDot.Outline -> colors.foreground
        KiraBadgeDot.Muted -> colors.mutedForeground
        null -> foreground
    }
    val dotColor = if (rawDotColor == baseBackground) foreground else rawDotColor

    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val hovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isClickable = onClick != null && enabled && !loading

    val targetBg = when {
        hovered && resolvedVariant == KiraBadgeVariant.Ghost ->
            colors.muted.copy(alpha = if (isDark) 0.50f else 0.80f)
        hovered && resolvedVariant == KiraBadgeVariant.Outline && isClickable ->
            colors.muted
        else -> baseBackground
    }
    val animatedBgState = animateColorAsState(
        targetValue = targetBg,
        animationSpec = KiraMotion.fastTween(),
        label = "Badge background",
    )

    val hoverOpacity = if (isClickable && hovered) 0.90f else 1f
    val pressScaleState = animateFloatAsState(
        targetValue = when {
            isClosing -> 0.95f
            isClickable && isPressed -> 0.95f
            else -> 1f
        },
        animationSpec = KiraMotion.fastTween(),
        label = "Badge scale",
    )
    val contentAlphaState = animateFloatAsState(
        targetValue = when {
            isClosing -> 0f
            !enabled -> 0.50f
            else -> hoverOpacity
        },
        animationSpec = KiraMotion.fastTween(),
        label = "Badge alpha",
    )

    val textStyle = KiraTheme.typography.caption.copy(
        color = foreground,
        fontWeight = FontWeight.Medium,
        fontSize = size.fontSize.sp,
        lineHeight = (size.fontSize + 4).sp,
        textDecoration = if (resolvedVariant == KiraBadgeVariant.Link && (hovered || focused)) {
            TextDecoration.Underline
        } else {
            null
        },
    )

    val borderModifier = when {
        focused -> Modifier.border(2.dp, colors.ring, shape)
        resolvedVariant == KiraBadgeVariant.Outline -> Modifier.border(1.dp, colors.border, shape)
        else -> Modifier
    }

    val isSoftOrLight = resolvedVariant in SoftOrLightBadgeVariants
    val shimmerTint = if (isSoftOrLight) {
        colors.foreground.copy(alpha = 0.15f)
    } else {
        Color.White.copy(alpha = 0.25f)
    }

    val interactiveModifier = if (onClick != null) {
        Modifier
            .pointerHoverIcon(if (isClickable) PointerIcon.Hand else PointerIcon.Default)
            .hoverable(interactionSource, enabled = isClickable)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isClickable,
                role = Role.Button,
                onClick = onClick,
            )
    } else {
        Modifier.hoverable(interactionSource, enabled = enabled)
    }

    val handleClose: () -> Unit = {
        if (enabled && !loading && !isClosing) {
            isClosing = true
            scope.launch {
                val scaleFactor = coroutineContext[MotionDurationScale]?.scaleFactor ?: 1f
                if (scaleFactor > 0f) {
                    delay(KiraMotion.RouteFadeMs.toLong())
                }
                isVisible = false
                onClose?.invoke()
            }
        }
    }

    val rowContent: @Composable () -> Unit = {
        if (dot != null) {
            BadgeDotIndicator(color = dotColor, pulse = dotPulse, size = size)
        }
        if (trend != null) {
            BadgeTrendIcon(trend = trend, color = foreground, size = size.iconSize)
        }
        if (loading) {
            BadgeSpinner(color = foreground, size = size.iconSize)
        } else if (icon != null && iconPosition == KiraBadgeIconPosition.Left) {
            icon(foreground, size.iconSize)
        }
        if (resolvedText.isNotEmpty()) {
            BasicText(
                text = resolvedText,
                style = textStyle,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = if (showDismiss) Modifier.padding(end = 2.dp) else Modifier,
            )
        }
        if (!loading && icon != null && iconPosition == KiraBadgeIconPosition.Right) {
            icon(foreground, size.iconSize)
        }
        if (showDismiss) {
            BadgeCloseButton(
                color = foreground,
                ringColor = colors.ring,
                size = size,
                enabled = enabled,
                label = if (resolvedText.isNotEmpty()) "$closeLabel $resolvedText" else closeLabel,
                onClick = handleClose,
            )
        }
    }

    Row(
        modifier = modifier
            .graphicsLayer {
                val s = pressScaleState.value
                scaleX = s
                scaleY = s
                alpha = contentAlphaState.value
            }
            .height(size.height)
            .clip(shape)
            .drawBehind {
                val bg = animatedBgState.value
                if (bg.alpha > 0f) drawRect(bg)
            }
            .then(borderModifier)
            .badgeShimmer(enabled = shimmer, tint = shimmerTint)
            .then(interactiveModifier)
            .semantics {
                if (pressed != null && onClick != null) {
                    selected = pressed
                }
                if (loading) {
                    progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
                }
                if (!enabled) {
                    disabled()
                }
            }
            .padding(horizontal = size.horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        rowContent()
    }
}

private val SoftOrLightBadgeVariants = setOf(
    KiraBadgeVariant.Ghost,
    KiraBadgeVariant.Outline,
    KiraBadgeVariant.Link,
    KiraBadgeVariant.PrimarySoft,
    KiraBadgeVariant.SecondarySoft,
    KiraBadgeVariant.DestructiveSoft,
    KiraBadgeVariant.SuccessSoft,
    KiraBadgeVariant.WarningSoft,
    KiraBadgeVariant.InfoSoft,
)

/**
 * Unified prop-driven KiraBadge covering `badge`, `status`, `count`, `tag`, and `dot` modes,
 * matching kira-kit-v2's `KiraBadge` component.
 */
@Composable
fun KiraBadge(
    mode: KiraBadgeMode,
    modifier: Modifier = Modifier,
    text: String? = null,
    variant: KiraBadgeVariant = KiraBadgeVariant.Default,
    size: KiraBadgeSize = KiraBadgeSize.Default,
    icon: KiraBadgeIcon? = null,
    iconPosition: KiraBadgeIconPosition = KiraBadgeIconPosition.Left,
    trend: KiraBadgeTrend? = null,
    shimmer: Boolean = false,
    preset: KiraBadgePreset? = null,
    dismissible: Boolean = false,
    onClose: (() -> Unit)? = null,
    status: KiraBadgeStatus = KiraBadgeStatus.Default,
    label: String? = null,
    pulse: KiraBadgePulse = KiraBadgePulse.None,
    count: Int = 0,
    max: Int = 99,
    format: KiraBadgeCountFormat = KiraBadgeCountFormat.Default,
    animateCount: Boolean = false,
    dot: KiraBadgeDot? = null,
    dotPulse: KiraBadgePulse = KiraBadgePulse.None,
    enabled: Boolean = true,
    loading: Boolean = false,
    pressed: Boolean? = null,
    onClick: (() -> Unit)? = null,
) {
    when (mode) {
        KiraBadgeMode.Status -> {
            val statusSize = when (size) {
                KiraBadgeSize.Sm, KiraBadgeSize.Lg -> size
                else -> KiraBadgeSize.Default
            }
            KiraStatusBadge(
                status = status,
                modifier = modifier,
                label = label ?: text,
                size = statusSize,
                pulse = pulse,
            )
        }
        KiraBadgeMode.Count -> {
            val countSize = when (size) {
                KiraBadgeSize.Xs, KiraBadgeSize.Sm -> size
                else -> KiraBadgeSize.Default
            }
            val countVariant = when (variant) {
                KiraBadgeVariant.Destructive, KiraBadgeVariant.Secondary -> variant
                else -> KiraBadgeVariant.Default
            }
            KiraCountBadge(
                count = count,
                modifier = modifier,
                max = max,
                format = format,
                size = countSize,
                variant = countVariant,
                icon = icon,
                iconPosition = iconPosition,
                pulse = pulse != KiraBadgePulse.None,
                animateCount = animateCount,
            )
        }
        KiraBadgeMode.Tag -> {
            KiraTagBadge(
                label = text.orEmpty(),
                modifier = modifier,
                variant = variant,
                size = size,
                icon = icon,
                iconPosition = iconPosition,
                removable = dismissible,
                onRemove = onClose,
                enabled = enabled,
            )
        }
        KiraBadgeMode.Dot -> {
            KiraBadge(
                text = text,
                modifier = modifier,
                variant = variant,
                size = size,
                icon = icon,
                iconPosition = iconPosition,
                dot = dot ?: KiraBadgeDot.Default,
                dotPulse = dotPulse,
                dismissible = dismissible,
                onClose = onClose,
                loading = loading,
                enabled = enabled,
                shimmer = shimmer,
                trend = trend,
                preset = preset,
                pressed = pressed,
                onClick = onClick,
            )
        }
        KiraBadgeMode.Badge -> {
            KiraBadge(
                text = text,
                modifier = modifier,
                variant = variant,
                size = size,
                icon = icon,
                iconPosition = iconPosition,
                dot = dot,
                dotPulse = dotPulse,
                dismissible = dismissible,
                onClose = onClose,
                loading = loading,
                enabled = enabled,
                shimmer = shimmer,
                trend = trend,
                preset = preset,
                pressed = pressed,
                onClick = onClick,
            )
        }
    }
}

@Composable
private fun resolveBadgeColors(
    variant: KiraBadgeVariant,
    isDark: Boolean,
): Pair<Color, Color> {
    val colors = KiraTheme.colors
    return when (variant) {
        KiraBadgeVariant.Default -> colors.primary to colors.primaryForeground
        KiraBadgeVariant.Secondary -> colors.secondary to colors.secondaryForeground
        KiraBadgeVariant.Destructive -> colors.destructive to colors.destructiveForeground
        KiraBadgeVariant.Success -> colors.success to colors.successForeground
        KiraBadgeVariant.Warning -> colors.warning to colors.warningForeground
        KiraBadgeVariant.Info -> colors.info to colors.infoForeground
        KiraBadgeVariant.Outline -> Color.Transparent to colors.foreground
        KiraBadgeVariant.Ghost -> colors.muted to colors.mutedForeground
        KiraBadgeVariant.Link -> Color.Transparent to colors.primary
        KiraBadgeVariant.PrimarySoft -> colors.primary.copy(alpha = 0.10f) to colors.primary
        KiraBadgeVariant.SecondarySoft -> colors.secondary.copy(alpha = 0.50f) to colors.secondaryForeground
        KiraBadgeVariant.DestructiveSoft -> colors.destructive.copy(alpha = 0.10f) to colors.destructive
        KiraBadgeVariant.SuccessSoft -> {
            val fg = if (!isDark && colors.successForeground.luminance() < 0.4f) {
                colors.successForeground
            } else {
                colors.success
            }
            colors.success.copy(alpha = 0.10f) to fg
        }
        KiraBadgeVariant.WarningSoft -> {
            val fg = if (!isDark && colors.warningForeground.luminance() < 0.4f) {
                colors.warningForeground
            } else {
                colors.warning
            }
            colors.warning.copy(alpha = 0.10f) to fg
        }
        KiraBadgeVariant.InfoSoft -> {
            val fg = if (!isDark && colors.infoForeground.luminance() < 0.4f) {
                colors.infoForeground
            } else {
                colors.info
            }
            colors.info.copy(alpha = 0.10f) to fg
        }
    }
}

@Composable
private fun BadgeDotIndicator(
    color: Color,
    pulse: KiraBadgePulse,
    size: KiraBadgeSize,
) {
    val diameter = size.dotSize
    val pingProgressProvider: () -> Float = when (pulse) {
        KiraBadgePulse.None -> ({ 1f })
        KiraBadgePulse.Continuous -> {
            val transition = rememberInfiniteTransition(label = "Badge dot ping")
            val state = transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1_000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "Dot ping progress",
            )
            ({ state.value })
        }
        KiraBadgePulse.Once -> {
            val anim = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                anim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = KiraMotion.PingOnceMs, easing = LinearEasing),
                )
            }
            ({ anim.value })
        }
    }

    Canvas(
        modifier = Modifier
            .size(diameter)
            .clearAndSetSemantics {},
    ) {
        val baseRadius = this.size.minDimension / 2f
        val pingProgress = pingProgressProvider()
        if (pulse != KiraBadgePulse.None && pingProgress < 1f) {
            // Tailwind ping keyframes: 0% -> 75% expands scale 1 -> 2 and fades opacity -> 0; 75% -> 100% holds at 0.
            val t = (pingProgress / 0.75f).coerceIn(0f, 1f)
            val eased = 1f - (1f - t) * (1f - t)
            val ringRadius = baseRadius * (1f + eased * 1.1f)
            val ringAlpha = (1f - t) * 0.85f
            if (ringAlpha > 0.01f) {
                drawCircle(
                    color = color,
                    alpha = ringAlpha,
                    radius = ringRadius,
                    center = center,
                )
            }
        }
        drawCircle(
            color = color,
            radius = baseRadius,
            center = center,
        )
    }
}

@Composable
private fun BadgeTrendIcon(
    trend: KiraBadgeTrend,
    color: Color,
    size: Dp,
) {
    Canvas(
        Modifier
            .size(size)
            .clearAndSetSemantics {},
    ) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(
            width = (w * 0.14f).coerceAtLeast(1.25.dp.toPx()),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        val line = Path()
        val head = Path()
        when (trend) {
            KiraBadgeTrend.Up -> {
                line.moveTo(w * 0.12f, h * 0.72f)
                line.lineTo(w * 0.38f, h * 0.46f)
                line.lineTo(w * 0.56f, h * 0.62f)
                line.lineTo(w * 0.88f, h * 0.28f)
                head.moveTo(w * 0.60f, h * 0.28f)
                head.lineTo(w * 0.88f, h * 0.28f)
                head.lineTo(w * 0.88f, h * 0.56f)
            }
            KiraBadgeTrend.Down -> {
                line.moveTo(w * 0.12f, h * 0.28f)
                line.lineTo(w * 0.38f, h * 0.54f)
                line.lineTo(w * 0.56f, h * 0.38f)
                line.lineTo(w * 0.88f, h * 0.72f)
                head.moveTo(w * 0.60f, h * 0.72f)
                head.lineTo(w * 0.88f, h * 0.72f)
                head.lineTo(w * 0.88f, h * 0.44f)
            }
        }
        drawPath(line, color = color, style = stroke)
        drawPath(head, color = color, style = stroke)
    }
}

@Composable
private fun BadgeSpinner(color: Color, size: Dp) {
    val transition = rememberInfiniteTransition(label = "Badge spinner")
    val rotationState = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 900, easing = LinearEasing)),
        label = "Badge spinner rotation",
    )
    Box(
        Modifier
            .size(size)
            .clearAndSetSemantics {}
            .drawWithCache {
                val strokeWidth = (this.size.minDimension * 0.15f).coerceAtLeast(1.4.dp.toPx())
                val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                val radius = this.size.minDimension / 2f - strokeWidth / 2f
                val inset = strokeWidth / 2f
                val topLeft = Offset(inset, inset)
                val arcSize = Size(this.size.width - strokeWidth, this.size.height - strokeWidth)
                onDrawBehind {
                    drawCircle(
                        color = color,
                        radius = radius,
                        alpha = 0.25f,
                        style = stroke,
                    )
                    drawArc(
                        color = color,
                        startAngle = rotationState.value - 90f,
                        sweepAngle = 100f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = stroke,
                    )
                }
            },
    )
}

@Composable
private fun BadgeCloseButton(
    color: Color,
    ringColor: Color,
    size: KiraBadgeSize,
    enabled: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val alphaState = animateFloatAsState(
        targetValue = if (hovered || focused) 1f else 0.70f,
        animationSpec = KiraMotion.fastTween(),
        label = "Close alpha",
    )
    Box(
        modifier = Modifier
            .size(size.closeButtonSize)
            .clip(KiraTheme.radius.shapeFull)
            .then(if (focused) Modifier.border(2.dp, ringColor, KiraTheme.radius.shapeFull) else Modifier)
            .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
            .hoverable(interactionSource, enabled = enabled)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { contentDescription = label }
            .graphicsLayer { alpha = alphaState.value },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(size.closeIconSize)
                .clearAndSetSemantics {}
                .drawWithCache {
                    val strokeWidth = (this.size.minDimension * 0.18f).coerceAtLeast(1.25.dp.toPx())
                    val inset = strokeWidth
                    val tl = Offset(inset, inset)
                    val br = Offset(this.size.width - inset, this.size.height - inset)
                    val tr = Offset(this.size.width - inset, inset)
                    val bl = Offset(inset, this.size.height - inset)
                    onDrawBehind {
                        drawLine(color = color, start = tl, end = br, strokeWidth = strokeWidth, cap = StrokeCap.Round)
                        drawLine(color = color, start = tr, end = bl, strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    }
                },
        )
    }
}

@Composable
private fun Modifier.badgeShimmer(enabled: Boolean, tint: Color): Modifier {
    if (!enabled) return this
    val transition = rememberInfiniteTransition(label = "Badge shimmer")
    val phaseState = transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(durationMillis = KiraMotion.ShimmerMs, easing = LinearEasing)),
        label = "Shimmer phase",
    )
    return drawWithCache {
        val bandWidth = size.width.coerceAtLeast(1f)
        val brush = Brush.linearGradient(
            colors = listOf(Color.Transparent, tint, Color.Transparent),
            start = Offset(0f, 0f),
            end = Offset(bandWidth, size.height.coerceAtLeast(1f)),
        )
        val bandSize = Size(bandWidth, size.height)
        onDrawWithContent {
            drawContent()
            val left = size.width * phaseState.value - bandWidth * 0.5f
            withTransform({ translate(left = left) }) {
                drawRect(brush = brush, size = bandSize)
            }
        }
    }
}

@Composable
fun KiraStatusBadge(
    status: KiraBadgeStatus,
    modifier: Modifier = Modifier,
    label: String? = null,
    pulse: KiraBadgePulse = KiraBadgePulse.None,
    size: KiraBadgeSize = KiraBadgeSize.Default,
) {
    val dot = when (status) {
        KiraBadgeStatus.Success -> KiraBadgeDot.Success
        KiraBadgeStatus.Warning -> KiraBadgeDot.Warning
        KiraBadgeStatus.Error -> KiraBadgeDot.Destructive
        KiraBadgeStatus.Info -> KiraBadgeDot.Info
        KiraBadgeStatus.Default -> KiraBadgeDot.Muted
    }
    val defaultLabel = when (status) {
        KiraBadgeStatus.Success -> "Success"
        KiraBadgeStatus.Warning -> "Warning"
        KiraBadgeStatus.Error -> "Error"
        KiraBadgeStatus.Info -> "Info"
        KiraBadgeStatus.Default -> "Unknown"
    }
    KiraBadge(
        text = label ?: defaultLabel,
        modifier = modifier,
        variant = KiraBadgeVariant.Ghost,
        size = size,
        dot = dot,
        dotPulse = pulse,
    )
}

@Composable
fun KiraCountBadge(
    count: Int,
    modifier: Modifier = Modifier,
    max: Int = 99,
    format: KiraBadgeCountFormat = KiraBadgeCountFormat.Default,
    size: KiraBadgeSize = KiraBadgeSize.Default,
    variant: KiraBadgeVariant = KiraBadgeVariant.Default,
    icon: KiraBadgeIcon? = null,
    iconPosition: KiraBadgeIconPosition = KiraBadgeIconPosition.Left,
    pulse: Boolean = false,
    animateCount: Boolean = false,
) {
    val colors = KiraTheme.colors
    val isDark = colors.background.luminance() < 0.5f
    val (background, foreground) = resolveBadgeColors(variant, isDark)

    // Keep track of previous count even when format != Default so switching back never uses a stale origin.
    val animatedValue = remember { Animatable(count.toFloat()) }
    var lastCount by remember { mutableIntStateOf(count) }

    LaunchedEffect(count, animateCount, format) {
        val from = lastCount
        lastCount = count
        if (!animateCount || format != KiraBadgeCountFormat.Default || from == count) {
            animatedValue.snapTo(count.toFloat())
        } else {
            val scaleFactor = coroutineContext[MotionDurationScale]?.scaleFactor ?: 1f
            if (scaleFactor == 0f) {
                animatedValue.snapTo(count.toFloat())
            } else {
                animatedValue.animateTo(
                    targetValue = count.toFloat(),
                    animationSpec = tween(durationMillis = 350, easing = KiraMotion.EaseOutStrong),
                )
            }
        }
    }

    val displayedCount = if (animateCount && format == KiraBadgeCountFormat.Default) {
        animatedValue.value.roundToInt()
    } else {
        count
    }
    val displayText = formatBadgeCount(displayedCount, format, max)

    val showPulse = pulse && count > 0
    val pulseState = if (showPulse) {
        val transition = rememberInfiniteTransition(label = "Count badge pulse")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1_000, easing = KiraMotion.EaseOutStrong),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "Count pulse",
        )
    } else {
        null
    }

    val minWidth = when (size) {
        KiraBadgeSize.Xs -> 16.dp
        KiraBadgeSize.Sm -> 20.dp
        KiraBadgeSize.Default -> 24.dp
        KiraBadgeSize.Lg -> 28.dp
        KiraBadgeSize.Xl -> 32.dp
    }
    val style = KiraTheme.typography.caption.copy(
        color = foreground,
        fontWeight = FontWeight.Medium,
        fontSize = size.fontSize.sp,
        lineHeight = (size.fontSize + 4).sp,
        fontFeatureSettings = "tnum",
    )

    val shape = KiraTheme.radius.shapeFull
    val borderMod = if (variant == KiraBadgeVariant.Outline) {
        Modifier.border(1.dp, colors.border, shape)
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .then(
                if (pulseState != null) {
                    Modifier.graphicsLayer {
                        val p = pulseState.value
                        val s = 1f + p * 0.08f
                        scaleX = s
                        scaleY = s
                        alpha = 1f - p * 0.15f
                    }
                } else {
                    Modifier
                },
            )
            .height(size.height)
            .defaultMinSize(minWidth = minWidth)
            .clip(shape)
            .background(background)
            .then(borderMod)
            .padding(horizontal = if (displayText.length <= 1 && icon == null) 4.dp else 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null && iconPosition == KiraBadgeIconPosition.Left) {
            icon(foreground, size.iconSize)
        }
        BasicText(
            text = displayText,
            style = style,
            maxLines = 1,
            softWrap = false,
        )
        if (icon != null && iconPosition == KiraBadgeIconPosition.Right) {
            icon(foreground, size.iconSize)
        }
    }
}

@Composable
fun KiraTagBadge(
    label: String,
    modifier: Modifier = Modifier,
    variant: KiraBadgeVariant = KiraBadgeVariant.Default,
    size: KiraBadgeSize = KiraBadgeSize.Sm,
    icon: KiraBadgeIcon? = null,
    iconPosition: KiraBadgeIconPosition = KiraBadgeIconPosition.Left,
    removable: Boolean = false,
    onRemove: (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    val isRemovable = removable || onRemove != null
    KiraBadge(
        text = label,
        modifier = modifier,
        variant = variant,
        size = size,
        icon = icon,
        iconPosition = iconPosition,
        dismissible = isRemovable,
        onClose = onRemove,
        enabled = enabled,
        shape = KiraTheme.radius.shapeMd,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KiraBadgeGroup(
    modifier: Modifier = Modifier,
    gap: KiraBadgeGroupGap = KiraBadgeGroupGap.Normal,
    orientation: KiraBadgeGroupOrientation = KiraBadgeGroupOrientation.Horizontal,
    content: @Composable () -> Unit,
) {
    when (orientation) {
        KiraBadgeGroupOrientation.Horizontal -> FlowRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(gap.spacing),
            verticalArrangement = Arrangement.spacedBy(gap.spacing),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            content()
        }
        KiraBadgeGroupOrientation.Vertical -> Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(gap.spacing),
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KiraTagInput(
    tags: List<String>,
    onTagsChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    suggestions: List<String> = emptyList(),
    placeholder: String = "Add tag…",
    maxTags: Int = Int.MAX_VALUE,
    enabled: Boolean = true,
    size: KiraBadgeSize = KiraBadgeSize.Sm,
    ariaLabel: String = "Tags",
) {
    val colors = KiraTheme.colors
    var draft by rememberSaveable { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    var popupDismissed by remember { mutableStateOf(false) }

    fun commit(next: List<String>) {
        val split = next.flatMap { it.split(",") }.map { it.trim() }
        val seen = LinkedHashSet<String>()
        val deduped = buildList {
            for (item in split) {
                if (item.isEmpty()) continue
                val key = item.lowercase()
                if (seen.add(key)) add(item)
            }
        }
        val capped = if (maxTags == Int.MAX_VALUE) deduped else deduped.take(maxTags.coerceAtLeast(0))
        onTagsChange(capped)
    }

    fun addTag(raw: String) {
        if (!enabled || tags.size >= maxTags) return
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return
        commit(tags + trimmed)
        draft = ""
    }

    val query = draft.trim().lowercase()
    val filteredSuggestions = remember(draft, suggestions, tags) {
        val existing = tags.map { it.lowercase() }.toSet()
        val matching = suggestions.filter { s ->
            s.lowercase() !in existing && (query.isEmpty() || s.lowercase().contains(query))
        }
        val typed = draft.trim()
        val all = buildList {
            addAll(matching)
            if (typed.isNotEmpty() && typed.lowercase() !in existing && matching.none { it.equals(typed, ignoreCase = true) }) {
                add(typed)
            }
        }
        val seen = LinkedHashSet<String>()
        all.filter { seen.add(it.lowercase()) }
    }

    val showPopup = enabled && isFocused && !popupDismissed && tags.size < maxTags &&
        (suggestions.isNotEmpty() || draft.isNotBlank())

    val containerShape = KiraTheme.radius.shapeLg
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) colors.ring else colors.border,
        animationSpec = KiraMotion.fastTween(),
        label = "TagInput border",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.50f)
            .semantics {
                contentDescription = ariaLabel
                if (!enabled) disabled()
            },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(containerShape)
                .background(colors.background)
                .border(if (isFocused) 2.dp else 1.dp, borderColor, containerShape)
                .padding(8.dp),
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                tags.forEachIndexed { index, tag ->
                    KiraTagBadge(
                        label = tag,
                        variant = KiraBadgeVariant.Secondary,
                        size = size,
                        removable = true,
                        enabled = enabled,
                        onRemove = {
                            onTagsChange(tags.filterIndexed { i, _ -> i != index })
                        },
                    )
                }
                Box(
                    modifier = Modifier
                        .heightIn(min = 24.dp)
                        .widthIn(min = 96.dp)
                        .weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (draft.isEmpty() && tags.isEmpty()) {
                        BasicText(
                            text = placeholder,
                            style = KiraTheme.typography.body.copy(color = colors.mutedForeground),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    BasicTextField(
                        value = draft,
                        onValueChange = { updated ->
                            popupDismissed = false
                            if (updated.contains(',')) {
                                val parts = updated.split(",")
                                val toCommit = parts.dropLast(1).filter { it.isNotBlank() }
                                if (toCommit.isNotEmpty()) {
                                    commit(tags + toCommit)
                                }
                                draft = parts.last()
                            } else {
                                draft = updated
                            }
                        },
                        enabled = enabled && tags.size < maxTags,
                        singleLine = true,
                        textStyle = KiraTheme.typography.body.copy(color = colors.foreground),
                        cursorBrush = SolidColor(colors.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { addTag(draft) }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { state ->
                                isFocused = state.isFocused
                                if (state.isFocused) popupDismissed = false
                            }
                            .onPreviewKeyEvent { event ->
                                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                                when (event.key) {
                                    Key.Enter -> {
                                        if (draft.isNotBlank()) {
                                            addTag(draft)
                                            true
                                        } else {
                                            false
                                        }
                                    }
                                    Key.Backspace -> {
                                        if (draft.isEmpty() && tags.isNotEmpty() && enabled) {
                                            onTagsChange(tags.dropLast(1))
                                            true
                                        } else {
                                            false
                                        }
                                    }
                                    Key.Escape -> {
                                        popupDismissed = true
                                        true
                                    }
                                    else -> false
                                }
                            },
                    )
                }
            }
        }

        if (showPopup) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(containerShape)
                    .background(colors.popover)
                    .border(1.dp, colors.border, containerShape)
                    .padding(4.dp),
            ) {
                if (filteredSuggestions.isEmpty()) {
                    BasicText(
                        text = "No matching tags",
                        style = KiraTheme.typography.body.copy(color = colors.mutedForeground),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    )
                } else {
                    filteredSuggestions.take(8).forEach { suggestion ->
                        TagSuggestionItem(
                            text = suggestion,
                            onClick = { addTag(suggestion) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TagSuggestionItem(
    text: String,
    onClick: () -> Unit,
) {
    val colors = KiraTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val highlighted = hovered || focused

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(KiraTheme.radius.shapeMd)
            .background(if (highlighted) colors.muted else Color.Transparent)
            .pointerHoverIcon(PointerIcon.Hand)
            .hoverable(interactionSource)
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        BasicText(
            text = text,
            style = KiraTheme.typography.body.copy(color = colors.popoverForeground),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
