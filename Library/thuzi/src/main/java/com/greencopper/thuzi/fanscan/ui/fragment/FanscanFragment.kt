package com.greencopper.thuzi.fanscan.ui.fragment

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.animation.AccelerateInterpolator
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.budiyev.android.codescanner.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.metrics.labels.MappedMetrics
import com.greencopper.core.metrics.provider.MappedProvider
import com.greencopper.core.services.track
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.navigation.layout.RedirectableLayout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.*
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.DefaultButtonsNavigationControlsHandler
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.NavigationControlsHandler
import com.greencopper.interfacekit.viewModel
import com.greencopper.thuzi.R
import com.greencopper.thuzi.style.ThuziColor.fanscan
import com.greencopper.thuzi.databinding.FanscanFragmentBinding
import com.greencopper.thuzi.fanscan.*
import com.greencopper.thuzi.style.ThuziTextStyle
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.logging.d
import com.greencopper.toolkit.logging.e
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

internal class FanscanFragment : ParameterizedFragment<FanscanLayoutData>, RedirectableLayout {

    constructor(fanscanData: FanscanLayoutData) : super(fanscanData)

    @Deprecated("Only for system purpose not to be called")
    constructor() : super(null)

    override val binding: FanscanFragmentBinding by viewBinding(FanscanFragmentBinding::inflate)
    private val viewModel: FanscanViewModel by viewModel()

    private val localizationService: LocalizationService by lazy { App.resolve() }
    private val routeController: RouteController by lazy { App.resolve() }

    private var successBottomSheet: BottomSheetBehavior<LinearLayout>? = null
    private var successBottomSheetBehaviour = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, startCamera: Int) {
            if (startCamera == BottomSheetBehavior.STATE_COLLAPSED) {
                startCamera()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    private var alreadyAskedPermmission: Boolean = false

    override val screenColor: ScreenColor get() = fanscan

    override fun createNavigationControlsHandler(): NavigationControlsHandler =
        DefaultButtonsNavigationControlsHandler(
            this,
            binding.navigateBackButton,
            binding.navigateCloseButton,
            fanscan.topBar
        )

    private lateinit var codeScanner: CodeScanner

    override val redirectionHash: RedirectionHash
        get() = data.redirectionHash

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.viewSwitcherFanscan.setBackgroundColor(fanscan.permissions.background)

        with(binding.cameraViewSwitcher) {
            val errorSquareParams =
                errorSquare.layoutParams as (ConstraintLayout.LayoutParams)
            errorSquareParams.height = errorSquareWidth
            errorSquareParams.width = errorSquareWidth
            errorSquare.layoutParams = errorSquareParams

            val font = ThuziTextStyle.fanscan.scanner

            title.setFont(font.title)
            title.setOtaText("thuzi.fanscan.scanner.title")

            instructions.setOtaText("thuzi.fanscan.scanner.instructions")
            instructions.setFont(font.instructions)
            errorTv.setOtaText("thuzi.fanscan.scanner.unsupported")

            setupCodeScanner(view.context, scannerView)
        }

        with(binding.permissionOverlayViewSwitcher) {
            val color = fanscan.permissions
            val font = ThuziTextStyle.fanscan.permissions
            with(ctaBtn) {
                setOnSafeClickListener {
                    openSettings()
                    App.track(CallToActionClick())
                }
                setBackgroundColor(color.button.background)
                setTextColor(color.button.text)
                setFont(font.button)
                setOtaText("thuzi.fanscan.permissions.button")
            }

            with(featureTitleTv) {
                setTextColor(color.title)
                setFont(font.title)
                setOtaText("thuzi.fanscan.permissions.title")
            }

            iconIv.setColorFilter(color.icon)
            permissionOverlayRoot.setBackgroundColor(color.background)

            with(majorDescriptionTv) {
                setTextColor(color.subtitle)
                setFont(font.subtitle)
                setOtaText("thuzi.fanscan.permissions.subtitle")
            }

            with(minorDescriptionTv) {
                setTextColor(color.description)
                setFont(font.description)
                setOtaText("thuzi.fanscan.permissions.description")
            }
        }

        with(binding.scanSuccess) {
            val backgroundDrawable =
                ContextCompat.getDrawable(requireContext(), R.drawable.dialog_background_rounded)
            val color = fanscan.success
            val font = ThuziTextStyle.fanscan.success

            with(successBtn) {
                setOnSafeClickListener {
                    data.successPage?.redirectionUrl?.let {
                        viewModel.handleRedirectionUrl(it, this@FanscanFragment)
                    }
                    successBottomSheet?.state = BottomSheetBehavior.STATE_COLLAPSED
                }
                setBackgroundColor(color.button.background)
                setTextColor(color.button.text)
                setFont(font.button)
                setOtaText(data.successPage?.redirectionTitle ?: "common.ok")
            }

            with(titleTv) {
                setTextColor(color.title)
                setFont(font.title)
                setOtaText("thuzi.fanscan.success.title")
            }
            with(descriptionTv) {
                setTextColor(color.subtitle)
                setFont(font.subtitle)
                setOtaText("thuzi.fanscan.success.subtitle")
            }
            scannerIconIv.setColorFilter(color.icon)
            backgroundDrawable?.setTint(color.background)
            scanSuccess.background = backgroundDrawable

            successBottomSheet = BottomSheetBehavior.from(scanSuccess).apply {
                state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }

        with(binding.continueScanButton) {
            setBackgroundColor(fanscan.permissions.button.background)
            setTextColor(fanscan.permissions.button.text)
            setOnSafeClickListener {
                startCamera()
                visibility = View.GONE
            }
            text = App.resolve<LocalizationService>()
                .getString("thuzi.fanscan.scanner.restart_scanning")
            setFont(ThuziTextStyle.fanscan.permissions.button)
            (layoutParams as? ConstraintLayout.LayoutParams)
                ?.verticalBias = alignContinueButton()
        }
    }

    private fun setupCodeScanner(context: Context, codeScannerView: CodeScannerView) {
        codeScanner = CodeScanner(context, codeScannerView).apply {
            val callback = App.resolve<KibaDecodeCallback>()
            callback.setAction {
                setContinueScanButtonVisibility(View.GONE)
                checkInModuleId(it.text)
            }
            decodeCallback = DecodeCallback {
                setContinueScanButtonVisibility(View.VISIBLE)
                callback.onDecoded(it)
            }
            errorCallback = ErrorCallback { exception ->
                App.log.e("Exception occurred setting up CodeScanner", throwable = exception)
                routeController.showAlert(
                    message = localizationService.getString("common.an_error_occured"),
                    positiveText = localizationService.getString("common.ok")
                )
            }
        }
    }

    private fun setContinueScanButtonVisibility(visibility: Int) =
        lifecycleScope.launch {
            binding.continueScanButton.visibility = visibility
        }

    private fun checkInModuleId(moduleId: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            onCheckInStart()
            try {
                viewModel.checkIn(moduleId, data.checkinUrl)
                onCheckInSuccess()
            } catch (e: Exception) {
                onCheckInFailure(e)
            }
        }
    }

    private suspend fun onCheckInStart() {
        withContext(Dispatchers.Main) {
            binding.cameraViewSwitcher.progressIndicator.visibility = View.VISIBLE
        }
    }

    private suspend fun onCheckInFailure(throwable: Throwable) {
        App.log.d("Failure to checkIn on HTTP call: ${throwable.message}")

        onCheckInFailure()
        viewModel.checkTokenExpiration(throwable)
    }

    private suspend fun onCheckInFailure() {
        withContext(Dispatchers.Main) {
            showError()
            binding.cameraViewSwitcher.progressIndicator.visibility = View.GONE
            delay(2000)
            hideError(duration = 1000)
            delay(1000)
            if (!codeScanner.isPreviewActive) codeScanner.startPreview()
        }
    }

    private suspend fun onCheckInSuccess() {
        App.log.d("Successfully updated performed checkIn")

        withContext(Dispatchers.Main) {
            successBottomSheet?.state = BottomSheetBehavior.STATE_EXPANDED
            App.track(ScreenViewEvent(Screen.success))
            binding.cameraViewSwitcher.progressIndicator.visibility = View.GONE
        }
    }

    private fun showError() {
        binding.cameraViewSwitcher.errorTv.visibility = View.VISIBLE
        binding.cameraViewSwitcher.errorTv.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
        binding.cameraViewSwitcher.errorSquare.visibility = View.VISIBLE
    }

    private fun hideError(duration: Long) {
        binding.cameraViewSwitcher.errorTv.fadeOut(duration)
        binding.cameraViewSwitcher.errorSquare.fadeOut(duration)
    }

    private fun View.fadeOut(duration: Long) {
        this.animate().alpha(0f).setDuration(duration).setInterpolator(AccelerateInterpolator())
            .withEndAction {
                this.alpha = 1f
                this.visibility = View.GONE
            }
    }

    override fun onResume() {
        super.onResume()
        startCameraPreviewIfPossible()
        successBottomSheet?.addBottomSheetCallback(successBottomSheetBehaviour)
    }

    override fun onPause() {
        successBottomSheet?.removeBottomSheetCallback(successBottomSheetBehaviour)
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun openSettings() {
        activity?.let {
            val intent = Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            }
            intent.data = Uri.fromParts("package", it.packageName, null)
            it.startActivity(intent)
        }
    }

    private fun startCameraPreviewIfPossible() {
        // Check if the camera can run, and start the preview
        lifecycleScope.launch {

            val startCamera = if (alreadyAskedPermmission) {
                viewModel.hasCameraPermission() && !codeScanner.isPreviewActive
            } else {
                alreadyAskedPermmission = true
                viewModel.requestCameraPermission(activity).first() && !codeScanner.isPreviewActive
            }

            if (startCamera) {
                codeScanner.startPreview()
                switchToCameraView()
            } else {
                switchToPermissionView()
            }
        }

    }

    private fun switchToCameraView() {
        App.track(ScreenViewEvent(Screen.fanscan))
        if (binding.viewSwitcherFanscan.currentView != binding.cameraViewSwitcher.cameraFanscanRoot) {
            binding.viewSwitcherFanscan.showPrevious()
        }
    }

    private fun switchToPermissionView() {
        App.track(ScreenViewEvent(Screen.permission))
        if (binding.viewSwitcherFanscan.currentView != binding.permissionOverlayViewSwitcher.permissionOverlayRoot) {
            binding.viewSwitcherFanscan.showNext()
        }
    }

    private val errorSquareWidth: Int by lazy { (Resources.getSystem().displayMetrics.widthPixels.toFloat() * (binding.cameraViewSwitcher.scannerView.frameSize - 0.15f)).toInt() }

    private fun displayMetrics() =
        Resources.getSystem().displayMetrics

    // Viewfinder is a square in the middle of the screen with side 75% of screen width
    private fun heightOfViewFinder() =
        displayMetrics().widthPixels * 0.75f

    // This should be 1/4 of free space in px without view finder
    private fun quarterOfFreeSpace(): Float {
        val screenMinusViewFinder = displayMetrics().heightPixels - heightOfViewFinder()
        return screenMinusViewFinder / 4f
    }

    private fun alignInstructions(): Float {
        // This should be the middle of the top region above the viewfinder.
        val percentageOfTotalScreen =
            quarterOfFreeSpace() / displayMetrics().heightPixels
        // minus 2 percent to account for the size of 2 lines of textview
        return percentageOfTotalScreen - 0.02f
    }

    private fun alignContinueButton(): Float {
        // Button height is 48dp, the half of height in px
        val halfOfButtonHeight = 48.dpToPx() / 2
        // Should be the middle of the free space below the viewfinder
        val scanButtonPositionInPixels = quarterOfFreeSpace() * 3 +
                heightOfViewFinder() + halfOfButtonHeight
        return scanButtonPositionInPixels / displayMetrics().heightPixels
    }

    internal fun startCamera() {
        binding.cameraViewSwitcher.scannerView.invalidate()
        if (!codeScanner.isPreviewActive) {
            codeScanner.startPreview()
        }
    }

    private val Screen.Companion.fanscan: Screen
        get() = Screen(data.analytics.screenName, "thuzi_fan_scan")

    private val Screen.Companion.permission: Screen
        get() = Screen(data.analytics.screenName + " Permissions", "thuzi_fan_scan_permission")

    private val Screen.Companion.success: Screen
        get() = Screen(data.analytics.screenName + " Success", "thuzi_fan_scan_checkin")

    private class CallToActionClick : MappedMetrics {
        override fun track(provider: MappedProvider) {
            val eventName = EventName("fan_scan/os_settings_click")
            provider.track(eventName, emptyMap())
        }
    }

    override fun restoreData(encodedData: String): FanscanLayoutData =
        KiboSerializable.decodeFromString(encodedData)
}
