package com.greencopper.interfacekit.favorites

public interface Favoriteable<T> {
    public val itemId: T
}

public fun <T> T.toFavoriteable(): Favoriteable<T> = object : Favoriteable<T> {
    override val itemId: T = this@toFavoriteable
}
