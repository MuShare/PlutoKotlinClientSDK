package com.mushare.demoapp.ui.login;

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.mushare.demoapp.R
import com.mushare.plutosdk.*
import java.lang.ref.WeakReference

class ProfileActivity : AppCompatActivity() {

    companion object {
        private val TAG = ProfileActivity::class.java.simpleName
    }

    private lateinit var nameEditText: WeakReference<EditText>
    private lateinit var avatarImageView: WeakReference<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_profile)

        nameEditText = WeakReference(findViewById(R.id.profile_name))
        avatarImageView = WeakReference(findViewById(R.id.profile_avatar))

        updateUserInfo()

        Pluto.getInstance()?.getToken(completion = {
            findViewById<TextView>(R.id.profile_access_token).text = it ?: "Refresh failed"
        })

        findViewById<Button>(R.id.profile_upload_avatar).setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(64)
                .maxResultSize(480, 480)
                .start { resultCode, data ->
                    when (resultCode) {
                        Activity.RESULT_OK -> {
                            Pluto.getInstance()?.uploadAvatar(
                                imageFile = ImagePicker.getFile(data) ?: return@start,
                                success = {
                                    updateUserInfo {
                                        Toast
                                            .makeText(
                                                this,
                                                "Avatar uploaded",
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    }
                                },
                                error = {
                                    Log.d(TAG, "Error uploading avatar $it")
                                }
                            )
                        }
                        ImagePicker.RESULT_ERROR -> {
                            Toast
                                .makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT)
                                .show()
                        }
                        else -> {
                            Toast
                                .makeText(this, "Task Cancelled", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
        }

        findViewById<Button>(R.id.profile_update_name).setOnClickListener {
            val name = nameEditText.get()?.text.toString() ?: return@setOnClickListener
            Pluto.getInstance()?.updateName(
                name = name,
                success = {
                    updateUserInfo {
                        Toast.makeText(this, "Name updated", Toast.LENGTH_SHORT).show()
                    }
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

    private fun updateUserInfo(completion: (() -> Unit)? = null) {
        Pluto.getInstance()?.myInfo(success = { user ->
            nameEditText.get()?.setText(user.name)
            avatarImageView.get()?.let {
                Glide.with(this).load(user.avatar).into(it)
            }
            completion?.let { it() }
        })

    }
}
