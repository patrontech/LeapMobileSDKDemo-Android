package com.example.kibasdkpoc

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.kibasdkpoc.analytics.ButtonClickEvent
import com.example.kibasdkpoc.analytics.MyScreenViewEvent
import com.example.kibasdkpoc.designsystem.DemoButtons
import com.example.kibasdkpoc.designsystem.DemoHeaderText
import com.example.kibasdkpoc.theme.KibaSdkPocTheme
import com.greencopper.leapmobilesdk.LeapMobileSDK

private const val DEEPLINK_SCHEME = "fanaticssdkstaging"

public class MainActivity : ComponentActivity() {
	private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
		handleCameraPermissionResult(granted)
	}

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
		requestCameraPermission()
        setContent {
			KibaSdkPocTheme {
				DeepLinkingList()
			}
        }

        // Track that the MainActivity screen was viewed
        LeapMobileSDK.track(MyScreenViewEvent("MainActivity"))
    }

	private fun requestCameraPermission() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
			cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
		} else {
			handleCameraPermissionResult(true)
		}
	}

	private fun handleCameraPermissionResult(granted: Boolean) {
		if (!granted) {
			Toast.makeText(this@MainActivity, "Camera permission is required for the SDK to function properly.", Toast.LENGTH_LONG).show()
		}
	}
}

@Composable
private fun DeepLinkingList() {
    val context = LocalContext.current
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp)
				.padding(innerPadding)
        ) {
			DemoHeaderText(text = "Deep Link Examples")
            uris.forEach { uri ->
				DemoButtons(buttonText = uri.second) {
                    // Track button click before navigating
                    LeapMobileSDK.track(
                        ButtonClickEvent(buttonName = uri.second, screenName = "MainActivity")
                    )
					val intent = Intent(Intent.ACTION_VIEW, uri.first.toUri())
                    context.startActivity(intent)
				}
			}
			DemoHeaderText(
				modifier = Modifier.padding(top = 12.dp),
				text = "SSO cookies POC",
			)
			DemoButtons(buttonText = "Open Ticketing WebView") {
				val intent = Intent(context, WebViewActivity::class.java)
				context.startActivity(intent)
            }

			DemoButtons(modifier = Modifier.padding(top = 32.dp), buttonText = "Start SDK") {
				// Track Start SDK button click
				LeapMobileSDK.track(
					ButtonClickEvent(buttonName = "Start SDK", screenName = "MainActivity")
				)
				context.startActivity(
					Intent(context, SdkActivity::class.java)
				)
			}
        }
    }
}

private val uris = listOf(
	Pair("$DEEPLINK_SCHEME://schedule", "Schedule"),
	Pair("$DEEPLINK_SCHEME://talents", "Talents"),
	Pair("$DEEPLINK_SCHEME://brands", "Brands"),
	Pair("$DEEPLINK_SCHEME://notificationSettings", "Notification Settings"),
	Pair("$DEEPLINK_SCHEME://thuziRegistration", "Registration"),
	Pair("$DEEPLINK_SCHEME://thuziBadges", "Badges"),
	Pair("$DEEPLINK_SCHEME://invalid", "Invalid"),
)

@Preview
@Composable
private fun DeepLinkingListPreview() {
	KibaSdkPocTheme {
		DeepLinkingList()
	}
}
