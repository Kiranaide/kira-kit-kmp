package com.kirakit.gallery.input

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kirakit.components.badge.KiraTagInput
import com.kirakit.components.button.KiraButton
import com.kirakit.components.button.KiraButtonSize
import com.kirakit.components.button.KiraButtonVariant
import com.kirakit.components.input.KiraCurrencyInput
import com.kirakit.components.input.KiraDecimalInput
import com.kirakit.components.input.KiraFileInput
import com.kirakit.components.input.KiraInput
import com.kirakit.components.input.KiraInputAffix
import com.kirakit.components.input.KiraInputCluster
import com.kirakit.components.input.KiraInputPosition
import com.kirakit.components.input.KiraInputSize
import com.kirakit.components.input.KiraInputWithButton
import com.kirakit.components.input.KiraInputWithDropdown
import com.kirakit.components.input.KiraInputWithSpinner
import com.kirakit.components.input.KiraNumericInput
import com.kirakit.components.input.KiraOtpGap
import com.kirakit.components.input.KiraOtpInput
import com.kirakit.components.input.KiraOtpSize
import com.kirakit.components.input.KiraOtpType
import com.kirakit.components.input.KiraOtpVariant
import com.kirakit.components.input.KiraPasswordInput
import com.kirakit.components.input.KiraPercentageInput
import com.kirakit.components.input.KiraRatingColor
import com.kirakit.components.input.KiraRatingDisplay
import com.kirakit.components.input.KiraRatingInput
import com.kirakit.components.input.KiraRatingSize
import com.kirakit.components.input.KiraSearchInput
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import com.kirakit.theme.typography
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun InputPlayground() {
    Column(verticalArrangement = Arrangement.spacedBy(KiraTheme.spacing.md)) {
        BasicSection()
        FileSection()
        NumericSection()
        FormattedSection()
        PrefixSuffixSection()
        CompositesSection()
        SearchActionsSection()
        OtpSection()
        RatingSection()
        PasswordSection()
        CounterSection()
        TagsSection()
        ShowcaseSection()
    }
}

@Composable
private fun BasicSection() {
    var text by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var pwd by rememberSaveable { mutableStateOf("") }

    GalleryExample("Basic") {
        Column(
            modifier = Modifier.widthIn(max = 384.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            KiraInput(text, { text = it }, placeholder = "Enter text...")
            KiraInput(email, { email = it }, placeholder = "Email address", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            KiraInput(pwd, { pwd = it }, placeholder = "Password", visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))

            SizePair("Extra Small (xs)", KiraInputSize.Xs, "Extra small input", prefix = { InputDemoIcon.Search(KiraTheme.colors.mutedForeground, 12.dp) })
            SizePair("Small (sm)", KiraInputSize.Sm, "Small input", prefix = { KiraInputAffix("@") })
            SizePair("Default", KiraInputSize.Default, "Default input", prefix = { KiraInputAffix("$") })
            SizePair("Large (lg)", KiraInputSize.Lg, "Large input", prefix = { KiraInputAffix("$") }, suffix = { KiraInputAffix("%") })
            SizePair("Extra Large (xl)", KiraInputSize.Xl, "Extra large input", prefix = { KiraInputAffix("$") }, suffix = { KiraInputAffix("%") })

            FieldLabel("Disabled States")
            KiraInput("", {}, placeholder = "Disabled input", enabled = false)
            KiraInput("", {}, placeholder = "Disabled", leading = { KiraInputAffix("$") }, enabled = false)
        }
    }
}

@Composable
private fun SizePair(
    title: String,
    size: KiraInputSize,
    placeholder: String,
    prefix: (@Composable () -> Unit)? = null,
    suffix: (@Composable () -> Unit)? = null,
) {
    var plain by rememberSaveable(title) { mutableStateOf("") }
    var adorned by rememberSaveable("$title-adorned") { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FieldLabel(title)
        KiraInput(plain, { plain = it }, size = size, placeholder = placeholder)
        KiraInput(
            adorned,
            { adorned = it },
            size = size,
            placeholder = if (suffix != null) "With prefix & suffix" else "With prefix",
            leading = prefix,
            trailing = suffix,
        )
    }
}

@Composable
private fun FileSection() {
    var files by remember { mutableStateOf(emptyList<String>()) }
    var error by remember { mutableStateOf(false) }
    val launcher = rememberFilePickerLauncher(
        type = FileKitType.File("pdf", "txt", "csv", "png", "jpg"),
        mode = FileKitMode.Multiple(),
        onError = { error = true },
    ) { selected ->
        if (selected != null) {
            files = selected.map { it.name }
            error = false
        }
    }
    GalleryExample("File") {
        Column(
            modifier = Modifier.widthIn(max = 384.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            KiraFileInput(
                fileNames = files,
                onBrowse = { launcher.launch() },
                onClear = { files = emptyList() },
                helperText = if (error) "Could not open the file picker. Try again." else "PDF, TXT, CSV, PNG, or JPG",
                isError = error,
            )
            KiraFileInput(
                fileNames = emptyList(),
                onBrowse = {},
                label = "Disabled",
                enabled = false,
            )
        }
    }
}

@Composable
private fun NumericSection() {
    var value by rememberSaveable { mutableStateOf("5") }
    var decimalStep by rememberSaveable { mutableStateOf("1.5") }

    GalleryExample("Numeric") {
        Column(
            modifier = Modifier.widthIn(max = 180.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            KiraNumericInput(value, { value = it }, label = "With Buttons", min = 0.0, max = 10.0)
            KiraNumericInput(value, { value = it }, label = "Without Buttons", showControls = false)
            KiraNumericInput(decimalStep, { decimalStep = it }, label = "Step = 0.1", step = 0.1, min = 0.0, max = 5.0)
            KiraNumericInput("5", {}, label = "Disabled", enabled = false)
            KiraNumericInput(
                value,
                { value = it },
                label = "Scrub (drag horizontally)",
                min = 0.0,
                max = 10.0,
                scrub = true,
            )
        }
    }
}

@Composable
private fun FormattedSection() {
    var currency by remember { mutableStateOf<Double?>(1234.56) }
    var percentage by remember { mutableStateOf<Double?>(12.5) }
    var decimal by remember { mutableStateOf<Double?>(98_765.43) }

    GalleryExample("Formatted") {
        Column(
            modifier = Modifier.widthIn(max = 384.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            KiraCurrencyInput(
                value = currency,
                onValueChange = { currency = it },
                label = "Currency (auto decimal + locale format)",
                currency = "IDR",
            )
            KiraPercentageInput(
                value = percentage,
                onValueChange = { percentage = it },
                label = "Percentage (auto decimal + % suffix)",
            )
            KiraDecimalInput(
                value = decimal,
                onValueChange = { decimal = it },
                label = "Decimal (fixed fraction digits)",
                fractionDigits = 3,
            )
        }
    }
}

@Composable
private fun PrefixSuffixSection() {
    var amount by rememberSaveable { mutableStateOf("") }
    var pct by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var search by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var minPrice by rememberSaveable { mutableStateOf("") }
    var maxPrice by rememberSaveable { mutableStateOf("") }

    GalleryExample("Prefix / Suffix") {
        Column(
            modifier = Modifier.widthIn(max = 384.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            KiraInput(amount, { amount = it }, label = "Currency Input", leading = { KiraInputAffix("$") }, placeholder = "0.00")
            KiraInput(pct, { pct = it }, label = "Percentage Input", trailing = { KiraInputAffix("%") }, placeholder = "0")
            KiraInput(
                email,
                { email = it },
                label = "Email Input",
                leading = { KiraInputAffix("@") },
                trailing = { KiraInputAffix("@example.com") },
                placeholder = "username",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            KiraInput(
                search,
                { search = it },
                label = "Search Input",
                leading = { InputDemoIcon.Search(KiraTheme.colors.mutedForeground, 16.dp) },
                placeholder = "Search...",
            )
            KiraInput(
                phone,
                { phone = it },
                label = "Phone Number",
                leading = { KiraInputAffix("+1") },
                placeholder = "(555) 000-0000",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("Price Range")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    KiraInput(minPrice, { minPrice = it }, leading = { KiraInputAffix("$") }, placeholder = "Min", modifier = Modifier.weight(1f))
                    BasicText("to", style = KiraTheme.typography.body.copy(color = KiraTheme.colors.mutedForeground))
                    KiraInput(maxPrice, { maxPrice = it }, leading = { KiraInputAffix("$") }, placeholder = "Max", modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CompositesSection() {
    var email by rememberSaveable { mutableStateOf("") }
    var pwd by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var subEmail by rememberSaveable { mutableStateOf("") }
    var subscribed by rememberSaveable { mutableStateOf(false) }
    var searchTerm by rememberSaveable { mutableStateOf("") }
    var verifyCode by rememberSaveable { mutableStateOf("") }
    var spinText by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var countryCode by rememberSaveable { mutableStateOf("+1") }
    var phone by rememberSaveable { mutableStateOf("") }
    var domain by rememberSaveable { mutableStateOf("gmail.com") }
    var handle by rememberSaveable { mutableStateOf("") }
    var startDate by rememberSaveable { mutableStateOf("") }
    var endDate by rememberSaveable { mutableStateOf("") }
    var minRange by rememberSaveable { mutableStateOf("") }
    var maxRange by rememberSaveable { mutableStateOf("") }

    GalleryExample("Composites") {
        Column(
            modifier = Modifier.widthIn(max = 384.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            KiraInput(
                email,
                { email = it },
                label = "Email Address",
                helperText = "We'll never share your email with anyone else.",
                placeholder = "name@example.com",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            KiraInput(
                pwd,
                { pwd = it },
                label = "Password",
                helperText = "Must be at least 8 characters",
                placeholder = "Enter password",
                visualTransformation = PasswordVisualTransformation(),
            )
            KiraInput(
                username,
                { username = it },
                label = "Username",
                isError = true,
                helperText = "Username is already taken",
                placeholder = "username",
            )

            KiraInputWithButton(
                value = subEmail,
                onValueChange = { subEmail = it; subscribed = false },
                label = "Subscribe",
                placeholder = "Enter your email",
                buttonText = if (subscribed) "Subscribed" else "Subscribe",
                buttonIcon = InputDemoIcon.Send,
                buttonPosition = KiraInputPosition.Right,
                onButtonClick = { subscribed = true },
            )
            KiraInputWithButton(
                value = searchTerm,
                onValueChange = { searchTerm = it },
                label = "Search",
                placeholder = "Search...",
                buttonText = "Search",
                buttonIcon = InputDemoIcon.Search,
                buttonPosition = KiraInputPosition.Left,
                buttonVariant = KiraButtonVariant.Outline,
                onButtonClick = {},
            )
            KiraInputWithButton(
                value = verifyCode,
                onValueChange = { verifyCode = it },
                label = "Verify",
                placeholder = "Enter code",
                buttonText = "Verify",
                buttonIcon = InputDemoIcon.Check,
                buttonPosition = KiraInputPosition.Right,
                buttonVariant = KiraButtonVariant.Success,
                onButtonClick = {},
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraInputWithSpinner(
                    value = spinText,
                    onValueChange = { spinText = it },
                    label = "Loading State",
                    placeholder = "Searching...",
                    isLoading = isLoading,
                )
                KiraButton(
                    text = if (isLoading) "Stop loading" else "Toggle loading",
                    size = KiraButtonSize.Xs,
                    variant = KiraButtonVariant.Ghost,
                    onClick = { isLoading = !isLoading },
                )
            }

            KiraInputWithDropdown(
                value = phone,
                onValueChange = { phone = it },
                label = "Country Code",
                placeholder = "Phone number",
                options = listOf("+1" to "+1", "+44" to "+44", "+33" to "+33", "+49" to "+49", "+81" to "+81"),
                selectedOption = countryCode,
                onOptionSelect = { countryCode = it },
                dropdownPosition = KiraInputPosition.Left,
            )
            KiraInputWithDropdown(
                value = handle,
                onValueChange = { handle = it },
                label = "Domain",
                placeholder = "Username",
                options = listOf("gmail.com" to "@gmail.com", "yahoo.com" to "@yahoo.com", "outlook.com" to "@outlook.com"),
                selectedOption = domain,
                onOptionSelect = { domain = it },
                dropdownPosition = KiraInputPosition.Right,
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("Date Range")
                KiraInputCluster(
                    left = { KiraInput(startDate, { startDate = it }, placeholder = "Start date", modifier = Modifier.weight(1f)) },
                    right = { KiraInput(endDate, { endDate = it }, placeholder = "End date", modifier = Modifier.weight(1f)) },
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("Price Range")
                KiraInputCluster(
                    left = { KiraInput(minRange, { minRange = it }, leading = { KiraInputAffix("$") }, placeholder = "Min", modifier = Modifier.weight(1f)) },
                    right = { KiraInput(maxRange, { maxRange = it }, leading = { KiraInputAffix("$") }, placeholder = "Max", modifier = Modifier.weight(1f)) },
                )
            }
        }
    }
}

@Composable
private fun SearchActionsSection() {
    var query by rememberSaveable { mutableStateOf("") }
    var result by rememberSaveable { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var request by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()

    var sXs by rememberSaveable { mutableStateOf("") }
    var sSm by rememberSaveable { mutableStateOf("") }
    var sDef by rememberSaveable { mutableStateOf("") }
    var sLg by rememberSaveable { mutableStateOf("") }
    var sXl by rememberSaveable { mutableStateOf("") }
    var clearable by rememberSaveable { mutableStateOf("Type to clear me") }

    GalleryExample("Search & Actions") {
        Column(
            modifier = Modifier.widthIn(max = 384.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraSearchInput(
                    value = query,
                    onValueChange = { query = it },
                    onSearch = { term ->
                        request?.cancel()
                        loading = true
                        request = scope.launch {
                            delay(400)
                            result = term
                            loading = false
                        }
                    },
                    label = "Debounced Search",
                    debounceMillis = 500,
                    loading = loading,
                )
                ExampleNote("Current value: \"$query\"" + if (result.isNotBlank()) " · Last search: \"$result\"" else "")
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FieldLabel("Size Variants")
                KiraSearchInput(sXs, { sXs = it }, size = KiraInputSize.Xs, placeholder = "Extra small search...")
                KiraSearchInput(sSm, { sSm = it }, size = KiraInputSize.Sm, placeholder = "Small search...")
                KiraSearchInput(sDef, { sDef = it }, size = KiraInputSize.Default, placeholder = "Default search...")
                KiraSearchInput(sLg, { sLg = it }, size = KiraInputSize.Lg, placeholder = "Large search...")
                KiraSearchInput(sXl, { sXl = it }, size = KiraInputSize.Xl, placeholder = "Extra large search...")
            }

            KiraInput(
                value = clearable,
                onValueChange = { clearable = it },
                label = "Clearable (Controlled)",
                clearable = true,
                placeholder = "Type something...",
            )
            KiraInput(
                value = "https://example.com/share/abc123",
                onValueChange = {},
                label = "Shareable Link",
                readOnly = true,
                copyable = true,
            )
            KiraInput(
                value = "sk-12345abcdef67890",
                onValueChange = {},
                label = "API Key",
                size = KiraInputSize.Sm,
                readOnly = true,
                copyable = true,
            )
            KiraInput(
                value = "WELCOME-2024",
                onValueChange = {},
                label = "Invite Code",
                size = KiraInputSize.Lg,
                readOnly = true,
                copyable = true,
            )
        }
    }
}

@Composable
private fun OtpSection() {
    var otp by rememberSaveable { mutableStateOf("") }
    var otpSm by rememberSaveable { mutableStateOf("") }
    var otpDef by rememberSaveable { mutableStateOf("") }
    var otpLg by rememberSaveable { mutableStateOf("") }
    var otpOutline by rememberSaveable { mutableStateOf("") }
    var otpFilled by rememberSaveable { mutableStateOf("") }
    var otpSep by rememberSaveable { mutableStateOf("") }
    var otpAlpha by rememberSaveable { mutableStateOf("") }
    var otpPin by rememberSaveable { mutableStateOf("") }

    GalleryExample("OTP") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraOtpInput(otp, { otp = it }, label = "6-Digit Verification Code")
                ExampleNote(if (otp.length == 6) "Verification code entered: $otp" else "${otp.length} of 6 digits entered")
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FieldLabel("Size Variants")
                ExampleNote("Small")
                KiraOtpInput(otpSm, { otpSm = it }, length = 4, size = KiraOtpSize.Sm)
                ExampleNote("Default")
                KiraOtpInput(otpDef, { otpDef = it }, length = 4, size = KiraOtpSize.Default)
                ExampleNote("Large")
                KiraOtpInput(otpLg, { otpLg = it }, length = 4, size = KiraOtpSize.Lg)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FieldLabel("Style Variants")
                ExampleNote("Outline")
                KiraOtpInput(otpOutline, { otpOutline = it }, length = 4, variant = KiraOtpVariant.Outline)
                ExampleNote("Filled")
                KiraOtpInput(otpFilled, { otpFilled = it }, length = 4, variant = KiraOtpVariant.Filled)
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraOtpInput(
                    otpSep,
                    { otpSep = it },
                    length = 6,
                    label = "With Separator",
                    separatorAfter = 3,
                    gap = KiraOtpGap.Lg,
                )
                ExampleNote("Separator appears after the 3rd digit")
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KiraOtpInput(
                    otpAlpha,
                    { otpAlpha = it },
                    length = 6,
                    label = "Alphanumeric Code",
                    type = KiraOtpType.Alphanumeric,
                    placeholder = "—",
                )
                ExampleNote("Accepts both letters and numbers (auto-uppercased)")
            }

            KiraOtpInput(otpPin, { otpPin = it }, length = 4, label = "PIN Input (Masked)", masked = true)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                KiraOtpInput("0000", {}, length = 4, label = "Invalid State", isError = true)
                BasicText("Invalid verification code", style = KiraTheme.typography.caption.copy(color = KiraTheme.colors.destructive))
            }
            KiraOtpInput("1234", {}, length = 4, label = "Disabled", enabled = false)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RatingSection() {
    var rating by rememberSaveable { mutableFloatStateOf(3.5f) }
    var feedback by rememberSaveable { mutableFloatStateOf(0f) }

    GalleryExample("Rating") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("Interactive Rating")
                KiraRatingInput(rating, { rating = it }, allowHalf = true, showValue = true)
                ExampleNote("Tap left/right half of a star or use arrow keys. Supports half-stars.")
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("Size Variants")
                listOf("Small" to KiraRatingSize.Sm, "Default" to KiraRatingSize.Default, "Large" to KiraRatingSize.Lg, "XL" to KiraRatingSize.Xl).forEach { (name, sz) ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        BasicText(name, modifier = Modifier.width(64.dp), style = KiraTheme.typography.caption.copy(color = KiraTheme.colors.mutedForeground))
                        KiraRatingInput(4f, {}, size = sz, readOnly = true)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("Color Variants")
                listOf(
                    "Yellow" to KiraRatingColor.Yellow,
                    "Orange" to KiraRatingColor.Orange,
                    "Red" to KiraRatingColor.Red,
                    "Pink" to KiraRatingColor.Pink,
                    "Primary" to KiraRatingColor.Default,
                ).forEach { (name, tint) ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        BasicText(name, modifier = Modifier.width(64.dp), style = KiraTheme.typography.caption.copy(color = KiraTheme.colors.mutedForeground))
                        KiraRatingInput(4f, {}, color = tint, readOnly = true)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("Half Star Support")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    KiraRatingInput(2.5f, {}, allowHalf = true, readOnly = true, showValue = true)
                    KiraRatingInput(3.5f, {}, allowHalf = true, readOnly = true, showValue = true)
                    KiraRatingInput(4.5f, {}, allowHalf = true, readOnly = true, showValue = true)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("Rating Display (Read-only)")
                KiraRatingDisplay(value = 4.7f, reviewCount = 1_234)
                KiraRatingDisplay(value = 3.2f, reviewCount = 89, color = KiraRatingColor.Orange)
                KiraRatingDisplay(value = 5.0f, reviewCount = 42, color = KiraRatingColor.Pink)
            }

            Column(
                modifier = Modifier
                    .widthIn(max = 384.dp)
                    .fillMaxWidth()
                    .clip(KiraTheme.radius.shapeLg)
                    .background(KiraTheme.colors.muted.copy(alpha = 0.30f))
                    .border(1.dp, KiraTheme.colors.border, KiraTheme.radius.shapeLg)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FieldLabel("How was your experience?")
                KiraRatingInput(feedback, { feedback = it }, size = KiraRatingSize.Lg)
                if (feedback > 0f) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val sentiment = when (feedback.toInt()) {
                            1 -> "Poor"
                            2 -> "Fair"
                            3 -> "Good"
                            4 -> "Great"
                            else -> "Excellent!"
                        }
                        BasicText(sentiment, style = KiraTheme.typography.body.copy(color = KiraTheme.colors.foreground))
                        KiraButton("Submit", size = KiraButtonSize.Sm, onClick = { feedback = 0f })
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("Disabled")
                KiraRatingInput(3f, {}, enabled = false)
            }
        }
    }
}

@Composable
private fun PasswordSection() {
    var pwd by remember { mutableStateOf("") }
    var strong by remember { mutableStateOf("") }

    GalleryExample("Password") {
        Column(
            modifier = Modifier.widthIn(max = 384.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            KiraPasswordInput(pwd, { pwd = it }, label = "Password (with visibility toggle)", placeholder = "Enter password")
            KiraPasswordInput(
                strong,
                { strong = it },
                label = "With strength meter",
                placeholder = "Try mixing cases, numbers & symbols",
                showStrength = true,
            )
        }
    }
}

@Composable
private fun CounterSection() {
    var handle by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    GalleryExample("Counter") {
        Column(
            modifier = Modifier.widthIn(max = 384.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            KiraInput(
                value = handle,
                onValueChange = { handle = it },
                label = "Username (max 20)",
                placeholder = "username",
                maxLength = 20,
                showCounter = true,
            )
            KiraInput(
                value = description,
                onValueChange = { description = it },
                label = "Description (max 120)",
                placeholder = "Write a short bio...",
                singleLine = false,
                maxLength = 120,
                showCounter = true,
            )
        }
    }
}

@Composable
private fun TagsSection() {
    var tags by rememberSaveable { mutableStateOf(listOf("design", "react")) }
    var simpleTags by rememberSaveable { mutableStateOf(listOf("one")) }

    GalleryExample("Tags") {
        Column(
            modifier = Modifier.widthIn(max = 384.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("Tags (Enter or comma to add, Backspace to remove)")
                KiraTagInput(
                    tags = tags,
                    onTagsChange = { tags = it },
                    suggestions = listOf("design", "react", "kotlin", "compose", "kmp", "a11y"),
                    maxTags = 6,
                )
                ExampleNote("${tags.size}/6 tags — ${if (tags.isEmpty()) "none yet" else tags.joinToString(", ")}")
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("Simple")
                KiraTagInput(
                    tags = simpleTags,
                    onTagsChange = { simpleTags = it },
                    placeholder = "Add a tag…",
                )
            }
        }
    }
}

@Composable
private fun ShowcaseSection() {
    var zero by rememberSaveable { mutableStateOf("0") }
    var zeroText by rememberSaveable { mutableStateOf("") }
    var longVal by rememberSaveable {
        mutableStateOf("A very long controlled value that overflows a narrow input container and must scroll rather than break the layout")
    }
    var rtlVal by rememberSaveable { mutableStateOf("مايكل جاكسون") }

    GalleryExample("Showcase") {
        Column(
            modifier = Modifier.widthIn(max = 448.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("Controlled numeric zero (0 must count as a value)")
                KiraNumericInput(
                    value = zero,
                    onValueChange = { zero = it.ifEmpty { "0" } },
                    min = 0.0,
                    max = 10.0,
                    modifier = Modifier.widthIn(max = 160.dp),
                )
                ExampleNote("Value: $zero")
                KiraInput(
                    value = zeroText,
                    onValueChange = { zeroText = it },
                    clearable = true,
                    showCounter = true,
                    maxLength = 10,
                    placeholder = "Type up to 10 chars — counter warns at 9",
                )
            }
            KiraInput(
                value = "",
                onValueChange = {},
                label = "Very long placeholder",
                placeholder = "An extremely long placeholder string that should not break the layout of the input or overflow its container in any direction",
            )
            KiraInput(
                value = longVal,
                onValueChange = { longVal = it },
                label = "Very long value",
            )
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                KiraInput(
                    value = rtlVal,
                    onValueChange = { rtlVal = it },
                    label = "RTL",
                    placeholder = "البريد الإلكتروني",
                )
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    BasicText(
        text = text,
        style = KiraTheme.typography.label.copy(
            color = KiraTheme.colors.foreground,
            fontWeight = FontWeight.Medium,
        ),
    )
}

private object InputDemoIcon {
    val Search: @Composable (Color, Dp) -> Unit = { color, size ->
        Canvas(Modifier.size(size).clearAndSetSemantics {}) {
            val w = this.size.width
            val h = this.size.height
            val strokeWidth = (this.size.minDimension * 0.12f).coerceAtLeast(1.4.dp.toPx())
            drawCircle(color, radius = this.size.minDimension * 0.30f, center = Offset(w * 0.44f, h * 0.44f), style = Stroke(width = strokeWidth))
            drawLine(color, Offset(w * 0.66f, h * 0.66f), Offset(w * 0.86f, h * 0.86f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        }
    }

    val Send: @Composable (Color, Dp) -> Unit = { color, size ->
        Canvas(Modifier.size(size).clearAndSetSemantics {}) {
            val w = this.size.width
            val h = this.size.height
            val stroke = Stroke(width = (this.size.minDimension * 0.12f).coerceAtLeast(1.3.dp.toPx()), cap = StrokeCap.Round, join = StrokeJoin.Round)
            val path = Path().apply {
                moveTo(w * 0.86f, h * 0.14f)
                lineTo(w * 0.12f, h * 0.46f)
                lineTo(w * 0.44f, h * 0.56f)
                lineTo(w * 0.54f, h * 0.88f)
                close()
            }
            drawPath(path, color = color, style = stroke)
            drawLine(color, Offset(w * 0.86f, h * 0.14f), Offset(w * 0.44f, h * 0.56f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        }
    }

    val Check: @Composable (Color, Dp) -> Unit = { color, size ->
        Canvas(Modifier.size(size).clearAndSetSemantics {}) {
            val stroke = Stroke(width = (this.size.minDimension * 0.15f).coerceAtLeast(1.4.dp.toPx()), cap = StrokeCap.Round, join = StrokeJoin.Round)
            val path = Path().apply {
                moveTo(this@Canvas.size.width * 0.18f, this@Canvas.size.height * 0.52f)
                lineTo(this@Canvas.size.width * 0.42f, this@Canvas.size.height * 0.76f)
                lineTo(this@Canvas.size.width * 0.84f, this@Canvas.size.height * 0.26f)
            }
            drawPath(path, color = color, style = stroke)
        }
    }
}
