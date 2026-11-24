package com.kirakit.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.kirakit.components.sidebar.KiraSidebarDivider
import com.kirakit.components.sidebar.KiraSidebarFooter
import com.kirakit.components.sidebar.KiraSidebarHeader
import com.kirakit.components.sidebar.KiraSidebarItem
import com.kirakit.components.sidebar.KiraSidebarLazyContent
import com.kirakit.components.sidebar.KiraSidebarScaffold
import com.kirakit.components.sidebar.KiraSidebarSearch
import com.kirakit.components.sidebar.KiraSidebarTrigger
import com.kirakit.components.sidebar.KiraSidebarVariant
import com.kirakit.components.sidebar.LocalKiraSidebarState
import com.kirakit.components.sidebar.rememberKiraSidebarState
import com.kirakit.components.toast.KiraToastHost
import com.kirakit.gallery.accordion.AccordionPlayground
import com.kirakit.gallery.alert.AlertPlayground
import com.kirakit.gallery.alertdialog.AlertDialogPlayground
import com.kirakit.gallery.avatar.AvatarPlayground
import com.kirakit.gallery.badge.badgePlaygroundItems
import com.kirakit.gallery.button.buttonPlaygroundItems
import com.kirakit.gallery.card.CardPlayground
import com.kirakit.gallery.checkbox.CheckboxPlayground
import com.kirakit.gallery.contextmenu.contextMenuPlaygroundItems
import com.kirakit.gallery.datepicker.datePickerPlaygroundItems
import com.kirakit.gallery.dialog.DialogPlayground
import com.kirakit.gallery.drawer.DrawerPlayground
import com.kirakit.gallery.dropdownmenu.dropdownMenuPlaygroundItems
import com.kirakit.gallery.input.inputPlaygroundItems
import com.kirakit.gallery.label.LabelPlayground
import com.kirakit.gallery.menubar.menubarPlaygroundItems
import com.kirakit.gallery.modal.ModalPlayground
import com.kirakit.gallery.navigationmenu.navigationMenuPlaygroundItems
import com.kirakit.gallery.popover.popoverPlaygroundItems
import com.kirakit.gallery.progress.progressPlaygroundItems
import com.kirakit.gallery.radiogroup.radioGroupPlaygroundItems
import com.kirakit.gallery.select.SelectPlayground
import com.kirakit.gallery.separator.separatorPlaygroundItems
import com.kirakit.gallery.sheet.sheetPlaygroundItems
import com.kirakit.gallery.sidebar.SidebarPlayground
import com.kirakit.gallery.skeleton.skeletonPlaygroundItems
import com.kirakit.gallery.slider.SliderPlayground
import com.kirakit.gallery.spotlight.spotlightPlaygroundItems
import com.kirakit.gallery.stepper.stepperPlaygroundItems
import com.kirakit.gallery.switch.SwitchPlayground
import com.kirakit.gallery.tabs.TabsPlayground
import com.kirakit.gallery.textarea.TextareaPlayground
import com.kirakit.gallery.timepicker.timePickerPlaygroundItems
import com.kirakit.gallery.toast.ToastPlayground
import com.kirakit.gallery.toggle.TogglePlayground
import com.kirakit.gallery.togglegroup.ToggleGroupPlayground
import com.kirakit.gallery.tokens.TokensAndRadiusPreview
import com.kirakit.gallery.tooltip.TooltipPlayground
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
    Accordion("Accordion", "Ac"),
    Alert("Alert", "Al"),
    AlertDialog("Alert Dialog", "AD"),
    Avatar("Avatar", "Av", "New"),
    Badge("Badge", "Bg"),
    Button("Button", "Bt"),
    Card("Card", "Cd"),
    Checkbox("Checkbox", "Cb"),
    ContextMenu("Context Menu", "CM", "New"),
    DatePicker("Date Picker", "DP"),
    Dialog("Dialog", "Dg"),
    Drawer("Drawer", "Dr"),
    DropdownMenu("Dropdown Menu", "DM"),
    Input("Input", "In"),
    Label("Label", "Lb"),
    Menubar("Menu Bar", "MB"),
    Modal("Modal", "Md"),
    NavigationMenu("Navigation Menu", "NM"),
    Popover("Popover", "Po", "New"),
    Progress("Progress", "Pr", "New"),
    RadioGroup("Radio Group", "RG", "New"),
    Select("Select", "Sl"),
    Separator("Separator", "Sp", "New"),
    Sheet("Sheet", "Sh", "New"),
    Sidebar("Sidebar", "Sb"),
    Skeleton("Skeleton", "Sk", "New"),
    Slider("Slider", "Sr"),
    Spotlight("Spotlight", "Sl", "New"),
    Stepper("Stepper", "St", "New"),
    Switch("Switch", "Sw"),
    Tabs("Tabs", "Tb"),
    Textarea("Textarea", "Ta", "New"),
    TimePicker("Time Picker", "TP", "New"),
    Toast("Toast", "Ts", "New"),
    Toggle("Toggle", "Tg", "New"),
    ToggleGroup("Toggle Group", "TG", "New"),
    Tooltip("Tooltip", "Tt"),
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
    var sidebarHoverExpand by rememberSaveable { mutableStateOf(false) }
    var showStudioSheet by rememberSaveable { mutableStateOf(false) }
    var componentsGroupOpen by rememberSaveable { mutableStateOf(true) }
    var themesGroupOpen by rememberSaveable { mutableStateOf(false) }
    var radiusGroupOpen by rememberSaveable { mutableStateOf(true) }

    val theme = remember(themeId) { KiraThemes.All.firstOrNull { it.id == themeId } ?: KiraThemes.Default }
    val radiusOverride = remember(radiusPresetId) { KiraRadiusPresets.All.firstOrNull { it.id == radiusPresetId }?.scale }
    val dark = darkOverride ?: isSystemInDarkTheme()
    val followsSystem = darkOverride == null

    val latestOnAppearanceChanged by rememberUpdatedState(onAppearanceChanged)
    LaunchedEffect(dark, followsSystem) {
        latestOnAppearanceChanged(dark, followsSystem)
    }

    KiraTheme(theme = theme, darkTheme = dark, radiusOverride = radiusOverride) {
        KiraToastHost {
        val colors = KiraTheme.colors
        val spacing = KiraTheme.spacing
        val haptics = rememberKiraHaptics()
        val sidebarState = rememberKiraSidebarState(
            collapsible = sidebarCollapsible,
            variant = sidebarVariant,
            hoverExpand = sidebarHoverExpand,
        )

        val filteredSections = remember(sidebarQuery) {
            val trimmed = sidebarQuery.trim()
            if (trimmed.isEmpty()) {
                GallerySection.entries
            } else {
                GallerySection.entries.filter { it.title.contains(trimmed, ignoreCase = true) }
            }
        }

        val mainListState = rememberLazyListState()
        LaunchedEffect(activeSection) {
            mainListState.scrollToItem(0)
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

                KiraSidebarLazyContent {
                    if (navState.expanded) {
                        item(key = "sidebar-search", contentType = "search") {
                            Box(Modifier.padding(bottom = spacing.sm)) {
                                KiraSidebarSearch(
                                    value = sidebarQuery,
                                    onValueChange = { sidebarQuery = it },
                                )
                            }
                        }
                        item(key = "group-components", contentType = "group-header") {
                            LazySidebarGroupHeader(
                                label = "Components",
                                open = componentsGroupOpen,
                                onToggle = { componentsGroupOpen = !componentsGroupOpen },
                            )
                        }
                    }

                    if (!navState.expanded || componentsGroupOpen) {
                        items(
                            items = filteredSections,
                            key = { "section-${it.name}" },
                            contentType = { "sidebar-item" },
                        ) { section ->
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
                        item(key = "divider-themes", contentType = "divider") {
                            KiraSidebarDivider(Modifier.padding(vertical = spacing.xs))
                        }

                        item(key = "group-themes", contentType = "group-header") {
                            LazySidebarGroupHeader(
                                label = "Theme (${KiraThemes.All.size})",
                                open = themesGroupOpen,
                                onToggle = { themesGroupOpen = !themesGroupOpen },
                            )
                        }

                        if (themesGroupOpen) {
                            items(
                                items = KiraThemes.All,
                                key = { "theme-${it.id}" },
                                contentType = { "sidebar-item" },
                            ) { preset ->
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

                        item(key = "group-radius", contentType = "group-header") {
                            LazySidebarGroupHeader(
                                label = "Radius",
                                open = radiusGroupOpen,
                                onToggle = { radiusGroupOpen = !radiusGroupOpen },
                                modifier = Modifier.padding(top = spacing.sm),
                            )
                        }

                        if (radiusGroupOpen) {
                            item(key = "radius-auto", contentType = "sidebar-item") {
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
                            }
                            items(
                                items = KiraRadiusPresets.All,
                                key = { "radius-${it.id}" },
                                contentType = { "sidebar-item" },
                            ) { preset ->
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

                // Main scrollable workspace (virtualized LazyColumn for smooth mobile scrolling & off-screen pausing)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    LazyColumn(
                        state = mainListState,
                        modifier = Modifier
                            .widthIn(max = 680.dp)
                            .fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(spacing.md),
                    ) {
                        if (showStudioSheet) {
                            item(key = "theme-studio-card", contentType = "studio") {
                                Box(Modifier.padding(bottom = spacing.sm)) {
                                    ThemeStudioCard(
                                        selectedThemeId = theme.id,
                                        selectedRadiusId = radiusPresetId,
                                        dark = dark,
                                        onSelectTheme = { themeId = it },
                                        onSelectRadius = { radiusPresetId = it },
                                        onToggleDark = { darkOverride = !dark },
                                    )
                                }
                            }
                        }

                        when (activeSection) {
                            GallerySection.Button -> buttonPlaygroundItems()
                            GallerySection.Accordion -> item(key = "sec-accordion") { AccordionPlayground() }
                            GallerySection.Alert -> item(key = "sec-alert") { AlertPlayground() }
                            GallerySection.AlertDialog -> item(key = "sec-alertdialog") { AlertDialogPlayground() }
                            GallerySection.Avatar -> item(key = "sec-avatar") { AvatarPlayground() }
                            GallerySection.Badge -> badgePlaygroundItems()
                            GallerySection.Card -> item(key = "sec-card") { CardPlayground() }
                            GallerySection.Checkbox -> item(key = "sec-checkbox") { CheckboxPlayground() }
                            GallerySection.ContextMenu -> contextMenuPlaygroundItems()
                            GallerySection.DatePicker -> datePickerPlaygroundItems()
                            GallerySection.Dialog -> item(key = "sec-dialog") { DialogPlayground() }
                            GallerySection.Drawer -> item(key = "sec-drawer") { DrawerPlayground() }
                            GallerySection.DropdownMenu -> dropdownMenuPlaygroundItems()
                            GallerySection.Input -> inputPlaygroundItems()
                            GallerySection.Label -> item(key = "sec-label") { LabelPlayground() }
                            GallerySection.Menubar -> menubarPlaygroundItems()
                            GallerySection.Modal -> item(key = "sec-modal") { ModalPlayground() }
                            GallerySection.NavigationMenu -> navigationMenuPlaygroundItems()
                            GallerySection.Popover -> popoverPlaygroundItems()
                            GallerySection.Progress -> progressPlaygroundItems()
                            GallerySection.RadioGroup -> radioGroupPlaygroundItems()
                            GallerySection.Select -> item(key = "sec-select") { SelectPlayground() }
                            GallerySection.Separator -> separatorPlaygroundItems()
                            GallerySection.Sheet -> sheetPlaygroundItems()
                            GallerySection.Sidebar -> item(key = "sec-sidebar") {
                                SidebarPlayground(
                                    variant = sidebarVariant,
                                    collapsible = sidebarCollapsible,
                                    hoverExpand = sidebarHoverExpand,
                                    onVariantChange = { sidebarVariant = it },
                                    onCollapsibleChange = { sidebarCollapsible = it },
                                    onHoverExpandChange = { sidebarHoverExpand = it },
                                    onOpenDrawer = { sidebarState.toggle() },
                                )
                            }
                            GallerySection.Skeleton -> skeletonPlaygroundItems()
                            GallerySection.Slider -> item(key = "sec-slider") { SliderPlayground() }
                            GallerySection.Spotlight -> spotlightPlaygroundItems()
                            GallerySection.Stepper -> stepperPlaygroundItems()
                            GallerySection.Switch -> item(key = "sec-switch") { SwitchPlayground() }
                            GallerySection.Tabs -> item(key = "sec-tabs") { TabsPlayground() }
                            GallerySection.Textarea -> item(key = "sec-textarea") { TextareaPlayground() }
                            GallerySection.TimePicker -> timePickerPlaygroundItems()
                            GallerySection.Toast -> item(key = "sec-toast") { ToastPlayground() }
                            GallerySection.Toggle -> item(key = "sec-toggle") { TogglePlayground() }
                            GallerySection.ToggleGroup -> item(key = "sec-togglegroup") { ToggleGroupPlayground() }
                            GallerySection.Tooltip -> item(key = "sec-tooltip") { TooltipPlayground() }
                            GallerySection.Tokens -> item(key = "sec-tokens") {
                                TokensAndRadiusPreview(
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
    }
}

private val GalleryRadiusChoices: List<Pair<String, String>> =
    listOf("auto" to "Auto") + KiraRadiusPresets.All.map { it.id to it.title }

@Composable
private fun LazySidebarGroupHeader(
    label: String,
    open: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KiraTheme.colors
    val spacing = KiraTheme.spacing
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(KiraTheme.radius.shapeMd)
            .clickable(role = Role.Button, onClick = onToggle)
            .padding(horizontal = spacing.sm, vertical = 6.dp)
            .semantics {
                heading()
                stateDescription = if (open) "Expanded" else "Collapsed"
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
        BasicText(
            text = if (open) "▾" else "▸",
            modifier = Modifier.clearAndSetSemantics {},
            style = KiraTheme.typography.caption.copy(color = colors.mutedForeground),
        )
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
            val selectedRadius = remember(selectedRadiusId) {
                GalleryRadiusChoices.firstOrNull { it.first == selectedRadiusId } ?: GalleryRadiusChoices.first()
            }
            Choices(
                options = GalleryRadiusChoices,
                selected = selectedRadius,
                label = { it.second },
                onSelect = { onSelectRadius(it.first) },
            )
            BasicText(
                text = "Color theme",
                style = KiraTheme.typography.caption.copy(color = colors.mutedForeground),
            )
            val selectedTheme = remember(selectedThemeId) {
                KiraThemes.All.firstOrNull { it.id == selectedThemeId } ?: KiraThemes.Default
            }
            Choices(
                options = KiraThemes.All,
                selected = selectedTheme,
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
