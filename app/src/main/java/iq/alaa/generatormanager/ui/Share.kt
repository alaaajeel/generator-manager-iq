package iq.alaa.generatormanager.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import iq.alaa.generatormanager.data.Payment
import iq.alaa.generatormanager.data.Subscriber
import iq.alaa.generatormanager.domain.Money
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Share {
    // نحتفظ بمرجع الـ WebView حتى لا يُحذف قبل انتهاء التحميل
    private var printWebView: WebView? = null

    /** يحوّل 07xxxxxxxxx إلى 9647xxxxxxxxx لرابط واتساب */
    fun iraqiPhone(raw: String): String {
        val d = raw.filter { it.isDigit() }
        return when {
            d.startsWith("964") -> d
            d.startsWith("00964") -> d.removePrefix("00")
            d.startsWith("0") -> "964" + d.drop(1)
            d.length == 10 && d.startsWith("7") -> "964$d"
            else -> d
        }
    }

    fun whatsapp(context: Context, phone: String, text: String) {
        val p = iraqiPhone(phone)
        val uri = if (p.isNotEmpty())
            Uri.parse("https://wa.me/$p?text=" + Uri.encode(text))
        else Uri.parse("https://wa.me/?text=" + Uri.encode(text))
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: ActivityNotFoundException) {
            val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
            context.startActivity(Intent.createChooser(send, "مشاركة").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    private fun date(ms: Long) = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US).format(Date(ms))

    fun receiptText(site: String, s: Subscriber, p: Payment, balanceAfter: Long, footer: String): String = buildString {
        appendLine("🧾 وصل قبض رقم ${p.receiptNo}")
        appendLine(site)
        appendLine("المشترك: ${s.name}")
        appendLine("رقم الجوزة: ${s.code} — ${s.board}")
        appendLine("المبلغ المستلم: ${Money.iqd(p.amount)}")
        appendLine("الرصيد المتبقي: ${Money.iqd(balanceAfter.coerceAtLeast(0))}")
        if (balanceAfter < 0) appendLine("رصيد مقدم: ${Money.iqd(-balanceAfter)}")
        appendLine("التاريخ: ${date(p.createdAt)}")
        append(footer)
    }

    fun balanceText(site: String, s: Subscriber, balance: Long): String = buildString {
        appendLine("مرحباً ${s.name}")
        appendLine("$site — رقم الجوزة ${s.code}")
        if (balance > 0) append("المبلغ المستحق: ${Money.iqd(balance)}")
        else append("حسابك مسدد، شكراً لك ✅")
    }

    /** طباعة الوصل عبر نظام الطباعة في أندرويد (PDF أو أي طابعة مدعومة) */
    fun printReceipt(context: Context, site: String, s: Subscriber, p: Payment, balanceAfter: Long, footer: String) {
        val html = """
            <html dir="rtl"><head><meta charset="utf-8">
            <style>
              body{font-family:sans-serif;width:72mm;margin:0 auto;font-size:13px;color:#000}
              h2{text-align:center;margin:4px 0} .c{text-align:center}
              table{width:100%;border-collapse:collapse} td{padding:3px 0}
              .amt{font-size:18px;font-weight:bold;text-align:center;border:1px dashed #000;padding:6px;margin:6px 0}
            </style></head><body>
            <h2>${esc(site)}</h2>
            <div class="c">وصل قبض رقم <b>${p.receiptNo}</b></div><hr>
            <table>
              <tr><td>المشترك</td><td>${esc(s.name)}</td></tr>
              <tr><td>رقم الجوزة</td><td>${esc(s.code)} — ${esc(s.board)}</td></tr>
              <tr><td>الأمبير</td><td>${s.amps}</td></tr>
              <tr><td>التاريخ</td><td>${date(p.createdAt)}</td></tr>
            </table>
            <div class="amt">${Money.iqd(p.amount)}</div>
            <table><tr><td>الرصيد المتبقي</td><td>${Money.iqd(balanceAfter.coerceAtLeast(0))}</td></tr></table>
            <hr><div class="c">${esc(footer)}</div>
            </body></html>
        """.trimIndent()
        val web = WebView(context)
        printWebView = web
        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String?) {
                val pm = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                pm.print("receipt_${p.receiptNo}", view.createPrintDocumentAdapter("receipt_${p.receiptNo}"),
                    PrintAttributes.Builder().build())
            }
        }
        web.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
        Toast.makeText(context, "جاري تجهيز الطباعة…", Toast.LENGTH_SHORT).show()
    }

    private fun esc(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
}
