package com.greencopper.interfacekit.rootview

import com.greencopper.interfacekit.navigation.layout.Layout
import io.mockk.mockkClass
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class RootLayoutHolderTest {

    private val layout2 = mockkClass(Layout::class)

    private lateinit var rootLayoutHolder: RootLayoutHolder

    @BeforeEach
    fun setup() {
        rootLayoutHolder = RootLayoutHolder()
    }

    @Test
    fun rootLayoutStaticFlow_shouldBeNullable() {
        rootLayoutHolder.clearRootLayout()
        assertThat(RootLayoutHolder.rootLayoutHolder.value).isNull()
    }

    @Test
    fun rootLayout_set_shouldNotThrow() {
        assertDoesNotThrow {
            rootLayoutHolder.setRootLayout(layout2)
        }
        runTest {
            assertThat(RootLayoutHolder.rootLayoutHolder.filterNotNull().first()).isEqualTo(layout2)
            assertThat(RootLayoutHolder.onBackPressDispatcher).isNotNull
        }
    }

    @Test
    fun clearRootLayout_rootLayoutHolder_returnsNull() {
        rootLayoutHolder.setRootLayout(layout2)
        rootLayoutHolder.clearRootLayout()
        assertThat(RootLayoutHolder.rootLayoutHolder.value).isNull()
    }
}
