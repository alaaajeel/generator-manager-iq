package iq.alaa.generatormanager.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import iq.alaa.generatormanager.domain.Money
import iq.alaa.generatormanager.ui.theme.Gold
import iq.alaa.generatormanager.ui.theme.Late
import iq.alaa.generatormanager.ui.theme.LateBg
import iq.alaa.generatormanager.ui.theme.Muted
import iq.alaa.generatormanager.ui.theme.Paid
import iq.alaa.generatormanager.ui.theme.PaidBg
import iq.alaa.generatormanager.ui.theme.Petrol

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTopBar(title: String, onBack: (() -> Unit)? = null) {
    CenterAlignedTopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 19.sp) },
        navigationIcon = {
            if (onBack != null) IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun StatusBadge(paid: Boolean) {
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (paid) Paid else Late)
            .padding(horizontal = 12.dp, vertical = 3.dp)
    ) {
        Text(if (paid) "مسدد" else "متأخر", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    container: Color,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontWeight = FontWeight.Bold, color = Petrol, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, color = valueColor, fontSize = 21.sp)
        }
    }
}

@Composable
fun ActionTile(title: String, icon: ImageVector, tint: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(34.dp))
            Spacer(Modifier.height(6.dp))
            Text(title, fontWeight = FontWeight.Bold, color = Petrol, fontSize = 16.sp)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, icon: ImageVector? = null) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, tint = Gold, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(label, color = Muted, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

/** رسم أعمدة بسيط لإيرادات الأشهر */
@Composable
fun BarChart(values: List<Pair<String, Long>>, modifier: Modifier = Modifier) {
    if (values.isEmpty()) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text("لا توجد بيانات بعد — سجّل أول قبض", color = Muted, fontSize = 13.sp)
        }
        return
    }
    val max = (values.maxOf { it.second }).coerceAtLeast(1)
    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().weight(1f)) {
            val n = values.size
            val slot = size.width / n
            val barW = slot * 0.55f
            values.forEachIndexed { i, (_, v) ->
                val h = size.height * (v.toFloat() / max)
                val x = i * slot + (slot - barW) / 2
                drawRoundRect(
                    color = Color(0xFF2BB673).copy(alpha = 0.55f + 0.45f * (i + 1) / n),
                    topLeft = Offset(x, size.height - h),
                    size = Size(barW, h),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
        }
        Row(Modifier.fillMaxWidth()) {
            values.forEach { (label, _) ->
                Text(label, fontSize = 9.sp, color = Muted, textAlign = TextAlign.Center, modifier = Modifier.weight(1f), maxLines = 1)
            }
        }
    }
}

/** حلقة صافي الربح: الأخضر للإيرادات والأحمر للمصروفات */
@Composable
fun NetDonut(revenue: Long, expenses: Long, modifier: Modifier = Modifier) {
    val net = revenue - expenses
    val total = (revenue + expenses).coerceAtLeast(1)
    val greenSweep = 360f * revenue / total
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(8.dp)) {
            val stroke = Stroke(width = size.minDimension * 0.11f)
            drawArc(Color(0xFFEDF1F3), 0f, 360f, false, style = stroke)
            if (revenue + expenses > 0) {
                drawArc(Paid, -90f, greenSweep, false, style = stroke)
                drawArc(Late, -90f + greenSweep, 360f - greenSweep, false, style = stroke)
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("صافي الربح", fontSize = 13.sp, color = Muted)
            Text(Money.plain(net), fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = if (net >= 0) Petrol else Late)
            Text("د.ع", fontSize = 12.sp, color = Muted)
        }
    }
}

@Composable
fun Dot(color: Color) {
    Box(Modifier.size(10.dp).clip(CircleShape).background(color))
}

@Composable
fun SectionCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) { content() }
    }
}

fun statusColors(paid: Boolean) = if (paid) Paid to PaidBg else Late to LateBg
