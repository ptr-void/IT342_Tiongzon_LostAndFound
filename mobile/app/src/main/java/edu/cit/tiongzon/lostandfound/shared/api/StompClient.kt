package edu.cit.tiongzon.lostandfound.shared.api

import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class StompClient(private val token: String) {

    private val TAG = "StompClient"
    private val BASE_WS_URL = "ws://10.0.2.2:8080/api/ws-native"

    private var ws: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(10, TimeUnit.SECONDS)
        .build()

    private val pendingSubscriptions = mutableListOf<Triple<String, String, (String) -> Unit>>()
    private val subscriptions = mutableMapOf<String, (String) -> Unit>()
    private var connected = false
    private var onConnected: (() -> Unit)? = null

    fun connect(onConnected: (() -> Unit)? = null) {
        this.onConnected = onConnected
        val request = Request.Builder()
            .url(BASE_WS_URL)
            .addHeader("Authorization", "Bearer $token")
            .build()

        ws = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket opened, sending STOMP CONNECT")
                
                val connectFrame = buildString {
                    append("CONNECT\n")
                    append("accept-version:1.2\n")
                    append("heart-beat:0,0\n")
                    append("Authorization:Bearer $token\n")
                    append("\n")
                    append("\u0000")
                }
                webSocket.send(connectFrame)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "STOMP frame received: ${text.take(200)}")
                val command = text.substringBefore("\n").trim()

                when (command) {
                    "CONNECTED" -> {
                        Log.d(TAG, "STOMP CONNECTED")
                        connected = true
                        onConnected?.invoke()
                        
                        pendingSubscriptions.forEach { (destination, id, handler) ->
                            subscriptions[destination] = handler
                            sendSubscribe(destination, id)
                        }
                        pendingSubscriptions.clear()
                    }
                    "MESSAGE" -> {
                        val destination = extractHeader(text, "destination")
                        val body = text.substringAfter("\n\n").trimEnd('\u0000')
                        Log.d(TAG, "MESSAGE on $destination: $body")
                        subscriptions[destination]?.invoke(body)
                    }
                    "ERROR" -> {
                        Log.e(TAG, "STOMP ERROR: $text")
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket failure: ${t.message}")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $reason")
                connected = false
            }
        })
    }

    fun subscribe(destination: String, id: String, handler: (String) -> Unit) {
        if (connected) {
            subscriptions[destination] = handler
            sendSubscribe(destination, id)
        } else {
            pendingSubscriptions.add(Triple(destination, id, handler))
        }
    }

    fun send(destination: String, body: JSONObject) {
        val bodyStr = body.toString()
        val frame = buildString {
            append("SEND\n")
            append("destination:$destination\n")
            append("content-type:application/json\n")
            append("content-length:${bodyStr.toByteArray().size}\n")
            append("\n")
            append(bodyStr)
            append("\u0000")
        }
        val success = ws?.send(frame) ?: false
        Log.d(TAG, "Sent STOMP message to $destination: success=$success")
    }

    fun disconnect() {
        val disconnectFrame = "DISCONNECT\n\n\u0000"
        ws?.send(disconnectFrame)
        ws?.close(1000, "User disconnected")
        ws = null
        connected = false
        subscriptions.clear()
        pendingSubscriptions.clear()
    }

    private fun sendSubscribe(destination: String, id: String) {
        val frame = buildString {
            append("SUBSCRIBE\n")
            append("id:$id\n")
            append("destination:$destination\n")
            append("\n")
            append("\u0000")
        }
        ws?.send(frame)
        Log.d(TAG, "Subscribed to $destination")
    }

    private fun extractHeader(frame: String, key: String): String {
        return frame.lines()
            .firstOrNull { it.startsWith("$key:") }
            ?.substringAfter("$key:")
            ?.trim() ?: ""
    }
}
