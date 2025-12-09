package com.greencopper.interfacekit.ui.activity

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout.LayoutParams
import androidx.activity.*
import androidx.annotation.IdRes
import androidx.compose.ui.platform.ComposeView
import androidx.core.graphics.Insets
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.*
import androidx.lifecycle.*
import com.greencopper.core.CoreAssembly
import com.greencopper.core.notification.repository.NotificationRepository
import com.greencopper.core.permissions.PermissionManager
import com.greencopper.core.remotestate.RemoteStateDispatcher
import com.greencopper.core.utils.getAppManifestPermissions
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.databinding.LoadingViewBinding
import com.greencopper.interfacekit.navigation.NavigationController
import com.greencopper.interfacekit.navigation.NavigationFragment
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.notification.NotificationTap
import com.greencopper.interfacekit.onboarding.AppOnboardingManager
import com.greencopper.interfacekit.oneTimeFragmentOnAttachListener
import com.greencopper.interfacekit.rootview.RootLayoutHolder
import com.greencopper.interfacekit.rootview.RootLayoutManager
import com.greencopper.interfacekit.ui.AppOverlay
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.versionprovider.BuildConfigProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

public abstract class KibaMainActivity : BaseActivity() {

    public companion object {
        public const val INTENT_KEY_ON_TAP: String = "onTap"
        public const val INTENT_KEY_NOTIFICATION_ID: String = "notification_id"
    }

    @IdRes
    public abstract fun getContainerId(): Int
    public abstract fun getOverlayView(): ComposeView

    @IdRes
    protected abstract fun getFragmentContainerId(): Int
    public abstract fun applyInsets(
        insets: Insets,
        statusBarColor: Int?,
        navBarColor: Int,
        shouldColorNavBar: Boolean,
    )

    protected var brandedImage: Drawable? = null
    protected val loadingViewBinding: LoadingViewBinding by lazy {
        LoadingViewBinding.inflate(layoutInflater)
    }
    protected val rootLayoutManager: RootLayoutManager by App.lazy()
    protected val viewModel: KibaMainActivityViewModel by viewModels()

    private val remoteStateDispatcher: RemoteStateDispatcher by App.lazy()
    private val onboardingManager: AppOnboardingManager by App.lazy()
    private val permissionManager: PermissionManager by App.lazy()
    private val singleThreadScope = App.resolve<CoroutineScope>(tag = CoreAssembly.singleThreadScopeTag)
    private val buildConfigProvider: BuildConfigProvider by App.lazy()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (buildConfigProvider.sdkInt > Build.VERSION_CODES.Q) {
            enableEdgeToEdge()
        }

        viewModel.deeplinkScheme = getString(R.string.deeplink_scheme)

        initRootLayout(savedInstanceState != null)
        super.onCreate(savedInstanceState)

        subscribeToRootLayoutManager()
        lifecycle.addObserver(remoteStateDispatcher)
        lifecycle.addObserver(permissionManager)
        App.resolve<NotificationRepository>()
    }

    private fun initRootLayout(restoringState: Boolean) {
        if (!restoringState) {
            App.resolve<LayoutDataProvider>().clear()
            viewModel.rootLayoutHolder.clearRootLayout()
        }
        var onAttachListener: FragmentOnAttachListener? = null
        onAttachListener = oneTimeFragmentOnAttachListener { _, fragment ->
            //only want to set root layout here if state is being restored,
            //otherwise RootLayoutManager will handle setting it.
            if (fragment is NavigationFragment && restoringState) {
                viewModel.rootLayoutHolder.setRootLayout(fragment)
                loadingViewBinding.root.visibility = View.GONE
            }
        }

        supportFragmentManager.addFragmentOnAttachListener(onAttachListener)

        onBackPressedDispatcher.addCallback {
            if (RootLayoutHolder.onBackPressDispatcher?.hasEnabledCallbacks() == true) {
                RootLayoutHolder.onBackPressDispatcher?.onBackPressed()
            } else {
                val navigationControllers =
                    findNavigationControllersInStack(supportFragmentManager)
                val navigationController = navigationControllers.firstOrNull { navCon ->
                    navCon.ncChildFragmentManager.backStackEntryCount > 1
                }
                if (navigationController != null) {
                    navigationController.ncChildFragmentManager.popBackStack()
                } else {
                    moveTaskToBack(true)
                }
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        with(loadingViewBinding) {
            addContentView(root, ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

            if (brandedImage != null) {
                brandedIcon.setImageBitmap(brandedImage?.toBitmap())
            } else {
                brandedIcon.visibility = View.GONE
            }
        }
        App.resolve<AppOverlay>().setOverlayOn(getOverlayView(), lifecycleScope)
    }

    override fun onStart() {
        super.onStart()
        viewModel.aggregateMetricsService.onActivityStart(this)
    }

    override fun onResume() {
        super.onResume()

        singleThreadScope.launch {
            viewModel.sessionManager.resume()
        }

        permissionManager.refreshPermissionsStatus(getAppManifestPermissions(buildConfigProvider, this))
        onboardingManager.checkAppOnboarding(supportFragmentManager, getFragmentContainerId())
    }

    override fun onPause() {
        super.onPause()
        singleThreadScope.launch {
            viewModel.sessionManager.pause()
        }
    }

    override fun onStop() {
        viewModel.aggregateMetricsService.onActivityStop(this)
        super.onStop()
    }

    private fun subscribeToRootLayoutManager() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                withContext(Dispatchers.IO) {
                    rootLayoutManager.setupRootLayout(
                        supportFragmentManager,
                        supportFragmentManager.fragments.isNotEmpty()
                    ).collectLatest { rootFragment ->
                        setRootLayoutIfNeeded(rootFragment)
                    }
                }
            }
        }
    }

    private suspend fun setRootLayoutIfNeeded(rootFragment: Layout) {
        if (rootFragment.id == 0 || this.supportFragmentManager.findFragmentById(rootFragment.id) == null) {
            withContext(Dispatchers.Main) {
                loadingViewBinding.root.visibility = View.GONE
                supportFragmentManager.apply {
                    addFragmentOnAttachListener(oneTimeFragmentOnAttachListener { _, _ ->
                        viewModel.uiRefreshCount.value += 1
                        viewModel.refreshUiVersion()
                    })

                    commit(true) {
                        replace(getFragmentContainerId(), rootFragment)
                        setPrimaryNavigationFragment(rootFragment)
                    }
                }
            }
        }
    }

    protected open suspend fun handleRedirectionIfAvailable(intent: Intent): Boolean {
        if (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0) {
            // If the app is relaunched from history, we should skip redirection.
            return true
        }

        if (intent.extras?.containsKey(INTENT_KEY_NOTIFICATION_ID) == true) {
            val id = intent.extras?.getString(INTENT_KEY_NOTIFICATION_ID).orEmpty()
            viewModel.aggregateMetricsService.track(NotificationTap(id))
        }

        RootLayoutHolder.rootLayoutHolder.filterNotNull().first()
        val newIntent = Intent(intent)
        this@KibaMainActivity.intent = null
        return viewModel.redirect(newIntent)
    }

    private fun findNavigationControllersInStack(fragmentManager: FragmentManager): ArrayList<NavigationController<*>> {
        return try {
            // Find the latest NavigationController in this stack
            val fragment = fragmentManager.fragments.last { fragment ->
                fragment is NavigationController<*>
            } as NavigationController<*>
            // Check if this NavigationController contains another in its own child stack
            findNavigationControllersInStack(fragment.ncChildFragmentManager).apply {
                add(fragment)
            }
        } catch (e: NoSuchElementException) {
            // This exception occurs if no match where found for last{}
            // it would meant we reach the end of our journey in the stack
            arrayListOf()
        }
    }
}
