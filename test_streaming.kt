/*
 * Copyright 2014-2025 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

/**
 * Test script to demonstrate the new requestInStream functionality
 * for the OhosJsClientEngine in HarmonyOS
 */

import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*

suspend fun main() {
    println("Testing OhosJsClientEngine with streaming support...")
    
    // Test 1: Regular request (existing behavior)
    println("\n=== Test 1: Regular Request ===")
    val regularClient = HttpClient(Ohos) {
        engine {
            isDebug = true
            printLog = { message -> println("[REGULAR] ${message()}") }
            // useStreaming is false by default
        }
    }
    
    try {
        val regularResponse = regularClient.get("https://httpbin.org/get")
        println("Regular request status: ${regularResponse.status}")
        println("Regular request body length: ${regularResponse.bodyAsText().length}")
    } catch (e: Exception) {
        println("Regular request error: ${e.message}")
    }
    
    // Test 2: Streaming request (new functionality)
    println("\n=== Test 2: Streaming Request ===")
    val streamingClient = HttpClient(Ohos) {
        engine {
            isDebug = true
            printLog = { message -> println("[STREAMING] ${message()}") }
            useStreaming = true  // Enable the new streaming functionality
        }
    }
    
    try {
        val streamingResponse = streamingClient.get("https://httpbin.org/get")
        println("Streaming request status: ${streamingResponse.status}")
        println("Streaming request body length: ${streamingResponse.bodyAsText().length}")
    } catch (e: Exception) {
        println("Streaming request error: ${e.message}")
    }
    
    // Test 3: POST with streaming
    println("\n=== Test 3: POST with Streaming ===")
    try {
        val postResponse = streamingClient.post("https://httpbin.org/post") {
            setBody("Test data for streaming POST request")
        }
        println("Streaming POST status: ${postResponse.status}")
        println("Streaming POST response received")
    } catch (e: Exception) {
        println("Streaming POST error: ${e.message}")
    }
    
    regularClient.close()
    streamingClient.close()
    println("\n=== Tests completed ===")
}

/**
 * Example usage in application code:
 * 
 * val client = HttpClient(Ohos) {
 *     engine {
 *         useStreaming = true  // Enable requestInStream API
 *         isDebug = true       // Optional: enable debug logging
 *         printLog = { message -> println(message()) }
 *     }
 * }
 * 
 * // Now all HTTP requests will use the requestInStream API
 * // which provides better streaming support for large responses
 * val response = client.get("https://example.com/large-file")
 * val content = response.bodyAsText()
 */
