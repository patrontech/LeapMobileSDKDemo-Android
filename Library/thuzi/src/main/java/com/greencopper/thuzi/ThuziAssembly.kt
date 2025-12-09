package com.greencopper.thuzi

import android.content.Context
import android.os.Build
import com.greencopper.core.content.manager.ContentManager
import com.greencopper.thuzi.account.AccountAssembly
import com.greencopper.thuzi.account.DeviceSessionManager
import com.greencopper.thuzi.account.deletion.AccountDeletionAssembly
import com.greencopper.thuzi.account.registration.manager.logout.ThuziLogoutManager
import com.greencopper.thuzi.badges.BadgesAssembly
import com.greencopper.thuzi.conditions.ConditionsAssembly
import com.greencopper.thuzi.eventpass.EventPassAssembly
import com.greencopper.thuzi.fanscan.FanscanAssembly
import com.greencopper.thuzi.logout.LogoutAssembly
import com.greencopper.thuzi.microsite.MicrositeAssembly
import com.greencopper.thuzi.okhttp.MobileAgent
import com.greencopper.thuzi.okhttp.MobileAgentInterceptor
import com.greencopper.thuzi.services.ServicesAssembly
import com.greencopper.thuzi.services.attendee.AttendeeService
import com.greencopper.thuzi.survey.SurveyAssembly
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.bindSingleton
import com.greencopper.toolkit.di.resolver.Resolver
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.logging.e
import com.greencopper.toolkit.versionprovider.BuildConfigProvider
import okhttp3.OkHttpClient
import retrofit2.Retrofit

public class ThuziAssembly : Assembly {

    public companion object {
        internal const val thuziDirectory = "thuzi"
    }

    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindAssembly(AccountAssembly())
            bindAssembly(MicrositeAssembly())
            bindAssembly(FanscanAssembly())
            bindAssembly(BadgesAssembly())
            bindAssembly(EventPassAssembly())
            bindAssembly(ServicesAssembly())
            bindAssembly(ConditionsAssembly())
            bindAssembly(SurveyAssembly())
            bindAssembly(AccountDeletionAssembly())
            bindAssembly(LogoutAssembly())

            bindSingleton<MobileAgent> {
                val context = resolve<Context>()
                val pm = context.packageManager
                val versionName = try {
                    pm.getPackageInfo(context.packageName, 0).versionName.orEmpty()
                } catch (e: Throwable) {
                    App.log.e("Error resolving package info", "ThuziAssembly", e)
                    "unknownName"
                }

                MobileAgent(
                    versionName = versionName,
                    packageId = context.packageName,
                    contentVersion = { resolve<ContentManager>().currentContent?.version?.toString() ?: "unknownVersion" },
                    osVersion = Build.VERSION.RELEASE,
                    libraryVersion = { resolve<BuildConfigProvider>().libraryVersion }
                )
            }

            bindSingleton<ThuziAPI> {
                resolve<Retrofit>().newBuilder()
                    .client(
                        resolve<OkHttpClient>()
                            .newBuilder()
                            .addInterceptor(MobileAgentInterceptor(resolve<MobileAgent>()))
                            .build()
                    )
                    .build().create(ThuziAPI::class.java)
            }
        }
    }

    override fun onBindingsRegistered(resolver: Resolver) {
        with (resolver) {
            resolve<DeviceSessionManager>()
            resolve<AttendeeService>()
            resolve<ThuziLogoutManager>()
        }
    }
}
