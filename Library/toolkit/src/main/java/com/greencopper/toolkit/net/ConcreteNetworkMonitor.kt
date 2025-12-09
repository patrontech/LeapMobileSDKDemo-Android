package com.greencopper.toolkit.net

import android.content.Context
import android.net.*
import android.net.ConnectivityManager.NetworkCallback
import android.os.Build
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Just because this reports that we have internet connectivity does
 * not mean that a request will be successful. That depends on many
 * other factors, such as whether the server at the other end is
 * up and functioning, etc.
 */
internal class ConcreteNetworkMonitor(context: Context): NetworkMonitor {
    // On a mobile phone, having connectivity should be assumed as
    // the default.
    private val mutableConnectedFlow = MutableStateFlow(true)
    override val connectedFlow: Flow<Boolean> = mutableConnectedFlow

    init {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        manager.registerNetworkCallback(request, object: NetworkCallback() {
            /*
             The methods below are called when a network becomes available
             or unavailable. A typical cell phone has two networks: wifi
             and cellular. Just because "onLost" or "onAvailable" is called
             doesn't mean that we have (lost) connectivity. We have to
             check explicitly. The easiest way to do that is to ping
             Google's 8.8.8.8 DNS server.
             */

            override fun onAvailable(network: Network) {
                checkConnectivity()
            }

            override fun onLost(network: Network) {
                checkConnectivity()
            }
        })
    }

    @Suppress("DEPRECATION")
    private fun checkConnectivity() {
        val socket = Socket()
        // This is the address of Google's primary DNS server for its
        // Google Public DNS. (See https://en.wikipedia.org/wiki/Google_Public_DNS.)
        // It's been around since 2009 and handles over a trillion queries per day.
        // It is very commonly used for the purpose of checking connectivity.
        val address = InetSocketAddress("8.8.8.8", 53)
        try {
            val threadId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                Thread.currentThread().threadId().toInt()
            } else {
                Thread.currentThread().id.toInt()
            }

            TrafficStats.setThreadStatsTag(threadId)
            TrafficStats.tagSocket(socket)
            socket.connect(address, 1000)
            mutableConnectedFlow.value = true
        } catch (_: Exception) {
            mutableConnectedFlow.value = false
        } finally {
            socket.close()
        }
    }
}
