package com.kirakit.components.alertdialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.components.input.KiraInput
import com.kirakit.components.label.KiraLabel
import com.kirakit.components.platform.KiraHapticFeedbackType
import com.kirakit.components.platform.rememberKiraHaptics
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class KiraAlertDialogVariant {
    Default,
    Info,
    Success,
    Warning,
    Destructive,
}

enum class KiraAlertDialogSize(val maxWidth: Dp) {
    Sm(320.dp),
    Default(384.dp),
    Lg(448.dp),
    Xl(512.dp),
    Full(Dp.Unspecified),
}

enum class KiraAlertDialogFooterLayout {
    Horizontal,
    Stacked,
}

enum class KiraAlertDialogListVariant {
    Bullet,
    Numbered,
    Checkbox,
}

fun isAlertDialogConfirmEnabled(
    confirmText: String?,
    typedText: String,
    remainingDelaySeconds: Int,
    loading: Boolean,
): Boolean {
    if (loading || remainingDelaySeconds > 0) return false
    if (!confirmText.isNullOrEmpty() && typedText != confirmText) return false
    return true
}

/**
 * High-stakes confirmation dialog aligned with kira-kit-v2's `AlertDialog` & `KiraAlertDialog`.
 *
 * Unlike `KiraDialog`, an `AlertDialog` requires an explicit user choice (outside clicks do not
 * dismiss by default) and supports semantic media badges, warning callouts, consequence lists,
 * typed `confirmText` gates, `confirmDelaySeconds` countdowns, and async `onConfirmAsync` loading.
 */
@Composable
fun KiraAlertDialog(
    open: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    variant: KiraAlertDialogVariant = KiraAlertDialogVariant.Default,
    size: KiraAlertDialogSize = KiraAlertDialogSize.Default,
    confirmLabel: String = "Continue",
    cancelLabel: String = "Cancel",
    showCancel: Boolean = true,
    showCloseButton: Boolean = false,
    centered: Boolean = false,
    footerLayout: KiraAlertDialogFooterLayout = KiraAlertDialogFooterLayout.Horizontal,
    confirmText: String? = null,
    confirmDelaySeconds: Int = 0,
    loading: Boolean = false,
    errorMessage: String? = null,
    onConfirm: (() -> Unit)? = null,
    onConfirmAsync: (suspend () -> Result<Unit>)? = null,
    onCancel: (() -> Unit)? = null,
    body: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val transitionState = remember { MutableTransitionState(false) }
    transitionState.targetState = open
    if (!transitionState.currentState && !transitionState.targetState) return

    val colors = KiraTheme.colors
    val haptics = rememberKiraHaptics()
    val scope = rememberCoroutineScope()

    var typed by remember(open) { mutableStateOf("") }
    var remainingDelay by remember(open, confirmDelaySeconds) {
        mutableIntStateOf(confirmDelaySeconds.coerceAtLeast(0))
    }
    var asyncPending by remember(open) { mutableStateOf(false) }
    var asyncError by remember(open) { mutableStateOf<String?>(null) }

    LaunchedEffect(open, confirmDelaySeconds) {
        if (!open || confirmDelaySeconds <= 0) return@LaunchedEffect
        var left = confirmDelaySeconds
        while (left > 0) {
            delay(1_000L)
            left--
            remainingDelay = left
        }
    }

    val busy = loading || asyncPending
    val canConfirm = isAlertDialogConfirmEnabled(confirmText, typed, remainingDelay, busy)
    val resolvedError = asyncError ?: errorMessage
    val confirmButtonVariant = when (variant) {
        KiraAlertDialogVariant.Destructive -> KiraButtonVariant.Destructive
        else -> KiraButtonVariant.Primary
    }

    fun handleConfirm() {
        if (!canConfirm) return
        haptics.perform(
            if (variant == KiraAlertDialogVariant.Destructive) {
                KiraHapticFeedbackType.Medium
            } else {
                KiraHapticFeedbackType.Selection
            },
        )
        if (onConfirmAsync != null) {
            asyncPending = true
            asyncError = null
            scope.launch {
                val res = runCatching { onConfirmAsync().getOrThrow() }
                asyncPending = false
                res.onSuccess { onDismissRequest() }
                    .onFailure { asyncError = it.message ?: "Action failed. Please try again." }
            }
        } else {
            onConfirm?.invoke()
            onDismissRequest()
        }
    }

    fun handleCancel() {
        if (busy) return
        onCancel?.invoke()
        onDismissRequest()
    }

    Dialog(
        onDismissRequest = { if (!busy) handleCancel() },
        properties = DialogProperties(
            dismissOnBackPress = !busy,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.50f))
                .safeDrawingPadding()
                .imePadding()
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(
                visibleState = transitionState,
                enter = fadeIn(tween(180, easing = KiraMotion.EaseOutStrong)) +
                    scaleIn(tween(180, easing = KiraMotion.EaseOutStrong), initialScale = 0.95f),
                exit = fadeOut(tween(140, easing = KiraMotion.EaseOutStrong)) +
                    scaleOut(tween(140, easing = KiraMotion.EaseOutStrong), targetScale = 0.95f),
            ) {
                val shape = KiraTheme.radius.shapeXl
                val sizeModifier = if (size == KiraAlertDialogSize.Full) {
                    Modifier.fillMaxSize()
                } else {
                    Modifier.widthIn(max = size.maxWidth).fillMaxWidth()
                }

                Box(
                    modifier = modifier
                        .then(sizeModifier)
                        .shadow(20.dp, shape, clip = false)
                        .clip(shape)
                        .background(colors.background, shape)
                        .border(1.dp, colors.border, shape)
                        .semantics { paneTitle = title },
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .verticalScroll(rememberScrollState())
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
                        ) {
                            if (variant != KiraAlertDialogVariant.Default) {
                                KiraAlertDialogMedia(variant = variant)
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(if (showCloseButton && !centered) Modifier.padding(end = 28.dp) else Modifier),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
                            ) {
                                BasicText(
                                    text = title,
                                    modifier = Modifier.semantics { heading() },
                                    style = KiraTheme.typography.heading3.copy(
                                        color = colors.foreground,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = if (centered) TextAlign.Center else TextAlign.Start,
                                    ),
                                )
                                if (description != null) {
                                    BasicText(
                                        text = description,
                                        style = KiraTheme.typography.body.copy(
                                            color = colors.mutedForeground,
                                            textAlign = if (centered) TextAlign.Center else TextAlign.Start,
                                        ),
                                    )
                                }
                            }

                            if (body != null) {
                                body()
                            }

                            if (!confirmText.isNullOrEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    KiraLabel(
                                        text = "Type \"$confirmText\" to confirm",
                                        required = true,
                                    )
                                    KiraInput(
                                        value = typed,
                                        onValueChange = { typed = it },
                                        placeholder = confirmText,
                                        enabled = !busy,
                                        isError = typed.isNotEmpty() && typed != confirmText,
                                    )
                                }
                            }

                            if (!resolvedError.isNullOrBlank()) {
                                KiraAlertDialogWarning(
                                    text = resolvedError,
                                    variant = KiraAlertDialogVariant.Destructive,
                                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                                )
                            }
                        }

                        // Tinted footer band matching alert-dialog.tsx (`bg-muted/50 border-t p-4`)
                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                        val resolvedConfirmLabel = if (remainingDelay > 0) {
                            "$confirmLabel (${remainingDelay}s)"
                        } else {
                            confirmLabel
                        }

                        if (footerLayout == KiraAlertDialogFooterLayout.Stacked) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.muted.copy(alpha = 0.45f))
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                KiraButton(
                                    text = resolvedConfirmLabel,
                                    variant = confirmButtonVariant,
                                    enabled = canConfirm,
                                    loading = busy,
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = ::handleConfirm,
                                )
                                if (showCancel) {
                                    KiraButton(
                                        text = cancelLabel,
                                        variant = KiraButtonVariant.Outline,
                                        enabled = !busy,
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = ::handleCancel,
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.muted.copy(alpha = 0.45f))
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(
                                    8.dp,
                                    if (centered) Alignment.CenterHorizontally else Alignment.End,
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (showCancel) {
                                    KiraButton(
                                        text = cancelLabel,
                                        variant = KiraButtonVariant.Outline,
                                        size = KiraButtonSize.Sm,
                                        enabled = !busy,
                                        onClick = ::handleCancel,
                                    )
                                }
                                KiraButton(
                                    text = resolvedConfirmLabel,
                                    variant = confirmButtonVariant,
                                    size = KiraButtonSize.Sm,
                                    enabled = canConfirm,
                                    loading = busy,
                                    onClick = ::handleConfirm,
                                )
                            }
                        }
                    }

                    if (showCloseButton) {
                        AlertDialogCloseButton(
                            enabled = !busy,
                            onClick = ::handleCancel,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 6.dp, end = 6.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KiraAlertDialogMedia(
    variant: KiraAlertDialogVariant,
    modifier: Modifier = Modifier,
) {
    val colors = KiraTheme.colors
    val (bg, fg) = when (variant) {
        KiraAlertDialogVariant.Info -> colors.info.copy(alpha = 0.18f) to colors.info
        KiraAlertDialogVariant.Success -> colors.success.copy(alpha = 0.18f) to colors.success
        KiraAlertDialogVariant.Warning -> colors.warning.copy(alpha = 0.18f) to colors.warning
        KiraAlertDialogVariant.Destructive -> colors.destructive.copy(alpha = 0.18f) to colors.destructive
        KiraAlertDialogVariant.Default -> colors.muted to colors.foreground
    }
    val shape = KiraTheme.radius.shapeMd

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(shape)
            .background(bg, shape),
        contentAlignment = Alignment.Center,
    ) {
        AlertDialogSemanticGlyph(variant = variant, color = fg, size = 22.dp)
    }
}

@Composable
fun KiraAlertDialogWarning(
    text: String,
    modifier: Modifier = Modifier,
    variant: KiraAlertDialogVariant = KiraAlertDialogVariant.Warning,
) {
    val colors = KiraTheme.colors
    val tone = when (variant) {
        KiraAlertDialogVariant.Info -> colors.info
        KiraAlertDialogVariant.Success -> colors.success
        KiraAlertDialogVariant.Destructive -> colors.destructive
        else -> colors.warning
    }
    val shape = KiraTheme.radius.shapeLg
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(tone.copy(alpha = 0.10f), shape)
            .border(1.dp, tone.copy(alpha = 0.30f), shape)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        AlertDialogSemanticGlyph(variant = variant, color = tone, size = 16.dp)
        BasicText(
            text = text,
            modifier = Modifier.weight(1f),
            style = KiraTheme.typography.bodySmall.copy(color = tone),
        )
    }
}

@Composable
fun KiraAlertDialogList(
    items: List<String>,
    modifier: Modifier = Modifier,
    variant: KiraAlertDialogListVariant = KiraAlertDialogListVariant.Bullet,
) {
    val colors = KiraTheme.colors
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items.forEachIndexed { index, item ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when (variant) {
                    KiraAlertDialogListVariant.Bullet -> BasicText(
                        text = "•",
                        style = KiraTheme.typography.body.copy(color = colors.mutedForeground),
                    )
                    KiraAlertDialogListVariant.Numbered -> BasicText(
                        text = "${index + 1}.",
                        style = KiraTheme.typography.bodySmall.copy(
                            color = colors.mutedForeground,
                            fontFeatureSettings = "tnum",
                        ),
                    )
                    KiraAlertDialogListVariant.Checkbox -> Box(
                        modifier = Modifier
                            .size(14.dp)
                            .border(1.dp, colors.mutedForeground.copy(alpha = 0.45f), RoundedCornerShape(3.dp)),
                    )
                }
                BasicText(
                    text = item,
                    style = KiraTheme.typography.bodySmall.copy(color = colors.foreground),
                )
            }
        }
    }
}

@Composable
private fun AlertDialogCloseButton(
    enabled: Boolean,
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
            .alpha(if (enabled) 1f else 0.4f)
            .then(if (enabled) Modifier.pointerHoverIcon(PointerIcon.Hand) else Modifier)
            .hoverable(interactionSource, enabled = enabled)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
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
                .then(if (focused) Modifier.border(2.dp, colors.ring, shape) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(14.dp).clearAndSetSemantics {}) {
                val strokeWidth = 1.8.dp.toPx()
                val pad = size.width * 0.18f
                drawLine(colors.foreground, Offset(pad, pad), Offset(size.width - pad, size.height - pad), strokeWidth, StrokeCap.Round)
                drawLine(colors.foreground, Offset(size.width - pad, pad), Offset(pad, size.height - pad), strokeWidth, StrokeCap.Round)
            }
        }
    }
}

@Composable
private fun AlertDialogSemanticGlyph(variant: KiraAlertDialogVariant, color: Color, size: Dp) {
    Canvas(Modifier.size(size).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val r = this.size.minDimension / 2f - stroke.width
        when (variant) {
            KiraAlertDialogVariant.Info, KiraAlertDialogVariant.Default -> {
                drawCircle(color, radius = r, style = stroke)
                drawCircle(color, radius = 1.dp.toPx(), center = Offset(this.size.width / 2f, this.size.height * 0.34f))
                drawLine(color, Offset(this.size.width / 2f, this.size.height * 0.48f), Offset(this.size.width / 2f, this.size.height * 0.72f), stroke.width, StrokeCap.Round)
            }
            KiraAlertDialogVariant.Success -> {
                drawCircle(color, radius = r, style = stroke)
                val path = Path().apply {
                    moveTo(this@Canvas.size.width * 0.30f, this@Canvas.size.height * 0.52f)
                    lineTo(this@Canvas.size.width * 0.45f, this@Canvas.size.height * 0.67f)
                    lineTo(this@Canvas.size.width * 0.72f, this@Canvas.size.height * 0.36f)
                }
                drawPath(path, color, style = stroke)
            }
            KiraAlertDialogVariant.Warning -> {
                val path = Path().apply {
                    moveTo(this@Canvas.size.width * 0.50f, this@Canvas.size.height * 0.14f)
                    lineTo(this@Canvas.size.width * 0.88f, this@Canvas.size.height * 0.84f)
                    lineTo(this@Canvas.size.width * 0.12f, this@Canvas.size.height * 0.84f)
                    close()
                }
                drawPath(path, color, style = stroke)
                drawLine(color, Offset(this.size.width / 2f, this.size.height * 0.40f), Offset(this.size.width / 2f, this.size.height * 0.62f), stroke.width, StrokeCap.Round)
                drawCircle(color, radius = 1.dp.toPx(), center = Offset(this.size.width / 2f, this.size.height * 0.74f))
            }
            KiraAlertDialogVariant.Destructive -> {
                drawCircle(color, radius = r, style = stroke)
                val p1 = this.size.width * 0.34f
                val p2 = this.size.width * 0.66f
                drawLine(color, Offset(p1, p1), Offset(p2, p2), stroke.width, StrokeCap.Round)
                drawLine(color, Offset(p2, p1), Offset(p1, p2), stroke.width, StrokeCap.Round)
            }
        }
    }
}
