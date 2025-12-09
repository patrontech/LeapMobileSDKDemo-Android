package com.greencopper.interfacekit.webview

import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Message
import android.view.KeyEvent
import android.webkit.*

public interface WebViewClientListener {
    public fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) {}

    public fun shouldOverrideKeyEvent(view: WebView, event: KeyEvent?) {}

    public fun shouldInterceptRequest(view: WebView, request: WebResourceRequest) {}

    public fun onUnhandledKeyEvent(view: WebView, event: KeyEvent?) {}

    public fun onScaleChanged(view: WebView, oldScale: Float, newScale: Float) {}

    public fun onSafeBrowsingHit(
        view: WebView,
        request: WebResourceRequest?,
        threatType: Int,
        callback: SafeBrowsingResponse?,
    ) {
    }

    public fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail?) {}

    public fun onReceivedSslError(view: WebView, handler: SslErrorHandler?, error: SslError?) {}

    public fun onReceivedLoginRequest(view: WebView, realm: String?, account: String?, args: String?) {}

    public fun onReceivedHttpError(view: WebView, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {}

    public fun onReceivedHttpAuthRequest(view: WebView, handler: HttpAuthHandler?, host: String?, realm: String?) {}

    public fun onReceivedError(view: WebView, request: WebResourceRequest?, error: WebResourceError?) {}

    public fun onReceivedClientCertRequest(view: WebView, request: ClientCertRequest?) {}

    public fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {}

    public fun onPageFinished(view: WebView, url: String?) {}

    public fun onPageCommitVisible(view: WebView, url: String?) {}

    public fun onLoadResource(view: WebView, url: String?) {}

    public fun onFormResubmission(view: WebView, dontResend: Message?, resend: Message?) {}

    public fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {}

}

public fun Iterable<WebViewClientListener>.shouldOverrideUrlLoading(
    view: WebView,
    request: WebResourceRequest,
): Unit = forEach { it.shouldOverrideUrlLoading(view, request) }

public fun Iterable<WebViewClientListener>.shouldOverrideKeyEvent(
    view: WebView,
    event: KeyEvent?,
): Unit = forEach { it.shouldOverrideKeyEvent(view, event) }

public fun Iterable<WebViewClientListener>.shouldInterceptRequest(
    view: WebView,
    request: WebResourceRequest,
): Unit = forEach { it.shouldInterceptRequest(view, request) }

public fun Iterable<WebViewClientListener>.onUnhandledKeyEvent(
    view: WebView,
    event: KeyEvent?,
): Unit = forEach { it.onUnhandledKeyEvent(view, event) }

public fun Iterable<WebViewClientListener>.onScaleChanged(
    view: WebView,
    oldScale: Float,
    newScale: Float,
): Unit = forEach { it.onScaleChanged(view, oldScale, newScale) }

public fun Iterable<WebViewClientListener>.onSafeBrowsingHit(
    view: WebView,
    request: WebResourceRequest?,
    threatType: Int,
    callback: SafeBrowsingResponse?,
): Unit = forEach { it.onSafeBrowsingHit(view, request, threatType, callback) }

public fun Iterable<WebViewClientListener>.onRenderProcessGone(
    view: WebView,
    detail: RenderProcessGoneDetail?,
): Unit = forEach { it.onRenderProcessGone(view, detail) }

public fun Iterable<WebViewClientListener>.onReceivedSslError(
    view: WebView,
    handler: SslErrorHandler?,
    error: SslError?,
): Unit = forEach { it.onReceivedSslError(view, handler, error) }

public fun Iterable<WebViewClientListener>.onReceivedLoginRequest(
    view: WebView,
    realm: String?,
    account: String?,
    args: String?,
): Unit = forEach { it.onReceivedLoginRequest(view, realm, account, args) }

public fun Iterable<WebViewClientListener>.onReceivedHttpError(
    view: WebView,
    request: WebResourceRequest?,
    errorResponse: WebResourceResponse?,
): Unit = forEach { it.onReceivedHttpError(view, request, errorResponse) }

public fun Iterable<WebViewClientListener>.onReceivedHttpAuthRequest(
    view: WebView,
    handler: HttpAuthHandler?,
    host: String?,
    realm: String?,
): Unit = forEach { it.onReceivedHttpAuthRequest(view, handler, host, realm) }

public fun Iterable<WebViewClientListener>.onReceivedError(
    view: WebView,
    request: WebResourceRequest?,
    error: WebResourceError?,
): Unit = forEach { it.onReceivedError(view, request, error) }

public fun Iterable<WebViewClientListener>.onReceivedClientCertRequest(
    view: WebView,
    request: ClientCertRequest?,
): Unit = forEach { it.onReceivedClientCertRequest(view, request) }

public fun Iterable<WebViewClientListener>.onPageStarted(
    view: WebView,
    url: String?,
    favicon: Bitmap?,
): Unit = forEach { it.onPageStarted(view, url, favicon) }

public fun Iterable<WebViewClientListener>.onPageFinished(
    view: WebView,
    url: String?,
): Unit = forEach { it.onPageFinished(view, url) }

public fun Iterable<WebViewClientListener>.onPageCommitVisible(
    view: WebView,
    url: String?,
): Unit = forEach { it.onPageCommitVisible(view, url) }

public fun Iterable<WebViewClientListener>.onLoadResource(
    view: WebView,
    url: String?,
): Unit = forEach { it.onLoadResource(view, url) }

public fun Iterable<WebViewClientListener>.onFormResubmission(
    view: WebView,
    dontResend: Message?,
    resend: Message?,
): Unit = forEach { it.onFormResubmission(view, dontResend, resend) }

public fun Iterable<WebViewClientListener>.doUpdateVisitedHistory(
    view: WebView,
    url: String?,
    isReload: Boolean,
): Unit = forEach { it.doUpdateVisitedHistory(view, url, isReload) }
