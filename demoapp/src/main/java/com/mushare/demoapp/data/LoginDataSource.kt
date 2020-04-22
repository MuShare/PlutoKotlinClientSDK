package com.mushare.demoapp.data

import android.util.Log
import com.mushare.demoapp.data.model.LoggedInUser
import com.mushare.plutosdk.*
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {
    private val pluto = Pluto.getInstance()

    fun login(username: String, password: String, onComplete: (Result<LoggedInUser>) -> Unit) {
        try {
            // TODO: handle loggedInUser authentication
            //pluto?.registerByEmail(username, password, "Test", {
            //pluto?.resendValidationEmail(username, {
            pluto?.loginWithEmail(username, password, {
                pluto.myInfo({
                    val fakeUser = LoggedInUser(java.util.UUID.randomUUID().toString(), it.name)
                    onComplete(Result.Success(fakeUser))
                }, {
                    onComplete(Result.Error(IOException("Error getting account info $it")))
                    Log.e("getInfo", "failed $it")
                })
            }, {
                onComplete(Result.Error(IOException("Error logging in $it")))
                Log.e("login", "failed $it")
            })
        } catch (e: Throwable) {
            e.printStackTrace()
            onComplete(Result.Error(IOException("Error logging in", e)))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}

