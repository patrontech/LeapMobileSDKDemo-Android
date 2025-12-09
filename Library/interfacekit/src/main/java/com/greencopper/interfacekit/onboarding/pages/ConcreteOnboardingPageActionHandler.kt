package com.greencopper.interfacekit.onboarding.pages

import android.annotation.SuppressLint
import android.os.Build.VERSION_CODES.TIRAMISU
import com.greencopper.core.bluetooth.BluetoothService
import com.greencopper.core.conditions.conditionchecker.ConditionChecker
import com.greencopper.core.location.service.LocationService
import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.metrics.labels.MappedMetrics
import com.greencopper.core.metrics.provider.MappedProvider
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.core.permissions.notification.service.NotificationPermissionService
import com.greencopper.core.remotestate.RemoteStateDispatcher
import com.greencopper.core.remotestate.RemoteStateEntry
import com.greencopper.core.remotestate.dispatch
import com.greencopper.interfacekit.commands.system.CommandExecutor
import com.greencopper.interfacekit.metrics.LocationPermissionEvent
import com.greencopper.interfacekit.metrics.NotificationPermissionEvent
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.LazyResolver
import com.greencopper.toolkit.logging.e
import com.greencopper.toolkit.versionprovider.BuildConfigProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first

// Since there are so many services used here, they're all lazy so that
// only the ones that need to be used will be resolved
internal class ConcreteOnboardingPageActionHandler(
    private val routeController: LazyResolver<RouteController>,
    private val commandExecutor: LazyResolver<CommandExecutor>,
    private val conditionChecker: LazyResolver<ConditionChecker>,
    private val metricsService: LazyResolver<AggregateMetricsService>,
    private val locationService: LazyResolver<LocationService>,
    private val bluetoothService: LazyResolver<BluetoothService>,
    private val notificationPermissionService: LazyResolver<NotificationPermissionService>,
    private val buildConfigProvider: LazyResolver<BuildConfigProvider>,
    private val remoteStateDispatcher: LazyResolver<RemoteStateDispatcher>,
) : OnboardingPageActionHandler {

    @SuppressLint("InlinedApi")
    override suspend fun executeAction(action: OnboardingPageAction, origin: Layout): Boolean {
        val aggregateMetricsService: AggregateMetricsService? = if(action !is OnboardingPageAction.Execute) {
            metricsService.resolve()
        } else {
            null
        }

        return when (action) {
            is OnboardingPageAction.LocationPermission -> {
                origin.activity?.let { activity ->
                    aggregateMetricsService.trackClickEvent(action.analyticsEvent)

                    val locationService = locationService.resolve()

                    val withBackgroundLocation =
                        action.request == OnboardingPageAction.LocationPermission.ALWAYS
                    locationService.requestPermissions(activity, null, null, withBackgroundLocation)
                        .first()

                    val authStatus = locationService.getAuthorizationStatus()
                    aggregateMetricsService?.track(LocationPermissionEvent(authStatus))
                    remoteStateDispatcher.resolve().dispatch(
                        key = "location_permission",
                        value = authStatus.isAuthorized(),
                        domain = RemoteStateEntry.Domain.APP,
                        isUrgent = false,
                    )
                }
                true
            }
            is OnboardingPageAction.BluetoothPermission -> {
                origin.activity?.let { activity ->
                    aggregateMetricsService.trackClickEvent(action.analyticsEvent)
                    bluetoothService.resolve().openBluetoothSettings(activity)
                }
                true
            }
            is OnboardingPageAction.NotificationPermission -> {
                origin.activity?.let { activity ->
                    var completed = false
                    val notificationPermissionService = notificationPermissionService.resolve()

                    if (buildConfigProvider.resolve().sdkInt >= TIRAMISU) {
                        aggregateMetricsService.trackClickEvent(action.analyticsEvent)
                        notificationPermissionService.requestPermission(activity).first()
                        aggregateMetricsService?.track(NotificationPermissionEvent(notificationPermissionService.getAuthorizationStatus()))
                        completed = true
                    }

                    remoteStateDispatcher.resolve().dispatch(
                        key = "notification_permission",
                        value = notificationPermissionService.getAuthorizationStatus().isAuthorized(),
                        domain = RemoteStateEntry.Domain.APP,
                        isUrgent = false,
                    )

                    return completed
                }
                return false
            }
            is OnboardingPageAction.Complete -> {
                aggregateMetricsService.trackClickEvent(action.analyticsEvent)
                action.persistAsCompleted
            }
            is OnboardingPageAction.Present -> return try {
                aggregateMetricsService.trackClickEvent(action.analyticsEvent)
                routeController.resolve().resolve(Route.Present(action.featureInfo), origin)
                conditionChecker.resolve().checkFlow(action.completionConditions).drop(1).first()
            } catch (e: Throwable) {
                if (e is CancellationException) {
                    throw e
                }

                App.log.e(
                    message = "Error happened during onboarding action execution",
                    throwable = e
                )
                false
            }
            is OnboardingPageAction.Execute -> {
                return try {
                    commandExecutor.resolve().execute(action.commandInfo, origin).first()
                } catch (e: IllegalArgumentException) {
                    App.log.e(
                        message = "Unable to execute command",
                        throwable = e
                    )
                    false
                }
            }
        }
    }

    private fun AggregateMetricsService?.trackClickEvent(eventName: String?) = eventName?.let {
        this?.track(ActionClickEvent(it))
    }

    internal data class ActionClickEvent(private val eventName: String) : MappedMetrics {
        override fun track(provider: MappedProvider) =
            provider.track(EventName(eventName), emptyMap())
    }
}
