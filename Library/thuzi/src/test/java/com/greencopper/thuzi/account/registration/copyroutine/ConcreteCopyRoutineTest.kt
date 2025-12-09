package com.greencopper.thuzi.account.registration.copyroutine

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.testmocks.core.MockContentManager
import com.greencopper.testmocks.setupTest
import com.greencopper.thuzi.ThuziResponse
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.thuzi.mocks.MockThuziAPI
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.ZonedDateTime

internal class ConcreteCopyRoutineTest {

    private val contentManager = MockContentManager()
    private val thuziAPI = MockThuziAPI()
    private val localStorage: LocalStorage
    private val classUnderTest: ConcreteCopyRoutine

    init {
        Toolkit.setupTest()
        localStorage = App.resolve()
        classUnderTest = ConcreteCopyRoutine(
            localStorage,
            contentManager,
            thuziAPI,
        )
    }

    @Test
    fun testWhenSuccessful() {
        val response = ThuziResponse.CopyResponse("test")
        contentManager.previousProjectsValue = { setOf("project") }
        localStorage["project"].project.thuzi.jwt.value = "test"
        localStorage["project"].project.thuzi.jwtExpirationDate.value = ZonedDateTime.now().plusDays(1).toString()
        thuziAPI.copyRoutineResponse = { response }
        runTest {
            val jwt = classUnderTest.getNewJwt("http://test.com/", "brand", "event")
            assertThat(jwt).isEqualTo(response.authToken)
        }
    }

    @Test
    fun testWhenResponseFailed() {
        thuziAPI.copyRoutineResponse = { throw RuntimeException() }
        localStorage["project"].project.thuzi.jwt.value = "test"
        localStorage["project"].project.thuzi.jwtExpirationDate.value = ZonedDateTime.now().plusDays(1).toString()
        contentManager.previousProjectsValue = { setOf("project") }
        runTest {
            assertThrows<RuntimeException> {
                classUnderTest.getNewJwt("http://test.com/", "brand", "event")
            }
        }
    }

    @Test
    fun testWhenThereIsNoPreviousProjects() {
        thuziAPI.copyRoutineResponse = { ThuziResponse.CopyResponse("test") }
        localStorage["project"].project.thuzi.jwt.value = "test"
        localStorage["project"].project.thuzi.jwtExpirationDate.value = ZonedDateTime.now().plusDays(1).toString()
        contentManager.previousProjectsValue = { setOf() }
        runTest {
            val response = classUnderTest.getNewJwt("http://test.com/", "brand", "event")
            assertThat(response).isNull()
        }
    }

    @Test
    fun testWhenThereIsNoJwtForPreviousProject() {
        contentManager.previousProjectsValue = { setOf("test") }
        thuziAPI.copyRoutineResponse = { ThuziResponse.CopyResponse("test") }
        runTest {
            val response = classUnderTest.getNewJwt("http://test.com/", "brand", "event")
            assertThat(response).isNull()
        }
    }
}
