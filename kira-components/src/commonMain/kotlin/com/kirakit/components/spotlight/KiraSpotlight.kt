package com.kirakit.components.spotlight

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.components.platform.KiraHapticFeedbackType
import com.kirakit.components.platform.rememberKiraHaptics
import com.kirakit.components.skeleton.KiraSkeletonListItem
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography

enum class KiraSpotlightSize(val maxWidth: Dp) {
    Sm(448.dp),
    Default(576.dp),
    Lg(672.dp),
    Full(896.dp),
}

@Immutable
data class KiraSpotlightItem<T>(
    val id: String,
    val label: String,
    val description: String? = null,
    val icon: (@Composable (Color) -> Unit)? = null,
    val shortcut: List<String> = emptyList(),
    val keywords: List<String> = emptyList(),
    val scope: String? = null,
    val disabled: Boolean = false,
    val disabledReason: String? = null,
    val data: T? = null,
    val onSelect: (() -> Unit)? = null,
)

@Immutable
data class KiraSpotlightGroup<T>(
    val id: String,
    val label: String,
    val items: List<KiraSpotlightItem<T>>,
)

@Immutable
data class KiraSpotlightScope(
    val id: String,
    val label: String,
    val disabled: Boolean = false,
    val disabledReason: String? = null,
)

/**
 * Default case-insensitive match across label, description, and keywords.
 */
fun <T> matchesSpotlightQuery(item: KiraSpotlightItem<T>, query: String): Boolean {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return true
    if (item.label.lowercase().contains(q)) return true
    if (item.description?.lowercase()?.contains(q) == true) return true
    return item.keywords.any { it.lowercase().contains(q) }
}

/**
 * Filters [items] by optional [scope] (`"all"` or `null` matches everything) and [query].
 */
fun <T> filterSpotlightItems(
    items: List<KiraSpotlightItem<T>>,
    query: String,
    scope: String? = null,
    filter: ((KiraSpotlightItem<T>, String) -> Boolean)? = null,
): List<KiraSpotlightItem<T>> {
    val scoped = if (scope.isNullOrBlank() || scope == "all") {
        items
    } else {
        items.filter { it.scope == scope }
    }
    if (query.isBlank()) return scoped
    val predicate = filter ?: ::matchesSpotlightQuery
    return scoped.filter { predicate(it, query) }
}

/**
 * Prepends [item] to [recents], deduplicating by id and trimming to [maxRecent].
 */
fun <T> recordSpotlightRecent(
    recents: List<KiraSpotlightItem<T>>,
    item: KiraSpotlightItem<T>,
    maxRecent: Int = 5,
): List<KiraSpotlightItem<T>> {
    if (maxRecent <= 0) return emptyList()
    return (listOf(item) + recents.filter { it.id != item.id }).take(maxRecent)
}

/**
 * Search bar trigger button aligned with kira-kit-v2's `SpotlightTrigger`.
 */
@Composable
fun KiraSpotlightTrigger(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Search workspace",
    showShortcut: Boolean = true,
    shortcutLabel: String = "⌘K",
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()

    val bg by animateColorAsState(
        targetValue = if (hovered) colors.accent.copy(alpha = 0.5f) else colors.background,
        animationSpec = KiraMotion.fastTween(),
    )

    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 44.dp)
            .clip(radius.shapeLg)
            .background(bg, radius.shapeLg)
            .border(1.dp, colors.input, radius.shapeLg)
            .hoverable(interaction)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SpotlightSearchGlyph(color = colors.mutedForeground)
        BasicText(
            text = text,
            style = KiraTheme.typography.body.copy(color = colors.mutedForeground),
        )
        if (showShortcut) {
            SpotlightKbdChip(label = shortcutLabel)
        }
    }
}

/**
 * Command-palette / workspace search modal aligned with kira-kit-v2's `Spotlight` & `KiraSpotlight`.
 *
 * - Supports flat `items`, grouped `groups`, filter `scopes` pills, recent history (`rememberRecent`),
 *   custom `filter` or remote `loading` + `errorMessage`/`onRetry`, live result preview (`renderPreview`),
 *   and keyboard navigation (`ArrowUp`, `ArrowDown`, `Enter`, `Escape`).
 * - Adapts cleanly to mobile Android/iOS with safe-area & IME insets so the soft keyboard never covers results.
 */
@Composable
fun <T> KiraSpotlight(
    open: Boolean,
    onOpenChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    items: List<KiraSpotlightItem<T>> = emptyList(),
    groups: List<KiraSpotlightGroup<T>> = emptyList(),
    scopes: List<KiraSpotlightScope> = emptyList(),
    selectedScope: String? = null,
    onScopeChange: ((String) -> Unit)? = null,
    query: String? = null,
    onQueryChange: ((String) -> Unit)? = null,
    onSelect: ((KiraSpotlightItem<T>) -> Unit)? = null,
    recentItems: List<KiraSpotlightItem<T>>? = null,
    rememberRecent: Boolean = false,
    maxRecent: Int = 5,
    onClearRecent: (() -> Unit)? = null,
    filter: ((KiraSpotlightItem<T>, String) -> Boolean)? = null,
    disableFilter: Boolean = false,
    loading: Boolean = false,
    errorMessage: String? = null,
    onRetry: (() -> Unit)? = null,
    closeOnSelect: Boolean = true,
    size: KiraSpotlightSize = KiraSpotlightSize.Default,
    title: String = "Search workspace",
    placeholder: String = "Search resources, commands, people…",
    emptyMessage: String = "No matching results.",
    renderPreview: (@Composable (KiraSpotlightItem<T>) -> Unit)? = null,
    emptyContent: (@Composable () -> Unit)? = null,
) {
    if (!open) return

    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val haptics = rememberKiraHaptics()
    val density = LocalDensity.current
    val windowWidthDp = with(density) { LocalWindowInfo.current.containerSize.width.toDp() }
    val isCompact = windowWidthDp > 0.dp && windowWidthDp < 640.dp

    var localQuery by remember { mutableStateOf("") }
    val currentQuery = query ?: localQuery
    fun updateQuery(next: String) {
        if (query == null) localQuery = next
        onQueryChange?.invoke(next)
    }

    var localScope by remember { mutableStateOf("all") }
    val currentScope = selectedScope ?: localScope
    fun updateScope(next: String) {
        if (selectedScope == null) localScope = next
        onScopeChange?.invoke(next)
    }

    var localRecents by remember { mutableStateOf<List<KiraSpotlightItem<T>>>(emptyList()) }
    val effectiveRecents = recentItems ?: if (rememberRecent) localRecents else emptyList()

    var activeIndex by remember(currentQuery, currentScope, open) { mutableIntStateOf(0) }
    var entered by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        entered = true
    }

    fun closeSpotlight() {
        updateQuery("")
        onOpenChange(false)
    }

    // Build sections: Recents (only when query blank) + Groups + Ungrouped items
    val filteredRecents = remember(effectiveRecents, currentQuery, currentScope) {
        if (currentQuery.isNotBlank()) emptyList()
        else filterSpotlightItems(effectiveRecents, "", currentScope, null)
    }
    val filteredGroups = remember(groups, currentQuery, currentScope, disableFilter, filter) {
        groups.mapNotNull { g ->
            val matched = if (disableFilter) {
                filterSpotlightItems(g.items, "", currentScope, null)
            } else {
                filterSpotlightItems(g.items, currentQuery, currentScope, filter)
            }
            if (matched.isEmpty()) null else g.copy(items = matched)
        }
    }
    val filteredItems = remember(items, currentQuery, currentScope, disableFilter, filter) {
        if (disableFilter) {
            filterSpotlightItems(items, "", currentScope, null)
        } else {
            filterSpotlightItems(items, currentQuery, currentScope, filter)
        }
    }

    val flatNavigable = remember(filteredRecents, filteredGroups, filteredItems) {
        buildList {
            addAll(filteredRecents)
            filteredGroups.forEach { addAll(it.items) }
            addAll(filteredItems)
        }.distinctBy { it.id }
    }

    val safeActiveIndex = activeIndex.coerceIn(0, (flatNavigable.lastIndex).coerceAtLeast(0))
    val highlightedItem = flatNavigable.getOrNull(safeActiveIndex)?.takeIf { !it.disabled && !loading }

    fun activateItem(item: KiraSpotlightItem<T>) {
        if (item.disabled || loading) return
        haptics.perform(KiraHapticFeedbackType.Selection)
        if (rememberRecent && recentItems == null) {
            localRecents = recordSpotlightRecent(localRecents, item, maxRecent)
        }
        item.onSelect?.invoke()
        onSelect?.invoke(item)
        if (closeOnSelect) closeSpotlight()
    }

    Dialog(
        onDismissRequest = ::closeSpotlight,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.50f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = ::closeSpotlight,
                )
                .safeDrawingPadding()
                .imePadding()
                .padding(horizontal = if (isCompact) 12.dp else 24.dp, vertical = if (isCompact) 16.dp else 48.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            AnimatedVisibility(
                visible = entered,
                enter = fadeIn(tween(180, easing = KiraMotion.EaseOutStrong)) +
                    scaleIn(tween(180, easing = KiraMotion.EaseOutStrong), initialScale = 0.96f),
                exit = fadeOut(tween(130)) + scaleOut(tween(130), targetScale = 0.96f),
            ) {
                LaunchedEffect(Unit) {
                    withFrameNanos {}
                    runCatching { focusRequester.requestFocus() }
                }
                Column(
                    modifier = modifier
                        .widthIn(max = size.maxWidth)
                        .fillMaxWidth()
                        .heightIn(max = if (isCompact) 540.dp else 600.dp)
                        .shadow(24.dp, radius.shapeXl)
                        .clip(radius.shapeXl)
                        .background(colors.popover)
                        .border(1.dp, colors.border, radius.shapeXl)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                        )
                        .onPreviewKeyEvent { event ->
                            if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                            when (event.key) {
                                Key.Escape -> {
                                    closeSpotlight()
                                    true
                                }
                                Key.DirectionDown -> {
                                    if (flatNavigable.isNotEmpty()) {
                                        activeIndex = (safeActiveIndex + 1) % flatNavigable.size
                                    }
                                    true
                                }
                                Key.DirectionUp -> {
                                    if (flatNavigable.isNotEmpty()) {
                                        activeIndex = (safeActiveIndex - 1 + flatNavigable.size) % flatNavigable.size
                                    }
                                    true
                                }
                                Key.Enter -> {
                                    highlightedItem?.let { activateItem(it) }
                                    true
                                }
                                else -> false
                            }
                        }
                        .semantics { paneTitle = title },
                ) {
                    // Search input bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 52.dp)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        SpotlightSearchGlyph(color = colors.mutedForeground)
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            if (currentQuery.isEmpty()) {
                                BasicText(
                                    text = placeholder,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = KiraTheme.typography.body.copy(color = colors.mutedForeground),
                                )
                            }
                            BasicTextField(
                                value = currentQuery,
                                onValueChange = ::updateQuery,
                                singleLine = true,
                                textStyle = KiraTheme.typography.body.copy(color = colors.popoverForeground),
                                cursorBrush = SolidColor(colors.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                            )
                        }
                        if (currentQuery.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(radius.shapeMd)
                                    .pointerHoverIcon(PointerIcon.Hand)
                                    .clickable(role = Role.Button, onClick = { updateQuery("") })
                                    .semantics { contentDescription = "Clear search" },
                                contentAlignment = Alignment.Center,
                            ) {
                                SpotlightCloseGlyph(color = colors.mutedForeground)
                            }
                        } else if (!isCompact) {
                            SpotlightKbdChip(label = "ESC")
                        }
                    }

                    Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))

                    // Optional scope chips
                    if (scopes.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SpotlightScopeChip(
                                label = "All",
                                selected = currentScope == "all",
                                enabled = true,
                                onClick = { updateScope("all") },
                            )
                            scopes.forEach { scopeItem ->
                                SpotlightScopeChip(
                                    label = scopeItem.label,
                                    selected = currentScope == scopeItem.id,
                                    enabled = !scopeItem.disabled,
                                    onClick = { updateScope(scopeItem.id) },
                                )
                            }
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                    }

                    // Results body
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        when {
                            loading -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    repeat(3) { KiraSkeletonListItem() }
                                }
                            }
                            errorMessage != null -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .semantics { liveRegion = LiveRegionMode.Assertive },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    BasicText(
                                        text = errorMessage,
                                        style = KiraTheme.typography.bodySmall.copy(color = colors.destructive),
                                    )
                                    if (onRetry != null) {
                                        KiraButton(
                                            text = "Try again",
                                            variant = KiraButtonVariant.Outline,
                                            size = KiraButtonSize.Sm,
                                            onClick = onRetry,
                                        )
                                    }
                                }
                            }
                            flatNavigable.isEmpty() -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 28.dp, horizontal = 16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (emptyContent != null) {
                                        emptyContent()
                                    } else {
                                        BasicText(
                                            text = emptyMessage,
                                            style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                                        )
                                    }
                                }
                            }
                            else -> {
                                if (filteredRecents.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        BasicText(
                                            text = "Recent",
                                            modifier = Modifier.semantics { heading() },
                                            style = KiraTheme.typography.caption.copy(
                                                color = colors.mutedForeground,
                                                fontWeight = FontWeight.SemiBold,
                                            ),
                                        )
                                        if (rememberRecent || onClearRecent != null) {
                                            BasicText(
                                                text = "Clear",
                                                modifier = Modifier
                                                    .clip(radius.shapeSm)
                                                    .pointerHoverIcon(PointerIcon.Hand)
                                                    .clickable(role = Role.Button) {
                                                        localRecents = emptyList()
                                                        onClearRecent?.invoke()
                                                    }
                                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = KiraTheme.typography.caption.copy(
                                                    color = colors.primary,
                                                    fontWeight = FontWeight.Medium,
                                                ),
                                            )
                                        }
                                    }
                                    filteredRecents.forEach { item ->
                                        val isActive = highlightedItem?.id == item.id
                                        SpotlightResultRow(
                                            item = item,
                                            active = isActive,
                                            isCompact = isCompact,
                                            onHover = {
                                                val idx = flatNavigable.indexOfFirst { it.id == item.id }
                                                if (idx >= 0) activeIndex = idx
                                            },
                                            onClick = { activateItem(item) },
                                        )
                                    }
                                }

                                filteredGroups.forEach { group ->
                                    BasicText(
                                        text = group.label,
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                            .semantics { heading() },
                                        style = KiraTheme.typography.caption.copy(
                                            color = colors.mutedForeground,
                                            fontWeight = FontWeight.SemiBold,
                                        ),
                                    )
                                    group.items.forEach { item ->
                                        val isActive = highlightedItem?.id == item.id
                                        SpotlightResultRow(
                                            item = item,
                                            active = isActive,
                                            isCompact = isCompact,
                                            onHover = {
                                                val idx = flatNavigable.indexOfFirst { it.id == item.id }
                                                if (idx >= 0) activeIndex = idx
                                            },
                                            onClick = { activateItem(item) },
                                        )
                                    }
                                }

                                if (filteredItems.isNotEmpty()) {
                                    if (filteredGroups.isNotEmpty() || filteredRecents.isNotEmpty()) {
                                        BasicText(
                                            text = "Results",
                                            modifier = Modifier
                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                                .semantics { heading() },
                                            style = KiraTheme.typography.caption.copy(
                                                color = colors.mutedForeground,
                                                fontWeight = FontWeight.SemiBold,
                                            ),
                                        )
                                    }
                                    filteredItems.forEach { item ->
                                        val isActive = highlightedItem?.id == item.id
                                        SpotlightResultRow(
                                            item = item,
                                            active = isActive,
                                            isCompact = isCompact,
                                            onHover = {
                                                val idx = flatNavigable.indexOfFirst { it.id == item.id }
                                                if (idx >= 0) activeIndex = idx
                                            },
                                            onClick = { activateItem(item) },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Optional preview pane for active result
                    if (highlightedItem != null && renderPreview != null) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 150.dp)
                                .background(colors.muted.copy(alpha = 0.25f))
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            renderPreview(highlightedItem)
                        }
                    }

                    // Footer bar
                    Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val countText = when {
                            loading -> "Searching…"
                            flatNavigable.size == 1 -> "1 result"
                            else -> "${flatNavigable.size} results"
                        }
                        BasicText(
                            text = countText,
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                            style = KiraTheme.typography.caption.copy(color = colors.mutedForeground),
                        )
                        if (!isCompact) {
                            BasicText(
                                text = "↑↓ Navigate · ↵ Open · Esc Close",
                                style = KiraTheme.typography.caption.copy(color = colors.mutedForeground),
                            )
                        } else {
                            BasicText(
                                text = "Close",
                                modifier = Modifier
                                    .clip(radius.shapeSm)
                                    .clickable(role = Role.Button, onClick = ::closeSpotlight)
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                style = KiraTheme.typography.caption.copy(
                                    color = colors.primary,
                                    fontWeight = FontWeight.Medium,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpotlightScopeChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val bg by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.muted.copy(alpha = 0.6f),
        animationSpec = KiraMotion.fastTween(),
    )
    val fg = if (selected) colors.primaryForeground else colors.foreground

    Box(
        modifier = Modifier
            .defaultMinSize(minHeight = 32.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(radius.shapeMd)
            .background(bg)
            .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
            .clickable(enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .semantics {
                this.selected = selected
                if (!enabled) disabled()
            }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = label,
            style = KiraTheme.typography.caption.copy(
                color = fg,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun <T> SpotlightResultRow(
    item: KiraSpotlightItem<T>,
    active: Boolean,
    isCompact: Boolean,
    onHover: () -> Unit,
    onClick: () -> Unit,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()

    LaunchedEffect(hovered) {
        if (hovered && !item.disabled) onHover()
    }

    val bg by animateColorAsState(
        targetValue = when {
            (active || hovered) && !item.disabled -> colors.accent
            else -> Color.Transparent
        },
        animationSpec = KiraMotion.fastTween(),
    )
    val fg = if ((active || hovered) && !item.disabled) colors.accentForeground else colors.popoverForeground

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 44.dp)
            .alpha(if (item.disabled) 0.52f else 1f)
            .clip(radius.shapeMd)
            .background(bg)
            .hoverable(interaction, enabled = !item.disabled)
            .pointerHoverIcon(if (item.disabled) PointerIcon.Default else PointerIcon.Hand)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = !item.disabled,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics(mergeDescendants = true) {
                selected = active
                if (item.disabled) disabled()
            }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (item.icon != null) {
            Box(Modifier.size(18.dp), contentAlignment = Alignment.Center) {
                item.icon.invoke(fg)
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            BasicText(
                text = item.label,
                style = KiraTheme.typography.body.copy(
                    color = fg,
                    fontWeight = FontWeight.Medium,
                ),
            )
            if (item.description != null) {
                BasicText(
                    text = item.description,
                    style = KiraTheme.typography.caption.copy(color = colors.mutedForeground),
                )
            }
            if (item.disabledReason != null) {
                BasicText(
                    text = item.disabledReason,
                    style = KiraTheme.typography.caption.copy(color = colors.mutedForeground),
                )
            }
        }
        if (!isCompact && item.shortcut.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                item.shortcut.forEach { key ->
                    SpotlightKbdChip(label = key)
                }
            }
        }
    }
}

@Composable
private fun SpotlightKbdChip(label: String) {
    val colors = KiraTheme.colors
    Box(
        modifier = Modifier
            .height(20.dp)
            .widthIn(min = 20.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(colors.muted)
            .border(1.dp, colors.border, RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = label,
            style = KiraTheme.typography.caption.copy(
                color = colors.mutedForeground,
                fontSize = 11.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun SpotlightSearchGlyph(color: Color) {
    Canvas(Modifier.size(16.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        val radius = size.minDimension * 0.32f
        val center = Offset(size.width * 0.44f, size.height * 0.44f)
        drawCircle(color = color, radius = radius, center = center, style = stroke)
        drawLine(
            color = color,
            start = Offset(center.x + radius * 0.72f, center.y + radius * 0.72f),
            end = Offset(size.width * 0.88f, size.height * 0.88f),
            strokeWidth = 1.6.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun SpotlightCloseGlyph(color: Color) {
    Canvas(Modifier.size(12.dp).clearAndSetSemantics {}) {
        val stroke = 1.6.dp.toPx()
        val pad = size.width * 0.2f
        drawLine(color, Offset(pad, pad), Offset(size.width - pad, size.height - pad), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width - pad, pad), Offset(pad, size.height - pad), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}
