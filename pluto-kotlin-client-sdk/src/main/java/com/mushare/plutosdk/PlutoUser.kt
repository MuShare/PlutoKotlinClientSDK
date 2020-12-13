package com.mushare.plutosdk

import com.google.gson.annotations.SerializedName

data class PlutoUser(
    @field:SerializedName("sub") var id: Int,
    @field:SerializedName("user_id") var userId: String,
    @field:SerializedName("user_id_updated") var userIdUpdated: Boolean,
    @field:SerializedName("avatar") var avatar: String,
    @field:SerializedName("name") var name: String,
    @field:SerializedName("bindings") var bindings: Array<Binding>
) {
    data class Binding(
        @field:SerializedName("login_type") val loginType: Pluto.LoginType,
        @field:SerializedName("mail") var mail: String
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlutoUser

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (userIdUpdated != other.userIdUpdated) return false
        if (avatar != other.avatar) return false
        if (name != other.name) return false
        if (!bindings.contentEquals(other.bindings)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + userId.hashCode()
        result = 31 * result + userIdUpdated.hashCode()
        result = 31 * result + avatar.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + bindings.contentHashCode()
        return result
    }

    val mail: Binding?
        get() = bindings.firstOrNull { it.loginType == Pluto.LoginType.MAIL }

    val google: Binding?
        get() = bindings.firstOrNull { it.loginType == Pluto.LoginType.GOOGLE }
}

