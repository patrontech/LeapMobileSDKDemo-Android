package com.greencopper.interfacekit.multiproject

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.multiproject.ui.ProjectSwitcherFragment
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.feature.ParameterizedFeatureInitializer
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.onboarding.initializers.OnboardingPageInitializer
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayout
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayoutData
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

internal class ProjectSwitcherInitializer : ParameterizedFeatureInitializer<ProjectSwitcherData>(),
    OnboardingPageInitializer {

    companion object {
        val key: FeatureKey = FeatureKey("InterfaceKit.ProjectSwitcher", 1)
    }

    override val featureKey: FeatureKey = key

    override fun layoutForParams(params: ProjectSwitcherData): Layout =
        ProjectSwitcherFragment(params.toProjectSwitcherLayoutData("NotAnOnboardingPage"))

    override fun redirectionHashForParams(params: ProjectSwitcherData): RedirectionHash {
        return params
            .toRedirectionHash(key)
    }

    override fun resolve(params: JsonElement?, pageId: String): OnboardingPageLayout =
        ProjectSwitcherFragment(
            params?.let { decodeParams(it).toProjectSwitcherLayoutData(pageId) }
                ?: throw IllegalStateException("No parameters provided for ProjectSwitcher onboarding screen"))

    override fun decodeParams(params: FeatureParams): ProjectSwitcherData {
        val projectSwitcherParams = try {
            params.toProjectSwitcherData()
        } catch (t: Throwable) {
            throw FeatureInitializerException.ParametersDecodeFailed(params)
        }
        return projectSwitcherParams
    }

    override fun showInSequence(): Boolean = true
}

@Serializable
internal data class ProjectSwitcherData(
    val projects: List<Project>,
    val analytics: ScreenNameAnalytics? = null,
    val onboardingAnalytics: OnboardingPageLayoutData.OnboardingAnalytics? = null,
) : KiboSerializable<ProjectSwitcherData> {
    override fun getSerializer() = serializer()

    fun toRedirectionHash(key: FeatureKey): RedirectionHash =
        RedirectionHash(key, projects.joinToString { it.name })

    @Serializable
    data class Project(
        val name: String,
        val date: ProjectDate? = null,
        val image: String? = null,
        val content: Content,
    ) {
        @Serializable
        data class Content(val project: String, val otaApiUrl: String)

        @Serializable
        data class ProjectDate(val start: String? = null, val end: String? = null)
    }
}

@Serializable
internal data class ProjectSwitcherLayoutData(
    val projects: List<Project>,
    val analytics: ScreenNameAnalytics? = null,
    val onboardingPageLayoutData: OnboardingPageLayoutData? = null,
) : KiboSerializable<ProjectSwitcherLayoutData> {
    override fun getSerializer() = serializer()

    @Serializable
    data class Project(
        val name: String,
        val date: ProjectDate? = null,
        val image: String? = null,
        val content: Content,
    ) {
        @Serializable
        data class Content(val project: String, val otaApiUrl: String)

        @Serializable
        data class ProjectDate(val start: String? = null, val end: String? = null)
    }
}

private fun FeatureParams.toProjectSwitcherData() =
    KiboSerializable.decodeFromJsonElement<ProjectSwitcherData>(this)

private fun ProjectSwitcherData.toProjectSwitcherLayoutData(pageId: String?) =
    ProjectSwitcherLayoutData(
        projects = projects.map {
            ProjectSwitcherLayoutData.Project(
                name = it.name,
                date = ProjectSwitcherLayoutData.Project.ProjectDate(it.date?.start, it.date?.end),
                image = it.image,
                content = ProjectSwitcherLayoutData.Project.Content(
                    it.content.project,
                    it.content.otaApiUrl
                )
            )
        },
        analytics = analytics,
        onboardingPageLayoutData = pageId?.let {
            OnboardingPageLayoutData(
                it,
                onboardingAnalytics
            )
        }
    )
