package com.kirakit.gallery.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.kirakit.components.popover.KiraPopover
import com.kirakit.components.popover.KiraPopoverSize
import com.kirakit.components.progress.KiraCircularProgress
import com.kirakit.components.progress.KiraProgress
import com.kirakit.components.progress.KiraProgressPhase
import com.kirakit.components.progress.KiraProgressSegment
import com.kirakit.components.progress.KiraProgressSize
import com.kirakit.components.progress.KiraProgressStatus
import com.kirakit.components.progress.KiraProgressTracker
import com.kirakit.components.progress.KiraProgressVariant
import com.kirakit.components.progress.KiraSegmentedProgress
import androidx.compose.foundation.lazy.LazyListScope
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.galleryItem
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography
import kotlinx.coroutines.delay

internal fun LazyListScope.progressPlaygroundItems() {
    galleryItem("prog-buffer") { BufferDemo() }
    galleryItem("prog-lifecycle") { LifecycleDemo() }
    galleryItem("prog-phases") { PhasesDemo() }
    galleryItem("prog-bg-tasks") { BackgroundTasksDemo() }
    galleryItem("prog-states") { StatesStressDemo() }
    galleryItem("prog-basic") { BasicProgressDemo() }
    galleryItem("prog-sizes") { SizesDemo() }
    galleryItem("prog-colors") { ColorsDemo() }
    galleryItem("prog-animated") { AnimatedProgressDemo() }
    galleryItem("prog-indeterminate") { IndeterminateDemo() }
    galleryItem("prog-striped") { StripedDemo() }
    galleryItem("prog-labels") { LabelsAndAccessibilityDemo() }
    galleryItem("prog-complete") { OnCompleteDemo() }
    galleryItem("prog-segmented") { SegmentedDemo() }
    galleryItem("prog-circular") { CircularDemo() }
    galleryItem("prog-loading") { LoadingExampleDemo() }
}

@Composable
internal fun ProgressPlayground() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BufferDemo()
        LifecycleDemo()
        PhasesDemo()
        BackgroundTasksDemo()
        StatesStressDemo()
        BasicProgressDemo()
        SizesDemo()
        ColorsDemo()
        AnimatedProgressDemo()
        IndeterminateDemo()
        StripedDemo()
        LabelsAndAccessibilityDemo()
        OnCompleteDemo()
        SegmentedDemo()
        CircularDemo()
        LoadingExampleDemo()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BufferDemo() {
    var processed by remember { mutableFloatStateOf(35f) }
    var received by remember { mutableFloatStateOf(70f) }

    GalleryExample("Buffer") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            KiraProgress(
                value = processed,
                bufferValue = received,
                showLabel = true,
                contentDescription = "Processed rows",
            )
            ExampleNote("Received ${received.toInt()} MB of 100 MB; processed ${processed.toInt()} of 100 rows.")
            ExampleNote("The faint layer is received data; the solid layer is processed data.")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KiraButton(
                    text = "Receive 10 MB",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = { received = (received + 10f).coerceAtMost(100f) },
                )
                KiraButton(
                    text = "Process 10 rows",
                    size = KiraButtonSize.Sm,
                    onClick = { processed = (processed + 10f).coerceAtMost(received) },
                )
                KiraButton(
                    text = "Reset buffer",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = {
                        received = 70f
                        processed = 35f
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LifecycleDemo() {
    var status by remember { mutableStateOf(KiraProgressStatus.Idle) }
    var value by remember { mutableStateOf<Float?>(null) }

    LaunchedEffect(status, value) {
        while (status == KiraProgressStatus.Running && value != null && (value ?: 0f) < 100f) {
            delay(800L)
            value = ((value ?: 0f) + 5f).coerceAtMost(100f)
        }
    }

    GalleryExample("Kira / Lifecycle") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ExampleNote("Illustrative import. Reaching 100% remains Running until the caller confirms completion.")
            KiraProgressTracker(
                label = "Import customer records",
                status = status,
                value = value,
                description = "Paused, failed, and canceled work keeps the measured amount.",
                onPause = { status = KiraProgressStatus.Paused },
                onResume = { status = KiraProgressStatus.Running },
                onCancel = { status = KiraProgressStatus.Canceled },
                onRetry = {
                    if (status == KiraProgressStatus.Canceled) value = null
                    status = KiraProgressStatus.Running
                },
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (status == KiraProgressStatus.Idle) {
                    KiraButton(text = "Start unknown amount", size = KiraButtonSize.Sm, onClick = { status = KiraProgressStatus.Running })
                }
                if (status == KiraProgressStatus.Running && value == null) {
                    KiraButton(text = "Measure amount", size = KiraButtonSize.Sm, onClick = { value = 20f })
                }
                if (status == KiraProgressStatus.Running) {
                    KiraButton(text = "Simulate error", variant = KiraButtonVariant.Outline, size = KiraButtonSize.Sm, onClick = { status = KiraProgressStatus.Error })
                }
                if (status == KiraProgressStatus.Running && value != null && (value ?: 0f) < 100f) {
                    KiraButton(text = "Measure 100%", variant = KiraButtonVariant.Outline, size = KiraButtonSize.Sm, onClick = { value = 100f })
                }
                if (status == KiraProgressStatus.Running && value == 100f) {
                    KiraButton(text = "Confirm completion", size = KiraButtonSize.Sm, onClick = { status = KiraProgressStatus.Complete })
                }
                if (status == KiraProgressStatus.Complete) {
                    KiraButton(
                        text = "Restart illustration",
                        variant = KiraButtonVariant.Outline,
                        size = KiraButtonSize.Sm,
                        onClick = {
                            status = KiraProgressStatus.Idle
                            value = null
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PhasesDemo() {
    var unknown by remember { mutableStateOf(false) }
    var transform by remember { mutableFloatStateOf(50f) }

    GalleryExample("Weighted Phases") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            KiraProgressTracker(
                label = "Prepare quarterly report",
                status = KiraProgressStatus.Running,
                currentPhaseId = "transform",
                description = "Weights 1:3:1. At 50% transform, overall progress is 50%.",
                phases = listOf(
                    KiraProgressPhase("receive", "Receive source data", weight = 1f, value = 100f),
                    KiraProgressPhase("transform", "Transform rows", weight = 3f, value = if (unknown) null else transform),
                    KiraProgressPhase("write", "Write report", weight = 1f, value = 0f),
                ),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KiraButton(
                    text = if (unknown) "Restore phase measurement" else "Make phase unknown",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = { unknown = !unknown },
                )
                KiraButton(
                    text = "Advance transform",
                    size = KiraButtonSize.Sm,
                    onClick = { transform = if (transform >= 100f) 0f else transform + 25f },
                )
            }
        }
    }
}

private data class BackgroundTaskItem(
    val id: String,
    val label: String,
    val value: Float,
    val status: KiraProgressStatus,
)

@Composable
private fun BackgroundTasksDemo() {
    val tasks = remember {
        mutableStateListOf(
            BackgroundTaskItem("export", "Export customer report", 20f, KiraProgressStatus.Running),
            BackgroundTaskItem("index", "Index project files", 40f, KiraProgressStatus.Running),
            BackgroundTaskItem("archive", "Archive previous release", 100f, KiraProgressStatus.Complete),
        )
    }
    var popoverOpen by remember { mutableStateOf(false) }

    val runningCount = tasks.count { it.status == KiraProgressStatus.Running }

    LaunchedEffect(runningCount) {
        while (tasks.any { it.status == KiraProgressStatus.Running }) {
            delay(800L)
            for (i in tasks.indices) {
                val t = tasks[i]
                if (t.status == KiraProgressStatus.Running) {
                    val next = (t.value + 5f).coerceAtMost(100f)
                    tasks[i] = t.copy(
                        value = next,
                        status = if (next >= 100f) KiraProgressStatus.Complete else KiraProgressStatus.Running,
                    )
                }
            }
        }
    }

    GalleryExample("Background Tasks (In Popover)") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ExampleNote("Background tasks keep running even while the popup is closed.")
            KiraPopover(
                open = popoverOpen,
                onOpenChange = { popoverOpen = it },
                title = "Background tasks",
                size = KiraPopoverSize.Lg,
                useBottomSheetOnCompact = true,
                trigger = {
                    KiraButton(
                        text = "$runningCount tasks running",
                        variant = KiraButtonVariant.Outline,
                        onClick = { popoverOpen = !popoverOpen },
                    )
                },
            ) {
                tasks.forEachIndexed { idx, task ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        KiraProgressTracker(
                            label = task.label,
                            status = task.status,
                            value = task.value,
                            onCancel = { tasks[idx] = task.copy(status = KiraProgressStatus.Canceled) },
                            onRetry = { tasks[idx] = task.copy(status = KiraProgressStatus.Running) },
                        )
                        if (task.status == KiraProgressStatus.Running) {
                            KiraButton(
                                text = "Simulate ${task.id} failure",
                                variant = KiraButtonVariant.Outline,
                                size = KiraButtonSize.Sm,
                                onClick = { tasks[idx] = task.copy(status = KiraProgressStatus.Error) },
                            )
                        }
                    }
                }
            }
            ExampleNote("$runningCount background tasks running.")
            tasks.filter { it.status == KiraProgressStatus.Complete }.forEach { done ->
                ExampleNote("${done.label}: result ready.")
            }
            KiraButton(
                text = "Restart tasks",
                variant = KiraButtonVariant.Outline,
                size = KiraButtonSize.Sm,
                onClick = {
                    for (i in tasks.indices) {
                        tasks[i] = tasks[i].copy(value = 0f, status = KiraProgressStatus.Running)
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatesStressDemo() {
    var value by remember { mutableFloatStateOf(150f) }
    var crossings by remember { mutableIntStateOf(0) }

    GalleryExample("States / Stress (Bounds, NaN, Reentry & Capped Segments)") {
        Column(
            modifier = Modifier.widthIn(max = 340.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ExampleNote("A very long release name that should wrap without obscuring the measured progress value (range 50..150)")
            KiraProgress(
                value = value,
                min = 50f,
                max = 150f,
                showLabel = true,
                onComplete = { crossings++ },
            )
            KiraCircularProgress(value = value, min = 50f, max = 150f, showLabel = true)
            ExampleNote("Completion crossings: $crossings")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KiraButton(text = "Below complete (75)", size = KiraButtonSize.Sm, onClick = { value = 75f })
                KiraButton(text = "Above max (200)", size = KiraButtonSize.Sm, onClick = { value = 200f })
                KiraButton(text = "Below min (0)", size = KiraButtonSize.Sm, onClick = { value = 0f })
            }
            ExampleNote("Invalid bounds (min=200, max=50) fall back to 0..100")
            KiraProgress(value = 50f, min = 200f, max = 50f, showLabel = true)
            ExampleNote("Non-finite (NaN) becomes unknown/indeterminate")
            KiraProgress(value = Float.NaN, showLabel = true)
            ExampleNote("Empty (0%)")
            KiraProgress(value = 0f, showLabel = true)
            ExampleNote("Capped segments (80 + 90 + -10 out of 100)")
            KiraSegmentedProgress(
                total = 100f,
                showLabels = true,
                segments = listOf(
                    KiraProgressSegment("processed", 80f, KiraProgressVariant.Default, "Processed"),
                    KiraProgressSegment("verified", 90f, KiraProgressVariant.Success, "Verified"),
                    KiraProgressSegment("invalid", -10f, KiraProgressVariant.Destructive, "Invalid"),
                ),
            )
            KiraProgressTracker(label = "Empty task — choose a file to begin", status = KiraProgressStatus.Idle, value = null)
        }
    }
}

@Composable
private fun BasicProgressDemo() {
    GalleryExample("Basic") {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            KiraProgress(value = 33f)
            KiraProgress(value = 66f)
            KiraProgress(value = 100f)
        }
    }
}

@Composable
private fun SizesDemo() {
    GalleryExample("Sizes") {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            listOf(
                KiraProgressSize.Sm to "Small (sm)",
                KiraProgressSize.Default to "Default",
                KiraProgressSize.Lg to "Large (lg)",
                KiraProgressSize.Xl to "Extra Large (xl)",
            ).forEach { (sz, label) ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ExampleNote(label)
                    KiraProgress(value = 60f, size = sz)
                }
            }
        }
    }
}

@Composable
private fun ColorsDemo() {
    GalleryExample("Colors") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            KiraProgressVariant.entries.forEach { tone ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ExampleNote(tone.name)
                    KiraProgress(value = 80f, variant = tone)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnimatedProgressDemo() {
    var progress by remember { mutableFloatStateOf(45f) }
    var playing by remember { mutableStateOf(false) }

    LaunchedEffect(playing, progress) {
        while (playing && progress < 100f) {
            delay(800L)
            progress = (progress + 5f).coerceAtMost(100f)
        }
    }

    GalleryExample("Animated") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            KiraProgress(value = progress, showLabel = true, contentDescription = "Illustrative animated progress")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KiraButton(
                    text = if (playing) "Pause illustration" else "Play illustration",
                    size = KiraButtonSize.Sm,
                    onClick = { playing = !playing },
                )
                KiraButton(
                    text = "Reset illustration",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = { progress = 0f },
                )
                KiraButton(
                    text = "Advance 5%",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = { progress = (progress + 5f).coerceAtMost(100f) },
                )
            }
        }
    }
}

@Composable
private fun IndeterminateDemo() {
    GalleryExample("Indeterminate") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ExampleNote("Pass value = null for an indeterminate progress bar.")
            KiraProgress(value = null, variant = KiraProgressVariant.Default)
            KiraProgress(value = null, variant = KiraProgressVariant.Info)
            KiraProgress(value = null, variant = KiraProgressVariant.Success)
        }
    }
}

@Composable
private fun StripedDemo() {
    GalleryExample("Striped") {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ExampleNote("Striped")
                KiraProgress(value = 70f, striped = true, size = KiraProgressSize.Lg)
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ExampleNote("Striped + Animated")
                KiraProgress(value = 70f, striped = true, animated = true, size = KiraProgressSize.Lg)
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ExampleNote("Striped + Animated (Success)")
                KiraProgress(value = 70f, striped = true, animated = true, size = KiraProgressSize.Lg, variant = KiraProgressVariant.Success)
            }
        }
    }
}

@Composable
private fun LabelsAndAccessibilityDemo() {
    val colors = KiraTheme.colors
    GalleryExample("Labels & Accessibility") {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ExampleNote("Built-in label")
                KiraProgress(value = 75f, showLabel = true)
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    BasicText("Upload Progress", style = KiraTheme.typography.label.copy(color = colors.foreground, fontWeight = FontWeight.Medium))
                    BasicText("65%", style = KiraTheme.typography.bodySmall.copy(color = colors.mutedForeground))
                }
                KiraProgress(value = 65f, contentDescription = "Upload Progress")
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ExampleNote("Custom semantic label: File upload progress (Step 3 of 5)")
                KiraProgress(value = 60f, showLabel = true, contentDescription = "File upload progress, Step 3 of 5")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OnCompleteDemo() {
    var progress by remember { mutableFloatStateOf(0f) }
    var completedCount by remember { mutableIntStateOf(0) }

    GalleryExample("onComplete") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KiraButton(text = "Advance 25%", size = KiraButtonSize.Sm, onClick = { progress = (progress + 25f).coerceAtMost(100f) })
                KiraButton(text = "Reset below complete", variant = KiraButtonVariant.Outline, size = KiraButtonSize.Sm, onClick = { progress = 0f })
            }
            KiraProgress(
                value = progress,
                showLabel = true,
                contentDescription = "Measured completion",
                onComplete = { completedCount++ },
            )
            ExampleNote("onComplete fired $completedCount times.")
        }
    }
}

@Composable
private fun SegmentedDemo() {
    GalleryExample("Segmented") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExampleNote("Task Status")
                KiraSegmentedProgress(
                    total = 100f,
                    size = KiraProgressSize.Lg,
                    showLabels = true,
                    segments = listOf(
                        KiraProgressSegment("completed", 45f, KiraProgressVariant.Success, "Completed"),
                        KiraProgressSegment("in-progress", 25f, KiraProgressVariant.Info, "In Progress"),
                        KiraProgressSegment("pending", 15f, KiraProgressVariant.Warning, "Pending"),
                    ),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExampleNote("Storage Usage")
                KiraSegmentedProgress(
                    total = 100f,
                    showLabels = true,
                    segments = listOf(
                        KiraProgressSegment("docs", 30f, KiraProgressVariant.Info, "Documents"),
                        KiraProgressSegment("photos", 25f, KiraProgressVariant.Success, "Photos"),
                        KiraProgressSegment("videos", 15f, KiraProgressVariant.Warning, "Videos"),
                        KiraProgressSegment("others", 10f, KiraProgressVariant.Destructive, "Others"),
                    ),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CircularDemo() {
    GalleryExample("Circular") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ExampleNote("Sizes")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    KiraProgressSize.Sm to "Small",
                    KiraProgressSize.Default to "Default",
                    KiraProgressSize.Lg to "Large",
                    KiraProgressSize.Xl to "XL",
                ).forEach { (sz, label) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        KiraCircularProgress(value = 60f, size = sz)
                        ExampleNote(label)
                    }
                }
            }
            ExampleNote("With Label")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                KiraCircularProgress(value = 75f, showLabel = true)
                KiraCircularProgress(value = 45f, variant = KiraProgressVariant.Success, showLabel = true)
                KiraCircularProgress(value = 90f, variant = KiraProgressVariant.Warning, showLabel = true)
            }
            ExampleNote("Indeterminate (value = null)")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                KiraCircularProgress(value = null)
                KiraCircularProgress(value = null, variant = KiraProgressVariant.Success)
                KiraCircularProgress(value = null, variant = KiraProgressVariant.Info)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LoadingExampleDemo() {
    var running by remember { mutableStateOf(false) }
    var amount by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(running, amount) {
        while (running && amount < 100f) {
            delay(800L)
            amount = (amount + 10f).coerceAtMost(100f)
        }
    }

    GalleryExample("Loading Example") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ExampleNote("Illustrative circular loading with pause/resume and manual step.")
            KiraCircularProgress(value = amount, showLabel = true, contentDescription = "Illustrative loading")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KiraButton(
                    text = "Start loading",
                    size = KiraButtonSize.Sm,
                    onClick = {
                        amount = 0f
                        running = true
                    },
                )
                KiraButton(
                    text = if (running) "Pause loading" else "Resume loading",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = { running = !running },
                )
                KiraButton(
                    text = "Advance 10%",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = { amount = (amount + 10f).coerceAtMost(100f) },
                )
            }
            if (amount >= 100f) {
                ExampleNote("Illustrative data ready.")
            }
        }
    }
}
