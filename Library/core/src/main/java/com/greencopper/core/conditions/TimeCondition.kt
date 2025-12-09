package com.greencopper.core.conditions

import com.greencopper.core.conditions.conditionchecker.ParameterizedCondition
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.timezone.TimezoneProvider
import com.greencopper.toolkit.extensions.toZonedDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

internal class TimeCondition(
    private val flowIntervalSeconds: Long = 30,
    private val timezoneProvider: TimezoneProvider
): ParameterizedCondition<TimeCondition.TimeConditionData>() {

    override fun checkWith(parameter: TimeConditionData): Boolean {
        val zone = timezoneProvider.zoneId
        val now = ZonedDateTime.now(zone)
        val fromDate = parameter.fromDate
        val toDate = parameter.toDate
        return when {
            fromDate != null && toDate != null -> now.isAfter(fromDate) && now.isBefore(toDate)
            fromDate != null -> now.isAfter(fromDate)
            toDate != null -> now.isBefore(toDate)
            else -> throw ParameterizedConditionException.ParamsRequired
        }
    }

    override fun checkWithFlow(parameter: TimeConditionData): Flow<Boolean> = channelFlow {
        while(!isClosedForSend) {
            send(checkWith(parameter))
            delay(flowIntervalSeconds * 1000)
        }
    }

    override fun deserialize(conditionParameters: ConditionParameters): TimeConditionData =
        KiboSerializable.decodeFromJsonElement(conditionParameters)

    @Serializable
    internal data class TimeConditionData(
        private val from: String? = null,
        private val to: String? = null,
    ) : KiboSerializable<TimeConditionData> {
        val fromDate: ZonedDateTime?
            get() = from.toZonedDateTime()

        val toDate: ZonedDateTime?
            get() = to.toZonedDateTime()

        override fun getSerializer(): KSerializer<TimeConditionData> = serializer()
    }

    internal companion object {
        internal val key = ConditionInfo.Key("InterfaceKit.Time", 1)
    }
}
