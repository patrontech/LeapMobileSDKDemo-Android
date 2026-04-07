package com.example.kibasdkpoc.webview

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.example.kibasdkpoc.BuildConfig
import com.example.kibasdkpoc.databinding.WebviewBinding

public class WebViewActivity : ComponentActivity() {
    private val binding by lazy { WebviewBinding.inflate(layoutInflater) }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val webView = binding.webview
        val progressBar = binding.progressIndicator
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        webView.webViewClient = AuthWebViewClient(
            redirectDomain = UrlProvider.REDIRECT_DOMAIN,
            onRedirectDetected = { finish() },
        )
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

        webView.webChromeClient = AuthWebChromeClient(progressIndicator = progressBar)

        // Enable cookie sharing with LeapMobileSDK
        // If LeapMobileSDK uses WebView, cookies will be shared via CookieManager
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)
        cookieManager.flush()

        webView.loadUrl(UrlProvider.authUrlWithRedirect)
    }
}
