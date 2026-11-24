package com.kirakit.components.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.components.drawer.KiraDrawer
import com.kirakit.components.drawer.KiraDrawerBody
import com.kirakit.components.drawer.KiraDrawerDescription
import com.kirakit.components.drawer.KiraDrawerFooter
import com.kirakit.components.drawer.KiraDrawerHandle
import com.kirakit.components.drawer.KiraDrawerHeader
import com.kirakit.components.drawer.KiraDrawerSide
import com.kirakit.components.drawer.KiraDrawerSize
import com.kirakit.components.drawer.KiraDrawerTitle
import com.kirakit.components.skeleton.KiraSkeletonLines
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.shapeFull
import com.kirakit.theme.typography

typealias KiraSheetSide = KiraDrawerSide
typealias KiraSheetSize = KiraDrawerSize

/**
 * Resolves the effective [KiraSheetSide] for responsive sheets:
 * below `640.dp` uses [mobileSide] (default `Bottom`), otherwise [side].
 */
fun resolveSheetSide(
    viewportWidthDp: Float,
    responsive: Boolean,
    side: KiraSheetSide,
    mobileSide: KiraSheetSide,
): KiraSheetSide = if (responsive && viewportWidthDp > 0f && viewportWidthDp < 640f) mobileSide else side

/**
 * Composable slide-over / responsive bottom-sheet panel aligned with kira-kit-v2's `Sheet`.
 *
 * Wraps [KiraDrawer] with automatic compact-viewport adaptation (`responsive = true`),
 * native swipe-to-dismiss, safe-area & IME insets, and optional drag handle.
 */
@Composable
fun KiraSheet(
    open: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    side: KiraSheetSide = KiraDrawerSide.Right,
    mobileSide: KiraSheetSide = KiraDrawerSide.Bottom,
    responsive: Boolean = true,
    size: KiraSheetSize = KiraDrawerSize.Default,
    showCloseButton: Boolean = true,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    swipeToDismiss: Boolean = true,
    snapPoints: List<Float> = emptyList(),
    activeSnapIndex: Int = 0,
    onSnapIndexChange: ((Int) -> Unit)? = null,
    paneTitle: String = "Sheet",
    content: @Composable ColumnScope.() -> Unit,
) {
    val density = LocalDensity.current
    val windowWidthDp = with(density) { LocalWindowInfo.current.containerSize.width.toDp().value }
    val activeSide = resolveSheetSide(windowWidthDp, responsive, side, mobileSide)

    KiraDrawer(
        open = open,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        side = activeSide,
        size = size,
        showCloseButton = showCloseButton,
        dismissOnBackPress = dismissOnBackPress,
        dismissOnClickOutside = dismissOnClickOutside,
        swipeToDismiss = swipeToDismiss,
        snapPoints = snapPoints,
        activeSnapIndex = activeSnapIndex,
        onSnapIndexChange = onSnapIndexChange,
        paneTitle = paneTitle,
    ) {
        if (activeSide == KiraDrawerSide.Bottom && snapPoints.isEmpty()) {
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
                        .clip(KiraTheme.radius.shapeFull)
                        .background(KiraTheme.colors.mutedForeground.copy(alpha = 0.35f)),
                )
            }
        }
        content()
    }
}

/**
 * Opinionated responsive sheet aligned with kira-kit-v2's `KiraSheet`.
 *
 * Adds built-in `loading` skeleton state, `error` banner with `onRetry`, and
 * `closeDisabledReason` dismissal guard on top of responsive edge adaptation.
 */
@Composable
fun KiraSheet(
    open: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    side: KiraSheetSide = KiraDrawerSide.Right,
    mobileSide: KiraSheetSide = KiraDrawerSide.Bottom,
    responsive: Boolean = true,
    size: KiraSheetSize = KiraDrawerSize.Default,
    showCloseButton: Boolean = true,
    swipeToDismiss: Boolean = true,
    closeDisabledReason: String? = null,
    loading: Boolean = false,
    loadingLabel: String = "Loading details…",
    loadingContent: (@Composable () -> Unit)? = null,
    error: String? = null,
    onRetry: (() -> Unit)? = null,
    footer: (@Composable RowScope.() -> Unit)? = null,
    body: @Composable ColumnScope.() -> Unit,
) {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val closeBlocked = !closeDisabledReason.isNullOrBlank()

    KiraSheet(
        open = open,
        onDismissRequest = { if (!closeBlocked) onDismissRequest() },
        modifier = modifier,
        side = side,
        mobileSide = mobileSide,
        responsive = responsive,
        size = size,
        showCloseButton = showCloseButton && !closeBlocked,
        dismissOnBackPress = !closeBlocked,
        dismissOnClickOutside = !closeBlocked,
        swipeToDismiss = swipeToDismiss && !closeBlocked,
        paneTitle = title,
    ) {
        KiraSheetHeader(title = title, description = description)
        KiraSheetBody(modifier = Modifier.weight(1f, fill = false)) {
            if (closeDisabledReason != null && closeDisabledReason.isNotBlank()) {
                BasicText(
                    text = closeDisabledReason,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                )
            }
            if (!loading && error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(radius.shapeLg)
                        .background(colors.destructive.copy(alpha = 0.08f))
                        .border(1.dp, colors.destructive.copy(alpha = 0.35f), radius.shapeLg)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    BasicText(
                        text = error,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
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
            Box(modifier = Modifier.fillMaxWidth().heightIn(min = if (loading) 140.dp else Dp.Unspecified)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (loading) 0f else 1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    content = body,
                )
                if (loading) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        BasicText(
                            text = loadingLabel,
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                            style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                        )
                        if (loadingContent != null) {
                            loadingContent()
                        } else {
                            KiraSkeletonLines(count = 4, lastLineWidthFraction = 0.6f)
                        }
                    }
                }
            }
        }
        if (footer != null) {
            KiraSheetFooter(content = footer)
        }
    }
}

@Composable
fun KiraSheetHandle(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) = KiraDrawerHandle(expanded = expanded, onExpandedChange = onExpandedChange, modifier = modifier)

@Composable
fun KiraSheetHeader(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
) = KiraDrawerHeader(title = title, modifier = modifier, description = description)

@Composable
fun KiraSheetTitle(text: String, modifier: Modifier = Modifier) =
    KiraDrawerTitle(text = text, modifier = modifier)

@Composable
fun KiraSheetDescription(text: String, modifier: Modifier = Modifier) =
    KiraDrawerDescription(text = text, modifier = modifier)

@Composable
fun KiraSheetBody(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) = KiraDrawerBody(modifier = modifier, content = content)

@Composable
fun KiraSheetFooter(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) = KiraDrawerFooter(modifier = modifier, content = content)
