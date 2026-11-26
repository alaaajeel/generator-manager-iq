package iq.alaa.generatormanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import iq.alaa.generatormanager.AppViewModel
import iq.alaa.generatormanager.data.Subscriber
import iq.alaa.generatormanager.ui.components.SimpleTopBar
import iq.alaa.generatormanager.ui.theme.Gold
import iq.alaa.generatormanager.ui.theme.Petrol
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSubscriberScreen(vm: AppViewModel, id: Long, onDone: () -> Unit, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val boards by vm.boards.collectAsStateWithLifecycle()
    var original by rememberSaveable { mutableStateOf<Long?>(null) }
    var name by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var amps by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("عادي") }
    var board by rememberSaveable { mutableStateOf("بورد 1") }
    var createdAt by rememberSaveable { mutableStateOf(0L) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(id) {
        if (id != 0L && original == null) {
            vm.subscriber(id)?.let { s ->
                name = s.name; code = s.code; phone = s.phone; amps = s.amps.toString()
                type = s.type; board = s.board; createdAt = s.createdAt
            }
            original = id
        } else if (id == 0L && original == null) {
            boards.firstOrNull()?.let { board = it }
            original = 0L
        }
    }

    Scaffold(topBar = { SimpleTopBar(if (id == 0L) "إضافة مشترك" else "تعديل المشترك", onBack) }) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(name, { name = it }, label = { Text("اسم المشترك") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    code, { code = it.take(10) }, label = { Text("رقم الجوزة") }, singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    amps, { v -> amps = v.filter { it.isDigit() }.take(3) }, label = { Text("الأمبيرات") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                phone, { phone = it.take(15) }, label = { Text("رقم الهاتف (للواتساب)") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(board, { board = it }, label = { Text("البورد") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            if (boards.size > 1) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    boards.take(4).forEach { b ->
                        FilterChip(selected = board == b, onClick = { board = b }, label = { Text(b) })
                    }
                }
            }
            Text("نوع الاشتراك", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("عادي", "ذهبي", "تجاري").forEach { t ->
                    FilterChip(
                        selected = type == t, onClick = { type = t }, label = { Text(t) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Gold, selectedLabelColor = Petrol)
                    )
                }
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            Button(
                onClick = {
                    scope.launch {
                        val s = Subscriber(
                            id = id, code = code, name = name, phone = phone,
                            amps = amps.toIntOrNull() ?: 0, type = type, board = board,
                            createdAt = if (createdAt == 0L) System.currentTimeMillis() else createdAt
                        )
                        val err = vm.saveSubscriber(s)
                        if (err == null) onDone() else error = err
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Petrol)
            ) { Text("حفظ", fontWeight = FontWeight.Bold, fontSize = 17.sp) }
            Text(
                "ملاحظة: تغيير الأمبير يؤثر على الفواتير القادمة فقط، الفواتير الصادرة لا تتغير.",
                fontSize = 12.sp, color = iq.alaa.generatormanager.ui.theme.Muted
            )
        }
    }
}
