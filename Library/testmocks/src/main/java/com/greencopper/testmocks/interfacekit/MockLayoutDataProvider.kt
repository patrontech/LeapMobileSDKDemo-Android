package com.greencopper.testmocks.interfacekit

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider

public class MockLayoutDataProvider(
    public val mapData: MutableMap<Int, String> = mutableMapOf(),
) : LayoutDataProvider {

    override fun <T : KiboSerializable<T>> getLayoutData(dataHashcode: Int, restoreData: (String) -> T): T? {
        return mapData[dataHashcode]?.let {
            restoreData(it)
        }
    }

    override fun <T : KiboSerializable<T>> addLayoutData(hashCode: Int, layoutData: T) {
        mapData[hashCode] = layoutData.encodeToString()
    }

    override fun clear() {
        mapData.clear()
    }
}
