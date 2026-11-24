package com.kirakit.components.dropdownmenu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
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

enum class KiraDropdownMenuAlign {
    Start,
    Center,
    End,
}

enum class KiraDropdownMenuSide {
    Bottom,
    Top,
}

enum class KiraMenuItemVariant {
    Default,
    Destructive,
}

@Immutable
data class KiraMenuRadioOption(
    val value: String,
    val label: String,
    val icon: (@Composable (Color) -> Unit)? = null,
    val disabled: Boolean = false,
    val disabledReason: String? = null,
)

/**
 * Data model for data-driven [KiraDropdownMenu] and [com.kirakit.components.menubar.KiraMenubar].
 */
sealed interface KiraMenuItemData {
    val id: String

    @Immutable
    data class Action(
        override val id: String,
        val label: String,
        val icon: (@Composable (Color) -> Unit)? = null,
        val shortcut: String? = null,
        val variant: KiraMenuItemVariant = KiraMenuItemVariant.Default,
        val disabled: Boolean = false,
        val disabledReason: String? = null,
        val loading: Boolean = false,
        val closeOnClick: Boolean = true,
        val onSelect: () -> Unit = {},
    ) : KiraMenuItemData

    @Immutable
    data class Checkbox(
        override val id: String,
        val label: String,
        val checked: Boolean,
        val icon: (@Composable (Color) -> Unit)? = null,
        val disabled: Boolean = false,
        val disabledReason: String? = null,
        val closeOnClick: Boolean = false,
        val onCheckedChange: (Boolean) -> Unit,
    ) : KiraMenuItemData

    @Immutable
    data class RadioGroup(
        override val id: String,
        val label: String? = null,
        val selectedValue: String,
        val options: List<KiraMenuRadioOption>,
        val closeOnClick: Boolean = false,
        val onValueChange: (String) -> Unit,
    ) : KiraMenuItemData

    @Immutable
    data class Submenu(
        override val id: String,
        val label: String,
        val icon: (@Composable (Color) -> Unit)? = null,
        val disabled: Boolean = false,
        val disabledReason: String? = null,
        val searchable: Boolean = false,
        val items: List<KiraMenuItemData>,
    ) : KiraMenuItemData

    @Immutable
    data class Group(
        override val id: String,
        val label: String? = null,
        val items: List<KiraMenuItemData>,
    ) : KiraMenuItemData

    @Immutable
    data class Separator(
        override val id: String,
    ) : KiraMenuItemData
}

/**
 * Recursively filters a [KiraMenuItemData] tree by case-insensitive substring query,
 * dropping empty groups/submenus and collapsing redundant separators.
 */
fun filterKiraMenuItems(items: List<KiraMenuItemData>, query: String): List<KiraMenuItemData> {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return items

    val result = mutableListOf<KiraMenuItemData>()
    for (item in items) {
        when (item) {
            is KiraMenuItemData.Action -> {
                if (item.label.lowercase().contains(q) ||
                    (item.disabledReason?.lowercase()?.contains(q) == true)
                ) {
                    result.add(item)
                }
            }
            is KiraMenuItemData.Checkbox -> {
                if (item.label.lowercase().contains(q)) result.add(item)
            }
            is KiraMenuItemData.RadioGroup -> {
                val matchedOptions = item.options.filter { it.label.lowercase().contains(q) }
                if (matchedOptions.isNotEmpty()) {
                    result.add(item.copy(options = matchedOptions))
                }
            }
            is KiraMenuItemData.Submenu -> {
                val matchedChildren = filterKiraMenuItems(item.items, q)
                if (matchedChildren.isNotEmpty() || item.label.lowercase().contains(q)) {
                    result.add(
                        item.copy(
                            items = if (matchedChildren.isNotEmpty()) matchedChildren else item.items,
                        ),
                    )
                }
            }
            is KiraMenuItemData.Group -> {
                val matchedChildren = filterKiraMenuItems(item.items, q)
                if (matchedChildren.isNotEmpty()) {
                    result.add(item.copy(items = matchedChildren))
                }
            }
            is KiraMenuItemData.Separator -> Unit
        }
    }
    return result
}

/**
 * Shared detached-menu handle: lets multiple triggers open one shared [KiraDropdownMenu]
 * with per-trigger payloads (aligned with `createDropdownMenuHandle` in kira-kit-v2).
 */
@Stable
class KiraDropdownMenuHandle<T> {
    var payload by mutableStateOf<T?>(null)
        private set

    val isOpen: Boolean get() = payload != null

    fun open(payload: T) {
        this.payload = payload
    }

    fun close() {
        this.payload = null
    }
}

@Composable
fun <T> rememberKiraDropdownMenuHandle(): KiraDropdownMenuHandle<T> =
    remember { KiraDropdownMenuHandle() }

/**
 * Scope available inside a [KiraDropdownMenu] popup, allowing items to dismiss the menu cleanly.
 */
@Stable
interface KiraDropdownMenuScope {
    fun close()
}

internal val LocalDropdownMenuScope = compositionLocalOf<KiraDropdownMenuScope> {
    object : KiraDropdownMenuScope {
        override fun close() = Unit
    }
}

/**
 * Composable Dropdown Menu aligned with kira-kit-v2's `DropdownMenu`.
 *
 * Supports icons, shortcuts, destructive items, checkbox & radio groups, nested submenus,
 * loading spinners, disabled explanations (`disabledReason`), optional backdrop overlay,
 * and optional directional arrow.
 */
@Composable
fun KiraDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    trigger: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    align: KiraDropdownMenuAlign = KiraDropdownMenuAlign.Start,
    side: KiraDropdownMenuSide = KiraDropdownMenuSide.Bottom,
    showBackdrop: Boolean = false,
    showArrow: Boolean = false,
    minWidth: Dp = 200.dp,
    maxWidth: Dp = 320.dp,
    content: @Composable KiraDropdownMenuScope.() -> Unit,
) {
    val transitionState = remember { MutableTransitionState(false) }
    transitionState.targetState = expanded

    val density = LocalDensity.current
    val positionProvider = remember(density, align, side, showArrow) {
        DropdownPopupPositionProvider(
            align = align,
            side = side,
            gapPx = with(density) { (if (showArrow) 8.dp else 4.dp).roundToPx() },
            marginPx = with(density) { 8.dp.roundToPx() },
        )
    }

    val menuScope = remember(onDismissRequest) {
        object : KiraDropdownMenuScope {
            override fun close() = onDismissRequest()
        }
    }

    Box(modifier = modifier) {
        trigger()

        if (transitionState.currentState || transitionState.targetState) {
            if (showBackdrop) {
                Popup(
                    alignment = Alignment.TopStart,
                    onDismissRequest = onDismissRequest,
                    properties = PopupProperties(
                        focusable = false,
                        dismissOnBackPress = false,
                        dismissOnClickOutside = false,
                    ),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.38f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onDismissRequest,
                            ),
                    )
                }
            }

            Popup(
                popupPositionProvider = positionProvider,
                onDismissRequest = onDismissRequest,
                properties = PopupProperties(
                    focusable = true,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                ),
            ) {
                AnimatedVisibility(
                    visibleState = transitionState,
                    enter = fadeIn(tween(180, easing = KiraMotion.EaseOutStrong)) +
                        scaleIn(tween(180, easing = KiraMotion.EaseOutStrong), initialScale = 0.95f),
                    exit = fadeOut(tween(130, easing = KiraMotion.EaseOutStrong)) +
                        scaleOut(tween(130, easing = KiraMotion.EaseOutStrong), targetScale = 0.95f),
                ) {
                    KiraMenuPopupSurface(
                        minWidth = minWidth,
                        maxWidth = maxWidth,
                        showArrow = showArrow,
                        align = align,
                        onEscape = onDismissRequest,
                    ) {
                        CompositionLocalProvider(LocalDropdownMenuScope provides menuScope) {
                            menuScope.content()
                        }
                    }
                }
            }
        }
    }
}

/**
 * Data-driven Dropdown Menu aligned with kira-kit-v2's `KiraDropdownMenu`.
 *
 * Manages open state internally (or externally via `expanded`/`onExpandedChange`), supports
 * live search filtering (`searchable = true`), nested submenus, checkbox/radio items,
 * and disabled reasons.
 */
@Composable
fun KiraDropdownMenu(
    items: List<KiraMenuItemData>,
    trigger: @Composable (onToggle: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean? = null,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    searchable: Boolean = false,
    searchPlaceholder: String = "Search actions…",
    emptyMessage: String = "No matching items.",
    align: KiraDropdownMenuAlign = KiraDropdownMenuAlign.Start,
    side: KiraDropdownMenuSide = KiraDropdownMenuSide.Bottom,
    showBackdrop: Boolean = false,
    showArrow: Boolean = false,
    minWidth: Dp = 224.dp,
    maxWidth: Dp = 320.dp,
) {
    var internalExpanded by remember { mutableStateOf(false) }
    val isOpen = expanded ?: internalExpanded
    fun setOpen(next: Boolean) {
        if (expanded == null) internalExpanded = next
        onExpandedChange?.invoke(next)
    }

    var searchQuery by remember(isOpen) { mutableStateOf("") }
    val filteredItems = remember(items, searchQuery, searchable) {
        if (searchable) filterKiraMenuItems(items, searchQuery) else items
    }

    KiraDropdownMenu(
        expanded = isOpen,
        onDismissRequest = { setOpen(false) },
        modifier = modifier,
        align = align,
        side = side,
        showBackdrop = showBackdrop,
        showArrow = showArrow,
        minWidth = minWidth,
        maxWidth = maxWidth,
        trigger = { trigger { setOpen(!isOpen) } },
    ) {
        if (searchable) {
            Box(modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)) {
                KiraInput(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = searchPlaceholder,
                    size = KiraInputSize.Sm,
                    clearable = true,
                )
            }
            KiraDropdownMenuSeparator()
        }
        if (filteredItems.isEmpty()) {
            BasicText(
                text = emptyMessage,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                style = KiraTheme.typography.bodySmall.copy(color = KiraTheme.colors.mutedForeground),
            )
        } else {
            RenderKiraMenuItems(items = filteredItems)
        }
    }
}

@Composable
internal fun KiraMenuPopupSurface(
    minWidth: Dp,
    maxWidth: Dp,
    showArrow: Boolean,
    align: KiraDropdownMenuAlign,
    onEscape: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius

    Column(
        horizontalAlignment = when (align) {
            KiraDropdownMenuAlign.Start -> Alignment.Start
            KiraDropdownMenuAlign.Center -> Alignment.CenterHorizontally
            KiraDropdownMenuAlign.End -> Alignment.End
        },
        modifier = Modifier.onPreviewKeyEvent { event ->
            if (event.type == KeyEventType.KeyDown && event.key == Key.Escape) {
                onEscape()
                true
            } else {
                false
            }
        },
    ) {
        if (showArrow) {
            val arrowPadding = when (align) {
                KiraDropdownMenuAlign.Start -> Modifier.padding(start = 16.dp)
                KiraDropdownMenuAlign.Center -> Modifier
                KiraDropdownMenuAlign.End -> Modifier.padding(end = 16.dp)
            }
            MenuArrowGlyph(
                fillColor = colors.popover,
                borderColor = colors.border,
                modifier = arrowPadding,
            )
        }
        Column(
            modifier = Modifier
                .widthIn(min = minWidth, max = maxWidth)
                .heightIn(max = 420.dp)
                .shadow(10.dp, radius.shapeLg)
                .clip(radius.shapeLg)
                .background(colors.popover)
                .border(1.dp, colors.border, radius.shapeLg)
                .verticalScroll(rememberScrollState())
                .padding(4.dp),
            content = content,
        )
    }
}

@Composable
internal fun KiraDropdownMenuScope.RenderKiraMenuItems(
    items: List<KiraMenuItemData>,
) {
    items.forEach { item ->
        when (item) {
            is KiraMenuItemData.Action -> {
                KiraDropdownMenuItem(
                    text = item.label,
                    icon = item.icon,
                    shortcut = item.shortcut,
                    variant = item.variant,
                    enabled = !item.disabled,
                    disabledReason = item.disabledReason,
                    loading = item.loading,
                    closeOnClick = item.closeOnClick,
                    onClick = item.onSelect,
                )
            }
            is KiraMenuItemData.Checkbox -> {
                KiraDropdownMenuCheckboxItem(
                    text = item.label,
                    checked = item.checked,
                    icon = item.icon,
                    enabled = !item.disabled,
                    disabledReason = item.disabledReason,
                    closeOnClick = item.closeOnClick,
                    onCheckedChange = item.onCheckedChange,
                )
            }
            is KiraMenuItemData.RadioGroup -> {
                item.label?.let { KiraDropdownMenuLabel(it) }
                item.options.forEach { opt ->
                    KiraDropdownMenuRadioItem(
                        text = opt.label,
                        selected = opt.value == item.selectedValue,
                        icon = opt.icon,
                        enabled = !opt.disabled,
                        disabledReason = opt.disabledReason,
                        closeOnClick = item.closeOnClick,
                        onClick = { item.onValueChange(opt.value) },
                    )
                }
            }
            is KiraMenuItemData.Submenu -> {
                KiraDropdownMenuSub(
                    label = item.label,
                    icon = item.icon,
                    enabled = !item.disabled,
                    disabledReason = item.disabledReason,
                    searchable = item.searchable,
                ) {
                    RenderKiraMenuItems(items = item.items)
                }
            }
            is KiraMenuItemData.Group -> {
                item.label?.let { KiraDropdownMenuLabel(it) }
                RenderKiraMenuItems(items = item.items)
            }
            is KiraMenuItemData.Separator -> KiraDropdownMenuSeparator()
        }
    }
}

@Composable
fun KiraDropdownMenuLabel(
    text: String,
    modifier: Modifier = Modifier,
    inset: Boolean = false,
) {
    BasicText(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .semantics { heading() }
            .padding(
                start = if (inset) 32.dp else 8.dp,
                end = 8.dp,
                top = 6.dp,
                bottom = 4.dp,
            ),
        style = KiraTheme.typography.caption.copy(
            color = KiraTheme.colors.mutedForeground,
            fontWeight = FontWeight.SemiBold,
        ),
    )
}

@Composable
fun KiraDropdownMenuSeparator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(1.dp)
            .background(KiraTheme.colors.border),
    )
}

@Composable
fun KiraDropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable (Color) -> Unit)? = null,
    shortcut: String? = null,
    variant: KiraMenuItemVariant = KiraMenuItemVariant.Default,
    inset: Boolean = false,
    enabled: Boolean = true,
    disabledReason: String? = null,
    loading: Boolean = false,
    closeOnClick: Boolean = true,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val haptics = rememberKiraHaptics()
    val scope = LocalDropdownMenuScope.current

    val interactive = enabled && !loading
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()

    val isDestructive = variant == KiraMenuItemVariant.Destructive
    val bg by animateColorAsState(
        targetValue = when {
            hovered && interactive && isDestructive -> colors.destructive.copy(alpha = 0.12f)
            hovered && interactive -> colors.accent
            else -> Color.Transparent
        },
        animationSpec = KiraMotion.fastTween(),
    )
    val fg = when {
        isDestructive -> colors.destructive
        hovered && interactive -> colors.accentForeground
        else -> colors.popoverForeground
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 36.dp)
            .alpha(if (enabled) 1f else 0.55f)
            .clip(radius.shapeMd)
            .background(bg)
            .hoverable(interaction, enabled = interactive)
            .pointerHoverIcon(if (interactive) PointerIcon.Hand else PointerIcon.Default)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = interactive,
                role = Role.Button,
                onClick = {
                    haptics.perform(KiraHapticFeedbackType.Selection)
                    onClick()
                    if (closeOnClick) scope.close()
                },
            )
            .semantics(mergeDescendants = true) {
                if (!interactive) disabled()
                if (loading) stateDescription = "Loading"
                if (disabledReason != null) contentDescription = "$text. $disabledReason"
            }
            .padding(
                start = if (inset) 32.dp else 8.dp,
                end = 8.dp,
                top = 6.dp,
                bottom = 6.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (loading) {
            MenuSpinner(color = fg, size = 16.dp)
        } else if (icon != null) {
            Box(Modifier.size(16.dp), contentAlignment = Alignment.Center) {
                icon(fg)
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            BasicText(
                text = text,
                style = KiraTheme.typography.body.copy(color = fg),
            )
            if (disabledReason != null) {
                BasicText(
                    text = disabledReason,
                    style = KiraTheme.typography.caption.copy(color = colors.mutedForeground),
                )
            }
        }
        if (shortcut != null) {
            BasicText(
                text = shortcut,
                modifier = Modifier.clearAndSetSemantics {},
                style = KiraTheme.typography.caption.copy(
                    color = colors.mutedForeground,
                    letterSpacing = 0.5.sp,
                ),
            )
        }
    }
}

@Composable
fun KiraDropdownMenuCheckboxItem(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable (Color) -> Unit)? = null,
    enabled: Boolean = true,
    disabledReason: String? = null,
    closeOnClick: Boolean = false,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val haptics = rememberKiraHaptics()
    val scope = LocalDropdownMenuScope.current

    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val bg by animateColorAsState(
        targetValue = if (hovered && enabled) colors.accent else Color.Transparent,
        animationSpec = KiraMotion.fastTween(),
    )
    val fg = if (hovered && enabled) colors.accentForeground else colors.popoverForeground

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 36.dp)
            .alpha(if (enabled) 1f else 0.55f)
            .clip(radius.shapeMd)
            .background(bg)
            .hoverable(interaction, enabled = enabled)
            .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Checkbox,
                onClick = {
                    haptics.perform(KiraHapticFeedbackType.Selection)
                    onCheckedChange(!checked)
                    if (closeOnClick) scope.close()
                },
            )
            .semantics(mergeDescendants = true) {
                selected = checked
                stateDescription = if (checked) "Checked" else "Unchecked"
                if (!enabled) disabled()
                if (disabledReason != null) contentDescription = "$text. $disabledReason"
            }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(16.dp), contentAlignment = Alignment.Center) {
            if (checked) {
                CheckmarkGlyph(color = colors.primary, size = 14.dp)
            }
        }
        if (icon != null) {
            Box(Modifier.size(16.dp), contentAlignment = Alignment.Center) {
                icon(fg)
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            BasicText(
                text = text,
                style = KiraTheme.typography.body.copy(color = fg),
            )
            if (disabledReason != null) {
                BasicText(
                    text = disabledReason,
                    style = KiraTheme.typography.caption.copy(color = colors.mutedForeground),
                )
            }
        }
    }
}

@Composable
fun KiraDropdownMenuRadioItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable (Color) -> Unit)? = null,
    enabled: Boolean = true,
    disabledReason: String? = null,
    closeOnClick: Boolean = false,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val haptics = rememberKiraHaptics()
    val scope = LocalDropdownMenuScope.current

    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val bg by animateColorAsState(
        targetValue = if (hovered && enabled) colors.accent else Color.Transparent,
        animationSpec = KiraMotion.fastTween(),
    )
    val fg = if (hovered && enabled) colors.accentForeground else colors.popoverForeground

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 36.dp)
            .alpha(if (enabled) 1f else 0.55f)
            .clip(radius.shapeMd)
            .background(bg)
            .hoverable(interaction, enabled = enabled)
            .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = {
                    haptics.perform(KiraHapticFeedbackType.Selection)
                    onClick()
                    if (closeOnClick) scope.close()
                },
            )
            .semantics(mergeDescendants = true) {
                this.selected = selected
                if (!enabled) disabled()
                if (disabledReason != null) contentDescription = "$text. $disabledReason"
            }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(16.dp), contentAlignment = Alignment.Center) {
            if (selected) {
                CheckmarkGlyph(color = colors.primary, size = 14.dp)
            }
        }
        if (icon != null) {
            Box(Modifier.size(16.dp), contentAlignment = Alignment.Center) {
                icon(fg)
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            BasicText(
                text = text,
                style = KiraTheme.typography.body.copy(color = fg),
            )
            if (disabledReason != null) {
                BasicText(
                    text = disabledReason,
                    style = KiraTheme.typography.caption.copy(color = colors.mutedForeground),
                )
            }
        }
    }
}

/**
 * Nested submenu inside a dropdown menu. Expands cleanly inline with an indented card container
 * so it works identically on Desktop, Android, and iOS without off-screen popup clipping.
 */
@Composable
fun KiraDropdownMenuSub(
    label: String,
    modifier: Modifier = Modifier,
    icon: (@Composable (Color) -> Unit)? = null,
    enabled: Boolean = true,
    disabledReason: String? = null,
    searchable: Boolean = false,
    content: @Composable KiraDropdownMenuScope.() -> Unit,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val haptics = rememberKiraHaptics()
    val scope = LocalDropdownMenuScope.current

    var subOpen by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val chevronRotation by animateFloatAsState(
        targetValue = if (subOpen) 90f else 0f,
        animationSpec = KiraMotion.fastTween(),
    )
    val bg by animateColorAsState(
        targetValue = when {
            subOpen -> colors.accent.copy(alpha = 0.7f)
            hovered && enabled -> colors.accent
            else -> Color.Transparent
        },
        animationSpec = KiraMotion.fastTween(),
    )
    val fg = if ((hovered || subOpen) && enabled) colors.accentForeground else colors.popoverForeground

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 36.dp)
                .alpha(if (enabled) 1f else 0.55f)
                .clip(radius.shapeMd)
                .background(bg)
                .hoverable(interaction, enabled = enabled)
                .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = {
                        haptics.perform(KiraHapticFeedbackType.Light)
                        subOpen = !subOpen
                    },
                )
                .semantics(mergeDescendants = true) {
                    stateDescription = if (subOpen) "Expanded" else "Collapsed"
                    if (!enabled) disabled()
                    if (disabledReason != null) contentDescription = "$label. $disabledReason"
                }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (icon != null) {
                Box(Modifier.size(16.dp), contentAlignment = Alignment.Center) {
                    icon(fg)
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                BasicText(
                    text = label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = KiraTheme.typography.body.copy(color = fg),
                )
                if (disabledReason != null) {
                    BasicText(
                        text = disabledReason,
                        style = KiraTheme.typography.caption.copy(color = colors.mutedForeground),
                    )
                }
            }
            SubChevronRightGlyph(
                color = colors.mutedForeground,
                size = 14.dp,
                modifier = Modifier.graphicsLayer { rotationZ = chevronRotation },
            )
        }

        AnimatedVisibility(
            visible = subOpen && enabled,
            enter = expandVertically(tween(180, easing = KiraMotion.EaseOutStrong)) + fadeIn(tween(150)),
            exit = shrinkVertically(tween(140, easing = KiraMotion.EaseOutStrong)) + fadeOut(tween(120)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
                    .clip(radius.shapeMd)
                    .background(colors.muted.copy(alpha = 0.35f))
                    .border(1.dp, colors.border.copy(alpha = 0.7f), radius.shapeMd)
                    .padding(4.dp),
            ) {
                scope.content()
            }
        }
    }
}

private class DropdownPopupPositionProvider(
    private val align: KiraDropdownMenuAlign,
    private val side: KiraDropdownMenuSide,
    private val gapPx: Int,
    private val marginPx: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val rawX = when (align) {
            KiraDropdownMenuAlign.Start -> anchorBounds.left
            KiraDropdownMenuAlign.Center -> anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2
            KiraDropdownMenuAlign.End -> anchorBounds.right - popupContentSize.width
        }
        val x = rawX.coerceIn(
            marginPx,
            (windowSize.width - popupContentSize.width - marginPx).coerceAtLeast(marginPx),
        )

        val belowY = anchorBounds.bottom + gapPx
        val aboveY = anchorBounds.top - popupContentSize.height - gapPx
        val y = when (side) {
            KiraDropdownMenuSide.Bottom -> when {
                belowY + popupContentSize.height + marginPx <= windowSize.height -> belowY
                aboveY >= marginPx -> aboveY
                else -> belowY.coerceIn(marginPx, (windowSize.height - popupContentSize.height - marginPx).coerceAtLeast(marginPx))
            }
            KiraDropdownMenuSide.Top -> when {
                aboveY >= marginPx -> aboveY
                belowY + popupContentSize.height + marginPx <= windowSize.height -> belowY
                else -> aboveY.coerceIn(marginPx, (windowSize.height - popupContentSize.height - marginPx).coerceAtLeast(marginPx))
            }
        }
        return IntOffset(x, y)
    }
}

@Composable
private fun MenuArrowGlyph(
    fillColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier.size(width = 14.dp, height = 7.dp).clearAndSetSemantics {}) {
        val path = Path().apply {
            moveTo(0f, size.height)
            lineTo(size.width / 2f, 0f)
            lineTo(size.width, size.height)
            close()
        }
        drawPath(path, color = fillColor)
        val stroke = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val borderPath = Path().apply {
            moveTo(0f, size.height)
            lineTo(size.width / 2f, 0f)
            lineTo(size.width, size.height)
        }
        drawPath(borderPath, color = borderColor, style = stroke)
    }
}

@Composable
private fun CheckmarkGlyph(color: Color, size: Dp) {
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(this@Canvas.size.width * 0.18f, this@Canvas.size.height * 0.52f)
            lineTo(this@Canvas.size.width * 0.42f, this@Canvas.size.height * 0.76f)
            lineTo(this@Canvas.size.width * 0.84f, this@Canvas.size.height * 0.26f)
        }
        drawPath(path, color = color, style = stroke)
    }
}

@Composable
private fun SubChevronRightGlyph(
    color: Color,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier.size(size).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(this@Canvas.size.width * 0.34f, this@Canvas.size.height * 0.22f)
            lineTo(this@Canvas.size.width * 0.68f, this@Canvas.size.height * 0.50f)
            lineTo(this@Canvas.size.width * 0.34f, this@Canvas.size.height * 0.78f)
        }
        drawPath(path, color = color, style = stroke)
    }
}

@Composable
private fun MenuSpinner(color: Color, size: Dp) {
    val infinite = rememberInfiniteTransition(label = "MenuSpinner")
    val angleState = infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "MenuSpinnerAngle",
    )
    Canvas(
        Modifier
            .size(size)
            .graphicsLayer { rotationZ = angleState.value }
            .clearAndSetSemantics {},
    ) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        val pad = stroke.width
        drawArc(
            color = color.copy(alpha = 0.25f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(pad, pad),
            size = Size(this.size.width - pad * 2f, this.size.height - pad * 2f),
            style = stroke,
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 110f,
            useCenter = false,
            topLeft = Offset(pad, pad),
            size = Size(this.size.width - pad * 2f, this.size.height - pad * 2f),
            style = stroke,
        )
    }
}
