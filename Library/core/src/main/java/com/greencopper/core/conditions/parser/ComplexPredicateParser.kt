package com.greencopper.core.conditions.parser

import com.greencopper.parsimonious.*
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.e
import com.greencopper.toolkit.versionprovider.BuildConfigProvider

internal class ComplexPredicateParser(
    private val buildConfigProvider: BuildConfigProvider,
): PredicateParser {
    override fun parse(predicate: String): Predicate =
        try {
            parse(predicate, grammar)
        } catch (t: Throwable) {
            if (buildConfigProvider.isDebug) throw t
            App.log.e("Exception parsing predicate \"$predicate\"", throwable = t)
            Failing()
        }
}

private fun Char.isIdentifier() = this.isAlpha() || this == '_' || this == '-'

private val opAnd = string("AND") into Op.AND
private val opOr = string("OR") into Op.OR
private val op = (opAnd or opOr) followedBy rws

private val id: Parser<Char, Predicate> = (char(Char::isAlpha) + manyS(Char::isIdentifier or Char::isNumeric)) into ::Id
private val terminator = peek(rws) or peek(char(')')) or eof()
private val identifier: Parser<Char, Predicate> = id except ((string("NOT") or string("AND") or string("OR")) followedBy terminator)
private val notExpression: Parser<Char, Predicate> = late { string("NOT") preceding rws preceding expression.map(::Not) }
private val logicExpression: Parser<Char, Predicate> = late {
    (pair(Parser(Op.AND), expression) cons many(pair(op, expression))).map { expressions ->
        // The first expression has a dummy AND before it, so we just drop that.
        var logic: Predicate = expressions.first().second
        for ((op, predicate) in expressions.drop(1)) {
            logic = Logic(logic, op, predicate)
        }
        logic
    }
}
private val begin = char('(') followedBy ows
private val end = char(')') followedBy ows
private val parentheticExpression = begin preceding (logicExpression followedBy ows) followedBy end
private val expression = (notExpression or identifier or parentheticExpression) followedBy ows
private val grammar = ows preceding logicExpression followedBy eof()
