package iq.alaa.generatormanager.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // ---------- المشتركون ----------
    @Query(
        """
        SELECT s.*,
          COALESCE((SELECT SUM(i.amount) FROM invoices i WHERE i.subscriberId = s.id), 0) AS invoiced,
          COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.subscriberId = s.id), 0) AS paid,
          (SELECT i2.amount FROM invoices i2 WHERE i2.subscriberId = s.id ORDER BY i2.period DESC LIMIT 1) AS lastInvoice
        FROM subscribers s
        WHERE s.active = 1
        ORDER BY s.board, CAST(s.code AS INTEGER), s.code
        """
    )
    fun subscriberRows(): Flow<List<SubscriberRow>>

    @Query("SELECT * FROM subscribers WHERE id = :id")
    suspend fun subscriber(id: Long): Subscriber?

    @Query("SELECT COUNT(*) FROM subscribers WHERE active = 1 AND board = :board AND code = :code AND id != :exceptId")
    suspend fun countActiveWithCode(board: String, code: String, exceptId: Long): Int

    @Query("SELECT id FROM subscribers WHERE active = 1")
    suspend fun activeSubscriberIds(): List<Long>

    @Insert
    suspend fun insertSubscriber(s: Subscriber): Long

    @Update
    suspend fun updateSubscriber(s: Subscriber)

    @Query("SELECT DISTINCT board FROM subscribers WHERE active = 1 ORDER BY board")
    fun boards(): Flow<List<String>>

    // ---------- التسعيرة والفواتير ----------
    @Query("SELECT * FROM tariffs WHERE period = :period")
    suspend fun tariff(period: String): Tariff?

    @Query("SELECT * FROM tariffs ORDER BY period DESC LIMIT 1")
    fun latestTariff(): Flow<Tariff?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTariff(t: Tariff)

    @Query("SELECT subscriberId FROM invoices WHERE period = :period")
    suspend fun invoicedSubscriberIds(period: String): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInvoice(i: Invoice): Long

    @Query("SELECT * FROM invoices WHERE subscriberId = :id ORDER BY period DESC")
    fun invoicesFor(id: Long): Flow<List<Invoice>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM invoices WHERE period = :period")
    fun invoicedInPeriod(period: String): Flow<Long>

    // ---------- القبض ----------
    @Query("SELECT COALESCE(MAX(receiptNo), 0) FROM payments")
    suspend fun maxReceiptNo(): Long

    @Insert
    suspend fun insertPayment(p: Payment): Long

    @Query("SELECT * FROM payments WHERE id = :id")
    suspend fun payment(id: Long): Payment?

    @Query("SELECT * FROM payments WHERE subscriberId = :id ORDER BY createdAt DESC")
    fun paymentsFor(id: Long): Flow<List<Payment>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE createdAt >= :from AND createdAt < :to")
    fun paymentsBetween(from: Long, to: Long): Flow<Long>

    @Query(
        """
        SELECT strftime('%Y-%m', createdAt / 1000, 'unixepoch', 'localtime') AS period,
               SUM(amount) AS total
        FROM payments GROUP BY period ORDER BY period DESC LIMIT 12
        """
    )
    fun paymentsByMonth(): Flow<List<PeriodTotal>>

    // ---------- المصروفات ----------
    @Insert
    suspend fun insertExpense(e: Expense): Long

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpense(id: Long)

    @Query("SELECT * FROM expenses WHERE period = :period ORDER BY createdAt DESC")
    fun expensesFor(period: String): Flow<List<Expense>>

    @Query("SELECT category, SUM(amount) AS total FROM expenses WHERE period = :period GROUP BY category ORDER BY total DESC")
    fun expenseTotalsFor(period: String): Flow<List<CategoryTotal>>

    @Query("SELECT period, SUM(amount) AS total FROM expenses GROUP BY period ORDER BY period DESC LIMIT 12")
    fun expensesByMonth(): Flow<List<PeriodTotal>>
}
