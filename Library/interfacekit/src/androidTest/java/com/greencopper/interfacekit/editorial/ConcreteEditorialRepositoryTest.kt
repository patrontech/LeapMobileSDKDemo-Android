package com.greencopper.interfacekit.editorial

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.interfacekit.editorial.repository.ConcreteEditorialPageRepository
import com.greencopper.testmocks.core.MockLocalizationService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

internal class ConcreteEditorialRepositoryTest {

    private val context = InstrumentationRegistry.getInstrumentation().context
    private val localizationValue = "editorial"

    private val localizationService = MockLocalizationService(
        getStringFromRepository = { localizationValue }
    )

    private lateinit var repository: ConcreteEditorialPageRepository

    @BeforeEach
    fun beforeEach() {
        repository = ConcreteEditorialPageRepository(localizationService)
    }

    @Test
    fun withNullDirectory_returnsNull() {
        val uri = repository.getFileUri("fileName")
        assertThat(uri).isNull()
    }

    @Test
    fun withDirectory_returnsUri() {
        val parent = context.getDir("directory", Context.MODE_PRIVATE).apply { mkdirs() }
        val file = File(parent, localizationValue).apply { createNewFile() }

        repository.setContentDirectoryPath(parent.path)
        val uri = repository.getFileUri(file.name)

        assertThat(uri).isNotNull
        assertThat(uri.toString()).contains(localizationValue)

        file.delete()
        parent.delete()
    }
}