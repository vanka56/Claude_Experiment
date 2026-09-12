package com.swingcricket.game.connection

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.swingcricket.game.protocol.GameMessage
import com.swingcricket.game.protocol.MessageCodec
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "NearbyConnection"
private const val SERVICE_ID = "com.swingcricket.game.SERVICE_ID"

/**
 * Wraps Google Play Services Nearby Connections (P2P_POINT_TO_POINT strategy) to pair exactly two
 * phones over Bluetooth / Wi-Fi Direct / local Wi-Fi — no internet or pairing code required.
 */
class NearbyConnectionManager(context: Context, var localName: String) {

    private val appContext = context.applicationContext
    private val connectionsClient = Nearby.getConnectionsClient(appContext)

    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val state: StateFlow<ConnectionState> = _state.asStateFlow()

    private val _messages = MutableSharedFlow<GameMessage>(extraBufferCapacity = 32)
    val messages: SharedFlow<GameMessage> = _messages.asSharedFlow()

    private var connectedEndpointId: String? = null

    fun host() {
        _state.value = ConnectionState.Hosting
        val options = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build()
        connectionsClient.startAdvertising(
            localName, SERVICE_ID, connectionLifecycleCallback, options
        ).addOnFailureListener {
            Log.e(TAG, "startAdvertising failed", it)
            _state.value = ConnectionState.Failed(it.message ?: "Could not start hosting")
        }
    }

    fun join() {
        _state.value = ConnectionState.Discovering
        val options = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build()
        connectionsClient.startDiscovery(
            SERVICE_ID, endpointDiscoveryCallback, options
        ).addOnFailureListener {
            Log.e(TAG, "startDiscovery failed", it)
            _state.value = ConnectionState.Failed(it.message ?: "Could not search for a game")
        }
    }

    fun send(message: GameMessage) {
        val endpointId = connectedEndpointId ?: return
        connectionsClient.sendPayload(endpointId, Payload.fromBytes(MessageCodec.encode(message)))
    }

    fun disconnect() {
        connectedEndpointId?.let { connectionsClient.disconnectFromEndpoint(it) }
        connectedEndpointId = null
        connectionsClient.stopAdvertising()
        connectionsClient.stopDiscovery()
        connectionsClient.stopAllEndpoints()
        _state.value = ConnectionState.Disconnected
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            connectionsClient.requestConnection(localName, endpointId, connectionLifecycleCallback)
        }
        override fun onEndpointLost(endpointId: String) = Unit
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            _state.value = ConnectionState.PendingConnection(endpointId, info.endpointName)
            // Both sides auto-accept; Nearby only completes the connection once both have.
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.statusCode == ConnectionsStatusCodes.STATUS_OK) {
                connectedEndpointId = endpointId
                connectionsClient.stopAdvertising()
                connectionsClient.stopDiscovery()
                val name = (_state.value as? ConnectionState.PendingConnection)?.endpointName ?: "Opponent"
                _state.value = ConnectionState.Connected(endpointId, name)
            } else {
                _state.value = ConnectionState.Failed("Connection rejected")
            }
        }

        override fun onDisconnected(endpointId: String) {
            connectedEndpointId = null
            _state.value = ConnectionState.Disconnected
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type != Payload.Type.BYTES) return
            val bytes = payload.asBytes() ?: return
            runCatching { MessageCodec.decode(bytes) }
                .onSuccess { _messages.tryEmit(it) }
                .onFailure { Log.e(TAG, "Failed to decode payload", it) }
        }
        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) = Unit
    }
}
