package com.rzmmzdh.sima.feature_sim.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.rzmmzdh.sima.R

val vazir = FontFamily(
    Font(R.font.vazir_regular, FontWeight.Normal),
    Font(R.font.vazir_light, FontWeight.Light),
    Font(R.font.vazir_bold, FontWeight.Bold),
    Font(R.font.vazir_black, FontWeight.Black)
)
val defaultTextStyle = TextStyle(fontFamily = vazir, textAlign = TextAlign.Center)
val Typography = Typography(
    titleLarge = defaultTextStyle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
    titleMedium = defaultTextStyle.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
    titleSmall = defaultTextStyle.copy(fontSize = 12.sp, fontWeight = FontWeight.Normal),
    bodyLarge = defaultTextStyle.copy(fontSize = 16.sp, fontWeight = FontWeight.Normal),
    bodyMedium = defaultTextStyle.copy(fontSize = 14.sp, fontWeight = FontWeight.Normal),
    bodySmall = defaultTextStyle.copy(fontSize = 12.sp, fontWeight = FontWeight.Normal),
    labelLarge = defaultTextStyle.copy(fontSize = 16.sp, fontWeight = FontWeight.Light),
    labelMedium = defaultTextStyle.copy(fontSize = 14.sp, fontWeight = FontWeight.Light),
    labelSmall = defaultTextStyle.copy(fontSize = 12.sp, fontWeight = FontWeight.Light)
)
