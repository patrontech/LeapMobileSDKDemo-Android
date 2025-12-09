package com.greencopper.thuzi.badges.data

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.setupTest
import com.greencopper.thuzi.ThuziResponse
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.thuzi.mocks.MockThuziAPI
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

internal class ConcreteBadgesApiServiceTest: CoroutineTest(UnconfinedTestDispatcher()) {

    init {
        Toolkit.setupTest()
    }

    private val localStorage: LocalStorage = App.resolve()
    private val badgesImageDirectory = "badges"

    private val badgeData: List<ThuziResponse.Badge> = listOf(
        ThuziResponse.Badge(
            badgeId = "1",
            name = "name",
            earnedDescription = "description",
            unearnedDescription = "description",
            isEarned = true,
            earnedOn = "2007-12-03T10:15:30.00Z",
            earnedImageUrl = "url",
            unearnedImageUrl = "url",
        ),
        ThuziResponse.Badge(
            badgeId = "2",
            name = "name",
            earnedDescription = "description",
            unearnedDescription = "description",
            isEarned = false,
            earnedOn = null,
            earnedImageUrl = "url",
            unearnedImageUrl = "url",
        )
    )
    private val badges: List<Badge> = badgeData.map { it.toBadge() }

    private val response = ThuziResponse.Badges(
        id = "id",
        name = "name",
        badges = badgeData
    )

    private val thuziAPI: MockThuziAPI = MockThuziAPI(
        badgesResponse = { response },
    )

    private val apiService = ConcreteBadgesApiService(
        thuziAPI,
        localStorage,
        badgesImageDirectory,
        testScope.coroutineContext,
    )

    @BeforeEach
    fun beforeEach() {
        val expiration = LocalDateTime.now().plusYears(1)
        localStorage.project.thuzi.jwtExpirationDate.value = ZonedDateTime.of(expiration, ZoneId.systemDefault()).toString()
        localStorage.project.thuzi.qrCode.value = "value"
    }

    override fun afterEach() {
        File(badgesImageDirectory).deleteRecursively()
    }

    @Test
    fun givenInvalidId_getBadges_throwsNoSuchElementException() {
        localStorage.project.thuzi.qrCode.value = null
        thuziAPI.badgesResponse = { throw NoSuchElementException() }

        runTest {
            assertThrows<NoSuchElementException> { apiService.getBadges("") }
        }
    }

    @Test
    fun givenValidId_getBadges_returnsBadges() {
        runTest {
            val result = apiService.getBadges("")
            assertThat(result).isEqualTo(badges)
        }
    }

    @Test
    fun givenNetworkError_getBadges_throws() {
        thuziAPI.badgesResponse = { throw HttpException(Response.error<String>(400, "".toResponseBody())) }

        assertThrows<HttpException> {
            runTest { apiService.getBadges("https://www.google.com/") }
        }
    }

    @Test
    fun downloadImages_returnsBadges() {
        val responseBody = File.createTempFile("pref", "suf")
        thuziAPI.badgeImageResponse = { Response.success(responseBody.readBytes().toResponseBody()) }

        runTest {
            val result = apiService.downloadImagesIfNeeded(badges, true).first()
            val numUrls = badges.map { it.imageUrl }.distinct().size

            assertThat(thuziAPI.getBadgeImageCount).isEqualTo(numUrls)
            assertThat(result).isEqualTo(badges)
        }

        responseBody.deleteOnExit()
    }

    @Test
    fun givenShouldCleanTrue_downloadImages_cleansImages() {
        val dir = File(badgesImageDirectory).apply { mkdir() }
        File(badgesImageDirectory, "uselessFile1").createNewFile()
        File(badgesImageDirectory, "uselessFile2").createNewFile()

        runTest {
            apiService.downloadImagesIfNeeded(emptyList(), true).collect {}
            assertThat(dir.list()).isEmpty()
        }
    }

    @Test
    fun givenShouldCleanFalse_downloadImages_keepsImages() {
        val dir = File(badgesImageDirectory).apply { mkdir() }
        File(badgesImageDirectory, "uselessFile1").createNewFile()
        File(badgesImageDirectory, "uselessFile2").createNewFile()

        runTest {
            apiService.downloadImagesIfNeeded(emptyList(), false).collect()
            assertThat(dir.list()).hasSize(2)
        }
    }

    @Test
    fun earnedResponseData_toBadge_createsEarnedTime() {
        val data = ThuziResponse.Badge(
            badgeId = "id",
            name = "name",
            earnedDescription = null,
            unearnedDescription = null,
            isEarned = true,
            earnedOn = null,
            earnedImageUrl = "url",
            unearnedImageUrl = "url"
        )

        assertThat((data.toBadge() as Badge.EarnedBadge).earnedDateInMillis).isGreaterThan(0)
    }
}
