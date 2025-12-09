package com.greencopper.thuzi.eventpass

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.testmocks.setupTest
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

internal class EventPassViewModelTest {
    private val localStorage: LocalStorage
    private val eventPassViewModel: EventPassViewModel

    init {
        Toolkit.setupTest()
        localStorage = App.resolve()
        eventPassViewModel = EventPassViewModel(localStorage)
    }

    @BeforeEach
    fun beforeEach() {
        val expiration = LocalDateTime.now().plusYears(1)
        localStorage.project.thuzi.jwtExpirationDate.value = ZonedDateTime.of(expiration, ZoneId.systemDefault()).toString()
    }

    @Test
    fun whenBarcodeInfo_notPresent_shouldBeEmpty() {
        localStorage.project.thuzi.qrCode.value = null
        runTest {
            val barcode = eventPassViewModel.getBarcodeData().first()
            assertThat(barcode.barcodeValue).isBlank
        }
    }

    @Test
    fun whenFirstNameInfo_notPresent_shouldBeEmpty() {
        localStorage.project.thuzi.userFirstName.value = null
        runTest {
            val barcode = eventPassViewModel.getBarcodeData().first()
            assertThat(barcode.userFirstName).isBlank
        }
    }

    @Test
    fun whenBarcodeInfo_present_shouldBeMatching() {
        val barcodeValue = "testBarcodeValue"
        localStorage.project.thuzi.qrCode.value = barcodeValue
        runTest {
            val barcode = eventPassViewModel.getBarcodeData().first()
            assertThat(barcode.barcodeValue).isEqualTo(barcodeValue)
        }
    }

    @Test
    fun whenFirstNameInfo_present_shouldBeMatching() {
        val firstNameValue = "testFirstName"
        localStorage.project.thuzi.userFirstName.value = firstNameValue
        runTest {
            val barcode = eventPassViewModel.getBarcodeData().first()
            assertThat(barcode.userFirstName).isEqualTo(firstNameValue)
        }
    }
}