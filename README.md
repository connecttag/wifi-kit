# wifi-kit

Android Wi-Fi operations.

## Rules

- The base module has no Hilt dependency.
- Hilt bindings live in `services:wifi-kit-hilt`.
- `IWifiKit` uses Wi-Fi suggestions for app-level network requests.
- Legacy `WifiConfiguration` operations are isolated behind `DeviceOwnerWifiController`.
- Operations return `WifiOperationResult` instead of raw booleans.
