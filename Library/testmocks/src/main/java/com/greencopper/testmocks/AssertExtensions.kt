package com.greencopper.testmocks

import com.toggl.komposable.architecture.Effect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import org.assertj.core.api.Assertions.assertThat

public infix fun <T> T.shouldBe(other: T) {
    assertThat(this).isEqualTo(other)
}

public fun <T : Any> T?.assertNotNull(): T {
    assertThat(this).isNotNull
    return this as T
}

public suspend infix fun <T : Any> Effect<T>.shouldBeAction(other: T) {
    val flowActions = run()
    val actions = flowActions.take(1).toList()

    assertThat(actions).containsExactly(other)
}

public suspend infix fun <T : Any> Effect<T>.shouldBeActions(others: Array<T>) {
    val flowActions = run()
    val actions = flowActions.take(others.size).toList()

    assertThat(actions).containsExactlyInAnyOrder(*others)
}
