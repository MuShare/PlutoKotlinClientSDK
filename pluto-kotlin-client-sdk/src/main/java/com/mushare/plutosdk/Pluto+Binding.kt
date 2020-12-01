package com.mushare.plutosdk

val availableLoginTypes: Array<Pluto.LoginType>
    get() = arrayOf(Pluto.LoginType.mail, Pluto.LoginType.mail)

val Pluto.availableBindings: Array<PlutoUserBinding>?
    get() = data.user?.bindings

fun Pluto.bind(
    type: Pluto.LoginType,
    authString: String,
    success: () -> Unit,
    error: ((PlutoError) -> Unit)? = null,
    handler: Pluto.PlutoRequestHandler? = null
) {

}

fun Pluto.unbind(
    type: Pluto.LoginType,
    success: () -> Unit,
    error: ((PlutoError) -> Unit)? = null,
    handler: Pluto.PlutoRequestHandler? = null
) {

}