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
import java.net.HttpURLConnection
import java.net.URL

object NetworkMonitor {
    private const val TAG = "NetworkMonitor"
    private const val RECENT_API_SUCCESS_WINDOW_MS = 30_000L
    private const val MAX_HEALTH_ATTEMPTS = 2

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private val monitorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pingUrl: String = ""

    private var pingJob: Job? = null
    private var lastApiSuccessAtMs: Long = 0L

    fun register(context: Context, baseUrl: String) {
        pingUrl = baseUrl
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (networkCallback != null) return

        val currentNetwork = connectivityManager.activeNetwork
        if (currentNetwork != null) {
            checkHealthWithRetries()
        } else {
            _isOnline.value = false
        }

        networkCallback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                Log.d(TAG, "onAvailable: OS found a new network. Verifying...")
                checkHealthWithRetries()
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

                if (hasInternet && !_isOnline.value) {
                    if (pingJob?.isActive != true) {
                        Log.d(TAG, "onCapabilitiesChanged: Internet capability restored. Verifying...")
                        checkHealthWithRetries()
                    }
                }
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "onLost: OS lost network connection.")
                if (connectivityManager.activeNetwork == null) {
                    _isOnline.value = false
                }
            }
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback!!)
    }

    private fun checkHealthWithRetries() {
        if (pingUrl.isEmpty() || pingJob?.isActive == true) return

        if (hasRecentApiSuccess()) {
            if (!_isOnline.value) {
                _isOnline.value = true
            }
            Log.d(TAG, "Skipping health ping; recent API success.")
            return
        }

        pingJob = monitorScope.launch {
            delay(500)
            var success = false

            for (attempt in 1..MAX_HEALTH_ATTEMPTS) {
                try {
                    val url = URL(pingUrl)
                    val connection = url.openConnection() as HttpURLConnection

                    connection.connectTimeout = 2000
                    connection.readTimeout = 2000
                    connection.requestMethod = "HEAD"
                    connection.connect()

                    val responseCode = connection.responseCode
                    connection.disconnect()

                    if (responseCode in 200..499) {
                        markOnline()
                        Log.d(TAG, "Health check passed on attempt $attempt.")
                        success = true
                        break
                    } else {
                        Log.w(TAG, "Health check failed. Code: $responseCode on attempt $attempt")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Health check failed on attempt $attempt: ${e.message}")
                }

                if (attempt < MAX_HEALTH_ATTEMPTS) delay(1000)
            }

            if (!success) {
                _isOnline.value = false
                Log.e(TAG, "Health checks failed. Remaining offline.")
            }
        }
    }

    private fun hasRecentApiSuccess(): Boolean {
        val elapsed = System.currentTimeMillis() - lastApiSuccessAtMs
        return lastApiSuccessAtMs > 0L && elapsed < RECENT_API_SUCCESS_WINDOW_MS
    }

    private fun markOnline() {
        if (!_isOnline.value) {
            _isOnline.value = true
        }
    }

    fun forceRetryConnection() {
        if (hasRecentApiSuccess()) {
            markOnline()
            return
        }
        if (pingJob?.isActive != true) {
            checkHealthWithRetries()
        }
    }

    fun reportNetworkSuccess() {
        lastApiSuccessAtMs = System.currentTimeMillis()
        markOnline()
    }

    fun reportNetworkError() {
        if (_isOnline.value) {
            _isOnline.value = false
            lastApiSuccessAtMs = 0L
            Log.e(TAG, "Interceptor caught network drop. App is now OFFLINE.")
            forceRetryConnection()
        }
    }
}
