package com.example.kibasdkpoc.designsystem

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.kibasdkpoc.theme.KibaSdkPocTheme

@Composable
public fun DemoButtons(
	modifier: Modifier = Modifier, buttonText: String, onClick: () -> Unit
) {
	Button(
		modifier = modifier
			.padding(horizontal = 4.dp)
			.fillMaxWidth(), onClick = { onClick() }) {
		Text(
			text = buttonText
		)
	}
}

@PreviewLightDark
@Composable
private fun DemoButtonsPreview() {
	KibaSdkPocTheme {
		DemoButtons(
			buttonText = "Test Button",
			onClick = { /* Do nothing */ },
		)
	}
}