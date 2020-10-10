package com.mushare.demoapp.ui.login;

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mushare.demoapp.R
import com.mushare.plutosdk.Pluto
import com.mushare.plutosdk.getToken
import com.mushare.plutosdk.myInfo
import com.mushare.plutosdk.updateName
import java.lang.ref.WeakReference

class ProfileActivity : AppCompatActivity() {

    companion object {
        private val TAG = ProfileActivity::class.java.simpleName
    }

    private lateinit var nameEditText: WeakReference<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_profile)

        nameEditText = WeakReference(findViewById<EditText>(R.id.profile_name))

        Pluto.getInstance()?.myInfo(success = {
            nameEditText.get()?.setText(it.name)
        })

        Pluto.getInstance()?.getToken(completion = {
            findViewById<TextView>(R.id.profile_access_token).text = it ?: "Refresh failed"
        })

        findViewById<Button>(R.id.profile_update_name).setOnClickListener {
            val name = nameEditText.get()?.text.toString() ?: return@setOnClickListener
            Pluto.getInstance()?.updateName(
                name = name,
                success = {
                    Pluto.getInstance()?.myInfo(success = {
                        Toast.makeText(this, "Name updated", Toast.LENGTH_SHORT).show()
                    })
                },
                error = {
                    Log.d(TAG, "Error updating username $it")
                }
            )
        }

        findViewById<Button>(R.id.profile_refresh_token).setOnClickListener {
            Pluto.getInstance()?.getToken(isForceRefresh = true, completion = {
                findViewById<TextView>(R.id.profile_access_token).text = it ?: "Refresh failed"
            })
        }
    }
}
