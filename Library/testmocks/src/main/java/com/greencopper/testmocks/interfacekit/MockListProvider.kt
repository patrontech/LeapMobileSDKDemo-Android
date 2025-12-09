package com.greencopper.testmocks.interfacekit

import com.greencopper.interfacekit.list.provider.ListProvider
import com.greencopper.toolkit.testing.unimplemented

public class MockListProvider(
    public var _getElements: () -> List<ListProvider.Element> = { unimplemented() },
) : ListProvider {
    override suspend fun getElements(): List<ListProvider.Element> =
        _getElements()

}
