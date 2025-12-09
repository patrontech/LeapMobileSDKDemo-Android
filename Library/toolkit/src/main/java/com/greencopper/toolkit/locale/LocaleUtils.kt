package com.greencopper.toolkit.locale

import android.os.LocaleList
import java.util.*

public fun LocaleList.toList(): List<Locale> =
    toLanguageTags().split(",").map {
        it.toLocale()
    }

public fun List<Locale>.toLocaleList(): LocaleList {
    return LocaleList(*this.toTypedArray())
}

public fun String.toLocale(): Locale = Locale.forLanguageTag(this)
