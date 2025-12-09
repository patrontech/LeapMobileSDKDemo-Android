package com.greencopper.core.data

import android.os.Bundle
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.logging.e
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

/**
 * Kotlin serialization requires knowing the class type at compile time. Extending this class
 * allows us to get around this by making every child class provide its KSerializer. 
 */
public interface KiboSerializable<T : KiboSerializable<T>> {
    // You can annotate your class with @Serializable and you should access to a static serializer() then to set here
    public fun getSerializer(): KSerializer<T>
    public fun jsonProvider(): Json = App.resolve()

    @Suppress("UNCHECKED_CAST")
    @Throws(SerializationException::class, ClassCastException::class)
    public fun encodeToString(): String {
        return try {
            jsonProvider().encodeToString(getSerializer(), this as T)
        } catch (serializationException: SerializationException) {
            App.log.e("String encoding of ${this::class.java} went wrong: ${serializationException.message}")
            throw serializationException
        } catch (classCastException: ClassCastException) {
            App.log.e("String encoding of ${this::class.java} went wrong: ${classCastException.message}")
            throw classCastException
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(SerializationException::class, ClassCastException::class)
    public fun encodeToJsonElement(): JsonElement {
        return try {
            jsonProvider().encodeToJsonElement(getSerializer(), this as T)
        } catch (serializationException: SerializationException) {
            App.log.e("JsonElement encoding of ${this::class.java} went wrong: ${serializationException.message}")
            throw serializationException
        } catch (classCastException: ClassCastException) {
            App.log.e("JsonElement encoding of ${this::class.java} went wrong: ${classCastException.message}")
            throw classCastException
        }
    }

    public companion object {
        @Throws(SerializationException::class, NullPointerException::class)
        public inline fun <reified T> decodeFromString(string: String): T {
            return decodeFromString(string, App.resolve())
        }

        @Throws(SerializationException::class)
        public inline fun <reified T> decodeFromString(string: String, json: Json): T {
            return try {
                json.decodeFromString(string)
            } catch (serializationException: SerializationException) {
                App.log.e("String decoding of ${this::class.java} went wrong: ${serializationException.message}")
                throw serializationException
            }
        }

        @Throws(SerializationException::class, NullPointerException::class)
        public inline fun <reified T> decodeFromJsonElement(jsonElement: JsonElement): T {
            return decodeFromJsonElement(jsonElement, App.resolve())
        }

        @Throws(SerializationException::class)
        public inline fun <reified T> decodeFromJsonElement(
            jsonElement: JsonElement,
            json: Json
        ): T {
            return try {
                json.decodeFromJsonElement(jsonElement)
            } catch (serializationException: SerializationException) {
                App.log.e("JsonElement decoding of ${this::class.java} went wrong: ${serializationException.message}")
                throw serializationException
            }
        }
    }
}

public fun <T : KiboSerializable<T>> Bundle.putKiboSerializable(
    key: String,
    serializable: T?
): Bundle {
    putString(key, serializable?.encodeToString())
    return this
}

public inline fun <reified T : KiboSerializable<T>> Bundle.getKiboSerializable(key: String): T? {
    return getString(key)?.let {
        KiboSerializable.decodeFromString(it)
    }
}

public inline fun <reified T: KiboSerializable<T>> List<T>.encodeToString(): String {
    val json = App.resolve<Json>()
    return json.encodeToString( this)
}

public inline fun <reified T: KiboSerializable<T>> Set<T>.encodeToString(): String {
    val json = App.resolve<Json>()
    return json.encodeToString( this)
}

public inline fun <reified T: KiboSerializable<T>> Map<String, T>.encodeToString(): String {
    val json = App.resolve<Json>()
    return json.encodeToString( this)
}

@Throws(IOException::class, FileNotFoundException::class, SecurityException::class)
public inline fun <reified T : KiboSerializable<T>> KiboSerializable<T>.writeToPath(path: File) {
    path.writeText(encodeToString())
}
