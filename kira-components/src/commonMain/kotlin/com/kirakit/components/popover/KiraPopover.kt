package com.kirakit.components.popover

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
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
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.components.drawer.KiraDrawer
import com.kirakit.components.drawer.KiraDrawerSide
import com.kirakit.components.drawer.KiraDrawerSize
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.shapeFull
import com.kirakit.theme.typography

enum class KiraPopoverSide {
    Top,
    Bottom,
    Left,
    Right,
}

enum class KiraPopoverAlign {
    Start,
    Center,
    End,
}

enum class KiraPopoverSize(val width: Dp) {
    Auto(Dp.Unspecified),
    Sm(192.dp),
    Md(288.dp),
    Lg(384.dp),
    Xl(512.dp),
}

enum class KiraPopoverVariant {
    Default,
    Outline,
}

/**
 * Shared detached-popover handle: allows multiple triggers to open a single [KiraPopover]
 * with typed payload data (aligned with `createPopoverHandle` in kira-kit-v2).
 */
@Stable
class KiraPopoverHandle<T> {
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
fun <T> rememberKiraPopoverHandle(): KiraPopoverHandle<T> =
    remember { KiraPopoverHandle() }

/**
 * Returns `true` when closing or switching away from a popover should be intercepted
 * by the draft guard or close lock.
 */
fun shouldGuardPopoverDismissal(
    nextOpen: Boolean,
    dirty: Boolean,
    preventClose: Boolean,
    bypass: Boolean = false,
): Boolean = !nextOpen && !bypass && (dirty || preventClose)

/**
 * Resolves the effective popup side, flipping to the opposite edge when space is insufficient.
 */
fun resolvePopoverSide(
    preferred: KiraPopoverSide,
    anchorBounds: IntRect,
    windowSize: IntSize,
    popupSize: IntSize,
    gapPx: Int,
    marginPx: Int,
): KiraPopoverSide = when (preferred) {
    KiraPopoverSide.Bottom -> {
        val fitsBottom = anchorBounds.bottom + gapPx + popupSize.height + marginPx <= windowSize.height
        val fitsTop = anchorBounds.top - gapPx - popupSize.height >= marginPx
        if (!fitsBottom && fitsTop) KiraPopoverSide.Top else KiraPopoverSide.Bottom
    }
    KiraPopoverSide.Top -> {
        val fitsTop = anchorBounds.top - gapPx - popupSize.height >= marginPx
        val fitsBottom = anchorBounds.bottom + gapPx + popupSize.height + marginPx <= windowSize.height
        if (!fitsTop && fitsBottom) KiraPopoverSide.Bottom else KiraPopoverSide.Top
    }
    KiraPopoverSide.Right -> {
        val fitsRight = anchorBounds.right + gapPx + popupSize.width + marginPx <= windowSize.width
        val fitsLeft = anchorBounds.left - gapPx - popupSize.width >= marginPx
        if (!fitsRight && fitsLeft) KiraPopoverSide.Left else KiraPopoverSide.Right
    }
    KiraPopoverSide.Left -> {
        val fitsLeft = anchorBounds.left - gapPx - popupSize.width >= marginPx
        val fitsRight = anchorBounds.right + gapPx + popupSize.width + marginPx <= windowSize.width
        if (!fitsLeft && fitsRight) KiraPopoverSide.Right else KiraPopoverSide.Left
    }
}

/**
 * Anchored popover aligned with kira-kit-v2's `Popover` & `KiraPopover`.
 *
 * - Supports 4 sides with automatic collision flipping, 3 alignments, 5 width tiers,
 *   2 surface variants (`Default`, `Outline`), optional directional arrow, and backdrop scrim.
 * - Includes built-in **unsaved draft guard** (`dirty = true` prompts `Keep editing` / `Discard changes`)
 *   and `preventClose` locking while saving.
 * - On compact/mobile viewports (`< 640.dp`), automatically clamps width safely inside the viewport
 *   and optionally presents inside a swipe-to-dismiss bottom sheet (`useBottomSheetOnCompact = true`).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KiraPopover(
    open: Boolean,
    onOpenChange: (Boolean) -> Unit,
    trigger: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    description: String? = null,
    size: KiraPopoverSize = KiraPopoverSize.Md,
    variant: KiraPopoverVariant = KiraPopoverVariant.Default,
    side: KiraPopoverSide = KiraPopoverSide.Bottom,
    align: KiraPopoverAlign = KiraPopoverAlign.Center,
    sideOffset: Dp = 6.dp,
    showArrow: Boolean = false,
    showBackdrop: Boolean = false,
    showClose: Boolean = true,
    dirty: Boolean = false,
    onDiscard: (() -> Unit)? = null,
    preventClose: Boolean = false,
    useBottomSheetOnCompact: Boolean = false,
    footer: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val density = LocalDensity.current
    val containerWidthDp = with(density) { LocalWindowInfo.current.containerSize.width.toDp() }
    val isCompact = containerWidthDp > 0.dp && containerWidthDp < 640.dp

    var showDiscardConfirm by remember { mutableStateOf(false) }
    LaunchedEffect(open) {
        if (!open) showDiscardConfirm = false
    }

    fun requestClose(bypass: Boolean = false) {
        if (shouldGuardPopoverDismissal(nextOpen = false, dirty = dirty, preventClose = preventClose, bypass = bypass)) {
            if (!preventClose) {
                showDiscardConfirm = true
            }
            return
        }
        showDiscardConfirm = false
        onOpenChange(false)
    }

    val surfaceBg = when (variant) {
        KiraPopoverVariant.Default -> colors.popover
        KiraPopoverVariant.Outline -> colors.background
    }
    val surfaceFg = when (variant) {
        KiraPopoverVariant.Default -> colors.popoverForeground
        KiraPopoverVariant.Outline -> colors.foreground
    }

    val popoverBody: @Composable ColumnScope.() -> Unit = {
        if (title != null) {
            BasicText(
                text = title,
                modifier = Modifier.semantics { heading() },
                style = KiraTheme.typography.heading3.copy(
                    color = surfaceFg,
                    fontSize = 15.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
        if (description != null) {
            BasicText(
                text = description,
                style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (title != null || description != null) 8.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )

        if (footer != null) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                footer()
            }
        }

        if (showDiscardConfirm) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .border(1.dp, colors.border, radius.shapeMd)
                    .background(colors.muted.copy(alpha = 0.35f), radius.shapeMd)
                    .padding(12.dp)
                    .semantics { liveRegion = LiveRegionMode.Assertive },
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                BasicText(
                    text = "Discard unsaved changes?",
                    style = KiraTheme.typography.bodySmall.copy(
                        color = surfaceFg,
                        fontWeight = FontWeight.Medium,
                    ),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    KiraButton(
                        text = "Keep editing",
                        variant = KiraButtonVariant.Outline,
                        size = KiraButtonSize.Sm,
                        onClick = { showDiscardConfirm = false },
                    )
                    KiraButton(
                        text = "Discard changes",
                        variant = KiraButtonVariant.Destructive,
                        size = KiraButtonSize.Sm,
                        onClick = {
                            onDiscard?.invoke()
                            requestClose(bypass = true)
                        },
                    )
                }
            }
        }

        if (showClose && !showDiscardConfirm) {
            Box(modifier = Modifier.padding(top = 12.dp)) {
                KiraButton(
                    text = "Close",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    enabled = !preventClose,
                    onClick = { requestClose() },
                )
            }
        }
    }

    if (useBottomSheetOnCompact && isCompact) {
        Box(modifier = modifier) {
            trigger()
            KiraDrawer(
                open = open,
                onDismissRequest = { requestClose() },
                side = KiraDrawerSide.Bottom,
                size = KiraDrawerSize.Default,
                showCloseButton = !preventClose,
                dismissOnBackPress = !preventClose,
                dismissOnClickOutside = !preventClose,
                swipeToDismiss = !preventClose && !dirty,
                paneTitle = title ?: "Popover",
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 2.dp)
                        .clearAndSetSemantics {},
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(radius.shapeFull)
                            .background(colors.mutedForeground.copy(alpha = 0.35f)),
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    content = popoverBody,
                )
            }
        }
        return
    }

    val transitionState = remember { MutableTransitionState(false) }
    transitionState.targetState = open

    var resolvedSide by remember(side) { mutableStateOf(side) }
    val positionProvider = remember(density, side, align, sideOffset, showArrow) {
        PopoverPositionProvider(
            preferredSide = side,
            align = align,
            gapPx = with(density) { (sideOffset + if (showArrow) 6.dp else 0.dp).roundToPx() },
            marginPx = with(density) { 12.dp.roundToPx() },
            onSideResolved = { resolvedSide = it },
        )
    }

    val maxAllowedWidth = if (containerWidthDp > 0.dp) {
        (containerWidthDp - 24.dp).coerceAtLeast(220.dp)
    } else {
        512.dp
    }

    val widthModifier = when (size) {
        KiraPopoverSize.Auto -> Modifier.widthIn(min = 160.dp, max = maxAllowedWidth)
        else -> Modifier.width(size.width.coerceAtMost(maxAllowedWidth))
    }

    Box(modifier = modifier) {
        trigger()

        if (transitionState.currentState || transitionState.targetState) {
            if (showBackdrop) {
                Popup(
                    alignment = Alignment.TopStart,
                    onDismissRequest = { requestClose() },
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
                                onClick = { requestClose() },
                            ),
                    )
                }
            }

            Popup(
                popupPositionProvider = positionProvider,
                onDismissRequest = { requestClose() },
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
                    exit = fadeOut(tween(140, easing = KiraMotion.EaseOutStrong)) +
                        scaleOut(tween(140, easing = KiraMotion.EaseOutStrong), targetScale = 0.95f),
                ) {
                    val arrowAlignment = when (align) {
                        KiraPopoverAlign.Start -> Alignment.Start
                        KiraPopoverAlign.Center -> Alignment.CenterHorizontally
                        KiraPopoverAlign.End -> Alignment.End
                    }

                    val cardSurface: @Composable () -> Unit = {
                        Column(
                            modifier = widthModifier
                                .heightIn(max = 480.dp)
                                .shadow(12.dp, radius.shapeLg)
                                .clip(radius.shapeLg)
                                .background(surfaceBg)
                                .border(1.dp, colors.border, radius.shapeLg)
                                .onPreviewKeyEvent { event ->
                                    if (event.type == KeyEventType.KeyDown && event.key == Key.Escape) {
                                        requestClose()
                                        true
                                    } else {
                                        false
                                    }
                                }
                                .semantics { paneTitle = title ?: "Popover" }
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            content = popoverBody,
                        )
                    }

                    if (!showArrow) {
                        cardSurface()
                    } else {
                        when (resolvedSide) {
                            KiraPopoverSide.Bottom -> Column(horizontalAlignment = arrowAlignment) {
                                PopoverArrowGlyph(
                                    direction = KiraPopoverSide.Top,
                                    fillColor = surfaceBg,
                                    borderColor = colors.border,
                                    modifier = arrowEdgePadding(align),
                                )
                                cardSurface()
                            }
                            KiraPopoverSide.Top -> Column(horizontalAlignment = arrowAlignment) {
                                cardSurface()
                                PopoverArrowGlyph(
                                    direction = KiraPopoverSide.Bottom,
                                    fillColor = surfaceBg,
                                    borderColor = colors.border,
                                    modifier = arrowEdgePadding(align),
                                )
                            }
                            KiraPopoverSide.Right -> Row(verticalAlignment = Alignment.CenterVertically) {
                                PopoverArrowGlyph(
                                    direction = KiraPopoverSide.Left,
                                    fillColor = surfaceBg,
                                    borderColor = colors.border,
                                )
                                cardSurface()
                            }
                            KiraPopoverSide.Left -> Row(verticalAlignment = Alignment.CenterVertically) {
                                cardSurface()
                                PopoverArrowGlyph(
                                    direction = KiraPopoverSide.Right,
                                    fillColor = surfaceBg,
                                    borderColor = colors.border,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Stable
interface KiraPopoverFlowNavigation {
    val canGoBack: Boolean
    fun navigate(viewId: String)
    fun back()
}

@Immutable
data class KiraPopoverFlowView(
    val id: String,
    val title: String,
    val content: @Composable (KiraPopoverFlowNavigation) -> Unit,
)

/**
 * Multi-view in-popover navigation flow aligned with kira-kit-v2's `KiraPopoverFlow`.
 *
 * Maintains a navigation back-stack and animates smoothly between views without closing the popover.
 */
@Composable
fun KiraPopoverFlow(
    views: List<KiraPopoverFlowView>,
    open: Boolean,
    onOpenChange: (Boolean) -> Unit,
    trigger: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    initialViewId: String? = null,
    currentViewId: String? = null,
    onViewChange: ((String) -> Unit)? = null,
    size: KiraPopoverSize = KiraPopoverSize.Md,
    variant: KiraPopoverVariant = KiraPopoverVariant.Default,
    side: KiraPopoverSide = KiraPopoverSide.Bottom,
    align: KiraPopoverAlign = KiraPopoverAlign.Center,
    showArrow: Boolean = false,
    showClose: Boolean = true,
    useBottomSheetOnCompact: Boolean = true,
) {
    val startId = initialViewId ?: views.firstOrNull()?.id.orEmpty()
    var localCurrentId by remember(startId) { mutableStateOf(startId) }
    val activeId = currentViewId ?: localCurrentId
    val history = remember { mutableStateListOf<String>() }

    LaunchedEffect(open) {
        if (!open && currentViewId == null) {
            localCurrentId = startId
            history.clear()
        }
    }

    val navigation = remember(views, currentViewId, activeId) {
        object : KiraPopoverFlowNavigation {
            override val canGoBack: Boolean get() = history.isNotEmpty()
            override fun navigate(viewId: String) {
                if (viewId != activeId && views.any { it.id == viewId }) {
                    history.add(activeId)
                    if (currentViewId == null) localCurrentId = viewId
                    onViewChange?.invoke(viewId)
                }
            }
            override fun back() {
                if (history.isNotEmpty()) {
                    val prev = history.removeAt(history.lastIndex)
                    if (currentViewId == null) localCurrentId = prev
                    onViewChange?.invoke(prev)
                }
            }
        }
    }

    val currentView = views.firstOrNull { it.id == activeId }

    KiraPopover(
        open = open,
        onOpenChange = onOpenChange,
        trigger = trigger,
        modifier = modifier,
        title = currentView?.title ?: "No view available",
        size = size,
        variant = variant,
        side = side,
        align = align,
        showArrow = showArrow,
        showClose = showClose,
        useBottomSheetOnCompact = useBottomSheetOnCompact,
    ) {
        AnimatedContent(
            targetState = currentView?.id.orEmpty(),
            transitionSpec = {
                fadeIn(tween(KiraMotion.RouteFadeMs, easing = KiraMotion.EaseOutStrong)) togetherWith
                    fadeOut(tween(110))
            },
            label = "KiraPopoverFlowView",
        ) { viewId ->
            val target = views.firstOrNull { it.id == viewId }
            Column(modifier = Modifier.fillMaxWidth()) {
                target?.content?.invoke(navigation)
            }
        }
    }
}

private fun arrowEdgePadding(align: KiraPopoverAlign): Modifier = when (align) {
    KiraPopoverAlign.Start -> Modifier.padding(start = 16.dp)
    KiraPopoverAlign.Center -> Modifier
    KiraPopoverAlign.End -> Modifier.padding(end = 16.dp)
}

private class PopoverPositionProvider(
    private val preferredSide: KiraPopoverSide,
    private val align: KiraPopoverAlign,
    private val gapPx: Int,
    private val marginPx: Int,
    private val onSideResolved: (KiraPopoverSide) -> Unit,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val side = resolvePopoverSide(preferredSide, anchorBounds, windowSize, popupContentSize, gapPx, marginPx)
        onSideResolved(side)

        val rawX = when (side) {
            KiraPopoverSide.Left -> anchorBounds.left - popupContentSize.width - gapPx
            KiraPopoverSide.Right -> anchorBounds.right + gapPx
            KiraPopoverSide.Top, KiraPopoverSide.Bottom -> when (align) {
                KiraPopoverAlign.Start -> anchorBounds.left
                KiraPopoverAlign.Center -> anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2
                KiraPopoverAlign.End -> anchorBounds.right - popupContentSize.width
            }
        }
        val rawY = when (side) {
            KiraPopoverSide.Top -> anchorBounds.top - popupContentSize.height - gapPx
            KiraPopoverSide.Bottom -> anchorBounds.bottom + gapPx
            KiraPopoverSide.Left, KiraPopoverSide.Right -> when (align) {
                KiraPopoverAlign.Start -> anchorBounds.top
                KiraPopoverAlign.Center -> anchorBounds.top + (anchorBounds.height - popupContentSize.height) / 2
                KiraPopoverAlign.End -> anchorBounds.bottom - popupContentSize.height
            }
        }

        val maxX = (windowSize.width - popupContentSize.width - marginPx).coerceAtLeast(marginPx)
        val maxY = (windowSize.height - popupContentSize.height - marginPx).coerceAtLeast(marginPx)
        return IntOffset(
            x = rawX.coerceIn(marginPx, maxX),
            y = rawY.coerceIn(marginPx, maxY),
        )
    }
}

@Composable
private fun PopoverArrowGlyph(
    direction: KiraPopoverSide,
    fillColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
) {
    val isVertical = direction == KiraPopoverSide.Top || direction == KiraPopoverSide.Bottom
    val w = if (isVertical) 14.dp else 7.dp
    val h = if (isVertical) 7.dp else 14.dp

    Canvas(modifier.size(width = w, height = h).clearAndSetSemantics {}) {
        val fillPath = Path()
        val strokePath = Path()
        when (direction) {
            KiraPopoverSide.Top -> {
                fillPath.moveTo(0f, size.height)
                fillPath.lineTo(size.width / 2f, 0f)
                fillPath.lineTo(size.width, size.height)
                fillPath.close()
                strokePath.moveTo(0f, size.height)
                strokePath.lineTo(size.width / 2f, 0f)
                strokePath.lineTo(size.width, size.height)
            }
            KiraPopoverSide.Bottom -> {
                fillPath.moveTo(0f, 0f)
                fillPath.lineTo(size.width / 2f, size.height)
                fillPath.lineTo(size.width, 0f)
                fillPath.close()
                strokePath.moveTo(0f, 0f)
                strokePath.lineTo(size.width / 2f, size.height)
                strokePath.lineTo(size.width, 0f)
            }
            KiraPopoverSide.Left -> {
                fillPath.moveTo(size.width, 0f)
                fillPath.lineTo(0f, size.height / 2f)
                fillPath.lineTo(size.width, size.height)
                fillPath.close()
                strokePath.moveTo(size.width, 0f)
                strokePath.lineTo(0f, size.height / 2f)
                strokePath.lineTo(size.width, size.height)
            }
            KiraPopoverSide.Right -> {
                fillPath.moveTo(0f, 0f)
                fillPath.lineTo(size.width, size.height / 2f)
                fillPath.lineTo(0f, size.height)
                fillPath.close()
                strokePath.moveTo(0f, 0f)
                strokePath.lineTo(size.width, size.height / 2f)
                strokePath.lineTo(0f, size.height)
            }
        }
        drawPath(fillPath, color = fillColor)
        drawPath(
            strokePath,
            color = borderColor,
            style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}
