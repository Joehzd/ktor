/*
 * Copyright 2014-2025 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.util.ohos
import org.khronos.webgl.Uint8Array
public actual fun getNonceString(): String {
    return ""
}

public actual suspend fun digestBufferArray(name: String,snapshot: ByteArray): Uint8Array {
    return Uint8Array(0)
}
