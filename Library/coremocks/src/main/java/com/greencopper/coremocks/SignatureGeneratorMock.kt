package com.greencopper.coremocks

import com.greencopper.core.networking.SignatureGenerator

public class SignatureGeneratorMock(
    public var authKey: String = "authenticationKey",
) : SignatureGenerator {
    override fun getAuthenticationKey(projectTag: String?, apiKey: String): String {
        return authKey
    }
}
