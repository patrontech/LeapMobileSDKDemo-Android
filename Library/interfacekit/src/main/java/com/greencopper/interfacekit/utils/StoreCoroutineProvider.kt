package com.greencopper.interfacekit.utils

import com.toggl.komposable.scope.DispatcherProvider
import com.toggl.komposable.scope.StoreScopeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

public class StoreCoroutineProvider{

    public val dispatcherProvider: DispatcherProvider = DispatcherProvider(
        io = Dispatchers.IO,
        computation = Dispatchers.Default,
        main = Dispatchers.Main,
    )

    public val storeScopeProvider: StoreScopeProvider = StoreScopeProvider { coroutineScope }

    private val coroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = dispatcherProvider.main
    }
}
