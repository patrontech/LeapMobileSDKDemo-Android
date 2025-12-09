package com.greencopper.interfacekit.filtering

import com.greencopper.interfacekit.filtering.FilteringPredicate.*
import com.greencopper.parsimonious.*

private fun Char.isIdentifier() = isAlpha() || this == '_' || this == '-'
private val opAnd = string("AND") into FilteringPredicate.Operator.AND
private val opOr = string("OR") into FilteringPredicate.Operator.OR
private val op = (opAnd or opOr) followedBy rws

private val tagIdentifier = many1S(Char::isIdentifier or Char::isNumeric)
private val tagExpression: Parser<Char, FilteringPredicate> = string("TAG") preceding rws preceding tagIdentifier into ::Tag

private val filterIdentifier = many1S(Char::isIdentifier) + manyS(Char::isIdentifier or Char::isNumeric)
private val filterExpression: Parser<Char, FilteringPredicate> = string("FILTER") preceding rws preceding filterIdentifier into ::Filter

private val logicExpression: Parser<Char, FilteringPredicate> = late {
    (pair(Parser(FilteringPredicate.Operator.AND), expression) cons many(pair(op, expression))).map { expressions ->
        // The first expression has a dummy AND before it, so we just drop that.
        var logic: FilteringPredicate = expressions.first().second
        for ((op, condition) in expressions.drop(1)) {
            logic = Logic(logic, op, condition)
        }
        logic
    }
}
private val begin = char('(') followedBy ows
private val end = char(')') followedBy ows
private val parentheticExpression = begin preceding (logicExpression followedBy ows) followedBy end

private val subexpression: Parser<Char, FilteringPredicate> = (filterExpression or tagExpression or parentheticExpression) followedBy ows
private val notExpression: Parser<Char, FilteringPredicate> = string("NOT") preceding rws preceding subexpression into ::Not
private val expression: Parser<Char, FilteringPredicate> = (notExpression or subexpression) followedBy ows
internal val grammar = ows preceding logicExpression followedBy eof()
