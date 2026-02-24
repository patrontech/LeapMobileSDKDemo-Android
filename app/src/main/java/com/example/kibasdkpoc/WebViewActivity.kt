package com.example.kibasdkpoc

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.example.kibasdkpoc.databinding.WebviewBinding

private const val URL = "$FANATICS_URL&redirect_uri=https://fanatics-one.com/"

public class WebViewActivity : ComponentActivity() {
    private val binding by lazy { WebviewBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val webView = binding.webview
        val progressBar = binding.progressIndicator
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()
                Log.d("WebViewActivity", "Intercepted navigation to: $url")
                // Return true to block, false to allow navigation
                // For example, block redirects to a specific domain:
                if (url.contains("fanatics-one.com")) {
                    finish()
                    return true // Block navigation
                }
                return false // Allow navigation
            }
        }
        webView.settings.apply {
            layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
            loadWithOverviewMode = true
            useWideViewPort = true
            domStorageEnabled = true
            javaScriptEnabled = true
            allowContentAccess = false
            allowFileAccess = false
            setSupportMultipleWindows(true)
            javaScriptCanOpenWindowsAutomatically = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            loadsImagesAutomatically = true //default value but should declare too
            setSupportZoom(false) //disable to load scale to window size, but disables zoom :(
            cacheMode = WebSettings.LOAD_NO_CACHE
        }

        if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_ENABLE)) {
            WebSettingsCompat.setSafeBrowsingEnabled(webView.settings, true)
        }

        if (WebViewFeature.isFeatureSupported(WebViewFeature.OFF_SCREEN_PRERASTER)) {
            WebSettingsCompat.setOffscreenPreRaster(webView.settings, true)
        }

//      Left this commented out since it opens a new WebView in a dialog which is not needed for our use case, but leaving here for reference if we want to support links that open in new windows in the future
//        webView.webChromeClient = object : android.webkit.WebChromeClient() {
//            override fun onCreateWindow(
//                view: WebView?,
//                isDialog: Boolean,
//                isUserGesture: Boolean,
//                resultMsg: android.os.Message?
//            ): Boolean {
//                val newWebView = WebView(this@WebViewActivity)
//                newWebView.settings.javaScriptEnabled = true
//                newWebView.settings.domStorageEnabled = true
//                newWebView.webViewClient = WebViewClient()
//                val dialog = android.app.Dialog(this@WebViewActivity)
//                dialog.setContentView(newWebView)
//                dialog.show()
//                val transport = resultMsg?.obj as? WebView.WebViewTransport
//                transport?.webView = newWebView
//                resultMsg?.sendToTarget()
//                return true
//            }
//        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                if (newProgress == 100) {
                    Log.d("WebViewActivity", "Page loaded, hiding progress bar")
                    progressBar.visibility = View.GONE
                } else {
                    Log.d("WebViewActivity", "Loading progress: $newProgress%")
                    progressBar.visibility = View.VISIBLE
                }
            }
        }

        // Enable cookie sharing with LeapMobileSDK
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)
        // If LeapMobileSDK uses WebView, cookies will be shared via CookieManager

        webView.loadUrl(URL)
    }
}