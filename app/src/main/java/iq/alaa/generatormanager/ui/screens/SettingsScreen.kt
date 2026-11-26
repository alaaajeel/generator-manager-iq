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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import iq.alaa.generatormanager.AppViewModel
import iq.alaa.generatormanager.ui.components.SectionCard
import iq.alaa.generatormanager.ui.components.SimpleTopBar
import iq.alaa.generatormanager.ui.theme.Gold
import iq.alaa.generatormanager.ui.theme.Muted
import iq.alaa.generatormanager.ui.theme.Petrol

@Composable
fun SettingsScreen(vm: AppViewModel) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(vm.prefs.siteName) }
    var phone by remember { mutableStateOf(vm.prefs.ownerPhone) }
    var footer by remember { mutableStateOf(vm.prefs.receiptFooter) }
    var dark by remember { mutableStateOf(vm.prefs.darkMode) }

    Scaffold(topBar = { SimpleTopBar("الإعدادات") }) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(name, { name = it }, label = { Text("اسم المولدة / الموقع") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                phone, { phone = it }, label = { Text("هاتف صاحب المولدة") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(footer, { footer = it }, label = { Text("نص أسفل الوصل") }, modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("الوضع الليلي", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Switch(checked = dark, onCheckedChange = { dark = it })
            }
            Button(
                onClick = {
                    vm.updateSettings(name, phone, footer, dark)
                    Toast.makeText(context, "تم الحفظ", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Petrol)
            ) { Text("حفظ الإعدادات", fontWeight = FontWeight.Bold, fontSize = 16.sp) }

            SectionCard {
                Text("حالة هذه النسخة", fontWeight = FontWeight.Bold)
                Text(
                    "نسخة تجريبية أولى تعمل محلياً على الجهاز فقط.\n" +
                        "غير متوفر بعد: تسجيل الدخول بـ Google، المزامنة والنسخ الاحتياطي السحابي، " +
                        "بوابة المشترك، تعدد الموظفين والصلاحيات، طابعات POS عبر SDK.\n" +
                        "⚠️ حذف التطبيق يحذف البيانات — لا تستخدمها لبيانات حقيقية مهمة حالياً.",
                    fontSize = 13.sp, color = Muted
                )
            }
        }
    }
}
