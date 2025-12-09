package com.greencopper.interfacekit.filtering

import com.greencopper.parsimonious.parse
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.e
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.function.Predicate

@Serializable(with = FilteringPredicateSerializer::class)
public sealed class FilteringPredicate {

    internal abstract fun query(filtersState: Filters): FilteringPredicateComputed?

    public fun query(): FilteringPredicateComputed? = query(emptyMap())

    @Serializable
    public sealed class FilteringPredicateComputed {
        public abstract fun toSQL(): QueryPattern
        public abstract fun toPredicate(): Predicate<List<String>>
    }

    @Serializable
    public class Not(private val filter: FilteringPredicate): FilteringPredicate() {
        @Serializable
        private class Result(private val filter: FilteringPredicateComputed): FilteringPredicateComputed() {
            override fun toSQL(): QueryPattern = "NOT (${filter.toSQL()})"
            override fun toPredicate(): Predicate<List<String>> =
                Predicate.not(filter.toPredicate())

            override fun toString(): String = "NOT ($filter)"
        }
        override fun query(filtersState: Filters): FilteringPredicateComputed? =
            filter.query(filtersState)?.let { Result(it) }

        override fun toString(): String = "NOT $filter"
        override fun equals(other: Any?): Boolean =
            other is Not && filter == other.filter
        override fun hashCode(): Int = 31 * filter.hashCode()
    }

    @Serializable
    public class Tag(private val tag: String) : FilteringPredicate() {

        @Serializable
        private class Result(private val tag: String) : FilteringPredicateComputed() {

            override fun toSQL(): QueryPattern {
                return "tags LIKE '%\"$tag\"%'"
            }

            override fun toPredicate(): Predicate<List<String>> {
                return Predicate { it.contains(tag) }
            }

            override fun toString(): String = "TAG \"$tag\""
        }

        override fun query(filtersState: Filters): FilteringPredicateComputed = Result(tag)

        override fun toString(): String = "TAG $tag"

        override fun equals(other: Any?): Boolean =
            other is Tag
                    && tag == other.tag

        override fun hashCode(): Int = tag.hashCode()
    }

    @Serializable
    public class Filter(private val filterId: FilterId) : FilteringPredicate() {

        @Serializable
        private class Result(
            private val filterResult: FilteringPredicateComputed,
        ) : FilteringPredicateComputed() {
            override fun toSQL(): QueryPattern = filterResult.toSQL()

            override fun toPredicate(): Predicate<List<String>> = filterResult.toPredicate()

            override fun toString(): String = "$filterResult"
        }

        override fun query(filtersState: Filters): FilteringPredicateComputed? =
            filtersState[filterId]?.predicate?.query(filtersState)?.let { Result(it) }

        override fun toString(): String = "FILTER $filterId"

        override fun equals(other: Any?): Boolean =
            other is Filter
                    && filterId == other.filterId

        override fun hashCode(): Int = filterId.hashCode()
    }

    @Serializable
    public class Logic(
        private val leftPredicate: FilteringPredicate,
        private val operator: Operator,
        private val rightPredicate: FilteringPredicate,
    ) : FilteringPredicate() {

        @Serializable
        private class Result(
            private val leftQuery: FilteringPredicateComputed,
            private val operator: Operator,
            private val rightQuery: FilteringPredicateComputed,
        ) : FilteringPredicateComputed() {
            override fun toSQL(): QueryPattern {
                val leftSQL = leftQuery.toSQL()
                val rightSQL = rightQuery.toSQL()
                return when (operator) {
                    Operator.AND -> "($leftSQL AND $rightSQL)"
                    Operator.OR -> "($leftSQL OR $rightSQL)"
                }
            }

            override fun toPredicate(): Predicate<List<String>> {
                val leftPredicate = leftQuery.toPredicate()
                val rightPredicate = rightQuery.toPredicate()

                return when (operator) {
                    Operator.AND -> leftPredicate.and(rightPredicate)
                    Operator.OR -> leftPredicate.or(rightPredicate)
                }
            }

            override fun toString(): String {
                return "($leftQuery $operator $rightQuery)"
            }
        }

        override fun query(filtersState: Filters): FilteringPredicateComputed? {
            val leftQuery = leftPredicate.query(filtersState)
            val rightQuery = rightPredicate.query(filtersState)

            leftQuery ?: return rightQuery
            rightQuery ?: return leftQuery

            return Result(leftQuery, operator, rightQuery)
        }

        override fun toString(): String = "$leftPredicate $operator $rightPredicate"

        override fun equals(other: Any?): Boolean =
            other is Logic
                    && leftPredicate == other.leftPredicate
                    && operator == other.operator
                    && rightPredicate == other.rightPredicate

        override fun hashCode(): Int {
            var result = leftPredicate.hashCode()
            result = 31 * result + operator.hashCode()
            result = 31 * result + rightPredicate.hashCode()
            return result
        }
    }

    @Serializable(with = OperatorSerializer::class)
    public enum class Operator(internal val string: String) {
        AND("AND"), OR("OR");

        override fun toString(): String = string
    }

    internal object OperatorSerializer : KSerializer<Operator> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("OperatorSerializer", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: Operator) {
            encoder.encodeString(value.string)
        }

        override fun deserialize(decoder: Decoder): Operator {
            return when (decoder.decodeString()) {
                Operator.AND.string -> Operator.AND
                Operator.OR.string -> Operator.OR
                else -> throw SerializationException("Couldn't decode correct sub-class of Operator")
            }
        }
    }
}

internal object FilteringPredicateSerializer : KSerializer<FilteringPredicate> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FilteringPredicateSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: FilteringPredicate) {
        return encoder.encodeString(
            value.toString()
        )
    }

    override fun deserialize(decoder: Decoder): FilteringPredicate =
        try {
            parse(decoder.decodeString(), grammar)
        } catch (throwable: Throwable) {
            App.log.e("Predicate parsing failed", throwable = throwable)
            throw throwable
        }
}

public typealias QueryPattern = String

public fun List<String>.foldOr(): FilteringPredicate? {
    return if (isNotEmpty()) {
        val tag = get(0)
        minus(tag).fold(FilteringPredicate.Tag(tag) as FilteringPredicate) { acc, i ->
            FilteringPredicate.Logic(
                acc,
                FilteringPredicate.Operator.OR,
                FilteringPredicate.Tag(i)
            )
        }
    } else null
}
