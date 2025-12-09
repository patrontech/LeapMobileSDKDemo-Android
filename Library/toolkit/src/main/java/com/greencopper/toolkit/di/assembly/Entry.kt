package com.greencopper.toolkit.di.assembly

internal class Entry(internal val assembly: Assembly) {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is Entry) return false
        return assembly::class == other.assembly::class
    }

    override fun hashCode(): Int = assembly::class.hashCode()
}