/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.js

import com.kanyun.kotlin.ktor.ohos.api.Http
import io.ktor.client.engine.CLIENT_CONFIG
import io.ktor.client.engine.HttpClientEngineBase
import io.ktor.client.engine.callContext
import io.ktor.client.engine.js.ohos.*
import io.ktor.client.engine.js.ohos.WebSocket
import io.ktor.client.engine.js.ohos.WebSocket.Companion.createWebSocket
import io.ktor.client.engine.mergeHeaders
import io.ktor.client.plugins.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.utils.buildHeaders
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.*
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.*
import kotlinx.io.*
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.w3c.dom.events.*
import kotlin.coroutines.*

internal class OhosJsClientEngine(
    override val config: OhosHttpClientEngineConfig
) : HttpClientEngineBase("ktor-ohos") {

    override val supportedCapabilities = setOf(HttpTimeoutCapability, WebSocketCapability, SSECapability)

    @InternalAPI
    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        val callContext = callContext()
        val clientConfig = data.attributes[CLIENT_CONFIG]
        println("data 数据: data:${data.toString()}")
        if (data.isUpgradeRequest()) {
            return executeWebSocketRequest(data, callContext)
        }
        val requestTime = GMTDate()
        val httpRequest = Http.createHttp()

        val options: Http.HttpRequestOptions = js("{}")
        options.apply {
            method = data.method.value

            val jsHeaders = js("({})")
            mergeHeaders(data.headers, data.body) { key, value ->
                jsHeaders[key] = value
            }
            header = jsHeaders
            println("body 没有写入之前: data:${data},headers:${data.headers}")
            val bodyBytes = when (val content = data.body) {
                is OutgoingContent.ByteArrayContent -> content.bytes()
                is OutgoingContent.ReadChannelContent -> content.readFrom().readRemaining().readByteArray()
                is OutgoingContent.WriteChannelContent -> {
                    GlobalScope.writer(callContext) {
                        content.writeTo(channel)
                    }.channel.readRemaining().readByteArray()
                }

                else -> null
            }
            bodyBytes?.let { extraData = Uint8Array(it.toTypedArray()).buffer }
            connectTimeout = config.connectTimeout
            readTimeout = config.readTimeout
            usingProtocol = Http.HttpProtocol.HTTP1_1
            expectDataType = Http.HttpDataType.ARRAY_BUFFER
        }

        val response = httpRequest.request(data.url.toString(), options).await()

        val responseChannel = writer {
            when (val result = response.result as Any) {
                is String -> {
                    channel.writeFully(result.toByteArray())
                    channel.flush()
                }

                is ArrayBuffer -> {
                    val array = Uint8Array(result)
                    channel.writeFully(array.asByteArray())
                    channel.flush()
                }

                else -> {}
            }
        }.channel

        httpRequest.destroy()

        return HttpResponseData(
            HttpStatusCode(response.responseCode, ""),
            requestTime,
            buildHeaders {
                for (entry in js("Object").entries(response.header)) {
                    val key = entry[0]
                    val value = entry[1]
                    append(key, value)
                }
            },
            HttpProtocolVersion.HTTP_1_1,
            responseChannel,
            callContext
        )
    }

    private suspend fun executeWebSocketRequest(
        request: HttpRequestData,
        callContext: CoroutineContext
    ): HttpResponseData {
        val requestTime = GMTDate()

        val urlString = request.url.toString()
        val socket: WebSocket.WebSocket = createOhosWebSocket()
        val options: WebSocket.WebSocketRequestOptions = js("{}")
        options.apply {
            header = request.headers
        }
        socket.connect(url = urlString, options = options)
        val session = OhosJsWebSocketSession(callContext, socket)

        try {
            socket.awaitConnection()
        } catch (cause: Throwable) {
            callContext.cancel(kotlinx.coroutines.CancellationException("Failed to connect to $urlString", cause))
            throw cause
        }

        return HttpResponseData(
            HttpStatusCode.SwitchingProtocols,
            requestTime,
            Headers.Empty,
            HttpProtocolVersion.HTTP_1_1,
            session,
            callContext
        )
    }

    private fun createOhosWebSocket(): WebSocket.WebSocket {
        return createWebSocket()
    }

    private suspend fun WebSocket.WebSocket.awaitConnection() = suspendCancellableCoroutine { continuation ->
        if (continuation.isCancelled) return@suspendCancellableCoroutine
        on("open", callback = { result ->
            continuation.resume(this@awaitConnection)
        })

        on("error", callback = { err: Error ->
            continuation.resumeWithException(WebSocketException(err.message ?: ""))
        })

        continuation.invokeOnCancellation {
            off("open", callback = { result ->
                continuation.resume(this@awaitConnection)
            })
            off("error", callback = { err: Error ->
                continuation.resumeWithException(WebSocketException(err.message ?: ""))
            })

            if (it != null) {
                this@awaitConnection.close(null)
            }
        }
    }
}
