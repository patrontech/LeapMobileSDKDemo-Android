package com.greencopper.interfacekit.webview.data

import com.greencopper.core.data.KiboSerializable

public interface WebViewBaseData<T: WebViewBaseData<T>>: KiboSerializable<T> {
    public val url: String
}
