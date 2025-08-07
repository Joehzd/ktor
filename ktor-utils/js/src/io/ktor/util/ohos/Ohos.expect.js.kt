/*
 * Copyright 2014-2025 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.util.ohos
import io.ktor.util.NONCE_SIZE_IN_BYTES
import io.ktor.util.hex
import org.khronos.webgl.Uint8Array

public actual fun getNonceString(): String {
    return hex(OhosCrypto.createRandom().generateRandomSync(NONCE_SIZE_IN_BYTES).data.asOhosByteArray() as ByteArray)
}

public actual suspend fun digestBufferArray(name: String,snapshot: ByteArray): Uint8Array {
    val md = OhosCrypto.createMd(name)
    return md.digest(snapshot)
}
