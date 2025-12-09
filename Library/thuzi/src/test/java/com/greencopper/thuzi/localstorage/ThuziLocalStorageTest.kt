package com.greencopper.thuzi.localstorage

import com.greencopper.core.localstorage.LocalStorage
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

internal class ThuziLocalStorageTest {

    private val localStorage: LocalStorage = LocalStorage("test")

    init {
        localStorage.project.thuzi.jwt.value = "jwt"
        localStorage.project.thuzi.attendeeId.value = "attendeeId"
        localStorage.project.thuzi.qrCode.value = "qrCode"
        localStorage.project.thuzi.userFirstName.value = "userFirstName"
    }

    @Test
    fun whenTokenNotExpired_fieldsReturnNotNull() {
        val date = LocalDateTime.now().plusYears(1)
        localStorage.project.thuzi.jwtExpirationDate.value = ZonedDateTime.of(date, ZoneId.systemDefault()).toString()

        Assertions.assertThat(localStorage.project.thuzi.jwt.value).isNotNull
        Assertions.assertThat(localStorage.project.thuzi.attendeeId.value).isNotNull
        Assertions.assertThat(localStorage.project.thuzi.qrCode.value).isNotNull
        Assertions.assertThat(localStorage.project.thuzi.userFirstName.value).isNotNull
    }

    @Test
    fun whenTokenExpired_fieldsReturnNull() {
        val date = LocalDateTime.now().minusYears(1)
        localStorage.project.thuzi.jwtExpirationDate.value = ZonedDateTime.of(date, ZoneId.systemDefault()).toString()

        Assertions.assertThat(localStorage.project.thuzi.jwt.value).isNull()
        Assertions.assertThat(localStorage.project.thuzi.attendeeId.value).isNull()
        Assertions.assertThat(localStorage.project.thuzi.qrCode.value).isNull()
        Assertions.assertThat(localStorage.project.thuzi.userFirstName.value).isNull()
    }

}