package com.swingcricket.game.protocol

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Encodes/decodes [GameMessage]s to/from the byte payloads Nearby Connections transmits. */
object MessageCodec {

    private val json = Json {
        classDiscriminator = "type"
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(message: GameMessage): ByteArray =
        json.encodeToString(message).encodeToByteArray()

    fun decode(bytes: ByteArray): GameMessage =
        json.decodeFromString(GameMessage.serializer(), bytes.decodeToString())
}
