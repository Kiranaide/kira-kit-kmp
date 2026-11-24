package com.kirakit.components.switch

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kirakit.components.platform.KiraHapticFeedbackType
import com.kirakit.components.platform.rememberKiraHaptics
import com.kirakit.components.toast.LocalKiraToastController
import com.kirakit.components.tooltip.KiraSimpleTooltip
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography
import kotlinx.coroutines.launch

enum class KiraSwitchSize(
    internal val trackWidth: Dp,
    internal val trackHeight: Dp,
    internal val thumbSize: Dp,
    internal val trackPadding: Dp,
    internal val iconSize: Dp,
) {
    Sm(40.dp, 24.dp, 16.dp, 4.dp, 10.dp),
    Default(44.dp, 24.dp, 20.dp, 2.dp, 12.dp),
    Lg(52.dp, 28.dp, 24.dp, 2.dp, 14.dp),
}

enum class KiraSwitchColor {
    Default,
    Success,
    Destructive,
    Warning,
}

enum class KiraSwitchLabelPosition {
    Start,
    End,
}

enum class KiraSwitchGroupOrientation {
    Vertical,
    Horizontal,
}

private val LocalSwitchGroupDisabled = compositionLocalOf { false }

/**
 * Accessible toggle switch aligned with kira-kit-v2's `Switch`.
 *
 * - Compact visual track (`40x24`, `44x24`, `52x28`) with a `>= 44.dp` touch hit area.
 * - Spring-animated thumb slide + press scale, cross-fading `thumbIcon` / `thumbIconChecked`,
 *   loading spinner, error state & message, `readOnly` focusable mode, `disabledReason` tooltip,
 *   and `optimistic` async `onCommit` with automatic error rollback + toast.
 */
@Composable
fun KiraSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: KiraSwitchSize = KiraSwitchSize.Default,
    color: KiraSwitchColor = KiraSwitchColor.Default,
    label: String? = null,
    description: String? = null,
    labelPosition: KiraSwitchLabelPosition = KiraSwitchLabelPosition.End,
    required: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    loading: Boolean = false,
    error: Boolean = false,
    errorMessage: String? = null,
    thumbIcon: (@Composable (Color) -> Unit)? = null,
    thumbIconChecked: (@Composable (Color) -> Unit)? = null,
    optimistic: Boolean = false,
    onCommit: (suspend (Boolean) -> Result<Unit>)? = null,
    commitErrorTitle: String = "Couldn't save that",
    disabledReason: String? = null,
    contentDescription: String? = label,
) {
    val colors = KiraTheme.colors
    val haptics = rememberKiraHaptics()
    val toast = LocalKiraToastController.current
    val scope = rememberCoroutineScope()
    val groupDisabled = LocalSwitchGroupDisabled.current

    var pendingChecked by remember { mutableStateOf<Boolean?>(null) }
    val effectiveChecked = pendingChecked ?: checked
    val busy = loading || pendingChecked != null
    val effectiveDisabled = !enabled || groupDisabled || loading
    val interactive = !effectiveDisabled && !readOnly && !busy
    val isInvalid = error || !errorMessage.isNullOrBlank()

    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val pressed by interactionSource.collectIsPressedAsState()

    val checkedTrackColor = when (color) {
        KiraSwitchColor.Default -> colors.primary
        KiraSwitchColor.Success -> colors.success
        KiraSwitchColor.Destructive -> colors.destructive
        KiraSwitchColor.Warning -> colors.warning
    }

    val trackBgState = animateColorAsState(
        targetValue = when {
            isInvalid -> colors.destructive
            effectiveChecked -> checkedTrackColor
            else -> colors.mutedForeground.copy(alpha = 0.30f)
        },
        animationSpec = KiraMotion.fastTween(),
        label = "SwitchTrackBg",
    )

    val maxTravel = size.trackWidth - size.thumbSize - size.trackPadding * 2
    val thumbOffsetXState = animateDpAsState(
        targetValue = if (effectiveChecked) maxTravel else 0.dp,
        animationSpec = KiraMotion.bouncySpring(),
        label = "SwitchThumbX",
    )
    val thumbScaleState = animateFloatAsState(
        targetValue = if (interactive && pressed) 0.92f else 1f,
        animationSpec = KiraMotion.fastTween(),
        label = "SwitchThumbScale",
    )

    fun handleToggle(next: Boolean) {
        if (!interactive) return
        haptics.perform(KiraHapticFeedbackType.Selection)
        if (optimistic && onCommit != null) {
            pendingChecked = next
            scope.launch {
                val res = runCatching { onCommit(next).getOrThrow() }
                pendingChecked = null
                res.onSuccess {
                    onCheckedChange(next)
                }.onFailure {
                    toast.error(commitErrorTitle, description = "The change was reverted.")
                }
            }
        } else {
            onCheckedChange(next)
        }
    }

    val trackNode: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = 44.dp, minHeight = 44.dp)
                .then(if (interactive) Modifier.pointerHoverIcon(PointerIcon.Hand) else Modifier)
                .hoverable(interactionSource, enabled = !effectiveDisabled)
                .toggleable(
                    value = effectiveChecked,
                    enabled = !effectiveDisabled,
                    role = Role.Switch,
                    interactionSource = interactionSource,
                    indication = null,
                    onValueChange = ::handleToggle,
                )
                .semantics {
                    if (contentDescription != null) this.contentDescription = contentDescription
                    stateDescription = if (effectiveChecked) "On" else "Off"
                    if (effectiveDisabled) disabled()
                    if (isInvalid) error(errorMessage ?: "Invalid switch")
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(width = size.trackWidth, height = size.trackHeight)
                    .then(if (effectiveDisabled) Modifier.graphicsLayer { alpha = 0.50f } else Modifier)
                    .clip(CircleShape)
                    .drawBehind { drawRect(trackBgState.value) }
                    .then(if (focused) Modifier.border(2.dp, colors.ring, CircleShape) else Modifier)
                    .padding(size.trackPadding),
                contentAlignment = Alignment.CenterStart,
            ) {
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = thumbOffsetXState.value.toPx()
                            val s = thumbScaleState.value
                            scaleX = s
                            scaleY = s
                        }
                        .size(size.thumbSize)
                        .shadow(2.dp, CircleShape, clip = false)
                        .clip(CircleShape)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (busy) {
                        SwitchThumbSpinner(color = colors.mutedForeground, size = size.iconSize)
                    } else if (thumbIcon != null || thumbIconChecked != null) {
                        val iconTint = if (effectiveChecked) checkedTrackColor else colors.mutedForeground
                        val activeIcon = if (effectiveChecked) (thumbIconChecked ?: thumbIcon) else thumbIcon
                        if (activeIcon != null) {
                            Box(Modifier.size(size.iconSize), contentAlignment = Alignment.Center) {
                                activeIcon(iconTint)
                            }
                        }
                    }
                }
            }
        }
    }

    val hasContent = label != null || description != null || !errorMessage.isNullOrBlank()

    val labelNode: @Composable () -> Unit = {
        Column(
            modifier = Modifier.padding(top = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (label != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    BasicText(
                        text = label,
                        style = KiraTheme.typography.label.copy(
                            color = if (effectiveDisabled) colors.foreground.copy(alpha = 0.7f) else colors.foreground,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                    if (required) {
                        BasicText(
                            text = "*",
                            style = KiraTheme.typography.label.copy(color = colors.destructive),
                        )
                    }
                }
            }
            if (description != null) {
                BasicText(
                    text = description,
                    style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                )
            }
            if (!errorMessage.isNullOrBlank()) {
                BasicText(
                    text = errorMessage,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                    style = KiraTheme.typography.bodySmall.copy(color = colors.destructive),
                )
            }
        }
    }

    val fieldRow: @Composable () -> Unit = {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = if (hasContent) Alignment.Top else Alignment.CenterVertically,
        ) {
            if (labelPosition == KiraSwitchLabelPosition.Start && hasContent) {
                Box(Modifier.weight(1f, fill = false)) { labelNode() }
                trackNode()
            } else {
                trackNode()
                if (hasContent) {
                    Box(Modifier.weight(1f, fill = false)) { labelNode() }
                }
            }
        }
    }

    if (effectiveDisabled && !disabledReason.isNullOrBlank()) {
        KiraSimpleTooltip(text = disabledReason) {
            fieldRow()
        }
    } else {
        fieldRow()
    }
}

/**
 * Grouped switches with optional legend label, orientation, and shared `enabled` propagation.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KiraSwitchGroup(
    modifier: Modifier = Modifier,
    label: String? = null,
    orientation: KiraSwitchGroupOrientation = KiraSwitchGroupOrientation.Vertical,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val parentDisabled = LocalSwitchGroupDisabled.current
    val groupDisabled = parentDisabled || !enabled

    CompositionLocalProvider(LocalSwitchGroupDisabled provides groupDisabled) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (label != null) {
                BasicText(
                    text = label,
                    style = KiraTheme.typography.label.copy(
                        color = KiraTheme.colors.foreground,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
            if (orientation == KiraSwitchGroupOrientation.Vertical) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    content()
                }
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun SwitchThumbSpinner(color: Color, size: Dp) {
    val transition = rememberInfiniteTransition(label = "SwitchSpinner")
    val angleState = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing)),
        label = "SwitchSpinnerAngle",
    )
    Box(
        Modifier
            .size(size)
            .clearAndSetSemantics {}
            .drawWithCache {
                val strokeWidth = 1.5.dp.toPx()
                val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                val inset = strokeWidth / 2f
                val radius = this.size.minDimension / 2f - inset
                val topLeft = Offset(inset, inset)
                val arcSize = Size(this.size.width - strokeWidth, this.size.height - strokeWidth)
                val trackColor = color.copy(alpha = 0.25f)
                onDrawBehind {
                    drawCircle(trackColor, radius = radius, style = stroke)
                    drawArc(
                        color = color,
                        startAngle = angleState.value - 90f,
                        sweepAngle = 110f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = stroke,
                    )
                }
            },
    )
}
