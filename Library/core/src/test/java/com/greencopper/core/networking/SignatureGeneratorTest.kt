package com.greencopper.core.networking

import android.util.Base64
import com.greencopper.core.content.manager.CurrentProjectTagProvider
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class SignatureGeneratorTest {
    private lateinit var signatureGenerator: SignatureGenerator
    private lateinit var localStorage: LocalStorage
    private lateinit var currentProjectTagProvider: CurrentProjectTagProvider

    @BeforeEach
    internal fun setUp() {
        Toolkit.setupTest()
        localStorage = App.resolve()
        currentProjectTagProvider = object : CurrentProjectTagProvider {
            override val currentProject: String = "project"
            override val currentProjectFlow: StateFlow<String?> = MutableStateFlow(currentProject)
        }

        signatureGenerator = SignatureGeneratorClient(localStorage, currentProjectTagProvider)
    }

    @Test
    @DisplayName("Given a valid project tag, When calling getAuthenticationKey, Then a valid key is return")
    fun getAuthenticationKeyShouldSucceed() {
        mockkStatic(Base64::class)
        every { Base64.encodeToString(any(), Base64.NO_WRAP) } returns "hello"
        val result = signatureGenerator.getAuthenticationKey(
            currentProjectTagProvider.currentProject,
            "apiKey"
        )
        val installationId = localStorage.app.installationId.value
        assertThat(result).isEqualTo("$installationId:hello")
    }

    @Test
    @DisplayName("Given no project tag, When calling getAuthenticationKey, Then a valid key is return")
    fun getAuthenticationKeyWithoutProjectTagShouldSucceed() {
        mockkStatic(Base64::class)
        every { Base64.encodeToString(any(), Base64.NO_WRAP) } returns "hello"
        val result = signatureGenerator.getAuthenticationKey(apiKey = "apiKey")
        val installationId = localStorage.app.installationId.value
        assertThat(result).isEqualTo("$installationId:hello")
    }
}
