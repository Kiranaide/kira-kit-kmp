package com.kirakit.gallery.badge

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kirakit.components.badge.KiraBadge
import com.kirakit.components.badge.KiraBadgeCountFormat
import com.kirakit.components.badge.KiraBadgeDot
import com.kirakit.components.badge.KiraBadgeGroup
import com.kirakit.components.badge.KiraBadgeGroupGap
import com.kirakit.components.badge.KiraBadgeGroupOrientation
import com.kirakit.components.badge.KiraBadgeIcon
import com.kirakit.components.badge.KiraBadgeIconPosition
import com.kirakit.components.badge.KiraBadgeMode
import com.kirakit.components.badge.KiraBadgePreset
import com.kirakit.components.badge.KiraBadgePulse
import com.kirakit.components.badge.KiraBadgeSize
import com.kirakit.components.badge.KiraBadgeStatus
import com.kirakit.components.badge.KiraBadgeTrend
import com.kirakit.components.badge.KiraBadgeVariant
import com.kirakit.components.badge.KiraCountBadge
import com.kirakit.components.badge.KiraStatusBadge
import com.kirakit.components.badge.KiraTagBadge
import com.kirakit.components.badge.KiraTagInput
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonVariant
import androidx.compose.foundation.lazy.LazyListScope
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.SectionHeading
import com.kirakit.gallery.galleryHeading
import com.kirakit.gallery.galleryItem
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import com.kirakit.theme.typography
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal fun LazyListScope.badgePlaygroundItems() {
    galleryHeading("Variants & Sizes")
    galleryItem("badge-variants") { VariantsSection() }
    galleryItem("badge-sizes") { SizesSection() }

    galleryHeading("Icons, Dots & Interactive")
    galleryItem("badge-icons") { WithIconsSection() }
    galleryItem("badge-dots") { DotIndicatorSection() }
    galleryItem("badge-interactive") { InteractiveSection() }

    galleryHeading("Specialized Badges")
    galleryItem("badge-status") { StatusBadgeSection() }
    galleryItem("badge-count") { CountBadgeSection() }
    galleryItem("badge-tag") { TagBadgeSection() }
    galleryItem("badge-group") { BadgeGroupSection() }

    galleryHeading("Animation, Trends & Presets")
    galleryItem("badge-ticker") { NumberTickerSection() }
    galleryItem("badge-trends") { TrendsAndShimmerSection() }
    galleryItem("badge-presets") { PresetsSection() }

    galleryHeading("States, Tag Input & Unified API")
    galleryItem("badge-states") { StatesSection() }
    galleryItem("badge-tag-input") { TagInputSection() }
    galleryItem("badge-unified") { KiraBadgeModesSection() }
}

@Composable
internal fun BadgePlayground() {
    Column(verticalArrangement = Arrangement.spacedBy(KiraTheme.spacing.md)) {
        SectionHeading("Variants & Sizes")
        VariantsSection()
        SizesSection()

        SectionHeading("Icons, Dots & Interactive")
        WithIconsSection()
        DotIndicatorSection()
        InteractiveSection()

        SectionHeading("Specialized Badges")
        StatusBadgeSection()
        CountBadgeSection()
        TagBadgeSection()
        BadgeGroupSection()

        SectionHeading("Animation, Trends & Presets")
        NumberTickerSection()
        TrendsAndShimmerSection()
        PresetsSection()

        SectionHeading("States, Tag Input & Unified API")
        StatesSection()
        TagInputSection()
        KiraBadgeModesSection()
    }
}

@Composable
private fun VariantsSection() {
    GalleryExample("Badge Variants") {
        SubSection("Basic") {
            BadgeRow {
                KiraBadge("Default", variant = KiraBadgeVariant.Default)
                KiraBadge("Secondary", variant = KiraBadgeVariant.Secondary)
                KiraBadge("Outline", variant = KiraBadgeVariant.Outline)
                KiraBadge("Destructive", variant = KiraBadgeVariant.Destructive)
                KiraBadge("Ghost", variant = KiraBadgeVariant.Ghost)
                KiraBadge("Link", variant = KiraBadgeVariant.Link)
            }
        }
        SubSection("Semantic") {
            BadgeRow {
                KiraBadge("Success", variant = KiraBadgeVariant.Success)
                KiraBadge("Warning", variant = KiraBadgeVariant.Warning)
                KiraBadge("Info", variant = KiraBadgeVariant.Info)
            }
        }
        SubSection("Soft") {
            BadgeRow {
                KiraBadge("Primary Soft", variant = KiraBadgeVariant.PrimarySoft)
                KiraBadge("Secondary Soft", variant = KiraBadgeVariant.SecondarySoft)
                KiraBadge("Destructive Soft", variant = KiraBadgeVariant.DestructiveSoft)
                KiraBadge("Success Soft", variant = KiraBadgeVariant.SuccessSoft)
                KiraBadge("Warning Soft", variant = KiraBadgeVariant.WarningSoft)
                KiraBadge("Info Soft", variant = KiraBadgeVariant.InfoSoft)
            }
        }
    }
}

@Composable
private fun SizesSection() {
    GalleryExample("Size Variants") {
        BadgeRow {
            KiraBadge("Extra Small", size = KiraBadgeSize.Xs)
            KiraBadge("Small", size = KiraBadgeSize.Sm)
            KiraBadge("Default", size = KiraBadgeSize.Default)
            KiraBadge("Large", size = KiraBadgeSize.Lg)
            KiraBadge("Extra Large", size = KiraBadgeSize.Xl)
        }
    }
}

@Composable
private fun WithIconsSection() {
    GalleryExample("Badges with Icons") {
        BadgeRow {
            KiraBadge("Approved", variant = KiraBadgeVariant.Default, icon = BadgeDemoIcon.Check)
            KiraBadge("Rejected", variant = KiraBadgeVariant.Destructive, icon = BadgeDemoIcon.Cross)
            KiraBadge("Completed", variant = KiraBadgeVariant.Success, icon = BadgeDemoIcon.Check)
            KiraBadge("Warning", variant = KiraBadgeVariant.Warning, icon = BadgeDemoIcon.AlertCircle)
            KiraBadge("Information", variant = KiraBadgeVariant.Info, icon = BadgeDemoIcon.InfoCircle)
            KiraBadge(
                "Featured",
                variant = KiraBadgeVariant.Secondary,
                icon = BadgeDemoIcon.Star,
                iconPosition = KiraBadgeIconPosition.Right,
            )
        }
    }
}

@Composable
private fun DotIndicatorSection() {
    GalleryExample("Dot Indicators") {
        BadgeRow {
            KiraBadge("With Dot", dot = KiraBadgeDot.Default)
            KiraBadge("Success Dot", dot = KiraBadgeDot.Success)
            KiraBadge("Warning Dot", dot = KiraBadgeDot.Warning)
            KiraBadge("Error Dot", dot = KiraBadgeDot.Destructive)
            KiraBadge("Info Dot", dot = KiraBadgeDot.Info)
        }
        SubSection("With Pulse Animation") {
            BadgeRow {
                KiraBadge("Live", dot = KiraBadgeDot.Default, dotPulse = KiraBadgePulse.Continuous)
                KiraBadge("New", dot = KiraBadgeDot.Success, dotPulse = KiraBadgePulse.Once)
                KiraBadge("Processing", dot = KiraBadgeDot.Warning, dotPulse = KiraBadgePulse.Continuous)
            }
        }
    }
}

@Composable
private fun InteractiveSection() {
    var lastClicked by rememberSaveable { mutableStateOf<String?>(null) }
    var activeFilters by rememberSaveable { mutableStateOf(listOf("react")) }
    var dismissedRemovable by rememberSaveable { mutableStateOf(false) }
    var dismissedClosable by rememberSaveable { mutableStateOf(false) }

    GalleryExample("Interactive Badges") {
        SubSection("Clickable") {
            BadgeRow {
                KiraBadge(
                    "Premium",
                    variant = KiraBadgeVariant.PrimarySoft,
                    icon = BadgeDemoIcon.Star,
                    onClick = { lastClicked = "Premium clicked!" },
                )
                KiraBadge(
                    "Verified",
                    variant = KiraBadgeVariant.SuccessSoft,
                    icon = BadgeDemoIcon.Check,
                    onClick = { lastClicked = "Verified clicked!" },
                )
            }
            if (lastClicked != null) {
                ExampleNote(lastClicked!!)
            }
        }
        SubSection("Toggle chips") {
            BadgeRow {
                listOf("react", "vue", "svelte").forEach { framework ->
                    val isActive = framework in activeFilters
                    KiraBadge(
                        text = framework,
                        variant = if (isActive) KiraBadgeVariant.PrimarySoft else KiraBadgeVariant.Outline,
                        pressed = isActive,
                        onClick = {
                            activeFilters = if (isActive) {
                                activeFilters - framework
                            } else {
                                activeFilters + framework
                            }
                        },
                    )
                }
            }
            ExampleNote("Active: ${if (activeFilters.isEmpty()) "none" else activeFilters.joinToString(", ")}")
        }
        SubSection("Dismissible") {
            BadgeRow {
                if (!dismissedRemovable) {
                    KiraBadge(
                        "Removable",
                        variant = KiraBadgeVariant.SecondarySoft,
                        dismissible = true,
                        onClose = { dismissedRemovable = true },
                    )
                }
                if (!dismissedClosable) {
                    KiraBadge(
                        "Can be closed",
                        variant = KiraBadgeVariant.WarningSoft,
                        icon = BadgeDemoIcon.AlertCircle,
                        dismissible = true,
                        onClose = { dismissedClosable = true },
                    )
                }
                if (dismissedRemovable || dismissedClosable) {
                    KiraBadge(
                        "Restore",
                        variant = KiraBadgeVariant.Outline,
                        onClick = {
                            dismissedRemovable = false
                            dismissedClosable = false
                        },
                    )
                }
            }
        }
        SubSection("Loading") {
            BadgeRow {
                KiraBadge("Loading...", loading = true)
                KiraBadge("Saving...", variant = KiraBadgeVariant.Success, loading = true)
                KiraBadge("Deleting...", variant = KiraBadgeVariant.Destructive, loading = true)
            }
        }
    }
}

@Composable
private fun StatusBadgeSection() {
    GalleryExample("StatusBadge") {
        BadgeRow {
            KiraStatusBadge(KiraBadgeStatus.Success, label = "Online")
            KiraStatusBadge(KiraBadgeStatus.Warning, label = "Away")
            KiraStatusBadge(KiraBadgeStatus.Error, label = "Offline")
            KiraStatusBadge(KiraBadgeStatus.Info, label = "Busy")
            KiraStatusBadge(KiraBadgeStatus.Default, label = "Unknown")
        }
        BadgeRow {
            KiraStatusBadge(KiraBadgeStatus.Success, label = "Small", size = KiraBadgeSize.Sm)
            KiraStatusBadge(KiraBadgeStatus.Success, label = "Default", size = KiraBadgeSize.Default)
            KiraStatusBadge(KiraBadgeStatus.Success, label = "Large", size = KiraBadgeSize.Lg)
        }
        BadgeRow {
            KiraStatusBadge(KiraBadgeStatus.Success, label = "Live", pulse = KiraBadgePulse.Continuous)
            KiraStatusBadge(KiraBadgeStatus.Warning, label = "New", pulse = KiraBadgePulse.Once)
            KiraStatusBadge(KiraBadgeStatus.Error, label = "Error", pulse = KiraBadgePulse.Continuous)
        }
    }
}

@Composable
private fun CountBadgeSection() {
    GalleryExample("CountBadge") {
        BadgeRow {
            KiraCountBadge(5)
            KiraCountBadge(42)
            KiraCountBadge(150, max = 99)
            KiraCountBadge(3, variant = KiraBadgeVariant.Destructive)
            KiraCountBadge(12, variant = KiraBadgeVariant.Secondary)
        }
        SubSection("Short Format") {
            BadgeRow {
                KiraCountBadge(1_500, format = KiraBadgeCountFormat.Short)
                KiraCountBadge(25_000, format = KiraBadgeCountFormat.Short)
                KiraCountBadge(1_500_000, format = KiraBadgeCountFormat.Short)
            }
        }
        SubSection("With Icons") {
            BadgeRow {
                Box(Modifier.size(30.dp), contentAlignment = Alignment.Center) {
                    BadgeDemoIcon.Bell(KiraTheme.colors.foreground, 24.dp)
                    KiraCountBadge(
                        count = 3,
                        size = KiraBadgeSize.Xs,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp),
                    )
                }
                KiraCountBadge(5, icon = BadgeDemoIcon.Bell)
                KiraCountBadge(42, icon = BadgeDemoIcon.Bell, pulse = true)
            }
        }
    }
}

@Composable
private fun TagBadgeSection() {
    val initialOutline = listOf("React", "TypeScript", "Next.js", "Tailwind")
    val initialSoft = listOf("JavaScript", "Active", "Bug")
    var outlineTags by rememberSaveable { mutableStateOf(initialOutline) }
    var softTags by rememberSaveable { mutableStateOf(initialSoft) }

    GalleryExample("TagBadge") {
        BadgeRow(spacing = 8.dp) {
            outlineTags.forEach { tag ->
                KiraTagBadge(
                    label = tag,
                    variant = KiraBadgeVariant.Outline,
                    removable = true,
                    onRemove = { outlineTags = outlineTags - tag },
                )
            }
        }
        BadgeRow(spacing = 8.dp) {
            if ("JavaScript" in softTags) {
                KiraTagBadge(
                    label = "JavaScript",
                    variant = KiraBadgeVariant.PrimarySoft,
                    removable = true,
                    onRemove = { softTags = softTags - "JavaScript" },
                )
            }
            if ("Active" in softTags) {
                KiraTagBadge(
                    label = "Active",
                    variant = KiraBadgeVariant.SuccessSoft,
                    icon = BadgeDemoIcon.Check,
                    removable = true,
                    onRemove = { softTags = softTags - "Active" },
                )
            }
            if ("Bug" in softTags) {
                KiraTagBadge(
                    label = "Bug",
                    variant = KiraBadgeVariant.DestructiveSoft,
                    removable = true,
                    onRemove = { softTags = softTags - "Bug" },
                )
            }
            if (outlineTags.size < initialOutline.size || softTags.size < initialSoft.size) {
                KiraBadge(
                    "Reset tags",
                    variant = KiraBadgeVariant.Outline,
                    size = KiraBadgeSize.Sm,
                    onClick = {
                        outlineTags = initialOutline
                        softTags = initialSoft
                    },
                )
            }
        }
    }
}

@Composable
private fun BadgeGroupSection() {
    GalleryExample("BadgeGroup") {
        SubSection("Tight gap") {
            KiraBadgeGroup(gap = KiraBadgeGroupGap.Tight) {
                KiraBadge("Tag 1", variant = KiraBadgeVariant.Secondary)
                KiraBadge("Tag 2", variant = KiraBadgeVariant.Secondary)
                KiraBadge("Tag 3", variant = KiraBadgeVariant.Secondary)
            }
        }
        SubSection("Normal gap") {
            KiraBadgeGroup(gap = KiraBadgeGroupGap.Normal) {
                KiraBadge("Option 1", variant = KiraBadgeVariant.Outline)
                KiraBadge("Option 2", variant = KiraBadgeVariant.Outline)
                KiraBadge("Option 3", variant = KiraBadgeVariant.Outline)
            }
        }
        SubSection("Vertical orientation") {
            KiraBadgeGroup(
                orientation = KiraBadgeGroupOrientation.Vertical,
                gap = KiraBadgeGroupGap.Normal,
            ) {
                KiraBadge("Step 1", variant = KiraBadgeVariant.SuccessSoft)
                KiraBadge("Step 2", variant = KiraBadgeVariant.WarningSoft)
                KiraBadge("Step 3", variant = KiraBadgeVariant.DestructiveSoft)
            }
        }
    }
}

@Composable
private fun NumberTickerSection() {
    var count by rememberSaveable { mutableIntStateOf(3) }

    GalleryExample("Number Ticker") {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(30.dp), contentAlignment = Alignment.Center) {
                BadgeDemoIcon.Bell(KiraTheme.colors.mutedForeground, 24.dp)
                KiraCountBadge(
                    count = count,
                    animateCount = true,
                    size = KiraBadgeSize.Sm,
                    variant = KiraBadgeVariant.Destructive,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-8).dp),
                )
            }
            KiraCountBadge(count = count, animateCount = true)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KiraButton(
                "−3",
                size = KiraButtonSize.Sm,
                variant = KiraButtonVariant.Outline,
                onClick = { count = (count - 3).coerceAtLeast(0) },
            )
            KiraButton(
                "+3",
                size = KiraButtonSize.Sm,
                variant = KiraButtonVariant.Outline,
                onClick = { count += 3 },
            )
            KiraButton(
                "+25",
                size = KiraButtonSize.Sm,
                variant = KiraButtonVariant.Outline,
                onClick = { count += 25 },
            )
        }
        ExampleNote("The number rolls to its new value. Snaps immediately under reduced-motion.")
    }
}

@Composable
private fun TrendsAndShimmerSection() {
    GalleryExample("Trends & Shimmer") {
        SubSection("Trend arrows") {
            BadgeRow {
                KiraBadge("12.5%", variant = KiraBadgeVariant.SuccessSoft, trend = KiraBadgeTrend.Up)
                KiraBadge("3.2%", variant = KiraBadgeVariant.DestructiveSoft, trend = KiraBadgeTrend.Down)
                KiraBadge("Revenue", variant = KiraBadgeVariant.Secondary, trend = KiraBadgeTrend.Up)
            }
        }
        SubSection("Shimmer") {
            BadgeRow {
                KiraBadge("Featured", variant = KiraBadgeVariant.PrimarySoft, shimmer = true)
                KiraBadge("Pro", variant = KiraBadgeVariant.Success, shimmer = true, icon = BadgeDemoIcon.Star)
            }
        }
    }
}

@Composable
private fun PresetsSection() {
    GalleryExample("Preset Badges (Beta / Version)") {
        BadgeRow {
            KiraBadge(preset = KiraBadgePreset.Beta)
            KiraBadge(preset = KiraBadgePreset.New, shimmer = true)
            KiraBadge("v2.0.0", preset = KiraBadgePreset.Version)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    "Settings",
                    style = KiraTheme.typography.body.copy(color = KiraTheme.colors.foreground),
                )
                KiraBadge(preset = KiraBadgePreset.Beta, size = KiraBadgeSize.Xs)
            }
        }
    }
}

@Composable
private fun StatesSection() {
    var tickerCount by rememberSaveable { mutableIntStateOf(980) }
    var tickerFormat by rememberSaveable { mutableStateOf(KiraBadgeCountFormat.Default) }
    var dismissedDemo by rememberSaveable { mutableStateOf(false) }

    GalleryExample("Badge States") {
        BadgeRow {
            KiraBadge("Loading", loading = true)
            KiraBadge("Loading", loading = true, variant = KiraBadgeVariant.Outline)
            KiraBadge("Loading", loading = true, variant = KiraBadgeVariant.Ghost)
            KiraBadge("Disabled", enabled = false)
        }
        BadgeRow {
            KiraBadge(
                "Clickable (scales on press)",
                variant = KiraBadgeVariant.PrimarySoft,
                onClick = {},
            )
            if (!dismissedDemo) {
                KiraBadge(
                    "Dismissible",
                    variant = KiraBadgeVariant.Secondary,
                    dismissible = true,
                    onClose = { dismissedDemo = true },
                )
            } else {
                KiraBadge(
                    "Restore dismissible",
                    variant = KiraBadgeVariant.Outline,
                    onClick = { dismissedDemo = false },
                )
            }
        }
        BadgeRow {
            KiraStatusBadge(KiraBadgeStatus.Success)
            KiraStatusBadge(KiraBadgeStatus.Error)
            KiraStatusBadge(KiraBadgeStatus.Warning)
            KiraStatusBadge(KiraBadgeStatus.Info, label = "Children replace label")
        }
        BadgeRow {
            KiraBadge("Shimmer on solid", variant = KiraBadgeVariant.Default, shimmer = true)
            KiraBadge("Shimmer on soft", variant = KiraBadgeVariant.PrimarySoft, shimmer = true)
            KiraBadge("Shimmer on outline", variant = KiraBadgeVariant.Outline, shimmer = true)
        }
        BadgeRow {
            KiraBadge("XS", size = KiraBadgeSize.Xs)
            KiraBadge(
                "Very long badge label that must stay on one line without wrapping or breaking the pill",
                size = KiraBadgeSize.Xl,
            )
        }
        BadgeRow(spacing = 16.dp) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KiraCountBadge(count = tickerCount, max = 9999, animateCount = true)
                KiraButton(
                    "+137",
                    size = KiraButtonSize.Sm,
                    variant = KiraButtonVariant.Outline,
                    onClick = { tickerCount += 137 },
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KiraCountBadge(
                    count = 15_230,
                    max = 999_999,
                    animateCount = true,
                    format = tickerFormat,
                )
                KiraButton(
                    "Format: ${tickerFormat.name.lowercase()}",
                    size = KiraButtonSize.Sm,
                    variant = KiraButtonVariant.Outline,
                    onClick = {
                        tickerFormat = when (tickerFormat) {
                            KiraBadgeCountFormat.Default -> KiraBadgeCountFormat.Short
                            KiraBadgeCountFormat.Short -> KiraBadgeCountFormat.Commas
                            KiraBadgeCountFormat.Commas -> KiraBadgeCountFormat.Default
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun TagInputSection() {
    var tags by rememberSaveable { mutableStateOf(listOf("design", "frontend")) }
    var capped by rememberSaveable { mutableStateOf(emptyList<String>()) }

    GalleryExample("Tag Input") {
        Column(
            modifier = Modifier.widthIn(max = 448.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraTagInput(
                    tags = tags,
                    onTagsChange = { tags = it },
                    suggestions = listOf("design", "frontend", "a11y", "react", "motion"),
                    ariaLabel = "Project tags",
                )
                ExampleNote("Type to filter or create · Enter or comma commits · Backspace removes the last chip · Esc closes suggestions.")
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraTagInput(
                    tags = capped,
                    onTagsChange = { capped = it },
                    suggestions = listOf("one", "two", "three"),
                    maxTags = 3,
                    size = KiraBadgeSize.Sm,
                    placeholder = "Max 3 tags…",
                    ariaLabel = "Capped tags",
                )
                ExampleNote("maxTags=3 enforced — extra tags are ignored.")
            }
        }
    }
}

@Composable
private fun KiraBadgeModesSection() {
    GalleryExample("KiraBadge — Unified Mode API") {
        SubSection("One component, every mode") {
            BadgeRow {
                KiraBadge(
                    mode = KiraBadgeMode.Badge,
                    variant = KiraBadgeVariant.Success,
                    trend = KiraBadgeTrend.Up,
                    text = "Growth",
                )
                KiraBadge(
                    mode = KiraBadgeMode.Status,
                    status = KiraBadgeStatus.Success,
                )
                KiraBadge(
                    mode = KiraBadgeMode.Count,
                    count = 128,
                    animateCount = true,
                )
                KiraBadge(
                    mode = KiraBadgeMode.Dot,
                    variant = KiraBadgeVariant.Warning,
                    text = "Pending",
                )
                KiraBadge(
                    mode = KiraBadgeMode.Badge,
                    preset = KiraBadgePreset.New,
                    shimmer = true,
                )
            }
        }
    }
}

@Composable
private fun SubSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BasicText(
            text = title,
            style = KiraTheme.typography.bodySmall.copy(
                color = KiraTheme.colors.mutedForeground,
                fontWeight = FontWeight.Medium,
            ),
        )
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BadgeRow(
    spacing: Dp = 12.dp,
    content: @Composable () -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}

private object BadgeDemoIcon {
    val Check: KiraBadgeIcon = { color, size ->
        Canvas(Modifier.size(size).clearAndSetSemantics {}) {
            val stroke = Stroke(width = (this.size.minDimension * 0.16f).coerceAtLeast(1.3.dp.toPx()), cap = StrokeCap.Round, join = StrokeJoin.Round)
            val path = Path().apply {
                moveTo(this@Canvas.size.width * 0.18f, this@Canvas.size.height * 0.52f)
                lineTo(this@Canvas.size.width * 0.42f, this@Canvas.size.height * 0.76f)
                lineTo(this@Canvas.size.width * 0.84f, this@Canvas.size.height * 0.26f)
            }
            drawPath(path, color = color, style = stroke)
        }
    }

    val Cross: KiraBadgeIcon = { color, size ->
        Canvas(Modifier.size(size).clearAndSetSemantics {}) {
            val stroke = (this.size.minDimension * 0.16f).coerceAtLeast(1.3.dp.toPx())
            val inset = this.size.minDimension * 0.22f
            drawLine(color, Offset(inset, inset), Offset(this.size.width - inset, this.size.height - inset), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(color, Offset(this.size.width - inset, inset), Offset(inset, this.size.height - inset), strokeWidth = stroke, cap = StrokeCap.Round)
        }
    }

    val AlertCircle: KiraBadgeIcon = { color, size ->
        Canvas(Modifier.size(size).clearAndSetSemantics {}) {
            val strokeWidth = (this.size.minDimension * 0.13f).coerceAtLeast(1.25.dp.toPx())
            drawCircle(color, radius = this.size.minDimension * 0.42f, style = Stroke(width = strokeWidth))
            drawLine(
                color,
                Offset(this.size.width * 0.5f, this.size.height * 0.28f),
                Offset(this.size.width * 0.5f, this.size.height * 0.54f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            drawCircle(color, radius = strokeWidth * 0.65f, center = Offset(this.size.width * 0.5f, this.size.height * 0.70f))
        }
    }

    val InfoCircle: KiraBadgeIcon = { color, size ->
        Canvas(Modifier.size(size).clearAndSetSemantics {}) {
            val strokeWidth = (this.size.minDimension * 0.13f).coerceAtLeast(1.25.dp.toPx())
            drawCircle(color, radius = this.size.minDimension * 0.42f, style = Stroke(width = strokeWidth))
            drawCircle(color, radius = strokeWidth * 0.65f, center = Offset(this.size.width * 0.5f, this.size.height * 0.32f))
            drawLine(
                color,
                Offset(this.size.width * 0.5f, this.size.height * 0.46f),
                Offset(this.size.width * 0.5f, this.size.height * 0.72f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }

    val Star: KiraBadgeIcon = { color, size ->
        Canvas(Modifier.size(size).clearAndSetSemantics {}) {
            val stroke = Stroke(width = (this.size.minDimension * 0.13f).coerceAtLeast(1.2.dp.toPx()), join = StrokeJoin.Round)
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val outer = this.size.minDimension * 0.44f
            val inner = outer * 0.46f
            val path = Path()
            for (i in 0 until 10) {
                val angle = (-90.0 + i * 36.0) * PI / 180.0
                val r = if (i % 2 == 0) outer else inner
                val x = cx + (r * cos(angle)).toFloat()
                val y = cy + (r * sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, color = color, style = stroke)
        }
    }

    val Bell: KiraBadgeIcon = { color, size ->
        Canvas(Modifier.size(size).clearAndSetSemantics {}) {
            val w = this.size.width
            val h = this.size.height
            val strokeWidth = (this.size.minDimension * 0.11f).coerceAtLeast(1.3.dp.toPx())
            val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            val dome = Path().apply {
                moveTo(w * 0.22f, h * 0.70f)
                lineTo(w * 0.28f, h * 0.44f)
                cubicTo(w * 0.28f, h * 0.20f, w * 0.72f, h * 0.20f, w * 0.72f, h * 0.44f)
                lineTo(w * 0.78f, h * 0.70f)
                close()
            }
            drawPath(dome, color = color, style = stroke)
            drawLine(color, Offset(w * 0.16f, h * 0.70f), Offset(w * 0.84f, h * 0.70f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawCircle(color, radius = strokeWidth * 0.85f, center = Offset(w * 0.50f, h * 0.82f))
        }
    }
}
