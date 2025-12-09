package com.greencopper.core.localstorage

import kotlin.text.Regex
import kotlin.text.RegexOption

public class LocalStorageName(private val name: String) {
    public companion object {
        internal const val ELEMENT_PATTERN = "[0-9a-z_-]+"
        private val ELEMENT_REGEX = Regex("^(?:@|%|${ELEMENT_PATTERN})$", RegexOption.IGNORE_CASE)
    }

    init {
        if (!ELEMENT_REGEX.matches(name))
            throw IllegalArgumentException("$name is not a valid LocalStorageName.")
    }

    public override fun toString(): String = name

    public override fun equals(other: Any?): Boolean = this === other || other?.let {
        it is LocalStorageName && it.name == name
    } ?: false

    public override fun hashCode(): Int = name.hashCode()
}