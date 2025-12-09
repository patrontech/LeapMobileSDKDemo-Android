package com.greencopper.toolkit.di.resolver

import com.greencopper.toolkit.di.container.Key

@PublishedApi
internal fun <T> Pair<Key, T?>.getInstance(): T = this.second ?: throw ResolveException(this.first)