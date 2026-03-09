package com.example.kibasdkpoc.webview

import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * WebViewClient for the authentication flow.
 * Intercepts navigation and invokes [onRedirectDetected] when the user
 * is redirected to the success domain.
 */
internal class AuthWebViewClient(
    private val redirectDomain: String,
    private val onRedirectDetected: () -> Unit,
) : WebViewClient() {

    private companion object {
        const val TAG = "AuthWebViewClient"
    }

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?,
    ): Boolean {
        val url = request?.url.toString()

        Log.d(TAG, "Intercepted navigation to: $url")

        if (url.contains(redirectDomain)) {
            onRedirectDetected()
            return true
        }

        return false
    }
}
