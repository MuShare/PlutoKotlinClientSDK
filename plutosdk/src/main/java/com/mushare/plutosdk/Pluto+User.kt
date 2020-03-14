package com.mushare.plutosdk

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.lang.Exception

public fun Pluto.myInfo(success: (PlutoUser) -> Unit, error: ((PlutoError) -> Unit)? = null) {

    getHeaders {
        getRequest("api/user/info/me", it, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val plutoResponse = PlutoResponse(response)
                if (plutoResponse.statusOK()) {
                    val body = plutoResponse.getBody()
                    try {
                        val user = PlutoUser(
                            body.getInt("id"),
                            body.getString("mail"),
                            body.getString("avatar"),
                            body.getString("name")
                        )
                        data.user = user
                        success(user)
                    } catch (e: Exception) {
                        error?.let { it(PlutoError.parseError) }
                    }
                } else {
                    error?.let { it(plutoResponse.errorCode()) }
                }
            }
        })
    }
}