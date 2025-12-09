package com.greencopper.interfacekit.onboarding.pages

import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.core.conditions.ConditionSet
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.testKiboSerializable
import com.greencopper.toolkit.Toolkit
import org.junit.jupiter.api.Test

internal class OnboardingPageInfoTest {

    init {
        Toolkit.setupTest()
    }

    @Test
    fun kiboSerializable() {
        val info = OnboardingPageInfo(
            id = "id",
            key = OnboardingPageKey("string", 1),
            conditionSet = ConditionSet(
                predicate = "predicate",
                conditions = mapOf("key" to ConditionInfo(ConditionInfo.Key("name", 1), fallback = false))
            ),
        )
        testKiboSerializable(info)
    }
}
