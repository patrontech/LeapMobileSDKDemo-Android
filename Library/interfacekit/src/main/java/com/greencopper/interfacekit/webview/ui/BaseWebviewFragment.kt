package com.greencopper.interfacekit.webview.ui

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.*
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.os.Message
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.webkit.*
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.databinding.WebviewFragmentBinding
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import com.greencopper.interfacekit.ui.viewBinding
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.DefaultBackCloseToolbarNavigationControlsHandler
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.NavigationControlsHandler
import com.greencopper.interfacekit.viewModel
import com.greencopper.interfacekit.webview.*
import com.greencopper.interfacekit.webview.data.WebViewBaseData
import com.greencopper.interfacekit.webview.data.WebViewData
import com.greencopper.interfacekit.webview.data.WebViewNewWindowMode
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.e
import com.greencopper.toolkit.logging.w

public abstract class BaseWebViewFragment<T : WebViewBaseData<T>>(webViewBaseData: T?) :
    ParameterizedFragment<T>(webViewBaseData) {

    override val binding: WebviewFragmentBinding by viewBinding(WebviewFragmentBinding::inflate)
    override val screenColor: ScreenColor get() = InterfaceKitColor.webView

    private val viewModel: BaseWebViewViewModel by viewModel()

    protected var lastWebviewState: Bundle? = null

    protected open val loadUrl: Boolean = true

    /**
     * Attachment download complete receiver.
     *
     * 1. Receiver gets called once attachment download completed.
     * 2. Open the downloaded file.
     */
    private val attachmentDownloadCompleteReceive: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
                val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
                viewModel.openDownloadedAttachment(context, downloadId) {
                    activity?.onBackPressed()
                }
            }
        }
    }

    override fun createNavigationControlsHandler(): NavigationControlsHandler? =
        DefaultBackCloseToolbarNavigationControlsHandler(
            this,
            binding.simpleToolbar,
            InterfaceKitColor.widgetCollection.topBar,
            InterfaceKitTextStyle.webView.topBar,
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (viewModel.isDebugBuild) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        lastWebviewState = savedInstanceState?.getBundle(SAVED_WEBVIEW_STATE_KEY)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.setBackgroundColor(InterfaceKitColor.webView.background)

        binding.webview.apply {
            setBackgroundColor(InterfaceKitColor.webView.background)
            webViewClient = getCustomWebViewClient()
            webChromeClient = getCustomWebChromeClient()

            with(settings) {
                disabledActionModeMenuItems = WebSettings.MENU_ITEM_SHARE or WebSettings.MENU_ITEM_WEB_SEARCH
                javaScriptEnabled = true
                domStorageEnabled = true
                builtInZoomControls = true
                useWideViewPort = true
                loadWithOverviewMode = true

                setSupportMultipleWindows(true)
            }

            setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == MotionEvent.ACTION_UP && canGoBack()) {
                    goBack()
                    true
                } else {
                    false
                }
            }

            setDownloadListener { url, _, _, mimeType, _ ->
                try {
                    if (url.startsWith("data:")) {
                        throw IllegalArgumentException("\"data:\" URI are not supported for download")
                    } else {
                        viewModel.downloadFile(context, data.url, url, mimeType)
                    }
                } catch (t: Throwable) {
                    App.log.e("Couldn't download $url", throwable = t)
                }
            }

            lastWebviewState?.let {
                restoreState(it)
            } ?: run {
                if (loadUrl) {
                    if (manageUrl(data.url)) {
                        activity?.onBackPressedDispatcher?.onBackPressed()
                    } else {
                        loadUrl(data.url)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.webview.onResume()
        binding.webview.resumeTimers()

        activity?.registerReceiver(
            attachmentDownloadCompleteReceive,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            Context.RECEIVER_NOT_EXPORTED,
        )
    }

    override fun onPause() {
        super.onPause()
        binding.webview.onPause()
        binding.webview.pauseTimers()

        activity?.unregisterReceiver(attachmentDownloadCompleteReceive)
    }

    override fun onStop() {
        super.onStop()
        val webviewState = Bundle()
        binding.webview.saveState(webviewState)
        lastWebviewState = webviewState
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(SAVED_WEBVIEW_STATE_KEY, lastWebviewState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        CookieManager.getInstance().removeSessionCookies(null)
        binding.webview.destroy()
    }

    /**
     * Leave as protected access for KIBA-App customs.
     *
     * @return true if the URL is managed and it should not be passed back to the webview, otherwise false
     */
    protected fun manageUrl(url: String): Boolean {
        val context = context ?: return false

        when {
            url.startsWith(context.resources.getString(R.string.deeplink_scheme)) -> startBrowserActivity(context, url)
            url.startsWith("file:///") -> return false
            !url.startsWith("https://") -> startBrowserActivity(context, url)
            url.endsWith(".pdf") -> startBrowserActivity(context, url)
            else -> return false
        }
        return true
    }

    protected open fun getCustomWebViewClient(): WebViewClient = CustomWebViewClient()
    protected open fun getCustomWebChromeClient(): WebChromeClient = CustomWebChromeClient()

    protected open inner class CustomWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            viewModel.webviewClientListeners.shouldOverrideUrlLoading(view, request)
            return manageUrl(request.url.toString())
        }

        override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
            viewModel.webviewClientListeners.doUpdateVisitedHistory(view, url, isReload)
            super.doUpdateVisitedHistory(view, url, isReload)
        }

        override fun onFormResubmission(
            view: WebView,
            dontResend: Message?,
            resend: Message?,
        ) {
            viewModel.webviewClientListeners.onFormResubmission(view, dontResend, resend)
            super.onFormResubmission(view, dontResend, resend)
        }

        override fun onLoadResource(view: WebView, url: String?) {
            viewModel.webviewClientListeners.onLoadResource(view, url)
            super.onLoadResource(view, url)
        }

        override fun onPageCommitVisible(view: WebView, url: String?) {
            viewModel.webviewClientListeners.onPageCommitVisible(view, url)
            super.onPageCommitVisible(view, url)
        }

        override fun onPageFinished(view: WebView, url: String?) {
            viewModel.webviewClientListeners.onPageFinished(view, url)
            super.onPageFinished(view, url)
        }

        override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
            viewModel.webviewClientListeners.onPageStarted(view, url, favicon)
            super.onPageStarted(view, url, favicon)
        }

        override fun onReceivedClientCertRequest(view: WebView, request: ClientCertRequest?) {
            viewModel.webviewClientListeners.onReceivedClientCertRequest(view, request)
            super.onReceivedClientCertRequest(view, request)
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest?, error: WebResourceError?) {
            viewModel.webviewClientListeners.onReceivedError(view, request, error)
            super.onReceivedError(view, request, error)
        }

        override fun onReceivedHttpAuthRequest(view: WebView, handler: HttpAuthHandler?, host: String?, realm: String?) {
            viewModel.webviewClientListeners.onReceivedHttpAuthRequest(view, handler, host, realm)
            super.onReceivedHttpAuthRequest(view, handler, host, realm)
        }

        override fun onReceivedHttpError(view: WebView, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
            viewModel.webviewClientListeners.onReceivedHttpError(view, request, errorResponse)
            super.onReceivedHttpError(view, request, errorResponse)
        }

        override fun onReceivedLoginRequest(view: WebView, realm: String?, account: String?, args: String?) {
            viewModel.webviewClientListeners.onReceivedLoginRequest(view, realm, account, args)
            super.onReceivedLoginRequest(view, realm, account, args)
        }

        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler?, error: SslError?) {
            viewModel.webviewClientListeners.onReceivedSslError(view, handler, error)
            super.onReceivedSslError(view, handler, error)
        }

        override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail?): Boolean {
            viewModel.webviewClientListeners.onRenderProcessGone(view, detail)
            return super.onRenderProcessGone(view, detail)
        }

        override fun onSafeBrowsingHit(
            view: WebView,
            request: WebResourceRequest?,
            threatType: Int,
            callback: SafeBrowsingResponse?,
        ) {
            viewModel.webviewClientListeners.onSafeBrowsingHit(view, request, threatType, callback)
            super.onSafeBrowsingHit(view, request, threatType, callback)
        }

        override fun onScaleChanged(view: WebView, oldScale: Float, newScale: Float) {
            viewModel.webviewClientListeners.onScaleChanged(view, oldScale, newScale)
            super.onScaleChanged(view, oldScale, newScale)
        }

        override fun onUnhandledKeyEvent(view: WebView, event: KeyEvent?) {
            viewModel.webviewClientListeners.onUnhandledKeyEvent(view, event)
            super.onUnhandledKeyEvent(view, event)
        }

        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
            viewModel.webviewClientListeners.shouldInterceptRequest(view, request)
            return super.shouldInterceptRequest(view, request)
        }

        override fun shouldOverrideKeyEvent(view: WebView, event: KeyEvent?): Boolean {
            viewModel.webviewClientListeners.shouldOverrideKeyEvent(view, event)
            return super.shouldOverrideKeyEvent(view, event)
        }
    }

    protected open inner class CustomWebChromeClient : WebChromeClient() {
        override fun onCreateWindow(
            view: WebView,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message?,
        ): Boolean {
            val webViewData = data as? WebViewLayoutData
            val newUrl = view.hitTestResult.extra ?: ""

            if (
                (webViewData?.newWindowMode == WebViewNewWindowMode.Push ||
                        webViewData?.newWindowMode == WebViewNewWindowMode.Present) &&
                newUrl.startsWith("https")
            ) {
                openNewWebview(newUrl, webViewData)
            } else {
                openExternalBrowser(view, resultMsg)
            }

            return true
        }

        private fun openNewWebview(newUrl: String, webViewData: WebViewLayoutData) {
            val featureParams = WebViewData(
                url = newUrl,
                newWindowMode = webViewData.newWindowMode,
                analytics = ScreenNameAnalytics("$newUrl from ${webViewData.analytics.screenName}")
            )
            val featureInfo = FeatureInfo(
                key = WebviewInitializer.key,
                params = featureParams.encodeToJsonElement()
            )
            val route = if (webViewData.newWindowMode == WebViewNewWindowMode.Push)
                Route.Push(featureInfo) else Route.Present(featureInfo)
            viewModel.redirectToRoute(route, this@BaseWebViewFragment)
        }

        override fun getDefaultVideoPoster(): Bitmap? {
            return if (super.getDefaultVideoPoster() == null) {
                // Use an empty image
                createBitmap(50, 50)
            } else {
                super.getDefaultVideoPoster()
            }
        }

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            if (isAdded && this@BaseWebViewFragment.view != null) {
                binding.progressIndicator.setProgressCompat(newProgress, true)
                if (newProgress == 100) {
                    binding.progressIndicator.hide()
                } else {
                    binding.progressIndicator.show()
                }
            }
        }

        override fun onGeolocationPermissionsShowPrompt(
            origin: String,
            callback: GeolocationPermissions.Callback,
        ) {
            viewModel.showGeoLocationPermissionPrompt(this@BaseWebViewFragment, origin, callback)
        }

        override fun onPermissionRequest(request: PermissionRequest) {
            viewModel.showPermissionRequest(this@BaseWebViewFragment, request)
        }

        private fun openExternalBrowser(view: WebView, resultMsg: Message?) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, view.hitTestResult.extra?.toUri())
                view.context.startActivity(browserIntent)
            } catch (throwable: Throwable) {
                val temporaryWebView = WebView(view.context)
                temporaryWebView.webViewClient = object : WebViewClient() {
                    override fun onPageStarted(webView: WebView, url: String?, favicon: Bitmap?) {
                        try {
                            url?.let {
                                val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
                                view.context.startActivity(browserIntent)
                            }
                        } catch (throwable: Throwable) {
                            App.log.e("Failed to parse external url", throwable = throwable)
                        }
                        super.onPageStarted(view, url, favicon)
                    }
                }
                (resultMsg?.obj as? WebView.WebViewTransport?)?.webView = temporaryWebView
                resultMsg?.sendToTarget()
            }
        }
    }

    private companion object {
        const val SAVED_WEBVIEW_STATE_KEY = "savedWebviewStateKey"

        fun startBrowserActivity(context: Context, data: String) {
            val browserIntent = Intent(Intent.ACTION_VIEW, data.toUri())
            try {
                context.startActivity(browserIntent)
            } catch (ex: ActivityNotFoundException) {
                App.log.w("Couldn't find activity for $data")
            }
        }
    }
}
