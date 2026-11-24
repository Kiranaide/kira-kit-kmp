package com.kirakit.components.toggle

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kirakit.components.platform.KiraHapticFeedbackType
import com.kirakit.components.platform.rememberKiraHaptics
import com.kirakit.components.toast.LocalKiraToastController
import com.kirakit.components.tooltip.KiraSimpleTooltip
import com.kirakit.components.tooltip.KiraTooltipSide
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.shapeFull
import com.kirakit.theme.typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class KiraToggleVariant {
    Default,
    Outline,
    Ghost,
    Solid,
    Success,
    Warning,
    Destructive,
}

enum class KiraToggleSize(
    internal val visualHeight: Dp,
    internal val horizontalPadding: Dp,
    internal val fontSize: Int,
    internal val iconSize: Dp,
    internal val isIconOnly: Boolean = false,
) {
    Sm(32.dp, 8.dp, 12, 14.dp),
    Default(36.dp, 12.dp, 14, 16.dp),
    Lg(40.dp, 16.dp, 15, 20.dp),
    Icon(36.dp, 0.dp, 14, 16.dp, isIconOnly = true),
    IconSm(32.dp, 0.dp, 12, 14.dp, isIconOnly = true),
    IconLg(40.dp, 0.dp, 15, 20.dp, isIconOnly = true),
}

enum class KiraToggleRadius {
    Default,
    Pill,
    Square,
}

enum class KiraToggleIndicatorPosition {
    Left,
    Right,
}

private enum class KiraToggleAsyncStatus {
    Idle,
    Loading,
    Success,
}

/**
 * Accessible two-state toggle button aligned with kira-kit-v2's `Toggle` & `KiraToggle`.
 *
 * Supports all 7 variants (`Default`, `Outline`, `Ghost`, `Solid`, `Success`, `Warning`, `Destructive`),
 * 6 sizes, 3 radii, check indicator (`showIndicator`), `pressedIcon` swap with spring pop,
 * keyboard `shortcut` chip, `tooltip` & `disabledReason` tooltip, optional side `label`/`description`,
 * and optimistic async commits (`onToggleAsync`) with checkmark flash and error rollback toast.
 */
@Composable
fun KiraToggle(
    pressed: Boolean,
    onPressedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    variant: KiraToggleVariant = KiraToggleVariant.Default,
    size: KiraToggleSize = KiraToggleSize.Default,
    radius: KiraToggleRadius = KiraToggleRadius.Default,
    enabled: Boolean = true,
    loading: Boolean = false,
    showIndicator: Boolean = false,
    indicatorPosition: KiraToggleIndicatorPosition = KiraToggleIndicatorPosition.Left,
    icon: (@Composable (Color) -> Unit)? = null,
    pressedIcon: (@Composable (Color) -> Unit)? = null,
    shortcut: String? = null,
    tooltip: String? = null,
    tooltipSide: KiraTooltipSide = KiraTooltipSide.Top,
    disabledReason: String? = null,
    label: String? = null,
    description: String? = null,
    onToggleAsync: (suspend (Boolean) -> Result<Unit>)? = null,
    showSuccess: Boolean = true,
    successDurationMillis: Long = 1_200L,
    errorMessage: String = "Couldn't update toggle",
    contentDescription: String? = text ?: label,
    content: (@Composable (Color) -> Unit)? = null,
) {
    val colors = KiraTheme.colors
    val haptics = rememberKiraHaptics()
    val toast = LocalKiraToastController.current
    val scope = rememberCoroutineScope()

    var optimisticPressed by remember(pressed) { mutableStateOf(pressed) }
    var asyncStatus by remember { mutableStateOf(KiraToggleAsyncStatus.Idle) }

    val effectivePressed = if (onToggleAsync != null && asyncStatus == KiraToggleAsyncStatus.Loading) {
        optimisticPressed
    } else {
        pressed
    }
    val effectiveLoading = loading || asyncStatus == KiraToggleAsyncStatus.Loading
    val interactive = enabled && !effectiveLoading

    LaunchedEffect(asyncStatus, successDurationMillis) {
        if (asyncStatus == KiraToggleAsyncStatus.Success) {
            delay(successDurationMillis.coerceAtLeast(300L))
            asyncStatus = KiraToggleAsyncStatus.Idle
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val isPointerPressed by interactionSource.collectIsPressedAsState()
    val focused by interactionSource.collectIsFocusedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (interactive && isPointerPressed) 0.95f else 1f,
        animationSpec = KiraMotion.fastTween(),
        label = "TogglePressScale",
    )

    val iconPopScale by animateFloatAsState(
        targetValue = if (effectivePressed) 1f else 0.92f,
        animationSpec = KiraMotion.bouncySpring(),
        label = "ToggleIconPop",
    )

    val shape = when (radius) {
        KiraToggleRadius.Default -> KiraTheme.radius.shapeMd
        KiraToggleRadius.Pill -> KiraTheme.radius.shapeFull
        KiraToggleRadius.Square -> RectangleShape
    }

    val targetBg = when {
        effectivePressed -> when (variant) {
            KiraToggleVariant.Default, KiraToggleVariant.Outline, KiraToggleVariant.Ghost -> colors.accent
            KiraToggleVariant.Solid -> colors.primary
            KiraToggleVariant.Success -> colors.success
            KiraToggleVariant.Warning -> colors.warning
            KiraToggleVariant.Destructive -> colors.destructive
        }
        hovered && interactive -> when (variant) {
            KiraToggleVariant.Outline, KiraToggleVariant.Ghost -> colors.accent
            else -> colors.muted
        }
        variant == KiraToggleVariant.Solid -> colors.muted
        else -> Color.Transparent
    }

    val targetFg = when {
        effectivePressed -> when (variant) {
            KiraToggleVariant.Default, KiraToggleVariant.Outline, KiraToggleVariant.Ghost -> colors.accentForeground
            KiraToggleVariant.Solid -> colors.primaryForeground
            KiraToggleVariant.Success -> colors.successForeground
            KiraToggleVariant.Warning -> colors.warningForeground
            KiraToggleVariant.Destructive -> colors.destructiveForeground
        }
        hovered && interactive -> when (variant) {
            KiraToggleVariant.Outline, KiraToggleVariant.Ghost -> colors.accentForeground
            else -> colors.mutedForeground
        }
        variant == KiraToggleVariant.Solid -> colors.mutedForeground
        else -> colors.foreground
    }

    val bgColor by animateColorAsState(targetBg, KiraMotion.fastTween(), label = "ToggleBg")
    val fgColor by animateColorAsState(targetFg, KiraMotion.fastTween(), label = "ToggleFg")

    fun handleToggle(next: Boolean) {
        if (!interactive) return
        haptics.perform(KiraHapticFeedbackType.Selection)
        if (onToggleAsync != null) {
            optimisticPressed = next
            asyncStatus = KiraToggleAsyncStatus.Loading
            scope.launch {
                val res = runCatching { onToggleAsync(next).getOrThrow() }
                res.onSuccess {
                    onPressedChange(next)
                    asyncStatus = if (showSuccess) KiraToggleAsyncStatus.Success else KiraToggleAsyncStatus.Idle
                }.onFailure {
                    optimisticPressed = pressed
                    asyncStatus = KiraToggleAsyncStatus.Idle
                    toast.error(errorMessage)
                }
            }
        } else {
            onPressedChange(next)
        }
    }

    val visualPadding = if (size.isIconOnly) {
        PaddingValues(0.dp)
    } else {
        PaddingValues(horizontal = size.horizontalPadding)
    }

    val toggleControl: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = 44.dp, minHeight = 44.dp)
                .then(if (interactive) Modifier.pointerHoverIcon(PointerIcon.Hand) else Modifier)
                .hoverable(interactionSource, enabled = interactive)
                .toggleable(
                    value = effectivePressed,
                    enabled = interactive,
                    role = Role.Checkbox,
                    interactionSource = interactionSource,
                    indication = null,
                    onValueChange = ::handleToggle,
                )
                .semantics {
                    if (contentDescription != null) this.contentDescription = contentDescription
                    stateDescription = if (effectivePressed) "Pressed" else "Not pressed"
                    if (!enabled) disabled()
                },
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = pressScale
                        scaleY = pressScale
                    }
                    .then(
                        if (size.isIconOnly) {
                            Modifier.size(size.visualHeight)
                        } else {
                            Modifier.height(size.visualHeight)
                        },
                    )
                    .alpha(if (enabled) 1f else 0.50f)
                    .clip(shape)
                    .background(bgColor, shape)
                    .then(
                        when {
                            focused -> Modifier.border(2.dp, colors.ring, shape)
                            variant == KiraToggleVariant.Outline -> Modifier.border(1.dp, colors.input, shape)
                            else -> Modifier
                        },
                    )
                    .padding(visualPadding),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (indicatorPosition == KiraToggleIndicatorPosition.Left) {
                    ToggleLeadingIndicator(effectiveLoading, effectivePressed, showIndicator, fgColor, size.iconSize)
                }

                when {
                    asyncStatus == KiraToggleAsyncStatus.Success -> {
                        ToggleCheckGlyph(color = fgColor, size = size.iconSize)
                    }
                    effectivePressed && pressedIcon != null -> {
                        Box(Modifier.graphicsLayer { scaleX = iconPopScale; scaleY = iconPopScale }) {
                            pressedIcon(fgColor)
                        }
                    }
                    icon != null -> {
                        Box(Modifier.graphicsLayer { scaleX = iconPopScale; scaleY = iconPopScale }) {
                            icon(fgColor)
                        }
                    }
                }

                if (text != null) {
                    BasicText(
                        text = text,
                        maxLines = 1,
                        style = KiraTheme.typography.label.copy(
                            color = fgColor,
                            fontSize = size.fontSize.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }

                if (content != null) {
                    content(fgColor)
                }

                if (shortcut != null) {
                    Box(
                        modifier = Modifier
                            .height(18.dp)
                            .widthIn(min = 18.dp)
                            .background(fgColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                            .border(1.dp, fgColor.copy(alpha = 0.22f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = shortcut,
                            style = KiraTheme.typography.caption.copy(
                                color = fgColor,
                                fontSize = 10.sp,
                                lineHeight = 12.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                        )
                    }
                }

                if (indicatorPosition == KiraToggleIndicatorPosition.Right) {
                    ToggleLeadingIndicator(effectiveLoading, effectivePressed, showIndicator, fgColor, size.iconSize)
                }
            }
        }
    }

    val wrappedTrigger: @Composable () -> Unit = {
        val activeTip = when {
            !enabled && disabledReason != null -> disabledReason
            tooltip != null -> tooltip
            else -> null
        }
        if (activeTip != null) {
            KiraSimpleTooltip(text = activeTip, side = tooltipSide) {
                toggleControl()
            }
        } else {
            toggleControl()
        }
    }

    if (label != null || description != null) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            wrappedTrigger()
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (label != null) {
                    BasicText(
                        text = label,
                        style = KiraTheme.typography.label.copy(
                            color = if (enabled) colors.foreground else colors.mutedForeground,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }
                if (description != null) {
                    BasicText(
                        text = description,
                        style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                    )
                }
            }
        }
    } else {
        Box(modifier = modifier) {
            wrappedTrigger()
        }
    }
}

@Composable
private fun ToggleLeadingIndicator(
    loading: Boolean,
    pressed: Boolean,
    showIndicator: Boolean,
    color: Color,
    iconSize: Dp,
) {
    if (loading) {
        val transition = rememberInfiniteTransition(label = "ToggleSpinner")
        val angleState = transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(850, easing = LinearEasing)),
            label = "ToggleSpinnerAngle",
        )
        Box(
            Modifier
                .size(iconSize)
                .clearAndSetSemantics {}
                .drawWithCache {
                    val strokeWidth = 1.7.dp.toPx()
                    val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    val inset = strokeWidth / 2f
                    val radius = size.minDimension / 2f - inset
                    val topLeft = Offset(inset, inset)
                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                    val trackColor = color.copy(alpha = 0.25f)
                    onDrawBehind {
                        drawCircle(trackColor, radius = radius, style = stroke)
                        drawArc(
                            color = color,
                            startAngle = angleState.value - 90f,
                            sweepAngle = 100f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = stroke,
                        )
                    }
                },
        )
    } else if (showIndicator && pressed) {
        ToggleCheckGlyph(color = color, size = iconSize)
    }
}

@Composable
private fun ToggleCheckGlyph(color: Color, size: Dp) {
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(this@Canvas.size.width * 0.18f, this@Canvas.size.height * 0.52f)
            lineTo(this@Canvas.size.width * 0.42f, this@Canvas.size.height * 0.76f)
            lineTo(this@Canvas.size.width * 0.82f, this@Canvas.size.height * 0.26f)
        }
        drawPath(path, color, style = stroke)
    }
}
