package com.swingcricket.game.connection

sealed class ConnectionState {
    data object Idle : ConnectionState()
    data object Hosting : ConnectionState()
    data object Discovering : ConnectionState()
    data class PendingConnection(val endpointId: String, val endpointName: String) : ConnectionState()
    data class Connected(val endpointId: String, val endpointName: String) : ConnectionState()
    data class Failed(val reason: String) : ConnectionState()
    data object Disconnected : ConnectionState()
}
