package iq.alaa.generatormanager.data

import android.content.Context

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var siteName: String
        get() = sp.getString("siteName", "مولدة الحي") ?: "مولدة الحي"
        set(v) = sp.edit().putString("siteName", v).apply()

    var ownerPhone: String
        get() = sp.getString("ownerPhone", "") ?: ""
        set(v) = sp.edit().putString("ownerPhone", v).apply()

    var receiptFooter: String
        get() = sp.getString("receiptFooter", "شكراً لتسديدكم") ?: "شكراً لتسديدكم"
        set(v) = sp.edit().putString("receiptFooter", v).apply()

    var darkMode: Boolean
        get() = sp.getBoolean("darkMode", false)
        set(v) = sp.edit().putBoolean("darkMode", v).apply()

    val firstRunAt: Long
        get() {
            val v = sp.getLong("firstRunAt", 0L)
            if (v != 0L) return v
            val now = System.currentTimeMillis()
            sp.edit().putLong("firstRunAt", now).apply()
            return now
        }
}
