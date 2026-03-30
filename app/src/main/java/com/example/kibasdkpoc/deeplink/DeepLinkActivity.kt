package com.example.kibasdkpoc.deeplink

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.kibasdkpoc.R
import com.example.kibasdkpoc.designsystem.BackButtonOverlay
import com.example.kibasdkpoc.designsystem.DemoButtons
import com.example.kibasdkpoc.theme.KibaSdkPocTheme

public class DeepLinkActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            KibaSdkPocTheme {
                DeepLinkScreenContent(
                    onDismiss = { finish() },
                    onDeepLinkClick = { uri ->
                        startActivity(Intent(Intent.ACTION_VIEW, uri.toUri()))
                        finish()
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeepLinkScreenContent(
    onDismiss: () -> Unit,
    onDeepLinkClick: (String) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(innerPadding)
        ) {
            BackButtonOverlay {
                onDismiss()
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.deep_link_tests),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            deeplinkUris.forEach { (uri, labelResId) ->
                DemoButtons(buttonText = stringResource(labelResId)) {
                    onDeepLinkClick(uri)
                }
            }
        }
    }
}

@Preview
@Composable
private fun DeepLinkScreenContentPreview() {
    KibaSdkPocTheme {
        DeepLinkScreenContent(
            onDismiss = { },
            onDeepLinkClick = { },
        )
    }
}