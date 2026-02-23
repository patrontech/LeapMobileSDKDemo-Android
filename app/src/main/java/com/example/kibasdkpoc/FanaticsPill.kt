package com.example.kibasdkpoc

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kibasdkpoc.theme.KibaSdkPocTheme
import com.greencopper.leapmobilesdk.R

@Composable
public fun FanaticsPill(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Row(modifier = modifier
		.clickable { onClick() }
		.border(
			width = 1.dp,
			color = Color.White,
			shape = RoundedCornerShape(size = 20.dp)
		)
		.background(
			Color.DarkGray,
			shape = RoundedCornerShape(size = 20.dp)
		)
		.padding(all = 4.dp),
		verticalAlignment = Alignment.CenterVertically) {

		Spacer(modifier = Modifier.size(4.dp))
		Icon(
			imageVector = ImageVector.vectorResource(R.drawable.ic_info),
			contentDescription = null,
			modifier = Modifier.size(16.dp),
			tint = Color.White
		)
		Spacer(modifier = Modifier.size(4.dp))
		Text(
			text = text,
			color = Color.White,
			fontSize = 12.sp,
			fontWeight = FontWeight.Normal
		)
		Spacer(modifier = Modifier.size(8.dp))
		Spacer(
			modifier = Modifier
				.height(height = 20.dp)
				.width(1.dp)
				.background(color = Color.LightGray)
		)
		Spacer(modifier = Modifier.size(8.dp))
		Box {
			Text(
				text = "AB",
				color = Color.White,
				fontSize = 12.sp,
				modifier = Modifier
					.background(
						color = Color(0xFFFFC000),
						shape = CircleShape
					)
					.padding(
						horizontal = 6.dp,
						vertical = 2.dp
					),
				fontWeight = FontWeight.SemiBold
			)
		}
	}

}

@PreviewLightDark
@Composable
private fun FanaticsPillPreview() {
	KibaSdkPocTheme {
		FanaticsPill(
			text = "$17.08",
			onClick = { /* Handle click */ })
	}
}