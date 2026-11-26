package iq.alaa.generatormanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import iq.alaa.generatormanager.AppViewModel
import iq.alaa.generatormanager.data.Repository
import iq.alaa.generatormanager.domain.Money
import iq.alaa.generatormanager.domain.Periods
import iq.alaa.generatormanager.ui.components.Dot
import iq.alaa.generatormanager.ui.components.NetDonut
import iq.alaa.generatormanager.ui.components.SectionCard
import iq.alaa.generatormanager.ui.components.SimpleTopBar
import iq.alaa.generatormanager.ui.theme.CardRed
import iq.alaa.generatormanager.ui.theme.Gold
import iq.alaa.generatormanager.ui.theme.Late
import iq.alaa.generatormanager.ui.theme.Muted
import iq.alaa.generatormanager.ui.theme.Paid
import iq.alaa.generatormanager.ui.theme.Petrol

val ExpenseCategories = listOf("وقود (كاز)", "صيانة", "أجور العمال", "زيوت وفلاتر", "أخرى")

fun categoryIcon(c: String): Pair<ImageVector, Color> = when (c) {
    "وقود (كاز)" -> Icons.Filled.LocalGasStation to Late
    "صيانة" -> Icons.Filled.Build to Color(0xFFF29E1F)
    "أجور العمال" -> Icons.Filled.Groups to Color(0xFF2F80ED)
    "زيوت وفلاتر" -> Icons.Filled.Opacity to Gold
    else -> Icons.Filled.MoreHoriz to Muted
}

/** شاشة الإدارة المالية والمصروفات */
@Composable
fun FinanceScreen(vm: AppViewModel, startWithAdd: Boolean, onBack: (() -> Unit)?) {
    var month by remember { mutableStateOf(vm.currentMonth) }
    val period = Periods.key(month)
    val range = Repository.monthRange(month)
    val revenue by remember(period) { vm.repo.dao.paymentsBetween(range.first, range.second) }.collectAsState(initial = 0L)
    val totals by remember(period) { vm.expenseTotalsFor(period) }.collectAsState(initial = emptyList())
    val items by remember(period) { vm.expensesFor(period) }.collectAsState(initial = emptyList())
    val totalExp = totals.sumOf { it.total }
    var showAdd by remember { mutableStateOf(startWithAdd) }
    var toDelete by remember { mutableStateOf<Long?>(null) }

    Scaffold(topBar = { SimpleTopBar("الإدارة المالية", onBack) }) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFEDF1F3)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { month = month.minusMonths(1) }) { Icon(Icons.Filled.ChevronRight, "السابق") }
                Text(Periods.label(month), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                IconButton(onClick = { month = month.plusMonths(1) }) { Icon(Icons.Filled.ChevronLeft, "التالي") }
            }
            SectionCard {
                NetDonut(revenue, totalExp, Modifier.fillMaxWidth().height(190.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Dot(Paid); Spacer(Modifier.width(6.dp))
                    Text("إجمالي المقبوض", modifier = Modifier.weight(1f)); Text(Money.iqd(revenue), fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Dot(Late); Spacer(Modifier.width(6.dp))
                    Text("إجمالي المصروفات", modifier = Modifier.weight(1f)); Text(Money.iqd(totalExp), fontWeight = FontWeight.Bold)
                }
                Text("الحساب هنا نقدي مبسط: المقبوض − المصروف لهذا الشهر.", fontSize = 11.sp, color = Muted)
            }
            SectionCard {
                Text("المصروفات حسب النوع", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                ExpenseCategories.forEach { c ->
                    val (icon, tint) = categoryIcon(c)
                    val v = totals.firstOrNull { it.category == c }?.total ?: 0L
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(c, modifier = Modifier.weight(1f))
                        Text(Money.iqd(v), fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(
                "إجمالي المصروفات\n${Money.iqd(totalExp)}",
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(CardRed).padding(14.dp),
                textAlign = TextAlign.Center, color = Late, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp
            )
            if (items.isNotEmpty()) {
                SectionCard {
                    Text("سجل المصروفات", fontWeight = FontWeight.Bold)
                    items.forEach { e ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("${e.category} — ${Money.iqd(e.amount)}", fontSize = 14.sp)
                                if (e.note.isNotBlank()) Text(e.note, fontSize = 12.sp, color = Muted)
                            }
                            IconButton(onClick = { toDelete = e.id }) { Icon(Icons.Filled.Delete, "حذف", tint = Muted) }
                        }
                    }
                }
            }
            Button(
                onClick = { showAdd = true },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Petrol)
            ) {
                Icon(Icons.Filled.Add, null)
                Text("  إضافة مصروف", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        }
    }

    if (showAdd) {
        AddExpenseDialog(
            periodLabel = Periods.label(month),
            onDismiss = { showAdd = false },
            onSave = { c, a, n -> vm.addExpense(c, a, period, n); showAdd = false }
        )
    }
    toDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("حذف المصروف؟") },
            text = { Text("استخدم الحذف فقط لتصحيح إدخال خاطئ.") },
            confirmButton = { TextButton(onClick = { vm.deleteExpense(id); toDelete = null }) { Text("حذف") } },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("إلغاء") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseDialog(periodLabel: String, onDismiss: () -> Unit, onSave: (String, Long, String) -> Unit) {
    var cat by remember { mutableStateOf(ExpenseCategories.first()) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("مصروف جديد — $periodLabel") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExpenseCategories.chunked(3).forEach { rowCats ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        rowCats.forEach { c ->
                            FilterChip(
                                selected = cat == c, onClick = { cat = c }, label = { Text(c, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Gold, selectedLabelColor = Petrol)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    amount, { v -> amount = v.filter { it.isDigit() }.take(10); error = false },
                    label = { Text("المبلغ (د.ع)") }, singleLine = true, isError = error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(note, { note = it }, label = { Text("ملاحظة") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = {
                val a = amount.toLongOrNull() ?: 0L
                if (a <= 0) error = true else onSave(cat, a, note.trim())
            }) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
