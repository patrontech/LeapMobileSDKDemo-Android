package com.greencopper.ticketing.providers.showclix.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greencopper.core.localstorage.Email
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.user
import com.greencopper.ticketing.providers.showclix.repository.ShowclixMemberRepository
import com.greencopper.ticketing.providers.showclix.showclix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class ShowclixLoginViewModel(
    private val showclixMemberRepository: ShowclixMemberRepository,
    private val localStorage: LocalStorage
) : ViewModel() {

    fun isEmailFormatValid(email: String): Boolean =
        email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()

    suspend fun sendMagicLink(email: String, url: String, magicLink: String): Boolean =
        withContext(viewModelScope.coroutineContext + Dispatchers.Default) {
            val isMagicLinkSend = showclixMemberRepository.sendMagicLink(
                    url,
                    email,
                    magicLink
            )

            if(isMagicLinkSend) {
                localStorage.project.user.putEmail(Email.SHOWCLIX, email)
            }

            isMagicLinkSend
        }

    suspend fun verifyToken(url: String, token: String): Boolean =
        withContext(viewModelScope.coroutineContext + Dispatchers.Default) {
            val validToken = showclixMemberRepository.verifyToken(url, token)

            validToken?.let {
                localStorage.project.showclix.validationToken.value =
                    it.data.attributes.validationToken
                localStorage.project.showclix.userId.value = it.data.id
            }

            validToken != null
        }
}
