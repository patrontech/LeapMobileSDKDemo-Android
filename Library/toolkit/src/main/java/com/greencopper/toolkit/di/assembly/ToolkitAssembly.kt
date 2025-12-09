package com.greencopper.toolkit.di.assembly

import android.content.Context
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.binding.*
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.net.ConcreteNetworkMonitor
import com.greencopper.toolkit.net.NetworkMonitor
import com.greencopper.toolkit.serialization.JsonFactory
import com.greencopper.toolkit.storage.InternalStorageManager
import com.greencopper.toolkit.storage.StorageManager
import com.greencopper.toolkit.testing.TemporaryStorageManager
import com.greencopper.toolkit.versionprovider.BuildConfigProvider
import com.greencopper.toolkit.versionprovider.ConcreteBuildConfigProvider
import com.greencopper.toolkit.zip.Zip4jClient
import com.greencopper.toolkit.zip.ZipClient
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

internal class ToolkitAssembly(private val context: Context?) : Assembly {

    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            if (context != null) {
                bindProvider<Context> { context.applicationContext }
                bindProvider<StorageManager> { InternalStorageManager(context, App.log) }
                bindSingleton<NetworkMonitor> { ConcreteNetworkMonitor(context) }
                bindSingleton<BuildConfigProvider> { ConcreteBuildConfigProvider(context) }
            } else {
                // If we don't have a context, we can assume we're outside of the Android env
                // and we're probably Unit testing.
                bindProvider<StorageManager> { TemporaryStorageManager() }
            }
            bindSingleton<OkHttpClient> {
                val buildConfigProvider: BuildConfigProvider = resolve()
                if (buildConfigProvider.isDebug) {
                    OkHttpClient()
                        .newBuilder()
                        .addInterceptor(HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BASIC) })
                        .build()
                } else {
                    OkHttpClient()
                }
            }
            bindSingleton<Retrofit> {
                Retrofit.Builder()
                    .baseUrl("https://localhost/")
                    .addConverterFactory(resolve<Json>().asConverterFactory("application/json".toMediaType()))
                    .client(resolve())
                    .build()
            }
            bindProvider<ZipClient> { Zip4jClient(CoroutineScope(Dispatchers.IO + SupervisorJob())) }

            bindSingleton { JsonFactory.create() }

            bindSingleton { App.log }
        }
    }
}
