package iq.alaa.generatormanager.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.YearMonth

class BillingTest {

    @Test fun amountExamplesFromPrd() {
        assertEquals(45_000L, Billing.invoiceAmount(3, 15_000))
        assertEquals(30_000L, Billing.invoiceAmount(2, 15_000))
    }

    @Test fun priceDropAffectsOnlyNewPeriod() {
        // الفاتورة القديمة لقطة محفوظة؛ السعر الجديد ينتج مبالغ جديدة فقط
        val oldInvoice = Billing.invoiceAmount(3, 15_000)
        val newInvoice = Billing.invoiceAmount(3, 10_000)
        assertEquals(45_000L, oldInvoice)
        assertEquals(30_000L, newInvoice)
        assertEquals(20_000L, Billing.invoiceAmount(2, 10_000))
    }

    @Test fun partialPaymentLeavesDebt() {
        val bal = Billing.balance(totalInvoiced = 45_000, totalPaid = 20_000)
        assertEquals(25_000L, bal)
        assertEquals(PayStatus.LATE, Billing.status(bal))
    }

    @Test fun advancePaymentIsCredit() {
        val bal = Billing.balance(45_000, 60_000)
        assertEquals(-15_000L, bal)
        assertEquals(PayStatus.PAID, Billing.status(bal))
    }

    @Test fun carryOverDoesNotDuplicateDebt() {
        // شهران: فاتورة 45 ألف كل شهر، دفع 45 ألف فقط => الدين 45 ألف وليس 90
        val invoiced = Billing.invoiceAmount(3, 15_000) * 2
        assertEquals(45_000L, Billing.balance(invoiced, 45_000))
    }

    @Test fun applyingTariffTwiceDoesNotDuplicateInvoices() {
        val active = listOf(1L, 2L, 3L)
        val first = Billing.subscribersToInvoice(active, emptySet())
        assertEquals(listOf(1L, 2L, 3L), first)
        val second = Billing.subscribersToInvoice(active, first.toSet())
        assertEquals(emptyList<Long>(), second)
    }

    @Test fun paymentValidation() {
        assertNotNull(Billing.validatePayment(0))
        assertNotNull(Billing.validatePayment(-5))
        assertNull(Billing.validatePayment(45_000))
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeAmpsRejected() { Billing.invoiceAmount(-1, 15_000) }

    @Test fun periodsAndMoney() {
        assertEquals("2026-11", Periods.key(YearMonth.of(2026, 11)))
        assertEquals("تشرين الثاني 2026", Periods.label("2026-11"))
        assertEquals("45,000 د.ع", Money.iqd(45_000))
    }
}
