package iq.alaa.generatormanager.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import iq.alaa.generatormanager.AppViewModel
import iq.alaa.generatormanager.data.SubscriberRow
import iq.alaa.generatormanager.domain.Money
import iq.alaa.generatormanager.ui.components.SimpleTopBar
import iq.alaa.generatormanager.ui.components.StatusBadge
import iq.alaa.generatormanager.ui.theme.Gold
import iq.alaa.generatormanager.ui.theme.Muted
import iq.alaa.generatormanager.ui.theme.Petrol

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscribersScreen(vm: AppViewModel, onOpen: (Long) -> Unit, onAdd: () -> Unit, onBack: (() -> Unit)?) {
    val all by vm.subscribers.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(0) } // 0 الكل، 1 مسدد، 2 متأخر

    val list = remember(all, query, filter) {
        val q = query.trim()
        all.filter { r ->
            val s = r.subscriber
            val match = q.isEmpty() || s.name.contains(q, ignoreCase = true) ||
                s.code == q || s.code.startsWith(q) || s.phone.contains(q) || s.board.contains(q)
            val st = when (filter) { 1 -> r.balance <= 0; 2 -> r.balance > 0; else -> true }
            match && st
        }
    }

    Scaffold(
        topBar = { SimpleTopBar("المشتركين (${all.size})", onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd, containerColor = Gold, contentColor = Petrol) {
                Icon(Icons.Filled.Add, "إضافة مشترك")
            }
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("البحث بالاسم أو رقم الجوزة أو الهاتف…") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
            )
            Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("الكل", "مسدد", "متأخر").forEachIndexed { i, t ->
                    FilterChip(
                        selected = filter == i,
                        onClick = { filter = i },
                        label = { Text(t) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Gold, selectedLabelColor = Petrol)
                    )
                }
            }
            if (list.isEmpty()) {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(if (all.isEmpty()) "لا يوجد مشتركين بعد" else "لا توجد نتائج", color = Muted, fontSize = 16.sp)
                    if (all.isEmpty()) Text("اضغط + لإضافة أول مشترك", color = Muted, fontSize = 13.sp)
                }
            } else {
                Card(
                    Modifier.padding(12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                        items(list, key = { it.subscriber.id }) { row ->
                            SubscriberItem(row) { onOpen(row.subscriber.id) }
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubscriberItem(row: SubscriberRow, onClick: () -> Unit) {
    val s = row.subscriber
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(s.code, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.width(44.dp))
        Column(Modifier.weight(1f)) {
            Text(s.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(2.dp))
            val amount = row.lastInvoice ?: 0L
            Text("${s.amps} أمبير - ${Money.iqd(amount)} • ${s.board}", color = Muted, fontSize = 12.sp)
        }
        StatusBadge(paid = row.balance <= 0)
    }
}
