package com.greencopper.core.localstorage

import kotlinx.serialization.json.Json

public sealed class LocalStorageJsonFactory {
    public companion object {
        public fun create(): Json = Json {
            ignoreUnknownKeys = true
            allowStructuredMapKeys = true
            allowTrailingComma = true
        }

        public const val LOCAL_STORAGE_TAG: String = "localStorage"
    }
}
