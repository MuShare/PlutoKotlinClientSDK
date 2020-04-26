package com.mushare.plutosdk

import com.mushare.plutosdk.Pluto.Companion.appId
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun Pluto.registerByEmail(address: String, password: String, name: String, success: () -> Unit, error: ((PlutoError) -> Unit)? = null, handler: Pluto.PlutoRequestHandler? = null) {
    plutoService.registerWithEmail(RegisterWithEmailPostData(address, password, name), getLanguage()).apply {
        enqueue(object : Callback<PlutoResponse> {
            override fun onFailure(call: Call<PlutoResponse>, t: Throwable) {
                t.printStackTrace()
                error?.invoke(PlutoError.badRequest)
            }

            override fun onResponse(call: Call<PlutoResponse>, response: Response<PlutoResponse>) {
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
    }.also {
        handler?.setCall(it)
    }
}

fun Pluto.resendValidationEmail(address: String, success: () -> Unit, error: ((PlutoError) -> Unit)? = null, handler: Pluto.PlutoRequestHandler? = null) {
    plutoService.resendValidationEmail(EmailPostData(address), getLanguage()).apply {
        enqueue(object : Callback<PlutoResponse> {
            override fun onFailure(call: Call<PlutoResponse>, t: Throwable) {
                t.printStackTrace()
                error?.invoke(PlutoError.badRequest)
            }

            override fun onResponse(call: Call<PlutoResponse>, response: Response<PlutoResponse>) {
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
    }.also {
        handler?.setCall(it)
    }
}

fun Pluto.loginWithEmail(address: String, password: String, success: (() -> Unit)? = null, error: ((PlutoError) -> Unit)? = null, handler: Pluto.PlutoRequestHandler? = null) {
    val deviceId = data.deviceID
    if (deviceId == null) {
        error?.invoke(PlutoError.badRequest)
        return
    }
    plutoService.loginWithEmail(LoginWithEmailPostData(address, password, deviceId, appId)).apply {
        enqueue(object : Callback<PlutoResponseWithBody<LoginResponse>> {
            override fun onFailure(call: Call<PlutoResponseWithBody<LoginResponse>>, t: Throwable) {
                t.printStackTrace()
                error?.invoke(PlutoError.badRequest)
            }

            override fun onResponse(call: Call<PlutoResponseWithBody<LoginResponse>>, response: Response<PlutoResponseWithBody<LoginResponse>>) {
                val plutoResponse = response.body()
                if (plutoResponse != null) {
                    handleLogin(plutoResponse, success, error)
                } else {
                    error?.invoke(parseErrorCodeFromErrorBody(response.errorBody(), gson))
                }
            }
        })
    }.also {
        handler?.setCall(it)
    }
}

fun Pluto.loginWithGoogle(idToken: String, success: (() -> Unit)? = null, error: ((PlutoError) -> Unit)? = null, handler: Pluto.PlutoRequestHandler? = null) {
    val deviceId = data.deviceID
    if (deviceId == null) {
        error?.invoke(PlutoError.badRequest)
        return
    }
    plutoService.loginWithGoogle(LoginWithGooglePostData(idToken, deviceId, appId)).apply {
        enqueue(object : Callback<PlutoResponseWithBody<LoginResponse>> {
            override fun onFailure(call: Call<PlutoResponseWithBody<LoginResponse>>, t: Throwable) {
                t.printStackTrace()
                error?.invoke(PlutoError.badRequest)
            }

            override fun onResponse(call: Call<PlutoResponseWithBody<LoginResponse>>, response: Response<PlutoResponseWithBody<LoginResponse>>) {
                val plutoResponse = response.body()
                if (plutoResponse != null) {
                    handleLogin(plutoResponse, success, error)
                } else {
                    error?.invoke(parseErrorCodeFromErrorBody(response.errorBody(), gson))
                }
            }
        })
    }.also {
        handler?.setCall(it)
    }
}

fun Pluto.resetPassword(address: String, success: () -> Unit, error: ((PlutoError) -> Unit)? = null, handler: Pluto.PlutoRequestHandler? = null) {
    plutoService.resetPassword(EmailPostData(address), getLanguage()).apply {
        enqueue(object : Callback<PlutoResponse> {
            override fun onFailure(call: Call<PlutoResponse>, t: Throwable) {
                t.printStackTrace()
                error?.invoke(PlutoError.badRequest)
            }

            override fun onResponse(call: Call<PlutoResponse>, response: Response<PlutoResponse>) {
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
    }.also {
        handler?.setCall(it)
    }
}

fun Pluto.logout() {
    data.clear()
    state.postValue(Pluto.State.notSignin)
}

private fun Pluto.handleLogin(response: PlutoResponseWithBody<LoginResponse>, success: (() -> Unit)?, error: ((PlutoError) -> Unit)?) {
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