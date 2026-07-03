package com.inod.screenofftimer.ui.components

import android.annotation.SuppressLint
import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.navigation.NavController
import kotlin.coroutines.cancellation.CancellationException

enum class BackSource { GESTURE, CLICK }

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun PredictiveBackContainer(
    navController: NavController,
    previousContent: (@Composable () -> Unit)? = null,
    onDismiss: () -> Unit,
    currentContent: @Composable () -> Unit,
) {
    val currentEntry = navController.currentBackStackEntry
    val animatedProgress = remember { Animatable(0f) }

    var swipeEdge by remember { mutableIntStateOf(BackEventCompat.EDGE_LEFT) }

    var startTouchY by remember { mutableFloatStateOf(0f) }
    var currentTouchY by remember { mutableFloatStateOf(0f) }
    var hasStarted by remember { mutableStateOf(false) }


    PredictiveBackHandler(enabled = navController.previousBackStackEntry != null) { backEvent ->
        hasStarted = false
        try {
            backEvent.collect { event ->
                swipeEdge = event.swipeEdge
                if (!hasStarted) {
                    startTouchY = event.touchY
                    hasStarted = true
                }
                currentTouchY = event.touchY
                animatedProgress.snapTo(event.progress)
            }
            currentEntry?.savedStateHandle?.set("backSource", BackSource.GESTURE.name)
            onDismiss()
        } catch (e: CancellationException) {
            animatedProgress.animateTo(0f, tween(150))
        } finally {
            hasStarted = false
//            animatedProgress.snapTo(0f)
        }
    }

    val density = LocalDensity.current
    val p = FastOutSlowInEasing.transform(animatedProgress.value)
    val direction = if (swipeEdge == BackEventCompat.EDGE_LEFT) 1f else -1f

    Box(Modifier.fillMaxSize()) {
        val screenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
        val peekOffset = screenWidthPx * 0.15f
        val peekOffsetY = 0f

        val maxTranslateX = with(density) { 24.dp.toPx() }
        val maxTranslateY = with(density) { 16.dp.toPx()}

        val dampingFactorX = 1f //smoothness
        val currentTranslationX = lerp(0f, maxTranslateX, p) * dampingFactorX

        val touchDeltaY = currentTouchY - startTouchY
        val dampingFactor = 0.5f // smoothness
        val dampedDeltaY = (touchDeltaY * dampingFactor).coerceIn(-maxTranslateY, maxTranslateY)
        val currentTranslationY = dampedDeltaY * p

        val prevTranslationX = currentTranslationX - peekOffset
        val prevTranslationY = currentTranslationY - peekOffsetY

        val prevScale = lerp(1f, 1f, p)
        val prevAlpha = lerp(0.5f, 1f, p)

        Box(
            Modifier
                .background(Color.Black.copy(alpha = 0.09f * (1f + p)))
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = prevScale
                    scaleY = prevScale
//                    translationX = prevTranslationX
//                    translationY = prevTranslationY
                    alpha = prevAlpha
//                    transformOrigin = TransformOrigin(0f, 0.5f)
//                    shadowElevation = 1f
                }
        ) {
            previousContent?.invoke()
        }

        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.05f * (1f + p)))
        )

        val currentScale = lerp(1f, 0.9f, p)
//        val maxTranslateX = with(density) { 8.dp.toPx() }
        val cornerRadius = lerp(0f, 32f, p)

        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = currentScale
                    scaleY = currentScale
                    translationX = currentTranslationX * direction
                    translationY = currentTranslationY
                    shape = RoundedCornerShape(cornerRadius.dp)
                    clip = true
//                    shadowElevation = 1f
                }
        ) {
            currentContent()
        }
    }
}