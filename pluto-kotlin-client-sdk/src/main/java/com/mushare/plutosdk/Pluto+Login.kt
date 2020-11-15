package com.mushare.plutosdk

import com.mushare.plutosdk.Pluto.Companion.appId
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun Pluto.register(
    userId: String,
    mail: String,
    password: String,
    name: String,
    success: () -> Unit,
    error: ((PlutoError) -> Unit)? = null,
    handler: Pluto.PlutoRequestHandler? = null
) {
    val postData = RegisterPostData(userId, mail, password, name, appId)
    plutoService.register(postData, getLanguage()).apply {
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

fun Pluto.resendValidationEmail(
    account: String,
    success: () -> Unit,
    error: ((PlutoError) -> Unit)? = null,
    handler: Pluto.PlutoRequestHandler? = null
) {
    val postData =
        if (account.contains("@"))
            ResendValidationEmailPostData(null, account, appId)
        else
            ResendValidationEmailPostData(account, null, appId)
    plutoService.resendValidationEmail(postData, getLanguage()).apply {
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

fun Pluto.loginWithAccount(
    address: String,
    password: String,
    success: (() -> Unit)? = null,
    error: ((PlutoError) -> Unit)? = null,
    handler: Pluto.PlutoRequestHandler? = null
) {
    val deviceId = data.deviceID
    if (deviceId == null) {
        error?.invoke(PlutoError.badRequest)
        return
    }
    plutoService.loginWithAccount(LoginWithAccountPostData(address, password, deviceId, appId))
        .apply {
            enqueue(object : Callback<PlutoResponseWithBody<LoginResponse>> {
                override fun onFailure(
                    call: Call<PlutoResponseWithBody<LoginResponse>>,
                    t: Throwable
                ) {
                    t.printStackTrace()
                    error?.invoke(PlutoError.badRequest)
                }

                override fun onResponse(
                    call: Call<PlutoResponseWithBody<LoginResponse>>,
                    response: Response<PlutoResponseWithBody<LoginResponse>>
                ) {
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

fun Pluto.loginWithGoogle(
    idToken: String,
    success: (() -> Unit)? = null,
    error: ((PlutoError) -> Unit)? = null,
    handler: Pluto.PlutoRequestHandler? = null
) {
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

            override fun onResponse(
                call: Call<PlutoResponseWithBody<LoginResponse>>,
                response: Response<PlutoResponseWithBody<LoginResponse>>
            ) {
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

fun Pluto.resetPassword(
    address: String,
    success: () -> Unit,
    error: ((PlutoError) -> Unit)? = null,
    handler: Pluto.PlutoRequestHandler? = null
) {
    plutoService.resetPassword(EmailPostData(address, appId), getLanguage()).apply {
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

private fun Pluto.handleLogin(
    response: PlutoResponseWithBody<LoginResponse>,
    success: (() -> Unit)?, error: ((PlutoError) -> Unit)?
) {
    if (response.statusOK()) {
        val body = response.getBody()
        data.updateRefreshToken(body.refreshToken)
        if (!data.updateJwt(body.accessToken)) {
            error?.invoke(PlutoError.parseError)
            return
        }
        state.postValue(Pluto.State.signin)
        success?.invoke()
    } else {
        error?.invoke(response.errorCode())
    }
}