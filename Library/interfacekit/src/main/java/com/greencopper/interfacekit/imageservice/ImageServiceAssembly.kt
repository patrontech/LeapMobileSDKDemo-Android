package com.greencopper.interfacekit.imageservice

import android.content.Context
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.bindProvider
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

internal class ImageServiceAssembly : Assembly {

    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindProvider<ImageService> {
                ConcreteImageService(
                    resolve(),
                    resolve<Context>(),
                    resolve(),
                    resolve(),
                    resolve(),
                    resolve(),
                    CoroutineScope(
                        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()).asCoroutineDispatcher()
                    ),
                )
            }
        }
    }
}
