package com.greencopper.thuzi.logout.ui

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.databinding.ComposeViewBinding
import com.greencopper.interfacekit.ui.compose.MainCompositionLocalProvider
import com.greencopper.interfacekit.ui.compose.mockColors
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import com.greencopper.interfacekit.ui.viewBinding
import com.greencopper.interfacekit.ui.views.LeapButton
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.DefaultButtonsNavigationControlsHandler
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.NavigationControlsHandler
import com.greencopper.interfacekit.viewModel
import com.greencopper.thuzi.style.ThuziColor
import com.greencopper.thuzi.logout.LogoutLayoutData
import com.greencopper.thuzi.logout.LogoutState
import com.greencopper.thuzi.logout.LogoutViewModel
import com.greencopper.thuzi.style.ThuziTextStyle
import kotlinx.coroutines.launch

internal class LogoutFragment : ParameterizedFragment<LogoutLayoutData> {

    constructor(data: LogoutLayoutData) : super(data)

    @Deprecated("Only for system purpose not to be called")
    constructor() : super(null)

    override val binding: ComposeViewBinding by viewBinding(ComposeViewBinding::inflate)
    override val screenColor: ScreenColor get() = ThuziColor.logout

    private val viewModel: LogoutViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeView.setContent {
            viewModel.viewBuilder.buildContent {
                val viewState by viewModel.store.state.collectAsStateWithLifecycle(null)
                viewState?.let {
                    LogoutScreen(it) {
                        viewLifecycleOwner.lifecycleScope.launch { viewModel.logout() }
                    }
                }
            }
        }

        viewModel.setupView(data)
    }

    override fun createNavigationControlsHandler(): NavigationControlsHandler =
        DefaultButtonsNavigationControlsHandler(
            this,
            binding.navigateBackButton,
            binding.navigateCloseButton,
            screenColor.topBar,
        )

    override fun restoreData(encodedData: String): LogoutLayoutData =
        KiboSerializable.decodeFromString(encodedData)
}

@Composable
private fun LogoutScreen(
    state: LogoutState,
    onLogoutClick: () -> Unit,
) {
    val colors = ThuziColor.logout
    val textStyles = ThuziTextStyle.logout

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .background(colors.backgroundComposable)
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = state.title,
            style = textStyles.title,
            color = colors.title,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.size(24.dp))

        Text(
            text = state.subtitle,
            style = textStyles.subtitle,
            color = colors.subtitle,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.size(32.dp))

        LeapButton(
            onClick = onLogoutClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.button.background,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = state.buttonText,
                color = colors.button.text,
                style = textStyles.button,
            )
        }
    }
}

@Preview
@Composable
private fun LogoutPreview() {
    val mockedColors = mapOf(
        ThuziColor.logout.getLevels("title") to Color.Black,
        ThuziColor.logout.getLevels("subtitle") to Color.Black,
        ThuziColor.logout.button.getLevels("background") to Color.Blue,
        ThuziColor.logout.button.getLevels("text") to Color.White,
    )
    MaterialTheme {
        MainCompositionLocalProvider(mockColors(mockedColors)) {
            LogoutScreen(
                LogoutState(
                    title = "Confirm Log Out",
                    subtitle = "Are you sure you want to log out of your current account?",
                    buttonText = "Log Out",
                ),
                onLogoutClick = {  },
            )
        }
    }
}
