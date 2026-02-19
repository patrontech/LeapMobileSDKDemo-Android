package com.example.kibasdkpoc.designsystem

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.kibasdkpoc.theme.KibaSdkPocTheme


@Composable
public fun DemoHeaderText(
	modifier: Modifier = Modifier, text: String
) {
	Text(
		text = text, modifier = modifier.padding(bottom = 4.dp), style = MaterialTheme.typography.titleMedium

	)
}

@PreviewLightDark
@Composable
private fun DemoHeaderTextPreview() {
	KibaSdkPocTheme {
		DemoHeaderText(
			text = "Header test"
		)
	}
}