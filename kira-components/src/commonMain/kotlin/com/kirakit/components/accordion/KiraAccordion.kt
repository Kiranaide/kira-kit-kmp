package com.kirakit.components.accordion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.components.input.KiraInput
import com.kirakit.components.input.KiraInputSize
import com.kirakit.components.platform.KiraHapticFeedbackType
import com.kirakit.components.platform.rememberKiraHaptics
import com.kirakit.components.tooltip.KiraSimpleTooltip
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography
import kotlinx.coroutines.delay

enum class KiraAccordionVariant {
    Default,
    Info,
    Success,
    Warning,
    Destructive,
    Ghost,
    Outline,
}

enum class KiraAccordionSize(
    internal val triggerPadding: PaddingValues,
    internal val contentPadding: PaddingValues,
    internal val fontSize: Int,
) {
    Xs(PaddingValues(horizontal = 8.dp, vertical = 6.dp), PaddingValues(horizontal = 8.dp, vertical = 6.dp), 12),
    Sm(PaddingValues(horizontal = 12.dp, vertical = 8.dp), PaddingValues(horizontal = 12.dp, vertical = 8.dp), 12),
    Default(PaddingValues(horizontal = 16.dp, vertical = 12.dp), PaddingValues(horizontal = 16.dp, vertical = 12.dp), 14),
    Lg(PaddingValues(horizontal = 20.dp, vertical = 16.dp), PaddingValues(horizontal = 20.dp, vertical = 16.dp), 16),
}

enum class KiraAccordionChevronPosition {
    Start,
    End,
}

@Immutable
data class KiraAccordionItemData(
    val value: String,
    val title: String,
    val content: @Composable () -> Unit,
    val disabled: Boolean = false,
    val disabledReason: String? = null,
    val icon: (@Composable () -> Unit)? = null,
    val variant: KiraAccordionVariant? = null,
    val size: KiraAccordionSize? = null,
    val showArrow: Boolean = true,
    val chevronIcon: (@Composable () -> Unit)? = null,
    val chevronPosition: KiraAccordionChevronPosition = KiraAccordionChevronPosition.End,
    val inset: Boolean = false,
    val lazyMount: Boolean = false,
    val showSkeleton: Boolean = false,
    val skeletonLines: Int = 3,
    val loading: Boolean = false,
    val error: String? = null,
    val onRetry: (() -> Unit)? = null,
    val children: List<KiraAccordionItemData> = emptyList(),
    val searchText: String? = null,
)

fun toggleAccordionValue(
    current: Set<String>,
    value: String,
    multiple: Boolean,
    collapsible: Boolean = true,
): Set<String> {
    val isOpen = value in current
    return if (multiple) {
        if (isOpen) {
            if (collapsible || current.size > 1) current - value else current
        } else {
            current + value
        }
    } else {
        if (isOpen) {
            if (collapsible) emptySet() else current
        } else {
            setOf(value)
        }
    }
}

fun filterAccordionItems(
    items: List<KiraAccordionItemData>,
    query: String,
): List<KiraAccordionItemData> {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return items
    val result = mutableListOf<KiraAccordionItemData>()
    for (item in items) {
        val haystack = (item.searchText ?: item.title).lowercase()
        if (haystack.contains(q)) {
            result.add(item)
            continue
        }
        if (item.children.isNotEmpty()) {
            val filteredChildren = filterAccordionItems(item.children, query)
            if (filteredChildren.isNotEmpty()) {
                result.add(item.copy(children = filteredChildren))
            }
        }
    }
    return result
}

internal fun flattenAccordionValues(items: List<KiraAccordionItemData>): List<String> {
    val values = mutableListOf<String>()
    for (item in items) {
        if (!item.disabled) values.add(item.value)
        if (item.children.isNotEmpty()) values.addAll(flattenAccordionValues(item.children))
    }
    return values
}

@Stable
class KiraAccordionState(
    initialExpanded: Set<String> = emptySet(),
    val multiple: Boolean = false,
    val collapsible: Boolean = true,
) {
    var expandedValues by mutableStateOf(initialExpanded)

    fun isExpanded(value: String): Boolean = value in expandedValues

    fun toggle(value: String) {
        expandedValues = toggleAccordionValue(expandedValues, value, multiple, collapsible)
    }

    fun expandAll(values: Collection<String>) {
        expandedValues = if (multiple) expandedValues + values else values.firstOrNull()?.let(::setOf) ?: emptySet()
    }

    fun collapseAll() {
        expandedValues = emptySet()
    }
}

@Composable
fun rememberKiraAccordionState(
    initialExpanded: Set<String> = emptySet(),
    multiple: Boolean = false,
    collapsible: Boolean = true,
): KiraAccordionState = remember(multiple, collapsible) {
    KiraAccordionState(
        initialExpanded = initialExpanded,
        multiple = multiple,
        collapsible = collapsible,
    )
}

private data class AccordionScopeContext(
    val isExpanded: (String) -> Boolean,
    val toggle: (String) -> Unit,
    val defaultVariant: KiraAccordionVariant,
    val defaultSize: KiraAccordionSize,
)

private data class AccordionItemScopeContext(
    val value: String,
    val expanded: Boolean,
    val disabled: Boolean,
    val disabledReason: String?,
    val toggle: () -> Unit,
)

private val LocalAccordionContext = compositionLocalOf<AccordionScopeContext?> { null }
private val LocalAccordionItemContext = compositionLocalOf<AccordionItemScopeContext?> { null }

/**
 * Compositional accordion aligned with kira-kit-v2's `Accordion`.
 */
@Composable
fun KiraAccordion(
    modifier: Modifier = Modifier,
    state: KiraAccordionState = rememberKiraAccordionState(),
    variant: KiraAccordionVariant = KiraAccordionVariant.Default,
    size: KiraAccordionSize = KiraAccordionSize.Default,
    itemSpacing: Dp = 4.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val ctx = remember(state, variant, size) {
        AccordionScopeContext(
            isExpanded = { state.isExpanded(it) },
            toggle = { state.toggle(it) },
            defaultVariant = variant,
            defaultSize = size,
        )
    }
    CompositionLocalProvider(LocalAccordionContext provides ctx) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
            content = content,
        )
    }
}

@Composable
fun KiraAccordionItem(
    value: String,
    modifier: Modifier = Modifier,
    disabled: Boolean = false,
    disabledReason: String? = null,
    onOpen: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val parent = requireNotNull(LocalAccordionContext.current) {
        "KiraAccordionItem must be used inside KiraAccordion"
    }
    val expanded = parent.isExpanded(value)
    var previousExpanded by remember { mutableStateOf(expanded) }

    LaunchedEffect(expanded) {
        if (expanded && !previousExpanded) onOpen?.invoke()
        else if (!expanded && previousExpanded) onClose?.invoke()
        previousExpanded = expanded
    }

    val itemCtx = remember(value, expanded, disabled, disabledReason, parent) {
        AccordionItemScopeContext(
            value = value,
            expanded = expanded,
            disabled = disabled,
            disabledReason = disabledReason,
            toggle = { if (!disabled) parent.toggle(value) },
        )
    }

    val shape = KiraTheme.radius.shapeLg
    val colors = KiraTheme.colors

    CompositionLocalProvider(LocalAccordionItemContext provides itemCtx) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .alpha(if (disabled) 0.5f else 1f)
                .clip(shape)
                .border(1.dp, colors.border, shape),
            content = content,
        )
    }
}

@Composable
fun KiraAccordionTrigger(
    title: String,
    modifier: Modifier = Modifier,
    highlightQuery: String = "",
    variant: KiraAccordionVariant? = null,
    size: KiraAccordionSize? = null,
    showArrow: Boolean = true,
    chevronIcon: (@Composable () -> Unit)? = null,
    chevronPosition: KiraAccordionChevronPosition = KiraAccordionChevronPosition.End,
    icon: (@Composable () -> Unit)? = null,
    loading: Boolean = false,
    error: String? = null,
    onRetry: (() -> Unit)? = null,
    disabledReason: String? = null,
    trailingAction: (@Composable () -> Unit)? = null,
) {
    val parent = requireNotNull(LocalAccordionContext.current)
    val item = requireNotNull(LocalAccordionItemContext.current)
    val resolvedVariant = variant ?: parent.defaultVariant
    val resolvedSize = size ?: parent.defaultSize
    val resolvedReason = disabledReason ?: item.disabledReason
    val colors = KiraTheme.colors
    val haptics = rememberKiraHaptics()

    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()

    // Brief success checkmark flash when loading transitions true -> false without error
    var prevLoading by remember { mutableStateOf(loading) }
    var showSuccess by remember { mutableStateOf(false) }
    LaunchedEffect(loading, error) {
        if (prevLoading && !loading && error == null) {
            showSuccess = true
            delay(1_500L)
            showSuccess = false
        } else if (loading) {
            showSuccess = false
        }
        prevLoading = loading
    }

    val targetBg = when (resolvedVariant) {
        KiraAccordionVariant.Default -> if (hovered && !item.disabled) colors.muted.copy(alpha = 0.50f) else Color.Transparent
        KiraAccordionVariant.Info -> colors.info.copy(alpha = if (hovered && !item.disabled) 0.20f else 0.10f)
        KiraAccordionVariant.Success -> colors.success.copy(alpha = if (hovered && !item.disabled) 0.20f else 0.10f)
        KiraAccordionVariant.Warning -> colors.warning.copy(alpha = if (hovered && !item.disabled) 0.20f else 0.10f)
        KiraAccordionVariant.Destructive -> colors.destructive.copy(alpha = if (hovered && !item.disabled) 0.20f else 0.10f)
        KiraAccordionVariant.Ghost -> if (hovered && !item.disabled) colors.muted.copy(alpha = 0.30f) else Color.Transparent
        KiraAccordionVariant.Outline -> if (hovered && !item.disabled) colors.muted.copy(alpha = 0.30f) else Color.Transparent
    }
    val bgColorState = animateColorAsState(targetBg, KiraMotion.fastTween(), label = "AccordionTriggerBg")

    val contentColor = when (resolvedVariant) {
        KiraAccordionVariant.Info -> colors.info
        KiraAccordionVariant.Success -> colors.success
        KiraAccordionVariant.Warning -> colors.warning
        KiraAccordionVariant.Destructive -> colors.destructive
        else -> colors.foreground
    }

    val chevronRotationState = animateFloatAsState(
        targetValue = if (item.expanded) 180f else 0f,
        animationSpec = KiraMotion.standardTween(),
        label = "AccordionChevron",
    )

    val lgRadius = KiraTheme.radius.lg
    val expandedTriggerShape = remember(lgRadius) {
        RoundedCornerShape(topStart = lgRadius, topEnd = lgRadius)
    }
    val triggerShape = if (item.expanded) expandedTriggerShape else KiraTheme.radius.shapeLg

    val annotatedTitle = remember(title, highlightQuery, colors.warning) {
        buildHighlightedTitle(title, highlightQuery, colors.warning.copy(alpha = 0.30f))
    }

    val chevronSlot: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .size(16.dp)
                .graphicsLayer { rotationZ = chevronRotationState.value },
            contentAlignment = Alignment.Center,
        ) {
            if (chevronIcon != null) {
                chevronIcon()
            } else {
                AccordionChevronCanvas(color = contentColor)
            }
        }
    }

    val triggerRow: @Composable () -> Unit = {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp)
                .clip(triggerShape)
                .drawBehind {
                    val c = bgColorState.value
                    if (c.alpha > 0f) drawRect(c)
                }
                .then(if (focused) Modifier.border(2.dp, colors.ring, triggerShape) else Modifier)
                .then(if (!item.disabled) Modifier.pointerHoverIcon(PointerIcon.Hand) else Modifier)
                .hoverable(interactionSource, enabled = !item.disabled)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = !item.disabled,
                    role = Role.Button,
                    onClick = {
                        haptics.perform(KiraHapticFeedbackType.Selection)
                        item.toggle()
                    },
                )
                .padding(resolvedSize.triggerPadding)
                .semantics {
                    stateDescription = if (item.expanded) "Expanded" else "Collapsed"
                    if (item.disabled) disabled()
                },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showArrow && chevronPosition == KiraAccordionChevronPosition.Start) {
                chevronSlot()
            }
            if (icon != null) {
                Box(Modifier.size(16.dp), contentAlignment = Alignment.Center) { icon() }
            }
            BasicText(
                text = annotatedTitle,
                modifier = Modifier.weight(1f),
                style = KiraTheme.typography.label.copy(
                    color = contentColor,
                    fontSize = resolvedSize.fontSize.sp,
                    lineHeight = (resolvedSize.fontSize + 6).sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
            if (loading) {
                AccordionSpinner(color = contentColor, size = 16.dp)
            } else if (showSuccess) {
                AccordionCheckIcon(color = colors.success, size = 16.dp)
            } else if (error != null && onRetry != null) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(KiraTheme.radius.shapeSm)
                        .clickable(role = Role.Button, onClick = onRetry)
                        .semantics { contentDescription = "Retry loading content" },
                    contentAlignment = Alignment.Center,
                ) {
                    AccordionRetryIcon(color = colors.destructive, size = 14.dp)
                }
            }
            if (trailingAction != null) {
                trailingAction()
            }
            if (showArrow && chevronPosition == KiraAccordionChevronPosition.End) {
                chevronSlot()
            }
        }
    }

    if (item.disabled && !resolvedReason.isNullOrBlank()) {
        KiraSimpleTooltip(text = resolvedReason) { triggerRow() }
    } else {
        triggerRow()
    }
}

@Composable
fun KiraAccordionContent(
    modifier: Modifier = Modifier,
    size: KiraAccordionSize? = null,
    inset: Boolean = false,
    lazyMount: Boolean = false,
    showSkeleton: Boolean = false,
    skeletonLines: Int = 3,
    loading: Boolean = false,
    error: String? = null,
    onRetry: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val parent = requireNotNull(LocalAccordionContext.current)
    val item = requireNotNull(LocalAccordionItemContext.current)
    val resolvedSize = size ?: parent.defaultSize
    val colors = KiraTheme.colors

    var hasBeenOpened by remember { mutableStateOf(item.expanded) }
    LaunchedEffect(item.expanded) {
        if (item.expanded) hasBeenOpened = true
    }

    AnimatedVisibility(
        visible = item.expanded,
        enter = expandVertically(tween(KiraMotion.RouteTransitionMs, easing = KiraMotion.EaseOutStrong)) +
            fadeIn(tween(KiraMotion.RouteFadeMs, easing = KiraMotion.EaseOutStrong)),
        exit = shrinkVertically(tween(KiraMotion.RouteTransitionMs, easing = KiraMotion.EaseOutStrong)) +
            fadeOut(tween(KiraMotion.RouteFadeMs, easing = KiraMotion.EaseOutStrong)),
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .then(if (inset) Modifier.padding(start = 16.dp) else Modifier),
        ) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border.copy(alpha = 0.6f)))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(resolvedSize.contentPadding),
            ) {
                when {
                    loading -> AccordionSkeleton(lines = skeletonLines)
                    error != null -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        BasicText(
                            text = "Error loading content",
                            style = KiraTheme.typography.label.copy(color = colors.destructive),
                        )
                        BasicText(
                            text = error,
                            style = KiraTheme.typography.caption.copy(color = colors.destructive.copy(alpha = 0.85f)),
                        )
                        if (onRetry != null) {
                            KiraButton(
                                text = "Retry",
                                size = KiraButtonSize.Xs,
                                variant = KiraButtonVariant.Outline,
                                onClick = onRetry,
                            )
                        }
                    }
                    !lazyMount || hasBeenOpened -> content()
                    showSkeleton -> AccordionSkeleton(lines = skeletonLines)
                }
            }
        }
    }
}

/**
 * Data-driven accordion matching kira-kit-v2's `KiraAccordion` with built-in search filtering,
 * match highlighting, expand/collapse-all toolbar, nested child items, and accessible reorder controls.
 */
@Composable
fun KiraAccordionList(
    items: List<KiraAccordionItemData>,
    modifier: Modifier = Modifier,
    state: KiraAccordionState = rememberKiraAccordionState(multiple = true),
    variant: KiraAccordionVariant = KiraAccordionVariant.Default,
    size: KiraAccordionSize = KiraAccordionSize.Default,
    searchable: Boolean = false,
    searchPlaceholder: String = "Filter sections…",
    showExpandAll: Boolean = false,
    reorderable: Boolean = false,
    onReorder: ((List<KiraAccordionItemData>) -> Unit)? = null,
    onItemOpen: ((KiraAccordionItemData) -> Unit)? = null,
    onItemClose: ((KiraAccordionItemData) -> Unit)? = null,
) {
    var query by remember { mutableStateOf("") }
    val visibleItems = remember(items, query) { filterAccordionItems(items, query) }
    val allValues = remember(visibleItems) { flattenAccordionValues(visibleItems) }
    val allExpanded = allValues.isNotEmpty() && allValues.all { state.isExpanded(it) }
    val colors = KiraTheme.colors

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (searchable || showExpandAll) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (searchable) {
                    KiraInput(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = searchPlaceholder,
                        size = KiraInputSize.Sm,
                        clearable = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (showExpandAll) {
                    KiraButton(
                        text = if (allExpanded) "Collapse all" else "Expand all",
                        size = KiraButtonSize.Sm,
                        variant = KiraButtonVariant.Outline,
                        onClick = {
                            if (allExpanded) state.collapseAll() else state.expandAll(allValues)
                        },
                    )
                }
            }
        }

        if (visibleItems.isEmpty()) {
            BasicText(
                text = "No matching items.",
                style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                modifier = Modifier.padding(vertical = 8.dp),
            )
        } else {
            KiraAccordion(state = state, variant = variant, size = size) {
                visibleItems.forEachIndexed { index, item ->
                    RenderAccordionItemNode(
                        item = item,
                        index = index,
                        siblingCount = visibleItems.size,
                        highlightQuery = query,
                        reorderable = reorderable && query.isBlank(),
                        onMoveUp = if (reorderable && onReorder != null && index > 0) {
                            {
                                val mutable = items.toMutableList()
                                val removed = mutable.removeAt(index)
                                mutable.add(index - 1, removed)
                                onReorder(mutable)
                            }
                        } else null,
                        onMoveDown = if (reorderable && onReorder != null && index < items.lastIndex) {
                            {
                                val mutable = items.toMutableList()
                                val removed = mutable.removeAt(index)
                                mutable.add(index + 1, removed)
                                onReorder(mutable)
                            }
                        } else null,
                        onItemOpen = onItemOpen,
                        onItemClose = onItemClose,
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderAccordionItemNode(
    item: KiraAccordionItemData,
    index: Int,
    siblingCount: Int,
    highlightQuery: String,
    reorderable: Boolean,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
    onItemOpen: ((KiraAccordionItemData) -> Unit)?,
    onItemClose: ((KiraAccordionItemData) -> Unit)?,
) {
    KiraAccordionItem(
        value = item.value,
        disabled = item.disabled,
        disabledReason = item.disabledReason,
        onOpen = { onItemOpen?.invoke(item) },
        onClose = { onItemClose?.invoke(item) },
    ) {
        KiraAccordionTrigger(
            title = item.title,
            highlightQuery = highlightQuery,
            variant = item.variant,
            size = item.size,
            showArrow = item.showArrow,
            chevronIcon = item.chevronIcon,
            chevronPosition = item.chevronPosition,
            icon = item.icon,
            loading = item.loading,
            error = item.error,
            onRetry = item.onRetry,
            disabledReason = item.disabledReason,
            trailingAction = if (reorderable) {
                {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        ReorderStepButton(
                            label = "Move ${item.title} up",
                            up = true,
                            enabled = index > 0 && onMoveUp != null,
                            onClick = { onMoveUp?.invoke() },
                        )
                        ReorderStepButton(
                            label = "Move ${item.title} down",
                            up = false,
                            enabled = index < siblingCount - 1 && onMoveDown != null,
                            onClick = { onMoveDown?.invoke() },
                        )
                    }
                }
            } else null,
        )
        KiraAccordionContent(
            size = item.size,
            inset = item.inset,
            lazyMount = item.lazyMount,
            showSkeleton = item.showSkeleton,
            skeletonLines = item.skeletonLines,
            loading = item.loading,
            error = item.error,
            onRetry = item.onRetry,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item.content()
                if (item.children.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        item.children.forEachIndexed { childIndex, child ->
                            RenderAccordionItemNode(
                                item = child,
                                index = childIndex,
                                siblingCount = item.children.size,
                                highlightQuery = highlightQuery,
                                reorderable = false,
                                onMoveUp = null,
                                onMoveDown = null,
                                onItemOpen = onItemOpen,
                                onItemClose = onItemClose,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReorderStepButton(
    label: String,
    up: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = KiraTheme.colors
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 28.dp, minHeight = 28.dp)
            .clip(KiraTheme.radius.shapeSm)
            .alpha(if (enabled) 1f else 0.35f)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(12.dp).clearAndSetSemantics {}) {
            val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            val path = Path().apply {
                if (up) {
                    moveTo(size.width * 0.2f, size.height * 0.65f)
                    lineTo(size.width * 0.5f, size.height * 0.35f)
                    lineTo(size.width * 0.8f, size.height * 0.65f)
                } else {
                    moveTo(size.width * 0.2f, size.height * 0.35f)
                    lineTo(size.width * 0.5f, size.height * 0.65f)
                    lineTo(size.width * 0.8f, size.height * 0.35f)
                }
            }
            drawPath(path, colors.mutedForeground, style = stroke)
        }
    }
}

private val AccordionSkeletonWidths = floatArrayOf(0.75f, 0.85f, 0.60f, 0.90f, 0.70f)

@Composable
private fun AccordionSkeleton(lines: Int) {
    val transition = rememberInfiniteTransition(label = "AccordionSkeleton")
    val alphaState = transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "SkeletonPulse",
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(lines.coerceAtLeast(1)) { row ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(AccordionSkeletonWidths[row % AccordionSkeletonWidths.size])
                    .height(12.dp)
                    .graphicsLayer { alpha = alphaState.value }
                    .clip(KiraTheme.radius.shapeSm)
                    .background(KiraTheme.colors.muted),
            )
        }
    }
}

private fun buildHighlightedTitle(
    title: String,
    query: String,
    highlightBg: Color,
): AnnotatedString {
    val q = query.trim()
    if (q.isEmpty()) return AnnotatedString(title)
    val index = title.lowercase().indexOf(q.lowercase())
    if (index < 0) return AnnotatedString(title)
    return buildAnnotatedString {
        append(title.substring(0, index))
        withStyle(SpanStyle(background = highlightBg)) {
            append(title.substring(index, index + q.length))
        }
        append(title.substring(index + q.length))
    }
}

@Composable
private fun AccordionChevronCanvas(color: Color) {
    Canvas(Modifier.size(16.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(size.width * 0.22f, size.height * 0.36f)
            lineTo(size.width * 0.50f, size.height * 0.66f)
            lineTo(size.width * 0.78f, size.height * 0.36f)
        }
        drawPath(path = path, color = color, style = stroke)
    }
}

@Composable
private fun AccordionSpinner(color: Color, size: Dp) {
    val transition = rememberInfiniteTransition(label = "AccordionSpinner")
    val rotationState = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "AccordionSpinnerRotation",
    )
    Box(
        Modifier
            .size(size)
            .clearAndSetSemantics {}
            .drawWithCache {
                val strokeWidth = 1.8.dp.toPx()
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
private fun AccordionCheckIcon(color: Color, size: Dp) {
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(this@Canvas.size.width * 0.18f, this@Canvas.size.height * 0.52f)
            lineTo(this@Canvas.size.width * 0.42f, this@Canvas.size.height * 0.76f)
            lineTo(this@Canvas.size.width * 0.82f, this@Canvas.size.height * 0.28f)
        }
        drawPath(path = path, color = color, style = stroke)
    }
}

@Composable
private fun AccordionRetryIcon(color: Color, size: Dp) {
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val inset = stroke.width
        drawArc(
            color = color,
            startAngle = -60f,
            sweepAngle = 280f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = Size(this.size.width - inset * 2, this.size.height - inset * 2),
            style = stroke,
        )
        // Arrow head near top-right
        val path = Path().apply {
            moveTo(this@Canvas.size.width * 0.62f, this@Canvas.size.height * 0.14f)
            lineTo(this@Canvas.size.width * 0.84f, this@Canvas.size.height * 0.26f)
            lineTo(this@Canvas.size.width * 0.68f, this@Canvas.size.height * 0.42f)
        }
        drawPath(path = path, color = color, style = stroke)
    }
}
