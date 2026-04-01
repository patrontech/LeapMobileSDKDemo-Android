package com.example.kibasdkpoc

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.example.kibasdkpoc.analytics.MyScreenViewEvent
import com.example.kibasdkpoc.databinding.MainBinding
import com.greencopper.leapmobilesdk.LeapMobileSDK
import com.greencopper.leapmobilesdk.interfacekit.navigation.NavigationController
import com.greencopper.leapmobilesdk.interfacekit.rootview.RootLayoutHolder
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

public class SdkActivity : FragmentActivity() {

    private val binding by lazy { MainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupInsets()
        setupBackNavigation()
        handleDeeplink()

        // Track that this activity started (generic)
        LeapMobileSDK.track(MyScreenViewEvent("SdkActivity"))
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

    private fun handleDeeplink() {
        val intentData = intent.data

        val fragment = runCatching {
            LeapMobileSDK.resolveDeeplink(intentData ?: Uri.EMPTY)
        }.getOrNull()

        if (intentData != null && fragment != null) {
            runCatching {
                replaceView(fragment)
            }.onFailure {
                if (it is IllegalStateException) {
                    showErrorAndFinish(it)
                }
            }
        } else {
            lifecycleScope.launch {
                runCatching {
                    LeapMobileSDK
                        .getRootLayout(supportFragmentManager)
                        .collect { fragment ->
                            replaceView(fragment)
                        }
                }.onFailure {
                    if (it !is CancellationException) {
                        showErrorAndFinish(it)
                    }
                }
            }
        }
    }

    private fun showErrorAndFinish(e: Throwable) {
        Toast.makeText(this, e.message ?: "Unexpected error", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun replaceView(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit()
        // Track screen view using the fragment class simple name
        val screenName = fragment::class.java.simpleName ?: "unknown_screen"
        LeapMobileSDK.track(MyScreenViewEvent(screenName))
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
