package com.kirakit.components.contextmenu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.kirakit.components.dropdownmenu.KiraDropdownMenuAlign
import com.kirakit.components.dropdownmenu.KiraDropdownMenuCheckboxItem
import com.kirakit.components.dropdownmenu.KiraDropdownMenuItem
import com.kirakit.components.dropdownmenu.KiraDropdownMenuLabel
import com.kirakit.components.dropdownmenu.KiraDropdownMenuRadioItem
import com.kirakit.components.dropdownmenu.KiraDropdownMenuScope
import com.kirakit.components.dropdownmenu.KiraDropdownMenuSeparator
import com.kirakit.components.dropdownmenu.KiraDropdownMenuSub
import com.kirakit.components.dropdownmenu.KiraMenuItemData
import com.kirakit.components.dropdownmenu.KiraMenuItemVariant
import com.kirakit.components.dropdownmenu.KiraMenuPopupSurface
import com.kirakit.components.dropdownmenu.LocalDropdownMenuScope
import com.kirakit.components.dropdownmenu.RenderKiraMenuItems
import com.kirakit.components.platform.KiraHapticFeedbackType
import com.kirakit.components.platform.rememberKiraHaptics
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography
import kotlin.math.roundToInt

/**
 * Resolves the clamped popup coordinate anchored to a pointer [localClickOffset] inside [anchorBounds].
 */
fun resolveContextMenuOffset(
    anchorBounds: IntRect,
    localClickOffset: IntOffset?,
    windowSize: IntSize,
    popupSize: IntSize,
    marginPx: Int = 8,
): IntOffset {
    val targetX = if (localClickOffset != null) {
        anchorBounds.left + localClickOffset.x
    } else {
        anchorBounds.left + (anchorBounds.width - popupSize.width) / 2
    }
    val targetY = if (localClickOffset != null) {
        anchorBounds.top + localClickOffset.y
    } else {
        anchorBounds.top + (anchorBounds.height / 2)
    }
    val maxX = (windowSize.width - popupSize.width - marginPx).coerceAtLeast(marginPx)
    val maxY = (windowSize.height - popupSize.height - marginPx).coerceAtLeast(marginPx)
    return IntOffset(
        x = targetX.coerceIn(marginPx, maxX),
        y = targetY.coerceIn(marginPx, maxY),
    )
}

/**
 * Cross-platform Context Menu aligned with kira-kit-v2's `ContextMenu`.
 *
 * - **Desktop**: right-click anywhere inside [trigger] opens the menu at the exact cursor point.
 * - **Android & iOS**: long-press opens the menu with medium haptic feedback, clamped within viewport bounds.
 * - **Keyboard**: focus the trigger region and press `Shift + F10` to open centered; `Escape` dismisses.
 */
@Composable
fun KiraContextMenu(
    trigger: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean? = null,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    showBackdrop: Boolean = false,
    minWidth: Dp = 200.dp,
    maxWidth: Dp = 320.dp,
    content: @Composable KiraDropdownMenuScope.() -> Unit,
) {
    val density = LocalDensity.current
    val haptics = rememberKiraHaptics()

    var internalExpanded by remember { mutableStateOf(false) }
    val isOpen = expanded ?: internalExpanded
    var anchorOffset by remember { mutableStateOf<IntOffset?>(null) }

    fun setOpen(next: Boolean, offset: Offset? = null) {
        if (next) {
            anchorOffset = offset?.let { IntOffset(it.x.roundToInt(), it.y.roundToInt()) }
        }
        if (expanded == null) internalExpanded = next
        onExpandedChange?.invoke(next)
    }

    val transitionState = remember { MutableTransitionState(false) }
    transitionState.targetState = isOpen

    val menuScope = remember(expanded, onExpandedChange) {
        object : KiraDropdownMenuScope {
            override fun close() = setOpen(false)
        }
    }

    val positionProvider = remember(density, anchorOffset) {
        val marginPx = with(density) { 8.dp.roundToPx() }
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize,
            ): IntOffset = resolveContextMenuOffset(
                anchorBounds = anchorBounds,
                localClickOffset = anchorOffset,
                windowSize = windowSize,
                popupSize = popupContentSize,
                marginPx = marginPx,
            )
        }
    }

    Box(
        modifier = modifier
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && event.isShiftPressed && event.key == Key.F10) {
                    haptics.perform(KiraHapticFeedbackType.Medium)
                    setOpen(true, offset = null)
                    true
                } else {
                    false
                }
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                            val pos = event.changes.firstOrNull()?.position
                            event.changes.forEach { it.consume() }
                            haptics.perform(KiraHapticFeedbackType.Selection)
                            setOpen(true, offset = pos)
                        }
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { pos ->
                        haptics.perform(KiraHapticFeedbackType.Medium)
                        setOpen(true, offset = pos)
                    },
                )
            },
    ) {
        trigger()

        if (transitionState.currentState || transitionState.targetState) {
            if (showBackdrop) {
                Popup(
                    alignment = Alignment.TopStart,
                    onDismissRequest = { setOpen(false) },
                    properties = PopupProperties(
                        focusable = false,
                        dismissOnBackPress = false,
                        dismissOnClickOutside = false,
                    ),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { setOpen(false) },
                            ),
                    )
                }
            }

            Popup(
                popupPositionProvider = positionProvider,
                onDismissRequest = { setOpen(false) },
                properties = PopupProperties(
                    focusable = true,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                ),
            ) {
                AnimatedVisibility(
                    visibleState = transitionState,
                    enter = fadeIn(tween(160, easing = KiraMotion.EaseOutStrong)) +
                        scaleIn(tween(160, easing = KiraMotion.EaseOutStrong), initialScale = 0.94f),
                    exit = fadeOut(tween(120, easing = KiraMotion.EaseOutStrong)) +
                        scaleOut(tween(120, easing = KiraMotion.EaseOutStrong), targetScale = 0.94f),
                ) {
                    KiraMenuPopupSurface(
                        minWidth = minWidth,
                        maxWidth = maxWidth,
                        showArrow = false,
                        align = KiraDropdownMenuAlign.Start,
                        onEscape = { setOpen(false) },
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
 * Data-driven Context Menu aligned with kira-kit-v2's `KiraContextMenu`.
 *
 * Accepts a structured list of [KiraMenuItemData] (actions, checkboxes, radio groups,
 * submenus, groups, separators), shows an empty-state message when `items` is empty,
 * and supports an optional inline error status banner.
 */
@Composable
fun KiraContextMenu(
    items: List<KiraMenuItemData>,
    trigger: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean? = null,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    emptyMessage: String = "No actions available.",
    errorMessage: String? = null,
    showBackdrop: Boolean = false,
    minWidth: Dp = 200.dp,
    maxWidth: Dp = 320.dp,
) {
    KiraContextMenu(
        trigger = trigger,
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        showBackdrop = showBackdrop,
        minWidth = minWidth,
        maxWidth = maxWidth,
    ) {
        if (items.isEmpty()) {
            BasicText(
                text = emptyMessage,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                style = KiraTheme.typography.bodySmall.copy(color = KiraTheme.colors.mutedForeground),
            )
        } else {
            RenderKiraMenuItems(items = items)
        }
        if (!errorMessage.isNullOrBlank()) {
            KiraContextMenuSeparator()
            BasicText(
                text = errorMessage,
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .semantics { liveRegion = LiveRegionMode.Assertive },
                style = KiraTheme.typography.bodySmall.copy(color = KiraTheme.colors.destructive),
            )
        }
    }
}

@Composable
fun KiraContextMenuLabel(text: String, modifier: Modifier = Modifier, inset: Boolean = false) =
    KiraDropdownMenuLabel(text = text, modifier = modifier, inset = inset)

@Composable
fun KiraContextMenuSeparator(modifier: Modifier = Modifier) =
    KiraDropdownMenuSeparator(modifier = modifier)

@Composable
fun KiraContextMenuItem(
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
) = KiraDropdownMenuItem(
    text = text,
    onClick = onClick,
    modifier = modifier,
    icon = icon,
    shortcut = shortcut,
    variant = variant,
    inset = inset,
    enabled = enabled,
    disabledReason = disabledReason,
    loading = loading,
    closeOnClick = closeOnClick,
)

@Composable
fun KiraContextMenuCheckboxItem(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable (Color) -> Unit)? = null,
    enabled: Boolean = true,
    disabledReason: String? = null,
    closeOnClick: Boolean = false,
) = KiraDropdownMenuCheckboxItem(
    text = text,
    checked = checked,
    onCheckedChange = onCheckedChange,
    modifier = modifier,
    icon = icon,
    enabled = enabled,
    disabledReason = disabledReason,
    closeOnClick = closeOnClick,
)

@Composable
fun KiraContextMenuRadioItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable (Color) -> Unit)? = null,
    enabled: Boolean = true,
    disabledReason: String? = null,
    closeOnClick: Boolean = false,
) = KiraDropdownMenuRadioItem(
    text = text,
    selected = selected,
    onClick = onClick,
    modifier = modifier,
    icon = icon,
    enabled = enabled,
    disabledReason = disabledReason,
    closeOnClick = closeOnClick,
)

@Composable
fun KiraContextMenuSub(
    label: String,
    modifier: Modifier = Modifier,
    icon: (@Composable (Color) -> Unit)? = null,
    enabled: Boolean = true,
    disabledReason: String? = null,
    content: @Composable KiraDropdownMenuScope.() -> Unit,
) = KiraDropdownMenuSub(
    label = label,
    modifier = modifier,
    icon = icon,
    enabled = enabled,
    disabledReason = disabledReason,
    content = content,
)
