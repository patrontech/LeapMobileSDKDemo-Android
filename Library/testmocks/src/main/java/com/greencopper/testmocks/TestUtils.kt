package com.greencopper.testmocks

import android.content.Context
import android.os.Bundle
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localstorage.*
import com.greencopper.core.localstorage.LocalStorageJsonFactory.Companion.LOCAL_STORAGE_TAG
import com.greencopper.interfacekit.accountprovider.AccountProvider
import com.greencopper.interfacekit.bindAccountProvider
import com.greencopper.interfacekit.bindCounter
import com.greencopper.interfacekit.commands.system.*
import com.greencopper.interfacekit.counter.Counter
import com.greencopper.testmocks.toolkit.MockBuildConfigProvider
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.binding.*
import com.greencopper.toolkit.di.container.Key
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.logging.multilogging.LoggingConfiguration
import com.greencopper.toolkit.versionprovider.BuildConfigProvider
import io.mockk.every
import io.mockk.mockkConstructor
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertTimeoutPreemptively
import java.time.Duration

public inline fun <reified T : Any> mockAppResolve(mock: T?) {
    every {
        App.resolve(T::class, any(), any())
    } returns Pair(
        Key(T::class, Unit), mock
    )
}

public fun mockBundleConstructor() {
    mockkConstructor(Bundle::class)
    every { anyConstructed<Bundle>().putString(any(), any()) } returns Unit
    every { anyConstructed<Bundle>().putInt(any(), any()) } returns Unit
}

public fun Toolkit.setupTest(
    loggingConfigurations: List<LoggingConfiguration> = listOf(),
    applicationContext: Context? = null
) {
    setup(listOf(), loggingConfigurations, applicationContext)

    bindSingleton(LocalStorageJsonFactory.create(), LOCAL_STORAGE_TAG)
    bindProvider(LocalStorage("test", TestLocalStorageContainer()))
    bindSingleton<BuildConfigProvider>(MockBuildConfigProvider())
}

public inline fun <reified T : Any> bindProvider(mock: T, tag: Any = Unit) {
    App.resolve<Registrar>().bindProvider(tag = tag) { mock }
}

public inline fun <reified T : Any> bindSingleton(mock: T, tag: Any = Unit) {
    App.resolve<Registrar>().bindSingleton(tag = tag) { mock }
}

public inline fun <reified T : Command> bindCommand(
    key: CommandInfo.Key,
    noinline creator: Creator<T>,
): Key = App.resolve<Registrar>().bindCommand<Command>(key, creator)

public inline fun <reified T : Counter<*>> bindCounter(
    key: Counter.Key,
    noinline creator: Creator<T>,
): Key = App.resolve<Registrar>().bindCounter<Counter<*>>(key, creator)


public inline fun <reified T : AccountProvider> bindAccountProvider(
    key: AccountProvider.Key,
    noinline creator: Creator<T>,
): Key = App.resolve<Registrar>().bindAccountProvider<AccountProvider>(key, creator)

/**
 * Wait a set duration for a condition to eventually be true
 *
 * @param duration Most time to wait for condition to be true, in milliseconds
 * @param failureMessage Message to display if failure
 * @param condition Condition that is eventually true
 */
public fun runUntilEventually(duration: Duration, failureMessage: String, condition: () -> Boolean) {
    assertTimeoutPreemptively(duration, failureMessage) {
        while (!condition()) {
            // Wait for condition to change
        }
    }
}

/**
 * Wait a set duration for a condition to eventually succeed
 *
 * @param duration Most time to wait for condition to be true, in milliseconds
 * @param failureMessage Message to display if failure
 * @param function Function that eventually succeeds
 */
public fun tryUntilEventually(duration: Duration, failureMessage: String = "", function: () -> Unit) {
    var message = failureMessage
    runUntilEventually(duration, message) {
        try {
            function()
            true
        } catch (e: Throwable) {
            if (message == failureMessage) {
                message += e.message ?: "Error timed out ${e.stackTrace}"
            }
            false
        }
    }
}

public inline fun <reified T> testSerializable(
    entity: T,
    serializer: SerializationStrategy<T>? = null,
    deserializer: DeserializationStrategy<T>? = null,
) {
    val json = App.resolve<Json>()
    val serializedEntity =
        serializer?.let { json.encodeToString(it, entity) } ?: json.encodeToString(entity)
    val deserializedEntity =
        deserializer?.let { json.decodeFromString(deserializer, serializedEntity) }
            ?: json.decodeFromString<T>(serializedEntity)
    assertThat(entity).usingRecursiveComparison().isEqualTo(deserializedEntity)
}

public inline fun <reified E, T : KiboSerializable<E>> testKiboSerializable(
    entity: T,
) {
    val serializedEntity = entity.encodeToString()
    val deserializedEntity = KiboSerializable.decodeFromString<E>(serializedEntity)
    assertThat(entity).usingRecursiveComparison().isEqualTo(deserializedEntity)
}
