package com.greencopper.interfacekit.ui.views.navigationcontrols

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.TopBarColor
import com.greencopper.interfacekit.color.TopBarColorComposable
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.databinding.NavigateFabButtonBinding

public class NavigateCloseButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    public val binding: NavigateFabButtonBinding =
        NavigateFabButtonBinding.inflate(LayoutInflater.from(context), this)

    public fun setupButton(buttonColor: TopBarColor, onClick: OnClickListener) {
        binding.navigateFab.apply {
            contentDescription = resources.getString(R.string.close)
            setImageResource(R.drawable.ic_close)
            backgroundTintList = ColorStateList.valueOf(buttonColor.background)
            setColorFilter(buttonColor.item)
            setOnSafeClickListener { onClick.onClick(it) }
        }
    }
}

@Composable
public fun NavigateCloseButton(
    buttonColor: TopBarColorComposable,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = buttonColor.background,
        contentColor = buttonColor.item,
        shape = CircleShape,
        elevation = FloatingActionButtonDefaults.elevation(4.dp),
        content = {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = stringResource(R.string.close)
            )
        },
        modifier = modifier.size(42.dp)
    )
}
