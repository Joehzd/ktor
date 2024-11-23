/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.js.ohos

import io.ktor.client.fetch.ArrayBuffer
import kotlin.js.*

/**
 * HTTP response headers.
 * @typedef { object }
 * @syscap SystemCapability.Communication.NetStack
 * @since 12
 */
//        export type ResponseHeaders = {
//            [k: string]: string | string[] | undefined;
//        }
public typealias ResponseHeaders = Map<String, Any?>


/**
 * @file
 * @kit NetworkKit
 */
/**
 * Provides WebSocket APIs.
 * @namespace webSocket
 * @syscap SystemCapability.Communication.NetStack
 * @since 6
 */
/**
 * Provides WebSocket APIs.
 * @namespace webSocket
 * @syscap SystemCapability.Communication.NetStack
 * @crossplatform
 * @since 10
 */
/**
 * Provides WebSocket APIs.
 * @namespace webSocket
 * @syscap SystemCapability.Communication.NetStack
 * @crossplatform
 * @atomicservice
 * @since 11
 */
@JsModule("@ohos.net.webSocket")
@JsNonModule
internal external class WebSocket {
    companion object{
        /**
         * Creates a web socket connection.
         * @returns { WebSocket } the WebSocket of the createWebSocket.
         * @syscap SystemCapability.Communication.NetStack
         * @since 6
         */
        /**
         * Creates a web socket connection.
         * @returns { WebSocket } the WebSocket of the createWebSocket.
         * @syscap SystemCapability.Communication.NetStack
         * @crossplatform
         * @since 10
         */
        /**
         * Creates a web socket connection.
         * @returns { WebSocket } the WebSocket of the createWebSocket.
         * @syscap SystemCapability.Communication.NetStack
         * @crossplatform
         * @atomicservice
         * @since 11
         */
        fun createWebSocket(): WebSocket;
    }


    /**
     * @typedef { connection.HttpProxy }
     * @syscap SystemCapability.Communication.NetManager.Core
     * @since 12
     */
//    val HttpProxy = connection.HttpProxy;
        /**
         * Defines the optional parameters carried in the request for establishing a WebSocket connection.
         * @interface WebSocketRequestOptions
         * @syscap SystemCapability.Communication.NetStack
         * @since 6
         */
        /**
         * Defines the optional parameters carried in the request for establishing a WebSocket connection.
         * @interface WebSocketRequestOptions
         * @syscap SystemCapability.Communication.NetStack
         * @crossplatform
         * @since 10
         */
        /**
         * Defines the optional parameters carried in the request for establishing a WebSocket connection.
         * @interface WebSocketRequestOptions
         * @syscap SystemCapability.Communication.NetStack
         * @crossplatform
         * @atomicservice
         * @since 11
         */
        interface WebSocketRequestOptions {
            /**
             * HTTP request header.
             * @type {?Object}
             * @syscap SystemCapability.Communication.NetStack
             * @since 6
             */
            /**
             * HTTP request header.
             * @type {?Object}
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 10
             */
            /**
             * HTTP request header.
             * @type {?Object}
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @atomicservice
             * @since 11
             */
            var header: Any?

            /**
             * File path for client cert.
             * @type {?string}
             * @syscap SystemCapability.Communication.NetStack
             * @since 11
             */
            /**
             * File path for client cert.
             * @type {?string}
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 12
             */
            val caPath: String?

            /**
             * Client cert.
             * @type {?ClientCert}
             * @syscap SystemCapability.Communication.NetStack
             * @since 11
             */
            /**
             * Client cert.
             * @type {?ClientCert}
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 12
             */
            val clientCert: ClientCert?

            /**
             * HTTP proxy configuration. Use 'system' if this filed is not set.
             * @type {?ProxyConfiguration}
             * @syscap SystemCapability.Communication.NetStack
             * @since 12
             */
//            val proxy: ProxyConfiguration?

            /**
             * Self defined protocol.
             * @type {?string}
             * @syscap SystemCapability.Communication.NetStack
             * @since 12
             */
            var protocol: String?
        }

        /**
         * HTTP proxy configuration.
         * system: means that use system proxy configuration.
         * no-proxy: means do not use proxy.
         * object of @type {connection.HttpProxy} means providing custom proxy settings
         * @typedef { 'system' | 'no-proxy' | HttpProxy }
         * @syscap SystemCapability.Communication.NetStack
         * @since 12
         */
//        export type ProxyConfiguration = 'system' | 'no-proxy' | HttpProxy;


        /**
         * The clientCert field of the client certificate, which includes three attributes:
         * client certificate (certPath) and only support PEM format, certificate private key (keyPath),
         * and passphrase (keyPassword).
         * @interface ClientCert
         * @syscap SystemCapability.Communication.NetStack
         * @since 11
         */
        /**
         * The clientCert field of the client certificate, which includes three attributes:
         * client certificate (certPath) and only support PEM format, certificate private key (keyPath),
         * and passphrase (keyPassword).
         * @interface ClientCert
         * @syscap SystemCapability.Communication.NetStack
         * @crossplatform
         * @since 12
         */
        interface ClientCert {
            /**
             * The path to the client certificate file.
             * @type {string}
             * @syscap SystemCapability.Communication.NetStack
             * @since 11
             */
            /**
             * The path to the client certificate file.
             * @type {string}
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 12
             */
            val certPath: String

            /**
             * The path of the client certificate private key file.
             * @type {string}
             * @syscap SystemCapability.Communication.NetStack
             * @since 11
             */
            /**
             * The path of the client certificate private key file.
             * @type {string}
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 12
             */
            val keyPath: String

            /**
             * Client certificate password.
             * @type {?string}
             * @syscap SystemCapability.Communication.NetStack
             * @since 11
             */
            /**
             * Client certificate password.
             * @type {?string}
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 12
             */
            val keyPassword: String?
        }

        /**
         * Defines the optional parameters carried in the request for closing a WebSocket connection.
         * @interface WebSocketCloseOptions
         * @syscap SystemCapability.Communication.NetStack
         * @since 6
         */
        /**
         * Defines the optional parameters carried in the request for closing a WebSocket connection.
         * @interface WebSocketCloseOptions
         * @syscap SystemCapability.Communication.NetStack
         * @crossplatform
         * @since 10
         */
        /**
         * Defines the optional parameters carried in the request for closing a WebSocket connection.
         * @interface WebSocketCloseOptions
         * @syscap SystemCapability.Communication.NetStack
         * @crossplatform
         * @atomicservice
         * @since 11
         */
        interface WebSocketCloseOptions {
            /**
             * Error code.
             * @type {?number}
             * @syscap SystemCapability.Communication.NetStack
             * @since 6
             */
            /**
             * Error code.
             * @type {?number}
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 10
             */
            /**
             * Error code.
             * @type {?number}
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @atomicservice
             * @since 11
             */
            var code: Int?
            /**
             * Error cause.
             * @type {?string}
             * @syscap SystemCapability.Communication.NetStack
             * @since 6
             */
            /**
             * Error cause.
             * @type {?string}
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 10
             */
            /**
             * Error cause.
             * @type {?string}
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @atomicservice
             * @since 11
             */
            var reason: String
        }

        /**
         * The result for closing a WebSocket connection.
         * @interface CloseResult
         * @syscap SystemCapability.Communication.NetStack
         * @crossplatform
         * @since 10
         */
        /**
         * The result for closing a WebSocket connection.
         * @interface CloseResult
         * @syscap SystemCapability.Communication.NetStack
         * @crossplatform
         * @atomicservice
         * @since 11
         */
        interface CloseResult {
            /**
             * Error code.
             * @type {number}
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 10
             */
            /**
             * Error code.
             * @type {number}
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @atomicservice
             * @since 11
             */
            val code: Int
            /**
             * Error cause.
             * @type {string}
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 10
             */
            /**
             * Error cause.
             * @type {string}
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @atomicservice
             * @since 11
             */
            val reason: String
        }


    /**
         * <p>Defines a WebSocket object. Before invoking WebSocket APIs,
         * you need to call webSocket.createWebSocket to create a WebSocket object.</p>
         * @interface WebSocket
         * @syscap SystemCapability.Communication.NetStack
         * @since 6
         */
        /**
         * <p>Defines a WebSocket object. Before invoking WebSocket APIs,
         * you need to call webSocket.createWebSocket to create a WebSocket object.</p>
         * @interface WebSocket
         * @syscap SystemCapability.Communication.NetStack
         * @crossplatform
         * @since 10
         */
        /**
         * <p>Defines a WebSocket object. Before invoking WebSocket APIs,
         * you need to call webSocket.createWebSocket to create a WebSocket object.</p>
         * @interface WebSocket
         * @syscap SystemCapability.Communication.NetStack
         * @crossplatform
         * @atomicservice
         * @since 11
         */
        interface WebSocket {
            /**
             * Initiates a WebSocket request to establish a WebSocket connection to a given URL.
             * @permission ohos.permission.INTERNET
             * @param { string } url - URL for establishing a WebSocket connection.
             * @param { AsyncCallback<boolean> } callback - the callback of connect.
             * @throws { BusinessError } 401 - Parameter error.
             * @throws { BusinessError } 201 - Permission denied.
             * @syscap SystemCapability.Communication.NetStack
             * @since 6
             */
            /**
             * Initiates a WebSocket request to establish a WebSocket connection to a given URL.
             * @permission ohos.permission.INTERNET
             * @param { string } url URL for establishing a WebSocket connection.
             * @param { AsyncCallback<boolean> } callback - the callback of connect.
             * @throws { BusinessError } 401 - Parameter error.
             * @throws { BusinessError } 201 - Permission denied.
             * @throws { BusinessError } 2302999 - Websocket other unknown error.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 10
             */
            /**
             * Initiates a WebSocket request to establish a WebSocket connection to a given URL.
             * @permission ohos.permission.INTERNET
             * @param { string } url URL for establishing a WebSocket connection.
             * @param { AsyncCallback<boolean> } callback - the callback of connect.
             * @throws { BusinessError } 401 - Parameter error.
             * @throws { BusinessError } 201 - Permission denied.
             * @throws { BusinessError } 2302999 - Websocket other unknown error.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @atomicservice
             * @since 11
             */
            /**
             * Initiates a WebSocket request to establish a WebSocket connection to a given URL.
             * @permission ohos.permission.INTERNET
             * @param { string } url URL for establishing a WebSocket connection.
             * @param { AsyncCallback<boolean> } callback - the callback of connect.
             * @throws { BusinessError } 401 - Parameter error.
             * @throws { BusinessError } 201 - Permission denied.
             * @throws { BusinessError } 2302001 - Websocket url error.
             * @throws { BusinessError } 2302002 - Websocket certificate file does not exist.
             * @throws { BusinessError } 2302003 - Websocket connection already exists.
             * @throws { BusinessError } 2302999 - Websocket other unknown error.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @atomicservice
             * @since 12
             */
            fun connect(url: String, callback: (AsyncCallback<Boolean>)->Unit)

            /**
             * Initiates a WebSocket request to establish a WebSocket connection to a given URL.
             * @permission ohos.permission.INTERNET
             * @param { string } url URL for establishing a WebSocket connection.
             * @param { WebSocketRequestOptions } options - Optional parameters {@link WebSocketRequestOptions}.
             * @param { AsyncCallback<boolean> } callback - the callback of connect.
             * @throws { BusinessError } 401 - Parameter error.
             * @throws { BusinessError } 201 - Permission denied.
             * @syscap SystemCapability.Communication.NetStack
             * @since 6
             */
            /**
             * Initiates a WebSocket request to establish a WebSocket connection to a given URL.
             * @permission ohos.permission.INTERNET
             * @param { string } url URL for establishing a WebSocket connection.
             * @param { WebSocketRequestOptions } options - Optional parameters {@link WebSocketRequestOptions}.
             * @param { AsyncCallback<boolean> } callback - the callback of connect.
             * @throws { BusinessError } 401 - Parameter error.
             * @throws { BusinessError } 201 - Permission denied.
             * @throws { BusinessError } 2302999 - Websocket other unknown error.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 10
             */
            /**
             * Initiates a WebSocket request to establish a WebSocket connection to a given URL.
             * @permission ohos.permission.INTERNET
             * @param { string } url URL for establishing a WebSocket connection.
             * @param { WebSocketRequestOptions } options - Optional parameters {@link WebSocketRequestOptions}.
             * @param { AsyncCallback<boolean> } callback - the callback of connect.
             * @throws { BusinessError } 401 - Parameter error.
             * @throws { BusinessError } 201 - Permission denied.
             * @throws { BusinessError } 2302999 - Websocket other unknown error.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @atomicservice
             * @since 11
             */
            /**
             * Initiates a WebSocket request to establish a WebSocket connection to a given URL.
             * @permission ohos.permission.INTERNET
             * @param { string } url URL for establishing a WebSocket connection.
             * @param { WebSocketRequestOptions } options - Optional parameters {@link WebSocketRequestOptions}.
             * @param { AsyncCallback<boolean> } callback - the callback of connect.
             * @throws { BusinessError } 401 - Parameter error.
             * @throws { BusinessError } 201 - Permission denied.
             * @throws { BusinessError } 2302001 - Websocket url error.
             * @throws { BusinessError } 2302002 - Websocket certificate file does not exist.
             * @throws { BusinessError } 2302003 - Websocket connection already exists.
             * @throws { BusinessError } 2302999 - Websocket other unknown error.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @atomicservice
             * @since 12
             */
            fun connect(url: String, options: WebSocketRequestOptions, callback: (AsyncCallback<Boolean>)->Unit)

            /**
             * Initiates a WebSocket request to establish a WebSocket connection to a given URL.
             * @permission ohos.permission.INTERNET
             * @param { string } url URL for establishing a WebSocket connection.
             * @param { WebSocketRequestOptions } options - Optional parameters {@link WebSocketRequestOptions}.
             * @returns { Promise<boolean> } The promise returned by the function.
             * @throws { BusinessError } 401 - Parameter error.
             * @throws { BusinessError } 201 - Permission denied.
             * @syscap SystemCapability.Communication.NetStack
             * @since 6
             */
            /**
             * Initiates a WebSocket request to establish a WebSocket connection to a given URL.
             * @permission ohos.permission.INTERNET
             * @param { string } url URL for establishing a WebSocket connection.
             * @param { WebSocketRequestOptions } options - Optional parameters {@link WebSocketRequestOptions}.
             * @returns { Promise<boolean> } The promise returned by the function.
             * @throws { BusinessError } 401 - Parameter error.
             * @throws { BusinessError } 201 - Permission denied.
             * @throws { BusinessError } 2302999 - Websocket other unknown error.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 10
             */
            /**
             * Initiates a WebSocket request to establish a WebSocket connection to a given URL.
             * @permission ohos.permission.INTERNET
             * @param { string } url URL for establishing a WebSocket connection.
             * @param { WebSocketRequestOptions } options - Optional parameters {@link WebSocketRequestOptions}.
             * @returns { Promise<boolean> } The promise returned by the function.
             * @throws { BusinessError } 401 - Parameter error.
             * @throws { BusinessError } 201 - Permission denied.
             * @throws { BusinessError } 2302999 - Websocket other unknown error.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @atomicservice
             * @since 11
             */
            /**
             * Initiates a WebSocket request to establish a WebSocket connection to a given URL.
             * @permission ohos.permission.INTERNET
             * @param { string } url URL for establishing a WebSocket connection.
             * @param { WebSocketRequestOptions } options - Optional parameters {@link WebSocketRequestOptions}.
             * @returns { Promise<boolean> } The promise returned by the function.
             * @throws { BusinessError } 401 - Parameter error.
             * @throws { BusinessError } 201 - Permission denied.
             * @throws { BusinessError } 2302001 - Websocket url error.
             * @throws { BusinessError } 2302002 - Websocket certificate file does not exist.
             * @throws { BusinessError } 2302003 - Websocket connection already exists.
             * @throws { BusinessError } 2302999 - Websocket other unknown error.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @atomicservice
             * @since 12
             */
            fun connect(url: String, options: WebSocketRequestOptions?): Promise<Boolean>;

            /**
             * Sends data through a WebSocket connection.
             * @permission ohos.permission.INTERNET
             * @param { string | ArrayBuffer } data - Data to send. It can be a string(API 6) or an ArrayBuffer(API 8).
             * @param { AsyncCallback<boolean> } callback - the callback of send.
             * @throws { BusinessError } 401 - Parameter error.
             * @throws { BusinessError } 201 - Permission denied.
             * @syscap SystemCapability.Communication.NetStack
             * @since 6
             */
            /**
             * Sends data through a WebSocket connection.
             * @permission ohos.permission.INTERNET
             * @param { string | ArrayBuffer } data - Data to send. It can be a string(API 6) or an ArrayBuffer(API 8).
             * @param { AsyncCallback<boolean> } callback - the callback of send.
             * @throws { BusinessError } 401 - Parameter error.
             * @throws { BusinessError } 201 - Permission denied.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 10
             */
            /**
             * Sends data through a WebSocket connection.
             * @permission ohos.permission.INTERNET
             * @param { string | ArrayBuffer } data - Data to send. It can be a string(API 6) or an ArrayBuffer(API 8).
             * @param { AsyncCallback<boolean> } callback - the callback of send.
             * @throws { BusinessError } 401 - Parameter error.
             * @throws { BusinessError } 201 - Permission denied.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @atomicservice
             * @since 11
             */
            fun send(data: dynamic, callback: (AsyncCallback<Boolean>)->Unit)

            /**
             * Sends data through a WebSocket connection.
             * @permission ohos.permission.INTERNET
             * @param { string | ArrayBuffer } data - Data to send. It can be a string(API 6) or an ArrayBuffer(API 8).
             * @returns { Promise<boolean> } The promise returned by the function.
             * @throws { BusinessError } 401 - Parameter error.
             * @throws { BusinessError } 201 - Permission denied.
             * @syscap SystemCapability.Communication.NetStack
             * @since 6
             */
            /**
             * Sends data through a WebSocket connection.
             * @permission ohos.permission.INTERNET
             * @param { string | ArrayBuffer } data - Data to send. It can be a string(API 6) or an ArrayBuffer(API 8).
             * @returns { Promise<boolean> } The promise returned by the function.
             * @throws { BusinessError } 401 - Parameter error.
             * @throws { BusinessError } 201 - Permission denied.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 10
             */
            /**
             * Sends data through a WebSocket connection.
             * @permission ohos.permission.INTERNET
             * @param { string | ArrayBuffer } data - Data to send. It can be a string(API 6) or an ArrayBuffer(API 8).
             * @returns { Promise<boolean> } The promise returned by the function.
             * @throws { BusinessError } 401 - Parameter error.
             * @throws { BusinessError } 201 - Permission denied.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @atomicservice
             * @since 11
             */
            fun send(data: dynamic): Promise<Boolean>;

            /**
             * Closes a WebSocket connection.
             * @permission ohos.permission.INTERNET
             * @param { AsyncCallback<boolean> } callback - the callback of close.
             * @throws { BusinessError } 401 - Parameter error.
             * @throws { BusinessError } 201 - Permission denied.
             * @syscap SystemCapability.Communication.NetStack
             * @since 6
             */
            /**
             * Closes a WebSocket connection.
             * @permission ohos.permission.INTERNET
             * @param { AsyncCallback<boolean> } callback - the callback of close.
             * @throws { BusinessError } 401 - Parameter error.
             * @throws { BusinessError } 201 - Permission denied.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 10
             */
            /**
             * Closes a WebSocket connection.
             * @permission ohos.permission.INTERNET
             * @param { AsyncCallback<boolean> } callback - the callback of close.
             * @throws { BusinessError } 401 - Parameter error.
             * @throws { BusinessError } 201 - Permission denied.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @atomicservice
             * @since 11
             */
            fun close(callback: (AsyncCallback<Boolean>)->Unit)

            /**
             * Closes a WebSocket connection.
             * @permission ohos.permission.INTERNET
             * @param { WebSocketCloseOptions } options - Optional parameters {@link WebSocketCloseOptions}.
             * @param { AsyncCallback<boolean> } callback - the callback of close.
             * @throws { BusinessError } 401 - Parameter error.
             * @throws { BusinessError } 201 - Permission denied.
             * @syscap SystemCapability.Communication.NetStack
             * @since 6
             */
            /**
             * Closes a WebSocket connection.
             * @permission ohos.permission.INTERNET
             * @param { WebSocketCloseOptions } options - Optional parameters {@link WebSocketCloseOptions}.
             * @param { AsyncCallback<boolean> } callback - the callback of close.
             * @throws { BusinessError } 401 - Parameter error.
             * @throws { BusinessError } 201 - Permission denied.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 10
             */
            /**
             * Closes a WebSocket connection.
             * @permission ohos.permission.INTERNET
             * @param { WebSocketCloseOptions } options - Optional parameters {@link WebSocketCloseOptions}.
             * @param { AsyncCallback<boolean> } callback - the callback of close.
             * @throws { BusinessError } 401 - Parameter error.
             * @throws { BusinessError } 201 - Permission denied.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @atomicservice
             * @since 11
             */
            fun close(options: WebSocketCloseOptions, callback: (AsyncCallback<Boolean>)->Unit)

            /**
             * Closes a WebSocket connection.
             * @permission ohos.permission.INTERNET
             * @param { WebSocketCloseOptions } options - Optional parameters {@link WebSocketCloseOptions}.
             * @returns { Promise<boolean> } The promise returned by the function.
             * @throws { BusinessError } 401 - Parameter error.
             * @throws { BusinessError } 201 - Permission denied.
             * @syscap SystemCapability.Communication.NetStack
             * @since 6
             */
            /**
             * Closes a WebSocket connection.
             * @permission ohos.permission.INTERNET
             * @param { WebSocketCloseOptions } options - Optional parameters {@link WebSocketCloseOptions}.
             * @returns { Promise<boolean> } The promise returned by the function.
             * @throws { BusinessError } 401 - Parameter error.
             * @throws { BusinessError } 201 - Permission denied.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 10
             */
            /**
             * Closes a WebSocket connection.
             * @permission ohos.permission.INTERNET
             * @param { WebSocketCloseOptions } options - Optional parameters {@link WebSocketCloseOptions}.
             * @returns { Promise<boolean> } The promise returned by the function.
             * @throws { BusinessError } 401 - Parameter error.
             * @throws { BusinessError } 201 - Permission denied.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @atomicservice
             * @since 11
             */
            fun close(options: WebSocketCloseOptions?): Promise<Boolean>

            /**
             * Enables listening for the open events of a WebSocket connection.
             * @param { 'open' } type - event indicating that a WebSocket connection has been opened.
             * @param { AsyncCallback<Object> } callback - the callback used to return the result.
             * @syscap SystemCapability.Communication.NetStack
             * @since 6
             */
            /**
             * Enables listening for the open events of a WebSocket connection.
             * @param { 'open' } type - event indicating that a WebSocket connection has been opened.
             * @param { AsyncCallback<Object> } callback - the callback used to return the result.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 10
             */
            /**
             * Enables listening for the open events of a WebSocket connection.
             * @param { 'open' } type - event indicating that a WebSocket connection has been opened.
             * @param { AsyncCallback<Object> } callback - the callback used to return the result.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @atomicservice
             * @since 11
             */
            fun on(type: String/*'open'*/, callback: (Any)->Unit)

            /**
             * Cancels listening for the open events of a WebSocket connection.
             * @param { 'open' } type - event indicating that a WebSocket connection has been opened.
             * @param { AsyncCallback<Object> } callback - the callback used to return the result.
             * @syscap SystemCapability.Communication.NetStack
             * @since 6
             */
            /**
             * Cancels listening for the open events of a WebSocket connection.
             * @param { 'open' } type - event indicating that a WebSocket connection has been opened.
             * @param { AsyncCallback<Object> } callback the callback used to return the result.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 10
             */
            /**
             * Cancels listening for the open events of a WebSocket connection.
             * @param { 'open' } type - event indicating that a WebSocket connection has been opened.
             * @param { AsyncCallback<Object> } callback the callback used to return the result.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @atomicservice
             * @since 11
             */
            fun off(type: String/*'open'*/, callback: ((Any)->Unit)?)

            /**
             * Enables listening for the message events of a WebSocket connection.
             * data in AsyncCallback can be a string(API 6) or an ArrayBuffer(API 8).
             * @param { 'message' } type - event indicating that a message has been received from the server.
             * @param { AsyncCallback<string | ArrayBuffer> } callback - the callback used to return the result.
             * @syscap SystemCapability.Communication.NetStack
             * @since 6
             */
            /**
             * Enables listening for the message events of a WebSocket connection.
             * data in AsyncCallback can be a string(API 6) or an ArrayBuffer(API 8).
             * @param { 'message' } type - event indicating that a message has been received from the server.
             * @param { AsyncCallback<string | ArrayBuffer> } callback - the callback used to return the result.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 10
             */
            /**
             * Enables listening for the message events of a WebSocket connection.
             * data in AsyncCallback can be a string(API 6) or an ArrayBuffer(API 8).
             * @param { 'message' } type - event indicating that a message has been received from the server.
             * @param { AsyncCallback<string | ArrayBuffer> } callback - the callback used to return the result.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @atomicservice
             * @since 11
             */
            fun on(type: String/*'message'*/, callback: (BusinessError,Any)->Unit)

            /**
             * Cancels listening for the message events of a WebSocket connection.
             * data in AsyncCallback can be a string(API 6) or an ArrayBuffer(API 8).
             * @param { 'message' } type - event indicating that a message has been received from the server.
             * @param { AsyncCallback<string | ArrayBuffer> } callback - the callback used to return the result.
             * @syscap SystemCapability.Communication.NetStack
             * @since 6
             */
            /**
             * Cancels listening for the message events of a WebSocket connection.
             * data in AsyncCallback can be a string(API 6) or an ArrayBuffer(API 8).
             * @param { 'message' } type - event indicating that a message has been received from the server.
             * @param { AsyncCallback<string | ArrayBuffer> } callback - the callback used to return the result.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 10
             */
            /**
             * Cancels listening for the message events of a WebSocket connection.
             * data in AsyncCallback can be a string(API 6) or an ArrayBuffer(API 8).
             * @param { 'message' } type - event indicating that a message has been received from the server.
             * @param { AsyncCallback<string | ArrayBuffer> } callback - the callback used to return the result.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @atomicservice
             * @since 11
             */
            fun off(type: String/*'message'*/, callback: (org.khronos.webgl.ArrayBuffer)?)

            /**
             * Enables listening for the close events of a WebSocket connection.
             * @param { 'close' } type - event indicating that a WebSocket connection has been closed.
             * @param { AsyncCallback<CloseResult> } callback - the callback used to return the result.
             * <br>close indicates the close error code and reason indicates the error code description.
             * @syscap SystemCapability.Communication.NetStack
             * @since 6
             */
            /**
             * Enables listening for the close events of a WebSocket connection.
             * @param { 'close' } type - event indicating that a WebSocket connection has been closed.
             * @param { AsyncCallback<CloseResult> } callback - the callback used to return the result.
             * <br>close indicates the close error code and reason indicates the error code description.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 10
             */
            /**
             * Enables listening for the close events of a WebSocket connection.
             * @param { 'close' } type - event indicating that a WebSocket connection has been closed.
             * @param { AsyncCallback<CloseResult> } callback - the callback used to return the result.
             * <br>close indicates the close error code and reason indicates the error code description.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @atomicservice
             * @since 11
             */
            fun on(type: String/*'close'*/, callback: (BusinessError,CloseResult)->Unit)

            /**
             * Cancels listening for the close events of a WebSocket connection.
             * @param { 'close' } type - event indicating that a WebSocket connection has been closed.
             * @param { AsyncCallback<CloseResult> } callback - the callback used to return the result.
             * <br>close indicates the close error code and reason indicates the error code description.
             * @syscap SystemCapability.Communication.NetStack
             * @since 6
             */
            /**
             * Cancels listening for the close events of a WebSocket connection.
             * @param { 'close' } type - event indicating that a WebSocket connection has been closed.
             * @param { AsyncCallback<CloseResult> } callback - the callback used to return the result.
             * <br>close indicates the close error code and reason indicates the error code description.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 10
             */
            /**
             * Cancels listening for the close events of a WebSocket connection.
             * @param { 'close' } type - event indicating that a WebSocket connection has been closed.
             * @param { AsyncCallback<CloseResult> } callback - the callback used to return the result.
             * <br>close indicates the close error code and reason indicates the error code description.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @atomicservice
             * @since 11
             */
            fun off(type: String/*'close'*/, callback: ((BusinessError,CloseResult)->Unit)?)

            /**
             * Enables listening for the error events of a WebSocket connection.
             * @param { 'error' } type - event indicating the WebSocket connection has encountered an error.
             * @param { ErrorCallback } callback - the callback used to return the result.
             * @syscap SystemCapability.Communication.NetStack
             * @since 6
             */
            /**
             * Enables listening for the error events of a WebSocket connection.
             * @param { 'error' } type - event indicating the WebSocket connection has encountered an error.
             * @param { ErrorCallback } callback - the callback used to return the result.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 10
             */
            /**
             * Enables listening for the error events of a WebSocket connection.
             * @param { 'error' } type - event indicating the WebSocket connection has encountered an error.
             * @param { ErrorCallback } callback - the callback used to return the result.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @atomicservice
             * @since 11
             */
            fun on(type: String/*'error'*/, callback: (Error)->Unit)

            /**
             * Cancels listening for the error events of a WebSocket connection.
             * @param { 'error' } type - event indicating the WebSocket connection has encountered an error.
             * @param { ErrorCallback } callback - the callback used to return the result.
             * @syscap SystemCapability.Communication.NetStack
             * @since 6
             */
            /**
             * Cancels listening for the error events of a WebSocket connection.
             * @param { 'error' } type - event indicating the WebSocket connection has encountered an error.
             * @param { ErrorCallback } callback - the callback used to return the result.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 10
             */
            /**
             * Cancels listening for the error events of a WebSocket connection.
             * @param { 'error' } type - event indicating the WebSocket connection has encountered an error.
             * @param { ErrorCallback } callback - the callback used to return the result.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @atomicservice
             * @since 11
             */
            fun off(type: String/*'error'*/, callback: ((Error)->Unit)?)

            /**
             * Enables listening for receiving data ends events of a WebSocket connection.
             * @param { 'dataEnd' } type - event indicating the WebSocket connection has received data ends.
             * @param { Callback<void> } callback - the callback used to return the result.
             * @syscap SystemCapability.Communication.NetStack
             * @since 11
             */
            /**
             * Enables listening for receiving data ends events of a WebSocket connection.
             * @param { 'dataEnd' } type - event indicating the WebSocket connection has received data ends.
             * @param { Callback<void> } callback - the callback used to return the result.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 12
             */
            fun on(type: String/*'dataEnd'*/, callback: (Callback<Unit>)->Unit)

            /**
             * Cancels listening for receiving data ends events of a WebSocket connection.
             * @param { 'dataEnd' } type - event indicating the WebSocket connection has received data ends.
             * @param { Callback<void> } [ callback ] - the callback used to return the result.
             * @syscap SystemCapability.Communication.NetStack
             * @since 11
             */
            /**
             * Cancels listening for receiving data ends events of a WebSocket connection.
             * @param { 'dataEnd' } type - event indicating the WebSocket connection has received data ends.
             * @param { Callback<void> } [ callback ] - the callback used to return the result.
             * @syscap SystemCapability.Communication.NetStack
             * @crossplatform
             * @since 12
             */
            fun off(type: String/*'dataEnd'*/, callback: ((Callback<Unit>)->Unit)?)

            /**
             * Registers an observer for HTTP Response Header events.
             * @param { 'headerReceive'} type - Indicates Event name.
             * @param { Callback<ResponseHeaders> } callback - the callback used to return the result.
             * @syscap SystemCapability.Communication.NetStack
             * @since 12
             */
            fun on(type: String/*'headerReceive'*/, callback: (Callback<ResponseHeaders>)->Unit)

            /**
             * Unregisters the observer for HTTP Response Header events.
             * @param { 'headerReceive' } type - Indicates Event name.
             * @param { Callback<ResponseHeaders> } [callback] - the callback used to return the result.
             * @syscap SystemCapability.Communication.NetStack
             * @since 12
             */
            fun off(type: String/*'headerReceive'*/, callback: ((Callback<ResponseHeaders>)->Unit)?)
        }
}
