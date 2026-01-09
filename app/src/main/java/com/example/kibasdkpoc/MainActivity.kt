package com.example.kibasdkpoc

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.kibasdkpoc.databinding.MainBinding
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

    private val binding by lazy { MainBinding.inflate(layoutInflater) }
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        setupEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setupInsets()
        setupBackNavigation()
        observeRootLayout()
        observeSideEffects()
        observeContentChanges()

        onIntentReceived(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        onIntentReceived(intent)
        viewModel.onReadyToRedirect()
    }

    private fun setupEdgeToEdge() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
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

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this) {
            if (RootLayoutHolder.onBackPressDispatcher?.hasEnabledCallbacks() == true) {
                RootLayoutHolder.onBackPressDispatcher?.onBackPressed()
                return@addCallback
            }

            val navigationController = findNavigationControllersInStack(supportFragmentManager)
                .firstOrNull { it.ncChildFragmentManager.backStackEntryCount > 0 }

            if (navigationController != null) {
                navigationController.ncChildFragmentManager.popBackStack()
                return@addCallback
            }

            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
                return@addCallback
            }

            moveTaskToBack(true)
        }
    }

    private fun onIntentReceived(intent: Intent) {
        if (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0) return

        intent.data?.let { uri ->
            viewModel.onDeeplinkReceived(uri.toString())
        }
    }

    private fun observeRootLayout() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                LeapMobileSDK.getRootLayout(supportFragmentManager).collect { fragment ->
                    if (supportFragmentManager.backStackEntryCount > 0) {
                        viewModel.onReadyToRedirect()
                        return@collect
                    }

                    val needsReplacement = fragment.id == 0 ||
                            supportFragmentManager.findFragmentById(fragment.id) == null
                    
                    if (needsReplacement) {
                        replaceFragment(fragment)
                    }
                    viewModel.onReadyToRedirect()
                }
            }
        }
    }

    private fun observeSideEffects() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sideEffects.collectAndConsume { sideEffect ->
                    when (sideEffect) {
                        is MainAppSideEffect.HandleDeeplink -> handleDeeplink(sideEffect.deeplink)
                    }
                }
            }
        }
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

    private fun handleDeeplink(deeplinkUri: String) {
        val fragment = LeapMobileSDK.resolveDeeplink(deeplinkUri.toUri()) ?: return
        addFragment(fragment)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commitNow()
    }

    private fun addFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .add(binding.fragmentContainer.id, fragment)
            .addToBackStack(null)
            .commit()
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
}
