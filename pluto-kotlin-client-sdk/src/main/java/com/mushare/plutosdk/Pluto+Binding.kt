package com.mushare.plutosdk

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

val Pluto.availableLoginTypes: Array<Pluto.LoginType>
    get() = arrayOf(Pluto.LoginType.MAIL, Pluto.LoginType.GOOGLE, Pluto.LoginType.WECHAT)

val Pluto.availableBindings: Array<PlutoUser.Binding>?
    get() = data.user?.bindings

fun Pluto.bind(
    type: Pluto.LoginType,
    authString: String,
    success: () -> Unit,
    error: (PlutoError) -> Unit,
    handler: Pluto.PlutoRequestHandler? = null
) {
    getAuthorizationHeader(
        completion = { header ->
            if (header == null) {
                handler?.setCall(null)
                error(PlutoError.notSignIn)
                return@getAuthorizationHeader
            }

            val postData = when (type) {
                Pluto.LoginType.MAIL -> {
                    BindPostData(
                        type = type.identifier,
                        mail = authString
                    )
                }
                Pluto.LoginType.GOOGLE -> {
                    BindPostData(
                        type = type.identifier,
                        idToken = authString
                    )
                }
                Pluto.LoginType.WECHAT -> {
                    BindPostData(
                        type = type.identifier,
                        code = authString
                    )
                }
            }
            plutoService.bind(postData, header).apply {
                enqueue(object : Callback<PlutoResponse> {
                    override fun onFailure(call: Call<PlutoResponse>, t: Throwable) {
                        t.printStackTrace()
                        error(PlutoError.badRequest)
                    }

                    override fun onResponse(
                        call: Call<PlutoResponse>,
                        response: Response<PlutoResponse>
                    ) {
                        handleResponse(response, success, error)
                    }
                })
            }
        }
    )
}

fun Pluto.unbind(
    type: Pluto.LoginType,
    success: () -> Unit,
    error: (PlutoError) -> Unit,
    handler: Pluto.PlutoRequestHandler? = null
) {
    val bindings = availableBindings
    if (bindings == null) {
        error(PlutoError.notSignIn)
        return
    }
    if (bindings.size == 1) {
        error(PlutoError.unbindNotAllow)
        return
    }
    getAuthorizationHeader(
        completion = { header ->
            if (header == null) {
                handler?.setCall(null)
                error(PlutoError.notSignIn)
                return@getAuthorizationHeader
            }

            val postData = UnbindPostData(type.identifier)
            plutoService.unbind(postData, header).apply {
                enqueue(object : Callback<PlutoResponse> {
                    override fun onFailure(call: Call<PlutoResponse>, t: Throwable) {
                        t.printStackTrace()
                        error(PlutoError.badRequest)
                    }

                    override fun onResponse(
                        call: Call<PlutoResponse>,
                        response: Response<PlutoResponse>
                    ) {
                        handleResponse(response, success, error)
                    }
                })
            }
        }
    )
}