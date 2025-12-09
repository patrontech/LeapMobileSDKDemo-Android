package com.greencopper.interfacekit.navigation.layout

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.interfacekit.common.interfaceKit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

public interface LayoutDataProvider {
    public fun <T : KiboSerializable<T>> getLayoutData(dataHashcode: Int, restoreData: (String) -> T): T?

    /**
     * Store the layoutData in DataStore. If hashcode is already registered, operation will be skipped.
     */
    public fun <T : KiboSerializable<T>> addLayoutData(hashCode: Int, layoutData: T)
    public fun clear()
}

public class ConcreteLayoutDataProvider(
    private val localStorage: LocalStorage,
    private val singleThreadScope: CoroutineScope,
) : LayoutDataProvider {
    private val layoutDataStorage
        get() = localStorage.app.interfaceKit.layoutData

    override fun <T : KiboSerializable<T>> getLayoutData(
        dataHashcode: Int,
        restoreData: (String) -> T,
    ): T? {
        val encodedData: String = layoutDataStorage.value[dataHashcode] ?: return null
        return restoreData(encodedData)
    }

    override fun <T : KiboSerializable<T>> addLayoutData(hashCode: Int, layoutData: T) {
        val layoutDataStorage = layoutDataStorage

        if (!layoutDataStorage.value.containsKey(hashCode)) {
            singleThreadScope.launch {
                layoutDataStorage.value = layoutDataStorage.value + Pair(hashCode, layoutData.encodeToString())
            }
        }
    }

    override fun clear() {
        layoutDataStorage.value = emptyMap()
    }
}
