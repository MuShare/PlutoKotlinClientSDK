package com.mushare.plutosdk

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun Pluto.getToken(isForceRefresh: Boolean = false, completion: (String?) -> Unit, handler: Pluto.PlutoRequestHandler? = null) {
    val jwt = data.jwt
    val expire = data.expire
    if (isForceRefresh || jwt == null || expire == null || expire - System.currentTimeMillis() / 1000 < 5 * 60) {
        refreshToken({
            if (it == null) {
                data.clear()
            }
            completion(it)
        }, handler)
    }
    completion(data.jwt)
}

private fun Pluto.refreshToken(completion: (String?) -> Unit, handler: Pluto.PlutoRequestHandler? = null) {
    val userId = data.userId
    val refreshToken = data.refreshToken
    if (userId == null || refreshToken == null) {
        completion(null)
        return
    }
    plutoService.refreshAuth(RefreshAuthPostData(refreshToken, userId)).apply {
        enqueue(object : Callback<PlutoResponseWithBody<RefreshAuthResponse>> {
            override fun onFailure(
                call: Call<PlutoResponseWithBody<RefreshAuthResponse>>,
                t: Throwable
            ) {
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
    }.also {
        handler?.setCall(it)
    }
}

fun Pluto.getAuthorizationHeader(completion: (Map<String, String>?) -> Unit, handler: Pluto.PlutoRequestHandler? = null) {
    getToken(
        completion = { token ->
            if (token == null) {
                completion(null)
            } else {
                completion(hashMapOf("Authorization" to "jwt $token"))
            }
        },
        handler = handler
    )
}