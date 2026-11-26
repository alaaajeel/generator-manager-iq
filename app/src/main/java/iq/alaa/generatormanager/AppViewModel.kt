package iq.alaa.generatormanager

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import iq.alaa.generatormanager.data.Expense
import iq.alaa.generatormanager.data.Payment
import iq.alaa.generatormanager.data.Prefs
import iq.alaa.generatormanager.data.Repository
import iq.alaa.generatormanager.data.Subscriber
import iq.alaa.generatormanager.data.SubscriberRow
import iq.alaa.generatormanager.data.Tariff
import iq.alaa.generatormanager.domain.Periods
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

class AppViewModel(app: Application) : AndroidViewModel(app) {
    val repo: Repository = (app as GeneratorApp).repository
    private val dao = repo.dao
    val prefs = Prefs(app)

    var siteName by mutableStateOf(prefs.siteName)
        private set
    var darkMode by mutableStateOf(prefs.darkMode)
        private set

    val currentMonth: YearMonth = YearMonth.now()
    val currentPeriod: String = Periods.key(currentMonth)
    private val range = Repository.monthRange(currentMonth)

    private fun <T> asState(flow: kotlinx.coroutines.flow.Flow<T>, initial: T): StateFlow<T> =
        flow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)

    val subscribers: StateFlow<List<SubscriberRow>> = asState(dao.subscriberRows(), emptyList())
    val boards: StateFlow<List<String>> = asState(dao.boards(), emptyList())
    val latestTariff: StateFlow<Tariff?> = asState(dao.latestTariff(), null)
    val monthCollected: StateFlow<Long> = asState(dao.paymentsBetween(range.first, range.second), 0L)
    val monthInvoiced: StateFlow<Long> = asState(dao.invoicedInPeriod(currentPeriod), 0L)
    val revenueByMonth = asState(dao.paymentsByMonth(), emptyList())
    val expensesByMonth = asState(dao.expensesByMonth(), emptyList())

    fun expensesFor(period: String) = dao.expensesFor(period)
    fun expenseTotalsFor(period: String) = dao.expenseTotalsFor(period)
    fun paymentsFor(id: Long) = dao.paymentsFor(id)
    fun invoicesFor(id: Long) = dao.invoicesFor(id)

    suspend fun subscriber(id: Long): Subscriber? = dao.subscriber(id)
    suspend fun saveSubscriber(s: Subscriber): String? = repo.saveSubscriber(s)
    fun archive(id: Long) = viewModelScope.launch { repo.archiveSubscriber(id) }

    suspend fun tariffFor(period: String): Tariff? = dao.tariff(period)
    suspend fun applyTariff(period: String, price: Long) = repo.applyTariff(period, price)
    suspend fun recordPayment(subId: Long, amount: Long, note: String): Payment = repo.recordPayment(subId, amount, note)

    fun addExpense(category: String, amount: Long, period: String, note: String) = viewModelScope.launch {
        dao.insertExpense(Expense(category = category, amount = amount, period = period, note = note))
    }
    fun deleteExpense(id: Long) = viewModelScope.launch { dao.deleteExpense(id) }

    fun updateSettings(name: String, phone: String, footer: String, dark: Boolean) {
        prefs.siteName = name.ifBlank { "مولدة الحي" }
        prefs.ownerPhone = phone
        prefs.receiptFooter = footer
        prefs.darkMode = dark
        siteName = prefs.siteName
        darkMode = dark
    }

    /** أيام التجربة المتبقية (عرض فقط حتى ربط الخادم) */
    val trialDaysLeft: Int
        get() {
            val days = ((System.currentTimeMillis() - prefs.firstRunAt) / 86_400_000L).toInt()
            return (30 - days).coerceAtLeast(0)
        }
}
