package com.example.kibasdkpoc

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.greencopper.interfacekit.navigation.NavigationController
import com.greencopper.interfacekit.rootview.RootLayoutHolder
import kotlinx.coroutines.launch

public class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            enableEdgeToEdge()
        }

        setupBackNavigation()

        if (intent.data != null) {
            handleDeeplink(intent.data)
        } else {
            lifecycleScope.launch {
                LeapMobileSDK.getRootLayout(supportFragmentManager).collect { fragment ->
                    replaceView(fragment)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeeplink(intent.data)
    }

    private fun handleDeeplink(uri: Uri?) {
        uri?.let {
            LeapMobileSDK.resolveDeeplink(uri)?.let { fragment -> replaceView(fragment) }
        }
    }

    private fun replaceView(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    /** Handles back navigation for the SDK's nested NavigationController fragments. */
    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this) {
            if (RootLayoutHolder.onBackPressDispatcher?.hasEnabledCallbacks() == true) {
                RootLayoutHolder.onBackPressDispatcher?.onBackPressed()
                return@addCallback
            }

            val navigationController = findNavigationControllersInStack(supportFragmentManager)
                .firstOrNull { it.ncChildFragmentManager.backStackEntryCount > 0 }

            if (navigationController == null) {
                moveTaskToBack(true)
                return@addCallback
            }

            navigationController.ncChildFragmentManager.popBackStack()
        }
    }

    /** Finds all NavigationControllers in the fragment hierarchy, innermost first. */
    private fun findNavigationControllersInStack(fragmentManager: FragmentManager): ArrayList<NavigationController<*>> {
        val result = arrayListOf<NavigationController<*>>()

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
}
