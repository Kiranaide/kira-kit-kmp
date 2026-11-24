package com.kirakit.components.tabs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.kirakit.components.platform.KiraHapticFeedbackType
import com.kirakit.components.platform.rememberKiraHaptics
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.shapeFull
import com.kirakit.theme.typography
import kotlinx.coroutines.launch

enum class KiraTabsVariant {
    Default,
    Outline,
    Pills,
    Underline,
    Soft,
    Card,
    Segment,
}

enum class KiraTabsColor {
    Default,
    Primary,
    Success,
    Warning,
    Destructive,
}

enum class KiraTabsOrientation {
    Horizontal,
    Vertical,
}

@Immutable
data class KiraTabItem(
    val value: String,
    val label: String,
    val icon: (@Composable () -> Unit)? = null,
    val badge: String? = null,
    val disabled: Boolean = false,
    val closable: Boolean = false,
    val content: @Composable () -> Unit = {},
)

/**
 * Resolves the next active tab value when the currently selected tab is closed.
 */
fun resolveNextTabAfterClose(
    tabs: List<KiraTabItem>,
    closingValue: String,
    activeValue: String,
): String? {
    val remaining = tabs.filter { it.value != closingValue }
    if (remaining.isEmpty()) return null
    if (activeValue != closingValue) return activeValue
    val closedIdx = tabs.indexOfFirst { it.value == closingValue }.coerceAtLeast(0)
    return remaining.getOrNull(closedIdx.coerceAtMost(remaining.lastIndex))?.value
}

/**
 * Multi-variant animated tabs aligned with kira-kit-v2's `Tabs` & `KiraTabs`.
 *
 * Supports all 7 visual variants (`Default`, `Outline`, `Pills`, `Underline`, `Soft`, `Card`, `Segment`),
 * 5 semantic colors, horizontal & vertical orientations, sliding animated active indicator for
 * `Underline` and `Segment`, horizontal scrollable overflow with scroll chevrons, closable tabs,
 * add-tab affordance, `>= 44.dp` touch targets, and mobile selection haptics.
 */
@Composable
fun KiraTabs(
    tabs: List<KiraTabItem>,
    selectedValue: String,
    onSelectedValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    variant: KiraTabsVariant = KiraTabsVariant.Default,
    color: KiraTabsColor = KiraTabsColor.Default,
    orientation: KiraTabsOrientation = KiraTabsOrientation.Horizontal,
    showIndicator: Boolean = true,
    scrollable: Boolean = false,
    onCloseTab: ((String) -> Unit)? = null,
    onAddTab: (() -> Unit)? = null,
    showContent: Boolean = true,
) {
    val activeTab = tabs.firstOrNull { it.value == selectedValue } ?: tabs.firstOrNull()

    val tabList: @Composable (Modifier) -> Unit = { listMod ->
        KiraTabsList(
            tabs = tabs,
            selectedValue = activeTab?.value ?: selectedValue,
            onSelectedValueChange = onSelectedValueChange,
            variant = variant,
            color = color,
            orientation = orientation,
            showIndicator = showIndicator,
            scrollable = scrollable,
            onCloseTab = onCloseTab,
            onAddTab = onAddTab,
            modifier = listMod,
        )
    }

    val panel: @Composable (Modifier) -> Unit = { panelMod ->
        if (showContent && activeTab != null) {
            AnimatedContent(
                targetState = activeTab.value,
                transitionSpec = {
                    fadeIn(tween(KiraMotion.RouteFadeMs, easing = KiraMotion.EaseOutStrong)) togetherWith
                        fadeOut(tween(110, easing = KiraMotion.EaseOutStrong))
                },
                modifier = panelMod,
                label = "KiraTabPanel",
            ) { value ->
                val target = tabs.firstOrNull { it.value == value }
                Box(Modifier.fillMaxWidth()) {
                    target?.content?.invoke()
                }
            }
        }
    }

    if (orientation == KiraTabsOrientation.Vertical) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.Top,
        ) {
            tabList(Modifier.width(IntrinsicSize.Max))
            panel(Modifier.weight(1f))
        }
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            tabList(Modifier)
            panel(Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun KiraTabsList(
    tabs: List<KiraTabItem>,
    selectedValue: String,
    onSelectedValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    variant: KiraTabsVariant = KiraTabsVariant.Default,
    color: KiraTabsColor = KiraTabsColor.Default,
    orientation: KiraTabsOrientation = KiraTabsOrientation.Horizontal,
    showIndicator: Boolean = true,
    scrollable: Boolean = false,
    onCloseTab: ((String) -> Unit)? = null,
    onAddTab: (() -> Unit)? = null,
) {
    val colors = KiraTheme.colors
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val tabRects = remember { mutableStateMapOf<String, Pair<IntOffset, IntSize>>() }

    val tone = when (color) {
        KiraTabsColor.Primary -> colors.primary
        KiraTabsColor.Success -> colors.success
        KiraTabsColor.Warning -> colors.warning
        KiraTabsColor.Destructive -> colors.destructive
        KiraTabsColor.Default -> colors.primary
    }

    val listBg = when (variant) {
        KiraTabsVariant.Default -> if (color == KiraTabsColor.Default) colors.muted else tone.copy(alpha = 0.12f)
        KiraTabsVariant.Outline -> if (color == KiraTabsColor.Default) Color.Transparent else tone.copy(alpha = 0.06f)
        KiraTabsVariant.Card -> if (color == KiraTabsColor.Default) colors.muted.copy(alpha = 0.35f) else tone.copy(alpha = 0.12f)
        KiraTabsVariant.Segment -> colors.muted.copy(alpha = 0.45f)
        else -> Color.Transparent
    }
    val listBorder = when (variant) {
        KiraTabsVariant.Outline -> if (color == KiraTabsColor.Default) colors.border else tone.copy(alpha = 0.35f)
        else -> Color.Transparent
    }
    val listShape = when (variant) {
        KiraTabsVariant.Segment -> KiraTheme.radius.shapeFull
        KiraTabsVariant.Underline -> RectangleShape
        else -> KiraTheme.radius.shapeLg
    }
    val listPadding = when (variant) {
        KiraTabsVariant.Default, KiraTabsVariant.Outline, KiraTabsVariant.Card, KiraTabsVariant.Segment -> 4.dp
        else -> 0.dp
    }

    val activeRect = tabRects[selectedValue]
    val useSlidingIndicator = showIndicator && (variant == KiraTabsVariant.Underline || variant == KiraTabsVariant.Segment)

    val containerContent: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .clip(listShape)
                .background(listBg, listShape)
                .then(if (listBorder != Color.Transparent) Modifier.border(1.dp, listBorder, listShape) else Modifier)
                .padding(listPadding)
                .selectableGroup(),
        ) {
            // Baseline hairline for Underline variant
            if (variant == KiraTabsVariant.Underline) {
                if (orientation == KiraTabsOrientation.Horizontal) {
                    Box(
                        Modifier
                            .matchParentSize(),
                        contentAlignment = Alignment.BottomStart,
                    ) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                    }
                } else {
                    Box(
                        Modifier
                            .matchParentSize(),
                        contentAlignment = Alignment.TopEnd,
                    ) {
                        Box(Modifier.width(1.dp).height(Int.MAX_VALUE.dp).background(colors.border))
                    }
                }
            }

            // Sliding animated indicator drawn in Draw phase (0 recompositions during tab transitions)
            if (useSlidingIndicator && activeRect != null) {
                val (pos, sz) = activeRect
                val animXState = animateDpAsState(with(density) { pos.x.toDp() }, KiraMotion.standardTween(), label = "TabIndX")
                val animYState = animateDpAsState(with(density) { pos.y.toDp() }, KiraMotion.standardTween(), label = "TabIndY")
                val animWState = animateDpAsState(with(density) { sz.width.toDp() }, KiraMotion.standardTween(), label = "TabIndW")
                val animHState = animateDpAsState(with(density) { sz.height.toDp() }, KiraMotion.standardTween(), label = "TabIndH")

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .drawBehind {
                            val x = animXState.value.toPx()
                            val y = animYState.value.toPx()
                            val w = animWState.value.toPx()
                            val h = animHState.value.toPx()
                            if (w <= 0f || h <= 0f) return@drawBehind
                            when {
                                variant == KiraTabsVariant.Segment -> {
                                    val r = minOf(w, h) / 2f
                                    drawRoundRect(
                                        color = tone,
                                        topLeft = Offset(x, y),
                                        size = Size(w, h),
                                        cornerRadius = CornerRadius(r, r),
                                    )
                                }
                                orientation == KiraTabsOrientation.Horizontal -> {
                                    val barH = 2.dp.toPx()
                                    drawRect(
                                        color = tone,
                                        topLeft = Offset(x, y + h - barH),
                                        size = Size(w, barH),
                                    )
                                }
                                else -> {
                                    val barW = 2.dp.toPx()
                                    drawRect(
                                        color = tone,
                                        topLeft = Offset(x + w - barW, y),
                                        size = Size(barW, h),
                                    )
                                }
                            }
                        },
                )
            }

            val itemsBlock: @Composable () -> Unit = {
                tabs.forEach { tab ->
                    KiraTabTrigger(
                        tab = tab,
                        selected = tab.value == selectedValue,
                        variant = variant,
                        color = color,
                        orientation = orientation,
                        onSelect = { onSelectedValueChange(tab.value) },
                        onClose = if (tab.closable && onCloseTab != null) {
                            { onCloseTab(tab.value) }
                        } else null,
                        modifier = if (useSlidingIndicator) {
                            Modifier.onGloballyPositioned { coords ->
                                val p = coords.positionInParent()
                                val next = IntOffset(p.x.toInt(), p.y.toInt()) to coords.size
                                if (tabRects[tab.value] != next) {
                                    tabRects[tab.value] = next
                                }
                            }
                        } else {
                            Modifier
                        },
                    )
                }
                if (onAddTab != null) {
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 36.dp, minHeight = 44.dp)
                            .clip(KiraTheme.radius.shapeMd)
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(role = Role.Button, onClick = onAddTab)
                            .semantics { contentDescription = "Add tab" },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "+",
                            style = KiraTheme.typography.heading3.copy(color = colors.mutedForeground),
                        )
                    }
                }
            }

            if (orientation == KiraTabsOrientation.Vertical) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    itemsBlock()
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    itemsBlock()
                }
            }
        }
    }

    if (scrollable && orientation == KiraTabsOrientation.Horizontal) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (scrollState.canScrollBackward) {
                ScrollStepButton(
                    label = "Scroll tabs left",
                    glyph = "‹",
                    onClick = { scope.launch { scrollState.animateScrollTo((scrollState.value - 180).coerceAtLeast(0)) } },
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState),
            ) {
                containerContent()
            }
            if (scrollState.canScrollForward) {
                ScrollStepButton(
                    label = "Scroll tabs right",
                    glyph = "›",
                    onClick = { scope.launch { scrollState.animateScrollTo(scrollState.value + 180) } },
                )
            }
        }
    } else {
        Box(modifier = modifier) {
            containerContent()
        }
    }
}

@Composable
private fun KiraTabTrigger(
    tab: KiraTabItem,
    selected: Boolean,
    variant: KiraTabsVariant,
    color: KiraTabsColor,
    orientation: KiraTabsOrientation,
    onSelect: () -> Unit,
    onClose: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = KiraTheme.colors
    val haptics = rememberKiraHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()

    val (tone, toneFg) = when (color) {
        KiraTabsColor.Primary -> colors.primary to colors.primaryForeground
        KiraTabsColor.Success -> colors.success to colors.successForeground
        KiraTabsColor.Warning -> colors.warning to colors.warningForeground
        KiraTabsColor.Destructive -> colors.destructive to colors.destructiveForeground
        KiraTabsColor.Default -> colors.primary to colors.primaryForeground
    }

    val shape = when (variant) {
        KiraTabsVariant.Segment, KiraTabsVariant.Pills -> KiraTheme.radius.shapeFull
        KiraTabsVariant.Underline -> RectangleShape
        else -> KiraTheme.radius.shapeMd
    }

    val targetBg = when {
        variant == KiraTabsVariant.Segment -> Color.Transparent // sliding indicator owns fill
        variant == KiraTabsVariant.Underline -> Color.Transparent
        selected && variant == KiraTabsVariant.Default -> if (color == KiraTabsColor.Default) colors.background else tone
        selected && variant == KiraTabsVariant.Card -> if (color == KiraTabsColor.Default) colors.card else tone
        selected && (variant == KiraTabsVariant.Outline || variant == KiraTabsVariant.Pills) -> tone
        selected && variant == KiraTabsVariant.Soft -> tone.copy(alpha = 0.14f)
        hovered && !tab.disabled && variant != KiraTabsVariant.Default -> colors.muted.copy(alpha = 0.50f)
        else -> Color.Transparent
    }

    val targetFg = when {
        selected && variant == KiraTabsVariant.Segment -> toneFg
        selected && variant == KiraTabsVariant.Underline -> tone
        selected && variant == KiraTabsVariant.Soft -> if (color == KiraTabsColor.Default) colors.foreground else tone
        selected && (variant == KiraTabsVariant.Outline || variant == KiraTabsVariant.Pills) -> toneFg
        selected && (variant == KiraTabsVariant.Default || variant == KiraTabsVariant.Card) ->
            if (color == KiraTabsColor.Default) colors.foreground else toneFg
        hovered && !tab.disabled -> colors.foreground
        else -> colors.mutedForeground
    }

    val bgColorState = animateColorAsState(targetBg, KiraMotion.fastTween(), label = "TabBg")
    val fgColor by animateColorAsState(targetFg, KiraMotion.fastTween(), label = "TabFg")
    val elevation = if (selected && (variant == KiraTabsVariant.Default || variant == KiraTabsVariant.Card)) 3.dp else 0.dp

    Row(
        modifier = modifier
            .then(if (orientation == KiraTabsOrientation.Vertical) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = 44.dp)
            .then(if (tab.disabled) Modifier.graphicsLayer { alpha = 0.5f } else Modifier)
            .then(if (elevation > 0.dp) Modifier.shadow(elevation, shape, clip = false) else Modifier)
            .clip(shape)
            .drawBehind {
                val c = bgColorState.value
                if (c.alpha > 0f) drawRect(c)
            }
            .then(
                if (focused) {
                    Modifier.border(2.dp, colors.ring, shape)
                } else if (selected && variant == KiraTabsVariant.Card && color == KiraTabsColor.Default) {
                    Modifier.border(1.dp, colors.border, shape)
                } else {
                    Modifier
                },
            )
            .then(if (!tab.disabled) Modifier.pointerHoverIcon(PointerIcon.Hand) else Modifier)
            .hoverable(interactionSource, enabled = !tab.disabled)
            .selectable(
                selected = selected,
                enabled = !tab.disabled,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptics.perform(KiraHapticFeedbackType.Selection)
                    onSelect()
                },
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .semantics {
                if (tab.disabled) disabled()
            },
        horizontalArrangement = Arrangement.spacedBy(6.dp, if (orientation == KiraTabsOrientation.Vertical) Alignment.Start else Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (tab.icon != null) {
            Box(Modifier.size(16.dp), contentAlignment = Alignment.Center) { tab.icon.invoke() }
        }
        BasicText(
            text = tab.label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = KiraTheme.typography.label.copy(
                color = fgColor,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            ),
        )
        if (tab.badge != null) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(fgColor.copy(alpha = 0.14f))
                    .padding(horizontal = 6.dp, vertical = 1.dp),
            ) {
                BasicText(
                    text = tab.badge,
                    style = KiraTheme.typography.caption.copy(
                        color = fgColor,
                        fontFeatureSettings = "tnum",
                    ),
                )
            }
        }
        if (onClose != null && !tab.disabled) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onClose)
                    .semantics { contentDescription = "Close ${tab.label}" },
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.size(10.dp).clearAndSetSemantics {}) {
                    val sw = 1.5.dp.toPx()
                    val p = size.width * 0.2f
                    drawLine(fgColor, Offset(p, p), Offset(size.width - p, size.height - p), sw, StrokeCap.Round)
                    drawLine(fgColor, Offset(size.width - p, p), Offset(p, size.height - p), sw, StrokeCap.Round)
                }
            }
        }
    }
}

@Composable
private fun ScrollStepButton(
    label: String,
    glyph: String,
    onClick: () -> Unit,
) {
    val colors = KiraTheme.colors
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(colors.background)
            .border(1.dp, colors.border, CircleShape)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = glyph,
            style = KiraTheme.typography.label.copy(color = colors.foreground),
        )
    }
}
