package com.greencopper.ticketing.providers.showclix.conditions

import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.core.conditions.ConditionParameters
import com.greencopper.core.conditions.conditionchecker.ParameterizedCondition
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.LocalStorageProperty
import com.greencopper.ticketing.models.Ticket
import com.greencopper.ticketing.providers.showclix.showclix
import com.greencopper.toolkit.di.resolver.LazyResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal class ShowclixTicketsCondition(
    private val lazyLocalStorage: LazyResolver<LocalStorage>,
) : ParameterizedCondition<ShowclixTicketsCondition.ShowclixTicketsConditionParameter>() {

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun checkWith(parameter: ShowclixTicketsConditionParameter): Boolean =
        getTickets().value.checkParameters(parameter)

    override fun checkWithFlow(parameter: ShowclixTicketsConditionParameter): Flow<Boolean> =
        getTickets().state.map { it.checkParameters(parameter) }

    private fun List<Ticket>.checkParameters(params: ShowclixTicketsConditionParameter): Boolean {
        if (!params.isContentValid()) return false
        val date = params.date?.toDate()

        return any { ticket ->
            val ticketDate = ticket.startDate?.toLocalDate()

            date?.let {
                if (ticketDate?.isEqual(date) != true) return@any false
            }

            params.name?.let {
                if (it != ticket.primaryTitle) return@any false
            }

            params.between?.let {
                val startDate = it.date1.toDate()
                val endDate = it.date2.toDate()
                if (startDate != null && endDate != null && ticketDate != null) {
                    if (ticketDate.isBefore(startDate) || ticketDate.isAfter(endDate)) return@any false
                } else {
                    return@any false
                }
            }

            true
        }
    }

    private fun String?.toDate(): LocalDate? {
        return this?.let {
            try {
                LocalDate.parse(it, dateTimeFormatter)
            } catch (throwable: Throwable) {
                null
            }
        }
    }

    private fun getTickets(): LocalStorageProperty<List<Ticket>> =
        lazyLocalStorage.resolve().project.showclix.tickets

    override fun deserialize(conditionParameters: ConditionParameters): ShowclixTicketsConditionParameter =
        KiboSerializable.decodeFromJsonElement(conditionParameters)

    internal companion object {
        internal val key = ConditionInfo.Key("Ticketing.Showclix.Tickets", 1)
    }

    @Serializable
    internal data class ShowclixTicketsConditionParameter(
        val date: String? = null,
        val name: String? = null,
        val between: Between? = null,
    ) : KiboSerializable<ShowclixTicketsConditionParameter> {
        override fun getSerializer(): KSerializer<ShowclixTicketsConditionParameter> = serializer()

        fun isContentValid(): Boolean {
            return when {
                date == null && name == null && between == null -> false
                date != null && between != null -> false
                else -> true
            }
        }

        @Serializable
        data class Between(val date1: String, val date2: String)
    }
}
