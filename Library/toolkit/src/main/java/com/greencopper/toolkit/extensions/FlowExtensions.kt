package com.greencopper.toolkit.extensions

import kotlinx.coroutines.flow.*

// todo put this somewhere else
public inline fun <reified T> Throwable.mapErrorNotType(mapError: (Throwable) -> Throwable): Throwable {
    if (this is T) {
        return this
    } else {
        return mapError(this)
    }
}

/**
 * Map [Throwable] if not of type [T]. Re-throw if correct type.
 */
public inline fun <T, reified E> Flow<T>.mapErrorNotType(crossinline mapError: (Throwable) -> Unit): Flow<T> =
    catch {
        if (it is E) {
            throw it
        } else {
            mapError(it)
        }
    }

/**
 * Collect [Flow], [onError] will be called in case of error
 * during execution and [onSuccess] will be called otherwise
 */
public suspend fun <T> Flow<T>.safeCollect(
    onError: suspend FlowCollector<T>.(Throwable) -> Unit,
    onSuccess: suspend (T) -> Unit,
) {
    catch(onError).collect(onSuccess)
}

/**
 * Collect [Flow], [onError] will be called in case of error
 * during execution and nothing otherwise
 */
public suspend fun <T> Flow<T>.collectError(
    onError: suspend FlowCollector<T>.(Throwable) -> Unit,
) {
    catch(onError).collect {}
}

public fun <T> zipMany(flows: List<Flow<T>>): Flow<List<T>> {
    var listFlow = flowOf(emptyList<T>())

    flows.forEach { flow ->
        listFlow = listFlow.zip(flow) { listValue, zipValue -> listValue + zipValue }
    }

    return listFlow
}

/**
 * Return a flow of insertions and deletions in a collection
 * @return A [Differences] containing lists of insertions and deletions
 */
public fun <T : Collection<R>, R : Any> Flow<T>.difference(): Flow<Differences<R>> =
    runningFold(Pair<T?, T?>(null, null)) { accumulator, value ->
        accumulator.second to value
    }.map {
        val new = it.second ?: return@map null
        val previous = it.first ?: return@map null
        val insertions = new.minus(previous)
        val deletions = previous.minus(new)
        Differences(insertions, deletions).takeIf { insertions.isNotEmpty() || deletions.isNotEmpty() }
    }.filterNotNull()

public data class Differences<T : Any>(
    val insertions: List<T>,
    val deletions: List<T>,
)

/**
 * Return a flow emitting insertions in a Collection
 */
public fun <T : Collection<R>, R : Any> Flow<T>.insertions(): Flow<List<R>> =
    difference()
        .mapNotNull { it.insertions.takeIf { insertions -> insertions.isNotEmpty() } }

/**
 * Return a flow emitting deletions in a Collection
 */
public fun <T : Collection<R>, R : Any> Flow<T>.deletions(): Flow<List<R>> =
    difference()
        .mapNotNull {
            it.deletions.takeIf { deletions -> deletions.isNotEmpty() }
        }
