package com.greencopper.testmocks

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import kotlinx.coroutines.*

public fun <T : AppCompatActivity> getTabBarFragment(scenario: ActivityScenario<T>): DialogFragment {
    var tabBarFragment: Fragment? = null
    runBlocking {
        try {
            withTimeout(10000) {
                repeat(20) {
                    if(this@withTimeout.isActive) {
                        scenario.onActivity {
                            val fragments = it.supportFragmentManager.fragments
                            if (fragments.size != 0 && !fragments[0].isDetached) {
                                val childFragments = fragments[0].childFragmentManager.fragments
                                if (childFragments.size != 0 && !childFragments[0].isDetached) {
                                    tabBarFragment = childFragments[0]
                                    this@withTimeout.cancel()
                                }
                            }
                        }
                        delay(1000)
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            throw e
        } catch (e: CancellationException) {
        }
    }
    return tabBarFragment as? DialogFragment ?: throw NullPointerException()
}
