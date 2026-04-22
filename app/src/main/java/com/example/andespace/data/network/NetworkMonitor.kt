package com.example.andespace.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.HttpURLConnection
import java.net.URL

object NetworkMonitor {
    private const val TAG = "NetworkMonitor"
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var currentNetwork: Network? = null
    private val monitorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pingUrl: String = ""

    private var pingJob: Job? = null

    fun register(context: Context, baseUrl: String) {
        pingUrl = baseUrl
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (networkCallback != null) return

        currentNetwork = connectivityManager.activeNetwork
        if (currentNetwork != null) {
            checkHealthWithRetries(currentNetwork!!)
        } else {
            _isOnline.value = false
        }

        networkCallback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                Log.d(TAG, "onAvailable: OS found a new network. Verifying...")
                currentNetwork = network
                checkHealthWithRetries(network)
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

                if (hasInternet && !_isOnline.value && currentNetwork == network) {
                    if (pingJob?.isActive != true) {
                        Log.d(TAG, "onCapabilitiesChanged: Internet capability restored. Verifying...")
                        checkHealthWithRetries(network)
                    }
                }
            }
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback!!)
    }

    private fun checkHealthWithRetries(networkToTest: Network) {
        if (pingUrl.isEmpty()) return

        pingJob?.cancel()

        pingJob = monitorScope.launch {
            delay(500)
            var success = false

            for (attempt in 1..3) {
                try {
                    val url = URL(pingUrl)
                    val connection = networkToTest.openConnection(url) as HttpURLConnection

                    connection.connectTimeout = 2000
                    connection.readTimeout = 2000
                    connection.requestMethod = "GET"
                    connection.connect()

                    val responseCode = connection.responseCode
                    connection.disconnect()

                    if (responseCode in 200..499) {
                        if (!_isOnline.value) {
                            _isOnline.value = true
                        }
                        Log.d(TAG, "Health check passed on attempt $attempt.")
                        success = true
                        break
                    } else {
                        Log.w(TAG, "Health check failed. Code: $responseCode on attempt $attempt")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Health check failed on attempt $attempt: ${e.message}")
                }

                if (attempt < 3) delay(1000)
            }

            if (!success) {
                _isOnline.value = false
                Log.e(TAG, "All 3 health checks failed. Remaining offline.")
            }
        }
    }

    fun forceRetryConnection() {
        if (currentNetwork != null) {
            checkHealthWithRetries(currentNetwork!!)
        } else {
            _isOnline.value = false
        }
    }

    fun verifyConnectionNow(): Boolean {
        val network = currentNetwork ?: return false
        if (pingUrl.isEmpty()) return false
        return runBlocking(Dispatchers.IO) {
            healthCheckOnce(network)
        }
    }

    fun reportNetworkSuccess() {
        if (!_isOnline.value) {
            _isOnline.value = true
        }
    }

    fun reportNetworkError() {
        if (_isOnline.value) {
            _isOnline.value = false
            Log.e(TAG, "Interceptor caught network drop. App is now OFFLINE.")
        }
    }

    private fun healthCheckOnce(networkToTest: Network): Boolean {
        return try {
            val url = URL(pingUrl)
            val connection = networkToTest.openConnection(url) as HttpURLConnection
            connection.connectTimeout = 1500
            connection.readTimeout = 1500
            connection.requestMethod = "GET"
            connection.connect()
            val responseCode = connection.responseCode
            connection.disconnect()
            responseCode in 200..499
        } catch (_: Exception) {
            false
        }
    }
}