package com.greencopper.interfacekit.webview.ui

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView

public class SafeWebView: WebView {
    public constructor(context: Context) : super(context)
    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun loadUrl(url: String) {
        super.loadUrl(url.replace("http:", "https:"))
    }
}