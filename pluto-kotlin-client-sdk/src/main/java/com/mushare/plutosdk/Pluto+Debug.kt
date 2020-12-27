package com.mushare.plutosdk

import android.util.Log
import java.util.*

fun Pluto.simulateInvalidRefreshToken() {
    if (BuildConfig.DEBUG) {
        data.refreshToken = UUID.randomUUID().toString()
    } else {
        Log.d("Pluto", "Pluto.simulateInvalidRefreshToken() should be invoked in DEBUG mode.")
    }
}

fun Pluto.simulateInvalidAccessToken() {
    if (BuildConfig.DEBUG) {
        data.accessToken = UUID.randomUUID().toString()
    } else {
        Log.d("Pluto", "Pluto.simulateInvalidAccessToken() should be invoked in DEBUG mode.")
    }
}

fun Pluto.resetExpire(date: Date) {
    if (BuildConfig.DEBUG) {
        data.expire = date.time.toInt() / 1000
    } else {
        Log.d("Pluto", "Pluto.resetExpire(date: Date) should be invoked in DEBUG mode.")
    }
}