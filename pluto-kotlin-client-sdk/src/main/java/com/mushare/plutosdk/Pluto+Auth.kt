package com.mushare.plutosdk

import com.mushare.plutosdk.Pluto.Companion.appId
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

fun Pluto.getToken(completion: (String?) -> Unit) {
    val jwt = data.jwt
    val expire = data.expire
    if (jwt == null || expire == null || expire - System.currentTimeMillis() / 1000 < 5 * 60) {
        refreshToken {
            if (it == null) {
                data.clear()
            }
            completion(it)
        }
        return
    }
    completion(data.jwt)
}

private fun Pluto.refreshToken(completion: (String?) -> Unit) {
    val userId = data.userId
    val refreshToken = data.refreshToken
    if (userId == null || refreshToken == null) {
        completion(null)
        return
    }
    val bodyJson = JSONObject()
    bodyJson.put("refresh_token", refreshToken)
    bodyJson.put("user_id", userId)
    bodyJson.put("device_id", data.deviceID)
    bodyJson.put("app_id", appId)
    requestPost("api/auth/refresh", bodyJson, commonHeaders, object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            completion(null)
        }

        override fun onResponse(call: Call, response: Response) {
            val plutoResponse = PlutoResponse(response)
            if (plutoResponse.statusOK()) {
                try {
                    val jwt = plutoResponse.getBody().getString("jwt")
                    if (data.updateJwt(jwt)) {
                        completion(jwt)
                    } else {
                        completion(null)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    completion(null)
                }
            } else {
                completion(null)
            }
        }
    })
}

fun Pluto.getHeaders(completion: (Headers) -> Unit) {
    getToken {
        if (it == null) {
            completion(commonHeaders)
        } else {
            completion(commonHeaders.newBuilder().add("Authorization", "jwt $it").build())
        }
    }
}