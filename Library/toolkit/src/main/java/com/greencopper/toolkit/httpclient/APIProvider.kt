package com.greencopper.toolkit.httpclient

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.time.Duration

public interface APIProvider<API: Any> {
    public fun api(timeout: Duration? = null): API
}

public class ConcreteAPIProvider<API : Any>(
    private val okHttpClient: OkHttpClient,
    private val retrofit: Retrofit,
    private val apiClass: Class<API>,
) : APIProvider<API> {

    private val singletonAPI: API by lazy { retrofit.create(apiClass) }

    public override fun api(timeout: Duration?): API {
        return timeout?.let {
            val client = okHttpClient.newBuilder().callTimeout(timeout).build()
            val retrofit = retrofit.newBuilder().client(client).build()
            retrofit.create(apiClass)
        } ?: singletonAPI
    }
}
