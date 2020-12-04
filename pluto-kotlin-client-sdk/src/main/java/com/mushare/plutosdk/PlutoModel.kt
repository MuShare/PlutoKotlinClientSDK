package com.mushare.plutosdk

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.json.JSONObject
import java.util.*

const val SHARED_PREFERENCES_NAME = "PlutoSharedPrefs"

// Save key
const val UUID_SAVE_KEY = "uuid"

internal class PlutoModel(context: Context) {
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    val deviceID: String? by lazy {
        var uuid = sharedPref.getString(UUID_SAVE_KEY, null)
        if (uuid == null) {
            uuid = UUID.randomUUID().toString()
            with(sharedPref.edit()) {
                putString(UUID_SAVE_KEY, uuid)
                apply()
            }
        }
        uuid
    }

    private val accessTokenWrapper = StringWrapper("jwt", sharedPref)
    private val refreshTokenWrapper = StringWrapper("refresh", sharedPref)
    private val expireWrapper = IntWrapper("expire", sharedPref)
    private val subWrapper = IntWrapper("userId", sharedPref)
    private val infoJSONStringWrapper = StringWrapper("infoJSONString", sharedPref)

    var accessToken: String?
        get() = accessTokenWrapper.value
        set(value) {
            accessTokenWrapper.value = value
        }

    var refreshToken: String?
        get() = refreshTokenWrapper.value
        set(value) {
            refreshTokenWrapper.value = value
        }

    var expire: Int?
        get() = expireWrapper.value
        set(value) {
            expireWrapper.value = value
        }

    var sub: Int?
        get() = subWrapper.value
        set(value) {
            subWrapper.value = value
        }

    private var infoJSONString: String?
        get() = infoJSONStringWrapper.value
        set(value) {
            infoJSONStringWrapper.value = value
        }

    private val gson = Gson()

    var user: PlutoUser?
        get() {
            if (infoJSONString == null) {
                return null
            }
            return gson.fromJson(infoJSONString, PlutoUser::class.java)
        }
        set(value) {
            infoJSONString = gson.toJson(value)
        }

    fun updateAccessToken(jwt: String): Boolean {
        val body = JwtUtils.decodeBody(jwt) ?: return false
        return try {
            val json = JSONObject(body)
            sub = json.getInt("sub")
            expire = json.getInt("exp")
            accessToken = jwt
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun clear() {
        accessToken = null
        refreshToken = null
        expire = 0
        sub = null
        infoJSONString = null
    }
}
