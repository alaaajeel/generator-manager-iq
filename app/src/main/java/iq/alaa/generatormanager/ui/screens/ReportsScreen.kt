package iq.alaa.generatormanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import iq.alaa.generatormanager.AppViewModel
import iq.alaa.generatormanager.domain.Money
import iq.alaa.generatormanager.domain.Periods
import iq.alaa.generatormanager.ui.components.BarChart
import iq.alaa.generatormanager.ui.components.InfoRow
import iq.alaa.generatormanager.ui.components.SectionCard
import iq.alaa.generatormanager.ui.components.SimpleTopBar
import iq.alaa.generatormanager.ui.theme.Late
import iq.alaa.generatormanager.ui.theme.Muted
import iq.alaa.generatormanager.ui.theme.Paid

@Composable
fun ReportsScreen(vm: AppViewModel) {
    val subs by vm.subscribers.collectAsStateWithLifecycle()
    val revenue by vm.revenueByMonth.collectAsStateWithLifecycle()
    val expenses by vm.expensesByMonth.collectAsStateWithLifecycle()
    val invoiced by vm.monthInvoiced.collectAsStateWithLifecycle()
    val collected by vm.monthCollected.collectAsStateWithLifecycle()

    val periods = (revenue.map { it.period } + expenses.map { it.period }).distinct().sortedDescending().take(12)
    val debts = subs.sumOf { it.balance.coerceAtLeast(0) }
    val lateCount = subs.count { it.balance > 0 }
    val byBoard = subs.groupBy { it.subscriber.board }

    Scaffold(topBar = { SimpleTopBar("التقارير") }) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionCard {
                Text("الشهر الحالي — ${Periods.label(vm.currentMonth)}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                InfoRow("الفواتير الصادرة", Money.iqd(invoiced))
                InfoRow("المقبوض", Money.iqd(collected))
                InfoRow("نسبة التحصيل", if (invoiced > 0) "${collected * 100 / invoiced}%" else "—")
                InfoRow("إجمالي الديون الحالية", Money.iqd(debts))
                InfoRow("المشتركون المتأخرون", "$lateCount من ${subs.size}")
            }
            SectionCard {
                Text("المقبوض شهرياً", fontWeight = FontWeight.Bold)
                BarChart(revenue.reversed().map { Periods.shortLabel(it.period) to it.total }, Modifier.fillMaxWidth().height(140.dp))
            }
            SectionCard {
                Text("ملخص الأشهر", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row {
                    listOf("الشهر", "المقبوض", "المصروف", "الصافي").forEach {
                        Text(it, color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    }
                }
                HorizontalDivider()
                if (periods.isEmpty()) Text("لا توجد بيانات بعد", color = Muted)
                periods.forEach { p ->
                    val r = revenue.firstOrNull { it.period == p }?.total ?: 0L
                    val e = expenses.firstOrNull { it.period == p }?.total ?: 0L
                    Row(Modifier.padding(vertical = 6.dp)) {
                        Text(Periods.label(p), fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                        Text(Money.plain(r), fontSize = 12.sp, color = Paid, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                        Text(Money.plain(e), fontSize = 12.sp, color = Late, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                        Text(Money.plain(r - e), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    }
                }
            }
            if (byBoard.size > 1) {
                SectionCard {
                    Text("حسب البورد", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    byBoard.forEach { (board, list) ->
                        InfoRow("$board (${list.size} مشترك)", "ديون ${Money.plain(list.sumOf { it.balance.coerceAtLeast(0) })}")
                    }
                }
            }
        }
    }
}
