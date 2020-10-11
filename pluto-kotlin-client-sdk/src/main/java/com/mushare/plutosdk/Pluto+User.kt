package com.mushare.plutosdk

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File

fun Pluto.myInfo(
    success: (PlutoUser) -> Unit,
    error: ((PlutoError) -> Unit)? = null,
    handler: Pluto.PlutoRequestHandler? = null
) {
    data.user?.let {
        success(it)
        return
    }
    getAuthorizationHeader(
        completion = { header ->
            if (header == null) {
                handler?.setCall(null)
                error?.invoke(PlutoError.notSignin)
                return@getAuthorizationHeader
            }

            plutoService.getUserInfo(header).apply {
                enqueue(object : Callback<PlutoResponseWithBody<PlutoUser>> {
                    override fun onFailure(
                        call: Call<PlutoResponseWithBody<PlutoUser>>,
                        t: Throwable
                    ) {
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
                            error?.invoke(
                                parseErrorCodeFromErrorBody(
                                    response.errorBody(),
                                    gson
                                )
                            )
                        }
                    }
                })
            }.also {
                handler?.setCall(it)
            }
        },
        handler = handler
    )
}

fun Pluto.updateName(
    name: String,
    success: () -> Unit,
    error: ((PlutoError) -> Unit)? = null,
    handler: Pluto.PlutoRequestHandler? = null
) {
    getAuthorizationHeader(
        completion = { header ->
            if (header == null) {
                handler?.setCall(null)
                error?.invoke(PlutoError.notSignin)
                return@getAuthorizationHeader
            }

            val body = UpdateUserInfoPutData(name, null)
            plutoService.updateUserInfo(body, header).apply {
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
                            error?.invoke(
                                parseErrorCodeFromErrorBody(
                                    response.errorBody(),
                                    gson
                                )
                            )
                        }
                    }
                })
            }.also {
                handler?.setCall(it)
            }
        },
        handler = handler
    )
}

fun Pluto.uploadAvatar(
    imageFile: File,
    success: () -> Unit,
    error: ((PlutoError) -> Unit)? = null,
    handler: Pluto.PlutoRequestHandler? = null
) {
    val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    getAuthorizationHeader(
        completion = { header ->
            if (header == null) {
                handler?.setCall(null)
                error?.invoke(PlutoError.notSignin)
                return@getAuthorizationHeader
            }

            val body = UpdateUserInfoPutData(null, base64)
            plutoService.updateUserInfo(body, header).apply {
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
                            error?.invoke(
                                parseErrorCodeFromErrorBody(
                                    response.errorBody(),
                                    gson
                                )
                            )
                        }
                    }
                })
            }.also {
                handler?.setCall(it)
            }
        },
        handler = handler
    )
}


