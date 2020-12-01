package com.mushare.plutosdk

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