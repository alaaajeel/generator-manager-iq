package iq.alaa.generatormanager.data

import androidx.room.withTransaction
import iq.alaa.generatormanager.domain.Billing
import java.time.YearMonth
import java.time.ZoneId

class Repository(private val db: AppDatabase) {
    val dao = db.dao()

    /** يرجع رسالة خطأ أو null عند النجاح */
    suspend fun saveSubscriber(s: Subscriber): String? {
        if (s.name.isBlank()) return "اكتب اسم المشترك"
        if (s.code.isBlank()) return "اكتب رقم الجوزة"
        if (s.amps <= 0) return "عدد الأمبيرات يجب أن يكون 1 أو أكثر"
        val board = s.board.ifBlank { "بورد 1" }
        val dup = dao.countActiveWithCode(board, s.code.trim(), s.id)
        if (dup > 0) return "رقم الجوزة ${s.code} مستخدم في $board"
        val clean = s.copy(name = s.name.trim(), code = s.code.trim(), phone = s.phone.trim(), board = board)
        if (s.id == 0L) dao.insertSubscriber(clean) else dao.updateSubscriber(clean)
        return null
    }

    /** أرشفة بدل الحذف — لا حذف لمشترك له سجل مالي */
    suspend fun archiveSubscriber(id: Long) {
        val s = dao.subscriber(id) ?: return
        dao.updateSubscriber(s.copy(active = false))
    }

    data class ApplyResult(val created: Int, val skipped: Int)

    /**
     * حفظ سعر الشهر وإصدار فواتير لمن ليس له فاتورة في هذا الشهر.
     * الفواتير الصادرة سابقاً لا تتغير.
     */
    suspend fun applyTariff(period: String, price: Long): ApplyResult = db.withTransaction {
        dao.upsertTariff(Tariff(period, price))
        val active = dao.activeSubscriberIds()
        val already = dao.invoicedSubscriberIds(period).toSet()
        val todo = Billing.subscribersToInvoice(active, already)
        var created = 0
        for (id in todo) {
            val s = dao.subscriber(id) ?: continue
            val amount = Billing.invoiceAmount(s.amps, price)
            if (dao.insertInvoice(Invoice(subscriberId = id, period = period, amps = s.amps, pricePerAmp = price, amount = amount)) > 0) created++
        }
        ApplyResult(created, active.size - created)
    }

    /** تسجيل قبض مع رقم وصل متسلسل داخل معاملة واحدة */
    suspend fun recordPayment(subscriberId: Long, amount: Long, note: String): Payment = db.withTransaction {
        val next = dao.maxReceiptNo() + 1
        val p = Payment(subscriberId = subscriberId, amount = amount, receiptNo = next, note = note)
        val id = dao.insertPayment(p)
        p.copy(id = id)
    }

    companion object {
        fun monthRange(ym: YearMonth): Pair<Long, Long> {
            val zone = ZoneId.systemDefault()
            val from = ym.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val to = ym.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            return from to to
        }
    }
}
