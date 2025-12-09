package com.greencopper.thuzi.account.registration.plugins

public interface CustomLogoutAction {
    public fun onLogout()
}

internal class NoOpLogoutAction : CustomLogoutAction {
    override fun onLogout() {}
}
