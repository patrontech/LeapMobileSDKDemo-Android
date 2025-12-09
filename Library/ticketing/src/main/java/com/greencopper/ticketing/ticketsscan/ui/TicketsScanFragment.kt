package com.greencopper.ticketing.ticketsscan.ui

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.lifecycle.*
import androidx.recyclerview.widget.*
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.common.SafeClickListener
import com.greencopper.interfacekit.links.resolver.LinkResolver
import com.greencopper.interfacekit.navigation.layout.RedirectableLayout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import com.greencopper.interfacekit.ui.setOtaText
import com.greencopper.interfacekit.ui.viewBinding
import com.greencopper.interfacekit.ui.views.navigationcontrols.KibaToolbar
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.DefaultBackCloseToolbarNavigationControlsHandler
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.NavigationControlsHandler
import com.greencopper.interfacekit.viewModel
import com.greencopper.ticketing.R
import com.greencopper.ticketing.TicketingColor
import com.greencopper.ticketing.databinding.TicketsScanFragmentBinding
import com.greencopper.ticketing.metrics.ticketsScan
import com.greencopper.ticketing.models.Ticket
import com.greencopper.ticketing.providers.ProviderException
import com.greencopper.ticketing.textstyle.TicketingTextStyle
import com.greencopper.ticketing.ticketsscan.*
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.launch

internal class TicketsScanFragment : ParameterizedFragment<TicketsScanLayoutData>,
    RedirectableLayout {

    constructor(ticketsScanData: TicketsScanLayoutData) : super(ticketsScanData)

    @Deprecated("Only for system purpose not to be called")
    constructor() : super(null)

    override val binding: TicketsScanFragmentBinding by viewBinding(
        TicketsScanFragmentBinding::inflate
    )
    override val screenColor: ScreenColor get() = TicketingColor.ticketsScan

    private val localizationService: LocalizationService by App.lazy()
    private val routeController: RouteController by App.lazy()
    private val linkResolver: LinkResolver by App.lazy()
    private val metricService: AggregateMetricsService by App.lazy()

    private val viewModel: TicketsScanViewModel by viewModel { listOf(data.provider) }

    private lateinit var listAdapter: TicketsScanListAdapter
    private var currentTicketShownIndex: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listAdapter = TicketsScanListAdapter()
        listAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        val cache = viewModel.getCacheTickets()
        if (cache.isNotEmpty()) {
            listAdapter.setTicketItems(cache)
            setupIndicatorView()
        }
        displayEmptyState(cache.isEmpty())

        setupView()
        setupToolbar()
        disableScreenshots()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.ticketUiState.collect { uiState ->
                    when (uiState) {
                        is TicketsUiState.Success -> showTickets(uiState.tickets)
                        is TicketsUiState.Error -> showError(uiState.throwable)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        fetchAndShowTickets()
    }

    override fun onResume() {
        super.onResume()
        metricService.track(ScreenViewEvent(Screen.ticketsScan(data.analytics.screenName)))
    }

    private fun showTickets(tickets: List<Ticket>?) {
        tickets?.let {
            if (tickets.isNotEmpty()) {
                listAdapter.setTicketItems(tickets)
                setupIndicatorView()
            }

            clearAnimation()
        }
    }

    private fun showError(throwable: Throwable) {
        if (throwable is ProviderException.TokenExpiredException) {
            routeController.showAlert(
                title = localizationService.getString("ticketing.tickets_scan.session_expired.alert.title"),
                message = localizationService.getString("ticketing.tickets_scan.session_expired.alert.message"),
                positiveText = localizationService.getString("common.ok"),
                onPositiveClicked = { logout() }
            )
        } else {
            if (listAdapter.itemCount == 0) {
                routeController.showAlert(
                    message = localizationService.getString("common.an_error_occured"),
                    positiveText = localizationService.getString("common.ok")
                )
            }
        }

        clearAnimation()
    }

    private fun clearAnimation() {
        binding.refreshIcon.clearAnimation()
        displayEmptyState(listAdapter.itemCount == 0)
    }

    private fun displayEmptyState(isDisplayed: Boolean) {
        with(binding) {
            if (isDisplayed) {
                ticketsScanEmptyStateGroup.visibility = View.VISIBLE
                ticketsScanScrollableContent.visibility = View.GONE

                emptyStateHeaderTitle.setOtaText("ticketing.tickets_scan.no_tickets.title")
                emptyStateHeaderSubtitle.setOtaText("ticketing.tickets_scan.no_tickets.subtitle")
                refreshLabel.setOtaText("ticketing.tickets_scan.no_tickets.reload")

                ticketsScanRefreshLayout.setOnClickListener(fetchTicketsClickListener)
            } else {
                ticketsScanRefreshLayout.setOnClickListener(null)
                ticketsScanEmptyStateGroup.visibility = View.INVISIBLE
                ticketsScanScrollableContent.visibility = View.VISIBLE
            }
        }
    }

    private fun setupView() {
        binding.apply {
            val colors = TicketingColor.ticketsScan
            val textStyle = TicketingTextStyle.ticketsScan
            root.setBackgroundColor(colors.background)

            ticketsScanColoredBackground.setBackgroundColor(colors.header.background)

            ticketsScanTitle.setTextColor(colors.header.title)
            ticketsScanTitle.setFont(textStyle.title)
            ticketsScanTitle.setOtaText("ticketing.tickets_scan.title")

            refreshLabel.setTextColor(colors.noTickets.reload.label)
            refreshLabel.setFont(textStyle.reload)
            refreshIcon.drawable.setTint(colors.noTickets.reload.icon)

            ticketsScanEmptyStateCard.strokeColor = colors.noTickets.card.border

            emptyStateHeaderTitle.setFont(textStyle.noTickets.title)
            emptyStateHeaderSubtitle.setFont(textStyle.noTickets.subtitle)

            ticketsScanRecyclerView.let {
                if (it.layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL) {
                    (it.layoutManager as? LinearLayoutManager)?.stackFromEnd = true
                }
                PagerSnapHelper().attachToRecyclerView(it)
                it.adapter = listAdapter
                it.setOnScrollChangeListener { _, _, _, _, _ ->

                    it.findChildViewUnder((it.x + it.width) / 2, 0f)?.let { activeChild ->
                        val activePosition = it.getChildAdapterPosition(activeChild)
                        if (activePosition != currentTicketShownIndex) {
                            currentTicketShownIndex = activePosition
                            setupIndicatorView()
                        }
                    }
                }
            }
        }
    }

    private fun setupToolbar() {
        with(binding.ticketsScanToolbar) {
            isVisible = true
            insertMenuOption(
                title = localizationService.getString("common.logout"),
                icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_logout),
                shouldColor = true,
                side = KibaToolbar.Side.RIGHT,
                index = 0,
            ) {
                routeController.showAlert(
                    title = localizationService.getString("ticketing.tickets_scan.logout.alert.title"),
                    message = localizationService.getString("ticketing.tickets_scan.logout.alert.message"),
                    positiveText = localizationService.getString("common.logout"),
                    negativeText = localizationService.getString("common.cancel"),
                    onPositiveClicked = {
                        logout()
                    }
                )
            }
        }
    }

    private fun setupIndicatorView() {
        if (listAdapter.itemCount > 1) {
            binding.ticketsScanDotsIndicatorView.apply {
                visibility = View.VISIBLE
                setup(
                    numberOfDots = listAdapter.itemCount,
                    currentPosition = currentTicketShownIndex,
                    selectedDotColor = TicketingColor.ticketsScan.tickets.pageIndicator.selected,
                    defaultDotColor = TicketingColor.ticketsScan.tickets.pageIndicator.normal
                )
            }
        } else {
            binding.ticketsScanDotsIndicatorView.visibility = View.GONE
        }
    }

    private val fetchTicketsClickListener = SafeClickListener {
        fetchAndShowTickets()
    }

    private fun fetchAndShowTickets() {
        binding.refreshIcon.apply {
            val rotateAnimation =
                AnimationUtils.loadAnimation(context, R.anim.rotate_refresh).apply {
                    repeatCount = Animation.INFINITE
                }
            startAnimation(rotateAnimation)
        }
        binding.emptyStateHeaderTitle.setOtaText("ticketing.tickets_scan.loading.title")
        binding.emptyStateHeaderSubtitle.setOtaText("ticketing.tickets_scan.loading.subtitle")
        binding.refreshLabel.setOtaText("ticketing.tickets_scan.loading.reload")

        viewModel.fetchTickets()
    }

    private fun logout() {
        lifecycleScope.launch {
            viewModel.logout()
            linkResolver.featureInfo(data.featureLink)?.let {
                routeController.replace(this@TicketsScanFragment, it)
            }
        }
    }

    override fun createNavigationControlsHandler(): NavigationControlsHandler =
        DefaultBackCloseToolbarNavigationControlsHandler(
            this,
            binding.ticketsScanToolbar,
            TicketingColor.ticketsScan.topBar,
            TicketingTextStyle.ticketsScan.topBar,
        )

    override val redirectionHash: RedirectionHash
        get() = data.redirectionHash

    override fun restoreData(encodedData: String): TicketsScanLayoutData =
        KiboSerializable.decodeFromString(encodedData)

    override fun onDestroyView() {
        super.onDestroyView()
        enableScreenshots()
    }

    private fun enableScreenshots() {
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    private fun disableScreenshots() {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}
