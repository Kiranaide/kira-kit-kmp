package com.kirakit.components.radiogroup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kirakit.components.platform.KiraHapticFeedbackType
import com.kirakit.components.platform.rememberKiraHaptics
import com.kirakit.components.skeleton.KiraSkeletonLines
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography

enum class KiraRadioGroupOrientation {
    Vertical,
    Horizontal,
}

enum class KiraRadioSize(
    val outerSize: Dp,
    val dotSize: Dp,
    val segmentFontSize: Int,
) {
    Sm(outerSize = 16.dp, dotSize = 6.dp, segmentFontSize = 12),
    Default(outerSize = 20.dp, dotSize = 8.dp, segmentFontSize = 14),
    Lg(outerSize = 24.dp, dotSize = 10.dp, segmentFontSize = 15),
}

enum class KiraRadioColor {
    Default,
    Success,
    Warning,
    Destructive,
}

enum class KiraRadioGroupLayout {
    List,
    Cards,
    Segmented,
}

@Immutable
data class KiraRadioOption<T>(
    val value: T,
    val label: String,
    val description: String? = null,
    val icon: (@Composable (Color) -> Unit)? = null,
    val metadata: String? = null,
    val disabled: Boolean = false,
    val disabledReason: String? = null,
    val details: (@Composable () -> Unit)? = null,
)

/**
 * Resolves whether selecting [candidate] in [options] is allowed, returning the next active value.
 */
fun <T> resolveRadioSelection(
    options: List<KiraRadioOption<T>>,
    current: T?,
    candidate: T,
): T? {
    val match = options.firstOrNull { it.value == candidate } ?: return current
    return if (match.disabled || match.disabledReason != null) current else candidate
}

/**
 * High-level radio group aligned with kira-kit-v2's `KiraRadioGroup`.
 *
 * Supports `List`, `Cards`, and sliding `Segmented` layouts, vertical/horizontal orientations,
 * 3 sizes, 4 semantic tones, disabled reasons, inline expandable option `details`, and error state.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> KiraRadioGroup(
    options: List<KiraRadioOption<T>>,
    selectedValue: T?,
    onValueChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    description: String? = null,
    layout: KiraRadioGroupLayout = KiraRadioGroupLayout.List,
    orientation: KiraRadioGroupOrientation = KiraRadioGroupOrientation.Vertical,
    size: KiraRadioSize = KiraRadioSize.Default,
    color: KiraRadioColor = KiraRadioColor.Default,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    loading: Boolean = false,
    loadingLabel: String = "Loading options…",
    isError: Boolean = false,
    errorMessage: String? = null,
    emptyMessage: String = "No options available.",
) {
    val colors = KiraTheme.colors
    val interactive = enabled && !readOnly

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                if (isError && errorMessage != null) error(errorMessage)
            },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (label != null) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                BasicText(
                    text = label,
                    modifier = Modifier.semantics { heading() },
                    style = KiraTheme.typography.label.copy(
                        color = colors.foreground,
                        fontWeight = FontWeight.Medium,
                    ),
                )
                if (description != null) {
                    BasicText(
                        text = description,
                        style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                    )
                }
            }
        }

        if (loading) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BasicText(
                    text = loadingLabel,
                    style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                )
                KiraSkeletonLines(count = 2, lastLineWidthFraction = 0.7f)
            }
        } else if (options.isEmpty()) {
            BasicText(
                text = emptyMessage,
                style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
            )
        } else {
            when (layout) {
                KiraRadioGroupLayout.Segmented -> {
                    KiraRadioButtonGroup(
                        options = options,
                        selectedValue = selectedValue,
                        onValueChange = { if (interactive) onValueChange(it) },
                        size = size,
                        enabled = enabled,
                        isError = isError,
                    )
                }
                KiraRadioGroupLayout.Cards -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().selectableGroup(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        options.forEach { option ->
                            val isSelected = option.value == selectedValue
                            val optionEnabled = enabled && !option.disabled && option.disabledReason == null
                            KiraRadioCard(
                                selected = isSelected,
                                onClick = { if (interactive) onValueChange(option.value) },
                                label = option.label,
                                description = option.description,
                                metadata = option.metadata,
                                icon = option.icon,
                                disabledReason = option.disabledReason,
                                enabled = optionEnabled,
                                isError = isError,
                                details = option.details,
                            )
                        }
                    }
                }
                KiraRadioGroupLayout.List -> {
                    if (orientation == KiraRadioGroupOrientation.Horizontal) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().selectableGroup(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            options.forEach { option ->
                                val isSelected = option.value == selectedValue
                                val optionEnabled = enabled && !option.disabled && option.disabledReason == null
                                KiraRadioWithLabel(
                                    selected = isSelected,
                                    onClick = { if (interactive) onValueChange(option.value) },
                                    label = option.label,
                                    description = option.description,
                                    disabledReason = option.disabledReason,
                                    size = size,
                                    color = color,
                                    enabled = optionEnabled,
                                    isError = isError,
                                    fillWidth = false,
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth().selectableGroup(),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            options.forEach { option ->
                                val isSelected = option.value == selectedValue
                                val optionEnabled = enabled && !option.disabled && option.disabledReason == null
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    KiraRadioWithLabel(
                                        selected = isSelected,
                                        onClick = { if (interactive) onValueChange(option.value) },
                                        label = option.label,
                                        description = option.description,
                                        disabledReason = option.disabledReason,
                                        size = size,
                                        color = color,
                                        enabled = optionEnabled,
                                        isError = isError,
                                    )
                                    if (option.details != null) {
                                        AnimatedVisibility(
                                            visible = isSelected,
                                            enter = expandVertically(tween(KiraMotion.RouteFadeMs)) + fadeIn(tween(KiraMotion.RouteFadeMs)),
                                            exit = shrinkVertically(tween(130)) + fadeOut(tween(110)),
                                        ) {
                                            Box(modifier = Modifier.padding(start = 32.dp, bottom = 6.dp)) {
                                                option.details.invoke()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isError && errorMessage != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                RadioErrorGlyph(color = colors.destructive)
                BasicText(
                    text = errorMessage,
                    style = KiraTheme.typography.bodySmall.copy(color = colors.destructive),
                )
            }
        }
    }
}

/**
 * Standalone circular radio button control with animated spring dot & `>= 44.dp` touch target.
 */
@Composable
fun KiraRadioItem(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: KiraRadioSize = KiraRadioSize.Default,
    color: KiraRadioColor = KiraRadioColor.Default,
    enabled: Boolean = true,
    isError: Boolean = false,
    contentDescription: String? = null,
) {
    val haptics = rememberKiraHaptics()
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()

    Box(
        modifier = modifier
            .size(44.dp)
            .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
            .hoverable(interaction, enabled = enabled)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                interactionSource = interaction,
                indication = null,
                onClick = {
                    haptics.perform(KiraHapticFeedbackType.Selection)
                    onClick()
                },
            )
            .semantics {
                if (!enabled) disabled()
                if (contentDescription != null) this.contentDescription = contentDescription
            },
        contentAlignment = Alignment.Center,
    ) {
        RadioCircleIndicator(
            selected = selected,
            size = size,
            color = color,
            enabled = enabled,
            focused = focused,
            isError = isError,
        )
    }
}

/**
 * Labeled radio option aligned with kira-kit-v2's `RadioWithLabel`.
 */
@Composable
fun KiraRadioWithLabel(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    disabledReason: String? = null,
    size: KiraRadioSize = KiraRadioSize.Default,
    color: KiraRadioColor = KiraRadioColor.Default,
    enabled: Boolean = true,
    isError: Boolean = false,
    fillWidth: Boolean = true,
) {
    val colors = KiraTheme.colors
    val haptics = rememberKiraHaptics()
    val effectiveEnabled = enabled && disabledReason == null
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()

    Row(
        modifier = modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .defaultMinSize(minHeight = 44.dp)
            .alpha(if (effectiveEnabled) 1f else 0.55f)
            .pointerHoverIcon(if (effectiveEnabled) PointerIcon.Hand else PointerIcon.Default)
            .hoverable(interaction, enabled = effectiveEnabled)
            .selectable(
                selected = selected,
                enabled = effectiveEnabled,
                role = Role.RadioButton,
                interactionSource = interaction,
                indication = null,
                onClick = {
                    haptics.perform(KiraHapticFeedbackType.Selection)
                    onClick()
                },
            )
            .semantics(mergeDescendants = true) {
                if (!effectiveEnabled) disabled()
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.padding(top = 1.dp)) {
            RadioCircleIndicator(
                selected = selected,
                size = size,
                color = color,
                enabled = effectiveEnabled,
                focused = focused,
                isError = isError,
            )
        }
        Column(
            modifier = if (fillWidth) Modifier.weight(1f) else Modifier,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            BasicText(
                text = label,
                style = KiraTheme.typography.body.copy(
                    color = colors.foreground,
                    fontWeight = FontWeight.Medium,
                ),
            )
            if (description != null) {
                BasicText(
                    text = description,
                    style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                )
            }
            if (disabledReason != null) {
                BasicText(
                    text = "Unavailable: $disabledReason",
                    style = KiraTheme.typography.caption.copy(color = colors.mutedForeground),
                )
            }
        }
    }
}

/**
 * Rich selectable card option aligned with kira-kit-v2's `RadioCard`.
 */
@Composable
fun KiraRadioCard(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    metadata: String? = null,
    icon: (@Composable (Color) -> Unit)? = null,
    disabledReason: String? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    details: (@Composable () -> Unit)? = null,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val shape = radius.shapeLg
    val haptics = rememberKiraHaptics()
    val effectiveEnabled = enabled && disabledReason == null
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val focused by interaction.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> colors.destructive
            focused -> colors.ring
            selected -> colors.primary
            hovered && effectiveEnabled -> colors.primary.copy(alpha = 0.5f)
            else -> colors.input
        },
        animationSpec = KiraMotion.fastTween(),
    )
    val bgColor by animateColorAsState(
        targetValue = when {
            selected -> colors.primary.copy(alpha = 0.06f)
            hovered && effectiveEnabled -> colors.muted.copy(alpha = 0.5f)
            else -> colors.background
        },
        animationSpec = KiraMotion.fastTween(),
    )
    val checkScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.7f,
        animationSpec = KiraMotion.bouncySpring(),
    )

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 56.dp)
                .alpha(if (effectiveEnabled) 1f else 0.55f)
                .clip(shape)
                .background(bgColor, shape)
                .border(if (selected || focused) 1.5.dp else 1.dp, borderColor, shape)
                .hoverable(interaction, enabled = effectiveEnabled)
                .pointerHoverIcon(if (effectiveEnabled) PointerIcon.Hand else PointerIcon.Default)
                .selectable(
                    selected = selected,
                    enabled = effectiveEnabled,
                    role = Role.RadioButton,
                    interactionSource = interaction,
                    indication = null,
                    onClick = {
                        haptics.perform(KiraHapticFeedbackType.Selection)
                        onClick()
                    },
                )
                .semantics(mergeDescendants = true) {
                    if (!effectiveEnabled) disabled()
                }
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicText(
                        text = label,
                        style = KiraTheme.typography.body.copy(
                            color = colors.foreground,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                    if (metadata != null) {
                        BasicText(
                            text = metadata,
                            style = KiraTheme.typography.caption.copy(
                                color = colors.mutedForeground,
                                fontWeight = FontWeight.Medium,
                            ),
                        )
                    }
                }
                if (description != null) {
                    BasicText(
                        text = description,
                        style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                    )
                }
                if (disabledReason != null) {
                    BasicText(
                        text = "Unavailable: $disabledReason",
                        style = KiraTheme.typography.caption.copy(color = colors.mutedForeground),
                    )
                }
            }

            if (icon != null) {
                Box(
                    modifier = Modifier.padding(top = 2.dp).size(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    icon(if (selected) colors.primary else colors.mutedForeground)
                }
            }

            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (selected) colors.primary else Color.Transparent)
                    .border(1.5.dp, if (selected) colors.primary else colors.input, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    RadioCheckGlyph(
                        color = colors.primaryForeground,
                        modifier = Modifier.scale(checkScale),
                    )
                }
            }
        }

        if (details != null) {
            AnimatedVisibility(
                visible = selected,
                enter = expandVertically(tween(KiraMotion.RouteFadeMs)) + fadeIn(tween(KiraMotion.RouteFadeMs)),
                exit = shrinkVertically(tween(130)) + fadeOut(tween(110)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(radius.shapeMd)
                        .background(colors.muted.copy(alpha = 0.35f))
                        .border(1.dp, colors.border, radius.shapeMd)
                        .padding(12.dp),
                ) {
                    details()
                }
            }
        }
    }
}

/**
 * Horizontal segmented radio button group with a smoothly sliding active pill background
 * (aligned with kira-kit-v2's `RadioButtonGroup` + `RadioButton`).
 */
@Composable
fun <T> KiraRadioButtonGroup(
    options: List<KiraRadioOption<T>>,
    selectedValue: T?,
    onValueChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    size: KiraRadioSize = KiraRadioSize.Default,
    enabled: Boolean = true,
    isError: Boolean = false,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val density = LocalDensity.current
    val haptics = rememberKiraHaptics()

    val itemBounds = remember { mutableStateMapOf<Int, Pair<IntOffset, IntSize>>() }
    val selectedIndex = options.indexOfFirst { it.value == selectedValue }
    val activeBounds = itemBounds[selectedIndex]

    val pillXState = animateDpAsState(
        targetValue = with(density) { (activeBounds?.first?.x ?: 0).toDp() },
        animationSpec = tween(200, easing = KiraMotion.EaseOutStrong),
        label = "RadioPillX",
    )
    val pillWidthState = animateDpAsState(
        targetValue = with(density) { (activeBounds?.second?.width ?: 0).toDp() },
        animationSpec = tween(200, easing = KiraMotion.EaseOutStrong),
        label = "RadioPillWidth",
    )
    val pillHeightState = animateDpAsState(
        targetValue = with(density) { (activeBounds?.second?.height ?: 0).toDp() },
        animationSpec = tween(200, easing = KiraMotion.EaseOutStrong),
        label = "RadioPillHeight",
    )

    Box(
        modifier = modifier
            .clip(radius.shapeLg)
            .background(colors.muted)
            .border(
                width = 1.dp,
                color = if (isError) colors.destructive else colors.border.copy(alpha = 0.6f),
                shape = radius.shapeLg,
            )
            .padding(4.dp),
    ) {
        if (activeBounds != null) {
            val pillRadius = radius.md
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawBehind {
                        val w = pillWidthState.value.toPx()
                        val h = pillHeightState.value.toPx()
                        if (w > 0f && h > 0f) {
                            val r = pillRadius.toPx()
                            drawRoundRect(
                                color = colors.background,
                                topLeft = Offset(pillXState.value.toPx(), 0f),
                                size = Size(w, h),
                                cornerRadius = CornerRadius(r, r),
                            )
                        }
                    },
            )
        }

        Row(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            options.forEachIndexed { index, option ->
                val selected = index == selectedIndex
                val itemEnabled = enabled && !option.disabled && option.disabledReason == null
                val interaction = remember { MutableInteractionSource() }
                val hovered by interaction.collectIsHoveredAsState()

                val textColor by animateColorAsState(
                    targetValue = when {
                        selected -> colors.foreground
                        hovered && itemEnabled -> colors.foreground
                        else -> colors.mutedForeground
                    },
                    animationSpec = KiraMotion.fastTween(),
                )

                val hPad = when (size) {
                    KiraRadioSize.Sm -> 10.dp
                    KiraRadioSize.Default -> 14.dp
                    KiraRadioSize.Lg -> 18.dp
                }

                Box(
                    modifier = Modifier
                        .onGloballyPositioned { coords ->
                            val pos = coords.positionInParent()
                            val next = IntOffset(pos.x.toInt(), pos.y.toInt()) to coords.size
                            if (itemBounds[index] != next) {
                                itemBounds[index] = next
                            }
                        }
                        .heightIn(min = 44.dp)
                        .alpha(if (itemEnabled) 1f else 0.5f)
                        .clip(radius.shapeMd)
                        .hoverable(interaction, enabled = itemEnabled)
                        .pointerHoverIcon(if (itemEnabled) PointerIcon.Hand else PointerIcon.Default)
                        .selectable(
                            selected = selected,
                            enabled = itemEnabled,
                            role = Role.RadioButton,
                            interactionSource = interaction,
                            indication = null,
                            onClick = {
                                haptics.perform(KiraHapticFeedbackType.Selection)
                                onValueChange(option.value)
                            },
                        )
                        .padding(horizontal = hPad, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = option.label,
                        style = KiraTheme.typography.label.copy(
                            color = textColor,
                            fontSize = size.segmentFontSize.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun RadioCircleIndicator(
    selected: Boolean,
    size: KiraRadioSize,
    color: KiraRadioColor,
    enabled: Boolean,
    focused: Boolean,
    isError: Boolean,
) {
    val colors = KiraTheme.colors
    val accentColor = when (color) {
        KiraRadioColor.Default -> colors.primary
        KiraRadioColor.Success -> colors.success
        KiraRadioColor.Warning -> colors.warning
        KiraRadioColor.Destructive -> colors.destructive
    }
    val borderColorState = animateColorAsState(
        targetValue = when {
            isError -> colors.destructive
            focused -> colors.ring
            selected -> accentColor
            else -> colors.input
        },
        animationSpec = KiraMotion.fastTween(),
        label = "RadioBorder",
    )
    val dotScaleState = animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = KiraMotion.bouncySpring(),
        label = "RadioDotScale",
    )
    val dotColor = if (isError) colors.destructive else accentColor

    Box(
        modifier = Modifier
            .size(size.outerSize)
            .then(if (enabled) Modifier else Modifier.graphicsLayer { alpha = 0.55f })
            .clip(CircleShape)
            .background(colors.background)
            .drawWithCache {
                val strokeWidth = 2.dp.toPx()
                val stroke = Stroke(width = strokeWidth)
                val borderRadius = (this.size.minDimension - strokeWidth) / 2f
                val maxDotRadius = size.dotSize.toPx() / 2f
                onDrawBehind {
                    drawCircle(color = borderColorState.value, radius = borderRadius, style = stroke)
                    val s = dotScaleState.value
                    if (s > 0.01f) {
                        drawCircle(color = dotColor, radius = maxDotRadius * s)
                    }
                }
            },
    )
}

@Composable
private fun RadioCheckGlyph(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(12.dp)
            .clearAndSetSemantics {}
            .drawWithCache {
                val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                val path = Path().apply {
                    moveTo(size.width * 0.18f, size.height * 0.52f)
                    lineTo(size.width * 0.42f, size.height * 0.76f)
                    lineTo(size.width * 0.84f, size.height * 0.24f)
                }
                onDrawBehind { drawPath(path, color = color, style = stroke) }
            },
    )
}

@Composable
private fun RadioErrorGlyph(color: Color) {
    Box(
        Modifier
            .size(14.dp)
            .clearAndSetSemantics {}
            .drawWithCache {
                val sw = 1.6.dp.toPx()
                val stroke = Stroke(width = sw, cap = StrokeCap.Round)
                val r = size.minDimension * 0.44f
                val dotR = 1.dp.toPx()
                val lineStart = Offset(size.width * 0.5f, size.height * 0.28f)
                val lineEnd = Offset(size.width * 0.5f, size.height * 0.56f)
                val dotCenter = Offset(size.width * 0.5f, size.height * 0.72f)
                onDrawBehind {
                    drawCircle(color = color, radius = r, style = stroke)
                    drawLine(color, lineStart, lineEnd, strokeWidth = sw, cap = StrokeCap.Round)
                    drawCircle(color = color, radius = dotR, center = dotCenter)
                }
            },
    )
}
