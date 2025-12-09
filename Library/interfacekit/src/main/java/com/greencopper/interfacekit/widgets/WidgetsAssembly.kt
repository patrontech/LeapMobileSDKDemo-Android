package com.greencopper.interfacekit.widgets

import com.greencopper.interfacekit.bindViewModel
import com.greencopper.interfacekit.navigation.feature.bindFeature
import com.greencopper.interfacekit.widgets.initializer.*
import com.greencopper.interfacekit.widgets.recipe.WidgetCollectionRecipe
import com.greencopper.interfacekit.widgets.recipe.WidgetCollectionRecipeOverride
import com.greencopper.interfacekit.widgets.resolver.ConcreteWidgetResolver
import com.greencopper.interfacekit.widgets.resolver.WidgetCollectionResolver
import com.greencopper.interfacekit.widgets.resolver.WidgetResolver
import com.greencopper.interfacekit.widgets.viewmodel.widgetcollection.WidgetCollectionViewModel
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.*
import com.greencopper.toolkit.di.resolver.resolve

internal class WidgetsAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindSingleton { WidgetCollectionConfigurationHolder() }
            bindRecipe(auto(::WidgetCollectionRecipe))
            bindRecipeOverride(auto(::WidgetCollectionRecipeOverride))
            bindProvider<WidgetResolver> { ConcreteWidgetResolver(this) }
            bindProvider { WidgetCollectionResolver(resolve()) }
            bindViewModel(auto(::WidgetCollectionViewModel))
            bindFeature(WidgetCollectionInitializer.key, auto(::WidgetCollectionInitializer))
            bindWidget(ButtonWidgetInitializer.key, auto(::ButtonWidgetInitializer))
            bindWidget(ButtonWidgetV2Initializer.key, auto(::ButtonWidgetV2Initializer))
            bindWidget(FullWidthImageWidgetInitializer.key, auto(::FullWidthImageWidgetInitializer))
            bindWidget(ImageWidgetInitializer.key, auto(::ImageWidgetInitializer))
            bindWidget(TitleSubtitleWidgetInitializer.key, auto(::TitleSubtitleWidgetInitializer))
            bindWidget(LinksCollectionWidgetInitializer.key, auto(::LinksCollectionWidgetInitializer))
            bindWidget(TextWidgetInitializer.key, auto(::TextWidgetInitializer))
            bindWidget(CardAdWidgetInitializer.key, auto(::CardAdWidgetInitializer))
            bindWidget(ImageCollectionWidgetInitializer.key, auto(::ImageCollectionWidgetInitializer))
            bindWidget(ImageWithLabelWidgetInitializer.key, auto(::ImageWithLabelWidgetInitializer))
            bindWidget(TextOnImageWidgetInitializer.key, auto(::TextOnImageWidgetInitializer))
            bindWidget(TitleCounterWidgetInitializer.key, auto(::TitleCounterWidgetInitializer))
            bindWidget(SettingWidgetInitializer.key, auto(::SettingWidgetInitializer))
            bindWidget(DebugInfoWidgetInitializer.key, auto(::DebugInfoWidgetInitializer))
            bindWidget(AccountSummaryWidgetInitializer.key, auto(::AccountSummaryWidgetInitializer))
            bindWidget(UnregisteredAccountWidgetInitializer.key, auto(::UnregisteredAccountWidgetInitializer))
            bindWidget(AccountProfileWidgetInitializer.key, auto(::AccountProfileWidgetInitializer))
            bindWidget(CountdownWidgetInitializer.key, auto(::CountdownWidgetInitializer))
            bindWidget(CardCollectionWidgetInitializer.key, auto(::CardCollectionWidgetInitializer))
            bindWidget(BannerWidgetInitializer.key, auto(::BannerWidgetInitializer))
            bindWidget(FullWidthImageCarouselWidgetInitializer.key, (auto(::FullWidthImageCarouselWidgetInitializer)))
        }
    }
}
