package com.greencopper.interfacekit.search.ui

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.*
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.services.track
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.databinding.SearchFragmentBinding
import com.greencopper.interfacekit.empty.EmptyState
import com.greencopper.interfacekit.metrics.search
import com.greencopper.interfacekit.navigation.layout.RedirectableLayout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.search.SearchLayoutData
import com.greencopper.interfacekit.search.logic.SearchEntry
import com.greencopper.interfacekit.search.viewmodel.SearchViewModel
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.*
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import com.greencopper.interfacekit.viewModel
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

internal class SearchFragment : ParameterizedFragment<SearchLayoutData>,
    RedirectableLayout {

    constructor(searchLayoutData: SearchLayoutData) : super(searchLayoutData)

    @Deprecated("For system purpose only. Don't use it")
    constructor() : super(null)

    private val routeController: RouteController by App.lazy()
    private val localizationService: LocalizationService by App.lazy()

    override val binding: SearchFragmentBinding by viewBinding(SearchFragmentBinding::inflate)
    override val screenColor: ScreenColor get() = InterfaceKitColor.search

    private val viewModel: SearchViewModel by viewModel {
        listOf(data.entries)
    }
    private lateinit var listAdapter: SearchAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLayout()

        listAdapter = SearchAdapter(
            data.displayImages,
            onTapEvent(),
            viewLifecycleOwner.lifecycleScope,
        )

        with(binding.searchListRecycler) {
            adapter = listAdapter
            addItemDecoration(
                BottomDrawableItemDecorator(
                    SimpleLineDecorator(
                        tintColor = InterfaceKitColor.search.separator,
                        showLast = true,
                        drawableHorizontalPaddingDp = 20,
                    )))
        }

        collectSearchedEntries()

        binding.inputLayoutSearch.setStartIconOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }

        with(binding.inputTextSearch) {
            post {
                requestFocus()
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }

            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    closeKeyboard()
                }
                true
            }

            doAfterTextChanged {
                viewModel.queryPattern.value = it.toString()
            }
        }

    }

    private fun onTapEvent(): (SearchEntry.ViewData) -> Unit = { entry ->
        context?.let {
            routeController.resolveRouteLink(entry.routeLink, this)
        }
    }

    private fun collectSearchedEntries() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getSearchedEntries().collectLatest {
                    withContext(Dispatchers.Main) {
                        listAdapter.submitList(it) {
                            binding.searchListRecycler.post {
                                binding.searchListRecycler.scrollToPosition(0)
                            }
                        }
                        binding.searchListRecycler.isVisible = it.isNotEmpty()
                        binding.searchEmpty.isVisible = it.isEmpty()
                    }
                }
            }
        }
    }

    private fun setupLayout() {
        val colors = InterfaceKitColor.search
        binding.root.setBackgroundColor(colors.background)
        binding.headerBackground.setBackgroundColor(colors.header.background)
        binding.separator.setBackgroundColor(colors.header.separator)

        with(binding.inputLayoutSearch) {
            setEndIconTintList(ColorStateList.valueOf(colors.header.searchField.button))
            setStartIconTintList(ColorStateList.valueOf(colors.header.searchField.button))
        }

        with(binding.inputTextSearch) {
            setFont(InterfaceKitTextStyle.search.header.searchField.text)
            backgroundTintList = ColorStateList.valueOf(colors.header.searchField.background)
            setTextColor(ColorStateList.valueOf(colors.header.searchField.text))
            hint = localizationService.getString("interfaceKit.search.search_text_field.placeholder")
            setHintTextColor(colors.header.searchField.placeHolder)
        }

        with(binding.searchEmpty) {
            setup(
                colors.empty,
                InterfaceKitTextStyle.search.empty,
            )
            fillIn(
                emptyState = EmptyState(
                    localizationService.getString("interfaceKit.search.empty.title"),
                    localizationService.getString("interfaceKit.search.empty.subtitle"),
                    data.emptySearchImage,
                    null,
                    data.analytics.screenName,
                ),
                origin = this@SearchFragment,
                conditionChecker = viewModel.conditionChecker,
            )
        }
    }

    override fun onResume() {
        super.onResume()
        App.track(ScreenViewEvent(Screen.search(data.analytics.screenName)))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        closeKeyboard()
    }

    override val redirectionHash: RedirectionHash
        get() = data.redirectionHash

    override fun restoreData(encodedData: String): SearchLayoutData =
        KiboSerializable.decodeFromString(encodedData)

    private fun closeKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }
}

