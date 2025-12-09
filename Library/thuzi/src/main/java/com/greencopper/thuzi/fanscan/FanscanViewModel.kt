package com.greencopper.thuzi.fanscan

import android.Manifest
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.greencopper.core.content.manager.CurrentProjectTagProvider
import com.greencopper.core.deferredcommand.*
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.permissions.PermissionManager
import com.greencopper.core.permissions.RationalePanelConfig
import com.greencopper.interfacekit.links.resolver.LinkResolver
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.thuzi.ThuziAPI
import com.greencopper.thuzi.account.DeviceSessionManager
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.thuzi.account.registration.manager.ThuziRegistrationManager
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.e
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import retrofit2.HttpException
import java.net.HttpURLConnection

internal class FanscanViewModel(
    private val thuziAPI: ThuziAPI,
    private val permissionManager: PermissionManager,
    private val localizationService: LocalizationService,
    private val registrationManager: ThuziRegistrationManager,
    private val currentProjectTagProvider: CurrentProjectTagProvider,
    private val deviceSessionManager: DeviceSessionManager,
    private val localStorage: LocalStorage,
    private val linkResolver: LinkResolver,
    private val routeController: RouteController,
    private val deferredCommandService: DeferredCommandService,
    private val logging: Logging,
) : ViewModel() {

    internal suspend fun checkIn(moduleId: String, checkInUrl: String) {
        val currentProject = currentProjectTagProvider.currentProject ?: run {
            logging.e("Trying to check in with no project")
            return
        }
        val jwt = localStorage.project.thuzi.jwt.value!!
        val deviceSession = deviceSessionManager.getDeviceSession(currentProject).urn
        val caller = FanscanApiCaller(jwt, thuziAPI)
        try {
            caller.checkIn(checkInUrl, moduleId, deviceSession)
        } catch (e: HttpException) {
            // The server responded and didn't like our request.
            // Let the fragment handle it.
            throw e
        } catch (_: Exception) {
            // Any other kind of exception means that we should
            // try again later. This is almost always a connectivity
            // issue.
            val checkIn = FanscanCheckIn(checkInUrl, moduleId, currentProject)
            deferredCommandService.defer(DeferredCommandKey.fanscan, checkIn)
            // At this point, the caller believes the check-in succeeded.
            // This is by design.
        }
    }

    internal fun hasCameraPermission(): Boolean {
        return permissionManager.hasAllPermissions(Manifest.permission.CAMERA)
    }

    internal fun requestCameraPermission(activity: FragmentActivity?): Flow<Boolean> {
        return activity?.let {
            permissionManager.startPermissionsRequestFlow(
                activity = it,
                rationalePanelConfig = with(localizationService) {
                    RationalePanelConfig(
                        title = getString("thuzi.fanscan.scanner.popup.title"),
                        message = getString("thuzi.fanscan.scanner.popup.description"),
                        positiveButtonString = getString("permissions.rationale_dialog.positive_button")
                    )
                },
                settingsPanelConfig = null,
                Manifest.permission.CAMERA
            )
        } ?: flowOf(false)
    }

    internal fun checkTokenExpiration(throwable: Throwable) {
        when ((throwable as? HttpException)?.code()) {
            HttpURLConnection.HTTP_FORBIDDEN, HttpURLConnection.HTTP_UNAUTHORIZED -> {
                registrationManager.logout()
            }
        }
    }

    internal fun handleRedirectionUrl(redirectionUrl: String, origin: Layout) =
        try {
            linkResolver.route(redirectionUrl)?.let {
                routeController.resolve(it, origin)
            }
        } catch (throwable: Throwable) {
            logging.e(message = "Failed to parse fanscan success redirection url: $redirectionUrl", throwable = throwable)
        }
}
