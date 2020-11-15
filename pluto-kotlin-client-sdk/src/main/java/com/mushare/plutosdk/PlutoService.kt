package com.mushare.plutosdk

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.*

interface PlutoService {
    @POST("v1/token/refresh")
    fun refreshAuth(
        @Body body: RefreshAuthPostData
    ): Call<PlutoResponseWithBody<RefreshAuthResponse>>

    @POST("v1/user/register")
    fun register(
        @Body body: RegisterPostData,
        @Header("Accept-Language") language: String
    ): Call<PlutoResponse>

    @POST("v1/user/register/verify/mail")
    fun resendValidationEmail(
        @Body body: ResendValidationEmailPostData,
        @Header("Accept-Language") language: String
    ): Call<PlutoResponse>

    @POST("v1/user/password/reset/mail")
    fun resetPassword(
        @Body body: EmailPostData,
        @Header("Accept-Language") language: String
    ): Call<PlutoResponse>

    @POST("v1/user/login/account")
    fun loginWithAccount(
        @Body body: LoginWithAccountPostData
    ): Call<PlutoResponseWithBody<LoginResponse>>

    @POST("v1/user/login/google/mobile")
    fun loginWithGoogle(
        @Body body: LoginWithGooglePostData
    ): Call<PlutoResponseWithBody<LoginResponse>>

    @GET("v1/user/info")
    fun getUserInfo(
        @HeaderMap authorizationHeader: Map<String, String>
    ): Call<PlutoResponseWithBody<PlutoUser>>

    @PUT("v1/user/info")
    fun updateUserInfo(
        @Body body: UpdateUserInfoPutData,
        @HeaderMap authorizationHeader: Map<String, String>
    ): Call<PlutoResponse>
}

class RefreshAuthPostData(
    @field:SerializedName("refresh_token") var refreshToken: String,
    @field:SerializedName("app_id") var appId: String
)

class RegisterPostData(
    @field:SerializedName("user_id") var userId: String,
    @field:SerializedName("mail") var mail: String,
    @field:SerializedName("password") var password: String,
    @field:SerializedName("name") var name: String,
    @field:SerializedName("app_id") var appId: String
)

class ResendValidationEmailPostData(
    @field:SerializedName("user_id") var userId: String?,
    @field:SerializedName("mail") var mail: String?,
    @field:SerializedName("app_id") var appId: String
)

class EmailPostData(
    @field:SerializedName("mail") var mail: String,
    @field:SerializedName("app_id") var appId: String
)

class LoginWithAccountPostData(
    @field:SerializedName("account") var account: String,
    @field:SerializedName("password") var password: String,
    @field:SerializedName("device_id") var deviceId: String,
    @field:SerializedName("app_id") var appId: String
)

class LoginWithGooglePostData(
    @field:SerializedName("id_token") var idToken: String,
    @field:SerializedName("device_id") var deviceId: String,
    @field:SerializedName("app_id") var appId: String
)

class UpdateUserInfoPutData(
    @field:SerializedName("name") var name: String?,
    @field:SerializedName("avatar") var avatar: String?
)