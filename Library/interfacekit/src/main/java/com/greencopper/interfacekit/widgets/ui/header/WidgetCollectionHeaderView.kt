package com.greencopper.interfacekit.widgets.ui.header

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.greencopper.core.localization.service.getString
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.databinding.WidgetCollectionHeaderBinding
import com.greencopper.interfacekit.ui.compose.*
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration.Instance.HeaderInfo
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy

internal class WidgetCollectionHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {
    internal var binding: WidgetCollectionHeaderBinding =
        WidgetCollectionHeaderBinding.inflate(LayoutInflater.from(context), this)

    private val viewBuilder: IKViewBuilder by App.lazy()

    internal var hasCornerRadius = false
        private set

    fun setup(data: HeaderInfo) {
        hasCornerRadius = data.cornerRadius == null || data.cornerRadius > 0

        binding.composeView.setContent {
            viewBuilder.buildContent {
                ImageHeader(data = data)
            }
        }
    }

    @Composable
    private fun ImageHeader(data: HeaderInfo) {
        val cornerRadius = data.cornerRadius?.dp ?: dimensionResource(id = R.dimen.default_rounder_corner_radius)
        val description = LocalLocalizationAccess.current.getString(data.accessibilityLabel)
        val modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(data.ratio)
            .run {
                val shape = RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius)
                if (data.shadow == false) {
                    clip(shape)
                } else {
                    shadow(
                        elevation = 4.dp,
                        shape = shape,
                    )
                }
            }
            .semantics { contentDescription = description }
        Image(
            painter = rememberAsyncImagePainter(imageName = data.imageName),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}
