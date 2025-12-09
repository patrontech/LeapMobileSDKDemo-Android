package com.greencopper.interfacekit.appreview

import com.google.android.play.core.review.ReviewManagerFactory
import com.greencopper.core.conditions.conditionchecker.bindCondition
import com.greencopper.interfacekit.appreview.commands.RequestAppReviewCommand
import com.greencopper.interfacekit.appreview.conditions.CanRequestAppReviewCondition
import com.greencopper.interfacekit.commands.system.bindCommand
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.auto
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

public class AppReviewAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindCommand(RequestAppReviewCommand.key) {
                RequestAppReviewCommand(
                    resolve(),
                    resolve(),
                    ReviewManagerFactory.create(resolve()),
                    CoroutineScope(Dispatchers.IO),
                )
            }

            bindCondition(CanRequestAppReviewCondition.key, auto(::CanRequestAppReviewCondition))
        }
    }
}
