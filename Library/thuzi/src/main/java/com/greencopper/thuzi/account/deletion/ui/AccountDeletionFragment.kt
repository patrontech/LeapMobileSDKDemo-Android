package com.greencopper.thuzi.account.deletion.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.services.track
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.navigation.layout.RedirectableLayout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.rootview.RootLayoutManager
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import com.greencopper.interfacekit.ui.setOtaText
import com.greencopper.interfacekit.ui.viewBinding
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.*
import com.greencopper.interfacekit.viewModel
import com.greencopper.thuzi.account.deletion.AccountDeletionViewModel
import com.greencopper.thuzi.account.deletion.AccountDeletionViewModel.State
import com.greencopper.thuzi.account.deletion.initializer.AccountDeletionLayoutData
import com.greencopper.thuzi.style.ThuziColor
import com.greencopper.thuzi.databinding.AccountDeletionFragmentBinding
import com.greencopper.thuzi.style.ThuziTextStyle
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

internal class AccountDeletionFragment : ParameterizedFragment<AccountDeletionLayoutData>,
    RedirectableLayout {
    constructor(accountDeletionData: AccountDeletionLayoutData) : super(accountDeletionData)

    @Deprecated("Only for system purpose not to be called")
    constructor() : super(null)

    override val screenColor: ScreenColor get() = colorStyle

    override val binding: AccountDeletionFragmentBinding by viewBinding(
        AccountDeletionFragmentBinding::inflate
    )

    private val colorStyle: ThuziColor.AccountDeletion get() = ThuziColor.accountDeletion
    private val textStyle: ThuziTextStyle.AccountDeletion get() = ThuziTextStyle.accountDeletion

    private val viewModel: AccountDeletionViewModel by viewModel()
    private val rootLayoutManager: RootLayoutManager by App.lazy()

    private val Screen.Companion.accountDeletion: Screen
        get() = Screen(data.analytics.screenName, "thuzi_account_deletion")

    override fun createNavigationControlsHandler(): NavigationControlsHandler =
        DefaultButtonsNavigationControlsHandler(
            this,
            binding.navigateBackButton,
            binding.navigateCloseButton,
            colorStyle.topBar
        )

    override val redirectionHash: RedirectionHash
        get() = data.redirectionHash

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundColor(colorStyle.background)
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            viewModel.state.collectLatest { state ->
                withContext(Dispatchers.Main) {
                    setupView(state)
                }
            }
        }
    }

    private suspend fun setupView(state: State) {
        with(binding) {
            when (state) {
                State.CONFIRM -> {
                    title.apply {
                        setTextColor(colorStyle.title)
                        setFont(textStyle.title)
                        setOtaText("thuzi.account_deletion.title")
                    }

                    subtitle.apply {
                        setTextColor(colorStyle.subtitle)
                        setFont(textStyle.subtitle)
                        setOtaText("thuzi.account_deletion.confirm.subtitle")
                    }

                    footnote.apply {
                        setTextColor(colorStyle.footnote)
                        setFont(textStyle.footnote)
                        setOtaText("thuzi.account_deletion.confirm.footnote")
                    }

                    primaryButton.apply {
                        setTextColor(colorStyle.primaryButton.text)
                        setBackgroundColor(colorStyle.primaryButton.background)
                        setFont(textStyle.primaryButton)
                        setOtaText("thuzi.account_deletion.confirm.delete_button")

                        setOnSafeClickListener {
                            viewModel.handlePrimaryButtonClick(data.apiUrl)
                        }
                    }
                }

                State.LOADING -> {
                    subtitle.isVisible = false

                    footnote.apply {
                        visibility = View.VISIBLE
                        setOtaText("thuzi.account_deletion.loading.footnote")
                    }

                    progressBar.isVisible = true

                    primaryButton.isVisible = false

                    navigateBackButton.visibility = View.GONE
                    navigateCloseButton.visibility = View.GONE
                }

                State.SUCCESS -> {
                    subtitle.apply {
                        isVisible = true
                        setOtaText("thuzi.account_deletion.success.subtitle")
                    }

                    footnote.apply {
                        isVisible = true
                        setOtaText("thuzi.account_deletion.success.footnote")
                    }

                    progressBar.isVisible = false

                    primaryButton.apply {
                        isVisible = true
                        setOtaText("thuzi.account_deletion.success.continue_button")
                    }
                }

                State.FAIL -> {
                    subtitle.apply {
                        isVisible = true
                        setOtaText("thuzi.account_deletion.failure.subtitle")
                    }

                    footnote.isVisible = false

                    progressBar.isVisible = false

                    navigateBackButton.isVisible = shouldShowBackButton()
                    navigateCloseButton.isVisible = shouldShowCloseButton()

                    primaryButton.apply {
                        isVisible = true
                        setOtaText("thuzi.account_deletion.failure.retry_button")
                    }
                }

                State.RESTART_APP -> {
                    rootLayoutManager.updateRootLayout()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        App.track(ScreenViewEvent(Screen.accountDeletion))
    }

    override fun restoreData(encodedData: String): AccountDeletionLayoutData =
        KiboSerializable.decodeFromString(encodedData)
}
