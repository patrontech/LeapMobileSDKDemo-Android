package com.example.kibasdkpoc

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.greencopper.core.content.manager.ContentManager
import com.greencopper.interfacekit.color.UIColor
import com.greencopper.interfacekit.color.toColorInt
import com.greencopper.interfacekit.navigation.NavigationController
import com.greencopper.interfacekit.rootview.RootLayoutHolder
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

public class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            enableEdgeToEdge()
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        applySDKColors()
        setupWindowInsets()
        observeContentChanges()
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

    private fun setupWindowInsets() {
        val rootContainer = findViewById<View>(R.id.rootContainer)
        val fragmentContainer = findViewById<View>(R.id.fragmentContainer)

        ViewCompat.setOnApplyWindowInsetsListener(rootContainer) { view, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            fragmentContainer.updatePadding(top = insets.top, bottom = insets.bottom)
            view.updatePadding(left = insets.left, right = insets.right)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun observeContentChanges() {
        lifecycleScope.launch {
            runCatching {
                App.resolve<ContentManager>().currentContentFlow.collectLatest {
                    applySDKColors()
                }
            }
        }
    }

    private fun applySDKColors() {
        runCatching {
            val backgroundColor = UIColor.default.topBar.background.toColorInt()
            window.decorView.setBackgroundColor(backgroundColor)
            findViewById<View>(R.id.rootContainer)?.setBackgroundColor(backgroundColor)
        }
    }
}
