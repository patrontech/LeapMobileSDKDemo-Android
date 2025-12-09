package com.greencopper.interfacekit.session

import com.greencopper.interfacekit.navigation.route.Route

/** Handles initial content and OTA processing and apply during or after a session
 * A session is considered as the interval of time when the app is in foreground.
 *
 * When the app is opened by the user, resume() should be called.
 * When it's put into background, pause() should be called.
 *
 * If the app is killed, the latest OTA will be downloaded and processed the next time the app is opened
 * or a silent notification is received by calling resume() or silentUpdate().
 *
 * In resume(), we force the update as quickly as possible by passing a short timeout to the OTAManager.
 * In pause() and silentUpdate(), the timeout is much longer because the app is in background and we are allowed to wait.
 **/
public interface SessionManager {
    /** Start or resume content initialization or processing and apply of the latest OTA */
    public suspend fun resume()

    /** Start or resume content initialization or processing and apply of the latest OTA */
    public suspend fun pause()

    /** Redirect to the following routeLink after the latest OTA has been processed and applied */
    public suspend fun redirectTo(routeLink: String)

    /** Redirect to the following route after the latest OTA has been processed and applied
     * This method should be removed when the CMS will use route links in notifications.
     **/
    public suspend fun redirectTo(route: Route)

    /**
     * Download and process the latest OTA in background.
     * Should be called when a silent notification is received by the app.
     */
    public suspend fun silentUpdate()
}
