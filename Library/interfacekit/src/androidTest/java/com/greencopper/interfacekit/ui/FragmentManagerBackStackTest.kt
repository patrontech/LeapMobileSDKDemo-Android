package com.greencopper.interfacekit.ui

import androidx.fragment.app.FragmentManager
import com.greencopper.interfacekit.popBackStackIfPossible
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class FragmentManagerBackStackTest {
    @Test
    fun fragmentManagerPopBackStack_withoutBackStack_returnsFalse() {
        val fragmentManager = mockkClass(FragmentManager::class)
        every { fragmentManager.backStackEntryCount } returns 0
        every { fragmentManager.popBackStack() } throws IllegalStateException("FragmentManager error")

        assertThat(fragmentManager.popBackStackIfPossible().not())
    }

    @Test
    fun fragmentManagerPopBackStack_withBackStack_returnsTrue() {
        val fragmentManager = mockkClass(FragmentManager::class)
        every { fragmentManager.backStackEntryCount } returns 1
        every { fragmentManager.popBackStack() } returns Unit

        assertThat(fragmentManager.popBackStackIfPossible())
        verify(exactly = 1) {
            fragmentManager.popBackStack()
        }
    }
}