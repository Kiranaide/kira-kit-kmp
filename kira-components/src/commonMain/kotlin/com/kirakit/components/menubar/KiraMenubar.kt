package com.kirakit.components.menubar

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
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
import com.kirakit.components.dropdownmenu.KiraDropdownMenu
import com.kirakit.components.dropdownmenu.KiraDropdownMenuAlign
import com.kirakit.components.dropdownmenu.KiraDropdownMenuScope
import com.kirakit.components.dropdownmenu.KiraMenuItemData
import com.kirakit.components.platform.KiraHapticFeedbackType
import com.kirakit.components.platform.rememberKiraHaptics
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography

enum class KiraMenubarVariant {
    Default,
    Outline,
    Soft,
}

enum class KiraMenubarDensity {
    Comfortable,
    Compact,
}

enum class KiraMenubarOrientation {
    Horizontal,
    Vertical,
}

@Immutable
data class KiraMenubarMenuData(
    val id: String,
    val label: String,
    val icon: (@Composable (Color) -> Unit)? = null,
    val disabled: Boolean = false,
    val searchable: Boolean = false,
    val items: List<KiraMenuItemData>,
)

/**
 * Collects invokable actions from all enabled menus into grouped [KiraMenuItemData] entries
 * for the `commandSearch` palette (aligned with `menuCommands` in kira-kit-v2's `menubar.tsx`).
 */
fun collectMenubarCommands(menus: List<KiraMenubarMenuData>): List<KiraMenuItemData> {
    fun flatten(items: List<KiraMenuItemData>, prefix: String): List<KiraMenuItemData> =
        items.flatMap { item ->
            val id = "$prefix/${item.id}"
            when (item) {
                is KiraMenuItemData.Action -> listOf(item.copy(id = id))
                is KiraMenuItemData.Group -> {
                    val children = flatten(item.items, id)
                    if (children.isNotEmpty()) listOf(KiraMenuItemData.Group(id, item.label, children)) else emptyList()
                }
                is KiraMenuItemData.Submenu -> {
                    if (item.disabled) {
                        emptyList()
                    } else {
                        val children = flatten(item.items, id)
                        if (children.isNotEmpty()) listOf(KiraMenuItemData.Group(id, item.label, children)) else emptyList()
                    }
                }
                else -> emptyList()
            }
        }

    return menus
        .filterNot { it.disabled }
        .mapNotNull { menu ->
            val children = flatten(menu.items, menu.id)
            if (children.isNotEmpty()) {
                KiraMenuItemData.Group(id = "commands/${menu.id}", label = menu.label, items = children)
            } else {
                null
            }
        }
}

@Stable
internal class KiraMenubarController(
    val density: KiraMenubarDensity,
    val enabled: Boolean,
) {
    var openMenuId by mutableStateOf<String?>(null)
}

private val LocalMenubarController = compositionLocalOf<KiraMenubarController?> { null }

/**
 * Data-driven editor menubar aligned with kira-kit-v2's `KiraMenubar`.
 *
 * Coordinates sibling menu open states (hovering another top-level trigger while one menu is open
 * switches smoothly), supports 3 variants (`Default`, `Outline`, `Soft`), 2 densities
 * (`Comfortable`, `Compact`), horizontal or vertical layout, and optional `commandSearch` (`Ctrl/⌘ K`).
 */
@Composable
fun KiraMenubar(
    menus: List<KiraMenubarMenuData>,
    modifier: Modifier = Modifier,
    variant: KiraMenubarVariant = KiraMenubarVariant.Default,
    density: KiraMenubarDensity = KiraMenubarDensity.Comfortable,
    orientation: KiraMenubarOrientation = KiraMenubarOrientation.Horizontal,
    enabled: Boolean = true,
    commandSearch: Boolean = false,
    emptyMessage: String = "No actions available.",
    ariaLabel: String = "Menubar",
) {
    val colors = KiraTheme.colors
    val controller = remember(density, enabled) { KiraMenubarController(density, enabled) }
    val allCommands = remember(menus) { collectMenubarCommands(menus) }

    KiraMenubarContainer(
        controller = controller,
        variant = variant,
        density = density,
        orientation = orientation,
        enabled = enabled,
        ariaLabel = ariaLabel,
        modifier = modifier.onPreviewKeyEvent { event ->
            if (commandSearch && enabled &&
                event.type == KeyEventType.KeyDown &&
                (event.isCtrlPressed || event.isMetaPressed) &&
                event.key == Key.K
            ) {
                controller.openMenuId = COMMAND_SEARCH_ID
                true
            } else {
                false
            }
        },
    ) {
        if (menus.isEmpty()) {
            BasicText(
                text = emptyMessage,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
            )
        } else {
            menus.forEach { menu ->
                val menuEnabled = enabled && !menu.disabled
                val isOpen = controller.openMenuId == menu.id
                KiraDropdownMenu(
                    items = menu.items,
                    expanded = isOpen,
                    onExpandedChange = { open ->
                        controller.openMenuId = if (open) menu.id else if (controller.openMenuId == menu.id) null else controller.openMenuId
                    },
                    searchable = menu.searchable,
                    searchPlaceholder = "Filter ${menu.label.lowercase()}…",
                    trigger = { onToggle ->
                        MenubarTriggerButton(
                            label = menu.label,
                            icon = menu.icon,
                            open = isOpen,
                            density = density,
                            enabled = menuEnabled,
                            onHoverWhileAnyOpen = {
                                if (controller.openMenuId != null && controller.openMenuId != menu.id && menuEnabled) {
                                    controller.openMenuId = menu.id
                                }
                            },
                            onClick = onToggle,
                        )
                    },
                )
            }
        }

        if (commandSearch) {
            val isCmdOpen = controller.openMenuId == COMMAND_SEARCH_ID
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (menus.isNotEmpty() && orientation == KiraMenubarOrientation.Horizontal) {
                    Box(Modifier.width(1.dp).height(20.dp).background(colors.border))
                }
                KiraDropdownMenu(
                    items = allCommands,
                    expanded = isCmdOpen,
                    onExpandedChange = { open ->
                        controller.openMenuId = if (open) COMMAND_SEARCH_ID else if (controller.openMenuId == COMMAND_SEARCH_ID) null else controller.openMenuId
                    },
                    searchable = true,
                    searchPlaceholder = "Find a command…",
                    align = KiraDropdownMenuAlign.End,
                    minWidth = 264.dp,
                    trigger = { onToggle ->
                        MenubarTriggerButton(
                            label = "Commands",
                            trailingHint = "Ctrl K",
                            icon = { tint -> SearchGlyph(color = tint, size = 14.dp) },
                            mutedStyle = true,
                            open = isCmdOpen,
                            density = density,
                            enabled = enabled,
                            onHoverWhileAnyOpen = {
                                if (controller.openMenuId != null && controller.openMenuId != COMMAND_SEARCH_ID && enabled) {
                                    controller.openMenuId = COMMAND_SEARCH_ID
                                }
                            },
                            onClick = onToggle,
                        )
                    },
                )
            }
        }
    }
}

private const val COMMAND_SEARCH_ID = "__kira_menubar_command_search__"

/**
 * Composable slot Menubar for custom [KiraMenubarMenu] composition.
 */
@Composable
fun KiraMenubar(
    modifier: Modifier = Modifier,
    variant: KiraMenubarVariant = KiraMenubarVariant.Default,
    density: KiraMenubarDensity = KiraMenubarDensity.Comfortable,
    orientation: KiraMenubarOrientation = KiraMenubarOrientation.Horizontal,
    enabled: Boolean = true,
    ariaLabel: String = "Menubar",
    content: @Composable () -> Unit,
) {
    val controller = remember(density, enabled) { KiraMenubarController(density, enabled) }
    KiraMenubarContainer(
        controller = controller,
        variant = variant,
        density = density,
        orientation = orientation,
        enabled = enabled,
        ariaLabel = ariaLabel,
        modifier = modifier,
        content = content,
    )
}

/**
 * One top-level menu inside a composable slot [KiraMenubar].
 */
@Composable
fun KiraMenubarMenu(
    id: String,
    label: String,
    modifier: Modifier = Modifier,
    icon: (@Composable (Color) -> Unit)? = null,
    enabled: Boolean = true,
    align: KiraDropdownMenuAlign = KiraDropdownMenuAlign.Start,
    content: @Composable KiraDropdownMenuScope.() -> Unit,
) {
    val controller = LocalMenubarController.current
    var fallbackOpen by remember { mutableStateOf(false) }

    val effectiveEnabled = enabled && (controller?.enabled ?: true)
    val isOpen = if (controller != null) controller.openMenuId == id else fallbackOpen
    val density = controller?.density ?: KiraMenubarDensity.Comfortable

    fun setOpen(next: Boolean) {
        if (controller != null) {
            controller.openMenuId = if (next) id else if (controller.openMenuId == id) null else controller.openMenuId
        } else {
            fallbackOpen = next
        }
    }

    KiraDropdownMenu(
        expanded = isOpen,
        onDismissRequest = { setOpen(false) },
        align = align,
        modifier = modifier,
        trigger = {
            MenubarTriggerButton(
                label = label,
                icon = icon,
                open = isOpen,
                density = density,
                enabled = effectiveEnabled,
                onHoverWhileAnyOpen = {
                    if (controller != null && controller.openMenuId != null && controller.openMenuId != id && effectiveEnabled) {
                        controller.openMenuId = id
                    }
                },
                onClick = { setOpen(!isOpen) },
            )
        },
        content = content,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KiraMenubarContainer(
    controller: KiraMenubarController,
    variant: KiraMenubarVariant,
    density: KiraMenubarDensity,
    orientation: KiraMenubarOrientation,
    enabled: Boolean,
    ariaLabel: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius

    val bg = when (variant) {
        KiraMenubarVariant.Default, KiraMenubarVariant.Outline -> colors.background
        KiraMenubarVariant.Soft -> colors.muted.copy(alpha = 0.6f)
    }
    val borderColor = when (variant) {
        KiraMenubarVariant.Default, KiraMenubarVariant.Outline -> colors.border
        KiraMenubarVariant.Soft -> Color.Transparent
    }
    val elevation = if (variant == KiraMenubarVariant.Default) 1.dp else 0.dp
    val minHeight = if (density == KiraMenubarDensity.Compact) 36.dp else 40.dp

    val containerModifier = modifier
        .alpha(if (enabled) 1f else 0.55f)
        .shadow(elevation, radius.shapeLg)
        .clip(radius.shapeLg)
        .background(bg)
        .border(1.dp, borderColor, radius.shapeLg)
        .semantics {
            contentDescription = ariaLabel
            if (!enabled) disabled()
        }
        .padding(4.dp)

    CompositionLocalProvider(LocalMenubarController provides controller) {
        if (orientation == KiraMenubarOrientation.Vertical) {
            Column(
                modifier = containerModifier.widthIn(min = 160.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                content()
            }
        } else {
            Box(
                modifier = modifier
                    .defaultMinSize(minHeight = minHeight)
                    .alpha(if (enabled) 1f else 0.55f)
                    .shadow(elevation, radius.shapeLg)
                    .clip(radius.shapeLg)
                    .background(bg)
                    .border(1.dp, borderColor, radius.shapeLg)
                    .semantics {
                        contentDescription = ariaLabel
                        if (!enabled) disabled()
                    }
                    .padding(4.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                    itemVerticalAlignment = Alignment.CenterVertically,
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun MenubarTriggerButton(
    label: String,
    open: Boolean,
    density: KiraMenubarDensity,
    enabled: Boolean,
    onHoverWhileAnyOpen: () -> Unit,
    onClick: () -> Unit,
    icon: (@Composable (Color) -> Unit)? = null,
    trailingHint: String? = null,
    mutedStyle: Boolean = false,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val haptics = rememberKiraHaptics()
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()

    LaunchedEffect(hovered) {
        if (hovered) onHoverWhileAnyOpen()
    }

    val bg by animateColorAsState(
        targetValue = if ((open || hovered) && enabled) colors.muted else Color.Transparent,
        animationSpec = KiraMotion.fastTween(),
    )
    val fg = if (mutedStyle && !open) colors.mutedForeground else colors.foreground
    val minHeight = if (density == KiraMenubarDensity.Compact) 28.dp else 32.dp
    val hPad = if (density == KiraMenubarDensity.Compact) 8.dp else 12.dp

    Row(
        modifier = Modifier
            .defaultMinSize(minHeight = minHeight)
            .alpha(if (enabled) 1f else 0.5f)
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
                    onClick()
                },
            )
            .semantics(mergeDescendants = true) {
                stateDescription = if (open) "Expanded" else "Collapsed"
                if (!enabled) disabled()
            }
            .padding(horizontal = hPad, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (icon != null) {
            Box(Modifier.size(16.dp), contentAlignment = Alignment.Center) {
                icon(fg)
            }
        }
        BasicText(
            text = label,
            style = KiraTheme.typography.label.copy(
                color = fg,
                fontWeight = FontWeight.Medium,
            ),
        )
        if (trailingHint != null) {
            BasicText(
                text = trailingHint,
                modifier = Modifier.clearAndSetSemantics {},
                style = KiraTheme.typography.caption.copy(
                    color = colors.mutedForeground,
                    fontSize = 11.sp,
                ),
            )
        }
    }
}

@Composable
private fun SearchGlyph(color: Color, size: Dp) {
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        val stroke = (this.size.minDimension * 0.13f).coerceAtLeast(1.3.dp.toPx())
        val r = this.size.minDimension * 0.30f
        val c = Offset(this.size.width * 0.44f, this.size.height * 0.44f)
        drawCircle(color = color, radius = r, center = c, style = Stroke(width = stroke))
        drawLine(
            color = color,
            start = Offset(c.x + r * 0.72f, c.y + r * 0.72f),
            end = Offset(this.size.width * 0.86f, this.size.height * 0.86f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}
