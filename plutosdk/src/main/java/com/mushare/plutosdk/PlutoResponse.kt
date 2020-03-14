package com.mushare.plutosdk

import okhttp3.Response
import org.json.JSONObject
import java.lang.Exception

class PlutoResponse(response: Response) {
    private val body = try {
        response.body?.string()?.let { JSONObject(it) }
    } catch (e: Exception) {
        null
    }

    fun statusOK(): Boolean {
        return try {
            body?.getString("status") == "ok"
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getBody(): JSONObject {
        return try {
            body?.getJSONObject("body") ?: JSONObject()
        } catch (e: Exception) {
            e.printStackTrace()
            JSONObject()
        }
    }

    fun errorCode(): PlutoError {
        return try {
            body?.getJSONObject("error")?.getInt("code")?.let { PlutoError.valueOf(it) }
                ?: PlutoError.badRequest
        } catch (e: Exception) {
            e.printStackTrace()
            PlutoError.badRequest
        }
    }
}

public enum class PlutoError(val value: Int) {
    unknown(-99999),
    badRequest(-99998),
    parseError(-99997),
    notSignin(1001),
    mailIsAlreadyRegister(2001),
    mailIsNotExsit(2002),
    mailIsNotVerified(2003),
    mailAlreadyVerified(2004),
    invalidPassword(3001),
    invalidRefreshToken(3002),
    invalidJWTToken(3003);

    companion object {
        fun valueOf(value: Int) = values().find { it.value == value }
    }
}