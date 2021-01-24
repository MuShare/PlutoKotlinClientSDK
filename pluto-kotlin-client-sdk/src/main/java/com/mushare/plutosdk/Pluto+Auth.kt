package com.mushare.plutosdk

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun Pluto.getAccessToken(
    isForceRefresh: Boolean = false,
    completion: (String?) -> Unit,
    handler: Pluto.PlutoRequestHandler? = null
) {
    if (state.value != Pluto.State.signIn) {
        completion(null)
        return
    }
    val accessToken = data.accessToken
    val expire = data.expire ?: 0
    if (isForceRefresh || accessToken == null || expire - System.currentTimeMillis() / 1000 < 30) {
        refreshAccessToken(
            completion = {
                completion(it)
            },
            handler = handler
        )
        return
    }
    completion(data.accessToken)
}

fun Pluto.refreshAccessToken(
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
                if (plutoResponse == null) {
                    completion(null)
                    return
                }
                plutoResponse.analysis(
                    success = {
                        val accessToken = plutoResponse.getBody().accessToken
                        val refreshToken = plutoResponse.getBody().refreshToken
                        if (data.updateAccessToken(accessToken)) {
                            data.refreshToken = refreshToken
                            completion(accessToken)
                        } else {
                            completion(null)
                        }
                    },
                    error = {
                        completion(null)
                    }
                )
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