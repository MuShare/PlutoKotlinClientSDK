package com.mushare.plutosdk

import com.mushare.plutosdk.Pluto.Companion.appId
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.util.*

fun Pluto.getToken(completion: (String?) -> Unit) {
    val expire = data.expire
    if (expire == null || expire - Calendar.getInstance().time.time / 1000 > 5 * 60) {
        refreshToken(completion)
        return
    }
    completion(data.jwt)
}

fun Pluto.refreshToken(completion: (String?) -> Unit) {
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
    postRequest("api/auth/refresh", bodyJson, commonHeaders, object : Callback {
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