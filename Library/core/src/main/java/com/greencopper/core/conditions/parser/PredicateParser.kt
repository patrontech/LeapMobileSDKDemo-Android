package com.greencopper.core.conditions.parser

internal interface PredicateParser {
    fun parse(predicate: String): Predicate
}
