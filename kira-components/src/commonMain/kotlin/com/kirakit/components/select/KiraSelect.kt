package com.kirakit.components.select

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.kirakit.components.input.KiraInput
import com.kirakit.components.input.KiraInputSize
import com.kirakit.components.platform.KiraHapticFeedbackType
import com.kirakit.components.platform.rememberKiraHaptics
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography
import kotlinx.coroutines.delay

enum class KiraSelectSize(
    val minHeight: Dp,
    internal val horizontalPadding: Dp,
    internal val fontSize: Int,
) {
    Xs(28.dp, 8.dp, 12),
    Sm(32.dp, 12.dp, 13),
    Default(36.dp, 14.dp, 14),
    Lg(40.dp, 16.dp, 14),
    Xl(44.dp, 18.dp, 15),
}

enum class KiraSelectVariant {
    Default,
    Outline,
    Ghost,
}

@Immutable
data class KiraSelectOption(
    val value: String,
    val label: String,
    val disabled: Boolean = false,
)

@Immutable
data class KiraSelectGroup(
    val label: String,
    val options: List<KiraSelectOption>,
)

/**
 * Matches an option label against a search query, supporting substring or subsequence fuzzy matching
 * (aligned with `subsequenceMatch` in `select.tsx`).
 */
fun matchesSelectQuery(label: String, query: String, fuzzy: Boolean = true): Boolean {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return true
    val l = label.lowercase()
    if (!fuzzy) return l.contains(q)
    var li = 0
    for (ch in q) {
        val found = l.indexOf(ch, li)
        if (found < 0) return false
        li = found + 1
    }
    return true
}

/**
 * Single-value Select aligned with kira-kit-v2's `Select` & `KiraSelect`.
 *
 * Supports 5 size tiers, 3 variants, grouped options, searchable fuzzy filtering, creatable options,
 * debounced async search, animated dropdown popup, rotating trigger chevron, and mobile haptics.
 */
@Composable
fun KiraSelect(
    options: List<KiraSelectOption>,
    value: String?,
    onValueChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
    groups: List<KiraSelectGroup> = emptyList(),
    placeholder: String = "Select an option…",
    size: KiraSelectSize = KiraSelectSize.Default,
    variant: KiraSelectVariant = KiraSelectVariant.Default,
    searchable: Boolean = false,
    fuzzyMatching: Boolean = true,
    creatable: Boolean = false,
    createLabel: String = "Create",
    onCreateOption: ((String) -> Unit)? = null,
    asyncSearch: (suspend (String) -> List<KiraSelectOption>)? = null,
    debounceMillis: Long = 250L,
    clearable: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    invalid: Boolean = false,
    hideChevron: Boolean = false,
    leftIcon: (@Composable () -> Unit)? = null,
    emptyMessage: String = "No options found.",
    loadingMessage: String = "Loading…",
) {
    KiraSelectCore(
        flatOptions = options,
        groups = groups,
        selectedValues = if (value != null) setOf(value) else emptySet(),
        multiple = false,
        onToggleValue = { toggled ->
            onValueChange(toggled)
        },
        onClearAll = { onValueChange(null) },
        modifier = modifier,
        placeholder = placeholder,
        size = size,
        variant = variant,
        searchable = searchable,
        fuzzyMatching = fuzzyMatching,
        creatable = creatable,
        createLabel = createLabel,
        onCreateOption = onCreateOption,
        asyncSearch = asyncSearch,
        debounceMillis = debounceMillis,
        clearable = clearable,
        maxDisplayedTags = 1,
        enabled = enabled,
        readOnly = readOnly,
        invalid = invalid,
        hideChevron = hideChevron,
        leftIcon = leftIcon,
        emptyMessage = emptyMessage,
        loadingMessage = loadingMessage,
    )
}

/**
 * Multi-select combobox aligned with kira-kit-v2's `KiraSelect(multiple = true)`.
 *
 * Displays removable tag chips with `maxDisplayedTags` overflow counter (`+N`), clear-all button,
 * search filtering, creatable tags, and async search.
 */
@Composable
fun KiraMultiSelect(
    options: List<KiraSelectOption>,
    selectedValues: Set<String>,
    onSelectedValuesChange: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
    groups: List<KiraSelectGroup> = emptyList(),
    placeholder: String = "Select options…",
    size: KiraSelectSize = KiraSelectSize.Default,
    variant: KiraSelectVariant = KiraSelectVariant.Default,
    searchable: Boolean = true,
    fuzzyMatching: Boolean = true,
    creatable: Boolean = false,
    createLabel: String = "Create",
    onCreateOption: ((String) -> Unit)? = null,
    asyncSearch: (suspend (String) -> List<KiraSelectOption>)? = null,
    debounceMillis: Long = 250L,
    clearable: Boolean = true,
    maxDisplayedTags: Int = 3,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    invalid: Boolean = false,
    hideChevron: Boolean = false,
    leftIcon: (@Composable () -> Unit)? = null,
    emptyMessage: String = "No options found.",
    loadingMessage: String = "Loading…",
) {
    KiraSelectCore(
        flatOptions = options,
        groups = groups,
        selectedValues = selectedValues,
        multiple = true,
        onToggleValue = { toggled ->
            val next = if (toggled in selectedValues) selectedValues - toggled else selectedValues + toggled
            onSelectedValuesChange(next)
        },
        onClearAll = { onSelectedValuesChange(emptySet()) },
        modifier = modifier,
        placeholder = placeholder,
        size = size,
        variant = variant,
        searchable = searchable,
        fuzzyMatching = fuzzyMatching,
        creatable = creatable,
        createLabel = createLabel,
        onCreateOption = onCreateOption,
        asyncSearch = asyncSearch,
        debounceMillis = debounceMillis,
        clearable = clearable,
        maxDisplayedTags = maxDisplayedTags,
        enabled = enabled,
        readOnly = readOnly,
        invalid = invalid,
        hideChevron = hideChevron,
        leftIcon = leftIcon,
        emptyMessage = emptyMessage,
        loadingMessage = loadingMessage,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KiraSelectCore(
    flatOptions: List<KiraSelectOption>,
    groups: List<KiraSelectGroup>,
    selectedValues: Set<String>,
    multiple: Boolean,
    onToggleValue: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier,
    placeholder: String,
    size: KiraSelectSize,
    variant: KiraSelectVariant,
    searchable: Boolean,
    fuzzyMatching: Boolean,
    creatable: Boolean,
    createLabel: String,
    onCreateOption: ((String) -> Unit)?,
    asyncSearch: (suspend (String) -> List<KiraSelectOption>)?,
    debounceMillis: Long,
    clearable: Boolean,
    maxDisplayedTags: Int,
    enabled: Boolean,
    readOnly: Boolean,
    invalid: Boolean,
    hideChevron: Boolean,
    leftIcon: (@Composable () -> Unit)?,
    emptyMessage: String,
    loadingMessage: String,
) {
    val colors = KiraTheme.colors
    val isDark = colors.background.luminance() < 0.5f
    val haptics = rememberKiraHaptics()
    val density = LocalDensity.current
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()

    var open by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var anchorWidthPx by remember { mutableStateOf(0) }
    var asyncOptions by remember { mutableStateOf<List<KiraSelectOption>>(emptyList()) }
    var loadingAsync by remember { mutableStateOf(false) }

    val interactive = enabled && !readOnly
    val shape = KiraTheme.radius.shapeLg

    val transitionState = remember { MutableTransitionState(false) }
    transitionState.targetState = open

    LaunchedEffect(open, searchQuery, asyncSearch) {
        if (!open || asyncSearch == null) return@LaunchedEffect
        loadingAsync = true
        if (debounceMillis > 0L) delay(debounceMillis)
        asyncOptions = runCatching { asyncSearch(searchQuery) }.getOrDefault(emptyList())
        loadingAsync = false
    }

    val allKnownOptions = remember(flatOptions, groups, asyncOptions) {
        (flatOptions + groups.flatMap { it.options } + asyncOptions).associateBy { it.value }
    }

    val chevronRotation by animateFloatAsState(
        targetValue = if (open) 180f else 0f,
        animationSpec = KiraMotion.fastTween(),
        label = "SelectChevron",
    )

    val triggerBg by animateColorAsState(
        targetValue = when (variant) {
            KiraSelectVariant.Ghost -> if (hovered && interactive) colors.muted.copy(alpha = 0.5f) else Color.Transparent
            else -> if (isDark) colors.input.copy(alpha = if (hovered && interactive) 0.5f else 0.3f) else Color.Transparent
        },
        animationSpec = KiraMotion.fastTween(),
        label = "SelectTriggerBg",
    )

    val triggerBorder by animateColorAsState(
        targetValue = when {
            invalid -> colors.destructive
            focused || open -> colors.ring
            variant == KiraSelectVariant.Ghost -> Color.Transparent
            else -> colors.input
        },
        animationSpec = KiraMotion.fastTween(),
        label = "SelectTriggerBorder",
    )

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { anchorWidthPx = it.width }
                .heightIn(min = size.minHeight.coerceAtLeast(44.dp))
                .alpha(if (enabled) 1f else 0.5f)
                .clip(shape)
                .background(triggerBg, shape)
                .then(
                    if (triggerBorder != Color.Transparent) {
                        Modifier.border(if (focused || open || invalid) 2.dp else 1.dp, triggerBorder, shape)
                    } else {
                        Modifier
                    },
                )
                .then(if (interactive) Modifier.pointerHoverIcon(PointerIcon.Hand) else Modifier)
                .hoverable(interactionSource, enabled = interactive)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = interactive,
                    role = Role.DropdownList,
                    onClick = {
                        haptics.perform(KiraHapticFeedbackType.Selection)
                        open = !open
                    },
                )
                .padding(horizontal = size.horizontalPadding, vertical = 6.dp)
                .semantics {
                    stateDescription = if (open) "Expanded" else "Collapsed"
                    if (!enabled) disabled()
                    if (invalid) error("Invalid selection")
                },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leftIcon != null) {
                Box(Modifier.size(16.dp), contentAlignment = Alignment.Center) { leftIcon() }
            }

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (selectedValues.isEmpty()) {
                    BasicText(
                        text = placeholder,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = KiraTheme.typography.body.copy(
                            color = colors.mutedForeground,
                            fontSize = size.fontSize.sp,
                        ),
                    )
                } else if (!multiple) {
                    val v = selectedValues.first()
                    val label = allKnownOptions[v]?.label ?: v
                    BasicText(
                        text = label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = KiraTheme.typography.body.copy(
                            color = colors.foreground,
                            fontSize = size.fontSize.sp,
                        ),
                    )
                } else {
                    val ordered = selectedValues.toList()
                    val visible = ordered.take(maxDisplayedTags.coerceAtLeast(1))
                    val overflow = ordered.size - visible.size
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        visible.forEach { v ->
                            val label = allKnownOptions[v]?.label ?: v
                            SelectTagChip(
                                label = label,
                                enabled = interactive,
                                onRemove = {
                                    haptics.perform(KiraHapticFeedbackType.Light)
                                    onToggleValue(v)
                                },
                            )
                        }
                        if (overflow > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(KiraTheme.radius.shapeSm)
                                    .background(colors.muted)
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            ) {
                                BasicText(
                                    text = "+$overflow",
                                    style = KiraTheme.typography.caption.copy(
                                        color = colors.mutedForeground,
                                        fontFeatureSettings = "tnum",
                                    ),
                                )
                            }
                        }
                    }
                }
            }

            if (clearable && selectedValues.isNotEmpty() && interactive) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .clickable(
                            role = Role.Button,
                            onClick = {
                                haptics.perform(KiraHapticFeedbackType.Light)
                                onClearAll()
                            },
                        )
                        .semantics { contentDescription = "Clear selection" },
                    contentAlignment = Alignment.Center,
                ) {
                    SelectClearGlyph(color = colors.mutedForeground)
                }
            }

            if (!hideChevron) {
                Canvas(
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer { rotationZ = chevronRotation }
                        .clearAndSetSemantics {},
                ) {
                    val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    val path = Path().apply {
                        moveTo(this@Canvas.size.width * 0.22f, this@Canvas.size.height * 0.36f)
                        lineTo(this@Canvas.size.width * 0.50f, this@Canvas.size.height * 0.66f)
                        lineTo(this@Canvas.size.width * 0.78f, this@Canvas.size.height * 0.36f)
                    }
                    drawPath(path, colors.mutedForeground, style = stroke)
                }
            }
        }

        if (transitionState.currentState || transitionState.targetState) {
            val provider = remember(density) {
                SelectDropdownPositionProvider(gapPx = with(density) { 4.dp.roundToPx() })
            }
            val popupWidthDp = with(density) { anchorWidthPx.toDp() }.coerceAtLeast(180.dp)

            Popup(
                popupPositionProvider = provider,
                onDismissRequest = { open = false },
                properties = PopupProperties(
                    focusable = true,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                ),
            ) {
                AnimatedVisibility(
                    visibleState = transitionState,
                    modifier = Modifier.padding(4.dp),
                    enter = fadeIn(tween(140, easing = KiraMotion.EaseOutStrong)) +
                        scaleIn(tween(140, easing = KiraMotion.EaseOutStrong), initialScale = 0.96f) +
                        slideInVertically(tween(140, easing = KiraMotion.EaseOutStrong)) { -8 },
                    exit = fadeOut(tween(110, easing = KiraMotion.EaseOutStrong)) +
                        scaleOut(tween(110, easing = KiraMotion.EaseOutStrong), targetScale = 0.96f) +
                        slideOutVertically(tween(110, easing = KiraMotion.EaseOutStrong)) { -8 },
                ) {
                    Column(
                        modifier = Modifier
                            .width(popupWidthDp)
                            .shadow(10.dp, shape, clip = false)
                            .clip(shape)
                            .background(colors.popover, shape)
                            .border(1.dp, colors.border, shape)
                            .padding(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (searchable || asyncSearch != null || creatable) {
                            KiraInput(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = "Search…",
                                size = KiraInputSize.Sm,
                                clearable = true,
                                modifier = Modifier.fillMaxWidth().padding(4.dp),
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 260.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            if (loadingAsync) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    SelectSpinner(color = colors.mutedForeground)
                                    BasicText(
                                        text = loadingMessage,
                                        style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                                    )
                                }
                            } else {
                                val effectiveFlat = remember(asyncSearch, asyncOptions, flatOptions, searchQuery, fuzzyMatching) {
                                    if (asyncSearch != null) {
                                        asyncOptions
                                    } else {
                                        flatOptions.filter { matchesSelectQuery(it.label, searchQuery, fuzzyMatching) }
                                    }
                                }
                                val effectiveGroups = remember(asyncSearch, groups, searchQuery, fuzzyMatching) {
                                    if (asyncSearch != null) {
                                        emptyList()
                                    } else {
                                        groups.mapNotNull { g ->
                                            val matching = g.options.filter { matchesSelectQuery(it.label, searchQuery, fuzzyMatching) }
                                            if (matching.isEmpty()) null else g.copy(options = matching)
                                        }
                                    }
                                }

                                val hasAny = effectiveFlat.isNotEmpty() || effectiveGroups.isNotEmpty()
                                val trimmedQuery = searchQuery.trim()
                                val exactExists = allKnownOptions.values.any { it.label.equals(trimmedQuery, ignoreCase = true) }

                                if (effectiveGroups.isNotEmpty()) {
                                    effectiveGroups.forEachIndexed { gi, group ->
                                        if (gi > 0) {
                                            Box(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 2.dp)
                                                    .height(1.dp)
                                                    .background(colors.border),
                                            )
                                        }
                                        BasicText(
                                            text = group.label,
                                            style = KiraTheme.typography.caption.copy(
                                                color = colors.mutedForeground,
                                                fontWeight = FontWeight.SemiBold,
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        )
                                        group.options.forEach { option ->
                                            SelectOptionRow(
                                                option = option,
                                                selected = option.value in selectedValues,
                                                onClick = {
                                                    haptics.perform(KiraHapticFeedbackType.Selection)
                                                    onToggleValue(option.value)
                                                    if (!multiple) open = false
                                                },
                                            )
                                        }
                                    }
                                } else {
                                    effectiveFlat.forEach { option ->
                                        SelectOptionRow(
                                            option = option,
                                            selected = option.value in selectedValues,
                                            onClick = {
                                                haptics.perform(KiraHapticFeedbackType.Selection)
                                                onToggleValue(option.value)
                                                if (!multiple) open = false
                                            },
                                        )
                                    }
                                }

                                if (creatable && trimmedQuery.isNotEmpty() && !exactExists && onCreateOption != null) {
                                    SelectOptionRow(
                                        option = KiraSelectOption(trimmedQuery, "$createLabel \"$trimmedQuery\""),
                                        selected = false,
                                        onClick = {
                                            haptics.perform(KiraHapticFeedbackType.Selection)
                                            onCreateOption(trimmedQuery)
                                            searchQuery = ""
                                            if (!multiple) open = false
                                        },
                                    )
                                } else if (!hasAny) {
                                    BasicText(
                                        text = emptyMessage,
                                        style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                                        modifier = Modifier.padding(12.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectOptionRow(
    option: KiraSelectOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = KiraTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val shape = KiraTheme.radius.shapeMd

    val bg by animateColorAsState(
        targetValue = when {
            hovered && !option.disabled -> colors.accent
            selected -> colors.accent.copy(alpha = 0.55f)
            else -> Color.Transparent
        },
        animationSpec = KiraMotion.fastTween(),
        label = "SelectItemBg",
    )
    val fg = if (hovered || selected) colors.accentForeground else colors.popoverForeground

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 40.dp)
            .alpha(if (option.disabled) 0.45f else 1f)
            .clip(shape)
            .background(bg, shape)
            .then(if (!option.disabled) Modifier.pointerHoverIcon(PointerIcon.Hand) else Modifier)
            .hoverable(interactionSource, enabled = !option.disabled)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !option.disabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(
            text = option.label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            style = KiraTheme.typography.body.copy(
                color = fg,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            ),
        )
        if (selected) {
            Canvas(Modifier.size(14.dp).clearAndSetSemantics {}) {
                val stroke = Stroke(width = 1.9.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                val path = Path().apply {
                    moveTo(size.width * 0.16f, size.height * 0.52f)
                    lineTo(size.width * 0.40f, size.height * 0.76f)
                    lineTo(size.width * 0.84f, size.height * 0.26f)
                }
                drawPath(path, fg, style = stroke)
            }
        }
    }
}

@Composable
private fun SelectTagChip(
    label: String,
    enabled: Boolean,
    onRemove: () -> Unit,
) {
    val colors = KiraTheme.colors
    val shape = KiraTheme.radius.shapeSm
    Row(
        modifier = Modifier
            .clip(shape)
            .background(colors.secondary, shape)
            .padding(start = 8.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = KiraTheme.typography.caption.copy(color = colors.secondaryForeground),
        )
        if (enabled) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onRemove)
                    .semantics { contentDescription = "Remove $label" },
                contentAlignment = Alignment.Center,
            ) {
                SelectClearGlyph(color = colors.secondaryForeground)
            }
        }
    }
}

@Composable
private fun SelectClearGlyph(color: Color) {
    Canvas(Modifier.size(10.dp).clearAndSetSemantics {}) {
        val sw = 1.5.dp.toPx()
        val p = size.width * 0.2f
        drawLine(color, Offset(p, p), Offset(size.width - p, size.height - p), sw, StrokeCap.Round)
        drawLine(color, Offset(size.width - p, p), Offset(p, size.height - p), sw, StrokeCap.Round)
    }
}

@Composable
private fun SelectSpinner(color: Color) {
    val transition = rememberInfiniteTransition(label = "SelectSpinner")
    val rotationState = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "SelectSpinnerRotation",
    )
    Box(
        Modifier
            .size(14.dp)
            .clearAndSetSemantics {}
            .drawWithCache {
                val strokeWidth = 1.8.dp.toPx()
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

private class SelectDropdownPositionProvider(private val gapPx: Int) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val belowY = anchorBounds.bottom + gapPx
        val aboveY = anchorBounds.top - popupContentSize.height - gapPx
        val y = if (belowY + popupContentSize.height <= windowSize.height || aboveY < 0) belowY else aboveY
        val x = anchorBounds.left.coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))
        return IntOffset(x, y.coerceIn(0, (windowSize.height - popupContentSize.height).coerceAtLeast(0)))
    }
}
