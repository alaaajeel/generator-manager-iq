package iq.alaa.generatormanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import iq.alaa.generatormanager.AppViewModel
import iq.alaa.generatormanager.domain.Billing
import iq.alaa.generatormanager.domain.Money
import iq.alaa.generatormanager.domain.Periods
import iq.alaa.generatormanager.ui.components.InfoRow
import iq.alaa.generatormanager.ui.components.SectionCard
import iq.alaa.generatormanager.ui.components.SimpleTopBar
import iq.alaa.generatormanager.ui.theme.CardGold
import iq.alaa.generatormanager.ui.theme.Gold
import iq.alaa.generatormanager.ui.theme.Muted
import iq.alaa.generatormanager.ui.theme.Petrol
import kotlinx.coroutines.launch

@Composable
fun TariffScreen(vm: AppViewModel, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val subs by vm.subscribers.collectAsStateWithLifecycle()
    var month by remember { mutableStateOf(vm.currentMonth) }
    val period = Periods.key(month)
    var priceText by remember { mutableStateOf("15000") }
    var existing by remember { mutableStateOf<Long?>(null) }
    var result by remember { mutableStateOf<String?>(null) }
    var confirm by remember { mutableStateOf(false) }

    LaunchedEffect(period) {
        val t = vm.tariffFor(period)
        existing = t?.pricePerAmp
        if (t != null) priceText = t.pricePerAmp.toString()
    }
    val price = priceText.toLongOrNull() ?: 0L
    val totalAmps = subs.sumOf { it.subscriber.amps }

    Scaffold(topBar = { SimpleTopBar("تسعيرة الأمبير", onBack) }) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // اختيار الشهر
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(androidx.compose.ui.graphics.Color(0xFFEDF1F3)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { month = month.minusMonths(1) }) { Icon(Icons.Filled.ChevronRight, "السابق") }
                Text(Periods.label(month), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                IconButton(onClick = { month = month.plusMonths(1) }) { Icon(Icons.Filled.ChevronLeft, "التالي") }
            }

            Text("سعر الأمبير الواحد", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                FilledTonalIconButton(onClick = { priceText = (price + 500).toString() }) { Icon(Icons.Filled.Add, "زيادة") }
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { v -> priceText = v.filter { it.isDigit() }.take(7) },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(170.dp).padding(horizontal = 10.dp)
                )
                FilledTonalIconButton(onClick = { priceText = (price - 500).coerceAtLeast(0).toString() }) { Icon(Icons.Filled.Remove, "نقصان") }
            }
            Text(
                if (existing != null) "سعر هذا الشهر محفوظ: ${Money.iqd(existing!!)} — الفواتير الصادرة لا تتغير"
                else "سيتم إصدار فواتير لجميع المشتركين النشطين لهذا الشهر",
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(CardGold).padding(10.dp),
                textAlign = TextAlign.Center, fontSize = 13.sp, color = Petrol
            )

            SectionCard {
                Text("معاينة سريعة", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                listOf(1, 2, 3, 5, 10).forEach { a -> InfoRow("$a أمبير", Money.iqd(Billing.invoiceAmount(a, price))) }
            }
            SectionCard {
                InfoRow("عدد المشتركين", subs.size.toString())
                InfoRow("مجموع الأمبيرات المتعاقد عليها", totalAmps.toString())
                InfoRow("إجمالي الفواتير المتوقع", Money.iqd(Billing.invoiceAmount(totalAmps, price)))
            }

            result?.let { Text(it, color = Muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }

            Button(
                onClick = { confirm = true },
                enabled = price > 0,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Petrol)
            ) {
                Icon(Icons.Filled.Check, null)
                Text("  حفظ وتطبيق السعر", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        }
    }

    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("تأكيد التسعيرة") },
            text = { Text("سعر الأمبير ${Money.iqd(price)} لشهر ${Periods.label(month)}.\nتصدر الفواتير لمن ليس له فاتورة في هذا الشهر فقط.") },
            confirmButton = {
                TextButton(onClick = {
                    confirm = false
                    scope.launch {
                        val r = vm.applyTariff(period, price)
                        existing = price
                        result = "تم إصدار ${r.created} فاتورة" + if (r.skipped > 0) " • ${r.skipped} مشترك لديه فاتورة سابقة لهذا الشهر لم تتغير" else ""
                    }
                }) { Text("تطبيق") }
            },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("إلغاء") } }
        )
    }
}
