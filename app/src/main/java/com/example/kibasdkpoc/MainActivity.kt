package com.example.kibasdkpoc

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

public class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            enableEdgeToEdge()
        }

        if (intent.data != null) {
            handleDeeplink(intent.data)
        } else {
            lifecycleScope.launch {
                LeapMobileSDK.getRootLayout(supportFragmentManager).collect { fragment ->
                    replaceView(fragment)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeeplink(intent.data)
    }

    private fun handleDeeplink(uri: Uri?) {
        uri?.let {
            LeapMobileSDK.resolveDeeplink(uri)?.let { fragment -> replaceView(fragment) }
        }
    }

    private fun replaceView(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
