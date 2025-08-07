/*
 * Copyright 2014-2025 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.util.ohos

import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array

/**
 * @desc   :
 * @author : houzhendong@moonshot.cn
 * @date   : 2025/8/7 17:18
 */
public fun Uint8Array.asOhosByteArray(): ByteArray {
    return Int8Array(buffer, byteOffset, length).asDynamic()
}
