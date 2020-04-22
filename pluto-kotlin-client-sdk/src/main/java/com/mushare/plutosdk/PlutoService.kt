package com.mushare.plutosdk

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.*

interface PlutoService {
    @POST("api/auth/refresh")
    fun refreshAuth(
        @Body body: RefreshAuthPostData
    ): Call<PlutoResponseWithBody<RefreshAuthResponse>>

    @POST("api/user/register")
    fun registerWithEmail(
        @Body body: RegisterWithEmailPostData,
        @Header("Accept-Language") language: String
    ): Call<PlutoResponse>

    @POST("api/user/register/verify/mail")
    fun resendValidationEmail(
        @Body body: EmailPostData,
        @Header("Accept-Language") language: String
    ): Call<PlutoResponse>

    @POST("api/user/password/reset/mail")
    fun resetPassword(
        @Body body: EmailPostData,
        @Header("Accept-Language") language: String
    ): Call<PlutoResponse>

    @POST("api/user/login")
    fun loginWithEmail(
        @Body body: LoginWithEmailPostData
    ): Call<PlutoResponseWithBody<LoginResponse>>

    @POST("api/user/login/google/mobile")
    fun loginWithGoogle(
        @Body body: LoginWithGooglePostData
    ): Call<PlutoResponseWithBody<LoginResponse>>

    @GET("api/user/info/me")
    fun getAccountInfo(
        @HeaderMap authorizationHeader: Map<String, String>
    ): Call<PlutoResponseWithBody<PlutoUser>>
}

class RefreshAuthPostData(
    @field:SerializedName("refresh_token") var refreshToken: String,
    @field:SerializedName("user_id") var userId: Int,
    @field:SerializedName("device_id") var deviceId: String,
    @field:SerializedName("app_id") var appId: String
)

class RegisterWithEmailPostData(
    @field:SerializedName("mail") var mail: String,
    @field:SerializedName("password") var password: String,
    @field:SerializedName("name") var name: String
)

class EmailPostData(
    @field:SerializedName("mail") var mail: String
)

class LoginWithEmailPostData(
    @field:SerializedName("mail") var mail: String,
    @field:SerializedName("password") var password: String,
    @field:SerializedName("device_id") var deviceId: String,
    @field:SerializedName("app_id") var appId: String
)

class LoginWithGooglePostData(
    @field:SerializedName("id_token") var idToken: String,
    @field:SerializedName("device_id") var deviceId: String,
    @field:SerializedName("app_id") var appId: String
)