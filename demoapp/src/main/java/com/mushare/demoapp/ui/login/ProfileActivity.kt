package com.mushare.demoapp.ui.login;

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.mushare.demoapp.R
import com.mushare.plutosdk.Pluto
import com.mushare.plutosdk.getToken
import com.mushare.plutosdk.myInfo
import com.mushare.plutosdk.updateName
import java.io.ByteArrayOutputStream
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

        Pluto.getInstance()?.myInfo(success = { user ->
            nameEditText.get()?.setText(user.name)
            avatarImageView.get()?.let {
                Glide.with(this).load(user.avatar).into(it)
            }
        })

        Pluto.getInstance()?.getToken(completion = {
            findViewById<TextView>(R.id.profile_access_token).text = it ?: "Refresh failed"
        })

        findViewById<Button>(R.id.profile_upload_avatar).setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(64)
                .maxResultSize(480, 480)
                .start { resultCode, data ->
                    if (resultCode == Activity.RESULT_OK) {
                        val filePath = ImagePicker.getFilePath(data) ?: return@start
                        val bitmap = BitmapFactory.decodeFile(filePath)
                        val outputStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                        val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
                        Log.d(TAG, "base64: $base64")
                    } else if (resultCode == ImagePicker.RESULT_ERROR) {
                        Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
                    }
                }
        }

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
