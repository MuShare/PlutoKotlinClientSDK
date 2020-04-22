package com.mushare.plutosdk

import com.mushare.plutosdk.Pluto.Companion.appId
import retrofit2.Callback

fun Pluto.registerByEmail(
    address: String,
    password: String,
    name: String,
    success: () -> Unit,
    error: ((PlutoError) -> Unit)? = null
) {
    plutoService.registerWithEmail(
        RegisterWithEmailPostData(address, password, name), getLanguage()
    ).enqueue(object :
        Callback<PlutoResponse> {
        override fun onFailure(call: retrofit2.Call<PlutoResponse>, t: Throwable) {
            t.printStackTrace()
            error?.invoke(PlutoError.badRequest)
        }

        override fun onResponse(
            call: retrofit2.Call<PlutoResponse>,
            response: retrofit2.Response<PlutoResponse>
        ) {
            val plutoResponse = response.body()
            if (plutoResponse != null) {
                if (plutoResponse.statusOK()) {
                    success()
                } else {
                    error?.invoke(plutoResponse.errorCode())
                }
            } else {
                error?.invoke(parseErrorCodeFromErrorBody(response.errorBody(), gson))
            }
        }
    })
}

fun Pluto.resendValidationEmail(
    address: String,
    success: () -> Unit,
    error: ((PlutoError) -> Unit)? = null
) {
    plutoService.resendValidationEmail(EmailPostData(address), getLanguage())
        .enqueue(object : Callback<PlutoResponse> {
            override fun onFailure(call: retrofit2.Call<PlutoResponse>, t: Throwable) {
                t.printStackTrace()
                error?.invoke(PlutoError.badRequest)
            }

            override fun onResponse(
                call: retrofit2.Call<PlutoResponse>,
                response: retrofit2.Response<PlutoResponse>
            ) {
                val plutoResponse = response.body()
                if (plutoResponse != null) {
                    if (plutoResponse.statusOK()) {
                        success()
                    } else {
                        error?.invoke(plutoResponse.errorCode())
                    }
                } else {
                    error?.invoke(parseErrorCodeFromErrorBody(response.errorBody(), gson))
                }
            }
        })
}

fun Pluto.loginWithEmail(
    address: String,
    password: String,
    success: (() -> Unit)? = null,
    error: ((PlutoError) -> Unit)? = null
) {
    val deviceId = data.deviceID
    if (deviceId == null) {
        error?.invoke(PlutoError.badRequest)
        return
    }
    plutoService.loginWithEmail(
        LoginWithEmailPostData(address, password, deviceId, appId)
    ).enqueue(object : Callback<PlutoResponseWithBody<LoginResponse>> {
        override fun onFailure(
            call: retrofit2.Call<PlutoResponseWithBody<LoginResponse>>,
            t: Throwable
        ) {
            t.printStackTrace()
            error?.invoke(PlutoError.badRequest)
        }

        override fun onResponse(
            call: retrofit2.Call<PlutoResponseWithBody<LoginResponse>>,
            response: retrofit2.Response<PlutoResponseWithBody<LoginResponse>>
        ) {
            val plutoResponse = response.body()
            if (plutoResponse != null) {
                handleLogin(plutoResponse, success, error)
            } else {
                error?.invoke(parseErrorCodeFromErrorBody(response.errorBody(), gson))
            }
        }
    })
}

fun Pluto.loginWithGoogle(
    idToken: String,
    success: (() -> Unit)? = null,
    error: ((PlutoError) -> Unit)? = null
) {
    val deviceId = data.deviceID
    if (deviceId == null) {
        error?.invoke(PlutoError.badRequest)
        return
    }
    plutoService.loginWithGoogle(
        LoginWithGooglePostData(idToken, deviceId, appId)
    ).enqueue(object : Callback<PlutoResponseWithBody<LoginResponse>> {
        override fun onFailure(
            call: retrofit2.Call<PlutoResponseWithBody<LoginResponse>>,
            t: Throwable
        ) {
            t.printStackTrace()
            error?.invoke(PlutoError.badRequest)
        }

        override fun onResponse(
            call: retrofit2.Call<PlutoResponseWithBody<LoginResponse>>,
            response: retrofit2.Response<PlutoResponseWithBody<LoginResponse>>
        ) {
            val plutoResponse = response.body()
            if (plutoResponse != null) {
                handleLogin(plutoResponse, success, error)
            } else {
                error?.invoke(parseErrorCodeFromErrorBody(response.errorBody(), gson))
            }
        }
    })
}

fun Pluto.resetPassword(
    address: String,
    success: () -> Unit,
    error: ((PlutoError) -> Unit)? = null
) {
    plutoService.resetPassword(EmailPostData(address), getLanguage())
        .enqueue(object : Callback<PlutoResponse> {
            override fun onFailure(call: retrofit2.Call<PlutoResponse>, t: Throwable) {
                t.printStackTrace()
                error?.invoke(PlutoError.badRequest)
            }

            override fun onResponse(
                call: retrofit2.Call<PlutoResponse>,
                response: retrofit2.Response<PlutoResponse>
            ) {
                val plutoResponse = response.body()
                if (plutoResponse != null) {
                    if (plutoResponse.statusOK()) {
                        success()
                    } else {
                        error?.invoke(plutoResponse.errorCode())
                    }
                } else {
                    error?.invoke(parseErrorCodeFromErrorBody(response.errorBody(), gson))
                }
            }
        })
}

fun Pluto.logout() {
    data.clear()
    state.postValue(Pluto.State.notSignin)
}

private fun Pluto.handleLogin(
    response: PlutoResponseWithBody<LoginResponse>,
    success: (() -> Unit)?,
    error: ((PlutoError) -> Unit)?
) {
    if (response.statusOK()) {
        val body = response.getBody()
        val refreshToken = body.refreshToken
        val jwt = body.jwt
        data.updateRefreshToken(refreshToken)
        if (!data.updateJwt(jwt)) {
            error?.invoke(PlutoError.parseError)
            return
        }
        state.postValue(Pluto.State.signin)
        success?.invoke()
    } else {
        error?.invoke(response.errorCode())
    }
}