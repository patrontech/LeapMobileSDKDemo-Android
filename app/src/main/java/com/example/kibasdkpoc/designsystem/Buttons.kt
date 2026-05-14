package com.example.kibasdkpoc.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.kibasdkpoc.R
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

@Composable
public fun DemoFloatingButton(
	modifier: Modifier = Modifier,
	buttonText: String,
	onClick: () -> Unit,
) {
	Button(
		modifier = modifier.size(44.dp),
		onClick = onClick,
		shape = CircleShape,
		contentPadding = PaddingValues(2.dp),
		colors = ButtonDefaults.buttonColors(
			containerColor = Color.Black,
			contentColor = Color.White,
		),
	) {
		Icon(
			imageVector = Icons.Default.AccountCircle,
			contentDescription = buttonText,
		)
	}
}

@Composable
public fun BackButtonOverlay(
	onClick: () -> Unit
) {
	Box(
		modifier = Modifier
			.size(48.dp)
			.background(color = Color.White, shape = CircleShape)
			.clickable { onClick() },
		contentAlignment = Alignment.Center
	) {
		Icon(
			imageVector = ImageVector.vectorResource(R.drawable.ic_back_btn), contentDescription = "Back", tint = Color(0xFF0A0AFF)
		)
	}

}

@PreviewLightDark
@Composable
private fun DemoButtonsPreview() {
	KibaSdkPocTheme {
		Column {
			DemoFloatingButton(
				buttonText = "Account",
				onClick = { /* Do nothing */ },
			)
		}
	}
}
