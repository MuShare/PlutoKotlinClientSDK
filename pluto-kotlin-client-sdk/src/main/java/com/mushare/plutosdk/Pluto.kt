package com.mushare.plutosdk

import android.content.Context
import android.util.Log
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class Pluto private constructor() {
    enum class State {
        notSignIn,
        loading,
        signIn,
        invalidRefreshToken
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

    enum class LoginType(val identifier: String) {
        @SerializedName("mail")
        MAIL("mail"),

        @SerializedName("google")
        GOOGLE("google"),

        @SerializedName("wechat")
        WECHAT("wechat");

        companion object {
            private val map = values().associateBy(LoginType::identifier)

            fun from(identifier: String): LoginType? {
                return map.getValue(identifier)
            }
        }
    }

    internal val data by lazy { PlutoModel(context) }

    val state: MutableLiveData<State> by lazy {
        MutableLiveData(if (data.isTokenNull) State.notSignIn else State.signIn)
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
            state.value = if (it == null) State.notSignIn else State.signIn
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

    fun handleResponse(
        response: Response<PlutoResponse>,
        success: () -> Unit,
        error: (PlutoError) -> Unit
    ) {
        val plutoResponse = response.body()
        if (plutoResponse != null) {
            plutoResponse.analysis(success, error)
        } else {
            error(parseErrorCodeFromErrorBody(response.errorBody(), gson))
        }
    }
}