package com.kirakit.components.drawer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kirakit.components.platform.KiraHapticFeedbackType
import com.kirakit.components.platform.rememberKiraHaptics
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.shapeFull
import com.kirakit.theme.typography
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class KiraDrawerSide {
    Left,
    Right,
    Top,
    Bottom,
}

enum class KiraDrawerSize(
    internal val horizontalMaxWidth: Dp,
    internal val verticalMaxFraction: Float,
) {
    Sm(320.dp, 0.30f),
    Default(384.dp, 0.80f),
    Lg(512.dp, 0.90f),
    Xl(576.dp, 1.00f),
    Full(Dp.Unspecified, 1.00f),
}

/**
 * Resolves the nearest snap point index for a bottom sheet given the current visible height fraction
 * and drag direction. Returns `-1` when dragged below the dismiss threshold.
 */
fun resolveDrawerSnapIndex(
    currentFraction: Float,
    snapPoints: List<Float>,
    dismissThreshold: Float = 0.22f,
): Int {
    if (snapPoints.isEmpty()) return if (currentFraction < dismissThreshold) -1 else 0
    val sorted = snapPoints.sorted()
    val lowest = sorted.first()
    if (currentFraction < (lowest * 0.55f).coerceAtLeast(dismissThreshold)) return -1
    var bestIdx = 0
    var bestDist = abs(sorted[0] - currentFraction)
    for (i in 1 until sorted.size) {
        val d = abs(sorted[i] - currentFraction)
        if (d < bestDist) {
            bestDist = d
            bestIdx = i
        }
    }
    return bestIdx
}

/**
 * Interactive slide-over / bottom-sheet drawer aligned with kira-kit-v2's `Drawer` & `KiraDrawer`.
 *
 * - Supports all 4 edges (`Left`, `Right`, `Top`, `Bottom`) and 5 size tiers (`Sm`, `Default`, `Lg`, `Xl`, `Full`).
 * - Native swipe-to-dismiss along the sheet's edge axis with dynamic backdrop scrim fade & haptic feedback.
 * - Optional bottom-sheet `snapPoints` (fractions in `0f..1f`) with `KiraDrawerHandle`.
 * - Respects mobile safe areas (`safeDrawingPadding`) and on-screen keyboard (`imePadding`).
 */
@Composable
fun KiraDrawer(
    open: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    side: KiraDrawerSide = KiraDrawerSide.Right,
    size: KiraDrawerSize = KiraDrawerSize.Default,
    showCloseButton: Boolean = true,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    swipeToDismiss: Boolean = true,
    snapPoints: List<Float> = emptyList(),
    activeSnapIndex: Int = 0,
    onSnapIndexChange: ((Int) -> Unit)? = null,
    paneTitle: String = "Drawer",
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!open) return

    val scope = rememberCoroutineScope()
    val haptics = rememberKiraHaptics()
    val colors = KiraTheme.colors
    val density = LocalDensity.current

    val hasSnaps = side == KiraDrawerSide.Bottom && snapPoints.isNotEmpty()
    val sortedSnaps = remember(snapPoints) { snapPoints.sorted() }
    val clampedSnapIndex = activeSnapIndex.coerceIn(0, (sortedSnaps.size - 1).coerceAtLeast(0))

    // 0f = fully open, 1f = fully closed off-screen
    val slideProgress = remember { Animatable(1f) }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        slideProgress.animateTo(0f, tween(280, easing = KiraMotion.EaseOutStrong))
    }

    fun animateClose() {
        scope.launch {
            haptics.perform(KiraHapticFeedbackType.Light)
            slideProgress.animateTo(1f, tween(220, easing = KiraMotion.EaseOutStrong))
            onDismissRequest()
        }
    }

    Dialog(
        onDismissRequest = { if (dismissOnBackPress) animateClose() },
        properties = DialogProperties(
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val viewportWidthPx = with(density) { maxWidth.toPx() }.coerceAtLeast(1f)
            val viewportHeightPx = with(density) { maxHeight.toPx() }.coerceAtLeast(1f)

            val isHorizontal = side == KiraDrawerSide.Left || side == KiraDrawerSide.Right
            val panelWidthDp = when {
                !isHorizontal -> maxWidth
                size == KiraDrawerSize.Full -> maxWidth
                else -> (maxWidth * 0.75f).coerceAtMost(size.horizontalMaxWidth)
            }
            val panelHeightDp = when {
                isHorizontal -> maxHeight
                hasSnaps -> maxHeight * sortedSnaps[clampedSnapIndex].coerceIn(0.2f, 1f)
                size == KiraDrawerSize.Full || size == KiraDrawerSize.Xl -> maxHeight
                else -> maxHeight * size.verticalMaxFraction
            }

            val axisExtentPx = with(density) {
                (if (isHorizontal) panelWidthDp else panelHeightDp).toPx().coerceAtLeast(1f)
            }

            // Scrim overlay rendered in Draw phase so drag/slide animation triggers 0 recompositions
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val effectiveClosedFraction = (slideProgress.value + (abs(dragOffsetPx) / axisExtentPx)).coerceIn(0f, 1f)
                        val scrimAlpha = (1f - effectiveClosedFraction) * 0.50f
                        if (scrimAlpha > 0.001f) {
                            drawRect(Color.Black, alpha = scrimAlpha)
                        }
                    }
                    .then(
                        if (dismissOnClickOutside) {
                            Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = ::animateClose,
                            )
                        } else {
                            Modifier
                        },
                    ),
            )

            val alignment = when (side) {
                KiraDrawerSide.Left -> Alignment.CenterStart
                KiraDrawerSide.Right -> Alignment.CenterEnd
                KiraDrawerSide.Top -> Alignment.TopCenter
                KiraDrawerSide.Bottom -> Alignment.BottomCenter
            }

            val cornerRadius = if (size == KiraDrawerSize.Full) 0.dp else KiraTheme.radius.xl
            val sheetShape = remember(side, cornerRadius) {
                when (side) {
                    KiraDrawerSide.Left -> RoundedCornerShape(topEnd = cornerRadius, bottomEnd = cornerRadius)
                    KiraDrawerSide.Right -> RoundedCornerShape(topStart = cornerRadius, bottomStart = cornerRadius)
                    KiraDrawerSide.Top -> RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius)
                    KiraDrawerSide.Bottom -> RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
                }
            }

            val gestureModifier = if (!swipeToDismiss) {
                Modifier
            } else if (isHorizontal) {
                Modifier.pointerInput(side, axisExtentPx) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (abs(dragOffsetPx) > axisExtentPx * 0.28f) {
                                dragOffsetPx = 0f
                                animateClose()
                            } else {
                                dragOffsetPx = 0f
                            }
                        },
                        onDragCancel = { dragOffsetPx = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            dragOffsetPx = when (side) {
                                KiraDrawerSide.Left -> (dragOffsetPx + dragAmount).coerceAtMost(0f)
                                KiraDrawerSide.Right -> (dragOffsetPx + dragAmount).coerceAtLeast(0f)
                                KiraDrawerSide.Top, KiraDrawerSide.Bottom -> 0f
                            }
                        },
                    )
                }
            } else {
                Modifier.pointerInput(side, axisExtentPx, sortedSnaps, clampedSnapIndex) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (hasSnaps) {
                                val currentHeightPx = axisExtentPx - dragOffsetPx
                                val nextIdx = resolveDrawerSnapIndex(
                                    currentFraction = currentHeightPx / viewportHeightPx,
                                    snapPoints = sortedSnaps,
                                )
                                dragOffsetPx = 0f
                                if (nextIdx < 0) {
                                    animateClose()
                                } else {
                                    haptics.perform(KiraHapticFeedbackType.Selection)
                                    onSnapIndexChange?.invoke(nextIdx)
                                }
                            } else if (abs(dragOffsetPx) > axisExtentPx * 0.28f) {
                                dragOffsetPx = 0f
                                animateClose()
                            } else {
                                dragOffsetPx = 0f
                            }
                        },
                        onDragCancel = { dragOffsetPx = 0f },
                        onVerticalDrag = { _, dragAmount ->
                            dragOffsetPx = when (side) {
                                KiraDrawerSide.Top -> (dragOffsetPx + dragAmount).coerceAtMost(0f)
                                KiraDrawerSide.Bottom -> {
                                    val minDrag = if (hasSnaps) -(viewportHeightPx - axisExtentPx) else 0f
                                    (dragOffsetPx + dragAmount).coerceAtLeast(minDrag)
                                }
                                KiraDrawerSide.Left, KiraDrawerSide.Right -> 0f
                            }
                        },
                    )
                }
            }

            val sizeModifier = when {
                isHorizontal && size == KiraDrawerSize.Full -> Modifier.fillMaxSize()
                isHorizontal -> Modifier.width(panelWidthDp).fillMaxHeight()
                hasSnaps -> Modifier.fillMaxWidth().height(panelHeightDp)
                size == KiraDrawerSize.Full || size == KiraDrawerSize.Xl -> Modifier.fillMaxSize()
                else -> Modifier.fillMaxWidth().heightIn(max = panelHeightDp)
            }

            Box(
                modifier = modifier
                    .align(alignment)
                    .then(sizeModifier)
                    .graphicsLayer {
                        val closedOffset = slideProgress.value * axisExtentPx
                        when (side) {
                            KiraDrawerSide.Left -> translationX = -closedOffset + dragOffsetPx
                            KiraDrawerSide.Right -> translationX = closedOffset + dragOffsetPx
                            KiraDrawerSide.Top -> translationY = -closedOffset + dragOffsetPx
                            KiraDrawerSide.Bottom -> translationY = closedOffset + dragOffsetPx
                        }
                    }
                    .then(gestureModifier)
                    .shadow(20.dp, sheetShape, clip = false)
                    .clip(sheetShape)
                    .background(colors.background, sheetShape)
                    .border(1.dp, colors.border, sheetShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    )
                    .safeDrawingPadding()
                    .imePadding()
                    .semantics { this.paneTitle = paneTitle },
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    content = content,
                )

                if (showCloseButton) {
                    DrawerCloseButton(
                        onClick = ::animateClose,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 6.dp, end = 6.dp),
                    )
                }
            }
        }
    }
}

/**
 * Convenience shorthand matching React's `KiraDrawer` (title + description + body + footer + optional handle).
 */
@Composable
fun KiraDrawer(
    open: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    side: KiraDrawerSide = KiraDrawerSide.Right,
    size: KiraDrawerSize = KiraDrawerSize.Default,
    showCloseButton: Boolean = true,
    swipeToDismiss: Boolean = true,
    snapPoints: List<Float> = emptyList(),
    activeSnapIndex: Int = 0,
    onSnapIndexChange: ((Int) -> Unit)? = null,
    handle: (@Composable () -> Unit)? = null,
    footer: (@Composable RowScope.() -> Unit)? = null,
    body: @Composable ColumnScope.() -> Unit,
) {
    KiraDrawer(
        open = open,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        side = side,
        size = size,
        showCloseButton = showCloseButton,
        swipeToDismiss = swipeToDismiss,
        snapPoints = snapPoints,
        activeSnapIndex = activeSnapIndex,
        onSnapIndexChange = onSnapIndexChange,
        paneTitle = title,
    ) {
        if (handle != null) {
            handle()
        }
        KiraDrawerHeader(title = title, description = description)
        KiraDrawerBody(
            modifier = Modifier.weight(1f, fill = false),
            content = body,
        )
        if (footer != null) {
            KiraDrawerFooter(content = footer)
        }
    }
}

@Composable
fun KiraDrawerHandle(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KiraTheme.colors
    val haptics = rememberKiraHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val shape = KiraTheme.radius.shapeLg

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 80.dp)
                .heightIn(min = 44.dp)
                .clip(shape)
                .then(if (focused) Modifier.border(2.dp, colors.ring, shape) else Modifier)
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = {
                        haptics.perform(KiraHapticFeedbackType.Selection)
                        onExpandedChange(!expanded)
                    },
                )
                .semantics {
                    contentDescription = if (expanded) "Collapse drawer" else "Expand drawer"
                    stateDescription = if (expanded) "Expanded" else "Collapsed"
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(KiraTheme.radius.shapeFull)
                    .background(colors.mutedForeground.copy(alpha = 0.40f)),
            )
        }
    }
}

@Composable
fun KiraDrawerHeader(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    val colors = KiraTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 48.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            KiraDrawerTitle(title)
            if (description != null) {
                KiraDrawerDescription(description)
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
    }
}

@Composable
fun KiraDrawerTitle(
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
fun KiraDrawerDescription(
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
fun KiraDrawerBody(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Composable
fun KiraDrawerFooter(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = KiraTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
private fun DrawerCloseButton(
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
                .then(if (hovered || focused) Modifier else Modifier.graphicsLayer { alpha = 0.7f }),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(14.dp)
                    .clearAndSetSemantics {}
                    .drawWithCache {
                        val strokeWidth = 1.8.dp.toPx()
                        val pad = size.width * 0.18f
                        val tl = Offset(pad, pad)
                        val br = Offset(size.width - pad, size.height - pad)
                        val tr = Offset(size.width - pad, pad)
                        val bl = Offset(pad, size.height - pad)
                        onDrawBehind {
                            drawLine(color = colors.foreground, start = tl, end = br, strokeWidth = strokeWidth, cap = StrokeCap.Round)
                            drawLine(color = colors.foreground, start = tr, end = bl, strokeWidth = strokeWidth, cap = StrokeCap.Round)
                        }
                    },
            )
        }
    }
}
