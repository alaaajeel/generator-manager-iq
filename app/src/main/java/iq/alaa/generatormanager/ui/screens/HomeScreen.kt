package iq.alaa.generatormanager.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import iq.alaa.generatormanager.AppViewModel
import iq.alaa.generatormanager.R
import iq.alaa.generatormanager.domain.Money
import iq.alaa.generatormanager.domain.Periods
import iq.alaa.generatormanager.ui.components.ActionTile
import iq.alaa.generatormanager.ui.components.BarChart
import iq.alaa.generatormanager.ui.components.StatCard
import iq.alaa.generatormanager.ui.theme.CardBlue
import iq.alaa.generatormanager.ui.theme.CardGold
import iq.alaa.generatormanager.ui.theme.CardGreen
import iq.alaa.generatormanager.ui.theme.CardRed
import iq.alaa.generatormanager.ui.theme.Gold
import iq.alaa.generatormanager.ui.theme.Late
import iq.alaa.generatormanager.ui.theme.Paid
import iq.alaa.generatormanager.ui.theme.Petrol
import iq.alaa.generatormanager.ui.theme.PetrolDark
import iq.alaa.generatormanager.ui.theme.PetrolLight

@Composable
fun HomeScreen(
    vm: AppViewModel,
    onAddSubscriber: () -> Unit,
    onCollect: () -> Unit,
    onExpense: () -> Unit,
    onTariff: () -> Unit
) {
    val subs by vm.subscribers.collectAsStateWithLifecycle()
    val collected by vm.monthCollected.collectAsStateWithLifecycle()
    val expensesByMonth by vm.expensesByMonth.collectAsStateWithLifecycle()
    val revenue by vm.revenueByMonth.collectAsStateWithLifecycle()
    val monthExpenses = expensesByMonth.firstOrNull { it.period == vm.currentPeriod }?.total ?: 0L
    val net = collected - monthExpenses
    val debts = subs.sumOf { it.balance.coerceAtLeast(0) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Petrol, PetrolDark)))
            .verticalScroll(rememberScrollState())
    ) {
        // ----- الترويسة -----
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(PetrolLight),
                contentAlignment = Alignment.Center
            ) {
                Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.size(64.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("مدير المولدات", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                Text(vm.siteName, color = Gold, fontSize = 15.sp)
            }
        }

        // ----- شريط التجربة -----
        Row(
            Modifier
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF0E6B4A))
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.EmojiEvents, null, tint = Gold, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("حساب تجريبي - متبقي ${vm.trialDaysLeft} يوم", color = Color.White, fontSize = 13.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            Periods.label(vm.currentMonth),
            color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(10.dp))

        // ----- البطاقات -----
        Column(Modifier.padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("المشتركين", subs.size.toString(), Icons.Filled.Groups, Color(0xFF2F80ED), CardBlue, Petrol, Modifier.weight(1f))
                StatCard("الإيرادات", Money.iqd(collected), Icons.Filled.TrendingUp, Paid, CardGreen, Paid, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("صافي الربح", Money.iqd(net), Icons.Filled.Savings, GoldDarkTint, CardGold, if (net >= 0) Paid else Late, Modifier.weight(1f))
                StatCard("المصروفات", Money.iqd(monthExpenses), Icons.Filled.TrendingDown, Late, CardRed, Late, Modifier.weight(1f))
            }
            if (debts > 0) {
                Text(
                    "ديون غير محصلة: ${Money.iqd(debts)}",
                    color = Color(0xFFFFC9C9), fontSize = 13.sp, fontWeight = FontWeight.Bold
                )
            }

            // ----- الرسم البياني -----
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(14.dp)) {
                    Text("إيرادات الأشهر الأخيرة", fontWeight = FontWeight.Bold, color = Petrol, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    BarChart(
                        values = revenue.reversed().map { Periods.shortLabel(it.period) to it.total },
                        modifier = Modifier.fillMaxWidth().height(130.dp)
                    )
                }
            }

            // ----- الإجراءات السريعة -----
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionTile("إضافة مشترك", Icons.Filled.PersonAdd, Gold, Modifier.weight(1f), onAddSubscriber)
                ActionTile("استلام دفعة", Icons.Filled.Paid, Paid, Modifier.weight(1f), onCollect)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionTile("تسجيل مصروف", Icons.Filled.LocalGasStation, Late, Modifier.weight(1f), onExpense)
                ActionTile("تسعيرة الأمبير", Icons.Filled.Bolt, Gold, Modifier.weight(1f), onTariff)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

private val GoldDarkTint = Color(0xFFE0A21F)
