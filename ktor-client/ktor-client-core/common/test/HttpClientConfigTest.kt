import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.plugins.api.createClientPlugin
import kotlin.test.Test
import kotlin.test.assertEquals

/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

class HttpClientConfigTest {

    @Test
    fun testPluginInstalledTwice() {
        var configuration = 0
        var installation = 0
        var first = 0
        var second = 0

        class Config {
            var firstConfig = 0
            var secondConfig = 0
            init {
                configuration++
            }
        }

        val plugin = createClientPlugin("hey", ::Config) {
            installation++
        }

        val client = HttpClient {
            install(plugin) {
                first += 1
            }

            install(plugin) {
                second += 1
            }
        }

        assertEquals(1, configuration)
        assertEquals(1, installation)
        assertEquals(1, first)
        assertEquals(1, second)
    }
}
