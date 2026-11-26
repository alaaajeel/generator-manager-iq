package iq.alaa.generatormanager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

val Petrol = Color(0xFF073B42)
val PetrolLight = Color(0xFF0E5560)
val PetrolDark = Color(0xFF042A30)
val Gold = Color(0xFFF4BD45)
val GoldDark = Color(0xFFE0A21F)
val Bg = Color(0xFFF5F7F9)
val Paid = Color(0xFF1E9E4A)
val PaidBg = Color(0xFFE3F6E9)
val Late = Color(0xFFE53935)
val LateBg = Color(0xFFFDE7E7)
val Muted = Color(0xFF6B7A80)
val CardBlue = Color(0xFFEAF3FB)
val CardGreen = Color(0xFFEAF7EE)
val CardRed = Color(0xFFFDECEC)
val CardGold = Color(0xFFFFF6E0)

private val LightColors = lightColorScheme(
    primary = Petrol,
    onPrimary = Color.White,
    secondary = Gold,
    onSecondary = Petrol,
    background = Bg,
    surface = Color.White,
    onSurface = Color(0xFF1B2629),
    surfaceVariant = Color(0xFFEDF1F3),
    error = Late
)

private val DarkColors = darkColorScheme(
    primary = Gold,
    onPrimary = Petrol,
    secondary = Gold,
    onSecondary = Petrol,
    background = PetrolDark,
    surface = Color(0xFF0B4A52),
    onSurface = Color(0xFFE8F0F1),
    surfaceVariant = Color(0xFF125861),
    error = Late
)

@Composable
fun GeneratorTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors) {
        // التطبيق عربي بالكامل: نفرض اتجاه من اليمين لليسار بغض النظر عن لغة الجهاز
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            content()
        }
    }
}
