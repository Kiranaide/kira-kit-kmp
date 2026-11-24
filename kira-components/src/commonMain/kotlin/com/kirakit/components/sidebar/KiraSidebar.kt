package com.kirakit.components.sidebar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kirakit.theme.KiraMotion
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import com.kirakit.theme.typography

enum class KiraSidebarSide {
    Left,
    Right,
}

enum class KiraSidebarVariant {
    Default,
    Floating,
    Inset,
}

enum class KiraSidebarCollapsible {
    Offcanvas,
    Icon,
    None,
}

enum class KiraSidebarBadgeVariant {
    Default,
    Primary,
    Secondary,
    Outline,
    Destructive,
    Success,
    Warning,
}

@Stable
class KiraSidebarState(
    initialCollapsed: Boolean = false,
    initialOpenMobile: Boolean = false,
    val collapsible: KiraSidebarCollapsible = KiraSidebarCollapsible.Icon,
    val variant: KiraSidebarVariant = KiraSidebarVariant.Default,
    val side: KiraSidebarSide = KiraSidebarSide.Left,
) {
    var collapsed by mutableStateOf(initialCollapsed)
    var openMobile by mutableStateOf(initialOpenMobile)
    var isMobile by mutableStateOf(true)
        internal set

    val expanded: Boolean
        get() = isMobile || !collapsed || collapsible == KiraSidebarCollapsible.None

    fun toggle() {
        if (isMobile) {
            openMobile = !openMobile
        } else if (collapsible != KiraSidebarCollapsible.None) {
            collapsed = !collapsed
        }
    }

    fun closeMobile() {
        openMobile = false
    }
}

@Composable
fun rememberKiraSidebarState(
    initialCollapsed: Boolean = false,
    initialOpenMobile: Boolean = false,
    collapsible: KiraSidebarCollapsible = KiraSidebarCollapsible.Icon,
    variant: KiraSidebarVariant = KiraSidebarVariant.Default,
    side: KiraSidebarSide = KiraSidebarSide.Left,
): KiraSidebarState = remember(collapsible, variant, side) {
    KiraSidebarState(
        initialCollapsed = initialCollapsed,
        initialOpenMobile = initialOpenMobile,
        collapsible = collapsible,
        variant = variant,
        side = side,
    )
}

val LocalKiraSidebarState = compositionLocalOf<KiraSidebarState> {
    error("LocalKiraSidebarState not provided. Wrap your layout in KiraSidebarScaffold.")
}

/**
 * Responsive Kira Kit Sidebar shell matching `kira-kit-v2`:
 * - `< 768.dp` (mobile): slide-over modal drawer with scrim, safe-area insets, and swipe-to-dismiss.
 * - `>= 768.dp` (desktop/tablet): persistent collapsible panel (`Default`, `Floating`, or `Inset`).
 */
@Composable
fun KiraSidebarScaffold(
    sidebar: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    state: KiraSidebarState = rememberKiraSidebarState(),
    mobileBreakpoint: Dp = 768.dp,
    expandedWidth: Dp = 264.dp,
    collapsedWidth: Dp = 68.dp,
    content: @Composable () -> Unit,
) {
    val colors = KiraTheme.colors
    val spacing = KiraTheme.spacing

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        val containerWidth = maxWidth
        val isMobile = containerWidth < mobileBreakpoint
        state.isMobile = isMobile

        CompositionLocalProvider(LocalKiraSidebarState provides state) {
            if (isMobile) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        content()
                    }

                    // Animated scrim
                    AnimatedVisibility(
                        visible = state.openMobile,
                        enter = fadeIn(tween(200, easing = KiraMotion.EaseOutStrong)),
                        exit = fadeOut(tween(150, easing = KiraMotion.EaseOutStrong)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.48f))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClickLabel = "Close navigation",
                                    onClick = { state.closeMobile() },
                                ),
                        )
                    }

                    // Slide-over mobile drawer
                    val drawerWidth = minOf(expandedWidth, containerWidth - 48.dp)
                    val drawerAlign = if (state.side == KiraSidebarSide.Left) {
                        Alignment.CenterStart
                    } else {
                        Alignment.CenterEnd
                    }
                    AnimatedVisibility(
                        visible = state.openMobile,
                        modifier = Modifier.align(drawerAlign),
                        enter = slideInHorizontally(
                            animationSpec = tween(220, easing = KiraMotion.EaseOutStrong),
                            initialOffsetX = { if (state.side == KiraSidebarSide.Left) -it else it },
                        ),
                        exit = slideOutHorizontally(
                            animationSpec = tween(160, easing = KiraMotion.EaseOutStrong),
                            targetOffsetX = { if (state.side == KiraSidebarSide.Left) -it else it },
                        ),
                    ) {
                        var dragDelta by remember { mutableStateOf(0f) }
                        Column(
                            modifier = Modifier
                                .width(drawerWidth)
                                .fillMaxHeight()
                                .background(colors.sidebar)
                                .border(BorderStroke(1.dp, colors.sidebarBorder))
                                .safeDrawingPadding()
                                .pointerInput(state.side) {
                                    detectHorizontalDragGestures(
                                        onDragEnd = {
                                            val dismissed = if (state.side == KiraSidebarSide.Left) {
                                                dragDelta < -72f
                                            } else {
                                                dragDelta > 72f
                                            }
                                            if (dismissed) state.closeMobile()
                                            dragDelta = 0f
                                        },
                                        onDragCancel = { dragDelta = 0f },
                                        onHorizontalDrag = { _, dragAmount ->
                                            dragDelta += dragAmount
                                        },
                                    )
                                },
                            content = sidebar,
                        )
                    }
                }
            } else {
                val targetWidth = when {
                    state.collapsible == KiraSidebarCollapsible.None -> expandedWidth
                    state.collapsed && state.collapsible == KiraSidebarCollapsible.Offcanvas -> 0.dp
                    state.collapsed && state.collapsible == KiraSidebarCollapsible.Icon -> collapsedWidth
                    else -> expandedWidth
                }
                val animatedWidth by animateDpAsState(
                    targetValue = targetWidth,
                    animationSpec = tween(220, easing = KiraMotion.EaseOutStrong),
                    label = "KiraSidebarWidth",
                )

                val shellBg = if (state.variant == KiraSidebarVariant.Inset) colors.sidebar else colors.background

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(shellBg)
                        .safeDrawingPadding(),
                ) {
                    val sidebarPanel: @Composable () -> Unit = {
                        if (animatedWidth > 0.dp) {
                            val panelShape = KiraTheme.radius.shapeXl
                            val panelModifier = when (state.variant) {
                                KiraSidebarVariant.Floating -> Modifier
                                    .padding(spacing.sm)
                                    .width(animatedWidth)
                                    .fillMaxHeight()
                                    .clip(panelShape)
                                    .background(colors.sidebar, panelShape)
                                    .border(BorderStroke(1.dp, colors.sidebarBorder), panelShape)

                                KiraSidebarVariant.Inset -> Modifier
                                    .width(animatedWidth)
                                    .fillMaxHeight()
                                    .background(colors.sidebar)

                                KiraSidebarVariant.Default -> Modifier
                                    .width(animatedWidth)
                                    .fillMaxHeight()
                                    .background(colors.sidebar)
                            }

                            Row(modifier = Modifier.fillMaxHeight()) {
                                if (state.variant == KiraSidebarVariant.Default && state.side == KiraSidebarSide.Right) {
                                    Box(Modifier.width(1.dp).fillMaxHeight().background(colors.sidebarBorder))
                                }
                                Column(modifier = panelModifier, content = sidebar)
                                if (state.variant == KiraSidebarVariant.Default && state.side == KiraSidebarSide.Left) {
                                    Box(Modifier.width(1.dp).fillMaxHeight().background(colors.sidebarBorder))
                                }
                            }
                        }
                    }

                    val mainContent: @Composable () -> Unit = {
                        val contentModifier = if (state.variant == KiraSidebarVariant.Inset) {
                            val shape = KiraTheme.radius.shapeXl
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(spacing.sm)
                                .clip(shape)
                                .background(colors.background, shape)
                                .border(BorderStroke(1.dp, colors.border), shape)
                        } else {
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        }
                        Box(modifier = contentModifier) {
                            content()
                        }
                    }

                    if (state.side == KiraSidebarSide.Left) {
                        sidebarPanel()
                        mainContent()
                    } else {
                        mainContent()
                        sidebarPanel()
                    }
                }
            }
        }
    }
}

@Composable
fun KiraSidebarHeader(
    modifier: Modifier = Modifier,
    showCloseOnMobile: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val state = LocalKiraSidebarState.current
    val colors = KiraTheme.colors
    val spacing = KiraTheme.spacing

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.md, vertical = spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        if (state.isMobile && showCloseOnMobile) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f), content = content)
                KiraSidebarCloseButton(onClick = { state.closeMobile() })
            }
        } else {
            content()
        }
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(colors.sidebarBorder))
}

@Composable
fun ColumnScope.KiraSidebarContent(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = KiraTheme.spacing
    Column(
        modifier = modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.sm, vertical = spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
        content = content,
    )
}

@Composable
fun KiraSidebarFooter(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = KiraTheme.colors
    val spacing = KiraTheme.spacing
    Box(Modifier.fillMaxWidth().height(1.dp).background(colors.sidebarBorder))
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.md, vertical = spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.xs),
        content = content,
    )
}

@Composable
fun KiraSidebarDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = KiraTheme.spacing.xs, vertical = KiraTheme.spacing.xs)
            .height(1.dp)
            .background(KiraTheme.colors.sidebarBorder),
    )
}

@Composable
fun KiraSidebarGroup(
    label: String,
    modifier: Modifier = Modifier,
    collapsible: Boolean = true,
    defaultOpen: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val state = LocalKiraSidebarState.current
    val colors = KiraTheme.colors
    val spacing = KiraTheme.spacing
    var open by remember { mutableStateOf(defaultOpen) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (state.expanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(KiraTheme.radius.shapeMd)
                    .then(
                        if (collapsible) {
                            Modifier.clickable(
                                role = Role.Button,
                                onClick = { open = !open },
                            )
                        } else {
                            Modifier
                        }
                    )
                    .padding(horizontal = spacing.sm, vertical = 6.dp)
                    .semantics {
                        heading()
                        if (collapsible) {
                            stateDescription = if (open) "Expanded" else "Collapsed"
                        }
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    text = label.uppercase(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = KiraTheme.typography.caption.copy(
                        color = colors.mutedForeground,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
                if (collapsible) {
                    ChevronGlyph(
                        expanded = open,
                        color = colors.mutedForeground,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = !state.expanded || open,
            enter = expandVertically(tween(180, easing = KiraMotion.EaseOutStrong)) + fadeIn(tween(150)),
            exit = shrinkVertically(tween(160, easing = KiraMotion.EaseOutStrong)) + fadeOut(tween(120)),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                content = content,
            )
        }
    }
}

@Composable
fun KiraSidebarItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingMonogram: String? = null,
    badge: String? = null,
    badgeVariant: KiraSidebarBadgeVariant = KiraSidebarBadgeVariant.Default,
    enabled: Boolean = true,
    closeMobileOnClick: Boolean = true,
) {
    val state = LocalKiraSidebarState.current
    val colors = KiraTheme.colors
    val spacing = KiraTheme.spacing
    // Matches kira-kit-v2 sidebar item (`rounded-lg`)
    val shape = KiraTheme.radius.shapeLg
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val targetBg = when {
        selected -> colors.sidebarAccent
        isPressed -> colors.sidebarAccent.copy(alpha = 0.85f)
        isHovered -> colors.sidebarAccent.copy(alpha = 0.55f)
        else -> Color.Transparent
    }
    val bg by animateColorAsState(
        targetValue = targetBg,
        animationSpec = KiraMotion.fastTween(),
        label = "KiraSidebarItemBg",
    )

    val contentColor = if (selected) colors.sidebarAccentForeground else colors.sidebarForeground
    val monogramText = leadingMonogram ?: label.take(2).uppercase()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 44.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(shape)
            .background(bg, shape)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    onClick()
                    if (closeMobileOnClick && state.isMobile) {
                        state.closeMobile()
                    }
                },
            )
            .padding(horizontal = if (state.expanded) spacing.sm else 6.dp, vertical = spacing.xs),
        horizontalArrangement = if (state.expanded) Arrangement.spacedBy(spacing.sm) else Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state.expanded && selected) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(18.dp)
                    .clip(CircleShape)
                    .background(colors.sidebarPrimary),
            )
        }

        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(KiraTheme.radius.shapeMd)
                .background(
                    if (selected) colors.sidebarPrimary else colors.sidebarBorder.copy(alpha = 0.45f),
                ),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                text = monogramText,
                maxLines = 1,
                style = KiraTheme.typography.caption.copy(
                    color = if (selected) colors.sidebarPrimaryForeground else contentColor,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }

        if (state.expanded) {
            BasicText(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
                style = KiraTheme.typography.label.copy(
                    color = contentColor,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                ),
            )
            if (badge != null) {
                KiraSidebarBadge(text = badge, variant = badgeVariant)
            }
        }
    }
}

@Composable
fun KiraSidebarBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: KiraSidebarBadgeVariant = KiraSidebarBadgeVariant.Default,
) {
    val colors = KiraTheme.colors
    // Matches kira-kit-v2 sidebar.tsx SidebarBadge (`rounded-md`)
    val shape = KiraTheme.radius.shapeMd

    val (bg, fg, border) = when (variant) {
        KiraSidebarBadgeVariant.Default -> Triple(colors.sidebarAccent, colors.sidebarAccentForeground, Color.Transparent)
        KiraSidebarBadgeVariant.Primary -> Triple(colors.sidebarPrimary, colors.sidebarPrimaryForeground, Color.Transparent)
        KiraSidebarBadgeVariant.Secondary -> Triple(colors.secondary, colors.secondaryForeground, Color.Transparent)
        KiraSidebarBadgeVariant.Outline -> Triple(Color.Transparent, colors.sidebarForeground, colors.sidebarBorder)
        KiraSidebarBadgeVariant.Destructive -> Triple(colors.destructive, colors.destructiveForeground, Color.Transparent)
        KiraSidebarBadgeVariant.Success -> Triple(colors.success.copy(alpha = 0.18f), colors.sidebarForeground, Color.Transparent)
        KiraSidebarBadgeVariant.Warning -> Triple(colors.warning.copy(alpha = 0.22f), colors.sidebarForeground, Color.Transparent)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(bg, shape)
            .then(if (border != Color.Transparent) Modifier.border(1.dp, border, shape) else Modifier)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = text,
            maxLines = 1,
            style = KiraTheme.typography.caption.copy(color = fg),
        )
    }
}

@Composable
fun KiraSidebarSearch(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search components…",
) {
    val state = LocalKiraSidebarState.current
    if (!state.expanded) return

    val colors = KiraTheme.colors
    val shape = KiraTheme.radius.shapeLg
    var focused by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = if (focused) colors.sidebarRing else colors.sidebarBorder,
        animationSpec = KiraMotion.fastTween(),
        label = "KiraSidebarSearchBorder",
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = KiraTheme.typography.bodySmall.copy(color = colors.sidebarForeground),
        cursorBrush = SolidColor(colors.sidebarPrimary),
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focused = it.isFocused }
            .semantics { contentDescription = placeholder },
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 40.dp)
                    .clip(shape)
                    .background(colors.background.copy(alpha = 0.65f), shape)
                    .border(BorderStroke(if (focused) 2.dp else 1.dp, borderColor), shape)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SearchGlyph(color = colors.mutedForeground, modifier = Modifier.size(15.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        BasicText(
                            text = placeholder,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground),
                        )
                    }
                    innerTextField()
                }
            }
        },
    )
}

@Composable
fun KiraSidebarTrigger(
    modifier: Modifier = Modifier,
    contentDescriptionText: String = "Toggle navigation sidebar",
) {
    val state = LocalKiraSidebarState.current
    val colors = KiraTheme.colors
    val shape = KiraTheme.radius.shapeLg
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bg by animateColorAsState(
        targetValue = if (isPressed) colors.accent else Color.Transparent,
        animationSpec = KiraMotion.fastTween(),
        label = "KiraSidebarTriggerBg",
    )

    Box(
        modifier = modifier
            .size(44.dp)
            .clip(shape)
            .background(bg, shape)
            .border(BorderStroke(1.dp, colors.border), shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = contentDescriptionText,
                onClick = { state.toggle() },
            )
            .semantics {
                contentDescription = contentDescriptionText
                stateDescription = when {
                    state.isMobile -> if (state.openMobile) "Open" else "Closed"
                    else -> if (state.collapsed) "Collapsed" else "Expanded"
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        SidebarGlyph(color = colors.foreground, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun KiraSidebarCloseButton(onClick: () -> Unit) {
    val colors = KiraTheme.colors
    val shape = KiraTheme.radius.shapeLg
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(shape)
            .clickable(
                role = Role.Button,
                onClickLabel = "Close navigation",
                onClick = onClick,
            )
            .semantics { contentDescription = "Close navigation" },
        contentAlignment = Alignment.Center,
    ) {
        CloseGlyph(color = colors.sidebarForeground, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun SidebarGlyph(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 1.8.dp.toPx()
        val inset = stroke / 2f
        val corner = 3.dp.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(inset, inset),
            size = Size(size.width - stroke, size.height - stroke),
            cornerRadius = CornerRadius(corner, corner),
            style = Stroke(width = stroke),
        )
        val dividerX = size.width * 0.36f
        drawLine(
            color = color,
            start = Offset(dividerX, inset),
            end = Offset(dividerX, size.height - inset),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun CloseGlyph(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 1.8.dp.toPx()
        val pad = size.width * 0.2f
        drawLine(color, Offset(pad, pad), Offset(size.width - pad, size.height - pad), stroke, StrokeCap.Round)
        drawLine(color, Offset(size.width - pad, pad), Offset(pad, size.height - pad), stroke, StrokeCap.Round)
    }
}

@Composable
private fun SearchGlyph(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 1.6.dp.toPx()
        val radius = size.minDimension * 0.34f
        val center = Offset(size.width * 0.42f, size.height * 0.42f)
        drawCircle(color = color, radius = radius, center = center, style = Stroke(width = stroke))
        drawLine(
            color = color,
            start = Offset(center.x + radius * 0.72f, center.y + radius * 0.72f),
            end = Offset(size.width * 0.88f, size.height * 0.88f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun ChevronGlyph(expanded: Boolean, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 1.6.dp.toPx()
        val left = size.width * 0.22f
        val right = size.width * 0.78f
        val midX = size.width * 0.5f
        val topY = size.height * 0.35f
        val botY = size.height * 0.65f
        if (expanded) {
            drawLine(color, Offset(left, topY), Offset(midX, botY), stroke, StrokeCap.Round)
            drawLine(color, Offset(midX, botY), Offset(right, topY), stroke, StrokeCap.Round)
        } else {
            drawLine(color, Offset(left, botY), Offset(midX, topY), stroke, StrokeCap.Round)
            drawLine(color, Offset(midX, topY), Offset(right, botY), stroke, StrokeCap.Round)
        }
    }
}
