package com.example.kibasdkpoc

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri

public class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            DeepLinkingList()
        }
    }
}

@Composable
private fun DeepLinkingList() {
    val context = LocalContext.current
    val uris = listOf(
        Pair("fanaticssdkstaging://schedule", "Schedule"),
        Pair("fanaticssdkstaging://talents", "Talents"),
        Pair("fanaticssdkstaging://brands", "Brands"),
        Pair("fanaticssdkstaging://notificationSettings", "Notification Settings"),
        Pair("fanaticssdkstaging://thuziRegistration", "Registration"),
        Pair("fanaticssdkstaging://thuziBadges", "Badges"),
        Pair("fanaticssdkstaging://invalid", "Invalid"),
    )

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            uris.forEach { uri ->
                DeepLinkButton(onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW, uri.first.toUri()
                    )
                    context.startActivity(intent)
                }, buttonText = uri.second)
            }

            DeepLinkButton(
                modifier = Modifier.padding(top = 32.dp), onClick = {
                    context.startActivity(
                        Intent(
                            context, SdkActivity::class.java
                        )
                    )
                }, buttonText = "Start SDK"
            )
        }
    }
}

@Composable
private fun DeepLinkButton(
    modifier: Modifier = Modifier, onClick: () -> Unit, buttonText: String
) {
    Button(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(), onClick = { onClick() }) {
        Text(
            text = buttonText
        )
    }
}

@Preview
@Composable
private fun DeepLinkingListPreview() {
    DeepLinkingList()
}
