// ktlint-disable filename
/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.ktor.util

public actual val PlatformUtils.platform: Platform by lazy {
    when {
        hasNodeApi() -> Platform.Js(
            Platform.JsPlatform.Node
        )
        hasWindowApi() -> Platform.Js(Platform.JsPlatform.Browser)
        else -> Platform.Ohos
    }
}
