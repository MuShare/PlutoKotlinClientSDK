package com.mushare.plutosdk

import com.mushare.plutosdk.Pluto.Companion.appId
import retrofit2.Callback

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
    val deviceId = data.deviceID
    if (userId == null || refreshToken == null || deviceId == null) {
        completion(null)
        return
    }
    plutoService.refreshAuth(
        RefreshAuthPostData(refreshToken, userId, deviceId, appId)
    ).enqueue(object : Callback<PlutoResponseWithBody<RefreshAuthResponse>> {
        override fun onFailure(
            call: retrofit2.Call<PlutoResponseWithBody<RefreshAuthResponse>>,
            t: Throwable
        ) {
            t.printStackTrace()
            completion(null)
        }

        override fun onResponse(
            call: retrofit2.Call<PlutoResponseWithBody<RefreshAuthResponse>>,
            response: retrofit2.Response<PlutoResponseWithBody<RefreshAuthResponse>>
        ) {
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
}

fun Pluto.getAuthorizationHeader(completion: (Map<String, String>?) -> Unit) {
    getToken {
        if (it == null) {
            completion(null)
        } else {
            completion(hashMapOf("Authorization" to "jwt $it"))
        }
    }
}