package com.example.andespace.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.andespace.data.repository.AnalyticsEventQueue
import com.example.andespace.data.repository.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object NetworkMonitor {

    private const val TAG = "NetworkMonitor"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var registered = false
    private var flushing = false

    fun register(context: Context) {
        if (registered) return
        registered = true

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (AnalyticsEventQueue.isEmpty || flushing) return
                flushing = true
                Log.d(TAG, "Network available, flushing analytics queue")
                scope.launch {
                    try {
                        AppRepository(context.applicationContext).flushPendingAnalytics()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error flushing analytics: ${e.message}", e)
                    } finally {
                        flushing = false
                    }
                }
            }
        })
    }
}
