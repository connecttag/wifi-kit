package org.connecttag.services.wifi

import android.net.wifi.WifiConfiguration

public interface IWifiKit {
    public fun connectTo(credentials: WifiNetworkCredentials): WifiOperationResult

    public fun connectTo(ssid: String, password: String): WifiOperationResult =
        connectTo(WifiNetworkCredentials(ssid = ssid, password = password))

    public fun suggestNetwork(credentials: WifiNetworkCredentials): WifiOperationResult

    public fun removeSuggestedNetwork(ssid: String): WifiOperationResult
}

public interface DeviceOwnerWifiController {
    public fun saveWifiConfiguration(wifiConfiguration: WifiConfiguration): WifiOperationResult

    public fun removeConfiguredNetwork(ssid: String): WifiOperationResult
}

public data class WifiNetworkCredentials(
    val ssid: String,
    val password: String = "",
    val hiddenSsid: Boolean = false,
)

public sealed interface WifiOperationResult {
    public data object Success : WifiOperationResult

    public data class PermissionDenied(
        val permissions: List<String>,
    ) : WifiOperationResult

    public data object Unsupported : WifiOperationResult

    public data class Failed(
        val reason: String,
        val statusCode: Int? = null,
    ) : WifiOperationResult

    public val isSuccess: Boolean
        get() = this is Success
}
