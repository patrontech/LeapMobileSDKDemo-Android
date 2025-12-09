package com.greencopper.interfacekit.ui.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.bottomsheet.*
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.databinding.BottomSheetDialogFragmentContainerBinding
import com.greencopper.interfacekit.navigation.NavigationController
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.ui.viewBinding

public class BottomSheetDialogFragmentContainer : BottomSheetDialogFragment(),
    NavigationController<BottomSheetDialogFragmentContainer> {

    internal companion object {
        internal const val EXTRA_BACKGROUND_COLOR = "EXTRA_BACKGROUND_COLOR"
    }

    private val binding: BottomSheetDialogFragmentContainerBinding by viewBinding(
        BottomSheetDialogFragmentContainerBinding::inflate
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getInt(EXTRA_BACKGROUND_COLOR)?.let { color ->
            binding.root.background = ResourcesCompat.getDrawable(resources, R.drawable.bottom_sheet_rounded_background, null)
            binding.root.backgroundTintList = ColorStateList.valueOf(color)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialogInterface: DialogInterface ->
            val d = dialogInterface as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            bottomSheet?.let {
                val bsb: BottomSheetBehavior<*> = BottomSheetBehavior.from(it)
                bsb.state = BottomSheetBehavior.STATE_EXPANDED
                bsb.skipCollapsed = true
            }
        }
        return dialog
    }

    @Override
    override fun present(layout: Layout, onAttach: () -> Unit) {
        super.present(layout, onAttach)
        dismiss()
    }

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialog
    }

    override fun getContainerId(): Int = R.id.bottom_sheet_fragment_holder

    public fun setSwipeIndicatorColor(@ColorInt color: Int) {
        binding.swipeIndicator.setColorFilter(color)
    }

}

/**
 * BottomSheetChildren are Layouts that should be opened as a bottom sheet when [Route.Present]'d
 */
public interface BottomSheetChild {
    public val backgroundColor: Int
}
