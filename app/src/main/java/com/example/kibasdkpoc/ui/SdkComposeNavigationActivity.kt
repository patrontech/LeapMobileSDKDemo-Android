package com.example.kibasdkpoc.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kibasdkpoc.R
import com.example.kibasdkpoc.deeplink.DEEPLINK_SCHEME
import com.example.kibasdkpoc.deeplink.DeepLinkActivity
import com.example.kibasdkpoc.designsystem.DemoFloatingButton
import com.example.kibasdkpoc.theme.KibaSdkPocTheme
import com.example.kibasdkpoc.theme.attrColorResource
import com.greencopper.leapmobilesdk.LeapMobileSDK
import com.greencopper.leapmobilesdk.compose.Content
import com.greencopper.leapmobilesdk.R as SdkR

private object Routes {
    const val LEAP_SDK = "leap_sdk"
    const val SCREEN_TWO = "screen_two"
}

public class SdkComposeNavigationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            KibaSdkPocTheme {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(attrColorResource(SdkR.attr.customStatusAndBottomBarColor))
                        .statusBarsPadding(),
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Routes.LEAP_SDK,
                    ) {
                        composable(Routes.LEAP_SDK) {
                            LeapSdkScreen(
                                onNavigateToScreenTwo = {
                                    navController.navigate(Routes.SCREEN_TWO)
                                }
                            )
                        }
                        composable(Routes.SCREEN_TWO) {
                            ScreenTwo(
                                onBack = { navController.popBackStack() },
                            )
                        }
                    }
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
    onNavigateToScreenTwo: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val deeplink = deeplinkPath?.takeIf { it.isNotEmpty() }?.let { path ->
        "$DEEPLINK_SCHEME://$path".toUri()
    }
    val deeplinkScheme = stringResource(SdkR.string.sample_app_deeplink_scheme)

    var isRoot by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        LeapMobileSDK.Content(
            modifier = Modifier.fillMaxSize(),
            deeplink = deeplink,
            deeplinkHandler = { uri: Uri ->
                if (uri.scheme == deeplinkScheme) {
                    val intent = Intent(context, DeepLinkActivity::class.java).apply {
                        data = uri
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    true
                } else {
                    false
                }
            },
            onBack = { activity?.finish() },
            showBackButton = false,
            onBackStackChanged = { isRootState, _ -> isRoot = isRootState }
        )

        if (isRoot && deeplinkPath.isNullOrEmpty() && activity?.intent?.data == null) {
            DemoFloatingButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                buttonText = stringResource(R.string.navigate_to_screen_two),
                onClick = onNavigateToScreenTwo,
            )
        }
    }
}
