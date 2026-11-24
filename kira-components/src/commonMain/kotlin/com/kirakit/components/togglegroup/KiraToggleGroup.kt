package com.kirakit.components.togglegroup

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kirakit.components.platform.KiraHapticFeedbackType
import com.kirakit.components.platform.rememberKiraHaptics
import com.kirakit.components.tooltip.KiraSimpleTooltip
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.shapeFull
import com.kirakit.theme.typography

enum class KiraToggleGroupVariant {
    Default,
    Outline,
    Segment,
    Pills,
}

enum class KiraToggleGroupSize(
    internal val visualHeight: Dp,
    internal val horizontalPadding: Dp,
    internal val fontSize: Int,
    internal val iconSize: Dp,
) {
    Sm(28.dp, 8.dp, 12, 14.dp),
    Default(36.dp, 12.dp, 14, 16.dp),
    Lg(40.dp, 16.dp, 15, 20.dp),
}

enum class KiraToggleGroupOrientation {
    Horizontal,
    Vertical,
}

enum class KiraToggleGroupRadius {
    Default,
    Pill,
}

enum class KiraToggleGroupColor {
    Default,
    Success,
    Warning,
    Destructive,
}

enum class KiraToggleGroupSemantics {
    Current,
    Radio,
    Toggle,
}

enum class KiraToggleGroupOverflowMode {
    None,
    Scroll,
    HideLabels,
}

@Immutable
data class KiraToggleGroupItem(
    val value: String,
    val label: String? = null,
    val icon: (@Composable (Color) -> Unit)? = null,
    val colorVariant: KiraToggleGroupColor = KiraToggleGroupColor.Default,
    val disabled: Boolean = false,
    val tooltip: String? = null,
    val disabledReason: String? = null,
    val contentDescription: String? = label,
)

/**
 * Pure selection reducer matching kira-kit-v2's `ToggleGroup` rules:
 * - Single-select keeps one item selected by default (`allowEmpty = false`), vetoing deselection.
 * - Multi-select toggles items independently.
 */
fun resolveToggleGroupSelection(
    current: Set<String>,
    toggled: String,
    multiple: Boolean,
    allowEmpty: Boolean = false,
): Set<String> {
    if (multiple) {
        return if (toggled in current) current - toggled else current + toggled
    }
    if (toggled in current) {
        return if (allowEmpty) emptySet() else current
    }
    return setOf(toggled)
}

/**
 * Grouped toggle / segmented control aligned with kira-kit-v2's `ToggleGroup` & `KiraToggleGroup`.
 *
 * Supports `Default`, `Outline`, `Segment`, and `Pills` variants, single & multiple selection,
 * one-selected-always (`allowEmpty = false`), animated sliding pill (`slidingIndicator`),
 * M3 Expressive `shapeMorph`, `showSelectedIcon` checkmark, `overflowMode` (`Scroll` / `HideLabels`),
 * `semantics` (`Current` / `Radio` / `Toggle`), validation error text, and per-item tooltips.
 */
@Composable
fun KiraToggleGroup(
    items: List<KiraToggleGroupItem>,
    selectedValues: Set<String>,
    onSelectedValuesChange: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
    variant: KiraToggleGroupVariant = KiraToggleGroupVariant.Default,
    size: KiraToggleGroupSize = KiraToggleGroupSize.Default,
    orientation: KiraToggleGroupOrientation = KiraToggleGroupOrientation.Horizontal,
    radius: KiraToggleGroupRadius = KiraToggleGroupRadius.Default,
    multiple: Boolean = false,
    allowEmpty: Boolean = false,
    slidingIndicator: Boolean = false,
    shapeMorph: Boolean = false,
    showSelectedIcon: Boolean = false,
    overflowMode: KiraToggleGroupOverflowMode = KiraToggleGroupOverflowMode.None,
    semanticsMode: KiraToggleGroupSemantics = KiraToggleGroupSemantics.Current,
    enabled: Boolean = true,
    invalid: Boolean = false,
    errorMessage: String? = null,
    contentDescription: String = "Toggle group",
) {
    val colors = KiraTheme.colors
    val density = LocalDensity.current
    val haptics = rememberKiraHaptics()

    val effectiveAllowEmpty = if (!multiple && semanticsMode == KiraToggleGroupSemantics.Radio) false else allowEmpty
    val useSlide = slidingIndicator && !multiple
    val useMorph = shapeMorph && !multiple && !useSlide &&
        variant != KiraToggleGroupVariant.Outline && variant != KiraToggleGroupVariant.Segment
    val hideLabels = overflowMode == KiraToggleGroupOverflowMode.HideLabels

    val itemRects = remember { mutableStateMapOf<String, Pair<IntOffset, IntSize>>() }
    val activeValue = selectedValues.firstOrNull()
    val activeRect = if (activeValue != null) itemRects[activeValue] else null

    val groupShape = when {
        radius == KiraToggleGroupRadius.Pill -> KiraTheme.radius.shapeFull
        variant == KiraToggleGroupVariant.Outline || variant == KiraToggleGroupVariant.Segment -> KiraTheme.radius.shapeLg
        else -> RectangleShape
    }
    val groupBg = when (variant) {
        KiraToggleGroupVariant.Segment -> colors.muted
        else -> Color.Transparent
    }
    val groupPadding = if (variant == KiraToggleGroupVariant.Segment) 4.dp else 0.dp
    val itemSpacing = when (variant) {
        KiraToggleGroupVariant.Default -> 4.dp
        KiraToggleGroupVariant.Pills -> 8.dp
        KiraToggleGroupVariant.Outline, KiraToggleGroupVariant.Segment -> 0.dp
    }

    val itemRole = when {
        multiple -> Role.Checkbox
        semanticsMode == KiraToggleGroupSemantics.Radio -> Role.RadioButton
        else -> Role.Tab
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val scrollMod = if (overflowMode == KiraToggleGroupOverflowMode.Scroll && orientation == KiraToggleGroupOrientation.Horizontal) {
            Modifier.horizontalScroll(rememberScrollState())
        } else {
            Modifier
        }

        Box(
            modifier = scrollMod
                .clip(groupShape)
                .background(groupBg, groupShape)
                .then(
                    when {
                        invalid -> Modifier.border(1.dp, colors.destructive, groupShape)
                        variant == KiraToggleGroupVariant.Outline -> Modifier.border(1.dp, colors.input, groupShape)
                        else -> Modifier
                    },
                )
                .padding(groupPadding)
                .selectableGroup()
                .semantics {
                    this.contentDescription = contentDescription
                    if (invalid) error(errorMessage ?: "Invalid selection")
                },
        ) {
            if (useSlide && activeRect != null) {
                val (pos, sz) = activeRect
                val animXState = animateDpAsState(with(density) { pos.x.toDp() }, KiraMotion.standardTween(), label = "TGSlideX")
                val animYState = animateDpAsState(with(density) { pos.y.toDp() }, KiraMotion.standardTween(), label = "TGSlideY")
                val animWState = animateDpAsState(with(density) { sz.width.toDp() }, KiraMotion.standardTween(), label = "TGSlideW")
                val animHState = animateDpAsState(with(density) { sz.height.toDp() }, KiraMotion.standardTween(), label = "TGSlideH")
                val isPill = radius == KiraToggleGroupRadius.Pill
                val mdRadius = KiraTheme.radius.md

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .drawBehind {
                            val w = animWState.value.toPx()
                            val h = animHState.value.toPx()
                            if (w > 0f && h > 0f) {
                                val r = if (isPill) minOf(w, h) / 2f else mdRadius.toPx()
                                drawRoundRect(
                                    color = colors.background,
                                    topLeft = Offset(animXState.value.toPx(), animYState.value.toPx()),
                                    size = Size(w, h),
                                    cornerRadius = CornerRadius(r, r),
                                )
                            }
                        },
                )
            }

            val itemsBlock: @Composable () -> Unit = {
                items.forEachIndexed { index, item ->
                    val isSelected = item.value in selectedValues
                    val isFirst = index == 0
                    val isLast = index == items.lastIndex
                    val itemEnabled = enabled && !item.disabled

                    val rowItem: @Composable () -> Unit = {
                        ToggleGroupItemCell(
                            item = item,
                            selected = isSelected,
                            enabled = itemEnabled,
                            variant = variant,
                            size = size,
                            orientation = orientation,
                            radius = radius,
                            isFirst = isFirst,
                            isLast = isLast,
                            useSlide = useSlide,
                            useMorph = useMorph,
                            showSelectedIcon = showSelectedIcon,
                            hideLabel = hideLabels,
                            role = itemRole,
                            onClick = {
                                val next = resolveToggleGroupSelection(
                                    current = selectedValues,
                                    toggled = item.value,
                                    multiple = multiple,
                                    allowEmpty = effectiveAllowEmpty,
                                )
                                if (next != selectedValues) {
                                    haptics.perform(KiraHapticFeedbackType.Selection)
                                    onSelectedValuesChange(next)
                                }
                            },
                            modifier = if (useSlide) {
                                Modifier.onGloballyPositioned { coords ->
                                    val p = coords.positionInParent()
                                    val next = IntOffset(p.x.toInt(), p.y.toInt()) to coords.size
                                    if (itemRects[item.value] != next) {
                                        itemRects[item.value] = next
                                    }
                                }
                            } else {
                                Modifier
                            },
                        )
                    }

                    val tip = if (!itemEnabled && item.disabledReason != null) {
                        item.disabledReason
                    } else if (hideLabels && item.label != null) {
                        item.tooltip ?: item.label
                    } else {
                        item.tooltip
                    }

                    if (tip != null) {
                        KiraSimpleTooltip(text = tip) { rowItem() }
                    } else {
                        rowItem()
                    }

                    if (variant == KiraToggleGroupVariant.Outline && !isLast) {
                        if (orientation == KiraToggleGroupOrientation.Horizontal) {
                            Box(Modifier.width(1.dp).height(size.visualHeight).background(colors.input))
                        } else {
                            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.input))
                        }
                    }
                }
            }

            if (orientation == KiraToggleGroupOrientation.Vertical) {
                Column(
                    modifier = Modifier.width(IntrinsicSize.Max),
                    verticalArrangement = Arrangement.spacedBy(itemSpacing),
                ) {
                    itemsBlock()
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    itemsBlock()
                }
            }
        }

        if (invalid && !errorMessage.isNullOrBlank()) {
            BasicText(
                text = errorMessage,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                style = KiraTheme.typography.caption.copy(color = colors.destructive),
            )
        }
    }
}

@Composable
private fun ToggleGroupItemCell(
    item: KiraToggleGroupItem,
    selected: Boolean,
    enabled: Boolean,
    variant: KiraToggleGroupVariant,
    size: KiraToggleGroupSize,
    orientation: KiraToggleGroupOrientation,
    radius: KiraToggleGroupRadius,
    isFirst: Boolean,
    isLast: Boolean,
    useSlide: Boolean,
    useMorph: Boolean,
    showSelectedIcon: Boolean,
    hideLabel: Boolean,
    role: Role,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KiraTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()

    val baseRadius = KiraTheme.radius.lg
    val itemShape = when {
        useMorph && selected -> KiraTheme.radius.shapeXl
        radius == KiraToggleGroupRadius.Pill -> KiraTheme.radius.shapeFull
        variant == KiraToggleGroupVariant.Pills -> KiraTheme.radius.shapeFull
        variant == KiraToggleGroupVariant.Outline -> {
            if (orientation == KiraToggleGroupOrientation.Horizontal) {
                RoundedCornerShape(
                    topStart = if (isFirst) baseRadius else 0.dp,
                    bottomStart = if (isFirst) baseRadius else 0.dp,
                    topEnd = if (isLast) baseRadius else 0.dp,
                    bottomEnd = if (isLast) baseRadius else 0.dp,
                )
            } else {
                RoundedCornerShape(
                    topStart = if (isFirst) baseRadius else 0.dp,
                    topEnd = if (isFirst) baseRadius else 0.dp,
                    bottomStart = if (isLast) baseRadius else 0.dp,
                    bottomEnd = if (isLast) baseRadius else 0.dp,
                )
            }
        }
        else -> KiraTheme.radius.shapeMd
    }

    val morphScaleState = if (useMorph) {
        animateFloatAsState(
            targetValue = if (selected) 1.03f else 1f,
            animationSpec = KiraMotion.bouncySpring(),
            label = "TGMorphScale",
        )
    } else {
        null
    }

    val (semanticBg, semanticFg) = when (item.colorVariant) {
        KiraToggleGroupColor.Success -> colors.success to colors.successForeground
        KiraToggleGroupColor.Warning -> colors.warning to colors.warningForeground
        KiraToggleGroupColor.Destructive -> colors.destructive to colors.destructiveForeground
        KiraToggleGroupColor.Default -> null to null
    }

    val targetBg = when {
        useSlide -> Color.Transparent
        selected && semanticBg != null -> semanticBg
        selected && variant == KiraToggleGroupVariant.Segment -> colors.background
        selected && variant == KiraToggleGroupVariant.Pills -> colors.primary.copy(alpha = 0.12f)
        selected -> colors.accent
        hovered && enabled && variant != KiraToggleGroupVariant.Segment -> colors.accent.copy(alpha = 0.7f)
        else -> Color.Transparent
    }

    val targetFg = when {
        selected && semanticFg != null -> semanticFg
        selected && variant == KiraToggleGroupVariant.Segment -> colors.foreground
        selected && variant == KiraToggleGroupVariant.Pills -> colors.primary
        selected -> colors.accentForeground
        hovered && enabled -> colors.foreground
        else -> colors.mutedForeground
    }

    val bgColorState = animateColorAsState(targetBg, KiraMotion.fastTween(), label = "TGItemBg")
    val fgColor by animateColorAsState(targetFg, KiraMotion.fastTween(), label = "TGItemFg")
    val elevation = if (!useSlide && selected && variant == KiraToggleGroupVariant.Segment) 2.dp else 0.dp

    Row(
        modifier = modifier
            .then(if (orientation == KiraToggleGroupOrientation.Vertical) Modifier.fillMaxWidth() else Modifier)
            .then(
                if (morphScaleState != null) {
                    Modifier.graphicsLayer {
                        val s = morphScaleState.value
                        scaleX = s
                        scaleY = s
                    }
                } else {
                    Modifier
                },
            )
            .heightIn(min = size.visualHeight.coerceAtLeast(36.dp))
            .then(if (enabled) Modifier else Modifier.graphicsLayer { alpha = 0.50f })
            .then(if (elevation > 0.dp) Modifier.shadow(elevation, itemShape, clip = false) else Modifier)
            .clip(itemShape)
            .drawBehind {
                val c = bgColorState.value
                if (c.alpha > 0f) drawRect(c)
            }
            .then(
                when {
                    focused -> Modifier.border(2.dp, colors.ring, itemShape)
                    selected && variant == KiraToggleGroupVariant.Pills && semanticBg == null ->
                        Modifier.border(1.dp, colors.primary, itemShape)
                    else -> Modifier
                },
            )
            .then(if (enabled) Modifier.pointerHoverIcon(PointerIcon.Hand) else Modifier)
            .hoverable(interactionSource, enabled = enabled)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = role,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = if (hideLabel) 10.dp else size.horizontalPadding, vertical = 6.dp)
            .semantics {
                if (item.contentDescription != null) this.contentDescription = item.contentDescription
                if (!enabled) disabled()
            },
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showSelectedIcon && selected) {
            Canvas(Modifier.size(size.iconSize).clearAndSetSemantics {}) {
                val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                val path = Path().apply {
                    moveTo(this@Canvas.size.width * 0.18f, this@Canvas.size.height * 0.52f)
                    lineTo(this@Canvas.size.width * 0.42f, this@Canvas.size.height * 0.76f)
                    lineTo(this@Canvas.size.width * 0.82f, this@Canvas.size.height * 0.26f)
                }
                drawPath(path, fgColor, style = stroke)
            }
        }
        if (item.icon != null) {
            item.icon.invoke(fgColor)
        }
        if (!hideLabel && item.label != null) {
            BasicText(
                text = item.label,
                maxLines = 1,
                style = KiraTheme.typography.label.copy(
                    color = fgColor,
                    fontSize = size.fontSize.sp,
                    fontWeight = if (selected && useSlide) FontWeight.SemiBold else FontWeight.Medium,
                ),
            )
        }
    }
}
