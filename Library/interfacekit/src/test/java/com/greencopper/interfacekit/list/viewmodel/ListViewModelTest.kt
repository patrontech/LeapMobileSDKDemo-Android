package com.greencopper.interfacekit.list.viewmodel

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.greencopper.interfacekit.empty.EmptyPage
import com.greencopper.interfacekit.favorites.FavoriteConfig
import com.greencopper.interfacekit.filtering.FilteringHandler
import com.greencopper.interfacekit.filtering.FilteringInfo
import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.interfacekit.filtering.MockFilteringPredicateComputed
import com.greencopper.interfacekit.filtering.filteringbar.FilteringBarData
import com.greencopper.interfacekit.filtering.filteringbar.FilteringButton
import com.greencopper.interfacekit.filtering.filteringbar.ui.FilteringBarCell
import com.greencopper.interfacekit.interests.integration.IntegratedInterestsData
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.ui.compose.IKViewBuilder
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.MockStore
import com.greencopper.testmocks.core.MockConditionChecker
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.interfacekit.MockColorRepository
import com.greencopper.testmocks.interfacekit.MockFilteringHandler
import com.greencopper.testmocks.interfacekit.MockImageService
import com.greencopper.testmocks.interfacekit.MockTextStyleRepository
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.shouldBe
import com.greencopper.toolkit.Toolkit
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ListViewModelTest : CoroutineTest(UnconfinedTestDispatcher()) {

    init {
        Toolkit.setupTest()
    }

    private val filteringHandler = MockFilteringHandler()
    private var initialData = createListData()
    private var initialState = ListState()
    private val store: MockStore<ListState, ListAction> by lazy { MockStore(initialState) }
    private val conditionChecker = MockConditionChecker()

    private val viewModel by lazy {
        ListViewModel(
            filteringHandler = filteringHandler,
            viewBuilder = IKViewBuilder(
                colorRepository = MockColorRepository(),
                textStyleRepository = MockTextStyleRepository(),
                localizationService = MockLocalizationService(),
                imageService = MockImageService(),
            ),
            listData = initialData,
            store = store,
            conditionChecker = conditionChecker,
            scope = testScope,
        )
    }

    override fun afterEach() {}

    @Test
    fun `changing filtering should send action`() = runTest {
        viewModel

        val predicate = MockFilteringPredicateComputed("testing")
        filteringHandler.mockedPredicate = predicate

        store.actionSent.last() shouldBe ListAction.User.FilteringUpdated(predicate)
    }

    @Test
    fun `changing selectedList should update filteringHandler`() = runTest {
        viewModel

        store.mutableState.value = initialState.copy(selectedList = setOf(SelectedList.MyFavorites))

        filteringHandler.currentMode shouldBe FilteringHandler.Mode.MY_FAVORITES
    }

    @Test
    fun `initialSetup should send action`() {
        val layout = mockk<Layout>()
        val uiClient = ListReducer.UiClient {}
        viewModel.initialSetup(layout, uiClient)

        assertThat(store.actionSent).contains(ListAction.ScreenLoaded(layout, uiClient))
    }

    @Test
    fun `get filtering buttons with no favorites should return an empty list`() = runTest {
        val buttons = viewModel.getFilteringBarButtons()

        buttons shouldBe emptyList()
    }

    @Test
    fun `get filtering buttons with no buttons provided should return an empty list`() = runTest {
        initialData = initialData.copy(
            myFavorites = FavoriteConfig(
                emptyPage = EmptyPage(
                    image = "imageName",
                    title = "title",
                    subtitle = "subtitle",
                    topWidgetCollection = WidgetCollectionConfiguration.Instance(
                        widgets = listOf(
                            WidgetCollectionConfiguration.Instance.WidgetInfo(
                                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                JsonArray(emptyList()),
                            )
                        )
                    )
                )
            )
        )
        val buttons = viewModel.getFilteringBarButtons()

        buttons shouldBe emptyList()
    }

    @Test
    fun `get filtering buttons with buttons provided should return buttons, with myfavorites checked`() = runTest {
        initialData = initialData.copy(
            myFavorites = FavoriteConfig(
                filteringButton = FilteringButton(
                    selected = FilteringButton.Button(
                        icon = "iconSelected",
                        title = "titleSelected",
                        displayNumber = true,
                        accessibilityLabel = "accessibilitySelected"
                    ), unselected = FilteringButton.Button(
                        icon = "iconUnselected",
                        title = "titleUnselected",
                        displayNumber = false,
                        accessibilityLabel = "accessibilityUnselected"
                    )
                ),
                emptyPage = EmptyPage(
                    image = "imageName",
                    title = "title",
                    subtitle = "subtitle",
                    topWidgetCollection = WidgetCollectionConfiguration.Instance(
                        widgets = listOf(
                            WidgetCollectionConfiguration.Instance.WidgetInfo(
                                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                JsonArray(emptyList()),
                            )
                        )
                    )
                )
            )
        )
        initialState = initialState.copy(selectedList = setOf(SelectedList.MyFavorites))

        val buttons = viewModel.getFilteringBarButtons()

        assertThat(buttons).hasSize(1)

        with(buttons[0]) {
            default shouldBe FilteringBarCell.ButtonState.State(
                title = "titleUnselected",
                icon = "iconUnselected",
                accessibilityLabel = "accessibilityUnselected"
            )
            selected shouldBe FilteringBarCell.ButtonState.State(
                title = "titleSelected",
                icon = "iconSelected",
                accessibilityLabel = "accessibilitySelected"
            )
            isCheckedAtSetup shouldBe true

            onButtonToggled(false)
        }

        store.actionSent.last() shouldBe ListAction.User.TappedMyFavorites(false)
    }

    @Test
    fun `get filtering buttons with buttons provided should return buttons, with myfavorites unchecked`() = runTest {
        initialData = initialData.copy(
            myFavorites = FavoriteConfig(
                filteringButton = FilteringButton(
                    selected = FilteringButton.Button(
                        icon = "iconSelected",
                        title = "titleSelected",
                        displayNumber = true,
                        accessibilityLabel = "accessibilitySelected"
                    ), unselected = FilteringButton.Button(
                        icon = "iconUnselected",
                        title = "titleUnselected",
                        displayNumber = false,
                        accessibilityLabel = "accessibilityUnselected"
                    )
                ),
                emptyPage = EmptyPage(
                    image = "imageName",
                    title = "title",
                    subtitle = "subtitle",
                    topWidgetCollection = WidgetCollectionConfiguration.Instance(
                        widgets = listOf(
                            WidgetCollectionConfiguration.Instance.WidgetInfo(
                                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                JsonArray(emptyList()),
                            )
                        )
                    )
                )
            )
        )
        initialState = initialState.copy(selectedList = emptySet())

        val buttons = viewModel.getFilteringBarButtons()

        assertThat(buttons).hasSize(1)

        with(buttons[0]) {
            default shouldBe FilteringBarCell.ButtonState.State(
                title = "titleUnselected",
                icon = "iconUnselected",
                accessibilityLabel = "accessibilityUnselected"
            )
            selected shouldBe FilteringBarCell.ButtonState.State(
                title = "titleSelected",
                icon = "iconSelected",
                accessibilityLabel = "accessibilitySelected"
            )
            isCheckedAtSetup shouldBe false

            onButtonToggled(true)
        }

        store.actionSent.last() shouldBe ListAction.User.TappedMyFavorites(true)
    }

    @Test
    fun `get filtering buttons with buttons provided should return buttons, with myInterests unchecked`() = runTest {
        initialData = initialData.copy(
            myInterests = IntegratedInterestsData(
                filteringButton = FilteringButton(
                    selected = FilteringButton.Button(
                        icon = "iconSelected",
                        title = "titleSelected",
                        displayNumber = true,
                        accessibilityLabel = "accessibilitySelected"
                    ), unselected = FilteringButton.Button(
                        icon = "iconUnselected",
                        title = "titleUnselected",
                        displayNumber = false,
                        accessibilityLabel = "accessibilityUnselected"
                    )
                ),
                emptyPage = EmptyPage(
                    image = "imageName",
                    title = "title",
                    subtitle = "subtitle",
                    topWidgetCollection = WidgetCollectionConfiguration.Instance(
                        widgets = listOf(
                            WidgetCollectionConfiguration.Instance.WidgetInfo(
                                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                JsonArray(emptyList()),
                            )
                        )
                    )
                )
            )
        )
        initialState = initialState.copy(selectedList = emptySet())

        val buttons = viewModel.getFilteringBarButtons()

        assertThat(buttons).hasSize(1)

        with(buttons[0]) {
            default shouldBe FilteringBarCell.ButtonState.State(
                title = "titleUnselected",
                icon = "iconUnselected",
                accessibilityLabel = "accessibilityUnselected"
            )
            selected shouldBe FilteringBarCell.ButtonState.State(
                title = "titleSelected",
                icon = "iconSelected",
                accessibilityLabel = "accessibilitySelected"
            )
            isCheckedAtSetup shouldBe false

            onButtonToggled(true)
        }

        store.actionSent.last() shouldBe ListAction.User.TappedMyInterests(true)
    }

    @Test
    fun `get filtering buttons with buttons provided should return buttons, with myInterests checked`() = runTest {
        initialData = initialData.copy(
            myInterests = IntegratedInterestsData(
                filteringButton = FilteringButton(
                    selected = FilteringButton.Button(
                        icon = "iconSelected",
                        title = "titleSelected",
                        displayNumber = true,
                        accessibilityLabel = "accessibilitySelected"
                    ), unselected = FilteringButton.Button(
                        icon = "iconUnselected",
                        title = "titleUnselected",
                        displayNumber = false,
                        accessibilityLabel = "accessibilityUnselected"
                    )
                ),
                emptyPage = EmptyPage(
                    image = "imageName",
                    title = "title",
                    subtitle = "subtitle",
                    topWidgetCollection = WidgetCollectionConfiguration.Instance(
                        widgets = listOf(
                            WidgetCollectionConfiguration.Instance.WidgetInfo(
                                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                JsonArray(emptyList()),
                            )
                        )
                    )
                )
            )
        )
        initialState = initialState.copy(selectedList = setOf(SelectedList.MyInterests))

        val buttons = viewModel.getFilteringBarButtons()

        assertThat(buttons).hasSize(1)

        with(buttons[0]) {
            default shouldBe FilteringBarCell.ButtonState.State(
                title = "titleUnselected",
                icon = "iconUnselected",
                accessibilityLabel = "accessibilityUnselected"
            )
            selected shouldBe FilteringBarCell.ButtonState.State(
                title = "titleSelected",
                icon = "iconSelected",
                accessibilityLabel = "accessibilitySelected"
            )
            isCheckedAtSetup shouldBe true

            onButtonToggled(false)
        }

        store.actionSent.last() shouldBe ListAction.User.TappedMyInterests(false)
    }

    @Test
    fun `contentState should return content's state`() = runTest {
        val expectedContent = ViewState.ContentState.Empty(
            title = "intellegat",
            subtitle = "commune",
            imageName = "Helen Dunn",
            widgets = WidgetCollectionConfiguration.Instance(
                widgets = listOf(
                    WidgetCollectionConfiguration.Instance.WidgetInfo(
                        WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                        JsonArray(emptyList()),
                    )
                )
            ),
            screenName = "screenName"
        )
        initialState = initialState.copy(content = expectedContent)

        val content = viewModel.contentState.first()

        content shouldBe expectedContent
    }

    @Test
    fun `onListItemTap should send action to store`() {
        viewModel.onListItemTap(123L)

        store.actionSent.last() shouldBe ListAction.User.TappedListItem(123L)
    }

    @Test
    fun `sendAction should send action to store`() {
        viewModel.sendAction(ListAction.ItemsReloaded)

        store.actionSent.last() shouldBe ListAction.ItemsReloaded
    }

    @Test
    fun `saveState should save state in provided bundle`() {
        val expectedContent = ViewState.ContentState.Empty(
            title = "intellegat",
            subtitle = "commune",
            imageName = "Helen Dunn",
            widgets = WidgetCollectionConfiguration.Instance(
                widgets = listOf(
                    WidgetCollectionConfiguration.Instance.WidgetInfo(
                        WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                        JsonArray(emptyList()),
                    )
                )
            ),
            screenName = "screenName"
        )
        initialState = initialState.copy(content = expectedContent)

        val addedToBundle = slot<String>()
        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putString("keyState", capture(addedToBundle)) } returns Unit

        val bundle = Bundle()
        viewModel.saveState(bundle, "keyState")

        addedToBundle.captured shouldBe initialState.encodeToString()
    }

    @Test
    fun `getFilteringBarData should return mocked list`() {
        filteringHandler.mockedFilteringBarData = FilteringBarData(emptyList())
        val filteringBarData = viewModel.getFilteringBarData(DialogFragment())
        assertThat(filteringBarData.filters).isEmpty()
    }

    @Test
    fun `getCurrentFilterState should return mocked CurrentStateInfo`() {
        val result = viewModel.getCurrentFilterState().filteringInfoMap
        result[FilteringHandler.Mode.DEFAULT] shouldBe null

        val testFilteringInfo = FilteringInfo(
            predicate = FilteringPredicate.Tag("tag")
        )
        filteringHandler.mockedCurrentStatesMap = mapOf(
            FilteringHandler.Mode.DEFAULT to testFilteringInfo
        )
        viewModel.getCurrentFilterState().filteringInfoMap[FilteringHandler.Mode.DEFAULT] shouldBe testFilteringInfo
    }
}
