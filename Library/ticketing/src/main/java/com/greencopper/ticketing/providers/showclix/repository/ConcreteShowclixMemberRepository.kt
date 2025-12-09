package com.greencopper.ticketing.providers.showclix.repository

import android.util.Base64
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.networking.SignatureGenerator
import com.greencopper.core.remotestate.RemoteStateDispatcher
import com.greencopper.core.remotestate.RemoteStateEntry
import com.greencopper.core.remotestate.dispatch
import com.greencopper.ticketing.models.Ticket
import com.greencopper.ticketing.providers.showclix.*
import com.greencopper.ticketing.providers.showclix.data.VerifyTokenData
import com.greencopper.toolkit.App
import com.greencopper.toolkit.extensions.toZonedDateTime
import com.greencopper.toolkit.logging.e
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

internal class ConcreteShowclixMemberRepository(
    private val showclixAPI: ShowclixAPI,
    signatureGenerator: SignatureGenerator,
    showclixApiSharedKey: String,
    private val remoteStateDispatcher: RemoteStateDispatcher,
    private val scope: CoroutineScope,
    private val localStorage: LocalStorage,
) : ShowclixMemberRepository {

    private val authorizationHeader = signatureGenerator.getAuthenticationKey(apiKey = showclixApiSharedKey)

    override suspend fun sendMagicLink(url: String, email: String, magicLink: String): Boolean = try {
        showclixAPI.getMagicLinkUrl(
            "${url}member/magic-link",
            authorizationHeader,
            ShowclixRequest.MagicLinkBody(email, magicLink)
        )
        true
    } catch (throwable: Throwable) {
        App.log.e(
            "Error while sending Showclix magic link $magicLink",
            throwable = throwable
        )
        false
    }

    override suspend fun verifyToken(url: String, token: String): VerifyTokenData? = try {
        showclixAPI.verifyToken(
            "${url}member/time-token",
            authorizationHeader,
            ShowclixRequest.TokenBody(token)
        )
    } catch (throwable: Throwable) {
        App.log.e("Error while verifying token $token", throwable = throwable)
        null
    }

    override suspend fun fetchTickets(url: String): List<Ticket> {
        val validationToken = localStorage.project.showclix.validationToken.value.orEmpty()
        val userId = localStorage.project.showclix.userId.value.orEmpty()
        val dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

        return showclixAPI.getTickets(
            "${url}tickets",
            "Basic " + Base64.encodeToString("$userId:$validationToken".toByteArray(), Base64.NO_WRAP)
        ).tickets.filterNot { ticket ->
            ticket.void
        }.also {
            scope.launch {
                remoteStateDispatcher.dispatch(
                    key = DispatchedShowclixTickets.dispatcherKey,
                    value = it.toDispatchEntry(),
                    domain = RemoteStateEntry.Domain.PROJECT,
                    isUrgent = false,
                )
            }
        }.map { ticket ->
            Ticket(
                primaryTitle = ticket.name,
                primarySubtitle = null,
                qrCode = ticket.id,
                secondaryTitle = null,
                startDate = ticket.startDate?.let {
                    it.toZonedDateTime(dateTimeFormatter)
                }
            )
        }.distinct()
    }
}
