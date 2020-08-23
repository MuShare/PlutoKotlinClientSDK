package com.mushare.plutosdk

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.annotations.SerializedName
import org.json.JSONObject
import java.lang.Exception
import java.util.*

const val SHARED_PREFERENCES_NAME = "PlutoSharedPrefs"

// Save key
const val UUID_SAVE_KEY = "uuid"
const val JWT_SAVE_KEY = "jwt"
const val REFRESH_TOKEN_SAVE_KEY = "refresh"
const val EXPIRE_SAVE_KEY = "expire"
const val USER_ID_SAVE_KEY = "userId"
const val MAIL_SAVE_KEY = "mail"
const val NAME_SAVE_KEY = "name"
const val AVATAR_SAVE_KEY = "avatar"

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

    private var _jwt = StringWrapper(JWT_SAVE_KEY, sharedPref)
    val jwt get() = _jwt.value
    private var _refreshToken = StringWrapper(REFRESH_TOKEN_SAVE_KEY, sharedPref)
    val refreshToken get() = _refreshToken.value
    private var _expire = IntWrapper(EXPIRE_SAVE_KEY, sharedPref)
    val expire get() = _expire.value
    private var _userId = IntWrapper(USER_ID_SAVE_KEY, sharedPref)
    val userId get() = _userId.value

    private var _name = StringWrapper(NAME_SAVE_KEY, sharedPref)
    private var _avatar = StringWrapper(AVATAR_SAVE_KEY, sharedPref)

    var user: PlutoUser?
        get() {
            return if (_userId.value == null || _name.value == null || _avatar.value == null) null
            else PlutoUser(_userId.value!!, _avatar.value!!, _name.value!!)
        }
        set(value) {
            _userId.value = value?.id
            _name.value = value?.name
            _avatar.value = value?.avatar
        }

    fun updateRefreshToken(refreshToken: String) {
        _refreshToken.value = refreshToken
    }

    fun updateJwt(jwt: String): Boolean {
        val body = JwtUtils.decodeBody(jwt) ?: return false
        return try {
            val json = JSONObject(body)
            _userId.value = json.getInt("sub")
            _expire.value = json.getInt("exp")
            _jwt.value = jwt
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun clear() {
        _jwt.value = null
        _refreshToken.value = null
        _expire.value = null
        user = null
    }
}

data class PlutoUser(
    @field:SerializedName("sub") var id: Int,
    @field:SerializedName("avatar") var avatar: String,
    @field:SerializedName("name") var name: String
)