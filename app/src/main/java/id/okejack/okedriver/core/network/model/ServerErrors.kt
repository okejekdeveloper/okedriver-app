package id.okejack.okedriver.core.network.model

/** Server codes remain machine-readable; only readable messages go to the UI. */
object ServerErrors {
    private val messages = mapOf(
        "input_not_valid" to "Data yang dimasukkan belum valid. Periksa kembali.",
        "bad_auth" to "Email atau password tidak sesuai.",
        "bad_auth_google_not_verified" to "Akun Google belum terverifikasi.",
        "bad_auth_google_not_registered" to "Akun Google belum terdaftar sebagai driver.",
        "user_not_activated" to "Akun driver belum diaktifkan. Hubungi admin.",
        "user_suspended" to "Akun driver sedang dinonaktifkan. Hubungi admin.",
        "session_not_valid" to "Sesi berakhir. Silakan masuk kembali.",
        "user_not_online" to "Aktifkan status Online terlebih dahulu.",
        "user_offline" to "Akun sedang Offline.",
        "order_not_available" to "Order sudah tidak tersedia.",
        "order_cancelled" to "Order sudah dibatalkan.",
        "user_has_unfinished_order" to "Selesaikan order aktif terlebih dahulu.",
        "unable_to_online" to "Belum dapat Online. Periksa status akun dan coba lagi.",
        "unable_to_offline" to "Belum dapat Offline. Selesaikan order aktif terlebih dahulu.",
        "driver_balance_not_sufficient" to "Saldo OkeDriver tidak mencukupi. Silakan top up.",
        "insufficient_balance" to "Saldo OkeDriver tidak mencukupi. Silakan top up.",
        "email_already_used" to "Email sudah digunakan akun lain.",
        "order_action_departure_delayed" to "Belum dapat memulai perjalanan. Tunggu sebentar dan coba lagi.",
        "order_action_arrival_delayed" to "Belum dapat menyelesaikan perjalanan. Tunggu sebentar dan coba lagi.",
        "order_progress_delayed" to "Perubahan status belum dapat diproses. Coba lagi sebentar.",
        "unable_cancel_order" to "Order ini sudah tidak dapat dibatalkan.",
        "payment_failed" to "Pembayaran belum dikonfirmasi. Periksa pembayaran customer.",
        "max_shopping_edit" to "Item belanja sudah pernah diubah dan tidak dapat diedit kembali.",
        "unable_to_edit_shopping_items" to "Item belanja tidak dapat diubah pada status order ini.",
        "pending_withdraw_request" to "Masih ada permintaan penarikan yang sedang diproses.",
        "invalid_withdraw_amount" to "Nominal penarikan tidak valid.",
        "existing_unconfirmed_topup_confirmation" to "Masih ada konfirmasi top up yang belum diproses.",
        "bonus_box_not_found" to "Bonus tidak ditemukan.",
        "bonus_box_redeemed" to "Bonus ini sudah dibuka.",
        "no_access" to "Anda tidak memiliki akses ke fitur ini.",
        "request_already_assigned" to "Order sudah diterima driver lain.",
        "outside_service_area" to "Lokasi berada di luar area layanan.",
        "order_location_too_far" to "Lokasi order terlalu jauh.",
        "attendance_already_recorded" to "Anda sudah melakukan absensi hari ini.",
        "location_not_allowed" to "Lokasi Anda tidak berada di area absensi.",
        "location_not_available" to "Lokasi GPS belum tersedia. Aktifkan GPS dan coba lagi.",
        "outside_working_hours" to "Absensi hanya dapat dilakukan pada jam kerja yang ditentukan.",
        "unable_to_record_attendance" to "Absensi belum berhasil dicatat. Coba lagi."
    )

    fun message(code: String, serverMessage: String = "", fallback: String = "Permintaan belum berhasil. Silakan coba lagi."): String {
        val key = code.lowercase()
        if (key == "order_progress_delayed" && readable(serverMessage)) return serverMessage
        return messages[key] ?: serverMessage.takeIf(::readable) ?: code.takeIf(::readable) ?: fallback
    }

    private fun readable(value: String): Boolean = value.isNotBlank() && !value.matches(Regex("[A-Za-z0-9_]+"))
}
