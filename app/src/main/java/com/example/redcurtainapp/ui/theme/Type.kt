package com.example.redcurtainapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Font size multipliers
private val SmallFontMultiplier = 0.85f
private val MediumFontMultiplier = 1.0f
private val LargeFontMultiplier = 1.15f

fun getFontSizeMultiplier(fontSizePreference: String): Float {
    return when (fontSizePreference) {
        "Small" -> SmallFontMultiplier
        "Large" -> LargeFontMultiplier
        else -> MediumFontMultiplier
    }
}

// Base typography
private val BaseTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

fun createTypography(fontSizePreference: String): Typography {
    val multiplier = getFontSizeMultiplier(fontSizePreference)
    
    return Typography(
        displayLarge = BaseTypography.displayLarge.copy(fontSize = (BaseTypography.displayLarge.fontSize.value * multiplier).sp),
        displayMedium = BaseTypography.displayMedium.copy(fontSize = (BaseTypography.displayMedium.fontSize.value * multiplier).sp),
        displaySmall = BaseTypography.displaySmall.copy(fontSize = (BaseTypography.displaySmall.fontSize.value * multiplier).sp),
        headlineLarge = BaseTypography.headlineLarge.copy(fontSize = (BaseTypography.headlineLarge.fontSize.value * multiplier).sp),
        headlineMedium = BaseTypography.headlineMedium.copy(fontSize = (BaseTypography.headlineMedium.fontSize.value * multiplier).sp),
        headlineSmall = BaseTypography.headlineSmall.copy(fontSize = (BaseTypography.headlineSmall.fontSize.value * multiplier).sp),
        titleLarge = BaseTypography.titleLarge.copy(fontSize = (BaseTypography.titleLarge.fontSize.value * multiplier).sp),
        titleMedium = BaseTypography.titleMedium.copy(fontSize = (BaseTypography.titleMedium.fontSize.value * multiplier).sp),
        titleSmall = BaseTypography.titleSmall.copy(fontSize = (BaseTypography.titleSmall.fontSize.value * multiplier).sp),
        bodyLarge = BaseTypography.bodyLarge.copy(fontSize = (BaseTypography.bodyLarge.fontSize.value * multiplier).sp),
        bodyMedium = BaseTypography.bodyMedium.copy(fontSize = (BaseTypography.bodyMedium.fontSize.value * multiplier).sp),
        bodySmall = BaseTypography.bodySmall.copy(fontSize = (BaseTypography.bodySmall.fontSize.value * multiplier).sp),
        labelLarge = BaseTypography.labelLarge.copy(fontSize = (BaseTypography.labelLarge.fontSize.value * multiplier).sp),
        labelMedium = BaseTypography.labelMedium.copy(fontSize = (BaseTypography.labelMedium.fontSize.value * multiplier).sp),
        labelSmall = BaseTypography.labelSmall.copy(fontSize = (BaseTypography.labelSmall.fontSize.value * multiplier).sp)
    )
}

// Default typography for backward compatibility
val Typography = BaseTypography