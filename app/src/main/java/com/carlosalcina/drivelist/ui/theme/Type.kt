package com.carlosalcina.drivelist.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.carlosalcina.drivelist.R

val Urbanist = FontFamily(
    Font(R.font.urbanist_regular),
    Font(R.font.urbanist_bold, weight = FontWeight.Bold),
    Font(R.font.urbanist_medium, weight = FontWeight.Medium)
)

val Poppins = FontFamily(
    Font(R.font.poppins_regular),
    Font(R.font.poppins_bold, weight = FontWeight.Bold),
    Font(R.font.poppins_medium, weight = FontWeight.Medium)
)
// Set of Material typography styles to start with


@Composable
fun typographyCustom(
    appTheme: ThemeOption
):Typography {
    val textColor = when (appTheme) {
        ThemeOption.LIGHT -> Color.Black
        ThemeOption.DARK -> Color.White
        ThemeOption.SYSTEM_DEFAULT -> Color.White
    }

    return Typography(
        displayLarge = TextStyle(
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 57.sp,
            color = textColor
        ),
        displayMedium = TextStyle(
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 45.sp,
            color = textColor
        ),
        displaySmall = TextStyle(
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 36.sp,
            color = textColor
        ),
        headlineLarge = TextStyle(
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = textColor
        ),
        headlineMedium = TextStyle(
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = textColor
        ),
        headlineSmall = TextStyle(
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = textColor
        ),
        titleLarge = TextStyle(
            fontFamily = Poppins,
            fontWeight = FontWeight.Medium,
            fontSize = 22.sp,
            color = textColor
        ),
        titleMedium = TextStyle(
            fontFamily = Poppins,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = textColor
        ),
        titleSmall = TextStyle(
            fontFamily = Poppins,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = textColor
        ),
        bodyLarge = TextStyle(
            fontFamily = Poppins,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = textColor
        ),
        bodyMedium = TextStyle(
            fontFamily = Poppins,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = textColor
        ),
        bodySmall = TextStyle(
            fontFamily = Poppins,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = textColor
        ),
        labelLarge = TextStyle(
            fontFamily = Poppins,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = textColor
        ),
        labelMedium = TextStyle(
            fontFamily = Poppins,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = textColor
        ),
        labelSmall = TextStyle(
            fontFamily = Poppins,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            color = textColor
        )
    )
}


/* Other default text styles to override
titleLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp
),
labelSmall = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp
)
*/