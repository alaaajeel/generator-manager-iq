package iq.alaa.generatormanager.domain

import java.text.NumberFormat
import java.time.YearMonth
import java.util.Locale

/** حالة المشترك المالية */
enum class PayStatus { PAID, LATE }

/**
 * قواعد الفوترة الأساسية — كل المبالغ بالدينار كأعداد صحيحة (Long).
 * لا نستخدم Double في أي حساب مالي.
 */
object Billing {

    /** مبلغ الفاتورة = عدد الأمبيرات × سعر الأمبير. */
    fun invoiceAmount(amps: Int, pricePerAmp: Long): Long {
        require(amps >= 0) { "عدد الأمبيرات لا يمكن أن يكون سالباً" }
        require(pricePerAmp >= 0) { "السعر لا يمكن أن يكون سالباً" }
        return Math.multiplyExact(amps.toLong(), pricePerAmp)
    }

    /**
     * الرصيد مشتق من الدفتر دائماً (لا يُخزّن): مجموع الفواتير − مجموع الدفعات.
     * موجب = دين على المشترك، سالب = دفعة مقدمة.
     */
    fun balance(totalInvoiced: Long, totalPaid: Long): Long = totalInvoiced - totalPaid

    fun status(balance: Long): PayStatus = if (balance > 0) PayStatus.LATE else PayStatus.PAID

    /**
     * المشتركون الذين يحتاجون فاتورة لهذه الفترة: النشطون الذين ليست لهم فاتورة بعد.
     * هذا يمنع ازدواج الفاتورة عند الضغط على "تطبيق السعر" أكثر من مرة.
     */
    fun subscribersToInvoice(activeIds: List<Long>, alreadyInvoiced: Set<Long>): List<Long> =
        activeIds.filter { it !in alreadyInvoiced }

    /** التحقق من مبلغ القبض. يرجع رسالة خطأ أو null. */
    fun validatePayment(amount: Long): String? = when {
        amount <= 0 -> "أدخل مبلغاً أكبر من صفر"
        amount > 100_000_000 -> "المبلغ كبير بشكل غير منطقي"
        else -> null
    }
}

object Periods {
    private val months = listOf(
        "كانون الثاني", "شباط", "آذار", "نيسان", "أيار", "حزيران",
        "تموز", "آب", "أيلول", "تشرين الأول", "تشرين الثاني", "كانون الأول"
    )

    fun key(ym: YearMonth): String = "%04d-%02d".format(Locale.US, ym.year, ym.monthValue)
    fun parse(key: String): YearMonth = YearMonth.parse(key)
    fun label(ym: YearMonth): String = "${months[ym.monthValue - 1]} ${ym.year}"
    fun label(key: String): String = label(parse(key))
    fun shortLabel(key: String): String = months[parse(key).monthValue - 1]
}

object Money {
    private val fmt: NumberFormat = NumberFormat.getIntegerInstance(Locale.US)
    fun plain(v: Long): String = fmt.format(v)
    fun iqd(v: Long): String = "${fmt.format(v)} د.ع"
}
