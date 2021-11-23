package com.example.musicappexercise6.untils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build

object NetworkHelper {
    fun checkNetwork(context: Context): Boolean {
        var result = false
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                result = checkNetworkConnection(this, this.activeNetwork)
            } else {
                val networks = this.allNetworks
                for (network in networks) {
                    if (checkNetworkConnection(this, network))
                        result = true
                }
            }
        }
        return result
    }

    private fun checkNetworkConnection(
        manager: ConnectivityManager,
        activeNetwork: Network?
    ): Boolean {
        manager.getNetworkCapabilities(activeNetwork)?.also {
            if (it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true
            } else if (it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return true
            }
        }
        return false
    }
}