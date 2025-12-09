package com.greencopper.ticketing.providers.showclix.login

import com.greencopper.core.localstorage.Email
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.user
import com.greencopper.testmocks.setupTest
import com.greencopper.ticketing.providers.showclix.data.VerifyTokenData
import com.greencopper.ticketing.providers.showclix.showclix
import com.greencopper.ticketingmocks.providers.showclix.repository.MockShowclixMemberRepository
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ShowclixLoginViewModelTest {

    init {
        Toolkit.setupTest()
    }

    private val memberRepository = MockShowclixMemberRepository()
    private val localStorage: LocalStorage = App.resolve()

    private val viewModel = ShowclixLoginViewModel(
        showclixMemberRepository = memberRepository,
        localStorage = localStorage,
    )

    @Test
    fun emptyEmail_isEmailFormatValid_returnsFalse() {
        assertThat(viewModel.isEmailFormatValid("")).isFalse
    }

    @Test
    fun validEmail_isEmailFormatValid_returnsTrue() {
        assertThat(viewModel.isEmailFormatValid("testname@testemail.com")).isTrue
    }

    @Test
    fun invalidEmail_isEmailFormatValid_returnsFalse() {
        assertThat(viewModel.isEmailFormatValid("testname@testemail")).isFalse
        assertThat(viewModel.isEmailFormatValid("@testemail.com")).isFalse
    }

    @Test
    fun magicLinkNotSent_sendMagicLink_returnsFalseStoresNothing() {
        memberRepository.sendMagicLinkFun = { _, _, _ -> false }

        runTest {
            val result = viewModel.sendMagicLink("", "", "")

            assertThat(result).isFalse
            assertThat(localStorage.project.user.email.value[Email.SHOWCLIX.key]).isNull()
        }
    }

    @Test
    fun magicLinkSent_sendMagicLink_returnsTrueStoresEmail() {
        memberRepository.sendMagicLinkFun = { _, _, _ -> true}

        runTest {
            val email = "testname@testemail.com"
            val result = viewModel.sendMagicLink(email, "", "")

            assertThat(result).isTrue
            assertThat(localStorage.project.user.email.value[Email.SHOWCLIX.key]).isEqualTo(email)
        }
    }

    @Test
    fun invalidToken_verifyToken_returnsFalseStoresNothing() {
        memberRepository.verifyTokenFun = { _, _ -> null }

        runTest {
            val result = viewModel.verifyToken("", "")

            assertThat(result).isFalse
            assertThat(localStorage.project.showclix.validationToken.value).isNull()
            assertThat(localStorage.project.showclix.userId.value).isNull()
        }
    }

    @Test
    fun validToken_verifyToken_returnsTrueStoresTokenAndUserId() {
        val id = "testId"
        val validationToken = "validationToken"
        memberRepository.verifyTokenFun = { _, _ ->
            VerifyTokenData(VerifyTokenData.Data(
                id = id,
                attributes = VerifyTokenData.Data.Attributes("", validationToken)
            ))
        }

        runTest {
            val result = viewModel.verifyToken("", "")

            assertThat(result).isTrue
            assertThat(localStorage.project.showclix.validationToken.value).isEqualTo(validationToken)
            assertThat(localStorage.project.showclix.userId.value).isEqualTo(id)
        }
    }
}
