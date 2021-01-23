package com.mushare.plutosdk

import android.util.Log
import java.util.*

fun Pluto.simulateInvalidRefreshToken() {
    if (Pluto.isDebug) {
        data.refreshToken = UUID.randomUUID().toString()
    } else {
        Log.d("Pluto", "Pluto.simulateInvalidRefreshToken() should be invoked in DEBUG mode.")
    }
}

fun Pluto.simulateInvalidAccessToken() {
    if (Pluto.isDebug) {
        data.accessToken = UUID.randomUUID().toString()
    } else {
        Log.d("Pluto", "Pluto.simulateInvalidAccessToken() should be invoked in DEBUG mode.")
    }
}

fun Pluto.resetExpire(date: Date) {
    if (Pluto.isDebug) {
        data.expire = date.time.toInt() / 1000
    } else {
        Log.d("Pluto", "Pluto.resetExpire(date: Date) should be invoked in DEBUG mode.")
    }
}