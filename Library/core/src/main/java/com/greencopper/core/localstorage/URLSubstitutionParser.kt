package com.greencopper.core.localstorage

import com.greencopper.parsimonious.*

internal object URLSubstitutionParser {
    internal sealed class Part {
        class Key(val key: LocalStorageKey): Part() {
            constructor(key: String): this(LocalStorageKey(key))
        }
        class Subscript(val subscript: String): Part()
        class Optional(val optional: Boolean = true): Part()
        class Range(val range: IntRange): Part()
    }

    internal class Substitution(
        val key: LocalStorageKey,
        val subscript: String?,
        val optional: Boolean,
        val range: IntRange
    ) {
        companion object {
            internal fun fromParts(parts: List<Part>): Substitution? {
                var key: LocalStorageKey? = null
                var subscript: String? = null
                var optional: Boolean = false
                var range: IntRange? = null
                for (part in parts) {
                    when (part) {
                        is Part.Key -> key = part.key
                        is Part.Subscript -> subscript = part.subscript
                        is Part.Optional -> optional = part.optional
                        is Part.Range -> range = part.range
                    }
                }
                if (key == null || range == null) return null
                return Substitution(key, subscript, optional, range)
            }
        }
    }

    private val name: Parser<Char, String> = many1S(Char::isIdentifier or Char::isNumeric)
    private val computation: Parser<Char, String> = string("%")
    private val app: Parser<Char, String> = string("@")
    private val currentProject: Parser<Char, String> = string("~")

    private val root: Parser<Char, String> = name or computation or app or currentProject
    private val key: Parser<Char, Part> = (root + many1S(string("/") + name)) into Part::Key

    private val singleQuotation: Parser<Char, String> = many1S(char { it != '\'' }) surroundedBy char('\'')
    private val doubleQuotation: Parser<Char, String> = many1S(char { it != '"'}) surroundedBy char('"')
    private val quotation = singleQuotation or doubleQuotation

    private val subscript: Parser<Char, Part> = char('[') preceding quotation followedBy char(']') into Part::Subscript
    private val opt: Parser<Char, Part> = string("?") into Part.Optional(true)
    private val nosubstitution: Parser<Char, List<Part>> = char { true } into emptyList()

    private val substitution: Parser<Char, List<Part>> =
        braced( (key.toList() + optional(subscript.toList()) + optional(opt.toList())) followedBy optional((ows preceding oneOf("=/")))).ranged.map {
            it.first + Part.Range(it.second)
        }

    /**
     * Parses the entire URL and creates a `List<Substitution>`.
     *
     * The way this works is that it visits every character. At each character,
     * it tries to match a [substitution] and consumes the entire substitution
     * if that succeeds. It outputs a `List<Part>` for each substitution it finds.
     * If it does not match, it consumes that character and outputs an empty
     * `List<Part>`. The end result is `List<List<Part>>`. Each non-empty `List<Part>`
     * represents a substitution. Once we have this, we pass each `List<Part>` to
     * the `fromParts` method of [Substitution.Companion]. For the empty lists,
     * it returns `null`. For the non-empty lists, it returns a [Substitution].
     * At the end `mapNotNull` gives us only the list of substitutions.
     */
    internal val url: Parser<Char, List<Substitution>> =
        many1(substitution or nosubstitution).map {
            it.mapNotNull(Substitution.Companion::fromParts)
        }
}

/*
 We also have these in our ConditionParser. The reason we don't reuse them is because one mini-
 language's definition of `isAlpha` can be different from another's. If we put them definitions
 in one place and change them, it could cause bugs by changing what a language expects. In general,
 it's safest to keep them separate.
 */
private fun Char.isIdentifier() = this.isAlpha() || this == '_' || this == '-'
