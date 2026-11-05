package id.okejack.okedriver.feature.wallet.domain.model

import java.util.Calendar
import java.util.TimeZone

/** IDs and operating hours from the active DepositNewActivity in legacy. */
object TopUpCatalog {
    val methods = listOf(
        PaymentMethod(16, "QRIS"),
        PaymentMethod(7, "Bank BCA"), PaymentMethod(5, "Bank Mandiri"),
        PaymentMethod(6, "Bank BNI"), PaymentMethod(9, "Bank BRI"),
        PaymentMethod(22, "Virtual Account BCA"), PaymentMethod(23, "Virtual Account Mandiri"),
        PaymentMethod(24, "Virtual Account BNI"), PaymentMethod(25, "Virtual Account BRI")
    )
    fun isBankTransfer(id: Int): Boolean = id in listOf(7, 5, 6, 9)
    fun isAvailable(id: Int, nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (methods.none { it.id == id }) return false
        if (!isBankTransfer(id)) return true
        val jakarta = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta")).apply { timeInMillis = nowMillis }
        return jakarta.get(Calendar.HOUR_OF_DAY) in 5..20
    }
    fun description(id: Int): String = when {
        isBankTransfer(id) -> "Transfer bank tersedia pukul 05.00–21.00 WIB. Proses sekitar 15 menit."
        id == 16 -> "Scan dengan dompet digital atau mobile banking."
        else -> "Tersedia 24 jam. Dikenakan biaya layanan."
    }
}
