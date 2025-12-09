package com.greencopper.core.conditions.conditionchecker

import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.core.conditions.MetadataQueryCondition
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.logging.e
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.JsonElement

internal class DIConditionResolver : ConditionResolver {
    override fun resolve(key: ConditionInfo.Key, metadata: MutableStateFlow<JsonElement>): Condition? {
        return try {
            val condition = App.resolve<Condition>(tag = key)
            (condition as? MetadataQueryCondition)?.metadata = metadata
            condition
        } catch (t: Throwable) {
            App.log.e("Condition was not resolved for $key: ${t.message}")
            null
        }
    }
}
