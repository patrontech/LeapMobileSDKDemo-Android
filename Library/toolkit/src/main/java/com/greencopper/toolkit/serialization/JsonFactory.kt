package com.greencopper.toolkit.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

public sealed class JsonFactory {
    public companion object {
        @OptIn(ExperimentalSerializationApi::class)
        public fun create(): Json = Json {
            ignoreUnknownKeys = true
            allowStructuredMapKeys = true
            allowTrailingComma = true
        }
    }
}
