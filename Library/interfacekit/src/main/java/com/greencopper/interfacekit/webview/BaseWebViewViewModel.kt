package com.greencopper.interfacekit.webview

import android.Manifest
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.webkit.GeolocationPermissions
import android.webkit.MimeTypeMap
import android.webkit.PermissionRequest
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.permissions.PermissionManager
import com.greencopper.core.permissions.RationalePanelConfig
import com.greencopper.core.utils.appManifestHasPermission
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.e
import com.greencopper.toolkit.logging.w
import com.greencopper.toolkit.versionprovider.BuildConfigProvider
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

internal class BaseWebViewViewModel(
    private val buildConfigProvider: BuildConfigProvider,
    private val routeController: RouteController,
    private val permissionManager: PermissionManager,
    private val localizationService: LocalizationService,
    internal val webviewClientListeners: List<WebViewClientListener>,
    private val appContext: Context,
    private val logging: Logging,
) : ViewModel() {

    internal fun redirectToRoute(route: Route, origin: Layout) = routeController.redirect(route, origin)

    internal val isDebugBuild: Boolean = buildConfigProvider.isDebug
    private var closeOnDownloadCompleted: Boolean = false

    /**
     * @return true if should show a loading state since the initialUrl was explicitly opened for downloading.
     */
    fun downloadFile(context: Context?, initialUrl: String, url: String, mimeType: String): Boolean {
        try {
            viewModelScope.launch {
                val request = DownloadManager.Request(url.toUri())

                //Notify client once download is completed!
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

                request.setMimeType(mimeType)
                val fileName = url.split("/").last()
                val fileExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: ""
                val subPath = if (fileName.endsWith(fileExtension)) fileName else "$fileName.$fileExtension"
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, subPath)
                request.allowScanningByMediaScanner()

                getSystemService(appContext, DownloadManager::class.java)?.enqueue(request)
                //To notify the Client that the file is being downloaded
                context?.let {
                    Toast.makeText(it, context.resources.getString(R.string.downloading_file), Toast.LENGTH_LONG).show()
                }
            }

            if (url == initialUrl) {
                closeOnDownloadCompleted = true
                return true
            }
        } catch (t: Throwable) {
            logging.e("Failed to download $url", throwable = t)
        }
        return false
    }

    /**
     * Used to open the downloaded attachment.
     *
     * @param context    Context.
     * @param downloadId Id of the downloaded file to open.
     */
    internal fun openDownloadedAttachment(context: Context, downloadId: Long, close: () -> Unit) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().apply { setFilterById(downloadId) }
        val cursor: Cursor = downloadManager.query(query)
        if (cursor.moveToFirst()) {
            try {
                val downloadStatus = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                val downloadMimeType = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_MEDIA_TYPE))
                if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL && downloadMimeType.contains("pdf")) {
                    val downloadLocalUri = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                    openFile(context, downloadLocalUri.toUri(), downloadMimeType)
                    if (closeOnDownloadCompleted) {
                        close()
                    }
                }
            } catch (e: Exception) {
                logging.w("Could not open downloaded file")
            }
        }
        cursor.close()
        closeOnDownloadCompleted = false
    }

    /**
     * Used to open the downloaded attachment.
     *
     * @param attachmentUri Uri of the downloaded attachment to be opened.
     * @param attachmentMimeType MimeType of the downloaded attachment.
     */
    private fun openFile(context: Context, attachmentUri: Uri, attachmentMimeType: String) {
        var uri = attachmentUri
        if (ContentResolver.SCHEME_FILE == attachmentUri.scheme && attachmentUri.path != null) {
            // FileUri - Convert it to contentUri.
            val file = File(attachmentUri.path!!)
            uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        }

        val openAttachmentIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, attachmentMimeType)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        try {
            context.startActivity(openAttachmentIntent)
        } catch (e: ActivityNotFoundException) {
            logging.w("Could not find activity to open file")
        }
    }

    fun showGeoLocationPermissionPrompt(
        layout: Layout,
        origin: String,
        callback: GeolocationPermissions.Callback,
    ) {
        if (appManifestHasPermission(buildConfigProvider, layout.requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
            layout.lifecycleScope.launch {
                permissionManager.startPermissionsRequestFlow(
                    layout.requireActivity(),
                    RationalePanelConfig(
                        title = localizationService.getString("webview.permission.location.title"),
                        message = localizationService.getString("webview.permission.location.message"),
                        positiveButtonString = layout.getString(android.R.string.ok)
                    ),
                    null,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ).collectLatest { granted ->
                    callback.invoke(origin, granted, false)
                }
            }
        }
    }

    fun showPermissionRequest(layout: Layout, request: PermissionRequest) {
        val context = layout.context ?: return
        if (appManifestHasPermission(buildConfigProvider, context, Manifest.permission.CAMERA) &&
            request.resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)
        ) {
            layout.lifecycleScope.launch {
                permissionManager.startPermissionsRequestFlow(
                    layout.requireActivity(),
                    RationalePanelConfig(
                        title = localizationService.getString("webview.permission.camera.title"),
                        message = localizationService.getString("webview.permission.camera.message"),
                        positiveButtonString = layout.getString(android.R.string.ok)
                    ),
                    null,
                    Manifest.permission.CAMERA,
                ).collectLatest { granted ->
                    if (granted) {
                        request.grant(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
                    }
                }
            }
        }
    }
}
