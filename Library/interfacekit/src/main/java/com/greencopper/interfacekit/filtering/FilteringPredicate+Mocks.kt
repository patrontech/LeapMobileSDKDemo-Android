package com.greencopper.interfacekit.filtering

import java.util.function.Predicate

public class MockFilteringPredicateComputed(
    public var queryPattern: QueryPattern = "",
    public var predicateResult: Boolean = true,
) : FilteringPredicate.FilteringPredicateComputed() {
    override fun toSQL(): QueryPattern = queryPattern

    override fun toPredicate(): Predicate<List<String>> =
        Predicate<List<String>> { predicateResult }

    override fun toString(): String {
        return queryPattern
    }
}

public fun FilteringPredicate.FilteringPredicateComputed?.copy(): MockFilteringPredicateComputed = this?.let {
    MockFilteringPredicateComputed(it.toSQL(), it.toPredicate().test(listOf("")))
} ?: MockFilteringPredicateComputed()
