/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.js

import com.kanyun.kotlin.ktor.ohos.api.Http
import io.ktor.client.engine.CLIENT_CONFIG
import io.ktor.client.engine.HttpClientEngineBase
import io.ktor.client.engine.callContext
import io.ktor.client.engine.js.ohos.BusinessError
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
import io.ktor.util.date.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.io.*
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.set
import kotlin.coroutines.*
import kotlin.random.Random

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
            when (val content = data.body) {
                is OutgoingContent.ByteArrayContent -> {
                    extraData = content.bytes().toJsArray().buffer
                }
                is OutgoingContent.ReadChannelContent -> {
                    // 返回 ByteReadChannel
                    val readChannel = content.readFrom()
                    // 这里 readRemaining() 会一直等到 channel 被写完或 close()
                    val byteArray = readChannel.readRemaining().readByteArray()
                    println("body 实际读入 ${byteArray.size}")
                    extraData = byteArray.toJsArray().buffer
                }

                is OutgoingContent.WriteChannelContent -> {
                    CoroutineScope(callContext).writer(callContext) {
                        content.writeTo(channel)
                    }.channel.readRemaining().readByteArray()
                    // 1. 启动一个协程写数据
                    val writerJob = CoroutineScope(callContext).writer(callContext) {
                        content.writeTo(channel)
                    }
                    // 2. 等待写协程完成后，再读出 ByteArray
                    val writtenChannel = writerJob.channel
                    // 3. 等写完后，统一读到 byteArray 中
                    extraData = writtenChannel.readRemaining().readByteArray().toJsArray().buffer
                }
                else -> {}
            }
            connectTimeout = config.connectTimeout
            readTimeout = config.readTimeout
            usingProtocol = Http.HttpProtocol.HTTP2
            expectDataType = Http.HttpDataType.ARRAY_BUFFER
        }

        val response = httpRequest.request(data.url.toString(), options).then(onFulfilled = {
            it
        }, onRejected = {
            println("收到异常：${data.url} -- ${it.message ?: it.cause?.message ?: "华为网络请求失败"}")
            val jsHeaders = js("({})")
            jsHeaders["Content-Type"] = "application/json"
            object : Http.HttpResponse {
                override val result: dynamic
                    // {"error_type":"throw","message":"华为网络请求失败","detail":"华为网络请求失败"}
                    // it.message?:it.cause?.message?:"华为网络请求失败"
                    get() = "{\"error_type\":\"throw\",\"message\":\"${it.message ?: it.cause?.message ?: "华为网络请求失败"}\",\"detail\":\"\"}"
                override val resultType: Http._HttpDataType
                    get() = Http._HttpDataType
                override val responseCode: Int
                    get() = 400
                override val header: Any
                    get() = jsHeaders
                override val cookies: String
                    get() = ""
            }
        }).await()
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

                else -> {
                    channel.writeFully("".toByteArray())
                    channel.flush()
                }
            }
        }.channel
        httpRequest.destroy()

        var hasContentLength = true
        for (entry in js("Object").entries(response.header)) {
            val key = entry[0]
            val value = entry[1]
            if (key.toString().equals("Content-Length", ignoreCase = true) && value.toString().toInt() == 0) {
                hasContentLength = false
                break
            }
        }
        // 如果是 PUT 请求，且返回码是 200，且 header 中有的 Content length 为 0，那么返回一个空的 HttpResponseData
        if (response.responseCode == 200 && data.method == HttpMethod.Put && !hasContentLength) {
            return HttpResponseData(
                HttpStatusCode(response.responseCode, ""),
                requestTime,
                buildHeaders {
                    append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                },
                HttpProtocolVersion.HTTP_1_1,
                "{}",
                callContext
            )
        } else {
            return HttpResponseData(
                HttpStatusCode(response.responseCode, ""),
                requestTime,
                buildHeaders {
                    for (entry in js("Object").entries(response.header)) {
                        val key = entry[0]
                        val value = entry[1]
                        println("key ${key.toString()} -- value ：${value.toString()}")
                        // todo hzd  cookie 需要单独处理
                        append(key.toString(), value.toString())
                    }
                    if (isEmpty()) {
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    }
                },
                HttpProtocolVersion.HTTP_1_1,
                responseChannel,
                callContext
            )
        }
    }

    @OptIn(InternalAPI::class)
    private suspend fun executeWebSocketRequest(
        request: HttpRequestData,
        callContext: CoroutineContext
    ): HttpResponseData {
        val requestTime = GMTDate()

        val urlString = request.url.toString()
        val socket: WebSocket.WebSocket = createOhosWebSocket()
        val options: WebSocket.WebSocketRequestOptions = js("{}")
        val jsHeaders = js("({})")
        mergeHeaders(request.headers, request.body) { key, value ->
            jsHeaders[key] = value
        }
        options.apply {
            header = jsHeaders
        }
        val session = OhosJsWebSocketSession(callContext, socket)
        val connect = socket.connect(url = urlString, options = options).catch {
            println("WebSocket 连接失败：${it.message}")
            false
        }.await()
        if (connect) {
            try {
                socket.awaitConnection()
            } catch (cause: Throwable) {
                callContext.cancel(kotlinx.coroutines.CancellationException("Failed to connect to $urlString", cause))
                throw cause
            }
        } else {
            val exception = kotlinx.coroutines.CancellationException("Failed to connect to $urlString")
            callContext.cancel(exception)
            throw exception
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
        on("open", callback = { err: BusinessError, data: Any ->
            continuation.resume(this@awaitConnection)
        })

        continuation.invokeOnCancellation {
            off("open", callback = { result ->
                if (continuation.isCancelled || continuation.isActive || continuation.isCompleted) return@off
                continuation.resume(this@awaitConnection)
            })
            if (it != null) {
                this@awaitConnection.close(null)
            }
        }
    }
}
