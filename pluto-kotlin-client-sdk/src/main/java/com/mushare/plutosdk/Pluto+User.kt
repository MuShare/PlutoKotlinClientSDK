package com.mushare.plutosdk

import retrofit2.Callback

fun Pluto.myInfo(success: (PlutoUser) -> Unit, error: ((PlutoError) -> Unit)? = null) {
    data.user?.let {
        success(it)
        return
    }
    getAuthorizationHeader { header ->
        if (header != null) {
            plutoService.getAccountInfo(header)
                .enqueue(object : Callback<PlutoResponseWithBody<PlutoUser>> {
                    override fun onFailure(call: retrofit2.Call<PlutoResponseWithBody<PlutoUser>>, t: Throwable) {
                        t.printStackTrace()
                        error?.invoke(PlutoError.badRequest)
                    }

                    override fun onResponse(call: retrofit2.Call<PlutoResponseWithBody<PlutoUser>>, response: retrofit2.Response<PlutoResponseWithBody<PlutoUser>>) {
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
        } else {
            error?.invoke(PlutoError.notSignin)
        }
    }
}