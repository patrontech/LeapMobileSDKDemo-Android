package com.greencopper.maps.geomap.ui

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.view.View
import androidx.activity.addCallback
import androidx.lifecycle.Lifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.databinding.BottomSheetDialogFragmentContainerBinding
import com.greencopper.interfacekit.rootview.RootLayoutHolder
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import com.greencopper.maps.colors.MapsColor

internal abstract class BottomSheetContainerFragment<T : KiboSerializable<T>>(constructorData: T?) :
    ParameterizedFragment<T>(constructorData) {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private var bottomSheetState: Int
        get() = bottomSheetBehavior.state
        set(state) {
            bottomSheetBehavior.state =
                state
        }

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            // unused
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN && isAdded) {
                onHideBottomSheet()
            }
        }
    }

    abstract fun onHideBottomSheet()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        RootLayoutHolder.onBackPressDispatcher?.addCallback(this) {
            when (bottomSheetState) {
                BottomSheetBehavior.STATE_EXPANDED -> bottomSheetState =
                    BottomSheetBehavior.STATE_COLLAPSED

                BottomSheetBehavior.STATE_HALF_EXPANDED, BottomSheetBehavior.STATE_COLLAPSED ->
                    bottomSheetState = BottomSheetBehavior.STATE_HIDDEN

                else -> {
                    isEnabled = false
                    activity?.onBackPressedDispatcher?.onBackPressed()
                }
            }
        }
    }

    protected fun setupBottomSheet(bottomSheetContainer: BottomSheetDialogFragmentContainerBinding) {
        with(bottomSheetContainer) {
            val locationDetailColors = MapsColor.locationDetail
            bottomSheetLayout.backgroundTintList = ColorStateList.valueOf(locationDetailColors.background)
            swipeIndicator.setColorFilter(locationDetailColors.header.swipeIndicator)

            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)
            bottomSheetBehavior.isHideable = true
            bottomSheetBehavior.peekHeight = (Resources.getSystem().displayMetrics.heightPixels * 0.25).toInt()
            bottomSheetBehavior.isFitToContents = false
            bottomSheetBehavior.halfExpandedRatio = 0.65f
            bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    protected fun setBottomSheetVisibility(isVisible: Boolean) {
        // calling this method quickly in succession could leave the bottom sheet in the wrong state
        // post forces subsequent calls to be called after previous calls are finished
        binding?.root?.post {
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
                bottomSheetState = if (isVisible) {
                    BottomSheetBehavior.STATE_COLLAPSED
                } else {
                    BottomSheetBehavior.STATE_HIDDEN
                }
            }
        }
    }
}
