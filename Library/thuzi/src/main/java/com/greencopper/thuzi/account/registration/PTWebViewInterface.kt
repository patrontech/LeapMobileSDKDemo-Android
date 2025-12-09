package com.greencopper.thuzi.account.registration

import android.webkit.JavascriptInterface
import com.greencopper.core.data.KiboSerializable
import com.greencopper.thuzi.account.registration.model.RegistrationResponse

internal class PTWebViewInterface(private val callback: (response: RegistrationResponse) -> Unit) {
    @JavascriptInterface
    fun postMessage(jsonDataObjAsStr: String) {
        callback.invoke(
            KiboSerializable.decodeFromString(jsonDataObjAsStr)
        )
    }
}
