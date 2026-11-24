package com.kirakit.gallery.avatar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.kirakit.components.avatar.KiraAvatar
import com.kirakit.components.avatar.KiraAvatarBadgeVariant
import com.kirakit.components.avatar.KiraAvatarCornerPosition
import com.kirakit.components.avatar.KiraAvatarGradient
import com.kirakit.components.avatar.KiraAvatarGroup
import com.kirakit.components.avatar.KiraAvatarGroupMember
import com.kirakit.components.avatar.KiraAvatarRing
import com.kirakit.components.avatar.KiraAvatarShape
import com.kirakit.components.avatar.KiraAvatarSize
import com.kirakit.components.avatar.KiraAvatarStack
import com.kirakit.components.avatar.KiraAvatarStatus
import com.kirakit.components.avatar.KiraAvatarTyping
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.SectionHeading
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import com.kirakit.theme.typography

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AvatarPlayground() {
    val gap = KiraTheme.spacing.md
    val colors = KiraTheme.colors

    val sampleMembers = remember {
        listOf(
            KiraAvatarGroupMember("Cody Nguyen", fallback = "CN", gradient = KiraAvatarGradient.Blue),
            KiraAvatarGroupMember("Vera Chen", fallback = "VC", gradient = KiraAvatarGradient.Purple),
            KiraAvatarGroupMember("Abe Brooks", fallback = "AB", gradient = KiraAvatarGradient.Green),
            KiraAvatarGroupMember("Dana Kim", fallback = "DK", gradient = KiraAvatarGradient.Orange),
            KiraAvatarGroupMember("Erin Patel", fallback = "EP", gradient = KiraAvatarGradient.Pink),
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        SectionHeading("Basic, Sizes & Shapes")
        GalleryExample("Basic Avatars") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                KiraAvatar(name = "Cody Nguyen", gradient = KiraAvatarGradient.Blue)
                KiraAvatar(name = "Vera Chen", gradient = KiraAvatarGradient.Purple)
                KiraAvatar(fallback = "JD")
                KiraAvatar(
                    name = "Guest",
                    icon = { AvatarUserGlyph(colors.mutedForeground) },
                )
            }
        }

        GalleryExample("Size Ladder (Xs 24dp to 4xl 128dp)") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                itemVerticalAlignment = Alignment.Bottom,
            ) {
                KiraAvatarSize.entries.forEach { sz ->
                    val tag = when (sz) {
                        KiraAvatarSize.Default -> "DF"
                        KiraAvatarSize.Xxl -> "2XL"
                        KiraAvatarSize.Xxxl -> "3XL"
                        KiraAvatarSize.Xxxxl -> "4XL"
                        else -> sz.name.uppercase()
                    }
                    KiraAvatar(fallback = tag, size = sz)
                }
            }
        }

        GalleryExample("Shapes (Circle, Rounded, Square)") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                KiraAvatar(fallback = "CI", shape = KiraAvatarShape.Circle, size = KiraAvatarSize.Lg)
                KiraAvatar(fallback = "RD", shape = KiraAvatarShape.Rounded, size = KiraAvatarSize.Lg)
                KiraAvatar(fallback = "SQ", shape = KiraAvatarShape.Square, size = KiraAvatarSize.Lg)
            }
        }

        SectionHeading("Status, Presence, Rings & Badges")
        GalleryExample("Status Indicators & Positions") {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    KiraAvatar(fallback = "ON", size = KiraAvatarSize.Lg, status = KiraAvatarStatus.Online, statusLabel = "Online")
                    KiraAvatar(fallback = "OF", size = KiraAvatarSize.Lg, status = KiraAvatarStatus.Offline, statusLabel = "Offline")
                    KiraAvatar(fallback = "BS", size = KiraAvatarSize.Lg, status = KiraAvatarStatus.Busy, statusLabel = "Busy")
                    KiraAvatar(fallback = "AW", size = KiraAvatarSize.Lg, status = KiraAvatarStatus.Away, statusLabel = "Away")
                    KiraAvatar(fallback = "GR", size = KiraAvatarSize.Lg, status = KiraAvatarStatus.OfflineGray)
                }

                ExampleNote("Status Positions")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    KiraAvatar(fallback = "TR", size = KiraAvatarSize.Lg, status = KiraAvatarStatus.Online, statusPosition = KiraAvatarCornerPosition.TopRight)
                    KiraAvatar(fallback = "TL", size = KiraAvatarSize.Lg, status = KiraAvatarStatus.Online, statusPosition = KiraAvatarCornerPosition.TopLeft)
                    KiraAvatar(fallback = "BR", size = KiraAvatarSize.Lg, status = KiraAvatarStatus.Online, statusPosition = KiraAvatarCornerPosition.BottomRight)
                    KiraAvatar(fallback = "BL", size = KiraAvatarSize.Lg, status = KiraAvatarStatus.Online, statusPosition = KiraAvatarCornerPosition.BottomLeft)
                }
            }
        }

        GalleryExample("Pulsing Online Ring & Typing Presence") {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    KiraAvatar(name = "Cody Nguyen", size = KiraAvatarSize.Lg, status = KiraAvatarStatus.Online, statusPulse = true, gradient = KiraAvatarGradient.Blue)
                    KiraAvatar(fallback = "ON", size = KiraAvatarSize.Lg, status = KiraAvatarStatus.Online, statusPulse = true)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    KiraAvatar(fallback = "JD", size = KiraAvatarSize.Default)
                    KiraAvatarTyping()
                    BasicText("typing…", style = KiraTheme.typography.caption.copy(color = colors.mutedForeground))
                }
            }
        }

        GalleryExample("Rings & Corner Badges") {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    KiraAvatar(fallback = "PR", size = KiraAvatarSize.Lg, ring = KiraAvatarRing.Primary)
                    KiraAvatar(fallback = "SU", size = KiraAvatarSize.Lg, ring = KiraAvatarRing.Success)
                    KiraAvatar(fallback = "WA", size = KiraAvatarSize.Lg, ring = KiraAvatarRing.Warning)
                    KiraAvatar(fallback = "DE", size = KiraAvatarSize.Lg, ring = KiraAvatarRing.Destructive)
                    KiraAvatar(fallback = "IN", size = KiraAvatarSize.Lg, ring = KiraAvatarRing.Info)
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    KiraAvatar(fallback = "CN", size = KiraAvatarSize.Lg, badgeText = "5", badgeVariant = KiraAvatarBadgeVariant.Destructive)
                    KiraAvatar(fallback = "VC", size = KiraAvatarSize.Lg, badgeText = "✓", badgeVariant = KiraAvatarBadgeVariant.Success)
                    KiraAvatar(fallback = "AB", size = KiraAvatarSize.Lg, badgeText = "!", badgeVariant = KiraAvatarBadgeVariant.Warning)
                    KiraAvatar(fallback = "GM", size = KiraAvatarSize.Lg, badgeText = "12", badgeVariant = KiraAvatarBadgeVariant.Info)
                }
            }
        }

        SectionHeading("Gradients, KiraAvatar & Interactive")
        GalleryExample("Gradient Fallbacks") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                KiraAvatarGradient.entries.forEach { grad ->
                    KiraAvatar(
                        fallback = grad.name.take(2).uppercase(),
                        size = KiraAvatarSize.Lg,
                        gradient = grad,
                    )
                }
            }
        }

        GalleryExample("KiraAvatar (Initials, Last-Seen Tooltip & Interactive)") {
            var profileClicks by remember { mutableIntStateOf(0) }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    KiraAvatar(name = "John Doe", size = KiraAvatarSize.Lg, status = KiraAvatarStatus.Online, statusPulse = true)
                    KiraAvatar(name = "Jane Smith", size = KiraAvatarSize.Lg, badgeText = "3", gradient = KiraAvatarGradient.Blue)
                    KiraAvatar(name = "Bob Johnson", size = KiraAvatarSize.Lg, gradient = KiraAvatarGradient.Purple)
                    KiraAvatar(name = "Alice", size = KiraAvatarSize.Lg, lastSeen = "2h ago", status = KiraAvatarStatus.Away)
                    KiraAvatar(
                        name = "Open profile",
                        fallback = "OP",
                        size = KiraAvatarSize.Lg,
                        ring = KiraAvatarRing.Primary,
                        onClick = { profileClicks++ },
                    )
                }
                ExampleNote("Hover/long-press Alice for last-seen tooltip. Interactive avatar clicks: $profileClicks")
            }
        }

        SectionHeading("Group & Scrollable Stack")
        GalleryExample("AvatarGroup (Max 3, Tooltips, Overflow Popover & Add Member)") {
            var dynamicMembers by remember { mutableStateOf(sampleMembers.take(2)) }
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ExampleNote("Default (max 3) — tap +2 to expand hidden members")
                KiraAvatarGroup(members = sampleMembers, max = 3)

                ExampleNote("With per-avatar tooltips + overflow popover")
                KiraAvatarGroup(members = sampleMembers, max = 3, tooltips = true)

                ExampleNote("With add-member button (${dynamicMembers.size} members)")
                KiraAvatarGroup(
                    members = dynamicMembers,
                    max = 4,
                    onAddMember = {
                        val nextIdx = dynamicMembers.size + 1
                        dynamicMembers = dynamicMembers + KiraAvatarGroupMember("Member $nextIdx", fallback = "M$nextIdx")
                    },
                )
            }
        }

        GalleryExample("AvatarStack (Horizontally Scrollable + Count)") {
            val stackMembers = remember {
                listOf("CN", "VC", "AB", "CD", "EF", "GH", "IJ", "KL").map {
                    KiraAvatarGroupMember(name = it, fallback = it)
                }
            }
            KiraAvatarStack(members = stackMembers)
        }
    }
}

@Composable
private fun AvatarUserGlyph(color: Color) {
    Canvas(Modifier.size(18.dp)) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(
            color = color,
            radius = size.minDimension * 0.20f,
            center = Offset(size.width * 0.5f, size.height * 0.30f),
            style = stroke,
        )
        drawArc(
            color = color,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(size.width * 0.16f, size.height * 0.54f),
            size = Size(size.width * 0.68f, size.height * 0.52f),
            style = stroke,
        )
    }
}
