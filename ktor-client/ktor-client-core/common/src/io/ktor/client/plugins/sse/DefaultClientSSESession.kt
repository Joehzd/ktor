/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.plugins.sse

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.sse.*
import io.ktor.util.logging.*
import io.ktor.utils.io.*
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext

@OptIn(InternalAPI::class)
@Deprecated("It should be marked with `@InternalAPI`, please use `ClientSSESession` instead")
public class DefaultClientSSESession(
    content: SSEClientContent,
    private var input: ByteReadChannel,
    override val coroutineContext: CoroutineContext
) : SSESession {
    private var lastEventId: String? = null
    private var reconnectionTimeMillis = content.reconnectionTime.inWholeMilliseconds
    private val showCommentEvents = content.showCommentEvents
    private val showRetryEvents = content.showRetryEvents
    private val maxReconnectionAttempts = content.maxReconnectionAttempts
    private var needToReconnect = maxReconnectionAttempts > 0

    private val initialRequest = content.initialRequest

    private val clientForReconnection = initialRequest.attributes[SSEClientForReconnectionAttr]

    public constructor(
        content: SSEClientContent,
        input: ByteReadChannel
    ) : this(content, input, content.callContext + Job() + CoroutineName("DefaultClientSSESession"))

    private var _incoming = flow {
        LOGGER.trace { "DefaultClientSSESession flow collect" }
        // inner while for parsing events of current input (=connection), and when the current input is closed,
        // we have an outer while to obtain new input
        while (this@DefaultClientSSESession.coroutineContext.isActive) {
            LOGGER.trace { "DefaultClientSSESession isActive1" }
            while (this@DefaultClientSSESession.coroutineContext.isActive) {
                LOGGER.trace { "DefaultClientSSESession isActive2" }
                val event = input.tryParseEvent()
                LOGGER.trace { "DefaultClientSSESession Parsed event: $event" }
                event ?: break

                if (event.isCommentsEvent() && !showCommentEvents) continue
                if (event.isRetryEvent() && !showRetryEvents) continue

                emit(event)
            }

            if (needToReconnect) {
                doReconnection()
            } else {
                close()
            }
        }
    }.catch { cause ->
        LOGGER.trace { "DefaultClientSSESession flow catch, $cause" }
        when (cause) {
            is CancellationException -> {
                // CancellationException will be handled by onCompletion operator
            }

            else -> {
                LOGGER.trace { "DefaultClientSSESession Error during SSE session processing: $cause" }
                close()
                throw cause
            }
        }
    }.onCompletion { cause ->
        LOGGER.trace { "DefaultClientSSESession flow onCompletion, $cause" }
        // Because catch operator only catch throwable occurs in upstream flow, so we use onCompletion operator instead
        // to handle CancellationException occurs in either upstream flow or downstream flow.
        if (cause is CancellationException) {
            close()
        }
    }

    init {
        coroutineContext.job.invokeOnCompletion {
            close()
        }
    }

    private suspend fun doReconnection() {
        withContext(coroutineContext) {
            var retries = 1
            while (retries <= maxReconnectionAttempts) {
                try {
                    input.cancel()

                    delay(reconnectionTimeMillis)

                    val reconnectionRequest = getRequestForReconnection()
                    LOGGER.trace {
                        "DefaultClientSSESession Sending SSE request ${reconnectionRequest.url} (attempt ${retries + 1}/${maxReconnectionAttempts + 1})"
                    }

                    val reconnectionResponse = clientForReconnection.execute(reconnectionRequest).response
                    LOGGER.trace { "DefaultClientSSESession Receive response for reconnection SSE request to ${reconnectionRequest.url}" }
                    checkResponse(reconnectionResponse)

                    if (reconnectionResponse.status == HttpStatusCode.NoContent) {
                        needToReconnect = false
                    }

                    input = reconnectionResponse.rawContent
                    return@withContext
                } catch (cause: Throwable) {
                    if (retries == maxReconnectionAttempts) {
                        LOGGER.trace {
                            "DefaultClientSSESession Max retries ($maxReconnectionAttempts) reached for SSE reconnection, closing session"
                        }
                        throw cause
                    }
                    LOGGER.trace { "DefaultClientSSESession SSE reconnection attempt ${retries + 1} failed" }
                    retries++
                }
            }
        }
    }

    private fun getRequestForReconnection() = HttpRequestBuilder().takeFrom(initialRequest).apply {
        attributes.remove(sseRequestAttr)
        attributes.put(SSEReconnectionRequestAttr, true)

        lastEventId?.let {
            headers.append("Last-Event-ID", it)
        }
    }

    override val incoming: Flow<ServerSentEvent>
        get() = _incoming

    private fun close() {
        LOGGER.trace { "DefaultClientSSESession Closing DefaultClientSSESession" }
        coroutineContext.cancel()
        input.cancel()
    }

    private suspend fun ByteReadChannel.tryParseEvent(): ServerSentEvent? =
        try {
            parseEvent()
        } catch (e: ClosedByteChannelException) {
            LOGGER.trace { "DefaultClientSSESession tryParseEvent error : $e" }
            throw e
        }

    private suspend fun ByteReadChannel.parseEvent(): ServerSentEvent? {
        val data = StringBuilder()
        val comments = StringBuilder()
        var eventType: String? = null
        var curRetry: Long? = null
        var lastEventId: String? = this@DefaultClientSSESession.lastEventId

        var wasData = false
        var wasComments = false

        LOGGER.trace { "DefaultClientSSESession Start parsing SSE event" }
        var line: String = readUTF8Line() ?: return null
        LOGGER.trace { "DefaultClientSSESession First line: $line" }
        while (line.isBlank()) {
            LOGGER.trace { "DefaultClientSSESession Skipping blank line" }
            line = readUTF8Line() ?: return null
        }

        LOGGER.trace { "DefaultClientSSESession First non-blank line: $line" }
        while (true) {
            when {
                line.isBlank() -> {
                    this@DefaultClientSSESession.lastEventId = lastEventId

                    val event = ServerSentEvent(
                        if (wasData) data.toText() else null,
                        eventType,
                        lastEventId,
                        curRetry,
                        if (wasComments) comments.toText() else null
                    )

                    if (!event.isEmpty()) {
                        return event
                    }
                }

                line.startsWith(COLON) -> {
                    wasComments = true
                    comments.appendComment(line)
                }

                else -> {
                    val field = line.substringBefore(COLON)
                    val value = line.substringAfter(COLON, missingDelimiterValue = "").removePrefix(SPACE)
                    when (field) {
                        "event" -> eventType = value
                        "data" -> {
                            wasData = true
                            data.append(value).append(END_OF_LINE)
                        }

                        "retry" -> {
                            value.toLongOrNull()?.let {
                                reconnectionTimeMillis = it
                                curRetry = it
                            }
                        }

                        "id" -> if (!value.contains(NULL)) {
                            lastEventId = value
                        }
                    }
                }
            }
            line = readUTF8Line() ?: return null
        }
    }

    private fun StringBuilder.appendComment(comment: String) {
        append(comment.removePrefix(COLON).removePrefix(SPACE)).append(END_OF_LINE)
    }

    private fun StringBuilder.toText() = toString().removeSuffix(END_OF_LINE)

    private fun ServerSentEvent.isEmpty() =
        data == null && id == null && event == null && retry == null && comments == null

    private fun ServerSentEvent.isCommentsEvent() =
        data == null && event == null && id == null && retry == null && comments != null

    private fun ServerSentEvent.isRetryEvent() =
        data == null && event == null && id == null && comments == null && retry != null
}

private const val NULL = "\u0000"
