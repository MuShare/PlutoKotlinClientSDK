package com.mushare.plutosdk

import com.mushare.plutosdk.Pluto.Companion.appId
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception

fun Pluto.registerByEmail(
    address: String,
    password: String,
    name: String,
    success: () -> Unit,
    error: ((PlutoError) -> Unit)? = null
) {
    val bodyJson = JSONObject()
    bodyJson.put("mail", address)
    bodyJson.put("password", password)
    bodyJson.put("name", name)
    bodyJson.put("app_id", appId)
    requestPost("api/user/register", bodyJson, commonHeaders, object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            error?.let { it(PlutoError.badRequest) }
        }

        override fun onResponse(call: Call, response: Response) {
            val plutoResponse = PlutoResponse(response)
            if (plutoResponse.statusOK()) {
                success()
            } else {
                error?.let { it(plutoResponse.errorCode()) }
            }
        }
    })
}

fun Pluto.resendValidationEmail(
    address: String,
    success: () -> Unit,
    error: ((PlutoError) -> Unit)? = null
) {
    val bodyJson = JSONObject()
    bodyJson.put("mail", address)
    bodyJson.put("app_id", appId)
    requestPost("api/user/register/verify/mail", bodyJson, commonHeaders, object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            error?.let { it(PlutoError.badRequest) }
        }

        override fun onResponse(call: Call, response: Response) {
            val plutoResponse = PlutoResponse(response)
            if (plutoResponse.statusOK()) {
                success()
            } else {
                error?.let { it(plutoResponse.errorCode()) }
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
    val bodyJson = JSONObject()
    bodyJson.put("mail", address)
    bodyJson.put("password", password)
    bodyJson.put("device_id", data.deviceID)
    bodyJson.put("app_id", appId)
    requestPost("api/user/login", bodyJson, commonHeaders, object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            error?.let { it(PlutoError.badRequest) }
        }

        override fun onResponse(call: Call, response: Response) {
            val plutoResponse = PlutoResponse(response)
            handleLogin(plutoResponse, success, error)
        }
    })
}

fun Pluto.resetPassword(
    address: String,
    success: () -> Unit,
    error: ((PlutoError) -> Unit)? = null
) {
    val bodyJson = JSONObject()
    bodyJson.put("mail", address)
    bodyJson.put("app_id", appId)
    requestPost("api/user/password/reset/mail", bodyJson, commonHeaders, object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            error?.let { it(PlutoError.badRequest) }
        }

        override fun onResponse(call: Call, response: Response) {
            val plutoResponse = PlutoResponse(response)
            if (plutoResponse.statusOK()) {
                success()
            } else {
                error?.let { it(plutoResponse.errorCode()) }
            }
        }
    })
}

fun Pluto.logout() {
    data.clear()
    state = Pluto.State.notSignin
}

private fun Pluto.handleLogin(
    response: PlutoResponse,
    success: (() -> Unit)?,
    error: ((PlutoError) -> Unit)?
) {
    if (response.statusOK()) {
        val body = response.getBody()
        try {
            val refreshToken = body.getString("refresh_token")
            val jwt = body.getString("jwt")
            data.updateRefreshToken(refreshToken)
            if (!data.updateJwt(jwt)) {
                error?.let { it(PlutoError.parseError) }
                return
            }
        } catch (e: Exception) {
            error?.let { it(PlutoError.parseError) }
            return
        }
        state = Pluto.State.signin
        success?.let { it() }
    } else {
        error?.let { it(response.errorCode()) }
    }
}