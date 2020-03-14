package com.mushare.plutosdk

import android.util.Base64
import java.nio.charset.Charset

class JwtUtils {
    companion object {
        fun decodeBody(jwt: String): String? {
            val splits = jwt.split('.')
            // the jwt must has 3 parts
            if (splits.size == 3) {
                val bodyStr = splits[1]
                return try {
                    val decodedBytes: ByteArray = Base64.decode(bodyStr, Base64.URL_SAFE)
                    String(decodedBytes, Charset.forName("UTF-8"))
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            return null
        }
    }
}