package com.greencopper.thuzi.account.deletion

import com.greencopper.core.metrics.labels.EventName
import com.greencopper.testmocks.core.MockingMappedProvider
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class AccountDeletionEventTest {
    @Test
    fun whenAccountDeletionEventTracked_shouldBeTracked() {
        val testEvent = AccountDeletionViewModel.AccountDeletionEvent("testEvent")
        val mockingMappedProvider = MockingMappedProvider()
        mockingMappedProvider.enable()
        testEvent.track(mockingMappedProvider)
        Assertions.assertThat(mockingMappedProvider.wasMetricTracked(EventName("testEvent"))).isTrue
    }
}