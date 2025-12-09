package com.greencopper.interfacekit.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.material.button.MaterialButton
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.ui.dpToPx

public class LeapButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : MaterialButton(context, attrs, defStyleAttr) {

    init {
        elevation = 0f
        height = resources.getDimension(R.dimen.button_height).toInt()
        insetTop = 0
        insetBottom = 0
        cornerRadius = resources.getDimension(R.dimen.button_corner_radius).toInt()
        stateListAnimator = null
        isAllCaps = false
        gravity = Gravity.CENTER
        val horizontalPadding = 24.dpToPx()
        setPadding(horizontalPadding, 0, horizontalPadding, 0)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun LeapButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(48.dp),
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    height: Dp? = 58.dp,
    content: @Composable RowScope.() -> Unit,
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) { // Disable minimum interactive component size enforcement
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            colors = colors,
            elevation = elevation,
            border = border,
            contentPadding = contentPadding,
            interactionSource = interactionSource,
            content = content,
            modifier = modifier.let {
                if (height != null) it.height(height) else it
            }
        )
    }
}

@Composable
public fun LeapOutlinedButton(
    onClick: () -> Unit,
    outlineColor: Color,
    modifier: Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    LeapButton(
        colors = ButtonDefaults.outlinedButtonColors(contentColor = outlineColor),
        border = BorderStroke(1.dp, outlineColor),
        onClick = onClick,
        content = content,
        modifier = modifier,
    )
}
