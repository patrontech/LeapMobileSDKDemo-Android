package com.greencopper.core.json

import com.greencopper.parsimonious.*

internal object JsonQueryParser {
    private val key = many1S(Char::isKeyAlpha) + manyS(Char::isNumeric or Char::isKeyAlpha)
    private val keySelector: Parser<Char, JsonQuery> = (jquote or key) into JsonQuery::Key

    private val indexSelector: Parser<Char, JsonQuery> =
        (optional(char('-')) + many1S(Char::isNumeric)).map {
            JsonQuery.Index(it.toInt())
        }

    private val countSelector: Parser<Char, JsonQuery> = char('#') into JsonQuery.Count
    private val flattenSelector: Parser<Char, JsonQuery> = char('^') into JsonQuery.Flatten
    private val uniqueSelector: Parser<Char, JsonQuery> = char('~') into JsonQuery.Unique
    private val sortSelector: Parser<Char, JsonQuery> = char('$') into JsonQuery.Sort
    private val functionSelector: Parser<Char, JsonQuery> = countSelector or flattenSelector or uniqueSelector or sortSelector

    /*
     Order matters a lot here. Trigraphs must precede digraphs,
     and digraphs must precede monographs. If not, parsing could
     consume part of the operator and then abort when it cannot
     match the rest.
     */
    private val ops =
            JsonQuery.Op.NOTYPEQ.parser or
            JsonQuery.Op.TYPEQ.parser or
            JsonQuery.Op.EQ.parser or
            JsonQuery.Op.NE.parser or
            JsonQuery.Op.GTE.parser or
            JsonQuery.Op.LTE.parser or
            JsonQuery.Op.GT.parser or
            JsonQuery.Op.LT.parser or
            JsonQuery.Op.MAP.parser or
            JsonQuery.Op.AND.parser or
            JsonQuery.Op.OR.parser

    private val pipeline: Parser<Char, JsonQuery> = late { many1(logic, char('|') surroundedBy ows) } into JsonQuery::Path
    private val parentheticSelector: Parser<Char, JsonQuery> = parenthesized(pipeline surroundedBy ows)

    private val currentSelector: Parser<Char, JsonQuery> = char('.') into JsonQuery.Current
    private val jsonSelector: Parser<Char, JsonQuery> = json into JsonQuery::Json
    private val filterSelector: Parser<Char, JsonQuery> = bracketed(pipeline surroundedBy ows) into JsonQuery::Filter

    private val subscriptSelector = indexSelector or keySelector
    private val commonSelector = functionSelector or parentheticSelector or currentSelector
    private val initialSelector = jsonSelector or commonSelector
    private val followingSelector = subscriptSelector or filterSelector or commonSelector

    private val pathParser = ((initialSelector followedBy ows) cons many(followingSelector, ows))
    private val path = late { negativePath or positivePath }
    private val positivePath: Parser<Char, JsonQuery> = pathParser into JsonQuery::Path
    private val negativePath: Parser<Char, JsonQuery> = (char('!') preceding path) into JsonQuery::Not

    /**
     * There is no operator precedence. All expressions are left-grouped.
     * Implementing operator precedence would require two-phase parsing
     * with a tokenization step, which just isn't worth it for a little
     * DSL like this. Use parentheses!
     */
    private val logic: Parser<Char, JsonQuery> =
        (pair(Parser(JsonQuery.Op.AND), path) cons many(pair(whitespaced(ops), path))).map {
            var logic: JsonQuery = it.first().second
            for ((op, p) in it.drop(1)) {
                logic = JsonQuery.Logic(logic, op, p)
            }
            logic
        }

    fun parse(input: String): JsonQuery =
        parse(input, whitespaced(pipeline) followedBy eof())
}

// Some helpers
private fun Char.isKeyAlpha() = this.isAlpha() || this == '_' || this == '-'
private val JsonQuery.Op.parser: Parser<Char, JsonQuery.Op>
    get() = string(op) into this
