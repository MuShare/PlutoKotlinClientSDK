package com.mushare.plutosdk

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun Pluto.getAccessToken(
    isForceRefresh: Boolean = false,
    completion: (String?) -> Unit,
    handler: Pluto.PlutoRequestHandler? = null
) {
    val accessToken = data.accessToken
    val expire = data.expire
    if (isForceRefresh || accessToken == null || expire == null || expire - System.currentTimeMillis() / 1000 < 5 * 60) {
        refreshToken({
            if (it == null) {
                data.clear()
            }
            completion(it)
        }, handler)
    }
    completion(data.accessToken)
}

private fun Pluto.refreshToken(
    completion: (String?) -> Unit,
    handler: Pluto.PlutoRequestHandler? = null
) {
    val refreshToken = data.refreshToken
    if (refreshToken == null) {
        completion(null)
        return
    }
    plutoService.refreshAuth(RefreshAuthPostData(refreshToken, Pluto.appId)).apply {
        enqueue(object : Callback<PlutoResponseWithBody<RefreshAuthResponse>> {
            override fun onFailure(
                call: Call<PlutoResponseWithBody<RefreshAuthResponse>>,
                t: Throwable
            ) {
                t.printStackTrace()
                completion(null)
            }

            override fun onResponse(
                call: Call<PlutoResponseWithBody<RefreshAuthResponse>>,
                response: Response<PlutoResponseWithBody<RefreshAuthResponse>>
            ) {
                val plutoResponse = response.body()
                if (plutoResponse != null && plutoResponse.statusOK()) {
                    val accessToken = plutoResponse.getBody().accessToken
                    val refreshToken = plutoResponse.getBody().refreshToken
                    if (data.updateAccessToken(accessToken)) {
                        data.refreshToken = refreshToken
                        completion(accessToken)
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

fun Pluto.getAuthorizationHeader(
    completion: (Map<String, String>?) -> Unit,
    handler: Pluto.PlutoRequestHandler? = null
) {
    getAccessToken(
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