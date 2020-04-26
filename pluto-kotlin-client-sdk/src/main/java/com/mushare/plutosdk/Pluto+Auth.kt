package com.mushare.plutosdk

import com.mushare.plutosdk.Pluto.Companion.appId
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun Pluto.getToken(completion: (String?) -> Unit): Pluto.PlutoRequestHandler {
    val handler = Pluto.PlutoRequestHandler()
    val jwt = data.jwt
    val expire = data.expire
    if (jwt == null || expire == null || expire - System.currentTimeMillis() / 1000 < 5 * 60) {
        handler.setCall(refreshToken {
            if (it == null) {
                data.clear()
            }
            completion(it)
        })
        return handler
    }
    completion(data.jwt)
    return handler
}

private fun Pluto.refreshToken(completion: (String?) -> Unit): Pluto.PlutoRequestHandler {
    val handler = Pluto.PlutoRequestHandler()
    val userId = data.userId
    val refreshToken = data.refreshToken
    val deviceId = data.deviceID
    if (userId == null || refreshToken == null || deviceId == null) {
        completion(null)
        return handler
    }
    handler.setCall( plutoService.refreshAuth(RefreshAuthPostData(refreshToken, userId, deviceId, appId)).apply {
        enqueue(object : Callback<PlutoResponseWithBody<RefreshAuthResponse>> {
            override fun onFailure(call: Call<PlutoResponseWithBody<RefreshAuthResponse>>, t: Throwable) {
                t.printStackTrace()
                completion(null)
            }

            override fun onResponse(call: Call<PlutoResponseWithBody<RefreshAuthResponse>>, response: Response<PlutoResponseWithBody<RefreshAuthResponse>>) {
                val plutoResponse = response.body()
                if (plutoResponse != null && plutoResponse.statusOK()) {
                    val jwt = plutoResponse.getBody().jwt
                    if (data.updateJwt(jwt)) {
                        completion(jwt)
                    } else {
                        completion(null)
                    }
                } else {
                    completion(null)
                }
            }
        })
    })
    return handler
}

fun Pluto.getAuthorizationHeader(completion: (Map<String, String>?) -> Unit): Pluto.PlutoRequestHandler {
    return Pluto.PlutoRequestHandler().apply {
        setCall(getToken {
            if (it == null) {
                completion(null)
            } else {
                completion(hashMapOf("Authorization" to "jwt $it"))
            }
        })
    }
}