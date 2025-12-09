package com.greencopper.thuzi.account.registration.copyroutine

import com.greencopper.core.content.manager.ContentManager
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.thuzi.ThuziAPI
import com.greencopper.thuzi.localstorage.thuzi

internal class ConcreteCopyRoutine(
    private val localStorage: LocalStorage,
    private val contentManager: ContentManager,
    private val thuziAPI: ThuziAPI,
) : CopyRoutine {

    override suspend fun getNewJwt(
        url: String,
        brandId: String,
        eventId: String,
    ): String? {
        val previousJwt = contentManager.previousProjects
            .firstNotNullOfOrNull { localStorage[it].project.thuzi.jwt.value }
            ?: return null

        return thuziAPI.copyRoutine(
            url = "${url}/brand/$brandId/multievent/$eventId/attendee/import",
            authorizationHeader = "Bearer $previousJwt",
        ).authToken
    }
}
