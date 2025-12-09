package com.greencopper.core.conditions

import com.greencopper.core.conditions.parser.*
import com.greencopper.parsimonious.ParseException
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockBuildConfigProvider
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ComplexPredicateParserTest {
    private val buildConfigProvider = MockBuildConfigProvider(mockIsDebug = false)
    private val predicateParser = ComplexPredicateParser(buildConfigProvider)

    init {
        Toolkit.setupTest()
    }

    @Test
    fun testComplexPredicate() {
        val text = "   NOT (registered OR (edible)) AND    peeved AND NOT (purple OR (green AND NOT    tall)  )  "
        val predicate = predicateParser.parse(text)
        assertThat(predicate).isInstanceOf(Logic::class.java)
        assertThat(predicate).isEqualTo(
            Logic(
                Logic(
                    Not(Logic(Id("registered"), Op.OR, Id("edible"))),
                    Op.AND,
                    Id("peeved")
                ),
                Op.AND,
                Not(
                    Logic(
                        Id("purple"),
                        Op.OR,
                        Logic(Id("green"), Op.AND, Not(Id("tall")))
                    )
                )
            )
        )
    }

    @Test
    fun testSimplePredicate() {
        val predicate = predicateParser.parse("condition")
        assertThat(predicate).isInstanceOf(Id::class.java)
        assertThat(predicate).isEqualTo(Id("condition"))
    }

    @Test
    fun notDebug_failingPredicate_returnsFailing() {
        val predicate = predicateParser.parse("??? and ???")
        assertThat(predicate).isInstanceOf(Failing::class.java)

        assertThat(predicate.check(mapOf())).isFalse
        runTest {
            assertThat(predicate.checkFlow(mapOf()).first()).isFalse
        }
    }

    @Test
    fun isDebug_testFailingPredicate_throws() {
        buildConfigProvider.mockIsDebug = true
        assertThrows<ParseException> {
            predicateParser.parse("??? and ???")
        }
    }
}