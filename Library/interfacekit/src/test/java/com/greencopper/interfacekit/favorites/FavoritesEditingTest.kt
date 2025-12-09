package com.greencopper.interfacekit.favorites

import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class FavoritesEditingTest {

    init {
        Toolkit.setupTest()
    }

    @Test
    fun translate_shouldTransform() {
        val localizationService = MockLocalizationService() {
            when (it) {
                "addKey" -> "addValue"
                "removeKey" -> "removeValue"
                else -> "null"
            }
        }

        val favEdit = FavoritesEditing(
            FavoritesEditing.Icon("add", "addKey"),
            FavoritesEditing.Icon("remove", "removeKey"),
        )
        val expected = FavoritesEditing(
            FavoritesEditing.Icon("add", "addValue"),
            FavoritesEditing.Icon("remove", "removeValue"),
        )
        val translated = favEdit.translate(localizationService)

        assertThat(translated).isEqualTo(expected)

    }

}
