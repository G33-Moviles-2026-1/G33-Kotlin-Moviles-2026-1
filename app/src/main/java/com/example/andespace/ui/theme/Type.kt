package com.example.andespace.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import android.graphics.Typeface

@Composable
fun AndeSpaceTypography(): Typography {
    val context = LocalContext.current

    val poppins = remember {
        FontFamily(Typeface.createFromAsset(context.assets, "fonts/Poppins-Regular.ttf"))
    }
    val poppinsMedium = remember {
        FontFamily(Typeface.createFromAsset(context.assets, "fonts/Poppins-Medium.ttf"))
    }
    val poppinsSemiBold = remember {
        FontFamily(Typeface.createFromAsset(context.assets, "fonts/Poppins-SemiBold.ttf"))
    }
    val adlam = remember {
        FontFamily(Typeface.createFromAsset(context.assets, "fonts/ADLaMDisplay-Regular.ttf"))
    }

    return Typography(
        bodyLarge = TextStyle(
            fontFamily = poppins,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.2.sp
        ),
        titleLarge = TextStyle(
            fontFamily = adlam,
            fontWeight = FontWeight.Normal,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = poppinsSemiBold,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 24.sp
        ),
        labelLarge = TextStyle(
            fontFamily = poppinsSemiBold,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = poppinsMedium,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    )
}
