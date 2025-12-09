package com.greencopper.interfacekit.ui.compose

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import com.greencopper.interfacekit.color.PressableColorComposable

@Composable
public fun Modifier.scaleOnPress(
    interactionSource: InteractionSource
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val sizeScale by animateFloatAsState(if (isPressed) 0.97f else 1f, label = "ScaleAnimation")
    this.scale(sizeScale)
}

@Composable
public fun Modifier.colorBackgroundOnClick(
    interactionSource: InteractionSource,
    pressableColor: PressableColorComposable,
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    this.background(if (isPressed) pressableColor.pressed else pressableColor.normal)
}

@Composable
public fun Modifier.bounceOnClick(
    interactionSource: InteractionSource,
    scaleDownTo: Float = 0.9f,
    scaleUpTo: Float = 1.1f,
    duration: Int = 300,
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val animation = updateTransition(isPressed)
    val scaleFactor by animation.animateFloat(
        transitionSpec = {
            if (targetState) { // isPressed
                snap()
            } else { // isReleased
                keyframes {
                    durationMillis = duration
                    scaleDownTo at 0 // start from pressed
                    scaleUpTo at duration / 2 // overshoot
                    1.0f at duration // settle
                }
            }
        },
        targetValueByState = { pressed -> if (pressed) scaleDownTo else 1.0f },
    )

    this.graphicsLayer {
        scaleX = scaleFactor
        scaleY = scaleFactor
    }
}
