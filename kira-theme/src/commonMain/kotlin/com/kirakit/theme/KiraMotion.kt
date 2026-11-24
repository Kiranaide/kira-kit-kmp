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
 *
 * Spec instances are pre-allocated as immutable singletons to avoid per-recomposition
 * allocations across Android ART and Kotlin/Native (iOS).
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

    private val cachedFastTween: TweenSpec<Any> = tween(
        durationMillis = RouteFadeMs,
        easing = EaseOutStrong,
    )

    private val cachedStandardTween: TweenSpec<Any> = tween(
        durationMillis = RouteTransitionMs,
        easing = EaseOutStrong,
    )

    private val cachedBouncySpring: SpringSpec<Any> = spring(
        dampingRatio = SpringDampingRatio,
        stiffness = Spring.StiffnessMediumLow,
    )

    @Suppress("UNCHECKED_CAST")
    fun <T> fastTween(): TweenSpec<T> = cachedFastTween as TweenSpec<T>

    @Suppress("UNCHECKED_CAST")
    fun <T> standardTween(): TweenSpec<T> = cachedStandardTween as TweenSpec<T>

    @Suppress("UNCHECKED_CAST")
    fun <T> bouncySpring(): SpringSpec<T> = cachedBouncySpring as SpringSpec<T>
}
