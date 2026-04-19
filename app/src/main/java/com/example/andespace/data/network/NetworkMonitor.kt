package com.example.andespace.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NetworkMonitor {
    private const val TAG = "NetworkMonitor"
    private var registered = false
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    fun register(context: Context) {
        if (registered) return
        registered = true

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        _isOnline.value = checkInitialState(cm)

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isOnline.value = true
                Log.d(TAG, "Network available")
            }
            override fun onLost(network: Network) {
                _isOnline.value = false
                Log.d(TAG, "Network lost")
            }
        })
    }

    fun reportNetworkSuccess() {
        if (!_isOnline.value) {
            _isOnline.value = true
        }
    }

    fun reportNetworkError() {
        if (_isOnline.value) {
            _isOnline.value = false
            Log.e(TAG, "Active validation failed: Network reported error via Interceptor")
        }
    }

    private fun checkInitialState(cm: ConnectivityManager): Boolean {
        val network = cm.activeNetwork ?: return false
        val actNw = cm.getNetworkCapabilities(network) ?: return false
        return actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}