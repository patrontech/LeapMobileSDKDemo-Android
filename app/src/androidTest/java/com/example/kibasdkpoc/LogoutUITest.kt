package com.example.kibasdkpoc

import android.webkit.CookieManager
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.example.kibasdkpoc.theme.KibaSdkPocTheme
import com.example.kibasdkpoc.webview.UrlProvider
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Rule
import org.junit.Test

/**
 * Integration tests for the logout functionality.
 * Uses ComponentActivity + setContent.
 *
 * Note: The scenario "button becomes visible after setting cookie post-launch" cannot be tested
 * because triggering ON_RESUME (via recreate, overlay activity, or setContent twice) either breaks
 * the Compose hierarchy connection or is not allowed by the test framework.
 */
public class LogoutUITest {

    @get:Rule
    public val composeTestRule: AndroidComposeTestRule<*, ComponentActivity> =
        createAndroidComposeRule<ComponentActivity>()

    @Test
    public fun logoutButton_isVisible_whenUserIsLoggedIn() {
        setCookieForLoggedInUser()

        composeTestRule.setContent {
            KibaSdkPocTheme {
                DeepLinkingList()
            }
        }

        composeTestRule.onNodeWithTag("logout_button").assertExists()
    }

    @Test
    public fun logoutButton_isNotVisible_whenUserIsNotLoggedIn() {
        clearCookies()

        composeTestRule.setContent {
            KibaSdkPocTheme {
                DeepLinkingList()
            }
        }

        composeTestRule.onNodeWithTag("logout_button").assertDoesNotExist()
    }

    @Test
    public fun logoutButton_isHidden_afterLogoutClicked() {
        setCookieForLoggedInUser()

        composeTestRule.setContent {
            KibaSdkPocTheme {
                DeepLinkingList()
            }
        }

        composeTestRule.onNodeWithTag("logout_button").assertExists()
        composeTestRule.onNodeWithTag("logout_button").performClick()

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("logout_button").assertDoesNotExist()
    }

    private fun setCookieForLoggedInUser() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            CookieManager.getInstance().setCookie(UrlProvider.AUTH_URL, "session=test")
            CookieManager.getInstance().flush()
        }
    }

    private fun clearCookies() {
        val latch = CountDownLatch(1)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            CookieManager.getInstance().removeAllCookies { latch.countDown() }
        }
        latch.await(5, TimeUnit.SECONDS)
    }
}
