package com.greencopper.interfacekit.session

import com.greencopper.core.content.initialcontent.ContentInitializer
import com.greencopper.core.content.manager.ContentManager
import com.greencopper.core.content.ota.OTAManager
import com.greencopper.core.draftcontent.DraftContentManager
import com.greencopper.core.recipe.CoreConfigurationHolder
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.rootview.RootLayoutManager
import com.greencopper.toolkit.di.resolver.LazyResolver
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.d
import com.greencopper.toolkit.logging.e
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.time.Duration

internal class ConcreteSessionManager(
    private val configHolder: CoreConfigurationHolder,
    private val contentInitializer: ContentInitializer,
    private val contentManager: ContentManager,
    private val draftContentManager: DraftContentManager,
    private val lazyOTAManager: LazyResolver<OTAManager>,
    private val routeController: RouteController,
    private val rootLayoutManager: RootLayoutManager,
    private val backgroundCoroutineScope: CoroutineScope,
    private val logger: Logging,
) : SessionManager {

    private var resumeJob: Job? = null
    private var pauseJob: Job? = null
    private var processOTAJob: ProcessOTAJob? = null
    private var shouldInitialize = true
    private var otaManager: OTAManager? = null

    init {
        backgroundCoroutineScope.launch {
            configHolder.currentConfiguration.filterNotNull().collectLatest {
                otaManager = lazyOTAManager.resolve()
            }
        }

        backgroundCoroutineScope.launch { listenForDraftContentToggle() }
    }

    override suspend fun resume() {
        pauseJob?.join()
        resumeJob?.let {
            logger.d("Waiting for previous resume to finish")
            it.join()
            logger.d("resume done")
        } ?: run {
            logger.d("scheduling resume task")
            resumeSequence()
            resumeJob?.join()
            logger.d("resume done")
        }
    }

    override suspend fun pause() {
        resumeJob?.join()
        pauseJob?.let {
            logger.d("Waiting for previous pause to finish")
            it.join()
            logger.d("pause done")
        } ?: run {
            pauseSequence()
        }
    }

    override suspend fun redirectTo(routeLink: String) {
        resume()
        logger.d("starting redirection to $routeLink")
        routeController.redirectRouteLink(routeLink, null)
    }

    // TODO - Remove once we get rid of Routes from notifications
    override suspend fun redirectTo(route: Route) {
        resume()
        logger.d("starting redirection to $route")
        routeController.redirect(route, null)
    }

    //TODO - Use in the silent notification handling code
    override suspend fun silentUpdate() {
        logger.d("starting silent update")
        processContentOnceAtATime(false)?.job?.join()
        logger.d("silent update completed")
    }

    private fun resumeSequence() {
        resumeJob = backgroundCoroutineScope.launch {
            applyContentIfNeeded().join()
            processContentOnceAtATime(true)?.job?.join()
            applyContentIfNeeded().join()
        }.apply {
            invokeOnCompletion { resumeJob = null }
        }
    }

    private fun pauseSequence() {
        pauseJob = backgroundCoroutineScope.launch {
            logger.d("starting pause")
            processContentOnceAtATime(false)?.job?.join()
            logger.d("pause done")
        }.apply {
            invokeOnCompletion { pauseJob = null }
        }
    }

    private fun processContentOnceAtATime(quickly: Boolean): ProcessOTAJob? {
        processOTAJob?.let {
            if (it.shouldBeCancelled(quickly)) {
                it.job.cancel()
                processOTAJob = scheduleProcessOTAJob(quickly)
            }
        } ?: run {
            processOTAJob = scheduleProcessOTAJob(quickly)
        }
        return processOTAJob
    }

    private fun scheduleProcessOTAJob(quickly: Boolean): ProcessOTAJob {
        val job = backgroundCoroutineScope.launch {
            processOTAContentIfNeeded(quickly).join()
        }.apply {
            invokeOnCompletion { processOTAJob = null }
        }
        return ProcessOTAJob(job, quickly)
    }

    private fun processOTAContentIfNeeded(quickly: Boolean): Job {
        return backgroundCoroutineScope.launch {
            val (fetchTimeout, downloadTimeout) = if (quickly) {
                3L to 3L
            } else {
                60L to 60L
            }
            logger.d("Looking for content to process")
            try {
                val content =
                    otaManager?.otaContentToProcess(fetchTimeout = Duration.ofSeconds(fetchTimeout))
                content?.let {
                    otaManager?.process(content, true, Duration.ofSeconds(downloadTimeout))
                } ?: run {
                    logger.d("No content to process")
                }
            } catch (error: Throwable) {
                logger.e(message = "Fetching OTA and process failed: ", throwable = error)
            }
        }
    }

    private fun applyContentIfNeeded(): Job {
        return backgroundCoroutineScope.launch {
            if (shouldInitialize) {
                logger.d("Initializing content")
                contentInitializer.initialize()
                logger.d("Finished initializing")
                shouldInitialize = false
            } else {
                logger.d("Looking for content to apply")
                contentManager.contentToApply?.let {
                    contentManager.apply(it)
                } ?: run {
                    logger.d("No content to apply")
                }
            }
        }
    }

    private suspend fun listenForDraftContentToggle() {
        draftContentManager.passcodeFlow.drop(1).collectLatest { passcode ->
            logger.d("Resume after draft content toggle")
            resume()
            rootLayoutManager.updateRootLayout()
        }
    }

    /**
     * Current job should be cancelled if the new job has higher priority than the current one.
     * Priority is higher if new flag is true and the old one is false.
     */
    private fun ProcessOTAJob.shouldBeCancelled(other: Boolean) = quickly != other && other

    private data class ProcessOTAJob(val job: Job, val quickly: Boolean)
}
