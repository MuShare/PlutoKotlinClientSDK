package com.mushare.plutosdk

import android.content.Context
import android.util.Log
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class Pluto private constructor() {
    enum class State {
        notSignin,
        loading,
        signin
    }

    class PlutoRequestHandler {
        private var call: Call<out Any>? = null
        internal fun setCall(call: Call<out Any>?) {
            this.call = call
        }

        fun cancel() {
            call?.cancel()
        }
    }

    internal val data by lazy { PlutoModel(context) }

    val state: MutableLiveData<State> by lazy {
        MutableLiveData(if (data.isTokenNull) State.notSignin else State.signin)
    }

    internal val gson: Gson by lazy { GsonBuilder().serializeNulls().create() }
    internal val plutoService: PlutoService by lazy {
        Retrofit.Builder()
            .addConverterFactory(
                GsonConverterFactory.create(gson)
            )
            .baseUrl(server)
            .client(OkHttpClient())
            .build()
            .create(PlutoService::class.java)
    }

    internal fun getLanguage(): String {
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

    init {
        getAccessToken(completion = {
            state.postValue(
                if (it == null) {
                    State.notSignin
                } else {
                    State.signin
                }
            )
        })
    }

    companion object {
        private const val TAG = "PlutoSDK"

        @Volatile
        private var instance: Pluto? = null

        private lateinit var server: String
        internal lateinit var appId: String
        internal lateinit var context: Context
        private var isInitialized: Boolean = false

        fun initialize(_context: Context, _server: String, _appId: String) {
            context = _context.applicationContext
            server = _server
            appId = _appId
            isInitialized = true
        }

        fun getInstance(): Pluto? {
            return instance ?: synchronized(this) {
                if (!isInitialized) {
                    Log.e(TAG, "Not initialized.")
                    return null
                }
                return Pluto().also { instance = it }
            }
        }
    }
}