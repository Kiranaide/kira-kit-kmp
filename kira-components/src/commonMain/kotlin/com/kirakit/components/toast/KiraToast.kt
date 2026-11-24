package com.kirakit.components.toast

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kirakit.components.platform.KiraHapticFeedbackType
import com.kirakit.components.platform.rememberKiraHaptics
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

enum class KiraToastVariant {
    Default,
    Success,
    Error,
    Warning,
    Info,
    Loading,
}

@Immutable
data class KiraToastAction(
    val label: String,
    val onClick: () -> Unit,
)

@Immutable
data class KiraToastItem(
    val id: String,
    val title: String,
    val description: String? = null,
    val variant: KiraToastVariant = KiraToastVariant.Default,
    val durationMillis: Long = resolveToastTimeoutMillis(variant, null),
    val action: KiraToastAction? = null,
    val showIcon: Boolean = true,
    val icon: (@Composable (Color) -> Unit)? = null,
    val updateKey: Int = 0,
    val onClose: (() -> Unit)? = null,
)

/** Default auto-dismiss timeouts per variant (ms), matching `DURATION` in `toast.tsx`. `0L` disables auto-dismiss. */
object KiraToastDurations {
    const val Default: Long = 5_000L
    const val Success: Long = 4_000L
    const val Error: Long = 8_000L
    const val Warning: Long = 6_000L
    const val Info: Long = 5_000L
    const val Loading: Long = 0L
}

/**
 * Resolves the effective auto-dismiss timeout in milliseconds (`0L` means persistent).
 */
fun resolveToastTimeoutMillis(variant: KiraToastVariant, durationMillis: Long?): Long {
    if (durationMillis != null) {
        return if (durationMillis <= 0L || durationMillis == Long.MAX_VALUE) 0L else durationMillis
    }
    return when (variant) {
        KiraToastVariant.Default -> KiraToastDurations.Default
        KiraToastVariant.Success -> KiraToastDurations.Success
        KiraToastVariant.Error -> KiraToastDurations.Error
        KiraToastVariant.Warning -> KiraToastDurations.Warning
        KiraToastVariant.Info -> KiraToastDurations.Info
        KiraToastVariant.Loading -> KiraToastDurations.Loading
    }
}

/**
 * Imperative toast manager aligned with kira-kit-v2's `toast` API.
 *
 * Supports variant helpers (`success`, `error`, `warning`, `info`, `loading`), ID-based deduplication
 * & in-place `update`, `dismiss(id?)`, and suspending `promise(...)` lifecycle transitions.
 */
@Stable
class KiraToastController {
    private var nextSeq = 1
    private val _toasts = mutableStateListOf<KiraToastItem>()
    val toasts: List<KiraToastItem> get() = _toasts

    fun show(
        title: String,
        description: String? = null,
        variant: KiraToastVariant = KiraToastVariant.Default,
        durationMillis: Long? = null,
        action: KiraToastAction? = null,
        showIcon: Boolean = true,
        icon: (@Composable (Color) -> Unit)? = null,
        id: String? = null,
        onClose: (() -> Unit)? = null,
    ): String {
        val resolvedId = id ?: "kira-toast-${nextSeq++}"
        val timeout = resolveToastTimeoutMillis(variant, durationMillis)
        val existingIndex = _toasts.indexOfFirst { it.id == resolvedId }
        if (existingIndex >= 0) {
            val prev = _toasts[existingIndex]
            _toasts[existingIndex] = prev.copy(
                title = title,
                description = description,
                variant = variant,
                durationMillis = timeout,
                action = action,
                showIcon = showIcon,
                icon = icon,
                updateKey = prev.updateKey + 1,
                onClose = onClose,
            )
        } else {
            _toasts.add(
                KiraToastItem(
                    id = resolvedId,
                    title = title,
                    description = description,
                    variant = variant,
                    durationMillis = timeout,
                    action = action,
                    showIcon = showIcon,
                    icon = icon,
                    onClose = onClose,
                ),
            )
        }
        return resolvedId
    }

    fun message(
        title: String,
        description: String? = null,
        durationMillis: Long? = null,
        action: KiraToastAction? = null,
        id: String? = null,
        onClose: (() -> Unit)? = null,
    ): String = show(title, description, KiraToastVariant.Default, durationMillis, action, true, null, id, onClose)

    fun success(
        title: String,
        description: String? = null,
        durationMillis: Long? = null,
        action: KiraToastAction? = null,
        id: String? = null,
        onClose: (() -> Unit)? = null,
    ): String = show(title, description, KiraToastVariant.Success, durationMillis, action, true, null, id, onClose)

    fun error(
        title: String,
        description: String? = null,
        durationMillis: Long? = null,
        action: KiraToastAction? = null,
        id: String? = null,
        onClose: (() -> Unit)? = null,
    ): String = show(title, description, KiraToastVariant.Error, durationMillis, action, true, null, id, onClose)

    fun warning(
        title: String,
        description: String? = null,
        durationMillis: Long? = null,
        action: KiraToastAction? = null,
        id: String? = null,
        onClose: (() -> Unit)? = null,
    ): String = show(title, description, KiraToastVariant.Warning, durationMillis, action, true, null, id, onClose)

    fun info(
        title: String,
        description: String? = null,
        durationMillis: Long? = null,
        action: KiraToastAction? = null,
        id: String? = null,
        onClose: (() -> Unit)? = null,
    ): String = show(title, description, KiraToastVariant.Info, durationMillis, action, true, null, id, onClose)

    fun loading(
        title: String,
        description: String? = null,
        durationMillis: Long? = null,
        id: String? = null,
        onClose: (() -> Unit)? = null,
    ): String = show(title, description, KiraToastVariant.Loading, durationMillis, null, true, null, id, onClose)

    fun update(
        id: String,
        title: String? = null,
        description: String? = null,
        variant: KiraToastVariant? = null,
        durationMillis: Long? = null,
        action: KiraToastAction? = null,
    ) {
        val index = _toasts.indexOfFirst { it.id == id }
        if (index < 0) return
        val current = _toasts[index]
        val nextVariant = variant ?: current.variant
        val timingChanged = durationMillis != null || (variant != null && variant != current.variant)
        val nextTimeout = if (timingChanged) {
            resolveToastTimeoutMillis(nextVariant, durationMillis)
        } else {
            current.durationMillis
        }
        _toasts[index] = current.copy(
            title = title ?: current.title,
            description = description ?: current.description,
            variant = nextVariant,
            durationMillis = nextTimeout,
            action = action ?: current.action,
            updateKey = current.updateKey + 1,
        )
    }

    fun dismiss(id: String? = null) {
        if (id == null) {
            val all = _toasts.toList()
            _toasts.clear()
            all.forEach { it.onClose?.invoke() }
            return
        }
        val index = _toasts.indexOfFirst { it.id == id }
        if (index >= 0) {
            val removed = _toasts.removeAt(index)
            removed.onClose?.invoke()
        }
    }

    /**
     * Shows a loading toast while `block` executes, then transitions it in-place to Success or Error.
     */
    suspend fun <T> promise(
        loading: String,
        success: (T) -> String,
        error: (Throwable) -> String,
        description: String? = null,
        block: suspend () -> T,
    ): Result<T> {
        val id = loading(title = loading, description = description)
        val result = runCatching { block() }
        result.onSuccess { value ->
            update(id = id, title = success(value), variant = KiraToastVariant.Success)
        }.onFailure { err ->
            update(id = id, title = error(err), variant = KiraToastVariant.Error)
        }
        return result
    }
}

val LocalKiraToastController = compositionLocalOf<KiraToastController> {
    KiraToastController()
}

@Composable
fun rememberKiraToastController(): KiraToastController = remember { KiraToastController() }

/**
 * Provides [LocalKiraToastController] and renders the bottom-end stacked toast viewport above [content].
 *
 * - Displays up to [limit] visible toasts (newest on front, older peeking behind at reduced scale).
 * - Hovering (or tapping to expand on touch) fans out the stack and pauses auto-dismiss timers.
 * - Swiping horizontally dismisses any toast with haptic feedback.
 */
@Composable
fun KiraToastHost(
    controller: KiraToastController = rememberKiraToastController(),
    limit: Int = 3,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalKiraToastController provides controller) {
        Box(modifier = modifier.fillMaxSize()) {
            content()
            KiraToastViewport(
                controller = controller,
                limit = limit.coerceAtLeast(1),
                modifier = Modifier.align(Alignment.BottomEnd),
            )
        }
    }
}

@Composable
fun KiraToastViewport(
    controller: KiraToastController = LocalKiraToastController.current,
    limit: Int = 3,
    modifier: Modifier = Modifier,
) {
    val activeToasts = controller.toasts.takeLast(limit).reversed()
    val activeIds = remember(activeToasts) { activeToasts.map { it.id }.toSet() }

    val renderedToasts = remember { mutableStateListOf<KiraToastItem>() }
    var exitingIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val itemHeightsPx = remember { mutableStateMapOf<String, Int>() }
    val swipeExitDirs = remember { mutableStateMapOf<String, Float>() }

    // Synchronize renderedToasts with activeToasts while keeping dismissed toasts mounted for exit animation.
    LaunchedEffect(activeToasts) {
        val activeById = activeToasts.associateBy { it.id }
        // Update existing or mark missing as exiting
        val newlyExiting = mutableSetOf<String>()
        for (i in renderedToasts.indices) {
            val existing = renderedToasts[i]
            val updated = activeById[existing.id]
            if (updated != null) {
                if (updated != existing) renderedToasts[i] = updated
            } else if (existing.id !in exitingIds) {
                newlyExiting.add(existing.id)
            }
        }
        // Append newly added active toasts
        val existingIds = renderedToasts.map { it.id }.toSet()
        activeToasts.forEach { item ->
            if (item.id !in existingIds) {
                renderedToasts.add(0, item)
            }
        }
        // Re-order live items to match activeToasts order (newest first) while keeping exiting items in place
        val nextExiting = (exitingIds - activeIds) + newlyExiting
        exitingIds = nextExiting
    }

    // Remove exiting items once their exit animation completes.
    LaunchedEffect(exitingIds) {
        if (exitingIds.isEmpty()) return@LaunchedEffect
        val toRemove = exitingIds
        delay(190L)
        renderedToasts.removeAll { it.id in toRemove && it.id !in activeIds }
        toRemove.forEach { id ->
            if (id !in activeIds) {
                itemHeightsPx.remove(id)
                swipeExitDirs.remove(id)
            }
        }
        exitingIds = exitingIds - toRemove
    }

    if (renderedToasts.isEmpty()) return

    val stackInteraction = remember { MutableInteractionSource() }
    val hovered by stackInteraction.collectIsHoveredAsState()
    var tapExpanded by remember { mutableStateOf(false) }
    val expanded = hovered || tapExpanded

    LaunchedEffect(activeToasts.size) {
        if (activeToasts.size <= 1) tapExpanded = false
    }

    val density = LocalDensity.current
    val gapDp = 10.dp
    val peekDp = 11.dp

    // Compute live stack index (0 = newest) and cumulative expanded Y offset from bottom.
    val liveOrdered = renderedToasts.filter { it.id in activeIds }.sortedBy { item ->
        activeToasts.indexOfFirst { it.id == item.id }.let { if (it < 0) Int.MAX_VALUE else it }
    }
    val stackIndexById = remember(liveOrdered) {
        liveOrdered.mapIndexed { idx, item -> item.id to idx }.toMap()
    }

    // Total viewport height expands smoothly so hover stays alive over the entire fanned-out stack.
    val expandedTotalHeightDp: Dp = remember(liveOrdered, itemHeightsPx.toMap(), density) {
        if (liveOrdered.isEmpty()) 0.dp else {
            val heightsSum = liveOrdered.sumOf { itemHeightsPx[it.id] ?: with(density) { 56.dp.roundToPx() } }
            val gaps = (liveOrdered.size - 1).coerceAtLeast(0) * with(density) { gapDp.roundToPx() }
            with(density) { (heightsSum + gaps).toDp() }
        }
    }
    val collapsedHeightDp: Dp = remember(liveOrdered, itemHeightsPx.toMap(), density) {
        val frontPx = liveOrdered.firstOrNull()?.let { itemHeightsPx[it.id] } ?: with(density) { 56.dp.roundToPx() }
        val extraPeek = (liveOrdered.size - 1).coerceAtLeast(0) * with(density) { peekDp.roundToPx() }
        with(density) { (frontPx + extraPeek).toDp() }
    }
    val containerHeight by animateDpAsState(
        targetValue = if (expanded) expandedTotalHeightDp else collapsedHeightDp,
        animationSpec = tween(220, easing = KiraMotion.EaseOutStrong),
        label = "ToastViewportHeight",
    )

    // Render back-to-front so newest (index 0) sits visually on top.
    val drawOrder = renderedToasts.sortedByDescending { item ->
        stackIndexById[item.id] ?: -1
    }

    Box(
        modifier = modifier
            .safeDrawingPadding()
            .imePadding()
            .padding(16.dp)
            .widthIn(max = 384.dp)
            .fillMaxWidth()
            .defaultMinSize(minHeight = containerHeight)
            .hoverable(stackInteraction),
        contentAlignment = Alignment.BottomEnd,
    ) {
        drawOrder.forEach { item ->
            val stackIndex = stackIndexById[item.id] ?: 0
            val isExiting = item.id in exitingIds
            val expandedOffsetDp = remember(liveOrdered, stackIndex, itemHeightsPx.toMap(), density) {
                var accPx = 0
                val gapPx = with(density) { gapDp.roundToPx() }
                for (i in 0 until stackIndex) {
                    val belowId = liveOrdered.getOrNull(i)?.id ?: break
                    accPx += (itemHeightsPx[belowId] ?: with(density) { 56.dp.roundToPx() }) + gapPx
                }
                with(density) { (-accPx).toDp() }
            }

            key(item.id) {
                KiraToastCard(
                    item = item,
                    stackIndex = stackIndex,
                    expanded = expanded,
                    expandedOffsetY = expandedOffsetDp,
                    peekStep = peekDp,
                    isExiting = isExiting,
                    swipeExitDirection = swipeExitDirs[item.id] ?: 0f,
                    paused = expanded,
                    onMeasuredHeight = { h -> itemHeightsPx[item.id] = h },
                    onDismiss = { swipeDir ->
                        if (swipeDir != 0f) swipeExitDirs[item.id] = swipeDir
                        controller.dismiss(item.id)
                    },
                    onTapStack = {
                        if (activeToasts.size > 1) tapExpanded = !tapExpanded
                    },
                )
            }
        }
    }
}

@Composable
private fun KiraToastCard(
    item: KiraToastItem,
    stackIndex: Int,
    expanded: Boolean,
    expandedOffsetY: Dp,
    peekStep: Dp,
    isExiting: Boolean,
    swipeExitDirection: Float,
    paused: Boolean,
    onMeasuredHeight: (Int) -> Unit,
    onDismiss: (swipeDirection: Float) -> Unit,
    onTapStack: () -> Unit,
) {
    val colors = KiraTheme.colors
    val haptics = rememberKiraHaptics()
    val scope = rememberCoroutineScope()

    val (containerBg, borderColor, accentColor) = when (item.variant) {
        KiraToastVariant.Default -> Triple(colors.background, colors.border, colors.foreground)
        KiraToastVariant.Success -> Triple(colors.background, colors.success, colors.success)
        KiraToastVariant.Error -> Triple(colors.background, colors.destructive, colors.destructive)
        KiraToastVariant.Warning -> Triple(colors.background, colors.warning, colors.warning)
        KiraToastVariant.Info -> Triple(colors.background, colors.info, colors.info)
        KiraToastVariant.Loading -> Triple(colors.muted, colors.border, colors.mutedForeground)
    }

    val tintOverlay = when (item.variant) {
        KiraToastVariant.Success -> colors.success.copy(alpha = 0.09f)
        KiraToastVariant.Error -> colors.destructive.copy(alpha = 0.09f)
        KiraToastVariant.Warning -> colors.warning.copy(alpha = 0.09f)
        KiraToastVariant.Info -> colors.info.copy(alpha = 0.09f)
        else -> Color.Transparent
    }

    // Enter & exit transitions (slide up + scale + fade in, slide down / swipe fling + fade out).
    var appeared by remember(item.id) { mutableStateOf(false) }
    LaunchedEffect(item.id) { appeared = true }

    val enterProgress by animateFloatAsState(
        targetValue = if (appeared && !isExiting) 1f else 0f,
        animationSpec = if (isExiting) {
            tween(160, easing = KiraMotion.EaseOutStrong)
        } else {
            tween(220, easing = KiraMotion.EaseOutStrong)
        },
        label = "ToastPresence",
    )

    // Smooth linear countdown progress bar with pause/resume support.
    val progress = remember(item.id, item.updateKey) { Animatable(1f) }
    val dragX = remember(item.id) { Animatable(0f) }

    LaunchedEffect(item.id, item.updateKey, item.durationMillis, paused, isExiting) {
        if (isExiting) return@LaunchedEffect
        if (item.durationMillis <= 0L) {
            progress.snapTo(1f)
            return@LaunchedEffect
        }
        if (paused) {
            progress.stop()
            return@LaunchedEffect
        }
        val remainingMs = (item.durationMillis * progress.value).toLong().coerceAtLeast(1L)
        progress.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = remainingMs.toInt(), easing = LinearEasing),
        )
        if (progress.value <= 0.001f) {
            onDismiss(0f)
        }
    }

    val targetScale = if (expanded) 1f else (1f - stackIndex * 0.06f).coerceAtLeast(0.84f)
    val scale by animateFloatAsState(
        targetValue = targetScale * (0.95f + 0.05f * enterProgress),
        animationSpec = tween(220, easing = KiraMotion.EaseOutStrong),
        label = "ToastStackScale",
    )
    val stackOffsetY by animateDpAsState(
        targetValue = if (expanded) expandedOffsetY else -(peekStep * stackIndex),
        animationSpec = tween(220, easing = KiraMotion.EaseOutStrong),
        label = "ToastStackOffset",
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (expanded || stackIndex == 0) 1f else 0.90f,
        animationSpec = tween(200, easing = KiraMotion.EaseOutStrong),
        label = "ToastBehindAlpha",
    )

    val swipeAlpha = (1f - (abs(dragX.value) / 240f)).coerceIn(0.20f, 1f)
    val shape = KiraTheme.radius.shapeLg
    val isAssertive = item.variant == KiraToastVariant.Error || item.variant == KiraToastVariant.Warning
    val hasDescription = !item.description.isNullOrBlank()
    val hasProgressBar = item.durationMillis > 0L

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { onMeasuredHeight(it.height) }
            .graphicsLayer {
                val presenceOffsetY = (1f - enterProgress) * 44.dp.toPx()
                val swipeExitX = if (isExiting && swipeExitDirection != 0f) {
                    (1f - enterProgress) * swipeExitDirection * 220.dp.toPx()
                } else {
                    0f
                }
                translationX = dragX.value + swipeExitX
                translationY = stackOffsetY.toPx() + if (swipeExitDirection == 0f) presenceOffsetY else 0f
                scaleX = scale
                scaleY = scale
                alpha = (enterProgress * contentAlpha * swipeAlpha).coerceIn(0f, 1f)
            }
            .shadow(12.dp, shape, clip = false)
            .clip(shape)
            .background(containerBg, shape)
            .then(if (tintOverlay != Color.Transparent) Modifier.background(tintOverlay, shape) else Modifier)
            .border(1.dp, borderColor, shape)
            .pointerInput(item.id, isExiting) {
                if (isExiting) return@pointerInput
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (abs(dragX.value) > 96f) {
                            haptics.perform(KiraHapticFeedbackType.Light)
                            onDismiss(sign(dragX.value))
                        } else {
                            scope.launch {
                                dragX.animateTo(0f, KiraMotion.bouncySpring())
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            dragX.animateTo(0f, KiraMotion.bouncySpring())
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        scope.launch {
                            dragX.snapTo(dragX.value + dragAmount)
                        }
                    },
                )
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTapStack,
            )
            .semantics {
                liveRegion = if (isAssertive) LiveRegionMode.Assertive else LiveRegionMode.Polite
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 14.dp,
                    top = 12.dp,
                    end = 12.dp,
                    bottom = if (hasProgressBar) 14.dp else 12.dp,
                ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = if (hasDescription) Alignment.Top else Alignment.CenterVertically,
        ) {
            if (item.showIcon && (item.icon != null || item.variant != KiraToastVariant.Default)) {
                Box(
                    modifier = Modifier
                        .then(if (hasDescription) Modifier.padding(top = 1.dp) else Modifier)
                        .size(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (item.icon != null) {
                        item.icon.invoke(accentColor)
                    } else {
                        ToastVariantGlyph(variant = item.variant, color = accentColor)
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                BasicText(
                    text = item.title,
                    style = KiraTheme.typography.label.copy(
                        color = if (item.variant == KiraToastVariant.Default) colors.foreground else accentColor,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
                val description = item.description
                if (!description.isNullOrBlank()) {
                    BasicText(
                        text = description,
                        style = KiraTheme.typography.bodySmall.copy(
                            color = (if (item.variant == KiraToastVariant.Default) colors.mutedForeground else accentColor)
                                .copy(alpha = 0.88f),
                        ),
                    )
                }
            }

            if (item.action != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .clip(KiraTheme.radius.shapeSm)
                        .pointerHoverIcon(PointerIcon.Hand)
                        .clickable(
                            role = Role.Button,
                            onClick = {
                                haptics.perform(KiraHapticFeedbackType.Selection)
                                item.action.onClick()
                                onDismiss(0f)
                            },
                        )
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = item.action.label,
                        style = KiraTheme.typography.label.copy(
                            color = accentColor,
                            fontWeight = FontWeight.Medium,
                            textDecoration = TextDecoration.Underline,
                        ),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(KiraTheme.radius.shapeSm)
                    .alpha(0.75f)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable(
                        role = Role.Button,
                        onClick = {
                            haptics.perform(KiraHapticFeedbackType.Light)
                            onDismiss(0f)
                        },
                    )
                    .semantics { contentDescription = "Close toast" },
                contentAlignment = Alignment.Center,
            ) {
                ToastCloseGlyph(color = accentColor)
            }
        }

        if (hasProgressBar) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(accentColor.copy(alpha = 0.15f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.value.coerceIn(0f, 1f))
                        .height(2.dp)
                        .background(accentColor),
                )
            }
        }
    }
}

@Composable
private fun ToastVariantGlyph(variant: KiraToastVariant, color: Color) {
    if (variant == KiraToastVariant.Loading) {
        val transition = rememberInfiniteTransition(label = "ToastSpinner")
        val rotationState = transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
            label = "ToastSpinnerAngle",
        )
        Box(
            Modifier
                .size(18.dp)
                .clearAndSetSemantics {}
                .drawWithCache {
                    val strokeWidth = 2.dp.toPx()
                    val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    val inset = strokeWidth / 2f
                    val radius = size.minDimension / 2f - inset
                    val topLeft = Offset(inset, inset)
                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                    val trackColor = color.copy(alpha = 0.25f)
                    onDrawBehind {
                        drawCircle(trackColor, radius = radius, style = stroke)
                        drawArc(
                            color = color,
                            startAngle = rotationState.value - 90f,
                            sweepAngle = 100f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = stroke,
                        )
                    }
                },
        )
        return
    }

    Canvas(Modifier.size(18.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val r = size.minDimension / 2f - stroke.width
        when (variant) {
            KiraToastVariant.Success -> {
                val path = Path().apply {
                    moveTo(size.width * 0.18f, size.height * 0.52f)
                    lineTo(size.width * 0.42f, size.height * 0.76f)
                    lineTo(size.width * 0.82f, size.height * 0.26f)
                }
                drawPath(path, color, style = stroke)
            }
            KiraToastVariant.Error -> {
                val p = size.width * 0.24f
                drawLine(color, Offset(p, p), Offset(size.width - p, size.height - p), stroke.width, StrokeCap.Round)
                drawLine(color, Offset(size.width - p, p), Offset(p, size.height - p), stroke.width, StrokeCap.Round)
            }
            KiraToastVariant.Warning -> {
                val path = Path().apply {
                    moveTo(size.width * 0.50f, size.height * 0.14f)
                    lineTo(size.width * 0.88f, size.height * 0.84f)
                    lineTo(size.width * 0.12f, size.height * 0.84f)
                    close()
                }
                drawPath(path, color, style = stroke)
                drawLine(color, Offset(size.width * 0.50f, size.height * 0.40f), Offset(size.width * 0.50f, size.height * 0.62f), stroke.width, StrokeCap.Round)
                drawCircle(color, radius = 0.9.dp.toPx(), center = Offset(size.width * 0.50f, size.height * 0.74f))
            }
            KiraToastVariant.Info -> {
                drawCircle(color, radius = r, style = stroke)
                drawCircle(color, radius = 0.9.dp.toPx(), center = Offset(size.width * 0.50f, size.height * 0.34f))
                drawLine(color, Offset(size.width * 0.50f, size.height * 0.48f), Offset(size.width * 0.50f, size.height * 0.72f), stroke.width, StrokeCap.Round)
            }
            else -> Unit
        }
    }
}

@Composable
private fun ToastCloseGlyph(color: Color) {
    Canvas(Modifier.size(12.dp).clearAndSetSemantics {}) {
        val sw = 1.6.dp.toPx()
        val p = size.width * 0.2f
        drawLine(color, Offset(p, p), Offset(size.width - p, size.height - p), sw, StrokeCap.Round)
        drawLine(color, Offset(size.width - p, p), Offset(p, size.height - p), sw, StrokeCap.Round)
    }
}
