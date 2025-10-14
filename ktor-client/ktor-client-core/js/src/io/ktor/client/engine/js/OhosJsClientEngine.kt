/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.js

import com.kanyun.kotlin.ktor.ohos.api.Http
import com.kanyun.kotlin.ktor.ohos.api.Http.HttpRequest.DataProgress
import io.ktor.client.engine.*
import io.ktor.client.engine.js.ohos.*
import io.ktor.client.engine.js.ohos.WebSocket.Companion.createWebSocket
import io.ktor.client.plugins.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.utils.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.util.date.*
import io.ktor.utils.io.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.await
import kotlinx.coroutines.cancel
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.io.readByteArray
import io.ktor.utils.io.core.*
import io.ktor.client.call.UnsupportedContentTypeException
import io.ktor.client.call.body
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
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.charsets.MalformedInputException
import io.ktor.utils.io.charsets.charset
import io.ktor.utils.io.charsets.name
import io.ktor.utils.io.core.*
import io.ktor.utils.io.js.ohos.Util
import io.ktor.websocket.Frame
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
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

    internal suspend fun OutgoingContent.convertToOhosBody(callContext: CoroutineContext, method: HttpMethod): Any =
        when (this) {
            is OutgoingContent.ByteArrayContent -> bytes().toJsArray().buffer

            is OutgoingContent.ReadChannelContent -> {
                // 返回 ByteReadChannel
                val readChannel = readFrom()
                // 这里 readRemaining() 会一直等到 channel 被写完或 close()
                val byteArray = readChannel.readRemaining().readByteArray()
                if (config.isDebug) {
                    config.printLog {
                        "body 实际读入 ${byteArray.size}"
                    }
                }
                byteArray.toJsArray().buffer
            }

            is OutgoingContent.WriteChannelContent -> {
                CoroutineScope(callContext).writer(callContext) {
                    writeTo(channel)
                }.channel.readRemaining().readByteArray()
                // 1. 启动一个协程写数据
                val writerJob = CoroutineScope(callContext).writer(callContext) {
                    writeTo(channel)
                }
                // 2. 等待写协程完成后，再读出 ByteArray
                val writtenChannel = writerJob.channel
                // 3. 等写完后，统一读到 byteArray 中
                writtenChannel.readRemaining().readByteArray().toJsArray().buffer
            }

            is OutgoingContent.NoContent -> {
                config.printLog { "data 数据: NoContent" }
                if (method == HttpMethod.Post) {
                    "{}".toByteArray().toJsArray().buffer
                } else {
                    ByteArray(0).toJsArray().buffer
                }

            }

            is OutgoingContent.ContentWrapper -> delegate().convertToOhosBody(callContext, method = method)
            else -> {
                config.printLog { "data 数据: 不支持的类型" }
                if (method == HttpMethod.Post) {
                    "{}".toByteArray().toJsArray().buffer
                } else {
                    ByteArray(0).toJsArray().buffer
                }
            }
        }

    @InternalAPI
    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        val callContext = callContext()
        val clientConfig = data.attributes[CLIENT_CONFIG]
        if (config.isDebug) {
            config.printLog { "data 数据: data:${data}" }
        }
        if (data.isUpgradeRequest()) {
            return executeWebSocketRequest(data, callContext)
        }
        val requestTime = GMTDate()
        val httpRequest = Http.createHttp()

        val options: Http.HttpRequestOptions = js("{}")
        // 使用流式处理请求
        val isStreaming = data.headers["ktor-js-streaming"] == "true"
        options.apply {
            method = data.method.value

            val jsHeaders = js("({})")
            mergeHeaders(data.headers, data.body) { key, value ->
                jsHeaders[key] = value
            }
            header = jsHeaders
            if (config.isDebug) {
                config.printLog { "body 没有写入之前: data:${data},headers:${data.headers}" }
            }
            extraData = data.body.convertToOhosBody(callContext, data.method)
            connectTimeout = config.connectTimeout
            readTimeout = if (isStreaming) config.readStreamTimeout else config.readTimeout
            usingProtocol = Http.HttpProtocol.HTTP2
            expectDataType = Http.HttpDataType.ARRAY_BUFFER
            maxLimit = 100 * 1024 * 1024
        }

        if (data.isSseRequest()) {
            return executeSseRequest(httpRequest, data, options, callContext)
        }

        if (config.isDebug) {
            config.printLog { "executeStreamingRequest, data.body:${data.body}" }
        }
        if (isStreaming) {
            return executeStreamingRequest(httpRequest, data, options, callContext)
        }
        val response = httpRequest.request(data.url.toString(), options).then(onFulfilled = {
            it
        }, onRejected = {
            if (config.isDebug) {
                config.printLog {
                    "收到异常：${data.url} -- ${it.message ?: it.cause?.message ?: "华为网络请求失败"}"
                }
            }
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
                    get() = 402
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
                HttpStatusCode(response.responseCode, ""), requestTime, buildHeaders {
                    append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                }, HttpProtocolVersion.HTTP_1_1, "{}", callContext
            )
        } else {
            config.printLog { "响应数据：${data.url} -- ${response.result}" }
            return HttpResponseData(
                HttpStatusCode(response.responseCode, ""), requestTime, buildHeaders {
                    for (entry in js("Object").entries(response.header)) {
                        val key = entry[0]
                        val value = entry[1]
                        if (config.isDebug) {
                            config.printLog {
                                "key ${key.toString()} -- value ：${value.toString()}"
                            }
                        }
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
    private suspend fun executeStreamingRequest(
        httpRequest: Http.HttpRequest,
        requestData: HttpRequestData,
        options: Http.HttpRequestOptions,
        callContext: CoroutineContext
    ): HttpResponseData {
        val requestTime = GMTDate()
        val _incoming: Channel<ByteArray> = Channel(Channel.UNLIMITED)
        val incoming: ReceiveChannel<ByteArray> = _incoming

        // Store response headers
        var responseHeaders: dynamic = js("({})")
        var responseCode: Int = 200

        val responseChannel = ByteChannel(autoFlush = true)

        // 设置header接收监听器
        httpRequest.on(
            type = "headersReceive",
            callback = { headers: Any ->
                if (config.isDebug) {
                    jsForEach(headers, { key, value ->
                        config.printLog { "executeStreamingRequest, headersReceive, $key = $value" }
                    }
                    )
                }
                responseHeaders = headers
            }
        )

        // 设置数据接收监听器
        httpRequest.on(
            type = "dataReceive",
            callback = { arrayBuffer: ArrayBuffer ->
                val uintArray = Uint8Array(arrayBuffer)
                if (config.isDebug) {
                    val decoder = Util.TextDecoder("utf-8")
                    val result = try {
                        decoder.decodeToString(uintArray)
                    } catch (cause: Throwable) {
                        throw MalformedInputException("Failed to decode bytes: ${cause.message ?: "no cause provided"}")
                    }
                    config.printLog { "executeStreamingRequest, dataReceive $result" }
                }
                _incoming.trySend(uintArray.asByteArray())
            }
        )

        httpRequest.on(
            type = "dataEnd",
            callback = { unit: Unit ->
                CoroutineScope(callContext).launch {
                    if (config.isDebug) {
                        config.printLog { "executeStreamingRequest, dataEnd" }
                    }
                    _incoming.close()
                }

            }
        )

        httpRequest.on(
            type = "dataReceiveProgress",
            callback = { progress: DataProgress ->
                if (config.isDebug) {
                    config.printLog { "executeStreamingRequest, dataReceiveProgress, $progress" }
                }
            }
        )

        // 启动协程以将数据从通道传输到响应通道
        CoroutineScope(callContext).launch {
            try {
                incoming.consumeEach { byteArray ->
                    if (responseChannel.isClosedForWrite.not()) {
                        responseChannel.writeFully(byteArray)
                        responseChannel.flush()
                    }
                }
            } catch (e: Exception) {
                if (config.isDebug) {
                    config.printLog { "executeStreamingRequest, data transfer error: ${e.message}" }
                }
            } finally {
                responseChannel.close()
            }
        }

        // Start the streaming request
        val result = httpRequest.requestInStream(
            requestData.url.toString(),
            options
        ).then { errorCode ->
            responseCode = errorCode
            if (config.isDebug) {
                config.printLog { "executeStreamingRequest, completed with code: $errorCode" }
            }
            httpRequest.destroy()
        }.catch { throwable ->
            responseCode = 500
            httpRequest.destroy()
            if (config.isDebug) {
                config.printLog { "executeStreamingRequest, error: ${throwable::class.simpleName}, ${throwable.message}" }
            }
            responseChannel.close(throwable)
            _incoming.close(throwable)
        }

        return HttpResponseData(
            HttpStatusCode(responseCode, ""),
            requestTime,
            buildHeaders {
                jsForEach(obj = responseHeaders) { key, value ->
                    append(key.toString(), value.toString())
                    if (config.isDebug) {
                        config.printLog {
                            "streaming header: ${key.toString()} = ${value.toString()}"
                        }
                    }
                }
                if (isEmpty()) {
                    append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                }
            },
            HttpProtocolVersion.HTTP_2_0,
            responseChannel,
            callContext
        )
    }

    @OptIn(InternalAPI::class)
    private suspend fun CoroutineScope.executeSseRequest(
        httpRequest: Http.HttpRequest,
        requestData: HttpRequestData,
        options: Http.HttpRequestOptions,
        callContext: CoroutineContext
    ): HttpResponseData {
        val requestTime = GMTDate()
        val _incoming: Channel<ByteArray> = Channel(Channel.UNLIMITED)
        val incoming: ReceiveChannel<ByteArray> = _incoming


        val sseChannel = ByteChannel(autoFlush = true)
        val sseSession = DefaultClientSSESession(
            content = requestData.body as SSEClientContent,
            input = sseChannel,
            coroutineContext = callContext,
        )
        val scope = this@executeSseRequest
        scope.launch(callContext) {
            incoming.consumeEach {
                config.printLog { "executeSseRequest, incoming.consumeEach, ${this.isActive}" }
                if (this.isActive) {
                    sseChannel.writeFully(it)
                }
            }
            config.printLog { "executeSseRequest, incoming.consumeEach end" }
            callContext.cancel()
        }
        // 设置数据接收监听
        httpRequest.on(
            type = "dataReceive",
            callback = { arrayBuffer: ArrayBuffer ->
                config.printLog { "executeSseRequest, dataReceive, ${this.isActive}" }
                if (this.isActive) {
                    _incoming.trySend(Uint8Array(arrayBuffer).asByteArray())
                }
            }
        )

        httpRequest.on(
            type = "dataEnd",
            callback = { unit: Unit ->
                config.printLog { "executeSseRequest, dataEnd" }
                sseChannel.close()
            }
        )

        httpRequest.on(
            type = "dataReceiveProgress",
            callback = { progress: DataProgress ->
                config.printLog { "executeSseRequest, dataReceiveProgress, ${progress}" }
            }
        )

        val result = httpRequest.requestInStream(
            requestData.url.toString(),
            options
        ).then { i ->
            config.printLog { "executeSseRequest, ErrorCode: $i" }
            httpRequest.destroy()
            sseChannel.close()
        }
            .catch { throwable ->
                httpRequest.destroy()
                config.printLog { "executeSseRequest, throwable: ${throwable::class.simpleName}, ${throwable.message}" }
                sseChannel.close(throwable)
            }

        config.printLog { "executeSseRequest promise result: $result" }

        return HttpResponseData(
            HttpStatusCode.OK,
            requestTime,
            HeadersBuilder().apply {
                append(HttpHeaders.ContentType, ContentType.Text.EventStream.toString())
            }.build(),
            HttpProtocolVersion.HTTP_2_0,
            sseSession,
            callContext
        )
    }

    @OptIn(InternalAPI::class)
    private suspend fun executeWebSocketRequest(
        request: HttpRequestData, callContext: CoroutineContext
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
            if (config.isDebug) {
                config.printLog {
                    "WebSocket 连接失败：${it.message}"
                }
            }
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

public fun jsForEach(obj: dynamic, action: (key: String, value: dynamic) -> Unit) {
    val keys = js("Object.keys(obj)") as Array<String>
    keys.forEach { key ->
        action(key, obj[key])
    }
}
