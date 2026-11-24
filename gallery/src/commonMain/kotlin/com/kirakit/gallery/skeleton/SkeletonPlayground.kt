package com.kirakit.gallery.skeleton

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.components.input.KiraInput
import com.kirakit.components.label.KiraLabel
import com.kirakit.components.skeleton.KiraSkeleton
import com.kirakit.components.skeleton.KiraSkeletonAvatar
import com.kirakit.components.skeleton.KiraSkeletonAvatarSize
import com.kirakit.components.skeleton.KiraSkeletonButton
import com.kirakit.components.skeleton.KiraSkeletonButtonSize
import com.kirakit.components.skeleton.KiraSkeletonCard
import com.kirakit.components.skeleton.KiraSkeletonContainer
import com.kirakit.components.skeleton.KiraSkeletonGroup
import com.kirakit.components.skeleton.KiraSkeletonHeading
import com.kirakit.components.skeleton.KiraSkeletonImage
import com.kirakit.components.skeleton.KiraSkeletonLines
import com.kirakit.components.skeleton.KiraSkeletonListItem
import com.kirakit.components.skeleton.KiraSkeletonPreset
import com.kirakit.components.skeleton.KiraSkeletonTableRow
import com.kirakit.components.skeleton.KiraSkeletonText
import com.kirakit.components.skeleton.KiraSkeletonVariant
import androidx.compose.foundation.lazy.LazyListScope
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.galleryItem
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography
import kotlinx.coroutines.delay

internal fun LazyListScope.skeletonPlaygroundItems() {
    galleryItem("skel-basic") { BasicSkeletonDemo() }
    galleryItem("skel-shapes") { ShapesSkeletonDemo() }
    galleryItem("skel-motion") { MotionSkeletonDemo() }
    galleryItem("skel-presets") { PresetsSkeletonDemo() }
    galleryItem("skel-delay") { DelaySkeletonDemo() }
    galleryItem("skel-min-display") { MinimumDisplaySkeletonDemo() }
    galleryItem("skel-refresh") { RefreshSkeletonDemo() }
    galleryItem("skel-kira") { KiraSkeletonDemo() }
    galleryItem("skel-states") { StatesSkeletonDemo() }
}

@Composable
internal fun SkeletonPlayground() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BasicSkeletonDemo()
        ShapesSkeletonDemo()
        MotionSkeletonDemo()
        PresetsSkeletonDemo()
        DelaySkeletonDemo()
        MinimumDisplaySkeletonDemo()
        RefreshSkeletonDemo()
        KiraSkeletonDemo()
        StatesSkeletonDemo()
    }
}

@Composable
private fun BasicSkeletonDemo() {
    GalleryExample("Basic") {
        KiraSkeletonGroup(loadingLabel = "Loading article preview") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                KiraSkeleton(Modifier.fillMaxWidth().height(16.dp))
                KiraSkeleton(Modifier.fillMaxWidth(0.75f).height(16.dp))
                KiraSkeleton(Modifier.fillMaxWidth(0.50f).height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShapesSkeletonDemo() {
    GalleryExample("Shapes (Avatars, Headings, Lines, Buttons & Images)") {
        KiraSkeletonGroup(loadingLabel = "Loading profile", variant = KiraSkeletonVariant.Shimmer) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    KiraSkeletonAvatarSize.entries.forEach { sz -> KiraSkeletonAvatar(size = sz) }
                }
                KiraSkeletonHeading()
                KiraSkeletonText()
                KiraSkeletonLines(count = 3, lastLineWidthFraction = 0.67f)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    KiraSkeletonButtonSize.entries.forEach { sz -> KiraSkeletonButton(size = sz, width = 86.dp) }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KiraSkeletonImage(aspectRatio = 16f / 9f, modifier = Modifier.weight(1f))
                    KiraSkeletonImage(aspectRatio = 4f / 3f, modifier = Modifier.weight(1f))
                    KiraSkeletonImage(aspectRatio = 1f, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MotionSkeletonDemo() {
    var paused by remember { mutableStateOf(false) }
    var animated by remember { mutableStateOf(true) }
    var variant by remember { mutableStateOf(KiraSkeletonVariant.Shimmer) }

    GalleryExample("Motion") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KiraButton(
                    text = if (paused) "Resume animation" else "Pause animation",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = { paused = !paused },
                )
                KiraButton(
                    text = "Motion ${if (animated) "on" else "off"}",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = { animated = !animated },
                )
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    KiraSkeletonVariant.Default to "default",
                    KiraSkeletonVariant.Shimmer to "shimmer",
                    KiraSkeletonVariant.Wave to "wave",
                ).forEach { (v, label) ->
                    KiraButton(
                        text = label,
                        size = KiraButtonSize.Sm,
                        variant = if (variant == v) KiraButtonVariant.Primary else KiraButtonVariant.Outline,
                        onClick = { variant = v },
                    )
                }
            }
            KiraSkeletonGroup(animated = animated, paused = paused, durationMillis = 1600, variant = variant) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    KiraSkeleton(Modifier.fillMaxWidth(0.75f).height(20.dp))
                    KiraSkeleton(Modifier.fillMaxWidth().height(16.dp))
                    KiraSkeleton(Modifier.fillMaxWidth(0.67f).height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun PresetsSkeletonDemo() {
    GalleryExample("Presets (Card, List & Table)") {
        KiraSkeletonGroup(loadingLabel = "Loading workspace") {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                KiraSkeletonCard(animated = false)
                KiraSkeletonListItem()
                KiraSkeletonListItem()
                KiraSkeletonTableRow(columns = 3)
                KiraSkeletonTableRow(columns = 3)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DelaySkeletonDemo() {
    val colors = KiraTheme.colors
    var loading by remember { mutableStateOf(false) }
    var duration by remember { mutableIntStateOf(0) }
    var ticket by remember { mutableIntStateOf(0) }

    LaunchedEffect(ticket) {
        if (ticket > 0 && duration > 0) {
            loading = true
            delay(duration.toLong())
            loading = false
        }
    }

    GalleryExample("Delay (Prevent Flash on Fast Loads)") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KiraButton(
                    text = "Fast request (50 ms)",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = {
                        duration = 50
                        ticket++
                    },
                )
                KiraButton(
                    text = "Slow request (800 ms)",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = {
                        duration = 800
                        ticket++
                    },
                )
            }
            KiraSkeletonContainer(
                loading = loading,
                delayMs = 150L,
                minHeight = 96.dp,
                loadingLabel = "Loading release summary",
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BasicText("Release summary", style = KiraTheme.typography.label.copy(color = colors.foreground, fontWeight = FontWeight.SemiBold))
                    ExampleNote("The staging build passed. One production approval remains.")
                }
            }
            ExampleNote("A short 50 ms request resolves before the 150 ms placeholder delay.")
        }
    }
}

@Composable
private fun MinimumDisplaySkeletonDemo() {
    var loading by remember { mutableStateOf(false) }
    var ticket by remember { mutableIntStateOf(0) }

    LaunchedEffect(ticket) {
        if (ticket > 0) {
            loading = true
            delay(80L)
            loading = false
        }
    }

    GalleryExample("Minimum Display (minDurationMs = 500)") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            KiraButton(
                text = "Load summary with a minimum display",
                variant = KiraButtonVariant.Outline,
                size = KiraButtonSize.Sm,
                onClick = { ticket++ },
            )
            KiraSkeletonContainer(
                loading = loading,
                delayMs = 0L,
                minDurationMs = 500L,
                minHeight = 84.dp,
            ) {
                BasicText("Summary ready.", style = KiraTheme.typography.bodySmall.copy(color = KiraTheme.colors.foreground))
            }
            ExampleNote("Keeps a visible placeholder for at least 500 ms once shown so content doesn't strobe.")
        }
    }
}

@Composable
private fun RefreshSkeletonDemo() {
    var loading by remember { mutableStateOf(false) }
    var ticket by remember { mutableIntStateOf(0) }
    var releaseNote by remember { mutableStateOf("Keep the rollout gradual.") }

    LaunchedEffect(ticket) {
        if (ticket > 0) {
            loading = true
            delay(900L)
            loading = false
        }
    }

    GalleryExample("Refresh (retainContent = true)") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            KiraButton(
                text = "Refresh release details",
                variant = KiraButtonVariant.Outline,
                size = KiraButtonSize.Sm,
                onClick = { ticket++ },
            )
            KiraSkeletonContainer(
                loading = loading,
                retainContent = true,
                loadedLabel = "Release details updated",
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    KiraLabel(text = "Your release note")
                    KiraInput(value = releaseNote, onValueChange = { releaseNote = it })
                    ExampleNote("Edit this draft, then refresh. The same input stays mounted.")
                }
            }
            ExampleNote(if (loading) "Refreshing release details…" else "Release details are current.")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KiraSkeletonDemo() {
    val colors = KiraTheme.colors
    var loading by remember { mutableStateOf(true) }
    var preset by remember { mutableStateOf(KiraSkeletonPreset.List) }

    GalleryExample("Kira (Preset Switcher)") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KiraButton(
                    text = if (loading) "Show content" else "Load again",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = { loading = !loading },
                )
                KiraSkeletonPreset.entries.forEach { item ->
                    KiraButton(
                        text = item.name.lowercase(),
                        size = KiraButtonSize.Sm,
                        variant = if (preset == item) KiraButtonVariant.Primary else KiraButtonVariant.Outline,
                        onClick = { preset = item },
                    )
                }
            }
            KiraSkeletonContainer(
                loading = loading,
                preset = preset,
                count = 3,
                delayMs = 0L,
                minHeight = 120.dp,
                loadingLabel = "Loading workspace preview",
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicText("Workspace ready", style = KiraTheme.typography.label.copy(color = colors.foreground, fontWeight = FontWeight.SemiBold))
                    ExampleNote("Your release checklist, review queue, and activity history are available.")
                    KiraButton(
                        text = "Reload preview",
                        variant = KiraButtonVariant.Outline,
                        size = KiraButtonSize.Sm,
                        onClick = { loading = true },
                    )
                }
            }
        }
    }
}

@Composable
private fun StatesSkeletonDemo() {
    val colors = KiraTheme.colors
    var failed by remember { mutableStateOf(true) }

    GalleryExample("States (Error Recovery, Static & Zero Counts)") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            KiraSkeletonContainer(
                loading = false,
                loadedLabel = if (failed) "Preview unavailable" else "Preview restored",
            ) {
                if (failed) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        BasicText(
                            "The preview could not be loaded. Your saved workspace is safe.",
                            style = KiraTheme.typography.bodySmall.copy(color = colors.foreground),
                        )
                        KiraButton(
                            text = "Try again",
                            variant = KiraButtonVariant.Outline,
                            size = KiraButtonSize.Sm,
                            onClick = { failed = false },
                        )
                    }
                } else {
                    BasicText("Preview restored. The workspace is ready.", style = KiraTheme.typography.bodySmall.copy(color = colors.foreground))
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BasicText(
                    "Internationalisation_Release_Candidate_Review_2026_10_31",
                    style = KiraTheme.typography.label.copy(color = colors.foreground, fontWeight = FontWeight.Medium),
                )
                KiraSkeletonLines(count = 4, animated = false)
                KiraSkeleton(Modifier.fillMaxWidth().height(48.dp), variant = KiraSkeletonVariant.Shimmer, animated = false)
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BasicText("Empty counts (0 lines / 0 columns)", style = KiraTheme.typography.label.copy(color = colors.foreground))
                KiraSkeletonLines(count = 0)
                KiraSkeletonTableRow(columns = 0)
                ExampleNote("Zero renders no bones safely without crashing.")
            }
        }
    }
}
