package com.greencopper.interfacekit.multiproject

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.multiproject.ui.ProjectSwitchingFragment
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

internal class ProjectSwitchingInitializer :
    ParameterizedFeatureInitializer<ProjectSwitchingData>(), OnboardingPageInitializer {

    override val featureKey: FeatureKey = key

    companion object {
        val key: FeatureKey = FeatureKey("InterfaceKit.ProjectSwitching", 1)
    }

    override fun resolve(params: JsonElement?, pageId: String): OnboardingPageLayout =
        ProjectSwitchingFragment(
            params?.let { decodeParams(it).toProjectSwitchingLayoutData(pageId) }
                ?: throw IllegalStateException("No parameters provided for ProjectSwitching onboarding screen"))

    override fun decodeParams(params: FeatureParams): ProjectSwitchingData {
        val projectSwitchingParams = try {
            params.toProjectSwitchingData()
        } catch (t: Throwable) {
            throw FeatureInitializerException.ParametersDecodeFailed(params)
        }
        return projectSwitchingParams
    }

    override fun layoutForParams(params: ProjectSwitchingData): Layout =
        ProjectSwitchingFragment(params.toProjectSwitchingLayoutData("NotAnOnboardingPage"))

    override fun redirectionHashForParams(params: ProjectSwitchingData): RedirectionHash =
        params.toRedirectionHash(key)

    override fun showInSequence(): Boolean = true
}

@Serializable
internal data class ProjectSwitchingData(
    val project: Project,
    val analytics: ScreenNameAnalytics,
    val onboardingAnalytics: OnboardingPageLayoutData.OnboardingAnalytics? = null,
) : KiboSerializable<ProjectSwitchingData> {
    override fun getSerializer() = serializer()

    fun toRedirectionHash(key: FeatureKey): RedirectionHash = RedirectionHash(key, project.name)

    @Serializable
    data class Project(
        val name: String,
        val date: ProjectDate? = null,
        val image: String? = null,
        val content: Content
    ) {
        @Serializable
        data class Content(val project: String, val otaApiUrl: String)

        @Serializable
        data class ProjectDate(val start: String? = null, val end: String? = null)
    }
}

@Serializable
internal data class ProjectSwitchingLayoutData(
    val project: Project,
    val analytics: ScreenNameAnalytics,
    val onboardingPageLayoutData: OnboardingPageLayoutData? = null,
) : KiboSerializable<ProjectSwitchingLayoutData> {
    override fun getSerializer() = serializer()

    @Serializable
    data class Project(
        val name: String,
        val date: ProjectDate? = null,
        val image: String? = null,
        val content: Content
    ) {
        @Serializable
        data class Content(val project: String, val otaApiUrl: String)

        @Serializable
        data class ProjectDate(val start: String? = null, val end: String? = null)
    }
}

private fun FeatureParams.toProjectSwitchingData() =
    KiboSerializable.decodeFromJsonElement<ProjectSwitchingData>(this)

private fun ProjectSwitchingData.toProjectSwitchingLayoutData(pageId: String?) =
    ProjectSwitchingLayoutData(
        project = ProjectSwitchingLayoutData.Project(
            name = project.name,
            date = ProjectSwitchingLayoutData.Project.ProjectDate(
                project.date?.start,
                project.date?.end
            ),
            image = project.image,
            content = ProjectSwitchingLayoutData.Project.Content(
                project.content.project,
                project.content.otaApiUrl
            )
        ),
        analytics = analytics,
        onboardingPageLayoutData = pageId?.let {
            OnboardingPageLayoutData(
                it,
                onboardingAnalytics
            )
        }
    )
