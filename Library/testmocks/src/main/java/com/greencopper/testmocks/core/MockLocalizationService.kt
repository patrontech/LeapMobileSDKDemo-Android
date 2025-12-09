package com.greencopper.testmocks.core

import com.greencopper.core.localization.service.LocalizationService

public class MockLocalizationService(
    public var getDefaultLocaleString: (String) -> String = { it },
    public var getQuantityStringFromRepository: (String, Int) -> String? = { key, _ -> key },
    public var getStringFromRepository: (key: String) -> String? = { it }
) : LocalizationService {

    public val requestedKeys: MutableList<String> = mutableListOf()

    override fun getDefaultLocaleString(key: String): String {
        requestedKeys.add(key)
        return getDefaultLocaleString.invoke(key)
    }

    override fun getQuantityStringFromRepository(key: String, quantity: Int): String? {
        requestedKeys.add(key)
        return getQuantityStringFromRepository.invoke(key, quantity)
    }

    override fun getStringFromRepository(key: String): String? {
        requestedKeys.add(key)
        return getStringFromRepository.invoke(key)
    }
}
