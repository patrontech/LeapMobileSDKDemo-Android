package com.greencopper.interfacekit.permissions

import android.content.Context
import android.content.Intent
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class PermissionsContractTest {

    private val context: Context = mockk(relaxed = true)
    private val permissions = setOf("permission1", "permission2")

    @Test
    fun createIntent_shouldReturnGivenIntent() {
        //given
        val contract = PermissionsContract()
        val intent: Intent = mockk()
        val data = PermissionsContract.PermissionsContractData(
            intent = intent,
            permissions = permissions
        )

        //when
        val result = contract.createIntent(context, data)

        //then
        assertThat(result).isEqualTo(intent)
    }

    @Test
    fun parseResult_shouldReturnGivenPermissions() {
        //given
        val contract = PermissionsContract()
        val intent: Intent = mockk()
        val data = PermissionsContract.PermissionsContractData(
            intent = intent,
            permissions = permissions
        )

        //when
        contract.createIntent(context, data)
        val result = contract.parseResult(0, mockk())

        //then
        assertThat(result).isEqualTo(permissions)
    }

    @Test
    fun parseResult_shouldReturnEmptyIfNotLaunched() {
        //given
        val contract = PermissionsContract()

        //when
        val result = contract.parseResult(0, mockk())

        //then
        assertThat(result).isEmpty()
    }
}