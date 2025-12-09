package com.greencopper.interfacekit.interests.ui

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.databinding.ComposeViewBinding
import com.greencopper.interfacekit.interests.InterestsLayoutData
import com.greencopper.interfacekit.interests.viewmodel.*
import com.greencopper.interfacekit.metrics.interestsPicker
import com.greencopper.interfacekit.onboarding.OnboardingSequenceViewData
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayout
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.ui.compose.MainCompositionLocalProvider
import com.greencopper.interfacekit.ui.compose.mockColors
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import com.greencopper.interfacekit.ui.viewBinding
import com.greencopper.interfacekit.ui.views.DotsIndicator
import com.greencopper.interfacekit.ui.views.LeapButton
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.DefaultButtonsNavigationControlsHandler
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.NavigationControlsHandler
import com.greencopper.interfacekit.viewModel

internal class InterestsFragment : ParameterizedFragment<InterestsLayoutData>, OnboardingPageLayout {

    constructor(data: InterestsLayoutData) : super(data)

    @Deprecated("For system purpose only. Don't use it")
    constructor() : super(null)

    override val binding: ComposeViewBinding by viewBinding(ComposeViewBinding::inflate)
    override val onboardingPageId: String
        get() = data.onboardingPageLayoutData?.pageId ?: ""
    override val onboardingScreenViewEvent: ScreenViewEvent by lazy { screenViewEvent }

    private val viewModel: InterestsViewModel by viewModel { listOf(InterestsState(), data) }
    private val screenViewEvent by lazy { ScreenViewEvent(Screen.interestsPicker(data.analytics.screenName)) }
    override val navigationBarColor: Int by lazy { InterfaceKitColor.mainActionCardOnboardingPage.card.background }
    override val screenColor: ScreenColor get() = InterfaceKitColor.mainActionCardOnboardingPage

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setupView(data)

        binding.composeView.setContent {
            viewModel.viewBuilder.buildContent {
                val viewState by viewModel.viewState.collectAsStateWithLifecycle(null)
                val onboardingViewState: OnboardingSequenceViewData? by remember { mutableStateOf(
                    onboardingPageDelegate?.onboardingController?.onboardingSequence?.toViewData(onboardingPageId)
                ) }
                viewState?.let {
                    InterestsPicker(
                        it,
                        this::onInterestClick,
                        this::onConfirmClick,
                        onboardingViewState
                    )
                }
            }
        }
    }

    override fun restoreData(encodedData: String): InterestsLayoutData = KiboSerializable.decodeFromString(encodedData)

    override fun createNavigationControlsHandler(): NavigationControlsHandler =
        DefaultButtonsNavigationControlsHandler(
            this,
            binding.navigateBackButton,
            binding.navigateCloseButton,
            screenColor.topBar,
        )

    private fun onInterestClick(id: String, isSelected: Boolean) {
        viewModel.onInterestClick(id, isSelected)
    }

    private fun onConfirmClick() {
        viewModel.onInterestsClosed()

        onboardingPageDelegate?.pageDidComplete(onboardingPageId, true) ?: run {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InterestsPicker(
    state: InterestsState,
    onInterestClick: (String, Boolean) -> Unit,
    onConfirmClick: () -> Unit,
    onboardingSequenceViewData: OnboardingSequenceViewData?,
) {
    val colors = InterfaceKitColor.interestsPicker
    val textStyles = InterfaceKitTextStyle.interestsPicker
    Column(
        modifier = Modifier.background(colors.backgroundComposable)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(24.dp, 64.dp, 24.dp, 32.dp)
                .weight(1f)
        ) {
            Text(
                text = state.title,
                color = colors.title,
                style = textStyles.title,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            if (state.subtitle != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = AnnotatedString.fromHtml(state.subtitle),
                    color = colors.subtitle,
                    style = textStyles.subtitle,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                state.interests.forEach {
                    Interest(it, onInterestClick)
                }
            }
        }

        val cardShape = if (onboardingSequenceViewData == null) {
            RoundedCornerShape(0.dp)
        } else {
            RoundedCornerShape(10.dp, 10.dp, 0.dp, 0.dp)
        }
        val elevation = if (onboardingSequenceViewData == null) 0.dp else 4.dp

        Card(
            elevation = CardDefaults.cardElevation(elevation),
            colors = CardDefaults.cardColors(containerColor = InterfaceKitColor.interestsPicker.backgroundComposable),
            shape = cardShape,
        ) {
            Column {
                onboardingSequenceViewData
                    ?.takeIf { it.numberOfPages > 1 }
                    ?.let {
                        DotsIndicator(
                            numberOfDots = it.numberOfPages,
                            selectedPosition = it.selectedPage,
                            selectedDotColor = colors.dots.selected,
                            defaultDotColor = colors.dots.normal,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp, 32.dp, 24.dp, 16.dp)
                        )
                    }

                LeapButton(
                    onClick = onConfirmClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.confirmButton.background,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                ) {
                    Text(
                        text = state.buttonTitle,
                        color = colors.confirmButton.text,
                        style = textStyles.confirmButton,
                    )
                }
            }
        }
    }
}

@Composable
private fun Interest(
    state: InterestState,
    onInterestClick: (String, Boolean) -> Unit,
) {
    val colors = InterfaceKitColor.interestsPicker.item
    val textStyles = InterfaceKitTextStyle.interestsPicker.item

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor =
            if (state.selected) colors.background.selected else colors.background.normal),
        border = BorderStroke(1.dp,
            if (state.selected) colors.border.selected else colors.border.normal),
        onClick = { onInterestClick(state.id, state.selected) },
    ) {
        Text(
            text = state.title,
            color = if (state.selected) colors.label.selected else colors.label.normal,
            style = if (state.selected) textStyles.selected else textStyles.normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 10.dp)
                .semantics { selected = state.selected },
        )
    }
}

@Preview
@Composable
private fun InterestsViewPreview() {
    val mockedColors = mapOf(
        InterfaceKitColor.interestsPicker.getLevels("background") to Color.White,
        InterfaceKitColor.interestsPicker.getLevels("title") to Color.Black,
        InterfaceKitColor.interestsPicker.getLevels("subtitle") to Color.Black,
        InterfaceKitColor.interestsPicker.item.background.getLevels("normal") to Color(0xff7bb584),
        InterfaceKitColor.interestsPicker.item.background.getLevels("selected") to Color.Green,
        InterfaceKitColor.interestsPicker.item.label.getLevels("normal") to Color.Black,
        InterfaceKitColor.interestsPicker.item.label.getLevels("selected") to Color.Black,
        InterfaceKitColor.interestsPicker.item.border.getLevels("normal") to Color.LightGray,
        InterfaceKitColor.interestsPicker.item.border.getLevels("selected") to Color.LightGray,
        InterfaceKitColor.interestsPicker.confirmButton.getLevels("text") to Color.Black,
        InterfaceKitColor.interestsPicker.confirmButton.getLevels("background") to Color.Green,
        InterfaceKitColor.interestsPicker.dots.getLevels("selected") to Color.Black,
        InterfaceKitColor.interestsPicker.dots.getLevels("normal") to Color.LightGray,
    )

    MaterialTheme {
        MainCompositionLocalProvider(
            mockColors(mockedColors),
        ) {
            InterestsPicker(
                InterestsState(
                    title = "What Stands Out?",
                    subtitle = "Let us know which of these items you are interested in and we can quickly recommend you relevant content for your event app!",
                    buttonTitle = "Confirm",
                    interests = listOf(
                        InterestState("", "Title", false),
                        InterestState("", "Title", true),
                        InterestState("", "Much Much Longer Title", false),
                        InterestState("", "Another Tag", true),
                        InterestState("", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.", true),
                    )
                ),
                onInterestClick = { _, _ ->  },
                onConfirmClick = { },
                onboardingSequenceViewData = OnboardingSequenceViewData(3, 1),
            )
        }
    }
}
