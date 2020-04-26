package com.mushare.plutosdk

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
fun Pluto.myInfo(success: (PlutoUser) -> Unit, error: ((PlutoError) -> Unit)? = null): Pluto.PlutoRequestHandler {
    val handler = Pluto.PlutoRequestHandler()
    data.user?.let {
        success(it)
        return handler
    }
    handler.setCall(getAuthorizationHeader { header ->
        if (header != null) {
            handler.setCall(plutoService.getAccountInfo(header).apply {
                enqueue(object : Callback<PlutoResponseWithBody<PlutoUser>> {
                    override fun onFailure(call: Call<PlutoResponseWithBody<PlutoUser>>, t: Throwable) {
                        t.printStackTrace()
                        error?.invoke(PlutoError.badRequest)
                    }

                    override fun onResponse(call: Call<PlutoResponseWithBody<PlutoUser>>, response: Response<PlutoResponseWithBody<PlutoUser>>) {
                        val plutoResponse = response.body()
                        if (plutoResponse != null) {
                            if (plutoResponse.statusOK()) {
                                data.user = plutoResponse.getBody().also(success)
                            } else {
                                error?.invoke(plutoResponse.errorCode())
                            }
                        } else {
                            error?.invoke(parseErrorCodeFromErrorBody(response.errorBody(), gson))
                        }
                    }
                })
            })
        } else {
            handler.setCall(null)
            error?.invoke(PlutoError.notSignin)
        }
    })
    return handler
}