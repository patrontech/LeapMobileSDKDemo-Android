package com.greencopper.toolkit.di.binding

import com.greencopper.toolkit.di.resolver.Resolver

public typealias Creator<T> = Resolver.(Params) -> T