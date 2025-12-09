package com.greencopper.thuzi.account.registration.model

public data class PrepareResult(
    public val url: String,
    public val cookies: List<String>
) {
    public constructor(url: String, vararg cookies: String) : this(url, cookies.toList())
}
