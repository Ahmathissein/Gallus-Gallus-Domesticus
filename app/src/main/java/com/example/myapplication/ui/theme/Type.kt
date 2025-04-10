package com.example.myapplication.ui.theme


import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.unit.sp
import com.example.myapplication.R

val Quicksand = FontFamily(
    Font(R.font.quicksand_light, FontWeight.Light),
    Font(R.font.quicksand_regular, FontWeight.Normal),
    Font(R.font.quicksand_medium, FontWeight.Medium),
    Font(R.font.quicksand_semibold, FontWeight.SemiBold),
    Font(R.font.quicksand_bold, FontWeight.Bold)
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = Quicksand,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Quicksand,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Quicksand,
        fontWeight = FontWeight.Light,
        fontSize = 12.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Quicksand,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Quicksand,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    )
)

