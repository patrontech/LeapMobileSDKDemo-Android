package com.example.kibasdkpoc.deeplink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import com.example.kibasdkpoc.theme.KibaSdkPocTheme
import com.example.kibasdkpoc.theme.attrColorResource
import com.example.kibasdkpoc.ui.LeapSdkScreen
import com.greencopper.leapmobilesdk.LeapMobileSDK
import com.greencopper.leapmobilesdk.R as SdkR

public class DeepLinkActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Extract the composable deeplink path from the incoming URI.
        // If the domain is fanfest/xxxxx, extract everything following fanfest/
        // e.g. sampleapp://fanfest/ass → "ass"
        val uri = intent?.data
        val deeplinkPath = uri?.pathSegments?.let { segments ->
            if (segments.isNotEmpty() && segments[0] == "fanfest" && segments.size > 1) {
                // Extract everything after "fanfest/"
                segments.drop(1).joinToString("/")
            } else {
                segments.joinToString("/")
            }
        }

        setContent {
            KibaSdkPocTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(attrColorResource(SdkR.attr.customStatusAndBottomBarColor))
                        .statusBarsPadding(),
                ) {
                    LeapSdkScreen(deeplinkPath = deeplinkPath)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LeapMobileSDK.resume()
    }

    override fun onPause() {
        super.onPause()
        LeapMobileSDK.pause()
    }
}