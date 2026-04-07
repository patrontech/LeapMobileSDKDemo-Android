package com.example.kibasdkpoc.webview

/**
 * Centralizes authentication-related URLs.
 */
internal object UrlProvider {
    const val AUTH_URL =
        "https://id.fanatics.com/oauth2/auth?scope=openid&response_type=code&client_id=ficiXzvv9_V95JuAry4yhaKOMHhKG7bXVaoy1335PqOACW24"

    const val REDIRECT_URI = "https://fanatics-one.com/"

    const val REDIRECT_DOMAIN = "fanatics-one.com"

    val authUrlWithRedirect: String
        get() = "$AUTH_URL&redirect_uri=$REDIRECT_URI"
}