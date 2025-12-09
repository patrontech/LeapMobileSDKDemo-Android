package com.greencopper.thuzi.fanscan

import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.testmocks.interfacekit.MockLinkResolver
import com.greencopper.testmocks.interfacekit.MockRouteController
import kotlinx.serialization.json.buildJsonObject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ConcreteDecodeCallbackTest {

    private val classUnderTest: ConcreteDecodeCallback
    private var linkResolver = MockLinkResolver()
    private var routeController = MockRouteController()

    private var actionResult: String? = null
    private val route: Route = Route.Push(
        FeatureInfo(
            FeatureKey("", 1),
            buildJsonObject {}
        )
    )

    init {
        classUnderTest = ConcreteDecodeCallback(linkResolver, routeController)
        classUnderTest.setAction { actionResult = it.text }
    }

    @Test
    fun decodeValidDeeplink_shouldRedirect() {
        //given
        val qrCode = "https://123pouet/?test1=val1&deeplink=routePouet&test2=val2"
        linkResolver.mockRoutes = mapOf(
            "routePouet" to route
        )

        //when
        classUnderTest.onDecoded(qrCode.toResult())

        //then
        assertThat(actionResult).isNull()
        assertThat(routeController.lastRedirectRoute).isEqualTo(route)
    }

    @Test
    fun decodeInvalidDeeplink_shouldUseWholeResult() {
        //given
        val qrCode = "https://123pouet/?test1=val1&deeplink=routePouet&test2=val2"
        linkResolver.mockRoutes = mapOf(
            "unknown" to route
        )

        //when
        classUnderTest.onDecoded(qrCode.toResult())

        //then
        assertThat(actionResult).isEqualTo(qrCode)
        assertThat(routeController.lastRedirectRoute).isNull()
    }

    @Test
    fun decodeValidModuleId_shouldUseId() {
        //given
        val qrCode = "https://123pouet/?test1=val1&moduleID=123456&test2=val2"

        //when
        classUnderTest.onDecoded(qrCode.toResult())

        //then
        assertThat(actionResult).isEqualTo("123456")
        assertThat(routeController.lastRedirectRoute).isNull()
    }

    @Test
    fun decodeEmptyModuleId_shouldUseWholeResult() {
        //given
        val qrCode = "https://123pouet/?test1=val1&moduleID=&test2=val2"

        //when
        classUnderTest.onDecoded(qrCode.toResult())

        //then
        assertThat(actionResult).isEqualTo("https://123pouet/?test1=val1&moduleID=&test2=val2")
        assertThat(routeController.lastRedirectRoute).isNull()
    }

    @Test
    fun decodeDeeplinkAndModuleID_shouldRedirectDeeplink() {
        //given
        val qrCode = "https://123pouet/?test1=val1&moduleID=123456&deeplink=routePouet&test2=val2"
        linkResolver.mockRoutes = mapOf(
            "routePouet" to route
        )

        //when
        classUnderTest.onDecoded(qrCode.toResult())

        //then
        assertThat(actionResult).isNull()
        assertThat(routeController.lastRedirectRoute).isEqualTo(route)
    }

    @Test
    fun decodeSimpleUrl_shouldUseWholeResult() {
        //given
        val qrCode = "https://123pouet/?test1=val1&test2=val2"

        //when
        classUnderTest.onDecoded(qrCode.toResult())

        //then
        assertThat(actionResult).isEqualTo("https://123pouet/?test1=val1&test2=val2")
        assertThat(routeController.lastRedirectRoute).isNull()
    }

    @Test
    fun decodeBadUrl_shouldUseWholeResult() {
        //given
        val qrCode = "123pouet/?test1=val1&test2=val2"

        //when
        classUnderTest.onDecoded(qrCode.toResult())

        //then
        assertThat(actionResult).isEqualTo("123pouet/?test1=val1&test2=val2")
        assertThat(routeController.lastRedirectRoute).isNull()
    }

    private fun String.toResult() =
        Result(this, this.toByteArray(), emptyArray(), BarcodeFormat.QR_CODE)
}
