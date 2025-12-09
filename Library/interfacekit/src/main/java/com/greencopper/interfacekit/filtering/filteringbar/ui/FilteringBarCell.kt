package com.greencopper.interfacekit.filtering.filteringbar.ui

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.*
import androidx.annotation.DimenRes
import androidx.core.view.isVisible
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.SelectableColor
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.databinding.FilteringBarCellIconBinding
import com.greencopper.interfacekit.databinding.FilteringBarCellLabelBinding
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.setImageFrom
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.CoroutineScope

public abstract class FilteringBarCell @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : FrameLayout(context, attrs), Checkable {

    private companion object {
        private val checkedStateSet = intArrayOf(android.R.attr.state_checked)
    }

    private var isChecked = false
    protected lateinit var buttonState: ButtonState
    private lateinit var lifecycleScope: CoroutineScope

    init {
        isDuplicateParentStateEnabled = true
    }

    protected fun setup(
        backgroundColors: SelectableColor,
        borderColors: SelectableColor,
        @DimenRes cornerRadiusRes: Int,
        buttonState: ButtonState,
        lifecycleScope: CoroutineScope,
        onClick: () -> Unit,
    ) {
        val backgroundNormal =
            createBackground(backgroundColors.normal, borderColors.normal, cornerRadiusRes)
        val backgroundSelected =
            createBackground(backgroundColors.selected, borderColors.selected, cornerRadiusRes)

        val backgroundCell = StateListDrawable()
        backgroundCell.addState(intArrayOf(android.R.attr.state_checked), backgroundSelected)
        backgroundCell.addState(intArrayOf(-android.R.attr.state_checked), backgroundNormal)

        applyBackground(backgroundCell)

        this.lifecycleScope = lifecycleScope
        this.contentDescription = buttonState.default.accessibilityLabel

        this.buttonState = buttonState

        setOnSafeClickListener(500) { onClick() }
    }

    public abstract fun applyBackground(background: StateListDrawable)

    protected abstract fun iconToUpdate(): ImageView

    override fun setChecked(checked: Boolean) {
        isChecked = checked
        if (isChecked) {
            buttonState.selected?.icon?.let {
                iconToUpdate().isVisible = true
                iconToUpdate().setImageFrom(
                    it,
                    lifecycleScope,
                    hideIfUnknown = true,
                    hideIfLoading = true,
                )
            }
        } else {
            buttonState.default.icon?.let {
                iconToUpdate().isVisible = true
                iconToUpdate().setImageFrom(
                    it,
                    lifecycleScope,
                    hideIfUnknown = true,
                    hideIfLoading = true,
                )
            }
        }
    }

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent?) {
        super.onInitializeAccessibilityEvent(event)
        event?.isChecked = isChecked()
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo?) {
        super.onInitializeAccessibilityNodeInfo(info)
        info?.isCheckable = true
        info?.isChecked = isChecked()
    }

    override fun isChecked(): Boolean = isChecked

    override fun toggle() {
        setChecked(!isChecked)
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val intArray = super.onCreateDrawableState(extraSpace + 1)
        if (isChecked()) {
            mergeDrawableStates(intArray, checkedStateSet)
        }
        return intArray
    }

    private fun createBackground(
        backgroundColor: Int,
        borderColor: Int,
        @DimenRes cornerRadiusRes: Int,
    ) = GradientDrawable().apply {
        setColor(backgroundColor)
        val strokeWidth = context.resources.getDimension(R.dimen.filtering_bar_cell_stroke_width).toInt()
        setStroke(strokeWidth, borderColor)
        cornerRadius = context.resources.getDimension(cornerRadiusRes)
    }

    public data class ButtonState(
        val default: State,
        val selected: State? = null,
    ) {
        public data class State(
            val title: String? = null,
            val icon: String? = null,
            val accessibilityLabel: String,
        )
    }

    public class Label @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null,
    ) : FilteringBarCell(context, attrs) {

        init {
            val buttonHeight = context.resources.getDimension(R.dimen.filtering_bar_cell_height).toInt()
            layoutParams = LayoutParams(WRAP_CONTENT, buttonHeight)
        }

        private val binding = FilteringBarCellLabelBinding.inflate(LayoutInflater.from(context), this)

        private val localizationService: LocalizationService by App.lazy()
        private lateinit var fonts: FilteringBarTextStyle.Button

        private fun updateFont() {
            binding.filteringBarCellTitle.setFont(
                if (isChecked) fonts.name.selected else fonts.name.normal
            )
        }

        public fun setup(
            fonts: FilteringBarTextStyle.Button,
            backgroundColors: SelectableColor,
            borderColors: SelectableColor,
            titleColors: SelectableColor,
            @DimenRes cornerRadiusRes: Int,
            buttonState: ButtonState,
            lifecycleScope: CoroutineScope,
            onClick: () -> Unit,
        ) {
            super.setup(
                backgroundColors,
                borderColors,
                cornerRadiusRes,
                buttonState,
                lifecycleScope,
                onClick,
            )

            this.fonts = fonts
            binding.filteringBarCellTitle.setTextColor(titleColors.toColorStateList())

            binding.filteringBarCellEndIcon.imageTintList = titleColors.toColorStateList()
            binding.filteringBarCellStartIcon.imageTintList = titleColors.toColorStateList()

            updateFont()
        }

        public fun setArrowVisibility(isVisible: Boolean) {
            binding.filteringBarCellEndIcon.isVisible = isVisible
        }

        override fun iconToUpdate(): ImageView = binding.filteringBarCellStartIcon

        override fun setChecked(checked: Boolean) {
            super.setChecked(checked)

            binding.filteringBarCellTitle.text = localizationService.getString(
                if (isChecked) {
                    buttonState.selected?.title ?: buttonState.default.title
                } else {
                    buttonState.default.title
                }
            )
            updateFont()
            refreshDrawableState()
        }

        override fun applyBackground(background: StateListDrawable) {
            binding.background.background = background
        }
    }

    public class Icon @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null,
    ) : FilteringBarCell(context, attrs) {

        private val binding = FilteringBarCellIconBinding.inflate(LayoutInflater.from(context), this)
        override fun iconToUpdate(): ImageView = binding.filteringBarCellIcon

        public fun setup(
            backgroundColors: SelectableColor,
            borderColors: SelectableColor,
            titleColors: SelectableColor,
            buttonState: ButtonState,
            lifecycleScope: CoroutineScope,
            onClick: () -> Unit,
        ) {
            super.setup(
                backgroundColors,
                borderColors,
                R.dimen.card_corner_radius,
                buttonState,
                lifecycleScope,
                onClick,
            )

            binding.filteringBarCellIcon.imageTintList = titleColors.toColorStateList()
        }

        override fun applyBackground(background: StateListDrawable) {
            binding.filteringBarCellIcon.background = background
        }

        override fun setChecked(checked: Boolean) {
            super.setChecked(checked)
            refreshDrawableState()
        }
    }
}
