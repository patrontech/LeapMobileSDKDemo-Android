package com.example.kibasdkpoc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.kibasdkpoc.R
import com.example.kibasdkpoc.designsystem.DemoButtons
import com.example.kibasdkpoc.theme.KibaSdkPocTheme

@Composable
public fun ScreenTwo(
	onBack: () -> Unit = {},
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
			.padding(24.dp),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Text(
			text = stringResource(R.string.screen_two_title),
			style = MaterialTheme.typography.headlineMedium,
			color = MaterialTheme.colorScheme.onBackground,
		)
		Spacer(modifier = Modifier.height(16.dp))
		Text(
			text = stringResource(R.string.screen_two_description),
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onBackground,
		)
		Spacer(modifier = Modifier.height(32.dp))
		DemoButtons(
			buttonText = stringResource(R.string.go_back_to_sdk),
			onClick = onBack,
		)
	}
}

@Preview(showBackground = true)
@Composable
private fun ScreenTwoPreview() {
	KibaSdkPocTheme {
		ScreenTwo()
	}
}
