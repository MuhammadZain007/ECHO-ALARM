package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Helper function to resolve the perfect custom ColorScheme based on selected theme
fun getThemeColorScheme(themeName: String, systemDark: Boolean): ColorScheme {
    return when (themeName) {
        "Neon Cyberpunk" -> darkColorScheme(
            primary = CyberpunkPrimary,
            secondary = CyberpunkSecondary,
            tertiary = CyberpunkAccent,
            background = CyberpunkBg,
            surface = CyberpunkSurface,
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = Color(0xFFF0F0FF),
            onSurface = Color(0xFFE2E8F0),
            surfaceVariant = Color(0xFF1E113E),
            onSurfaceVariant = Color(0xFFCCB3FF)
        )
        "Matrix Green" -> darkColorScheme(
            primary = MatrixPrimary,
            secondary = MatrixSecondary,
            tertiary = MatrixAccent,
            background = MatrixBg,
            surface = MatrixSurface,
            onPrimary = Color.Black,
            onSecondary = Color.White,
            onBackground = Color(0xFF00FF41),
            onSurface = Color(0xFF00FF41),
            surfaceVariant = Color(0xFF021702),
            onSurfaceVariant = Color(0xFF4DE85B)
        )
        "Glassmorphism" -> darkColorScheme(
            primary = GlassPrimary,
            secondary = GlassSecondary,
            tertiary = GlassAccent,
            background = GlassBg,
            surface = GlassSurface,
            onPrimary = Color.White,
            onSecondary = Color(0xFF1E1B4B),
            onBackground = Color.White,
            onSurface = Color.White,
            surfaceVariant = Color(0x2BFFFFFF),
            onSurfaceVariant = Color(0xFFE2D6F5)
        )
        "Minimal White" -> lightColorScheme(
            primary = MinimalPrimary,
            secondary = MinimalSecondary,
            tertiary = MinimalAccent,
            background = MinimalBg,
            surface = MinimalSurface,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF18181B),
            onSurface = Color(0xFF18181B),
            surfaceVariant = Color(0xFFE4E4E7),
            onSurfaceVariant = Color(0xFF27272A)
        )
        "Luxury Gold" -> darkColorScheme(
            primary = GoldPrimary,
            secondary = GoldSecondary,
            tertiary = GoldAccent,
            background = GoldBg,
            surface = GoldSurface,
            onPrimary = Color.Black,
            onSecondary = Color.White,
            onBackground = Color(0xFFF9F6F0),
            onSurface = Color(0xFFF5EFEB),
            surfaceVariant = Color(0xFF1D1711),
            onSurfaceVariant = Color(0xFFE5C060)
        )
        "Modern Steel" -> darkColorScheme(
            primary = SteelPrimary,
            secondary = SteelSecondary,
            tertiary = SteelAccent,
            background = SteelBg,
            surface = SteelSurface,
            onPrimary = Color(0xFF0F172A),
            onSecondary = Color(0xFF0F172A),
            onBackground = Color(0xFFF1F5F9),
            onSurface = Color(0xFFF1F5F9),
            surfaceVariant = Color(0xFF2E373E),
            onSurfaceVariant = Color(0xFFCBD5E1)
        )
        "Wooden Clock" -> darkColorScheme(
            primary = WoodPrimary,
            secondary = WoodSecondary,
            tertiary = WoodAccent,
            background = WoodBg,
            surface = WoodSurface,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFFFFF7EC),
            onSurface = Color(0xFFF3E9DC),
            surfaceVariant = Color(0xFF472808),
            onSurfaceVariant = Color(0xFFDDA15E)
        )
        "Futuristic Clock" -> darkColorScheme(
            primary = FuturePrimary,
            secondary = FutureSecondary,
            tertiary = FutureAccent,
            background = FutureBg,
            surface = FutureSurface,
            onPrimary = Color.Black,
            onSecondary = Color.White,
            onBackground = Color(0xFFE0F2FE),
            onSurface = Color(0xFFE0F2FE),
            surfaceVariant = Color(0xFF112240),
            onSurfaceVariant = Color(0xFF00FFE0)
        )
        "Space Theme" -> darkColorScheme(
            primary = SpacePrimary,
            secondary = SpaceSecondary,
            tertiary = SpaceAccent,
            background = SpaceBg,
            surface = SpaceSurface,
            onPrimary = Color(0xFF0F172A),
            onSecondary = Color.White,
            onBackground = Color(0xFFF8FAFC),
            onSurface = Color(0xFFF1F5F9),
            surfaceVariant = Color(0xFF15193B),
            onSurfaceVariant = Color(0xFFE2E8F0)
        )
        "Galaxy Theme" -> darkColorScheme(
            primary = GalaxyPrimary,
            secondary = GalaxySecondary,
            tertiary = GalaxyAccent,
            background = GalaxyBg,
            surface = GalaxySurface,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFFFAF5FF),
            onSurface = Color(0xFFFAF5FF),
            surfaceVariant = Color(0xFF2E104A),
            onSurfaceVariant = Color(0xFFFFCCFF)
        )
        "Weather Theme" -> darkColorScheme(
            primary = WeatherPrimary,
            secondary = WeatherSecondary,
            tertiary = WeatherAccent,
            background = WeatherBg,
            surface = WeatherSurface,
            onPrimary = Color(0xFF082F49),
            onSecondary = Color(0xFF78350F),
            onBackground = Color(0xFFF0F9FF),
            onSurface = Color(0xFFF0F9FF),
            surfaceVariant = Color(0xFF1E293B),
            onSurfaceVariant = Color(0xFF38BDF8)
        )
        "Nature Theme" -> darkColorScheme(
            primary = NaturePrimary,
            secondary = NatureSecondary,
            tertiary = NatureAccent,
            background = NatureBg,
            surface = NatureSurface,
            onPrimary = Color(0xFF022C22),
            onSecondary = Color.White,
            onBackground = Color(0xFFECFDF5),
            onSurface = Color(0xFFECFDF5),
            surfaceVariant = Color(0xFF142E20),
            onSurfaceVariant = Color(0xFF34D399)
        )
        "Anime Theme" -> lightColorScheme(
            primary = AnimePrimary,
            secondary = AnimeSecondary,
            tertiary = AnimeAccent,
            background = AnimeBg,
            surface = AnimeSurface,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFFF72585),
            onSurface = Color(0xFF3A0CA3),
            surfaceVariant = Color(0xFFFFD1E1),
            onSurfaceVariant = Color(0xFF7209B7)
        )
        "Gaming Theme" -> darkColorScheme(
            primary = GamingPrimary,
            secondary = GamingSecondary,
            tertiary = GamingAccent,
            background = GamingBg,
            surface = GamingSurface,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFFF8F9FA),
            onSurface = Color(0xFFE9ECEF),
            surfaceVariant = Color(0xFF282530),
            onSurfaceVariant = Color(0xFF06D6A0)
        )
        "Bold Typography" -> darkColorScheme(
            primary = BoldTypographyPrimary,
            secondary = BoldTypographySecondary,
            tertiary = BoldTypographyAccent,
            background = BoldTypographyBg,
            surface = BoldTypographySurface,
            onPrimary = Color(0xFF381E72), // Dark violet label text on FAB, etc
            onSecondary = Color.White,
            onBackground = BoldTypographyOnSurface,
            onSurface = BoldTypographyOnSurface,
            surfaceVariant = BoldTypographySurfaceVariant,
            onSurfaceVariant = BoldTypographyPrimary
        )
        else -> { // "AMOLED Black"/Default
            darkColorScheme(
                primary = AmoledPrimary,
                secondary = AmoledSecondary,
                tertiary = AmoledAccent,
                background = AmoledBg,
                surface = AmoledSurface,
                onPrimary = Color.White,
                onSecondary = Color.White,
                onBackground = Color.White,
                onSurface = Color.White,
                surfaceVariant = Color(0xFF16161B),
                onSurfaceVariant = Color(0xFF8E8E9F)
            )
        }
    }
}

@Composable
fun EchoAlarmTheme(
    themeName: String = "Bold Typography",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Treat the theme setting with precedence.
    // Notice Minimal White and Anime Theme are light-centric, all others are luxurious dark aesthetics
    val themeDark = when (themeName) {
        "Minimal White" -> false
        "Anime Theme" -> false
        else -> true // default to black/dark for premium clocks
    }
    
    val colorScheme = getThemeColorScheme(themeName, themeDark)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
