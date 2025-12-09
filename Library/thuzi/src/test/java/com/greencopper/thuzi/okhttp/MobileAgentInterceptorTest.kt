package com.greencopper.thuzi.okhttp

import io.mockk.*
import okhttp3.*
import org.junit.jupiter.api.Test

internal class MobileAgentInterceptorTest {
    @Test
    fun intercept_shouldAddHeader() {
        val mobileAgent = MobileAgent(
            versionName = "1.0.0",
            packageId = "com.example.app",
            contentVersion = { "123" },
            osVersion = "12",
            libraryVersion = { "1.52.0" },
        )

        val chain = mockk<Interceptor.Chain>()
        val originalRequest = Request.Builder()
            .url("https://example.com")
            .build()

        every { chain.request() } returns originalRequest
        every { chain.proceed(any()) } returns mockk<Response>()

        val interceptor = MobileAgentInterceptor(mobileAgent)
        interceptor.intercept(chain)

        verify {
            chain.proceed(withArg { it.headers.contains("X-Mobile-Agent" to mobileAgent.toString()) })
        }
    }
}
