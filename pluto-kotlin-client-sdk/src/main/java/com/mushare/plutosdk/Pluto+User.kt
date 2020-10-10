package com.mushare.plutosdk

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun Pluto.myInfo(
    success: (PlutoUser) -> Unit,
    error: ((PlutoError) -> Unit)? = null,
    handler: Pluto.PlutoRequestHandler? = null
) {
    data.user?.let {
        success(it)
        return
    }
    getAuthorizationHeader({ header ->
        if (header != null) {
            plutoService.getAccountInfo(header).apply {
                enqueue(object : Callback<PlutoResponseWithBody<PlutoUser>> {
                    override fun onFailure(call: Call<PlutoResponseWithBody<PlutoUser>>, t: Throwable) {
                        t.printStackTrace()
                        error?.invoke(PlutoError.badRequest)
                    }

                    override fun onResponse(
                        call: Call<PlutoResponseWithBody<PlutoUser>>,
                        response: Response<PlutoResponseWithBody<PlutoUser>>
                    ) {
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
            }.also {
                handler?.setCall(it)
            }
        } else {
            handler?.setCall(null)
            error?.invoke(PlutoError.notSignin)
        }
    }, handler)
}

fun Pluto.updateName(
    name: String,
    success: () -> Unit,
    error: ((PlutoError) -> Unit)? = null,
    handler: Pluto.PlutoRequestHandler? = null
) {

}
