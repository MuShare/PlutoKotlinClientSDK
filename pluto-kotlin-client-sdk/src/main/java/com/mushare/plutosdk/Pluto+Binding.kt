package com.mushare.plutosdk

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

val availableLoginTypes: Array<Pluto.LoginType>
    get() = arrayOf(Pluto.LoginType.mail, Pluto.LoginType.mail)

val Pluto.availableBindings: Array<PlutoUser.Binding>?
    get() = data.user?.bindings

fun Pluto.bind(
    type: Pluto.LoginType,
    authString: String,
    success: () -> Unit,
    error: ((PlutoError) -> Unit)? = null,
    handler: Pluto.PlutoRequestHandler? = null
) {
    getAuthorizationHeader(
        completion = { header ->
            if (header == null) {
                handler?.setCall(null)
                error?.invoke(PlutoError.notSignIn)
                return@getAuthorizationHeader
            }

            val postData = when (type) {
                Pluto.LoginType.mail -> {
                    BindPostData(
                        type = type.identifier,
                        mail = authString
                    )
                }
                Pluto.LoginType.google -> {
                    BindPostData(
                        type = type.identifier,
                        idToken = authString
                    )
                }
            }
            plutoService.bind(postData, header).apply {
                enqueue(object : Callback<PlutoResponse> {
                    override fun onFailure(call: Call<PlutoResponse>, t: Throwable) {
                        t.printStackTrace()
                        error?.invoke(PlutoError.badRequest)
                    }

                    override fun onResponse(
                        call: Call<PlutoResponse>,
                        response: Response<PlutoResponse>
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
        }
    )
}

fun Pluto.unbind(
    type: Pluto.LoginType,
    success: () -> Unit,
    error: ((PlutoError) -> Unit)? = null,
    handler: Pluto.PlutoRequestHandler? = null
) {
    val bindings = availableBindings
    if (bindings == null) {
        error?.invoke(PlutoError.notSignIn)
        return
    }
    if (bindings.size == 1) {
        error?.invoke(PlutoError.unbindNotAllow)
        return
    }
    getAuthorizationHeader(
        completion = { header ->
            if (header == null) {
                handler?.setCall(null)
                error?.invoke(PlutoError.notSignIn)
                return@getAuthorizationHeader
            }

            val postData = UnbindPostData(type.identifier)
            plutoService.unbind(postData, header).apply {
                enqueue(object : Callback<PlutoResponse> {
                    override fun onFailure(call: Call<PlutoResponse>, t: Throwable) {
                        t.printStackTrace()
                        error?.invoke(PlutoError.badRequest)
                    }

                    override fun onResponse(
                        call: Call<PlutoResponse>,
                        response: Response<PlutoResponse>
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
        }
    )
}