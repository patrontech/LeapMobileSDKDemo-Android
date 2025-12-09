package com.greencopper.interfacekit.widgets.viewmodel

import com.greencopper.core.conditions.ConditionSet
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration.Instance.WidgetInfo
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration.Instance.WidgetKey
import com.greencopper.interfacekit.widgets.resolver.WidgetNotFoundException
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionCellLayoutData
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.core.MockConditionChecker
import com.greencopper.testmocks.interfacekit.MockWidgetResolver
import com.greencopper.testmocks.shouldBe
import com.greencopper.testmocks.toolkit.MockLogging
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class IndexedWidgetsTest : CoroutineTest(StandardTestDispatcher()) {

    override fun afterEach() {}

    private lateinit var initialPredicate: String
    private val mutablePredicate by lazy { MutableStateFlow(initialPredicate) }
    private val conditionChecker by lazy {
        MockConditionChecker(
            mockCheckConditionSetFlow = { conditionSet ->
                mutablePredicate.map { conditionSet.predicate.contains(it) }
            }
        )
    }
    private val logger = MockLogging()

    @Test
    @DisplayName(
        "Function should transform list of indexed WidgetCollections " +
                "to a Flow of indexed lists of WidgetGenerators " +
                "and react to conditions changes filtering out"
    )
    fun transformAndReactFilterOut() = runTest {
        initialPredicate = "ab"
        val expectedScreenName = "screenName123"
        val expectedOrigin = mockk<Layout>()

        val widgetResolver = MockWidgetResolver(
            resolveGenerator = { widgetInfo, screenName, origin ->
                screenName shouldBe expectedScreenName
                origin shouldBe expectedOrigin

                MockWidgetGenerator(id = widgetInfo.key.name)
            }
        )

        val widgetList = listOf(
            WidgetCollectionCellLayoutData(
                1, WidgetCollectionConfiguration.Instance(
                    null, listOf(
                        WidgetInfo(
                            key = WidgetKey("Mock11", 1),
                            params = JsonPrimitive(0),
                            conditionSet = ConditionSet("abc", mapOf())
                        ),
                        WidgetInfo(
                            key = WidgetKey("Mock12", 1),
                            params = JsonPrimitive(0),
                            conditionSet = ConditionSet("z", mapOf())
                        ),
                        WidgetInfo(
                            key = WidgetKey("Mock13", 1),
                            params = JsonPrimitive(0),
                            conditionSet = ConditionSet("ab", mapOf())
                        ),
                    )
                )
            ),
            WidgetCollectionCellLayoutData(
                0, WidgetCollectionConfiguration.Instance(
                    null, listOf(
                        WidgetInfo(
                            key = WidgetKey("Mock21", 1),
                            params = JsonPrimitive(0),
                            conditionSet = ConditionSet("ab", mapOf())
                        ),
                    )
                )
            ),
            WidgetCollectionCellLayoutData(
                0, WidgetCollectionConfiguration.Instance(
                    null, listOf(
                        WidgetInfo(
                            key = WidgetKey("Mock31", 1),
                            params = JsonPrimitive(0),
                            conditionSet = ConditionSet("abc", mapOf())
                        ),
                    )
                )
            )
        )

        val actionsList = mutableListOf<WidgetAction>()

        val job = launch {
            widgetList.getFlowOfIndexedWidgets(
                widgetResolver = widgetResolver,
                conditionChecker = conditionChecker,
                origin = expectedOrigin,
                screenName = expectedScreenName,
                logger = logger,
                toAction = { indexedWidgets ->
                    WidgetAction(indexedWidgets)
                }
            ).collect {
                actionsList.add(it)
            }
        }
        delay(500)

        mutablePredicate.value = "abc"
        delay(500)

        with(actionsList[0]) {
            list.size shouldBe 2
            with(list[0]) {
                widgets.size shouldBe 1
                widgets[0].id shouldBe "Mock21"
            }
            with(list[1]) {
                widgets.size shouldBe 2
                widgets[0].id shouldBe "Mock11"
                widgets[1].id shouldBe "Mock13"
            }
        }

        with(actionsList[1]) {
            list.size shouldBe 1
            with(list[0]) {
                widgets.size shouldBe 1
                widgets[0].id shouldBe "Mock11"
            }
        }

        actionsList.size shouldBe 2
        job.cancel()
    }

    @Test
    @DisplayName(
        "Function should transform list of indexed WidgetCollections " +
                "to a Flow of indexed lists of WidgetGenerators " +
                "and react to conditions changes filtering in"
    )
    fun transformAndReactFilterIn() = runTest {
        initialPredicate = "abc"
        val expectedScreenName = "screenName123"
        val expectedOrigin = mockk<Layout>()

        val widgetResolver = MockWidgetResolver(
            resolveGenerator = { widgetInfo, screenName, origin ->
                screenName shouldBe expectedScreenName
                origin shouldBe expectedOrigin

                MockWidgetGenerator(id = widgetInfo.key.name)
            }
        )

        val widgetList = listOf(
            WidgetCollectionCellLayoutData(
                1, WidgetCollectionConfiguration.Instance(
                    null, listOf(
                        WidgetInfo(
                            key = WidgetKey("Mock11", 1),
                            params = JsonPrimitive(0),
                            conditionSet = ConditionSet("abc", mapOf())
                        ),
                        WidgetInfo(
                            key = WidgetKey("Mock12", 1),
                            params = JsonPrimitive(0),
                            conditionSet = ConditionSet("z", mapOf())
                        ),
                        WidgetInfo(
                            key = WidgetKey("Mock13", 1),
                            params = JsonPrimitive(0),
                            conditionSet = ConditionSet("ab", mapOf())
                        ),
                    )
                )
            ),
            WidgetCollectionCellLayoutData(
                0, WidgetCollectionConfiguration.Instance(
                    null, listOf(
                        WidgetInfo(
                            key = WidgetKey("Mock21", 1),
                            params = JsonPrimitive(0),
                            conditionSet = ConditionSet("ab", mapOf())
                        ),
                    )
                )
            ),
            WidgetCollectionCellLayoutData(
                2, WidgetCollectionConfiguration.Instance(
                    null, listOf(
                        WidgetInfo(
                            key = WidgetKey("Mock31", 1),
                            params = JsonPrimitive(0),
                            conditionSet = ConditionSet("abc", mapOf())
                        ),
                    )
                )
            )
        )

        val actionsList = mutableListOf<WidgetAction>()

        val job = launch {
            widgetList.getFlowOfIndexedWidgets(
                widgetResolver = widgetResolver,
                conditionChecker = conditionChecker,
                origin = expectedOrigin,
                screenName = expectedScreenName,
                logger = logger,
                toAction = { indexedWidgets ->
                    WidgetAction(indexedWidgets)
                }
            ).collect {
                actionsList.add(it)
            }
        }
        delay(500)

        mutablePredicate.value = "ab"
        delay(500)

        println(actionsList)
        with(actionsList[0]) {
            list.size shouldBe 2
            with(list[0]) {
                widgets.size shouldBe 1
                widgets[0].id shouldBe "Mock11"
            }
            with(list[1]) {
                widgets.size shouldBe 1
                widgets[0].id shouldBe "Mock31"
            }
        }

        with(actionsList[1]) {
            list.size shouldBe 3
            with(list[0]) {
                widgets.size shouldBe 1
                widgets[0].id shouldBe "Mock21"
            }
            with(list[1]) {
                widgets.size shouldBe 2
                widgets[0].id shouldBe "Mock11"
                widgets[1].id shouldBe "Mock13"
            }
            with(list[2]) {
                widgets.size shouldBe 1
                widgets[0].id shouldBe "Mock31"
            }
        }

        actionsList.size shouldBe 2
        job.cancel()
    }

    @Test
    fun `Unresolvable widget shouldn't throw`() = runTest {
        initialPredicate = "ab"
        val expectedScreenName = "screenName123"
        val expectedOrigin = mockk<Layout>()

        val widgetResolver = MockWidgetResolver(
            resolveGenerator = { widgetInfo, screenName, origin ->
                screenName shouldBe expectedScreenName
                origin shouldBe expectedOrigin

                if (widgetInfo.key.name == "Throw") {
                    throw WidgetNotFoundException(
                        widgetInfo.key
                    )
                } else {
                    MockWidgetGenerator(id = widgetInfo.key.name)
                }
            }
        )

        val widgetList = listOf(
            WidgetCollectionCellLayoutData(
                1, WidgetCollectionConfiguration.Instance(
                    null, listOf(
                        WidgetInfo(
                            key = WidgetKey("Mock11", 1),
                            params = JsonPrimitive(0),
                            conditionSet = ConditionSet("abc", mapOf())
                        ),
                        WidgetInfo(
                            key = WidgetKey("Mock12", 1),
                            params = JsonPrimitive(0),
                            conditionSet = ConditionSet("abc", mapOf())
                        ),
                        WidgetInfo(
                            key = WidgetKey("Throw", 1),
                            params = JsonPrimitive(0),
                            conditionSet = ConditionSet("abc", mapOf())
                        ),
                    )
                )
            ),
        )


        val resultAction =
            widgetList.getFlowOfIndexedWidgets(
                widgetResolver = widgetResolver,
                conditionChecker = conditionChecker,
                origin = expectedOrigin,
                screenName = expectedScreenName,
                logger = logger,
                toAction = { indexedWidgets ->
                    WidgetAction(indexedWidgets)
                }
            ).first()

        with(resultAction) {
            list.size shouldBe 1
            with(list[0]) {
                widgets.size shouldBe 2
                widgets[0].id shouldBe "Mock11"
                widgets[1].id shouldBe "Mock12"
            }
        }

    }

    @Test
    fun `No widgets provided should return empty Action`() = runTest {
        initialPredicate = "ab"
        val expectedScreenName = "screenName123"
        val expectedOrigin = mockk<Layout>()

        val widgetResolver = MockWidgetResolver(
            resolveGenerator = { widgetInfo, screenName, origin ->
                screenName shouldBe expectedScreenName
                origin shouldBe expectedOrigin

                MockWidgetGenerator(id = widgetInfo.key.name)
            }
        )

        val resultAction = null.getFlowOfIndexedWidgets(
            widgetResolver = widgetResolver,
            conditionChecker = conditionChecker,
            origin = expectedOrigin,
            screenName = expectedScreenName,
            logger = logger,
            toAction = { indexedWidgets ->
                WidgetAction(indexedWidgets)
            }
        ).first()

        resultAction.list shouldBe emptyList()
    }

    @Test
    fun `Empty list of widgets provided should return empty Action`() = runTest {
        initialPredicate = "ab"
        val expectedScreenName = "screenName123"
        val expectedOrigin = mockk<Layout>()

        val widgetResolver = MockWidgetResolver(
            resolveGenerator = { widgetInfo, screenName, origin ->
                screenName shouldBe expectedScreenName
                origin shouldBe expectedOrigin

                MockWidgetGenerator(id = widgetInfo.key.name)
            }
        )

        val resultAction = listOf<WidgetCollectionCellLayoutData>().getFlowOfIndexedWidgets(
            widgetResolver = widgetResolver,
            conditionChecker = conditionChecker,
            origin = expectedOrigin,
            screenName = expectedScreenName,
            logger = logger,
            toAction = { indexedWidgets ->
                WidgetAction(indexedWidgets)
            }
        ).first()

        resultAction.list shouldBe emptyList()
    }

    @Test
    fun `No widgets respecting conditions should return empty Action`() = runTest {
        initialPredicate = "xyz"
        val expectedScreenName = "screenName123"
        val expectedOrigin = mockk<Layout>()

        val widgetResolver = MockWidgetResolver(
            resolveGenerator = { widgetInfo, screenName, origin ->
                screenName shouldBe expectedScreenName
                origin shouldBe expectedOrigin

                MockWidgetGenerator(id = widgetInfo.key.name)
            }
        )

        val widgetList = listOf(
            WidgetCollectionCellLayoutData(
                1, WidgetCollectionConfiguration.Instance(
                    null, listOf(
                        WidgetInfo(
                            key = WidgetKey("Mock11", 1),
                            params = JsonPrimitive(0),
                            conditionSet = ConditionSet("abc", mapOf())
                        ),
                        WidgetInfo(
                            key = WidgetKey("Mock12", 1),
                            params = JsonPrimitive(0),
                            conditionSet = ConditionSet("z", mapOf())
                        ),
                        WidgetInfo(
                            key = WidgetKey("Mock13", 1),
                            params = JsonPrimitive(0),
                            conditionSet = ConditionSet("ab", mapOf())
                        ),
                    )
                )
            )
        )

        val resultAction =
            widgetList.getFlowOfIndexedWidgets(
                widgetResolver = widgetResolver,
                conditionChecker = conditionChecker,
                origin = expectedOrigin,
                screenName = expectedScreenName,
                logger = logger,
                toAction = { indexedWidgets ->
                    WidgetAction(indexedWidgets)
                }
            ).first()

        resultAction.list shouldBe emptyList()
    }

    data class WidgetAction(val list: List<IndexedWidgets>)
}
