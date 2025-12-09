package com.greencopper.interfacekit.inbox.ui

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.services.track
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.common.interfaceKit
import com.greencopper.interfacekit.databinding.FragmentInboxBinding
import com.greencopper.interfacekit.empty.EmptyState
import com.greencopper.interfacekit.inbox.InboxItemTap
import com.greencopper.interfacekit.inbox.InboxLayoutData
import com.greencopper.interfacekit.inbox.InboxViewModel
import com.greencopper.interfacekit.inbox.Notifications
import com.greencopper.interfacekit.inbox.localstorage.inbox
import com.greencopper.interfacekit.metrics.inbox
import com.greencopper.interfacekit.navigation.layout.RedirectableLayout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.ui.StickyHeaderDecoration
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import com.greencopper.interfacekit.ui.fragment.launchRepeatingJob
import com.greencopper.interfacekit.ui.viewBinding
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.DefaultBackCloseToolbarNavigationControlsHandler
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.NavigationControlsHandler
import com.greencopper.interfacekit.viewModel
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import com.greencopper.toolkit.logging.e
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime

internal class InboxFragment : ParameterizedFragment<InboxLayoutData>,
    RedirectableLayout {

    constructor(inboxLayoutData: InboxLayoutData) : super(inboxLayoutData)

    @Deprecated("For system purpose only. Don't use it")
    constructor() : super(null)

    override val redirectionHash: RedirectionHash
        get() = data.redirectionHash

    override val binding: FragmentInboxBinding by viewBinding(FragmentInboxBinding::inflate)
    override val screenColor: ScreenColor get() = InterfaceKitColor.inbox

    private val colors: InterfaceKitColor.Inbox
        get() = InterfaceKitColor.inbox

    private val viewModel: InboxViewModel by viewModel()

    private val routeController: RouteController by App.lazy()

    private lateinit var inboxAdapter: InboxAdapter

    private val timezone: ZoneId
        get() = data.timezone?.let { ZoneId.of(it) } ?: viewModel.timezoneProvider.zoneId

    override fun createNavigationControlsHandler(): NavigationControlsHandler =
        DefaultBackCloseToolbarNavigationControlsHandler(
            this,
            binding.inboxToolbar,
            colors.topBar,
            InterfaceKitTextStyle.inbox.topBar,
            viewModel.localizationService.getString(data.topBar?.title),
        )

    private val onItemClicked: ((String) -> Unit) = { id: String ->
        val items = viewModel.localStorage.project.interfaceKit.inbox.offlineItems.value
        val item = items.firstOrNull { it.id == id }
        item?.onTap?.let {
            trackItemTapAnalytics(item.id)
            routeController.redirectRouteLink(it.routeLink, this@InboxFragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.setBackgroundColor(colors.background)
        setupRecyclerView()
        binding.inboxProgressBar.indeterminateTintList = ColorStateList.valueOf(colors.loader)
        binding.inboxEmptyView.setup(colors.empty, InterfaceKitTextStyle.inbox.empty)
        binding.inboxEmptyView.fillIn(
            EmptyState(
                "interfaceKit.inbox.empty.title",
                "interfaceKit.inbox.empty.message",
                data.emptyStateImage,
                null,
                data.analytics.screenName,
            ),
            this,
            viewModel.conditionChecker,
        )


        subscribeToUiState()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.fetchNotifications(data.inboxApiUrl + App.locale.toLanguageTag())
        }
    }

    private fun subscribeToUiState() {
        viewLifecycleOwner.launchRepeatingJob(Lifecycle.State.STARTED) {
            viewModel.uiState.collectLatest {
                when (it) {
                    is InboxViewModel.FetchNotificationsUiState.Success -> {
                        binding.inboxProgressBar.isVisible = false
                        val items = viewModel.notifications(timezone)
                        if (items.isEmpty()) {
                            showEmptyView()
                            binding.inboxRecyclerView.isVisible = false
                        } else {
                            hideEmptyView()
                            binding.inboxRecyclerView.isVisible = true
                            inboxAdapter.submitList(computeAdapterItems(items))
                        }
                    }
                    is InboxViewModel.FetchNotificationsUiState.Error -> {
                        binding.inboxProgressBar.isVisible = false
                        App.log.e(
                            message = "An error occured when fetching notifications",
                            throwable = it.throwable
                        )
                    }
                    is InboxViewModel.FetchNotificationsUiState.Loading ->
                        binding.inboxProgressBar.isVisible = true
                }
            }
        }
    }

    private fun setupRecyclerView() {
        inboxAdapter = InboxAdapter(timezone)
        inboxAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        with(binding.inboxRecyclerView) {
            adapter = inboxAdapter
            addItemDecoration(
                StickyHeaderDecoration(
                    this
                ) {
                    inboxAdapter.isHeader(it)
                }
            )

            val offlineItems = viewModel.notifications(timezone)
            if (offlineItems.isNotEmpty()) {
                inboxAdapter.submitList(computeAdapterItems(offlineItems))
                hideEmptyView()
            } else {
                showEmptyView()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        App.track(ScreenViewEvent(Screen.inbox(data.analytics.screenName)))
    }

    private fun trackItemTapAnalytics(selectedItemId: String) {
        val item =
            viewModel.notifications(timezone).values.flatten()
                .firstOrNull { it.id == selectedItemId }
        item?.onTap?.let {
            App.track(
                InboxItemTap(
                    it.analytics.itemName ?: "",
                    it.analytics.itemId
                )
            )
        }
    }

    private fun showEmptyView() {
        binding.inboxEmptyView.isVisible = true
        binding.inboxRecyclerView.isVisible = false
    }

    private fun hideEmptyView() {
        binding.inboxRecyclerView.isVisible = true
        binding.inboxEmptyView.isVisible = false
    }

    private fun computeAdapterItems(items: Map<ZonedDateTime, List<Notifications.Notification>>): List<InboxAdapter.InboxItem> {
        val result = mutableSetOf<InboxAdapter.InboxItem>()
        items.forEach { (day, notifications) ->
            result.add(InboxAdapter.HeaderItem(day))
            result.addAll(notifications.map { notif ->
                InboxAdapter.NotificationItem(
                    id = notif.id,
                    time = notif.date,
                    title = notif.title,
                    text = notif.message,
                    onItemClicked = notif.onTap?.let { this@InboxFragment.onItemClicked }
                )
            })
        }
        return result.toList()
    }

    override fun restoreData(encodedData: String): InboxLayoutData =
        KiboSerializable.decodeFromString(encodedData)
}
