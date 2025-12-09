package com.greencopper.interfacekit.color.recipe

import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.core.data.writeToPath
import com.greencopper.interfacekit.color.Color
import com.greencopper.interfacekit.color.ColorTheme
import com.greencopper.interfacekit.color.ColorsConfiguration
import com.greencopper.interfacekit.color.DefaultColors
import com.greencopper.interfacekit.color.repository.ConcreteColorRepository
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.io.File
import java.io.FileNotFoundException

internal class ColorRecipeAndroidTest {

    private val context = InstrumentationRegistry.getInstrumentation().context

    private lateinit var classUnderTest: ColorRecipe
    private val colorRepository: ConcreteColorRepository

    private val sourceDirectory by lazy { File(context.dataDir,"source") }
    private val colorsDirectory by lazy { File(sourceDirectory, "colors") }
    private val outputDirectory by lazy { File(context.dataDir,"output") }

    private val color = Color(0, 0)
    private val defaultColors = DefaultColors(
        DefaultColors.StatusBar(DefaultColors.StatusBar.Style.LIGHT),
        DefaultColors.Accent(color, color),
        DefaultColors.Background(color, color),
        DefaultColors.Label(color, color, color, color, color, color),
        DefaultColors.Fill(color, color, color, color, color),
        DefaultColors.TopBar(color, color, color),
        DefaultColors.Result(color, color)
    )

    init {
        Toolkit.setupTest(applicationContext = context)
        colorRepository = ConcreteColorRepository()
    }

    @BeforeEach
    fun setup() {
        classUnderTest = ColorRecipe(colorRepository)
        assert(sourceDirectory.mkdir())
        assert(colorsDirectory.mkdir())
        assert(outputDirectory.mkdir())
    }

    @AfterEach
    fun cleanUp() {
        sourceDirectory.deleteRecursively()
        colorsDirectory.deleteRecursively()
        outputDirectory.deleteRecursively()
    }

    @Test
    fun getComponentPath() {
        assertThat(classUnderTest.componentPath).isEqualTo("interfaceKit/color")
    }

    @Test
    fun tryToProcess_shouldThrow_withFileNotBeingADirectory() {
        val fileNotBeingADirectory = File("test.txt")
        assertThrows<IllegalArgumentException> {
            runTest {
                classUnderTest.tryToProcess(fileNotBeingADirectory, fileNotBeingADirectory)
            }
        }
    }

    @Test
    fun tryToProcess_shouldThrow_withDirectoryWithNoConfig() {
        val directoryWithNoConfig = File(sourceDirectory, "file_name")
        directoryWithNoConfig.writeText("random_content")
        assertThrows<IllegalArgumentException> {
            runTest {
                classUnderTest.tryToProcess(sourceDirectory, sourceDirectory)
            }
        }
    }

    @Test
    fun tryToProcess_shouldThrow_withNonExistentOutPutDirectory() {
        val configFile = File(sourceDirectory, "config.json")
        val colorsConfiguration = ColorsConfiguration(
            ColorTheme(
                defaultColors,
                null
            )
        )
        configFile.writeText(colorsConfiguration.encodeToString())
        assertThrows<FileNotFoundException> {
            runTest {
                classUnderTest.tryToProcess(
                    sourceDirectory,
                    File("random_invalid_path", "random_invalid_file")
                )
            }
        }
    }

    @Test
    fun tryToProcess_shouldThrow_withInvalidPath() {
        val configFile = File(sourceDirectory, "config.json")
        val colorsConfiguration = ColorsConfiguration(
            ColorTheme(
                defaultColors,
                null
            )
        )
        configFile.writeText(colorsConfiguration.encodeToString())
        assertThrows<FileNotFoundException> {
            runTest {
                classUnderTest.tryToProcess(
                    sourceDirectory,
                    File("random_invalid_path", "random_invalid_file")
                )
            }
        }
    }

    @Test
    fun tryToProcess_onSuccess_shouldGenerateOutputAtPath() {
        val configFile = File(sourceDirectory, "config.json")
        val colorsConfiguration = ColorsConfiguration(
            ColorTheme(
                defaultColors,
                null
            )
        )
        configFile.writeText(colorsConfiguration.encodeToString())
        assertDoesNotThrow {
            runTest {
                classUnderTest.tryToProcess(
                    sourceDirectory,
                    outputDirectory
                )
            }
        }
        assert(File(outputDirectory, "config.json").exists())
    }

    @Test
    fun tryToApply_shouldThrow_withNonExistingContent() {
        assertThrows<FileNotFoundException> {
            runTest {
                classUnderTest.tryToApply(File(""))
            }
        }
    }

    @Test
    fun tryToApply_shouldThrow_withWronglyFormattedConfigContent() {
        val configFile = File(sourceDirectory, "config.json")
        configFile.writeText("random_content")

        assertThrows<SerializationException> {
            runTest {
                classUnderTest.tryToApply(sourceDirectory)
            }
        }
    }

    @Test
    fun tryToApply_shouldSuccess_withConformConfig() {
        val colorsConfiguration = ColorsConfiguration(
            ColorTheme(
                defaultColors,
                null
            )
        )
        val configFile = File(sourceDirectory, "config.json")

        colorsConfiguration.writeToPath(configFile)
        assertDoesNotThrow {
            runTest {
                classUnderTest.tryToApply(sourceDirectory)
            }
        }

        assertThat(colorRepository.getDefaultColors()).isNotNull
    }

//    @Test
//    fun tryToProcess_withWrongAssets_shouldThrow() {
//        val testFile = File(colorsDirectory, "test")
//        testFile.writeText("random text")
//        every {
//            instrumentation.context.assets
//        } returns assetsManager
//        every {
//            assetsManager.list("colors")?.contains("colorsOverride-config.json")
//        } returns true
//        every {
//            assetsManager.open(any())
//        } returns FileInputStream(testFile)
//        every {
//            assetsManager.open("colors/colorsOverride-config.json").bufferedReader()
//                .use { it.readText() }
//        } returns "random text"
//        assertThrows<SerializationException> {
//            runTest {
//                colorRecipe.tryToProcess(
//                    sourceDirectory,
//                    outputDirectory
//                )
//            }
//        }
//    }
//
//    @Test
//    fun tryToProcess_withAssetMissing_shouldThrow() {
//        every {
//            instrumentation.context.assets
//        } returns assetsManager
//        every {
//            assetsManager.list("colors")?.contains("colorsOverride-config.json")
//        } returns true
//        every {
//            assetsManager.open(any())
//        } answers { throw FileNotFoundException() }
//        every {
//            assetsManager.open("colors/colorsOverride-config.json").bufferedReader()
//                .use { it.readText() }
//        } answers { throw FileNotFoundException() }
//
//        assertThrows<FileNotFoundException> {
//            runTest {
//                colorRecipe.tryToProcess(
//                    sourceDirectory,
//                    outputDirectory
//                )
//            }
//        }
//    }
}
