package com.mushare.plutosdk

import android.content.Context
import android.util.Log
import androidx.core.os.LocaleListCompat
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.properties.Delegates

class Pluto {
    enum class State {
        notSignin,
        loading,
        signin
    }

    init {
        refreshToken {
            state = if (it == null) {
                State.notSignin
            } else {
                State.signin
            }
        }
    }

    internal val data by lazy { PlutoModel(context!!) }

    private var stateObserver: ((State) -> Unit)? = null
    internal var state: State by Delegates.observable(State.loading) { _, _, new ->
        stateObserver?.let { it(new) }
    }

    private val client by lazy { OkHttpClient() }

    internal val commonHeaders by lazy {
        Headers.headersOf("Accept-Language", LocaleListCompat.getDefault().toLanguageTags())
    }

    private fun url(relativeUrl: String): String {
        return "$server/$relativeUrl"
    }

    internal fun postRequest(
        relativeUrl: String,
        bodyJson: JSONObject,
        headers: Headers,
        callback: Callback
    ) {
        val body: RequestBody = bodyJson.toString().toRequestBody()
        val request: Request = Request.Builder()
            .url(url(relativeUrl))
            .headers(headers)
            .post(body)
            .build()
        client.newCall(request).enqueue(callback)
    }

    internal fun getRequest(
        relativeUrl: String,
        headers: Headers,
        callback: Callback
    ) {
        val request: Request = Request.Builder()
            .url(url(relativeUrl))
            .headers(headers)
            .build()
        client.newCall(request).enqueue(callback)
    }

    fun observeState(observer: ((State) -> Unit)?) {
        stateObserver = observer
    }

    fun currentState() = state

    private fun destroy() {
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
        client.cache?.close()
    }

    companion object {
        private const val TAG = "PlutoSDK"

        private var instance: Pluto? = null

        private var server: String? = null
        internal var appId: String? = null
        internal var context: Context? = null

        fun initialize(_context: Context, _server: String, _appId: String) {
            context = _context.applicationContext
            server = _server
            appId = _appId
        }

        fun getInstance(): Pluto? {
            if (context == null || server == null || appId == null) {
                Log.e(TAG, "Not initialized.")
                return null
            }
            if (instance == null) instance = Pluto()
            return instance
        }

        fun destroy() {
            context = null
            server = null
            appId = null
            instance?.destroy()
            instance = null
        }
    }
}