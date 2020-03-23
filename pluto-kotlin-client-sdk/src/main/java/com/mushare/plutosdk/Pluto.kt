package com.mushare.plutosdk

import android.content.Context
import android.util.Log
import androidx.core.os.LocaleListCompat
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.*
import kotlin.properties.Delegates

class Pluto private constructor() {
    enum class State {
        notSignin,
        loading,
        signin
    }

    internal val data by lazy { PlutoModel(context!!) }

    private val stateObserverList = mutableListOf<WeakReference<(State) -> Unit>>()
    internal var state: State by Delegates.observable(State.loading) { _, _, new ->
        for (observer in stateObserverList) {
            observer.get()?.let { it(new) }
        }
    }

    private val client by lazy { OkHttpClient() }

    internal val commonHeaders
        get() = Headers.headersOf("Accept-Language", getLanguage())

    private fun getLanguage(): String {
        val locale = LocaleListCompat.getDefault()
            .getFirstMatch(arrayOf("zh-Hans", "zh-Hant", "yue-Hans", "yue-Hant", "en"))
            ?: Locale.getDefault()
        return with(locale.toString()) {
            when {
                contains("Hant") -> "zh-Hant"
                startsWith("zh") || startsWith("yue") -> "zh-Hans"
                else -> "en"
            }
        }
    }

    private fun url(relativeUrl: String): String {
        return "$server/$relativeUrl"
    }

    internal fun requestPost(
        relativeUrl: String,
        bodyJson: JSONObject,
        headers: Headers,
        callback: Callback
    ) {
        val body: RequestBody = bodyJson.toString().toRequestBody()
        val request: Request = Request.Builder()
            .url(url(relativeUrl))
            .headers(headers)
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()
        client.newCall(request).enqueue(callback)
    }

    internal fun requestGet(
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

    fun addStateObserver(observer: ((State) -> Unit)) {
        stateObserverList.add(WeakReference(observer))
    }

    fun removeStateObserver(observer: ((State) -> Unit)) {
        stateObserverList.removeAll {
            val o = it.get()
            o == null || o == observer
        }
    }

    fun removeAllStateObserver() {
        stateObserverList.clear()
    }

    fun currentState() = state

    private fun destroy() {
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
        client.cache?.close()
        removeAllStateObserver()
    }

    init {
        getToken {
            state = if (it == null) {
                State.notSignin
            } else {
                State.signin
            }
        }
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