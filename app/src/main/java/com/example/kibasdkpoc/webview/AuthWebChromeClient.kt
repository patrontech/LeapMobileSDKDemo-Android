package com.example.kibasdkpoc.webview

import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.google.android.material.progressindicator.LinearProgressIndicator

/**
 * WebChromeClient for the authentication WebView.
 * Updates the progress indicator visibility based on page load progress.
 */
internal class AuthWebChromeClient(
    private val progressIndicator: LinearProgressIndicator,
) : WebChromeClient() {

    private companion object {
        const val TAG = "AuthWebChromeClient"
    }

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        progressIndicator.progress = newProgress
        if (newProgress == 100) {
            Log.d(TAG, "Page loaded, hiding progress bar")
            progressIndicator.visibility = View.GONE
        } else {
            Log.d(TAG, "Loading progress: $newProgress%")
            progressIndicator.visibility = View.VISIBLE
        }
    }
}
