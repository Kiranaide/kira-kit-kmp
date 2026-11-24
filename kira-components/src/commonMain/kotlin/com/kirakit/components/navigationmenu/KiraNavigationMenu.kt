package com.kirakit.components.navigationmenu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.runtime.Immutable
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.kirakit.components.drawer.KiraDrawer
import com.kirakit.components.drawer.KiraDrawerSide
import com.kirakit.components.drawer.KiraDrawerSize
import com.kirakit.components.platform.KiraHapticFeedbackType
import com.kirakit.components.platform.rememberKiraHaptics
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography

enum class KiraNavigationMobileMode {
    Drawer,
    Inline,
}

enum class KiraNavigationMenuOrientation {
    Horizontal,
    Vertical,
}

/**
 * Tree item model for [KiraNavigationMenu] (aligned with `NavigationItemData` in kira-kit-v2).
 */
sealed interface KiraNavigationItemData {
    val id: String
    val label: String

    @Immutable
    data class Link(
        override val id: String,
        override val label: String,
        val href: String,
        val description: String? = null,
        val icon: (@Composable (Color) -> Unit)? = null,
        val disabled: Boolean = false,
    ) : KiraNavigationItemData

    @Immutable
    data class Group(
        override val id: String,
        override val label: String,
        val description: String? = null,
        val disabled: Boolean = false,
        val items: List<KiraNavigationItemData>,
    ) : KiraNavigationItemData
}

/**
 * Flattens all [KiraNavigationItemData.Link] leaves in a navigation tree.
 */
fun flattenNavigationLinks(items: List<KiraNavigationItemData>): List<KiraNavigationItemData.Link> =
    items.flatMap { item ->
        when (item) {
            is KiraNavigationItemData.Link -> listOf(item)
            is KiraNavigationItemData.Group -> flattenNavigationLinks(item.items)
        }
    }

/**
 * Returns the top-level [KiraNavigationItemData.Group.id] containing [activeHref], or `null`.
 */
fun findActiveNavigationGroupId(
    items: List<KiraNavigationItemData>,
    activeHref: String?,
): String? {
    if (activeHref == null) return null
    for (item in items) {
        if (item is KiraNavigationItemData.Group) {
            if (flattenNavigationLinks(item.items).any { it.href == activeHref }) {
                return item.id
            }
        }
    }
    return null
}

/**
 * Data-driven website disclosure navigation aligned with kira-kit-v2's `KiraNavigationMenu`.
 *
 * - On wide viewports (`>= mobileBreakpoint`) or `mobileMode = Inline`, renders top-level triggers
 *   that open anchored mega-menu panels and direct links.
 * - Below `mobileBreakpoint` with `mobileMode = Drawer`, renders a hamburger trigger that opens a
 *   labelled right-side [KiraDrawer] with collapsible disclosure groups and `>= 44.dp` link targets.
 * - Active links always use weight + underline (never color alone).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KiraNavigationMenu(
    items: List<KiraNavigationItemData>,
    label: String,
    modifier: Modifier = Modifier,
    activeHref: String? = null,
    onNavigate: (KiraNavigationItemData.Link) -> Unit = {},
    mobileMode: KiraNavigationMobileMode = KiraNavigationMobileMode.Drawer,
    mobileLabel: String = "Open navigation",
    mobileBreakpoint: Dp = 560.dp,
    emptyMessage: String = "No pages available.",
) {
    val colors = KiraTheme.colors
    var openGroupId by remember { mutableStateOf<String?>(null) }
    var drawerOpen by remember { mutableStateOf(false) }
    val activeGroupId = remember(items, activeHref) { findActiveNavigationGroupId(items, activeHref) }

    BoxWithConstraints(
        modifier = modifier.semantics { contentDescription = label },
    ) {
        val useDrawer = mobileMode == KiraNavigationMobileMode.Drawer && maxWidth < mobileBreakpoint

        if (useDrawer) {
            MobileNavTriggerButton(
                label = mobileLabel,
                onClick = { drawerOpen = true },
            )
            KiraDrawer(
                open = drawerOpen,
                onDismissRequest = { drawerOpen = false },
                title = label,
                side = KiraDrawerSide.Right,
                size = KiraDrawerSize.Sm,
            ) {
                if (items.isEmpty()) {
                    BasicText(
                        text = emptyMessage,
                        style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items.forEach { item ->
                            MobileNavNode(
                                item = item,
                                activeHref = activeHref,
                                emptyMessage = emptyMessage,
                                onNavigate = { link ->
                                    drawerOpen = false
                                    onNavigate(link)
                                },
                            )
                        }
                    }
                }
            }
        } else {
            if (items.isEmpty()) {
                BasicText(
                    text = emptyMessage,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    itemVerticalAlignment = Alignment.CenterVertically,
                ) {
                    items.forEach { item ->
                        when (item) {
                            is KiraNavigationItemData.Link -> {
                                KiraNavigationMenuLink(
                                    label = item.label,
                                    active = activeHref == item.href,
                                    icon = item.icon,
                                    enabled = !item.disabled,
                                    onClick = {
                                        openGroupId = null
                                        onNavigate(item)
                                    },
                                )
                            }
                            is KiraNavigationItemData.Group -> {
                                val expanded = openGroupId == item.id
                                Box {
                                    KiraNavigationMenuTrigger(
                                        label = item.label,
                                        expanded = expanded,
                                        active = activeGroupId == item.id,
                                        enabled = !item.disabled,
                                        onClick = {
                                            openGroupId = if (expanded) null else item.id
                                        },
                                    )
                                    KiraNavigationMenuPanel(
                                        expanded = expanded,
                                        onDismissRequest = { openGroupId = null },
                                        megaMenu = item.description != null,
                                        minWidth = 280.dp,
                                        maxWidth = 520.dp,
                                    ) {
                                        if (item.description != null) {
                                            KiraNavigationMenuHeader(
                                                title = item.label,
                                                description = item.description,
                                            )
                                        }
                                        Column(
                                            modifier = Modifier.padding(
                                                horizontal = if (item.description != null) 12.dp else 0.dp,
                                                vertical = if (item.description != null) 10.dp else 0.dp,
                                            ),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            DesktopNavItems(
                                                items = item.items,
                                                activeHref = activeHref,
                                                emptyMessage = emptyMessage,
                                                onNavigate = { link ->
                                                    openGroupId = null
                                                    onNavigate(link)
                                                },
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
    }
}

@Composable
private fun DesktopNavItems(
    items: List<KiraNavigationItemData>,
    activeHref: String?,
    emptyMessage: String,
    onNavigate: (KiraNavigationItemData.Link) -> Unit,
) {
    if (items.isEmpty()) {
        BasicText(
            text = emptyMessage,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            style = KiraTheme.typography.bodySmall.copy(color = KiraTheme.colors.mutedForeground),
        )
        return
    }
    items.forEach { item ->
        when (item) {
            is KiraNavigationItemData.Link -> {
                KiraNavigationMenuLinkCard(
                    title = item.label,
                    description = item.description,
                    icon = item.icon,
                    active = activeHref == item.href,
                    enabled = !item.disabled,
                    onClick = { onNavigate(item) },
                )
            }
            is KiraNavigationItemData.Group -> {
                KiraNavigationMenuSection(title = item.label) {
                    DesktopNavItems(
                        items = item.items,
                        activeHref = activeHref,
                        emptyMessage = emptyMessage,
                        onNavigate = onNavigate,
                    )
                }
            }
        }
    }
}

@Composable
private fun MobileNavNode(
    item: KiraNavigationItemData,
    activeHref: String?,
    emptyMessage: String,
    onNavigate: (KiraNavigationItemData.Link) -> Unit,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val haptics = rememberKiraHaptics()

    when (item) {
        is KiraNavigationItemData.Link -> {
            KiraNavigationMenuLinkCard(
                title = item.label,
                description = item.description,
                icon = item.icon,
                active = activeHref == item.href,
                enabled = !item.disabled,
                minHeight = 44.dp,
                onClick = { onNavigate(item) },
            )
        }
        is KiraNavigationItemData.Group -> {
            var expanded by remember(item.id) { mutableStateOf(true) }
            val chevronRotation by animateFloatAsState(
                targetValue = if (expanded) 180f else 0f,
                animationSpec = KiraMotion.fastTween(),
            )
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 44.dp)
                        .alpha(if (!item.disabled) 1f else 0.5f)
                        .clip(radius.shapeMd)
                        .pointerHoverIcon(if (!item.disabled) PointerIcon.Hand else PointerIcon.Default)
                        .clickable(
                            enabled = !item.disabled,
                            role = Role.Button,
                            onClick = {
                                haptics.perform(KiraHapticFeedbackType.Light)
                                expanded = !expanded
                            },
                        )
                        .semantics(mergeDescendants = true) {
                            stateDescription = if (expanded) "Expanded" else "Collapsed"
                            if (item.disabled) disabled()
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    BasicText(
                        text = item.label,
                        style = KiraTheme.typography.label.copy(
                            color = colors.foreground,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                    NavChevronDownGlyph(
                        color = colors.mutedForeground,
                        size = 14.dp,
                        modifier = Modifier.graphicsLayer { rotationZ = chevronRotation },
                    )
                }
                AnimatedVisibility(
                    visible = expanded && !item.disabled,
                    enter = expandVertically(tween(180, easing = KiraMotion.EaseOutStrong)) + fadeIn(tween(150)),
                    exit = shrinkVertically(tween(140, easing = KiraMotion.EaseOutStrong)) + fadeOut(tween(120)),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, bottom = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        if (item.items.isEmpty()) {
                            BasicText(
                                text = emptyMessage,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                            )
                        } else {
                            item.items.forEach { child ->
                                MobileNavNode(
                                    item = child,
                                    activeHref = activeHref,
                                    emptyMessage = emptyMessage,
                                    onNavigate = onNavigate,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MobileNavTriggerButton(
    label: String,
    onClick: () -> Unit,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val haptics = rememberKiraHaptics()
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val bg by animateColorAsState(
        targetValue = if (hovered) colors.muted else colors.background,
        animationSpec = KiraMotion.fastTween(),
    )

    Row(
        modifier = Modifier
            .defaultMinSize(minHeight = 44.dp)
            .clip(radius.shapeMd)
            .background(bg)
            .border(1.dp, colors.border, radius.shapeMd)
            .hoverable(interaction)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = {
                    haptics.perform(KiraHapticFeedbackType.Light)
                    onClick()
                },
            )
            .semantics(mergeDescendants = true) { contentDescription = label }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HamburgerGlyph(color = colors.foreground, size = 16.dp)
        BasicText(
            text = label,
            style = KiraTheme.typography.label.copy(color = colors.foreground),
        )
    }
}

/*                              Primitive Composables                         */

/**
 * Disclosure trigger button with animated rotating chevron (aligned with `NavigationMenuTrigger`).
 */
@Composable
fun KiraNavigationMenuTrigger(
    label: String,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable (Color) -> Unit)? = null,
    active: Boolean = false,
    enabled: Boolean = true,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val haptics = rememberKiraHaptics()
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = KiraMotion.fastTween(),
    )
    val bg by animateColorAsState(
        targetValue = if ((expanded || hovered) && enabled) colors.muted else Color.Transparent,
        animationSpec = KiraMotion.fastTween(),
    )

    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 40.dp)
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
                selected = active
                stateDescription = if (expanded) "Expanded" else "Collapsed"
                if (!enabled) disabled()
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (icon != null) {
            Box(Modifier.size(16.dp), contentAlignment = Alignment.Center) {
                icon(colors.mutedForeground)
            }
        }
        BasicText(
            text = label,
            style = KiraTheme.typography.label.copy(
                color = colors.foreground,
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                textDecoration = if (active) TextDecoration.Underline else TextDecoration.None,
            ),
        )
        NavChevronDownGlyph(
            color = colors.mutedForeground,
            size = 12.dp,
            modifier = Modifier.graphicsLayer { rotationZ = chevronRotation },
        )
    }
}

/**
 * Direct navigation link in a bar or list. Active state uses weight + underline (never color alone).
 */
@Composable
fun KiraNavigationMenuLink(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    icon: (@Composable (Color) -> Unit)? = null,
    enabled: Boolean = true,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val haptics = rememberKiraHaptics()
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val bg by animateColorAsState(
        targetValue = if (hovered && enabled) colors.muted else Color.Transparent,
        animationSpec = KiraMotion.fastTween(),
    )

    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 40.dp)
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
                    haptics.perform(KiraHapticFeedbackType.Selection)
                    onClick()
                },
            )
            .semantics(mergeDescendants = true) {
                selected = active
                if (active) stateDescription = "Current page"
                if (!enabled) disabled()
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (icon != null) {
            Box(Modifier.size(16.dp), contentAlignment = Alignment.Center) {
                icon(colors.mutedForeground)
            }
        }
        BasicText(
            text = label,
            style = KiraTheme.typography.label.copy(
                color = colors.foreground,
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                textDecoration = if (active) TextDecoration.Underline else TextDecoration.None,
            ),
        )
    }
}

/**
 * Rich link card row (icon + title + optional description) for mega-menu panels and mobile drawers.
 */
@Composable
fun KiraNavigationMenuLinkCard(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: (@Composable (Color) -> Unit)? = null,
    active: Boolean = false,
    enabled: Boolean = true,
    minHeight: Dp = 40.dp,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val haptics = rememberKiraHaptics()
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val bg by animateColorAsState(
        targetValue = if (hovered && enabled) colors.muted else Color.Transparent,
        animationSpec = KiraMotion.fastTween(),
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
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
                    haptics.perform(KiraHapticFeedbackType.Selection)
                    onClick()
                },
            )
            .semantics(mergeDescendants = true) {
                selected = active
                if (active) stateDescription = "Current page"
                if (!enabled) disabled()
            }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (icon != null) {
            Box(Modifier.padding(top = 2.dp).size(18.dp), contentAlignment = Alignment.Center) {
                icon(colors.mutedForeground)
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            BasicText(
                text = title,
                style = KiraTheme.typography.label.copy(
                    color = colors.popoverForeground,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                    textDecoration = if (active) TextDecoration.Underline else TextDecoration.None,
                ),
            )
            if (description != null) {
                BasicText(
                    text = description,
                    style = KiraTheme.typography.caption.copy(
                        color = colors.mutedForeground,
                        fontWeight = FontWeight.Normal,
                    ),
                )
            }
        }
    }
}

/**
 * Anchored popup panel for [KiraNavigationMenuTrigger].
 */
@Composable
fun KiraNavigationMenuPanel(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    megaMenu: Boolean = false,
    minWidth: Dp = 256.dp,
    maxWidth: Dp = 560.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val transitionState = remember { MutableTransitionState(false) }
    transitionState.targetState = expanded

    val density = LocalDensity.current
    val positionProvider = remember(density) {
        NavPanelPositionProvider(
            gapPx = with(density) { 6.dp.roundToPx() },
            marginPx = with(density) { 8.dp.roundToPx() },
        )
    }

    if (transitionState.currentState || transitionState.targetState) {
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
                    scaleIn(tween(180, easing = KiraMotion.EaseOutStrong), initialScale = 0.97f),
                exit = fadeOut(tween(130, easing = KiraMotion.EaseOutStrong)) +
                    scaleOut(tween(130, easing = KiraMotion.EaseOutStrong), targetScale = 0.97f),
            ) {
                Column(
                    modifier = modifier
                        .widthIn(min = minWidth, max = maxWidth)
                        .heightIn(max = 460.dp)
                        .shadow(12.dp, radius.shapeLg)
                        .clip(radius.shapeLg)
                        .background(colors.popover)
                        .border(1.dp, colors.border, radius.shapeLg)
                        .verticalScroll(rememberScrollState())
                        .onPreviewKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown && event.key == Key.Escape) {
                                onDismissRequest()
                                true
                            } else {
                                false
                            }
                        }
                        .padding(if (megaMenu) 0.dp else 8.dp),
                    content = content,
                )
            }
        }
    }
}

@Composable
fun KiraNavigationMenuHeader(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    val colors = KiraTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            BasicText(
                text = title,
                modifier = Modifier.semantics { heading() },
                style = KiraTheme.typography.label.copy(
                    color = colors.popoverForeground,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            if (description != null) {
                BasicText(
                    text = description,
                    style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                )
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
    }
}

@Composable
fun KiraNavigationMenuSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = KiraTheme.colors
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        BasicText(
            text = title,
            modifier = Modifier
                .semantics { heading() }
                .padding(horizontal = 10.dp, vertical = 4.dp),
            style = KiraTheme.typography.caption.copy(
                color = colors.mutedForeground,
                fontWeight = FontWeight.SemiBold,
            ),
        )
        content()
    }
}

@Composable
fun KiraNavigationMenuFooter(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = KiraTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.muted.copy(alpha = 0.35f))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
fun KiraNavigationMenuViewAll(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val bg by animateColorAsState(
        targetValue = if (hovered) colors.muted else Color.Transparent,
        animationSpec = KiraMotion.fastTween(),
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(radius.shapeMd)
            .background(bg)
            .hoverable(interaction)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            BasicText(
                text = title,
                style = KiraTheme.typography.label.copy(color = colors.popoverForeground),
            )
            if (description != null) {
                BasicText(
                    text = description,
                    style = KiraTheme.typography.caption.copy(color = colors.mutedForeground),
                )
            }
        }
        NavChevronRightGlyph(color = colors.mutedForeground, size = 14.dp)
    }
}

private class NavPanelPositionProvider(
    private val gapPx: Int,
    private val marginPx: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val x = anchorBounds.left.coerceIn(
            marginPx,
            (windowSize.width - popupContentSize.width - marginPx).coerceAtLeast(marginPx),
        )
        val belowY = anchorBounds.bottom + gapPx
        val aboveY = anchorBounds.top - popupContentSize.height - gapPx
        val y = when {
            belowY + popupContentSize.height + marginPx <= windowSize.height -> belowY
            aboveY >= marginPx -> aboveY
            else -> belowY.coerceIn(marginPx, (windowSize.height - popupContentSize.height - marginPx).coerceAtLeast(marginPx))
        }
        return IntOffset(x, y)
    }
}

@Composable
private fun NavChevronDownGlyph(
    color: Color,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier.size(size).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(this@Canvas.size.width * 0.20f, this@Canvas.size.height * 0.34f)
            lineTo(this@Canvas.size.width * 0.50f, this@Canvas.size.height * 0.68f)
            lineTo(this@Canvas.size.width * 0.80f, this@Canvas.size.height * 0.34f)
        }
        drawPath(path, color = color, style = stroke)
    }
}

@Composable
private fun NavChevronRightGlyph(
    color: Color,
    size: Dp,
) {
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(this@Canvas.size.width * 0.34f, this@Canvas.size.height * 0.22f)
            lineTo(this@Canvas.size.width * 0.68f, this@Canvas.size.height * 0.50f)
            lineTo(this@Canvas.size.width * 0.34f, this@Canvas.size.height * 0.78f)
        }
        drawPath(path, color = color, style = stroke)
    }
}

@Composable
private fun HamburgerGlyph(color: Color, size: Dp) {
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        val stroke = 1.6.dp.toPx()
        val pad = this.size.width * 0.14f
        val end = this.size.width - pad
        drawLine(color, Offset(pad, this.size.height * 0.26f), Offset(end, this.size.height * 0.26f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(pad, this.size.height * 0.50f), Offset(end, this.size.height * 0.50f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(pad, this.size.height * 0.74f), Offset(end, this.size.height * 0.74f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}
