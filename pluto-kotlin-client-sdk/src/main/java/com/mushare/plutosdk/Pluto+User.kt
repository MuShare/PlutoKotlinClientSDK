package com.mushare.plutosdk

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File

val Pluto.currentUser: PlutoUser?
    get() {
        if (state != Pluto.State.signIn) {
            return null
        }
        return data.user
    }

fun Pluto.myInfo(
    isForceRefresh: Boolean = false,
    success: (PlutoUser) -> Unit,
    error: ((PlutoError) -> Unit)? = null,
    handler: Pluto.PlutoRequestHandler? = null
) {
    val currentUser = data.user
    if (!isForceRefresh && currentUser != null) {
        success(currentUser)
        return
    }
    getAuthorizationHeader(
        completion = { header ->
            if (header == null) {
                handler?.setCall(null)
                error?.invoke(PlutoError.notSignIn)
                return@getAuthorizationHeader
            }

            plutoService.getUserInfo(header).apply {
                enqueue(object : Callback<PlutoResponseWithBody<PlutoUser>> {
                    override fun onFailure(
                        call: Call<PlutoResponseWithBody<PlutoUser>>,
                        t: Throwable
                    ) {
                        t.printStackTrace()
                        error(PlutoError.badRequest)
                    }

                    override fun onResponse(
                        call: Call<PlutoResponseWithBody<PlutoUser>>,
                        response: Response<PlutoResponseWithBody<PlutoUser>>
                    ) {
                        val plutoResponse = response.body()
                        if (plutoResponse == null) {
                            error?.invoke(parseErrorCodeFromErrorBody(response.errorBody(), gson))
                            return
                        }
                        plutoResponse.analysis(
                            success = {
                                data.user = plutoResponse.getBody()
                                success(plutoResponse.getBody())
                            },
                            error = {
                                error?.invoke(plutoResponse.errorCode)
                            }
                        )
                    }
                })
            }.also {
                handler?.setCall(it)
            }
        },
        handler = handler
    )
}

fun Pluto.updateUserId(
    userId: String,
    success: () -> Unit,
    error: (PlutoError) -> Unit,
    handler: Pluto.PlutoRequestHandler? = null
) {
    val postData = UpdateUserInfoPutData(null, null, userId)
    updateUserInfo(postData, success, error, handler)
}

fun Pluto.updateName(
    name: String,
    success: () -> Unit,
    error: (PlutoError) -> Unit,
    handler: Pluto.PlutoRequestHandler? = null
) {
    val postData = UpdateUserInfoPutData(name, null, null)
    updateUserInfo(postData, success, error, handler)
}

fun Pluto.uploadAvatar(
    imageFile: File,
    success: () -> Unit,
    error: (PlutoError) -> Unit,
    handler: Pluto.PlutoRequestHandler? = null
) {
    val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    val postData = UpdateUserInfoPutData(null, base64, null)
    updateUserInfo(postData, success, error, handler)
}

private fun Pluto.updateUserInfo(
    postData: UpdateUserInfoPutData,
    success: () -> Unit,
    error: (PlutoError) -> Unit,
    handler: Pluto.PlutoRequestHandler? = null
) {
    getAuthorizationHeader(
        completion = { header ->
            if (header == null) {
                handler?.setCall(null)
                error?.invoke(PlutoError.notSignIn)
                return@getAuthorizationHeader
            }

            plutoService.updateUserInfo(postData, header).apply {
                enqueue(object : Callback<PlutoResponse> {
                    override fun onFailure(call: Call<PlutoResponse>, t: Throwable) {
                        t.printStackTrace()
                        error?.invoke(PlutoError.badRequest)
                    }

                    override fun onResponse(
                        call: Call<PlutoResponse>,
                        response: Response<PlutoResponse>
                    ) {
                        handleResponse(response, success, error)
                    }
                })
            }.also {
                handler?.setCall(it)
            }
        },
        handler = handler
    )
}
