package com.kirakit.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonTooltipHost
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.components.card.KiraCard
import com.kirakit.components.platform.KiraHapticFeedbackType
import com.kirakit.components.platform.rememberKiraHaptics
import com.kirakit.components.sidebar.KiraSidebarBadge
import com.kirakit.components.sidebar.KiraSidebarBadgeVariant
import com.kirakit.components.sidebar.KiraSidebarCollapsible
import com.kirakit.components.sidebar.KiraSidebarContent
import com.kirakit.components.sidebar.KiraSidebarDivider
import com.kirakit.components.sidebar.KiraSidebarFooter
import com.kirakit.components.sidebar.KiraSidebarGroup
import com.kirakit.components.sidebar.KiraSidebarHeader
import com.kirakit.components.sidebar.KiraSidebarItem
import com.kirakit.components.sidebar.KiraSidebarScaffold
import com.kirakit.components.sidebar.KiraSidebarSearch
import com.kirakit.components.sidebar.KiraSidebarTrigger
import com.kirakit.components.sidebar.KiraSidebarVariant
import com.kirakit.components.sidebar.LocalKiraSidebarState
import com.kirakit.components.sidebar.rememberKiraSidebarState
import com.kirakit.gallery.badge.BadgePlayground
import com.kirakit.gallery.button.ButtonExamples
import com.kirakit.gallery.card.CardPlayground
import com.kirakit.gallery.input.InputPlayground
import com.kirakit.gallery.sidebar.SidebarPlayground
import com.kirakit.gallery.tokens.TokensAndRadiusPreview
import com.kirakit.theme.KiraRadiusPresets
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.KiraThemes
import com.kirakit.theme.spacing
import com.kirakit.theme.typography

private enum class GallerySection(
    val title: String,
    val monogram: String,
    val badge: String? = null,
) {
    Button("Button", "Bt"),
    Input("Input", "In"),
    Badge("Badge", "Bg"),
    Card("Card", "Cd"),
    Sidebar("Sidebar", "Sb", "New"),
    Tokens("Tokens & Radius", "Tk"),
}

@Composable
fun GalleryApp(onAppearanceChanged: (dark: Boolean, followsSystem: Boolean) -> Unit = { _, _ -> }) =
    KiraButtonTooltipHost { GalleryContent(onAppearanceChanged) }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GalleryContent(onAppearanceChanged: (dark: Boolean, followsSystem: Boolean) -> Unit) {
    var themeId by rememberSaveable { mutableStateOf(KiraThemes.Default.id) }
    var radiusPresetId by rememberSaveable { mutableStateOf("auto") }
    var darkOverride by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var activeSection by rememberSaveable { mutableStateOf(GallerySection.Button) }
    var sidebarQuery by rememberSaveable { mutableStateOf("") }
    var sidebarVariant by rememberSaveable { mutableStateOf(KiraSidebarVariant.Default) }
    var sidebarCollapsible by rememberSaveable { mutableStateOf(KiraSidebarCollapsible.Icon) }
    var showStudioSheet by rememberSaveable { mutableStateOf(false) }

    val theme = KiraThemes.All.firstOrNull { it.id == themeId } ?: KiraThemes.Default
    val radiusOverride = KiraRadiusPresets.All.firstOrNull { it.id == radiusPresetId }?.scale
    val dark = darkOverride ?: isSystemInDarkTheme()

    SideEffect { onAppearanceChanged(dark, darkOverride == null) }

    KiraTheme(theme = theme, darkTheme = dark, radiusOverride = radiusOverride) {
        val colors = KiraTheme.colors
        val spacing = KiraTheme.spacing
        val haptics = rememberKiraHaptics()
        val sidebarState = rememberKiraSidebarState(
            collapsible = sidebarCollapsible,
            variant = sidebarVariant,
        )

        val filteredSections = GallerySection.entries.filter {
            sidebarQuery.isBlank() || it.title.contains(sidebarQuery.trim(), ignoreCase = true)
        }

        KiraSidebarScaffold(
            state = sidebarState,
            sidebar = {
                val navState = LocalKiraSidebarState.current

                KiraSidebarHeader {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(KiraTheme.radius.shapeLg)
                                .background(colors.sidebarPrimary),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "K",
                                modifier = Modifier.clearAndSetSemantics {},
                                style = KiraTheme.typography.label.copy(
                                    color = colors.sidebarPrimaryForeground,
                                    fontWeight = FontWeight.Bold,
                                ),
                            )
                        }
                        if (navState.expanded) {
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = "Kira Kit",
                                    style = KiraTheme.typography.heading3.copy(color = colors.sidebarForeground),
                                )
                                BasicText(
                                    text = "${theme.title} · ${radiusPresetId.replaceFirstChar { it.uppercase() }}",
                                    style = KiraTheme.typography.caption.copy(color = colors.mutedForeground),
                                )
                            }
                            KiraSidebarBadge(text = "KMP", variant = KiraSidebarBadgeVariant.Outline)
                        }
                    }
                }

                KiraSidebarContent {
                    KiraSidebarSearch(
                        value = sidebarQuery,
                        onValueChange = { sidebarQuery = it },
                    )

                    KiraSidebarGroup(label = "Components") {
                        filteredSections.forEach { section ->
                            KiraSidebarItem(
                                label = section.title,
                                leadingMonogram = section.monogram,
                                badge = section.badge,
                                badgeVariant = if (section == GallerySection.Sidebar) {
                                    KiraSidebarBadgeVariant.Primary
                                } else {
                                    KiraSidebarBadgeVariant.Default
                                },
                                selected = activeSection == section,
                                onClick = {
                                    haptics.perform(KiraHapticFeedbackType.Selection)
                                    activeSection = section
                                },
                            )
                        }
                    }

                    if (navState.expanded) {
                        KiraSidebarDivider()

                        KiraSidebarGroup(label = "Theme (${KiraThemes.All.size})", defaultOpen = false) {
                            KiraThemes.All.forEach { preset ->
                                KiraSidebarItem(
                                    label = preset.title,
                                    leadingMonogram = preset.title.take(2).uppercase(),
                                    selected = preset.id == theme.id,
                                    closeMobileOnClick = false,
                                    onClick = {
                                        haptics.perform(KiraHapticFeedbackType.Light)
                                        themeId = preset.id
                                    },
                                )
                            }
                        }

                        KiraSidebarGroup(label = "Radius", defaultOpen = true) {
                            KiraSidebarItem(
                                label = "Auto (${theme.radius.lg.value.toInt()}dp)",
                                leadingMonogram = "Au",
                                selected = radiusPresetId == "auto",
                                closeMobileOnClick = false,
                                onClick = {
                                    haptics.perform(KiraHapticFeedbackType.Light)
                                    radiusPresetId = "auto"
                                },
                            )
                            KiraRadiusPresets.All.forEach { preset ->
                                KiraSidebarItem(
                                    label = preset.title,
                                    leadingMonogram = preset.title.take(2).uppercase(),
                                    badge = "${preset.scale.lg.value.toInt()}dp",
                                    selected = radiusPresetId == preset.id,
                                    closeMobileOnClick = false,
                                    onClick = {
                                        haptics.perform(KiraHapticFeedbackType.Light)
                                        radiusPresetId = preset.id
                                    },
                                )
                            }
                        }
                    }
                }

                KiraSidebarFooter {
                    if (navState.expanded) {
                        KiraButton(
                            text = if (dark) "Switch to Light Mode" else "Switch to Dark Mode",
                            variant = KiraButtonVariant.Outline,
                            size = KiraButtonSize.Sm,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                haptics.perform(KiraHapticFeedbackType.Selection)
                                darkOverride = !dark
                            },
                        )
                    } else {
                        KiraButton(
                            text = if (dark) "Lt" else "Dk",
                            variant = KiraButtonVariant.Outline,
                            size = KiraButtonSize.Sm,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                haptics.perform(KiraHapticFeedbackType.Selection)
                                darkOverride = !dark
                            },
                        )
                    }
                }
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
                    .safeDrawingPadding()
                    .imePadding(),
            ) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.lg, vertical = spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    KiraSidebarTrigger()
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = activeSection.title,
                            style = KiraTheme.typography.heading3.copy(color = colors.foreground),
                        )
                        BasicText(
                            text = "${theme.title} · lg ${KiraTheme.radius.lg.value.toInt()}dp / xl ${KiraTheme.radius.xl.value.toInt()}dp",
                            style = KiraTheme.typography.caption.copy(color = colors.mutedForeground),
                        )
                    }
                    KiraButton(
                        text = if (showStudioSheet) "Hide Studio" else "Theme",
                        size = KiraButtonSize.Sm,
                        variant = KiraButtonVariant.Outline,
                        onClick = { showStudioSheet = !showStudioSheet },
                    )
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))

                // Main scrollable workspace
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 680.dp)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(spacing.xl),
                    ) {
                        if (showStudioSheet) {
                            ThemeStudioCard(
                                selectedThemeId = theme.id,
                                selectedRadiusId = radiusPresetId,
                                dark = dark,
                                onSelectTheme = { themeId = it },
                                onSelectRadius = { radiusPresetId = it },
                                onToggleDark = { darkOverride = !dark },
                            )
                        }

                        when (activeSection) {
                            GallerySection.Button -> ButtonExamples()
                            GallerySection.Input -> InputPlayground()
                            GallerySection.Badge -> BadgePlayground()
                            GallerySection.Card -> CardPlayground()
                            GallerySection.Sidebar -> SidebarPlayground(
                                variant = sidebarVariant,
                                collapsible = sidebarCollapsible,
                                onVariantChange = { sidebarVariant = it },
                                onCollapsibleChange = { sidebarCollapsible = it },
                                onOpenDrawer = { sidebarState.toggle() },
                            )
                            GallerySection.Tokens -> TokensAndRadiusPreview(
                                selectedRadiusId = radiusPresetId,
                                onSelectRadius = { radiusPresetId = it },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ThemeStudioCard(
    selectedThemeId: String,
    selectedRadiusId: String,
    dark: Boolean,
    onSelectTheme: (String) -> Unit,
    onSelectRadius: (String) -> Unit,
    onToggleDark: () -> Unit,
) {
    val spacing = KiraTheme.spacing
    val colors = KiraTheme.colors
    KiraCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionHeading("Theme & Radius Studio")
                KiraButton(
                    text = if (dark) "Light" else "Dark",
                    size = KiraButtonSize.Sm,
                    variant = KiraButtonVariant.Ghost,
                    onClick = onToggleDark,
                )
            }
            BasicText(
                text = "Radius preset (matches kira-kit-v2: Button/Input = lg, Card = xl, Badge = full)",
                style = KiraTheme.typography.caption.copy(color = colors.mutedForeground),
            )
            val radiusChoices = listOf("auto" to "Auto") + KiraRadiusPresets.All.map { it.id to it.title }
            Choices(
                options = radiusChoices,
                selected = radiusChoices.first { it.first == selectedRadiusId },
                label = { it.second },
                onSelect = { onSelectRadius(it.first) },
            )
            BasicText(
                text = "Color theme",
                style = KiraTheme.typography.caption.copy(color = colors.mutedForeground),
            )
            Choices(
                options = KiraThemes.All,
                selected = KiraThemes.All.first { it.id == selectedThemeId },
                label = { it.title },
                onSelect = { onSelectTheme(it.id) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun <T> Choices(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    role: Role = Role.RadioButton,
    onSelect: (T) -> Unit,
) {
    val colors = KiraTheme.colors
    val spacing = KiraTheme.spacing
    val shape = KiraTheme.radius.shapeLg
    FlowRow(
        modifier = Modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        options.forEach { option ->
            val active = option == selected
            Box(
                modifier = Modifier
                    .clip(shape)
                    .background(if (active) colors.secondary else colors.card)
                    .border(1.dp, if (active) colors.primary else colors.border, shape)
                    .selectable(selected = active, role = role, onClick = { onSelect(option) })
                    .heightIn(min = 44.dp)
                    .padding(horizontal = spacing.md, vertical = spacing.sm),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    label(option),
                    style = KiraTheme.typography.label.copy(
                        color = if (active) colors.secondaryForeground else colors.cardForeground,
                    ),
                )
            }
        }
    }
}

@Composable
internal fun SectionHeading(title: String) {
    BasicText(
        text = title,
        modifier = Modifier.semantics { heading() },
        style = KiraTheme.typography.heading2.copy(color = KiraTheme.colors.foreground),
    )
}
