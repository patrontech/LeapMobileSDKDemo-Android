package com.greencopper.toolkit

import android.content.Context
import com.greencopper.toolkit.appinstance.AppInstance
import com.greencopper.toolkit.appinstance.ConcreteAppInstance
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.logging.multilogging.LoggingConfiguration

public lateinit var App: AppInstance

public object Toolkit {
    /**
     * Setup dependency injection with a list of [Assembly] and logging with a list of [LoggingConfiguration]
     * The context is optional in the case of testing but will restrict access to some dependencies
     */
    public fun setup(
        assemblies: List<Assembly>,
        loggingConfigurations: List<LoggingConfiguration>,
        applicationContext: Context?
    ) {
        val app = ConcreteAppInstance.create(assemblies, loggingConfigurations, applicationContext)
        // We have to set this before we call onBindingsRegistered,
        // otherwise we'll get a NullPointerException when something
        // calls App.resolve().
        App = app
        app.assemble()
    }
}