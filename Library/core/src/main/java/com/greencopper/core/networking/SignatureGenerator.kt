package com.greencopper.core.networking

import android.security.keystore.KeyProperties
import android.util.Base64
import com.greencopper.core.content.manager.CurrentProjectTagProvider
import com.greencopper.core.localstorage.LocalStorage
import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

public interface SignatureGenerator {
    public fun getAuthenticationKey(projectTag: String? = null, apiKey: String): String
}

public class SignatureGeneratorClient(
    private val localStorage: LocalStorage,
    private val currentProjectTagProvider: CurrentProjectTagProvider,
) : SignatureGenerator {

    override fun getAuthenticationKey(projectTag: String?, apiKey: String): String {
        val tag = projectTag ?: currentProjectTagProvider.currentProject
        val installationId = localStorage.app.installationId.value
        val securityString = "$tag$installationId"
        val secretKey =
            SecretKeySpec(
                apiKey.toByteArray(StandardCharsets.UTF_8),
                KeyProperties.KEY_ALGORITHM_HMAC_SHA256
            )
        val sha256Hmac = Mac.getInstance(KeyProperties.KEY_ALGORITHM_HMAC_SHA256).apply {
            init(secretKey)
        }
        val macData = sha256Hmac.doFinal(StandardCharsets.US_ASCII.encode(securityString).array())

        return "$installationId:${Base64.encodeToString(macData, Base64.NO_WRAP)}".trim()
    }
}
