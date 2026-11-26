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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import iq.alaa.generatormanager.AppViewModel
import iq.alaa.generatormanager.data.Payment
import iq.alaa.generatormanager.domain.Billing
import iq.alaa.generatormanager.domain.Money
import iq.alaa.generatormanager.domain.Periods
import iq.alaa.generatormanager.ui.Share
import iq.alaa.generatormanager.ui.components.InfoRow
import iq.alaa.generatormanager.ui.components.SectionCard
import iq.alaa.generatormanager.ui.components.SimpleTopBar
import iq.alaa.generatormanager.ui.components.StatusBadge
import iq.alaa.generatormanager.ui.theme.Muted
import iq.alaa.generatormanager.ui.theme.Paid
import iq.alaa.generatormanager.ui.theme.Petrol
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SubscriberDetailScreen(vm: AppViewModel, id: Long, startWithPay: Boolean, onEdit: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val rows by vm.subscribers.collectAsStateWithLifecycle()
    val row = rows.firstOrNull { it.subscriber.id == id }
    val payments by remember(id) { vm.paymentsFor(id) }.collectAsState(initial = emptyList())
    val invoices by remember(id) { vm.invoicesFor(id) }.collectAsState(initial = emptyList())

    var showPay by remember { mutableStateOf(false) }
    var lastPayment by remember { mutableStateOf<Payment?>(null) }
    var confirmArchive by remember { mutableStateOf(false) }

    LaunchedEffect(startWithPay, row != null) { if (startWithPay && row != null) showPay = true }

    Scaffold(topBar = { SimpleTopBar("تفاصيل المشترك", onBack) }) { pad ->
        if (row == null) {
            Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) { Text("جارٍ التحميل…", color = Muted) }
            return@Scaffold
        }
        val s = row.subscriber
        val balance = row.balance
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(s.name, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        Text("رقم الجوزة: ${s.code} • ${s.board}", color = Muted, fontSize = 14.sp)
                        Spacer(Modifier.height(6.dp))
                        StatusBadge(paid = balance <= 0)
                    }
                    Box(Modifier.size(56.dp).clip(CircleShape).background(Color(0xFFEAF3F4)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Person, null, tint = Petrol, modifier = Modifier.size(34.dp))
                    }
                }
            }
            SectionCard {
                InfoRow("عدد الأمبيرات", s.amps.toString(), Icons.Filled.Bolt)
                InfoRow("نوع الاشتراك", s.type, Icons.Filled.Bolt)
                InfoRow("قيمة آخر فاتورة", Money.iqd(row.lastInvoice ?: 0), Icons.Filled.Payments)
                InfoRow("آخر دفعة", payments.firstOrNull()?.let { dateLabel(it.createdAt) } ?: "—", Icons.Filled.DateRange)
                InfoRow(
                    if (balance >= 0) "الرصيد المستحق" else "رصيد مقدم",
                    Money.iqd(kotlin.math.abs(balance)), Icons.Filled.AccountBalanceWallet
                )
                if (s.phone.isNotBlank()) InfoRow("الهاتف", s.phone)
            }

            BigButton("استلام دفعة", Icons.Filled.Paid, Paid, Color.White) { showPay = true }
            BigButton("إرسال واتساب", Icons.Filled.Chat, Color(0xFF25D366), Color.White) {
                Share.whatsapp(context, s.phone, Share.balanceText(vm.siteName, s, balance))
            }
            if (payments.isNotEmpty()) {
                BigButton("طباعة آخر وصل", Icons.Filled.Print, Color(0xFFE8EEF0), Petrol) {
                    val p = payments.first()
                    Share.printReceipt(context, vm.siteName, s, p, balance, vm.prefs.receiptFooter)
                }
            }
            BigButton("تعديل البيانات", Icons.Filled.Edit, Color(0xFFE8EEF0), Petrol, onEdit)

            if (invoices.isNotEmpty() || payments.isNotEmpty()) {
                SectionCard {
                    Text("كشف الحساب", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    invoices.take(12).forEach {
                        InfoRow("فاتورة ${Periods.label(it.period)} (${it.amps}×${Money.plain(it.pricePerAmp)})", "+ ${Money.plain(it.amount)}")
                    }
                    payments.take(12).forEach {
                        InfoRow("وصل ${it.receiptNo} — ${dateLabel(it.createdAt)}", "− ${Money.plain(it.amount)}")
                    }
                }
            }
            TextButton(onClick = { confirmArchive = true }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Icon(Icons.Filled.Archive, null, tint = Muted)
                Spacer(Modifier.width(4.dp))
                Text("أرشفة المشترك", color = Muted)
            }
        }

        if (showPay) {
            PayDialog(
                suggested = balance.coerceAtLeast(0),
                onDismiss = { showPay = false },
                onConfirm = { amount, note ->
                    scope.launch {
                        lastPayment = vm.recordPayment(s.id, amount, note)
                        showPay = false
                    }
                }
            )
        }
        lastPayment?.let { p ->
            val after = balance // يُحدَّث تلقائياً من قاعدة البيانات
            AlertDialog(
                onDismissRequest = { lastPayment = null },
                title = { Text("تم القبض ✅") },
                text = { Text("وصل رقم ${p.receiptNo}\nالمبلغ: ${Money.iqd(p.amount)}\nالمتبقي: ${Money.iqd(after.coerceAtLeast(0))}") },
                confirmButton = {
                    TextButton(onClick = { Share.printReceipt(context, vm.siteName, s, p, after, vm.prefs.receiptFooter) }) { Text("طباعة") }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = {
                            Share.whatsapp(context, s.phone, Share.receiptText(vm.siteName, s, p, after, vm.prefs.receiptFooter))
                        }) { Text("واتساب") }
                        TextButton(onClick = { lastPayment = null }) { Text("إغلاق") }
                    }
                }
            )
        }
        if (confirmArchive) {
            AlertDialog(
                onDismissRequest = { confirmArchive = false },
                title = { Text("أرشفة المشترك؟") },
                text = { Text("لن يظهر في القوائم ولن تصدر له فواتير جديدة. سجله المالي يبقى محفوظاً.") },
                confirmButton = { TextButton(onClick = { vm.archive(s.id); confirmArchive = false; onBack() }) { Text("أرشفة") } },
                dismissButton = { TextButton(onClick = { confirmArchive = false }) { Text("إلغاء") } }
            )
        }
    }
}

@Composable
private fun PayDialog(suggested: Long, onDismiss: () -> Unit, onConfirm: (Long, String) -> Unit) {
    var text by remember { mutableStateOf(if (suggested > 0) suggested.toString() else "") }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("استلام دفعة") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { v -> text = v.filter { it.isDigit() }.take(10); error = null },
                    label = { Text("المبلغ (د.ع)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = error != null,
                    supportingText = { error?.let { Text(it) } }
                )
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("ملاحظة (اختياري)") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(enabled = !busy, onClick = {
                val amount = text.toLongOrNull() ?: 0L
                val err = Billing.validatePayment(amount)
                if (err != null) error = err else { busy = true; onConfirm(amount, note.trim()) }
            }) { Text("تأكيد القبض") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
private fun BigButton(text: String, icon: ImageVector, bg: Color, fg: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = fg)
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Icon(icon, null)
    }
}

private fun dateLabel(ms: Long) = SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date(ms))
