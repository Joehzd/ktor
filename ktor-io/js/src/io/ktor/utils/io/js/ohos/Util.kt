package io.ktor.utils.io.js.ohos

import io.ktor.utils.io.charsets.Decoder
import org.khronos.webgl.ArrayBufferView
import org.khronos.webgl.Uint8Array

@JsModule("@ohos.util")
@JsNonModule
public external class Util {
    public class TextDecoder(encoding: String, options: dynamic = definedExternally) {
        public val encoding: String

       public fun decodeWithStream(buffer: Uint8Array, options: dynamic = definedExternally): String
       public fun decodeToString(input: Uint8Array, options: dynamic = definedExternally): String
    }
    public class TextEncoder {
        public fun encode(input: String): Uint8Array
    }
}

internal fun Util.TextDecoder.toKtor(): Decoder = object : Decoder {
    override fun decode(): String = decodeWithStream(Uint8Array(0))
    override fun decode(buffer: ArrayBufferView): String {
        return decodeWithStream(Uint8Array(buffer.buffer, buffer.byteOffset, buffer.byteLength))
    }
    override fun decode(buffer: ArrayBufferView, options: dynamic): String {
        return decodeWithStream(Uint8Array(buffer.buffer, buffer.byteOffset, buffer.byteLength), options)
    }
}
