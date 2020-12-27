package com.mushare.plutosdk

import android.util.Patterns
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
    error: (PlutoError) -> Unit,
    handler: Pluto.PlutoRequestHandler? = null
) {
    val postData = RegisterPostData(userId, mail, password, name, appId)
    plutoService.register(postData, getLanguage()).apply {
        enqueue(object : Callback<PlutoResponse> {
            override fun onFailure(call: Call<PlutoResponse>, t: Throwable) {
                t.printStackTrace()
                error(PlutoError.badRequest)
            }

            override fun onResponse(call: Call<PlutoResponse>, response: Response<PlutoResponse>) {
                handleResponse(response, success, error)
            }
        })
    }.also {
        handler?.setCall(it)
    }
}

fun Pluto.resendValidationEmail(
    account: String,
    success: () -> Unit,
    error: (PlutoError) -> Unit,
    handler: Pluto.PlutoRequestHandler? = null
) {
    val postData =
        if (Patterns.EMAIL_ADDRESS.matcher(account).matches())
            ResendValidationEmailPostData(null, account, appId)
        else
            ResendValidationEmailPostData(account, null, appId)
    plutoService.resendValidationEmail(postData, getLanguage()).apply {
        enqueue(object : Callback<PlutoResponse> {
            override fun onFailure(call: Call<PlutoResponse>, t: Throwable) {
                t.printStackTrace()
                error(PlutoError.badRequest)
            }

            override fun onResponse(call: Call<PlutoResponse>, response: Response<PlutoResponse>) {
                handleResponse(response, success, error)
            }
        })
    }.also {
        handler?.setCall(it)
    }
}

fun Pluto.loginWithAccount(
    account: String,
    password: String,
    success: () -> Unit,
    error: (PlutoError) -> Unit,
    handler: Pluto.PlutoRequestHandler? = null
) {
    state.value = Pluto.State.loading
    val deviceId = data.deviceID
    if (deviceId == null) {
        error(PlutoError.badRequest)
        return
    }
    val postData = LoginWithAccountPostData(account, password, deviceId, appId)
    plutoService.loginWithAccount(postData)
        .apply {
            enqueue(handleLoginCallback(success, error))
        }
        .also {
            handler?.setCall(it)
        }
}

fun Pluto.loginWithGoogle(
    idToken: String,
    success: () -> Unit,
    error: (PlutoError) -> Unit,
    handler: Pluto.PlutoRequestHandler? = null
) {
    state.value = Pluto.State.loading
    val deviceId = data.deviceID
    if (deviceId == null) {
        error(PlutoError.badRequest)
        return
    }
    val postData = LoginWithGooglePostData(idToken, deviceId, appId)
    plutoService.loginWithGoogle(postData)
        .apply {
            enqueue(handleLoginCallback(success, error))
        }
        .also {
            handler?.setCall(it)
        }
}

fun Pluto.loginWithWeChat(
    code: String,
    success: () -> Unit,
    error: (PlutoError) -> Unit,
    handler: Pluto.PlutoRequestHandler? = null
) {
    state.value = Pluto.State.loading
    val deviceId = data.deviceID
    if (deviceId == null) {
        error(PlutoError.badRequest)
        return
    }
    val postData = LoginWithWeChatPostData(code, deviceId, appId)
    plutoService.loginWithWeChat(postData).apply {
        enqueue(handleLoginCallback(success, error))
    }.also {
        handler?.setCall(it)
    }
}

fun Pluto.resetPassword(
    address: String,
    success: () -> Unit,
    error: (PlutoError) -> Unit,
    handler: Pluto.PlutoRequestHandler? = null
) {
    plutoService.resetPassword(EmailPostData(address, appId), getLanguage()).apply {
        enqueue(object : Callback<PlutoResponse> {
            override fun onFailure(call: Call<PlutoResponse>, t: Throwable) {
                t.printStackTrace()
                error(PlutoError.badRequest)
            }

            override fun onResponse(call: Call<PlutoResponse>, response: Response<PlutoResponse>) {
                handleResponse(response, success, error)
            }
        })
    }.also {
        handler?.setCall(it)
    }
}

fun Pluto.logout(completion: (() -> Unit)? = null) {
    data.clear()
    state.value = Pluto.State.notSignIn
    completion?.invoke()
}

private fun Pluto.handleLoginCallback(
    success: () -> Unit,
    error: (PlutoError) -> Unit
): Callback<PlutoResponseWithBody<LoginResponse>> {
    return object : Callback<PlutoResponseWithBody<LoginResponse>> {
        override fun onFailure(
            call: Call<PlutoResponseWithBody<LoginResponse>>,
            t: Throwable
        ) {
            t.printStackTrace()
            error(PlutoError.badRequest)
        }

        override fun onResponse(
            call: Call<PlutoResponseWithBody<LoginResponse>>,
            response: Response<PlutoResponseWithBody<LoginResponse>>
        ) {
            val plutoResponse = response.body()
            if (plutoResponse == null) {
                error(parseErrorCodeFromErrorBody(response.errorBody(), gson))
                return
            }
            plutoResponse.analysis(
                success = {
                    val body = plutoResponse.getBody()
                    data.refreshToken = body.refreshToken
                    if (!data.updateAccessToken(body.accessToken)) {
                        error(PlutoError.parseError)
                        return@analysis
                    }
                    state.value = Pluto.State.signIn
                    success?.invoke()
                },
                error = error
            )
        }
    }
}
