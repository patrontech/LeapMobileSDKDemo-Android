package com.greencopper.thuzi.badges.data

import android.graphics.drawable.Drawable
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.setupTest
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.thuzi.mocks.MockBadgesApiService
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.net.HttpURLConnection
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

internal class ConcreteBadgesRepositoryTest: CoroutineTest() {

    init {
        Toolkit.setupTest()
    }

    private val localStorage: LocalStorage = App.resolve()
    private val cacheService = ConcreteBadgesCacheService(localStorage, "badges")
    private val apiService = MockBadgesApiService()

    private val badgesImagesDirectory = "imageDirectory"
    private val badgesRepository =
        ConcreteBadgesRepository(apiService, cacheService, testScope.coroutineContext, badgesImagesDirectory)

    private val badge = Badge.EarnedBadge("1", "name", "description", "url", 1)

    @BeforeEach
    fun beforeEach() {
        val expiration = LocalDateTime.now().plusYears(1)
        localStorage.project.thuzi.jwtExpirationDate.value = ZonedDateTime.of(expiration, ZoneId.systemDefault()).toString()
        localStorage.project.thuzi.qrCode.value = "someValue"
        localStorage.project.thuzi.badges.value = emptyList()
    }

    override fun afterEach() {
        File(badgesImagesDirectory).deleteRecursively()
    }

    @Test
    fun givenApiServicesThrows_whenGettingBadges_shouldThrow() {
        //given
        apiService.getBadges = { throw NoSuchElementException() }

        //then
        assertThrows<NoSuchElementException> {
            runTest { badgesRepository.getBadges("https://fakeUrl.com").first() }
        }
    }

    @Test
    fun givenNotEmptyCache_downloadImagesThrows_whenGettingBadges_shouldNotThrow() {
        //given
        cacheSomeBadge()
        apiService.downloadImagesIfNeeded = { throw Exception() }

        runTest {
            //when
            val result = badgesRepository.getBadges("https://fakeUrl.com").first()

            //then
            assertThat(result).isNotEmpty
        }
    }

    @Test
    fun whenGettingBadges_withWrongResponse_withoutCache_shouldThrow() {
        apiService.getBadges =
            { throw HttpException(Response.error<String>(HttpURLConnection.HTTP_BAD_REQUEST, "".toResponseBody())) }

        //then
        assertThrows<HttpException> {
            runTest { badgesRepository.getBadges("https://fakeUrl.com").toList() }
        }
    }

    @Test
    fun whenGettingBadges_withWrongResponse_withCache_shouldNotThrow() {
        //given
        cacheSomeBadge()
        apiService.getBadges =
            { throw HttpException(Response.error<String>(HttpURLConnection.HTTP_BAD_REQUEST, "".toResponseBody())) }

        runTest {
            //when
            val result = badgesRepository.getBadges("https://fakeUrl.com").toList()

            //then
            assertThat(result[0]).isNotEmpty
        }
    }

    @Test
    fun whenGettingBadges_withGoodResponse_shouldGetLatestBadges() {
        //given
        cacheSomeBadge()
        val fakeId = "fakeId123"
        val apiResult = listOf(
            Badge.EarnedBadge(
                badgeId = fakeId,
                name = "name",
                description = "desc",
                imageUrl = "https://fakeUrl.com",
                Instant.now().toEpochMilli()
            )
        )
        apiService.getBadges = { apiResult }
        apiService.downloadImagesIfNeeded = { flowOf(apiResult) }

        runTest {
            //when
            val result = badgesRepository.getBadges("https://fakeUrl.com").toList().last()

            //then
            assertThat(result).isNotEmpty
            assertThat(result[0].badgeId).isEqualTo(fakeId)
        }
    }

    @Test
    fun givenNoErrors_getDrawable_returnsDrawable() {
        mockkStatic(Drawable::class)
        every { Drawable.createFromPath(any()) } returns mockk()

        assertThat(badgesRepository.getImageDrawable(badge)).isNotNull
    }

    @Test
    fun givenException_getDrawable_returnsNull() {
        mockkStatic(Drawable::class)
        every { Drawable.createFromPath(any()) } throws Exception()

        assertThat(badgesRepository.getImageDrawable(badge)).isNull()
    }

    private fun cacheSomeBadge() {
        cacheService.saveBadges(
            listOf(
                Badge.EarnedBadge(
                    badgeId = "id",
                    name = "name",
                    description = "desc",
                    imageUrl = "https://fakeUrl.com",
                    Instant.now().toEpochMilli()
                )
            )
        )
    }
}
