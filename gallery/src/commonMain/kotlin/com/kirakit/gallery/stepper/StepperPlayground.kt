package com.kirakit.gallery.stepper

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.components.input.KiraInput
import com.kirakit.components.label.KiraLabel
import com.kirakit.components.stepper.KiraStepItem
import com.kirakit.components.stepper.KiraStepper
import com.kirakit.components.stepper.KiraStepperOrientation
import androidx.compose.foundation.lazy.LazyListScope
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.galleryItem
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.typography

internal fun LazyListScope.stepperPlaygroundItems() {
    galleryItem("step-basic") { BasicStepperDemo() }
    galleryItem("step-kira") { KiraStepperDemo() }
    galleryItem("step-vertical") { VerticalStepperDemo() }
    galleryItem("step-validation") { ValidationStepperDemo() }
    galleryItem("step-states") { StatesStepperDemo() }
    galleryItem("step-mobile") { MobileStepperDemo() }
}

@Composable
internal fun StepperPlayground() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BasicStepperDemo()
        KiraStepperDemo()
        VerticalStepperDemo()
        ValidationStepperDemo()
        StatesStepperDemo()
        MobileStepperDemo()
    }
}

@Composable
private fun BasicStepperDemo() {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val steps = remember {
        listOf(
            KiraStepItem("account", "Account", "Create your account"),
            KiraStepItem("profile", "Profile", "Set up your profile"),
            KiraStepItem("security", "Security", "Configure security"),
            KiraStepItem("complete", "Complete", "You're all set!"),
        )
    }
    var step by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    GalleryExample("Basic") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            KiraStepper(
                steps = steps,
                activeStep = step,
                onStepChange = {
                    step = it
                    finished = false
                },
                completedSteps = if (finished) setOf(0, 1, 2, 3) else emptySet(),
                showNavigation = !finished,
                onFinish = { finished = true },
                stepContent = { idx ->
                    val current = steps[idx]
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(radius.shapeLg)
                            .background(colors.muted.copy(alpha = 0.25f))
                            .border(1.dp, colors.border, radius.shapeLg)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        ExampleNote(if (finished) "Setup complete" else "Step ${idx + 1} of 4")
                        BasicText(
                            text = if (finished) "Your account is ready" else current.title,
                            style = KiraTheme.typography.heading3.copy(color = colors.foreground, fontWeight = FontWeight.SemiBold),
                        )
                        ExampleNote(
                            if (finished) "All four steps are complete. You can revisit a step or start again."
                            else current.description.orEmpty(),
                        )
                    }
                },
            )
            if (finished) {
                KiraButton(
                    text = "Start again",
                    variant = KiraButtonVariant.Outline,
                    size = KiraButtonSize.Sm,
                    onClick = {
                        step = 0
                        finished = false
                    },
                )
            }
        }
    }
}

@Composable
private fun KiraStepperDemo() {
    var step by remember { mutableIntStateOf(0) }
    val steps = remember {
        listOf(
            KiraStepItem("account", "Account", "Create your account", icon = { c -> StepUserGlyph(c) }),
            KiraStepItem("profile", "Profile", "Set up your profile", icon = { c -> StepMailGlyph(c) }),
            KiraStepItem("billing", "Billing", "Payment details", icon = { c -> StepLockGlyph(c) }),
        )
    }

    GalleryExample("Kira (Icons + Progress + Navigation)") {
        KiraStepper(
            steps = steps,
            activeStep = step,
            onStepChange = { step = it },
            showProgress = true,
            showNavigation = true,
        )
    }
}

@Composable
private fun VerticalStepperDemo() {
    var step by remember { mutableIntStateOf(1) }
    val steps = remember {
        listOf(
            KiraStepItem("account", "Account", "Create your account"),
            KiraStepItem("profile", "Profile", "Set up your profile"),
            KiraStepItem("security", "Security", "Configure security"),
            KiraStepItem("complete", "Complete", "You're all set!"),
        )
    }

    GalleryExample("Vertical") {
        KiraStepper(
            steps = steps,
            activeStep = step,
            onStepChange = { step = it },
            orientation = KiraStepperOrientation.Vertical,
        )
    }
}

@Composable
private fun ValidationStepperDemo() {
    val colors = KiraTheme.colors
    val radius = KiraTheme.radius
    val steps = remember {
        listOf(
            KiraStepItem("account", "Account", "Create your account"),
            KiraStepItem("profile", "Profile", "Set up your profile"),
            KiraStepItem("security", "Security", "Configure security"),
            KiraStepItem("complete", "Complete", "You're all set!"),
        )
    }
    var step by remember { mutableIntStateOf(0) }
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var completed by remember { mutableStateOf(emptySet<Int>()) }
    var finished by remember { mutableStateOf(false) }

    GalleryExample("Validation (Linear Gate on Step Exit)") {
        KiraStepper(
            steps = steps,
            activeStep = step,
            onStepChange = { step = it },
            linear = true,
            completedSteps = if (finished) setOf(0, 1, 2, 3) else completed,
            errorSteps = if (emailError != null) setOf(0) else emptySet(),
            showNavigation = true,
            nextDisabled = finished,
            finishLabel = if (finished) "Account created" else "Create account",
            onNext = {
                if (step == 0 && !email.contains("@")) {
                    emailError = "Enter a valid email address to continue"
                    false
                } else {
                    emailError = null
                    completed = completed + step
                    true
                }
            },
            onFinish = { finished = true },
            stepContent = { idx ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(radius.shapeLg)
                        .background(colors.muted.copy(alpha = 0.30f))
                        .border(1.dp, colors.border, radius.shapeLg)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    when (idx) {
                        0 -> {
                            KiraLabel(text = "Work email")
                            KiraInput(
                                value = email,
                                onValueChange = {
                                    email = it
                                    emailError = null
                                },
                                placeholder = "you@company.com",
                                isError = emailError != null,
                            )
                            if (emailError != null) {
                                BasicText(
                                    text = emailError.orEmpty(),
                                    style = KiraTheme.typography.bodySmall.copy(color = colors.destructive),
                                )
                            }
                        }
                        1 -> ExampleNote("Profile details go here.")
                        2 -> ExampleNote("Security settings go here.")
                        else -> ExampleNote("All done — review and finish.")
                    }
                }
            },
        )
    }
}

@Composable
private fun StatesStepperDemo() {
    var step by remember { mutableIntStateOf(2) }
    var skipped by remember { mutableStateOf(setOf(5)) }

    GalleryExample("States (Completed, Active, Error, Loading, Skipped & Pending)") {
        KiraStepper(
            steps = listOf(
                KiraStepItem("imported", "Imported", "completed"),
                KiraStepItem("validated", "Validated", "completed"),
                KiraStepItem("processing", "Processing", "active"),
                KiraStepItem("enrichment", "Enrichment", "error — retry available"),
                KiraStepItem("publishing", "Publishing", "loading"),
                KiraStepItem("archive", "Archive", "skipped / optional", optional = true),
                KiraStepItem("notify", "Notify", "pending"),
            ),
            activeStep = step,
            onStepChange = { step = it },
            orientation = KiraStepperOrientation.Vertical,
            completedSteps = setOf(0, 1),
            errorSteps = setOf(3),
            loadingSteps = setOf(4),
            skippedSteps = skipped,
            showProgress = true,
            showNavigation = true,
            onSkip = { skipped = skipped + it },
        )
    }
}

@Composable
private fun MobileStepperDemo() {
    var step by remember { mutableIntStateOf(1) }

    GalleryExample("Mobile (Auto-Stacks Vertically Below 640dp)") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ExampleNote("Resize below 640dp (or open on a phone): the horizontal rail automatically stacks vertically.")
            KiraStepper(
                mobileStacked = true,
                steps = listOf(
                    KiraStepItem("cart", "Cart", "Review your items"),
                    KiraStepItem("shipping", "Shipping", "Delivery details"),
                    KiraStepItem("payment", "Payment", "Payment method"),
                    KiraStepItem("review", "Review", "Confirm and pay"),
                ),
                activeStep = step,
                onStepChange = { step = it },
            )
        }
    }
}

@Composable
private fun StepUserGlyph(color: Color) {
    Canvas(Modifier.size(16.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color, radius = size.minDimension * 0.2f, center = Offset(size.width * 0.5f, size.height * 0.32f), style = stroke)
        drawArc(color, 180f, 180f, false, Offset(size.width * 0.18f, size.height * 0.58f), Size(size.width * 0.64f, size.height * 0.48f), style = stroke)
    }
}

@Composable
private fun StepMailGlyph(color: Color) {
    Canvas(Modifier.size(16.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawRoundRect(color, Offset(size.width * 0.12f, size.height * 0.22f), Size(size.width * 0.76f, size.height * 0.56f), CornerRadius(2.dp.toPx()), style = stroke)
        val flap = Path().apply {
            moveTo(size.width * 0.14f, size.height * 0.28f)
            lineTo(size.width * 0.50f, size.height * 0.56f)
            lineTo(size.width * 0.86f, size.height * 0.28f)
        }
        drawPath(flap, color, style = stroke)
    }
}

@Composable
private fun StepLockGlyph(color: Color) {
    Canvas(Modifier.size(16.dp).clearAndSetSemantics {}) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawRoundRect(color, Offset(size.width * 0.22f, size.height * 0.46f), Size(size.width * 0.56f, size.height * 0.42f), CornerRadius(2.dp.toPx()), style = stroke)
        drawArc(color, 180f, 180f, false, Offset(size.width * 0.32f, size.height * 0.16f), Size(size.width * 0.36f, size.height * 0.44f), style = stroke)
    }
}
