package com.kirakit.components.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography

enum class KiraDialogSize(val maxWidth: Dp) {
    Sm(384.dp),
    Default(512.dp),
    Lg(672.dp),
    Xl(896.dp),
    Full(Dp.Unspecified),
}

/**
 * Accessible modal & non-modal dialog aligned with kira-kit-v2's `Dialog` & `KiraDialog`.
 *
 * - Supports 5 size tiers (`Sm`, `Default`, `Lg`, `Xl`, `Full`).
 * - `scrollable = true` pins `KiraDialogHeader` and `KiraDialogFooter` while `KiraDialogBody` scrolls.
 * - `modal = false` renders a non-modal floating sheet that allows interaction with the background page.
 * - Respects mobile safe areas (`safeDrawingPadding`) and on-screen keyboard (`imePadding`).
 */
@Composable
fun KiraDialog(
    open: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    size: KiraDialogSize = KiraDialogSize.Default,
    modal: Boolean = true,
    scrollable: Boolean = false,
    showCloseButton: Boolean = true,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = modal,
    paneTitle: String = "Dialog",
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!open) return

    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    if (modal) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(
                dismissOnBackPress = dismissOnBackPress,
                dismissOnClickOutside = false, // handled by scrim for smooth alignment & reliable hit testing
                usePlatformDefaultWidth = false,
            ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.50f))
                    .then(
                        if (dismissOnClickOutside) {
                            Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onDismissRequest,
                            )
                        } else {
                            Modifier
                        },
                    )
                    .safeDrawingPadding()
                    .imePadding()
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedVisibility(
                    visible = entered,
                    enter = fadeIn(tween(200, easing = KiraMotion.EaseOutStrong)) +
                        scaleIn(tween(200, easing = KiraMotion.EaseOutStrong), initialScale = 0.95f),
                    exit = fadeOut(tween(150, easing = KiraMotion.EaseOutStrong)) +
                        scaleOut(tween(150, easing = KiraMotion.EaseOutStrong), targetScale = 0.95f),
                ) {
                    DialogCardSurface(
                        modifier = modifier,
                        size = size,
                        modal = true,
                        scrollable = scrollable,
                        showCloseButton = showCloseButton,
                        paneTitle = paneTitle,
                        onDismissRequest = onDismissRequest,
                        content = content,
                    )
                }
            }
        }
    } else {
        Popup(
            alignment = Alignment.TopEnd,
            onDismissRequest = {
                if (dismissOnClickOutside) onDismissRequest()
            },
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = dismissOnBackPress,
                dismissOnClickOutside = dismissOnClickOutside,
            ),
        ) {
            Box(
                modifier = Modifier
                    .safeDrawingPadding()
                    .imePadding()
                    .padding(16.dp)
                    .onPreviewKeyEvent { event ->
                        if (dismissOnBackPress && event.type == KeyEventType.KeyDown && event.key == Key.Escape) {
                            onDismissRequest()
                            true
                        } else {
                            false
                        }
                    },
            ) {
                AnimatedVisibility(
                    visible = entered,
                    enter = fadeIn(tween(180, easing = KiraMotion.EaseOutStrong)) +
                        scaleIn(tween(180, easing = KiraMotion.EaseOutStrong), initialScale = 0.98f),
                    exit = fadeOut(tween(140, easing = KiraMotion.EaseOutStrong)) +
                        scaleOut(tween(140, easing = KiraMotion.EaseOutStrong), targetScale = 0.98f),
                ) {
                    DialogCardSurface(
                        modifier = modifier,
                        size = size,
                        modal = false,
                        scrollable = scrollable,
                        showCloseButton = showCloseButton,
                        paneTitle = paneTitle,
                        onDismissRequest = onDismissRequest,
                        content = content,
                    )
                }
            }
        }
    }
}

/**
 * Convenience shorthand matching React's `KiraDialog` (title + description + body + footer).
 */
@Composable
fun KiraDialog(
    open: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    size: KiraDialogSize = KiraDialogSize.Default,
    modal: Boolean = true,
    scrollable: Boolean = false,
    showCloseButton: Boolean = true,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = modal,
    footer: (@Composable RowScope.() -> Unit)? = null,
    body: @Composable ColumnScope.() -> Unit,
) {
    KiraDialog(
        open = open,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        size = size,
        modal = modal,
        scrollable = scrollable,
        showCloseButton = showCloseButton,
        dismissOnBackPress = dismissOnBackPress,
        dismissOnClickOutside = dismissOnClickOutside,
        paneTitle = title,
    ) {
        KiraDialogHeader(title = title, description = description)
        KiraDialogBody(
            modifier = if (scrollable) Modifier.weight(1f, fill = false) else Modifier,
            scrollable = scrollable,
            content = body,
        )
        if (footer != null) {
            KiraDialogFooter(content = footer)
        }
    }
}

@Composable
private fun DialogCardSurface(
    modifier: Modifier,
    size: KiraDialogSize,
    modal: Boolean,
    scrollable: Boolean,
    showCloseButton: Boolean,
    paneTitle: String,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = KiraTheme.colors
    val shape = KiraTheme.radius.shapeXl
    val sizeModifier = when (size) {
        KiraDialogSize.Full -> Modifier.fillMaxSize()
        else -> Modifier.widthIn(max = size.maxWidth).fillMaxWidth()
    }

    Box(
        modifier = modifier
            .then(sizeModifier)
            .shadow(if (modal) 20.dp else 10.dp, shape, clip = false)
            .clip(shape)
            .background(colors.background, shape)
            .border(1.dp, colors.border, shape)
            // Absorb clicks inside the surface so the scrim underneath does not dismiss
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            )
            .semantics { this.paneTitle = paneTitle },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (size == KiraDialogSize.Full) Modifier.fillMaxHeight() else Modifier)
                .then(if (!scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                .padding(24.dp),
            content = content,
        )

        if (showCloseButton) {
            DialogCloseButton(
                onClick = onDismissRequest,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp),
            )
        }
    }
}

@Composable
fun KiraDialogHeader(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 32.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        KiraDialogTitle(title)
        if (description != null) {
            KiraDialogDescription(description)
        }
    }
}

@Composable
fun KiraDialogTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    BasicText(
        text = text,
        modifier = modifier.semantics { heading() },
        style = KiraTheme.typography.heading2.copy(
            color = KiraTheme.colors.foreground,
            fontSize = 18.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.SemiBold,
        ),
    )
}

@Composable
fun KiraDialogDescription(
    text: String,
    modifier: Modifier = Modifier,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = KiraTheme.typography.body.copy(color = KiraTheme.colors.mutedForeground),
    )
}

@Composable
fun KiraDialogBody(
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Composable
fun KiraDialogFooter(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
private fun DialogCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KiraTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val shape = KiraTheme.radius.shapeMd

    Box(
        modifier = modifier
            .size(44.dp)
            .pointerHoverIcon(PointerIcon.Hand)
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { contentDescription = "Close" },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(shape)
                .background(if (hovered) colors.muted else Color.Transparent, shape)
                .then(if (focused) Modifier.border(2.dp, colors.ring, shape) else Modifier)
                .alpha(if (hovered || focused) 1f else 0.7f),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(14.dp).clearAndSetSemantics {}) {
                val strokeWidth = 1.8.dp.toPx()
                val pad = size.width * 0.18f
                drawLine(
                    color = colors.foreground,
                    start = Offset(pad, pad),
                    end = Offset(size.width - pad, size.height - pad),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = colors.foreground,
                    start = Offset(size.width - pad, pad),
                    end = Offset(pad, size.height - pad),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}
