package com.example.kibasdkpoc.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import com.example.kibasdkpoc.deeplink.DEEPLINK_SCHEME
import com.example.kibasdkpoc.theme.KibaSdkPocTheme
import com.example.kibasdkpoc.theme.attrColorResource
import com.greencopper.leapmobilesdk.LeapMobileSDK
import com.greencopper.leapmobilesdk.R
import com.greencopper.leapmobilesdk.compose.Content

public class SdkComposeNavigationActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            KibaSdkPocTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(attrColorResource(R.attr.customStatusAndBottomBarColor))
                        .statusBarsPadding(),
                ) {
                    LeapSdkScreen()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

@Composable
public fun LeapSdkScreen(
    deeplinkPath: String? = null,
) {
    val activity = LocalActivity.current
    val deeplink = deeplinkPath?.takeIf { it.isNotEmpty() }?.let { path ->
        "${DEEPLINK_SCHEME}://$path".toUri()
    }

    LeapMobileSDK.Content(
        modifier = Modifier
            .fillMaxSize(),
        deeplink = deeplink,
        onBack = { activity?.finish() },
        showBackButton = true,
    )
}