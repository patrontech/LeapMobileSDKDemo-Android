package com.greencopper.thuzi.okhttp

import okhttp3.Interceptor

public class MobileAgentInterceptor(private val mobileAgent: MobileAgent) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        val newRequest = request.newBuilder()
                .header("X-Mobile-Agent", mobileAgent.toString())
                .build()

        return chain.proceed(newRequest)
    }
}

public data class MobileAgent(
    val versionName: String,
    val packageId: String,
    val contentVersion: () -> String,
    val osVersion: String,
    val libraryVersion: () -> String,
) {
    override fun toString(): String {
        return "app-version=${versionName}; app-id=${packageId}; content-version=${contentVersion()}; platform=android; os-version=${osVersion}; library-version=${libraryVersion()}"
    }
}
