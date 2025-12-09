package com.greencopper.interfacekit.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentOnAttachListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.greencopper.interfacekit.color.*
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.oneTimeFragmentOnAttachListener
import com.greencopper.interfacekit.ui.activity.KibaMainActivity
import com.greencopper.interfacekit.ui.setNavigationBarColor
import com.greencopper.interfacekit.ui.shouldColorNavigationBar
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.NavigationControlsHandler
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.LogLevel
import com.greencopper.toolkit.logging.i
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Deprecated(
    "You should not inherit from this class. " +
            "Please check ParameterizedFragment or UnparameterizedFragment",
    ReplaceWith(""),
    DeprecationLevel.WARNING
)
public abstract class BaseFragment(@LayoutRes layout: Int = 0) : Layout(layout) {

    protected abstract val screenColor: ScreenColor?
    protected open val navigationBarColor: Int by lazy { InterfaceKitColor.bottomBar.background }
    protected open val binding: ViewBinding? = null

    protected val navigationControlsHandler: NavigationControlsHandler?
        get() = _navigationControlsHandler
    private var _navigationControlsHandler: NavigationControlsHandler? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState) ?: binding?.root
        view?.layoutDirection = TextUtils.getLayoutDirectionFromLocale(App.locale)
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.log.i("Fragment created: ${getStackTag()}")
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backgroundColor = UIColor.default.background.secondary.toColorInt()
        view.setBackgroundColor(backgroundColor)
        view.setOnTouchListener { _, _ -> true }

        if (shouldColorNavigationBar) {
            (parentFragment as? BottomSheetDialogFragmentContainer)?.dialog?.let {
                setNavigationBarColor(it, navigationBarColor)
            } ?: run {
                setNavigationBarColor(activity, navigationBarColor)
            }
        }

        _navigationControlsHandler = createNavigationControlsHandler()
        navigationControlsHandler?.setup()

        setStatusBarStyle()
        consumeWindowInsets()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _navigationControlsHandler = null
    }

    private fun setStatusBarStyle() {
        val style = screenColor?.statusBar ?: UIColor.default.statusBar
        val window = activity?.window ?: return
        val statusBarColorStyle = if (isDarkMode()) style.dark else style.light
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            statusBarColorStyle == DefaultColors.StatusBar.Style.LIGHT
    }

    private fun consumeWindowInsets() {
        val activity = (activity as? KibaMainActivity) ?: return
        val container = activity.findViewById<View>(activity.getContainerId())

        ViewCompat.setOnApplyWindowInsetsListener(container) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            activity.applyInsets(
                insets = insets,
                statusBarColor = screenColor?.topBar?.background ?: navigationBarColor,
                navBarColor = navigationBarColor,
                shouldColorNavBar = shouldColorNavigationBar,
            )
            container.setBackgroundColor(navigationBarColor)
            WindowInsetsCompat.CONSUMED
        }
    }

    protected open fun createNavigationControlsHandler(): NavigationControlsHandler? = null
}

internal fun Fragment.getStackTag(): String {
    return "${javaClass.simpleName}{$arguments}"
}

public suspend fun Fragment.findVisibleFragment(): Fragment {
    waitToBeAttached().await()
    val children = this.childFragmentManager.fragments
    return if (children.isNotEmpty()) {
        children.last().findVisibleFragment()
    } else {
        this
    }
}

public fun Fragment.waitToBeAttached(): CompletableDeferred<Boolean> {
    val awaitIsAttached = CompletableDeferred<Boolean>()
    if (isAdded) {
        awaitIsAttached.complete(true)
    }
    val onAttachListener: FragmentOnAttachListener = oneTimeFragmentOnAttachListener { _, _ ->
        awaitIsAttached.complete(true)
    }
    parentFragmentManager.addFragmentOnAttachListener(onAttachListener)
    return awaitIsAttached
}

/**
 * Thrown by {@link FragmentFactory#instantiate(ClassLoader, String)} when
 * there is an instantiation failure.
 */
@Throws(Exception::class)
internal fun Fragment.checkProvideEmptyConstructor() {
    try {
        this::class.java.getConstructor()
    } catch (e: Exception) {
        App.log.log(
            LogLevel.ERROR,
            """You forgot to define a public empty constructor for this Fragment. 
                    |Remember to mark it as deprecated.""".trimMargin()
        )
        throw e
    }
}

public fun LifecycleOwner.launchRepeatingJob(
    state: Lifecycle.State,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit,
): Job = lifecycleScope.launch(coroutineContext) {
    repeatOnLifecycle(state, block)
}
