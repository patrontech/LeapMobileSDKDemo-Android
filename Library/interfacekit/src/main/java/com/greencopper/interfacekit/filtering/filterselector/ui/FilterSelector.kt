package com.greencopper.interfacekit.filtering.filterselector.ui

import android.content.Context
import android.content.res.ColorStateList
import android.view.*
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.databinding.FilterSelectorBinding
import com.greencopper.interfacekit.filtering.FilterId
import com.greencopper.interfacekit.filtering.filterselector.FilterSelectorData
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.*
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow

internal class FilterSelector(context: Context) :
    BottomSheetDialog(context, R.style.CustomBottomSheetDialog) {

    private val binding = FilterSelectorBinding.inflate(LayoutInflater.from(context))

    private val localizationService: LocalizationService by lazy { App.resolve() }

    private lateinit var dataState: StateFlow<FilterSelectorData>
    private var updateDataJob: Job? = null
    private lateinit var ids: List<FilterId>

    private val adapter = FilterSelectorAdapter()
    private var onClearTap: ((List<FilterId>) -> Unit)? = null

    init {
        setContentView(binding.root)
        (binding.root.parent as? ViewGroup)?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            private var currentState = BottomSheetBehavior.STATE_HIDDEN
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val isDraggingDown = currentState == BottomSheetBehavior.STATE_DRAGGING && slideOffset < 0
                val isSettlingDown = currentState == BottomSheetBehavior.STATE_SETTLING && slideOffset < 0

                if (!isDraggingDown && !isSettlingDown) {
                    binding.root.progress = slideOffset
                }
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                currentState = newState
            }
        })

        val colors = InterfaceKitColor.filters

        binding.dialogContainer.backgroundTintList = ColorStateList.valueOf(colors.background)

        binding.bottomPanel.apply {
            setBackgroundColor(colors.actions.background)
            setShadowColor(colors.actions.shadow)
        }
        setNavigationBarColor(this, InterfaceKitColor.filters.actions.background)

        with(binding.doneBt) {
            setFont(InterfaceKitTextStyle.filterSelector.actions.doneButton)
            text = localizationService.getString("interfaceKit.filter_selector.actions.done")
            setTextColor(colors.actions.doneButton.text)
            backgroundTintList = ColorStateList.valueOf(colors.actions.doneButton.background)
            strokeColor = ColorStateList.valueOf(colors.actions.doneButton.border)
            setOnSafeClickListener { dismiss() }
        }
        with(binding.clearAllTv) {
            setFont(InterfaceKitTextStyle.filterSelector.actions.clearButton)
            text = localizationService.getString("interfaceKit.filter_selector.actions.clear_all")
            setTextColor(colors.actions.clearButton.text)
            setOnSafeClickListener { onClearTap?.invoke(ids) }
        }
        val spacingMargin =
            context.resources.getDimension(R.dimen.filter_selector_recycler_view_item_margin_spacing).toInt()
        val horizontalMargin = context.resources.getDimension(R.dimen.horizontal_margin).toInt()
        binding.optionsRv.addItemDecoration(
            VerticalSpacingItemDecorator(
                spacing = spacingMargin,
                horizontalPadding = horizontalMargin,
            )
        )
        binding.optionsRv.itemAnimator = null
        binding.optionsRv.adapter = adapter

        binding.root.post {
            behavior.peekHeight = binding.bottomPanel.y.toInt() + binding.bottomPanel.height
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomPanel) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    fun setup(
        selectorDataState: StateFlow<FilterSelectorData>,
        ids: List<FilterId>,
    ) {
        dataState = selectorDataState
        this.ids = ids
    }

    private fun reload(data: FilterSelectorData) {
        adapter.setDataFilters(ids, data.filters)
        onClearTap = data.onClearTap
    }

    override fun onStart() {
        super.onStart()

        updateDataJob = CoroutineScope(Dispatchers.IO).launch {
            dataState.collect {
                withContext(Dispatchers.Main) {
                    reload(it)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()

        updateDataJob?.cancel()
    }
}
