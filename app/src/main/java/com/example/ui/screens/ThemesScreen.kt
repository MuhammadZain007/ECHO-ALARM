package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.EchoAlarmViewModel
import com.example.ui.components.AnimatedClock

@Composable
fun ThemesScreen(
    viewModel: EchoAlarmViewModel,
    modifier: Modifier = Modifier
) {
    val currentTheme by viewModel.currentTheme.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }

    // Define all 15 hand-coded premium themes categorized
    val themeList = remember {
        listOf(
            // Digital Themes
            ThemeDetails("Bold Typography", "Digital", "Mega-sized ultra-bold time with lavender drop shadows", Color(0xFFD0BCFF), Color(0xFF050505)),
            ThemeDetails("Neon Cyberpunk", "Digital", "Vivid cyan/magenta neon wireframes", Color(0xFFFF007F), Color(0xFF00E5FF)),
            ThemeDetails("AMOLED Black", "Digital", "Perfect deep blacks, battery efficient", Color(0xFF5C5CFF), Color(0xFF0D0D0E)),
            ThemeDetails("Matrix Green", "Digital", "Falling green terminal rain", Color(0xFF00FF41), Color(0xFF011101)),
            ThemeDetails("Glassmorphic", "Digital", "Translucent frosted slate overlays", Color(0xFF9D4EDD), Color(0x33FFFFFF)),
            ThemeDetails("Minimal White", "Digital", "Warm paper background and fine ink", Color(0xFF111111), Color(0xFFF1F1F1)),
            
            // Analog Themes
            ThemeDetails("Luxury Gold", "Analog", "Champagne gold dial, polished bezel", Color(0xFFE5C060), Color(0xFF12100E)),
            ThemeDetails("Modern Steel", "Analog", "Brushed industrial metallic rivets", Color(0xFF94A3B8), Color(0xFF242B30)),
            ThemeDetails("Wooden Clock", "Analog", "Rich oak wood texture aesthetics", Color(0xFF8B5A2B), Color(0xFF361E02)),
            ThemeDetails("Futuristic Clock", "Analog", "Holographic quantum laser radial ticks", Color(0xFF00FFE0), Color(0xFF0A1329)),
            
            // Special Themes
            ThemeDetails("Space Theme", "Special", "Astronaut silver with comet dust", Color(0xFFECF0F1), Color(0xFF0E122A)),
            ThemeDetails("Galaxy Theme", "Special", "Nebula dust on stardust violet-pink", Color(0xFF9013FE), Color(0xFF1A0A2D)),
            ThemeDetails("Weather Theme", "Special", "Color morphs matching current sky conditions", Color(0xFF38BDF8), Color(0xFF1E293B)),
            ThemeDetails("Nature Theme", "Special", "Deep moss greens and organic sienna", Color(0xFF10B981), Color(0xFF112217)),
            ThemeDetails("Anime Theme", "Special", "Sweet sakura cel-shaded bubblegum outline", Color(0xFFF72585), Color(0xFFFFE3EC)),
            ThemeDetails("Gaming Theme", "Special", "E-sports aggressively active red matrix", Color(0xFFE63946), Color(0xFF1D1B22))
        )
    }

    // Filter list based on chosen category
    val filteredThemes = remember(selectedCategory) {
        if (selectedCategory == "All") themeList
        else themeList.filter { it.category == selectedCategory }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "ECHO THEME STORE",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 2.sp
        )

        // Live preview panel
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Palette, "Preview", tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Real-time Live Theme Preview",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                // Inline mini active clock matching the store configuration
                AnimatedClock(
                    themeName = currentTheme,
                    modifier = Modifier.height(180.dp)
                )

                Text(
                    text = "Current Applied Theme: $currentTheme",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Categories selector bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Digital", "Analog", "Special").forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f).testTag("theme_cat_chip_$cat")
                )
            }
        }

        // Theme Store Cards list Layout
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredThemes) { theme ->
                val isApplied = currentTheme == theme.name
                
                Card(
                    onClick = { viewModel.setTheme(theme.name) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isApplied)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.5.dp,
                            if (isApplied) MaterialTheme.colorScheme.primary
                            else Color.Transparent,
                            RoundedCornerShape(16.dp)
                        )
                        .testTag("theme_store_card_${theme.name}")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Color preview circles
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(16.dp).clip(RoundedCornerShape(4.dp)).background(theme.primaryAccent))
                                    Box(modifier = Modifier.size(16.dp).clip(RoundedCornerShape(4.dp)).background(theme.bgColor))
                                }

                                if (isApplied) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Applied",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = theme.name,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = if (isApplied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = theme.category.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = theme.description,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            lineHeight = 13.sp,
                            minLines = 2
                        )
                    }
                }
            }
        }
    }
}

data class ThemeDetails(
    val name: String,
    val category: String,
    val description: String,
    val primaryAccent: Color,
    val bgColor: Color
)
