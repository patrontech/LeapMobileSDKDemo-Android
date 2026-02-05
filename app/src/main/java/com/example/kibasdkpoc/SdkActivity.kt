package com.example.kibasdkpoc

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.kibasdkpoc.analytics.MyScreenViewEvent
import com.example.kibasdkpoc.databinding.MainBinding
import com.greencopper.leapmobilesdk.core.content.manager.ContentManager
import com.greencopper.leapmobilesdk.core.services.track
import com.greencopper.leapmobilesdk.interfacekit.color.UIColor
import com.greencopper.leapmobilesdk.interfacekit.color.toColorInt
import com.greencopper.leapmobilesdk.interfacekit.navigation.NavigationController
import com.greencopper.leapmobilesdk.interfacekit.rootview.RootLayoutHolder
import com.greencopper.leapmobilesdk.toolkit.App
import com.greencopper.leapmobilesdk.toolkit.di.resolver.resolve
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

public class SdkActivity : FragmentActivity() {

    private val binding by lazy { MainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupInsets()
        setupBackNavigation()
        observeContentChanges()
        handleDeeplink()

        // Track that this activity started (generic)
        App.track(MyScreenViewEvent("SdkActivity"))
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this) {
            if (RootLayoutHolder.onBackPressDispatcher?.hasEnabledCallbacks() == true) {
                RootLayoutHolder.onBackPressDispatcher?.onBackPressed()
                return@addCallback
            }

            val navigationController =
                findNavigationControllersInStack(supportFragmentManager).firstOrNull { it.ncChildFragmentManager.backStackEntryCount > 0 }

            if (navigationController != null) {
                navigationController.ncChildFragmentManager.popBackStack()
                return@addCallback
            }

            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
                return@addCallback
            }

            finish()
        }
    }

    private fun findNavigationControllersInStack(fragmentManager: FragmentManager): List<NavigationController<*>> {
        val result = mutableListOf<NavigationController<*>>()
        for (fragment in fragmentManager.fragments) {
            if (fragment is NavigationController<*>) {
                result.addAll(findNavigationControllersInStack(fragment.ncChildFragmentManager))
                result.add(fragment)
            } else {
                result.addAll(findNavigationControllersInStack(fragment.childFragmentManager))
            }
        }
        return result
    }

    private fun observeContentChanges() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                runCatching {
                    App.resolve<ContentManager>().currentContentFlow.collectLatest {
                        runCatching {
                            val backgroundColor = UIColor.default.topBar.background.toColorInt()
                            window.decorView.setBackgroundColor(backgroundColor)
                            binding.rootContainer.setBackgroundColor(backgroundColor)
                        }
                    }
                }
            }
        }
    }

    private fun handleDeeplink() {
        val intentData = intent.data
        val fragment = LeapMobileSDK.resolveDeeplink(intentData ?: Uri.EMPTY)

        if (intentData != null && fragment != null) {
            replaceView(fragment)
        } else {
            lifecycleScope.launch {
                LeapMobileSDK.getRootLayout(supportFragmentManager).collect { fragment ->
                    replaceView(fragment)
                }
            }
        }
    }

    private fun replaceView(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit()
        // Track screen view using the fragment class simple name
        val screenName = fragment::class.java.simpleName ?: "unknown_screen"
        App.track(MyScreenViewEvent(screenName))
    }

    private fun setupEdgeToEdge() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            enableEdgeToEdge()
        }
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootContainer) { view, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            binding.fragmentContainer.updatePadding(top = insets.top, bottom = insets.bottom)
            view.updatePadding(left = insets.left, right = insets.right)
            WindowInsetsCompat.CONSUMED
        }
    }
}
