package com.mushare.plutosdk

import android.content.SharedPreferences
import kotlin.properties.Delegates

class StringWrapper(private val saveKey: String?, private val sharedPref: SharedPreferences) {
    var value: String? by Delegates.observable(sharedPref.getString(saveKey, null)) { _, _, new ->
        with(sharedPref.edit()) {
            putString(saveKey, new)
            apply()
        }
    }
}

class IntWrapper(private val saveKey: String?, private val sharedPref: SharedPreferences) {
    var value: Int? by Delegates.observable(
        if (sharedPref.contains(saveKey)) sharedPref.getInt(
            saveKey,
            0
        ) else null
    ) { _, _, new ->
        with(sharedPref.edit()) {
            if (new == null)
                remove(saveKey)
            else {
                putInt(saveKey, new)
            }
            apply()
        }
    }
}