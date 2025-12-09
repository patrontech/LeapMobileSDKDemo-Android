package com.greencopper.event

import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.event.dao.EventDatabase
import com.greencopper.event.mock.MockDatabaseHelper
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

internal class RoomEventDataProcessorTest: CoroutineTest() {

    init {
        Toolkit.setupTest()
    }

    private val context = InstrumentationRegistry.getInstrumentation().context

    private val roomEventDataProcessor = RoomEventDataProcessor(
        jsonParser = mockk(),
        context = context,
        databaseHelper = MockDatabaseHelper(context),
        eventDatabaseScope = testScope
    )

    override fun afterEach() {}

    @Test
    fun apply_updatesDatabaseFile() {
        runTest {
            roomEventDataProcessor.apply(File(""))
            assertThat(EventDatabase.databaseFile.value?.name).isEqualTo(EventDatabase.DATABASE_NAME)
        }
    }
}
