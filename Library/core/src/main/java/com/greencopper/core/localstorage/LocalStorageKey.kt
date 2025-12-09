package com.greencopper.core.localstorage

public class LocalStorageKey(private val key: String) {
    public enum class RootType {
        /**
         * The root is @.
         */
        APP,
        /**
         * The root is ~.
         */
        CURRENT_PROJECT,
        /**
         * The root is a specific, named project
         */
        NAMED_PROJECT,
        /**
         * The root is %, used for computed values.
         */
        COMPUTED
    }

    public companion object {
        public const val SEPARATOR: String = "/"
        private const val ELEMENT_PATTERN = LocalStorageName.ELEMENT_PATTERN
        internal const val KEY_PATTERN = "(?:@|%|~|$ELEMENT_PATTERN)(?:$SEPARATOR$ELEMENT_PATTERN)*"
        private val KEY_REGEX = Regex("^${KEY_PATTERN}$", RegexOption.IGNORE_CASE)
    }

    public constructor(name: LocalStorageName): this("$name")

    init {
        if (!KEY_REGEX.matches(key))
            throw IllegalArgumentException("$key is not a valid LocalStorageKey.")
    }

    public val root: String
        get() = key.split(SEPARATOR).first()

    public val rootType: RootType
        get() = when (key[0]) {
            '@' -> RootType.APP
            '~' -> RootType.CURRENT_PROJECT
            '%' -> RootType.COMPUTED
            else -> RootType.NAMED_PROJECT
        }

    /**
     * Returns a new key based on the receiver. If the root type
     * is RootType.APP, no change is made, even if `force` is set
     * to true. If the root type is RootType.NAMED_PROJECT, no change
     * is made unless `force` is set to `true`. If the root type is
     * `CURRENT_PROJECT`, the change is always made.
     *
     * The main purpose of this is to change keys of type `~/foo/bar`
     * to have a specific, named project so that they can be resolved.
     */
    public fun inProject(project: String, force: Boolean = false): LocalStorageKey {
        if (!arrayOf(RootType.NAMED_PROJECT, RootType.CURRENT_PROJECT).contains(rootType))
            return this
        if (!force && rootType == RootType.NAMED_PROJECT) return this
        val parts = key.split(SEPARATOR).toMutableList()
        parts[0] = project
        return LocalStorageKey(parts.joinToString(SEPARATOR))
    }

    public override fun toString(): String = key

    public override fun equals(other: Any?): Boolean = this === other || other?.let {
        it is LocalStorageKey && it.key == key
    } ?: false

    public override fun hashCode(): Int = key.hashCode()

    public operator fun div(element: String): LocalStorageKey =
        LocalStorageKey("$key$SEPARATOR$element")

    public operator fun div(name: LocalStorageName): LocalStorageKey =
        LocalStorageKey("$key$SEPARATOR$name")
}
