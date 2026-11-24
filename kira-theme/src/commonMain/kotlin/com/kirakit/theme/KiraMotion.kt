package com.kirakit.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * Motion tokens aligned with `tokens/kira-tokens.json` exported from kira-kit-v2.
 */
object KiraMotion {
    val EaseOutStrong: Easing = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)

    const val SpringDampingRatio: Float = 0.65f

    const val RouteFadeMs: Int = 150
    const val RouteTransitionMs: Int = 250
    const val FadeInMs: Int = 300
    const val ShakeMs: Int = 400
    const val PingOnceMs: Int = 800
    const val ShimmerMs: Int = 2000

    fun <T> fastTween(): TweenSpec<T> = tween(
        durationMillis = RouteFadeMs,
        easing = EaseOutStrong,
    )

    fun <T> standardTween(): TweenSpec<T> = tween(
        durationMillis = RouteTransitionMs,
        easing = EaseOutStrong,
    )

    fun <T> bouncySpring(): SpringSpec<T> = spring(
        dampingRatio = SpringDampingRatio,
        stiffness = Spring.StiffnessMediumLow,
    )
}
