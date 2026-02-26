/*
 * Copyright 2014-2026 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.util.ohos
import org.khronos.webgl.*
/**
 * @desc   :
 * @author : houzhendong@moonshot.cn
 * @date   : 2025/8/7 17:32
 */

public expect suspend fun digestBufferArray(name: String,snapshot: ByteArray): Uint8Array

public expect fun getNonceString(): String
