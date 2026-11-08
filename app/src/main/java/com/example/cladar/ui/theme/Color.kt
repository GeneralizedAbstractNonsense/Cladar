package com.example.cladar.ui.theme

import androidx.compose.ui.graphics.Color

// Palette estratta dal codice HTML fornito
val BurgundyTitle = Color(0xFF822443)       // --c-TITLE / --c-INTES
val GridBackgroundLight = Color(0xFFD3DEE4) // --c-BAGRO (rgba 211,222,228)
val GridBorderLight = Color(0xFFC0D0E0)     // --c-BAGRA
val ColumnHeaderBg = Color(0xFFE8F0F8)

// Palette Esclusiva per Categorie / Eventi (dall'HTML)
val PaletteSageGreen  = Color(0xFFAAC9B6)   // --c-MATE (Verde Salvia)
val PaletteGoldAmber  = Color(0xFFD7A900)   // --c-SMIA (Giallo Oro)
val PaletteWarmOrange = Color(0xFFF69240)   // --c-LMMP (Arancio)
val PaletteTeal       = Color(0xFF00B3BE)   // --c-ESAME (Ottanio / Azzurro)
val PaletteLinenGray  = Color(0xFFD7D3C7)   // --c-UNATA (Grigio/Muted)

val EventColors = listOf(
    PaletteSageGreen,
    PaletteGoldAmber,
    PaletteWarmOrange,
    PaletteTeal,
    PaletteLinenGray
)

// Tema OLED Dark
val OLEDBlack = Color(0xFF000000)
val OLEDSurface = Color(0xFF121212)
val OLEDBorder = Color(0xFF2C2C2C)
