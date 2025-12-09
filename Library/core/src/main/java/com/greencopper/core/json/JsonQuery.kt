package com.greencopper.core.json

import kotlinx.serialization.json.*

/**
A recursive type that represents a query against some JSON.

Not every [JsonQuery] that can be expressed in code can be expressed
in the query language. For instance, a [JsonQuery.Json] node can in theory
occur anywhere in a [JsonQuery.Path], but in the query language, [JsonQuery.Json] is
only possible as the very first (and usually only) element in a path.

The query language is very simple and is very similar to many other
JSON query languages, especially JQ.

A quick overview can be shown by some examples:

```json
{
    "custom": {
        "myID": "xyzabc",
        "yourID": "",
        "cool": true,
        "primes": [2, 3, 5, 7, 11, 13]
    }
}
```

Given the JSON above, here are some queries and what they mean:

- `.custom.myId`: Does this node exist and is it non-empty? In this case,
the answer is yes.
- `.custom.yourId`: Does this node exist and is it non-empty? in this case,
the answer is no, because the "truth value" of an empty string is `false`.
- `.custom.yourId != null`: Does this key exist? This is true even if the
value is an empty string.
- `.primes[. > 5]`: Are there any numbers in `primes` greater than 5?
- `.primes.0 == 2`: Is the first element in `primes` equal to 2?

In truth, these queries do not return Booleans. They return the actual JSON,
which of course could be `JSON.bool(true)`. But every JSON node has a
truth value, as explained below, and that's what we're interested in.

The basic unit of a query is a _selector_. They are:

- The current node selector, `.`.
- A key selector, such as `.name` or `."xyz abc"`.
- An index selector, such as `.0`.
- A filter selector, such as `[.name == "Alex DeLarge"]`.
This must be preceded by `.` if it is first in a path.
- The count selector, `#`, as in `.names#`
- The flatten selector, `^`. This flattens an array of arrays into a single array.
- The unique selector, `~`. This gets the unique values in an array.
- The sort selector, `$`. This sorts the values in an array.
- A JSON literal, which can be any arbitrary JSON, including objects and arrays.
- A parenthetical subquery, which can be any valid query.

A sequence of selectors is called a _path_ and is used to "drill down" into some JSON.

```
.people[.age >= 30].name#
```

This query counts the number of people over the age of 30.

Every node has a "truth value". Anything empty, such as an empty object,
array, or string, has a truth value of `false`. So does `0` and also `null`.
Everything else is `true`. (This is typical for JSON query languages.)

```
.adults[.children]
```

The query above filters the array of adults and includes only those that have
children, assuming that `children` is an array. If the array is `null` or empty,
the filter returns false for that adult, so it is excluded.

The filter selector cannot occur as the first element in a path. If it could,
we could not distinguish it from a JSON array literal. Thus, it must be preceded
by `.`, the current node selector.

```
.[. > 7]
```

If the JSON against which we're running the query is `[1, 3, 5, 7, 11, 13]`, the
query above will give us `[11, 13]`.

The subquery inside of a filter can be any valid query. It is run once per array
element and can include any construct.

The index selector selects an element from an array. If the current node is
not an array, it returns `null`.

```
.0
```

If the current JSON is an array, the query above selects the first element.
Negative numbers can be used to select from the end, with `-1` being the last element.

The JSON literal selector can _only_ appear as the first selector in a path. But
any other path construct can be added:

```
[12, 4, 9, 8][. <= 8]
```

This query ignores its input and always gives `[4, 8]`.

A more powerful construct is the pipe `|`. A pipe runs the path on its
left-hand side and uses it as the current node for the right-hand side.
This can make certain kinds of queries easier to write.

```
.dealerships.salesmen^ | .[.name == "Bob"]# == #
```

This query asks whether all the salesmen are named "Bob". If we don't use
a pipe, we have to ask it like this:

```
.dealerships.salesmen^# == .dealerships.salesmen^[.name == "Bob"]#
```

The pipe saves us from having to write `.dealerships.salesmen^` on both sides.
It's also slightly more computationally efficient.

This query can also be written with a parenthetical subquery, as so:

```
.dealerships.salesmen^.(.[.name == "Bob"]# == #)
```

This has the same performance as `|`, but is not as readable.
 */
internal sealed class JsonQuery {
    /**
     * Binary operators.
     *
     * Due to the way the parser works, binary operators are always left-associative.
     * There is no operator precedence. (Doing so is possible, but would require us
     * to first parse into a stream of tokens. Not worth it for this little language.)
     *
     * Use parentheses to group expressions unambiguously, e.g.,
     *
     * ```
     * ((. =@ {}) && (.name == "Bertrand")) || ((. =@ "") && (. == "Bertrand"))
     * ```
     */
    internal enum class Op(val op: String) {
        /**
         * Uses [jsonEquals] to determine the equality of two nodes.
         */
        EQ("==") {
            override fun eval(lhq: JsonQuery, rhq: JsonQuery, json: JsonElement): JsonElement =
                JsonPrimitive(lhq.eval(json) jsonEquals rhq.eval(json))
        },

        /**
         * Uses [jsonEquals] to determine the inequality of two nodes.
         */
        NE("!=") {
            override fun eval(lhq: JsonQuery, rhq: JsonQuery, json: JsonElement): JsonElement =
                JsonPrimitive(!(lhq.eval(json) jsonEquals rhq.eval(json)))
        },

        /**
         * Greater than. Uses [JsonSortDescriptor] to determine ranking.
         */
        GT(">") {
            override fun eval(lhq: JsonQuery, rhq: JsonQuery, json: JsonElement): JsonElement =
                JsonPrimitive(lhq.eval(json).sortDescriptor > rhq.eval(json).sortDescriptor)
        },

        /**
         * Greater than or equal to. Uses [JsonSortDescriptor] to determine ranking.
         */
        GTE(">=") {
            override fun eval(lhq: JsonQuery, rhq: JsonQuery, json: JsonElement): JsonElement =
                JsonPrimitive(lhq.eval(json).sortDescriptor >= rhq.eval(json).sortDescriptor)
        },

        /**
         * Less than. Uses [JsonSortDescriptor] to determine ranking.
         */
        LT("<") {
            override fun eval(lhq: JsonQuery, rhq: JsonQuery, json: JsonElement): JsonElement =
                JsonPrimitive(lhq.eval(json).sortDescriptor < rhq.eval(json).sortDescriptor)
        },

        /**
         * Less than or equal to. Uses [JsonSortDescriptor] to determine ranking.
         */
        LTE("<=") {
            override fun eval(lhq: JsonQuery, rhq: JsonQuery, json: JsonElement): JsonElement =
                JsonPrimitive(lhq.eval(json).sortDescriptor <= rhq.eval(json).sortDescriptor)
        },

        /**
         * Applies the path selector on the right-hand side to the JSON resolved on the
         * left-hand side.
         *
         * This is primarily useful in an edge case, when we have an array of arrays, such as
         * `[[7, 3, 9], [4, 2, 2]]`. If we want to get the first element of each sub-array,
         * we must say `. @ .0`. This gives `[7, 4]`. If we had said simply `.0`, the result
         * would be the first subarray, `[7, 3, 9]`.
         */
        MAP("@") {
            override fun eval(lhq: JsonQuery, rhq: JsonQuery, json: JsonElement): JsonElement {
                val lhj = lhq.eval(json)
                return if (lhj is JsonArray) {
                   JsonArray(lhj.map { rhq.eval(it) })
                } else {
                    rhq.eval(lhj)
                }
            }
        },

        /**
         * Returns true if the JSON type on the left-hand side is the same as the JSON type
         * on the right-hand side. The literal equality of the two sides is irrevelant.
         *
         * ```
         * . =@ []
         * ```
         * The code above asks "is the current node an array?".
         */
        TYPEQ("=@") {
            override fun eval(lhq: JsonQuery, rhq: JsonQuery, json: JsonElement): JsonElement =
                JsonPrimitive(lhq.eval(json) jsonTypeEquals rhq.eval(json))
        },

        /**
         * Negation of [TYPEQ].
         */
        NOTYPEQ("!=@") {
            override fun eval(lhq: JsonQuery, rhq: JsonQuery, json: JsonElement): JsonElement =
                JsonPrimitive(!(lhq.eval(json) jsonTypeEquals rhq.eval(json)))
        },

        /**
         * Logical `AND`. Uses the [truthValue] of the left- and right-hand sides.
         */
        AND("&&") {
            override fun eval(lhq: JsonQuery, rhq: JsonQuery, json: JsonElement): JsonElement =
                JsonPrimitive(lhq.eval(json).truthValue && rhq.eval(json).truthValue)
        },

        /**
         * Logical `OR`. Uses the [truthValue] of the left- and right-hand sides.
         */
        OR("||") {
            override fun eval(lhq: JsonQuery, rhq: JsonQuery, json: JsonElement): JsonElement =
                JsonPrimitive(lhq.eval(json).truthValue || rhq.eval(json).truthValue)
        };

        internal abstract fun eval(lhq: JsonQuery, rhq: JsonQuery, json: JsonElement): JsonElement
    }

    /**
    A path is an ordered sequence of queries, in which preceding queries
    provide the context for the evaluation of following ones.

    Some examples:

    ```
    .foo.bar.0
    .
    #
    ```

    Various grouping constructs for paths exist, such as juxtaposition,
    parentheses, and pipe characters.

    ```
    .salesmen | # == .[.name == "Bob"]#
    .salesmen.(# == .[.name == "Bob"]#)
    .salesmen(# == .[.name == "Bob"]#)
    ```

    Shown above are three ways to write the same query. The first
    is preferred. The expression after the pipe (or in parentheses)
    is evaluated with the result of `.salesmen` as the current node,
    so this query asks whether all of the salesmen are named Bob.
     */
    internal class Path(private val queries: List<JsonQuery>): JsonQuery() {
        @Suppress("NAME_SHADOWING")
        override fun eval(json: JsonElement): JsonElement =
            queries.fold(json) { json, query -> query.eval(json) }
        override fun toString(): String =
            "Path(${queries.joinToString(".") { it.toString() }})"
    }

    /**
    An expression that evaluates to `true` or `false`.

    ```
    .salesmen.sales# > 3
    .[(. > 3) && (. < 99)]
    ```
     */
    internal class Logic(
        private val lhs: JsonQuery,
        private val op: Op,
        private val rhs: JsonQuery
    ): JsonQuery() {
        override fun eval(json: JsonElement): JsonElement =
            op.eval(lhs, rhs, json)
        override fun toString(): String =
            "Logic(${lhs}${op.op}${rhs})"
    }

    /**
    Gets the value of a key.

    ```
    .foo
    ```
     */
    internal class Key(private val key: String): JsonQuery() {
        override fun eval(json: JsonElement): JsonElement =
            when (json) {
                is JsonObject -> json[key] ?: JsonNull
                is JsonArray -> JsonArray(json.map {
                    if (it is JsonObject) {
                        it[key] ?: JsonNull
                    } else {
                        JsonNull
                    }
                })
                else -> JsonNull
            }
        override fun toString(): String = "Key(\"$key\")"
    }

    /**
    Gets the value of an index in an array. (Zero-based.)

    If negative, indexes from the end.

    ```
    .0
    ```
    If position is out of range, `JsonNull` is returned.
     */
    internal class Index(private val position: Int): JsonQuery() {
        override fun eval(json: JsonElement): JsonElement =
            if (json is JsonArray) {
               val modifier = if (position >= 0) 0 else json.size
               val index = modifier + position
               if (index in 0 until json.size) {
                   json[index]
               } else {
                   JsonNull
               }
            } else JsonNull
        override fun toString(): String = "Index($position)"
    }

    /**
    The current node. Expressed by `.`.

    This is mostly useful when filtering an array:

    ```
    .numbers[. > 3]
    ```
     */
    internal object Current: JsonQuery() {
        override fun eval(json: JsonElement): JsonElement = json
        override fun toString(): String = "Current"
    }
    internal class Not(private val query: JsonQuery): JsonQuery() {
        override fun eval(json: JsonElement): JsonElement =
           JsonPrimitive(!query.eval(json).truthValue)
        override fun toString(): String = "Not($query)"
    }
    /**
    Counts the number of elements in the current JSON, if
    it is a dictionary or array.

    For any other type, if its `truthValue` is `true`, returns
    `1`, otherwise `0`.

    Represented by the symbol `#`.
     */
    internal object Count: JsonQuery() {
        override fun eval(json: JsonElement): JsonElement =
            JsonPrimitive(
                when (json) {
                    is JsonArray -> json.size
                    is JsonObject -> json.size
                    else -> if (json.truthValue) 1 else 0
                }
            )
        override fun toString(): String = "Count"
    }
    /**
    Flattens an array of arrays. For any other node type,
    does nothing.

    Represented by the symbol `^`.
     */
    internal object Flatten: JsonQuery() {
        override fun eval(json: JsonElement): JsonElement =
            if (json is JsonArray) {
                val result: MutableList<JsonElement> = mutableListOf()
                for (elem in json) {
                    if (elem is JsonArray) {
                        result += elem.toList()
                    } else {
                        result.add(elem)
                    }
                }
                JsonArray(result)
            } else json
        override fun toString(): String = "Flatten"
    }
    /**
    Returns the unique values in an array. Does not
    preserve order. For any other node type, does nothing.

    Represented by the symbol `~`.
     */
    internal object Unique: JsonQuery() {
        override fun eval(json: JsonElement): JsonElement =
            if (json is JsonArray) JsonArray(json.distinct()) else json
        override fun toString(): String = "Unique"
    }
    /**
    Sorts an array. For any other node type, does nothing.

    Represented by the symbol `$`.
     */
    internal object Sort: JsonQuery() {
        override fun eval(json: JsonElement): JsonElement =
            if (json is JsonArray) JsonArray(json.sortedBy { it.sortDescriptor }) else json
        override fun toString(): String = "Sort"
    }
    /**
    Executes a filter using the given subquery.

    If the target node is an array, the filter returns the array
    filtered according to the subquery. For any other node,
    the node itself is returned if the subquery's `truthValue` is
    `true`. Otherwise `null` is returned.

    ```
    .recipes[.ingredients[. == "paprika"].#]
    ```

    A filter and a literal JSON array both begin with `[`. The difference
    is that a filter begins with `.`, which can only be omitted if the filter
    is not the first element in a path. A literal JSON array must be the first
    element in a path.

    ```
    .ingredients[. == "paprika"] // filter (syntax sugar)
    .ingredients.[. == "paprika"] // filter (full form)
    [2, 3, 4] // literal JSON array
    .[.name == "Bob"] // filter
    .[2, 3, 4] // syntax error
    ```
     */
    internal class Filter(val filter: JsonQuery): JsonQuery() {
        override fun eval(json: JsonElement): JsonElement =
            if (json is JsonArray) {
                JsonArray(json.filter { filter.eval(it).truthValue })
            } else if (filter.eval(json).truthValue) {
                json
            } else {
                JsonNull
            }
        override fun toString(): String = "Filter($filter)"
    }
    /**
    Literal JSON.
     */
    internal class Json(val json: JsonElement): JsonQuery() {
        override fun eval(json: JsonElement): JsonElement = this.json
        override fun toString(): String = "Json($json)"
    }

    internal abstract fun eval(json: JsonElement): JsonElement
}