package com.greencopper.core.permissions

import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

public sealed class AuthorizationStatus : KiboSerializable<AuthorizationStatus> {
    public object NotDetermined : AuthorizationStatus()
    public object Denied : AuthorizationStatus()
    public object AuthorizedAlways : AuthorizationStatus()
    public object AuthorizedWhenInUse : AuthorizationStatus()

    override fun getSerializer(): KSerializer<AuthorizationStatus> = serializer()

    public fun isAuthorized(): Boolean = this is AuthorizedAlways || this is AuthorizedWhenInUse
}
