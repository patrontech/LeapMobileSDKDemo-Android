package com.greencopper.interfacekit.ui.fragment

import androidx.core.os.bundleOf
import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.extensions.lazyVar

@Suppress("BaseFragment", "DEPRECATION")
public abstract class ParameterizedFragment<T : KiboSerializable<T>>(private var constructorData: T?) :
    BaseFragment() {

    private val layoutDataProvider: LayoutDataProvider
        get() = App.resolve()

    protected var data: T by lazyVar {
        constructorData ?: run {
            val dataHashcode = arguments?.getInt(keyArguments) ?: return@run null
            layoutDataProvider.getLayoutData(dataHashcode, ::restoreData)
        }
        ?: throw IllegalArgumentException("Arguments, $arguments from ${this::class.java} should have an element at key: $keyArguments")
    }

    init {
        checkProvideEmptyConstructor()
        constructorData?.let { constructorData ->
            data = constructorData
            val hashCode = constructorData.hashCode()
            layoutDataProvider.addLayoutData(hashCode, constructorData)

            if (arguments == null) {
                arguments = bundleOf()
            }
            arguments?.putInt(keyArguments, hashCode)
        }
    }

    protected abstract fun restoreData(encodedData: String): T

    /** Helper function for children classes that mutualizes the data restauration logic.
     * Most classes in InterfaceKit don't use it because it's a one-line logic but it might be used
     * in other modules to hide the KiboSerializable implementation detail.
     **/
    protected inline fun <reified T : KiboSerializable<T>> decodeData(encodedData: String): T {
        return KiboSerializable.decodeFromString(encodedData)
    }

    public companion object {
        public const val keyArguments: String = "FragmentLayoutDataKeyArguments"
    }
}
