package com.greencopper.interfacekit.ui

import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.util.TypedValue
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.greencopper.core.asset.recipe.Asset
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.services.localizationService
import com.greencopper.interfacekit.imageservice.ImageResult
import com.greencopper.interfacekit.imageservice.ImageResult.READY
import com.greencopper.interfacekit.imageservice.ImageResult.UNKNOWN
import com.greencopper.interfacekit.imageservice.ImageService
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

public fun ImageView.resetImageFrom(
    name: String?,
    lifecycleScope: CoroutineScope,
    hideIfUnknown: Boolean = false,
    hideIfLoading: Boolean = false,
    format: Asset.Format.Name? = null,
    onResult: (ImageResult) -> Drawable? = { it.drawable },
): Job? {
    return if (tag == null || tag != name.toString() || drawable == null) {
        setImageDrawable(null)
        setImageFrom(name, lifecycleScope, hideIfUnknown, hideIfLoading, format, onResult)
    } else {
        null
    }
}

public fun ImageView.setImageFrom(
    name: String?,
    lifecycleScope: CoroutineScope,
    hideIfUnknown: Boolean = false,
    hideIfLoading: Boolean = false,
    format: Asset.Format.Name? = null,
    onResult: (ImageResult) -> Drawable? = { it.drawable },
): Job {
    return App.resolve<ImageService>().getImageDrawable(name, hideIfUnknown, hideIfLoading, format)
        .flowOn(Dispatchers.IO)
        .onEach {
            if (it is READY || it is UNKNOWN) {
                tag = name.toString()
            }
            isVisible = !(it is UNKNOWN && hideIfUnknown)

            (it.drawable as? AnimatedImageDrawable)?.start()

            setImageDrawable(onResult(it))
        }
        .launchIn(lifecycleScope)
}

public fun TextView.setTextOrGone(charSequence: CharSequence?) {
    charSequence?.let {
        text = charSequence
        visibility = VISIBLE
    } ?: run {
        visibility = GONE
    }
}

public fun TextView.setTextOrGone(string: String?) {
    string?.let {
        text = string
        visibility = VISIBLE
    } ?: run {
        visibility = GONE
    }
}

public fun TextView.setOtaText(key: String?) {
    key?.let {
        text = App.localizationService().getString(it)
    }
}

public fun TextView.setOtaTextOrGone(localizationService: LocalizationService, key: String?) {
    key?.let {
        text = localizationService.getString(it)
        visibility = VISIBLE
    } ?: run {
        text = ""
        visibility = GONE
    }
}

public fun TextView.setOtaHtmlTextOrGone(localizationService: LocalizationService, key: String?) {
    key?.let {
        text = Html.fromHtml(localizationService.getString(it), Html.FROM_HTML_MODE_LEGACY).trim()
        visibility = VISIBLE
    } ?: run {
        text = ""
        visibility = GONE
    }
}

public fun TextView.setOverFlow(minTextSize: Int, maxTextSize: Int) {
    if (lineCount > maxLines) {
        setAutoSizeTextTypeUniformWithConfiguration(
            minTextSize,
            maxTextSize,
            1,
            TypedValue.COMPLEX_UNIT_SP
        )

        maxLines = lineCount
    }
}
