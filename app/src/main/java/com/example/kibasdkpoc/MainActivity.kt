package com.example.kibasdkpoc

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.webkit.CookieManager
import android.widget.Space
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.net.toUri
import com.example.kibasdkpoc.analytics.ButtonClickEvent
import com.example.kibasdkpoc.analytics.MyScreenViewEvent
import com.example.kibasdkpoc.deeplink.deeplinkUris
import com.example.kibasdkpoc.designsystem.DemoButtons
import com.example.kibasdkpoc.designsystem.DemoHeaderText
import com.example.kibasdkpoc.designsystem.lifecycle.ObserveLifecycleEvents
import com.example.kibasdkpoc.theme.KibaSdkPocTheme
import com.example.kibasdkpoc.ui.SdkActivity
import com.example.kibasdkpoc.ui.SdkComposeNavigationActivity
import com.example.kibasdkpoc.webview.UrlProvider
import com.example.kibasdkpoc.webview.WebViewActivity
import com.greencopper.leapmobilesdk.LeapMobileSDK
import kotlinx.coroutines.launch

private const val CHANNEL_ID = "demo_app_channel_id"

public class MainActivity : ComponentActivity() {
    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            handleCameraPermissionResult(granted)
        }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            handleNotificationPermissionResult(granted)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        requestCameraPermission()

        setContent {
            KibaSdkPocTheme {
                MainScreen()
            }
        }

        // Track that the MainActivity screen was viewed
        LeapMobileSDK.track(MyScreenViewEvent("MainActivity"))
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            requestNotificationPermission()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun handleCameraPermissionResult(granted: Boolean) {
        if (!granted) {
            makeText(
                this@MainActivity,
                getString(R.string.camera_permission_required),
                Toast.LENGTH_LONG
            ).show()
        }

        requestNotificationPermission()
    }

    private fun handleNotificationPermissionResult(granted: Boolean) {
        if (!granted) {
            makeText(
                this@MainActivity,
                getString(R.string.notification_permission_required),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun MainScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isUserLogged by remember { mutableStateOf(false) }

    ObserveLifecycleEvents(
        onResume = {
            isUserLogged = isLoggedIn(UrlProvider.AUTH_URL)
        }
    )

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(innerPadding)
        ) {
            DeepLinkingSection()
            SSOSection(context)
            NotificationsSection()

            StartSdkButton(context)

            LogoutButton(isUserLogged) {
                LeapMobileSDK.track(ButtonClickEvent(buttonName = "Logout", screenName = "MainActivity"))
                coroutineScope.launch {
                    LeapMobileSDK.logout()
                    isUserLogged = false
                    makeText(context, R.string.you_have_been_logged_out, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

@Composable
private fun LogoutButton(
    isUserLogged: Boolean,
    onLoggedOut: () -> Unit,
) {
    if (!isUserLogged) return

    DemoButtons(
        modifier = Modifier
            .padding(top = 16.dp)
            .testTag(MainScreenTestTags.LOGOUT_BUTTON),
        buttonText = stringResource(R.string.logout)
    ) {
        onLoggedOut()
    }
}

@Composable
internal fun DeepLinkingSection() {
    val context = LocalContext.current

    DemoHeaderText(text = "Deep Link Examples")

    deeplinkUris.forEach { (uri, labelResId) ->
        val label = stringResource(labelResId)

        DemoButtons(buttonText = label) {
            // Track button click before navigating
            LeapMobileSDK.track(ButtonClickEvent(buttonName = label, screenName = "MainActivity"))
            val intent = Intent(Intent.ACTION_VIEW, uri.toUri())
            context.startActivity(intent)
        }
    }
}

@Composable
private fun SSOSection(context: Context) {
    DemoHeaderText(
        modifier = Modifier.padding(top = 12.dp),
        text = stringResource(R.string.sso_cookies_poc),
    )
    DemoButtons(buttonText = stringResource(R.string.authentication_webview)) {
        val intent = Intent(context, WebViewActivity::class.java)
        context.startActivity(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsSection() {
    val context = LocalContext.current
    val dropDownList = buildList {
        add(Pair("Start SDK", R.string.start_sdk))
        addAll(deeplinkUris)
    }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var selectedUri by rememberSaveable { mutableStateOf(dropDownList.first()) }

    DemoHeaderText(
        modifier = Modifier.padding(top = 12.dp),
        text = stringResource(R.string.notifications),
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                modifier = Modifier
                    .width(200.dp)
                    .weight(2f, fill = true)
                    .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                value = stringResource(selectedUri.second),
                onValueChange = {},
                readOnly = true,
            )
            CreateNotificationButton(context, selectedUri)
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            dropDownList.forEach { option ->
                DropdownMenuItem(text = { Text(stringResource(option.second)) }, onClick = {
                    selectedUri = option
                    expanded = false
                })
            }
        }
    }
    Spacer(modifier = Modifier.height(32.dp))
}

@Composable
private fun StartSdkButton(context: Context) {
    DemoButtons(
        buttonText = stringResource(R.string.start_sdk)
    ) {
        // Track Start SDK button click
        LeapMobileSDK.track(
            ButtonClickEvent(buttonName = "Start SDK", screenName = "MainActivity")
        )
        context.startActivity(
            Intent(context, SdkComposeNavigationActivity::class.java)
        )
    }
}

@Composable
private fun RowScope.CreateNotificationButton(
    context: Context,
    selectedUri: Pair<String, Int>
) {
    DemoButtons(
        buttonText = stringResource(R.string.create), modifier = Modifier
            .width(200.dp)
            .weight(1f)
    ) {
        makeText(
            context,
            context.getString(R.string.creating_notification_with_uri, selectedUri.first),
            Toast.LENGTH_SHORT
        ).show()
        showNotification(
            context,
            title = context.getString(R.string.host_app_notification),
            body = context.getString(R.string.click_to_start_deeplink, selectedUri.first),
            intent = if (deeplinkUris.contains(selectedUri)) Intent(
                Intent.ACTION_VIEW, selectedUri.first.toUri()
            ) else Intent(context, SdkActivity::class.java)
        )
    }
}

private fun showNotification(context: Context, title: String, body: String, intent: Intent) {
    val notificationManager = getSystemService(context, NotificationManager::class.java)
    if (notificationManager != null) {
        // Create notification channel if needed (Android 8.0+)
        val channel = NotificationChannel(
            CHANNEL_ID, "Sample app notifications", NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notificationBuilder = NotificationCompat.Builder(
            context, CHANNEL_ID
        ).setContentTitle(title).setContentText(body)
            .setSmallIcon(com.greencopper.leapmobilesdk.R.drawable.ic_email) // Ensure this icon exists
            .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}

/**
 * Checks if the user is logged in based on auth cookies.
 * Logged-in state is indicated by the presence of `sat` or `fid-sc` cookies,
 * which are only set after successful authentication.
 */
private fun isLoggedIn(authUrl: String): Boolean {
    val cookie = CookieManager.getInstance().getCookie(authUrl)
    if (cookie.isNullOrBlank()) return false

    return cookie.split(";").any { pair ->
        val parts = pair.trim().split("=", limit = 2)
        parts.size == 2 && (parts[0] == "sat" || parts[0] == "fid-sc")
    }
}

@Preview
@Composable
private fun DeepLinkingSectionPreview() {
    KibaSdkPocTheme {
        DeepLinkingSection()
    }
}
