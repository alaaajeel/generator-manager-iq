package iq.alaa.generatormanager.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "subscribers", indices = [Index(value = ["board", "code"])])
data class Subscriber(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,          // رقم الجوزة — فريد داخل البورد للمشتركين النشطين
    val name: String,
    val phone: String,
    val amps: Int,
    val type: String,          // عادي / ذهبي / تجاري
    val board: String,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

/** سعر الأمبير لكل شهر */
@Entity(tableName = "tariffs")
data class Tariff(
    @PrimaryKey val period: String, // yyyy-MM
    val pricePerAmp: Long,
    val updatedAt: Long = System.currentTimeMillis()
)

/** الفاتورة تحفظ لقطة من الأمبير والسعر — لا تتغير إذا تغيّر السعر لاحقاً */
@Entity(
    tableName = "invoices",
    indices = [Index(value = ["subscriberId", "period"], unique = true)]
)
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subscriberId: Long,
    val period: String,
    val amps: Int,
    val pricePerAmp: Long,
    val amount: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "payments",
    indices = [Index(value = ["receiptNo"], unique = true), Index(value = ["subscriberId"])]
)
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subscriberId: Long,
    val amount: Long,
    val receiptNo: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "expenses", indices = [Index(value = ["period"])])
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val amount: Long,
    val period: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class SubscriberRow(
    @Embedded val subscriber: Subscriber,
    @ColumnInfo(name = "invoiced") val invoiced: Long,
    @ColumnInfo(name = "paid") val paid: Long,
    @ColumnInfo(name = "lastInvoice") val lastInvoice: Long?
) {
    val balance: Long get() = invoiced - paid
}

data class PeriodTotal(
    @ColumnInfo(name = "period") val period: String,
    @ColumnInfo(name = "total") val total: Long
)

data class CategoryTotal(
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "total") val total: Long
)
