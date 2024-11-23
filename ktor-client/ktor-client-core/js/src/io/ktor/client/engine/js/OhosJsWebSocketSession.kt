/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.ktor.client.engine.js

import com.kanyun.kotlin.ktor.ohos.api.*
import com.kanyun.kotlin.ktor.ohos.api.Http
import io.ktor.client.engine.js.ohos.*
import io.ktor.client.engine.js.ohos.WebSocket
import io.ktor.client.plugins.websocket.*
import io.ktor.util.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.khronos.webgl.*
import org.w3c.dom.*
import kotlin.coroutines.*

@Suppress("CAST_NEVER_SUCCEEDS")
internal class OhosJsWebSocketSession(
    override val coroutineContext: CoroutineContext,
    private val websocket: WebSocket.WebSocket
) : DefaultWebSocketSession {
    private val _closeReason: CompletableDeferred<CloseReason> = CompletableDeferred()
    private val _incoming: Channel<Frame> = Channel(Channel.UNLIMITED)
    private val _outgoing: Channel<Frame> = Channel(Channel.UNLIMITED)

    override val incoming: ReceiveChannel<Frame> = _incoming
    override val outgoing: SendChannel<Frame> = _outgoing

    override val extensions: List<WebSocketExtension<*>>
        get() = emptyList()

    override val closeReason: Deferred<CloseReason?> = _closeReason

    override var pingIntervalMillis: Long
        get() = throw WebSocketException("Websocket ping-pong is not supported in Ohos engine.")
        set(_) = throw WebSocketException("Websocket ping-pong is not supported in Ohos engine.")

    override var timeoutMillis: Long
        get() = throw WebSocketException("Websocket timeout is not supported in Ohos engine.")
        set(_) = throw WebSocketException("Websocket timeout is not supported in Ohos engine.")

    override var masking: Boolean
        get() = true
        set(_) = throw WebSocketException("Masking switch is not supported in Ohos engine.")

    override var maxFrameSize: Long
        get() = Long.MAX_VALUE
        set(_) = throw WebSocketException("Max frame size switch is not supported in Ohos engine.")

    init {
//        websocket. = BinaryType.ARRAYBUFFER
//        val onMessage: AsyncCallback<ArrayBuffer> = js("{}")

        websocket.on(
            type = "message",
            callback = { err: BusinessError, data:Any ->
                val frame: Frame = when (val eventData = data) {
                    is ArrayBuffer -> Frame.Binary(false, Int8Array(eventData).toByteArray())
                    is String -> Frame.Text(text = eventData)
                    else -> {
                        val error = IllegalStateException("Unknown frame type: $eventData")
                        _closeReason.completeExceptionally(error)
                        throw error
                    }
                }
                _incoming.trySend(frame)
            }

        )

        websocket.on(
            "error",
            callback = { err: Error ->
                val cause = WebSocketException(message = "${err.message}", cause = err)
                _closeReason.completeExceptionally(cause)
                _incoming.close(cause)
                _outgoing.cancel()
            }
        )

        websocket.on(
            "close",
            callback = { err: BusinessError, data: WebSocket.CloseResult ->
                val reason = CloseReason(data.code as Short, data.reason)
                _closeReason.complete(reason)
                _incoming.trySend(Frame.Close(reason))
                _incoming.close()
                _outgoing.cancel()
            }
        )

        launch {
            _outgoing.consumeEach {
                when (it.frameType) {
                    FrameType.TEXT -> {
                        val text = it.data

                        websocket.send(text.decodeToString(0, 0 + text.size))
                    }

                    FrameType.BINARY -> {
                        val source = it.data as Int8Array
                        val frameData = source.buffer.slice(
                            source.byteOffset,
                            source.byteOffset + source.byteLength
                        )

                        websocket.send(frameData)
                    }

                    FrameType.CLOSE -> {
                        val data = buildPacket { writeFully(it.data) }
                        val code = data.readShort()
                        val reason = data.readText()
                        _closeReason.complete(CloseReason(code, reason))
                        if (code.isReservedStatusCode()) {
                            websocket.close(null)
                        } else {
                            val options: WebSocket.WebSocketCloseOptions = js("{}")
                            options.apply {
                                this.code = code.toInt()
                                this.reason = reason
                            }
                            websocket.close(options)
                        }
                    }

                    FrameType.PING, FrameType.PONG -> {
                        // ignore
                    }
                }
            }
        }

        coroutineContext[Job]?.invokeOnCompletion { cause ->
            if (cause == null) {
                websocket.close(null)
            } else {
                // We cannot use INTERNAL_ERROR similarly to other WebSocketSession implementations here
                // as sending it is not supported by browsers.
                val options: WebSocket.WebSocketCloseOptions = js("{}")
                options.apply {
                    this.code = CloseReason.Codes.NORMAL.code.toInt()
                    this.reason = "Client failed"
                }
                websocket.close(options)
            }
        }
    }

    @OptIn(InternalAPI::class)
    override fun start(negotiatedExtensions: List<WebSocketExtension<*>>) {
        require(negotiatedExtensions.isEmpty()) { "Extensions are not supported." }
    }

    override suspend fun flush() {
    }

    @Deprecated(
        "Use cancel() instead.",
        ReplaceWith("cancel()", "kotlinx.coroutines.cancel"),
        level = DeprecationLevel.ERROR
    )
    override fun terminate() {
        _incoming.cancel()
        _outgoing.cancel()
        _closeReason.cancel("WebSocket terminated")
        websocket.close(null)
    }

    @OptIn(InternalAPI::class)
    private fun Short.isReservedStatusCode(): Boolean {
        return CloseReason.Codes.byCode(this).let { resolved ->

            resolved == null || resolved == CloseReason.Codes.CLOSED_ABNORMALLY
        }
    }
}
