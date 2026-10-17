package org.connecttag.services.wifi

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import androidx.core.content.ContextCompat

public class WifiKitImpl(
    private val context: Context,
) : IWifiKit, DeviceOwnerWifiController {

    private val wifiManager: WifiManager by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    override fun connectTo(credentials: WifiNetworkCredentials): WifiOperationResult {
        return suggestNetwork(credentials)
    }

    override fun suggestNetwork(credentials: WifiNetworkCredentials): WifiOperationResult {
        if (!hasChangeWifiStatePermission()) {
            return WifiOperationResult.PermissionDenied(listOf(Manifest.permission.CHANGE_WIFI_STATE))
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            addNetworkSuggestion(credentials)
        } else {
            saveWifiConfiguration(credentials.toLegacyWifiConfiguration())
        }
    }

    override fun removeSuggestedNetwork(ssid: String): WifiOperationResult {
        if (!hasChangeWifiStatePermission()) {
            return WifiOperationResult.PermissionDenied(listOf(Manifest.permission.CHANGE_WIFI_STATE))
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val suggestion = WifiNetworkSuggestion.Builder()
                .setSsid(ssid)
                .build()
            val status = wifiManager.removeNetworkSuggestions(listOf(suggestion))
            status.toWifiOperationResult()
        } else {
            removeConfiguredNetwork(ssid)
        }
    }

    @SuppressLint("MissingPermission")
    private fun addNetworkSuggestion(credentials: WifiNetworkCredentials): WifiOperationResult {
        val builder = WifiNetworkSuggestion.Builder()
            .setSsid(credentials.ssid)
            .setIsHiddenSsid(credentials.hiddenSsid)

        if (credentials.password.isNotBlank()) {
            builder.setWpa2Passphrase(credentials.password)
        }

        val status = wifiManager.addNetworkSuggestions(listOf(builder.build()))
        return status.toWifiOperationResult()
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    override fun saveWifiConfiguration(wifiConfiguration: WifiConfiguration): WifiOperationResult {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return WifiOperationResult.Unsupported
        }

        if (!hasChangeWifiStatePermission()) {
            return WifiOperationResult.PermissionDenied(listOf(Manifest.permission.CHANGE_WIFI_STATE))
        }

        val networkId = if (wifiConfiguration.networkId == -1) {
            wifiManager.addNetwork(wifiConfiguration)
        } else {
            wifiManager.updateNetwork(wifiConfiguration)
        }

        if (networkId == -1) {
            return WifiOperationResult.Failed("Failed to add or update Wi-Fi configuration.")
        }

        wifiManager.saveConfiguration()
        return if (wifiManager.enableNetwork(networkId, false)) {
            WifiOperationResult.Success
        } else {
            WifiOperationResult.Failed("Failed to enable Wi-Fi network.")
        }
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    override fun removeConfiguredNetwork(ssid: String): WifiOperationResult {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return WifiOperationResult.Unsupported
        }

        if (!hasChangeWifiStatePermission()) {
            return WifiOperationResult.PermissionDenied(listOf(Manifest.permission.CHANGE_WIFI_STATE))
        }

        val network = wifiManager.configuredNetworks
            ?.find { it.SSID == "\"$ssid\"" || it.SSID == ssid }
            ?: return WifiOperationResult.Failed("Wi-Fi configuration was not found.")

        return if (wifiManager.removeNetwork(network.networkId)) {
            wifiManager.saveConfiguration()
            WifiOperationResult.Success
        } else {
            WifiOperationResult.Failed("Failed to remove Wi-Fi configuration.")
        }
    }

    private fun hasChangeWifiStatePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CHANGE_WIFI_STATE,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun Int.toWifiOperationResult(): WifiOperationResult {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            this == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS
        ) {
            WifiOperationResult.Success
        } else {
            WifiOperationResult.Failed("Wi-Fi suggestion operation failed.", this)
        }
    }
}

@Suppress("DEPRECATION")
private fun WifiNetworkCredentials.toLegacyWifiConfiguration(): WifiConfiguration {
    return WifiConfiguration().apply {
        SSID = "\"$ssid\""
        hiddenSSID = hiddenSsid
        if (password.isNotBlank()) {
            preSharedKey = "\"$password\""
            allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
        } else {
            allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
        }
    }
}
