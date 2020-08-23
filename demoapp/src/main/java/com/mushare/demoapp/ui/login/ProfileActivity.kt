package com.mushare.demoapp.ui.login;

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mushare.demoapp.R
import com.mushare.plutosdk.Pluto
import com.mushare.plutosdk.getToken
import com.mushare.plutosdk.myInfo

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_profile)

        Pluto.getInstance()?.myInfo(success = {
            findViewById<TextView>(R.id.profile_name).text = it.name
        })

        findViewById<Button>(R.id.profile_refresh_token).setOnClickListener {
            Pluto.getInstance()?.getToken(isForceRefresh = true, completion = {
                Toast.makeText(this, it ?: "Refresh failed", 3).show()
            })
        }
    }
}
