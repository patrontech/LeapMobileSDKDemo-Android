package com.greencopper.core.draftcontent

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.core
import com.greencopper.core.recipe.CoreConfiguration
import com.greencopper.core.recipe.CoreConfigurationHolder
import com.greencopper.coremocks.MockCoreAPI
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.MockRemoteStateDispatcher
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.LazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody.Companion.toResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import retrofit2.HttpException
import retrofit2.Response

internal class ConcreteDraftContentManagerTest : CoroutineTest() {

    init {
        Toolkit.setupTest()
    }

    private val coreConfig = CoreConfiguration(
        remoteState = CoreConfiguration.RemoteState("", 1),
        ota = CoreConfiguration.OTA(""),
        contentConfig = CoreConfiguration.ContentConfig(1L, emptyList()),
    )
    private val configHolder = CoreConfigurationHolder().apply {
        currentConfiguration.value = coreConfig
    }
    private val coreAPI = MockCoreAPI()
    private val localStorage: LocalStorage = App.resolve()
    private val json: Json = App.resolve()
    private val mockRemoteStateDispather = MockRemoteStateDispatcher(json)

    private val draftContentManager = ConcreteDraftContentManager(
        lazyLocalStorage = LazyResolver.adhoc(localStorage),
        coreConfigHolder = configHolder,
        coreAPI = coreAPI,
        lazyRemoteStateDispatcher = LazyResolver.adhoc(mockRemoteStateDispather),
        json = json,
    )

    override fun afterEach() {}

    @Test
    fun emptyLocalStorage_passcode_isNull() {
        assertThat(draftContentManager.passcode).isNull()
    }

    @Test
    fun valueInLocalStorage_passcode_matchesValue() {
        val passcode = "testpasscode"
        localStorage.app.core.draftContentPasscode.value = passcode

        assertThat(draftContentManager.passcode).isEqualTo(passcode)
    }

    @Test
    fun valueInLocalStorage_passcodeFlow_matchesValue() {
        val passcode = "testpasscode"
        localStorage.app.core.draftContentPasscode.value = passcode

        runTest {
            assertThat(draftContentManager.passcodeFlow.first()).isEqualTo(passcode)
        }
    }

    @Test
    fun valueInLocalStorage_deletePasscode_removesValuesSendsRemoteState() {
        localStorage.app.core.draftContentPasscode.value = "testpasscode"

        draftContentManager.deletePasscode()

        assertThat(localStorage.app.core.draftContentPasscode.value).isNull()
        assertThat(draftContentManager.passcode).isNull()
        assertThat(mockRemoteStateDispather.dispatchedEntry?.key).isEqualTo("draft_enabled")
        assertThat(mockRemoteStateDispather.dispatchedEntry?.toString()).isEqualTo("false")
    }

    @Test
    fun noConfig_setPasscode_returnsEarly() {
        configHolder.currentConfiguration.value = null
        var apiCalled = false
        coreAPI.getDraftOTAContentResponse = {
            apiCalled = true
            emptyList()
        }

        runTest {
            draftContentManager.setPasscode("passcode")
        }

        assertThat(apiCalled).isFalse
    }

    @Test
    fun failedApiCall_setPasscodeThrows_passcodeIsNotSet() {
        coreAPI.getDraftOTAContentResponse = { throw HttpException(Response.error<String>(401, "".toResponseBody())) }

        runTest {
            assertThrows<HttpException> {
                draftContentManager.setPasscode("passcode")
            }
        }

        assertThat(draftContentManager.passcode).isNull()
    }

    @Test
    fun successfulApiCall_setPasscode_setsPasscodeSendsRemoteState() {
        val passcode = "testpasscode"
        coreAPI.getDraftOTAContentResponse = { emptyList() }

        runTest {
            draftContentManager.setPasscode(passcode)
        }

        assertThat(localStorage.app.core.draftContentPasscode.value).isEqualTo(passcode)
        assertThat(draftContentManager.passcode).isEqualTo(passcode)
        assertThat(mockRemoteStateDispather.dispatchedEntry?.key).isEqualTo("draft_enabled")
        assertThat(mockRemoteStateDispather.dispatchedEntry?.toString()).isEqualTo("true")
    }
}
