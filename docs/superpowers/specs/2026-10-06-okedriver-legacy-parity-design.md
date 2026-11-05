# Spec: Menyamakan okedriver-app dengan okedriver (legacy) sebelum production

Tanggal: 6 Okt 2026 · Status: **menunggu review** · Sumber temuan: [PARITY_AUDIT.md](../../../PARITY_AUDIT.md)

## 1. Tujuan

`okedriver-app` (Compose) akan menimpa `okedriver` (Activity/XML) yang dipakai driver sekarang (applicationId sama,
`versionCode 4855 > 4854`). Spec ini menetapkan **cara** menyamakan perilaku, aturan bisnis, integrasi API, dan isi
tampilan modern dengan legacy. Styling visual (warna, spasi, bentuk, animasi) tidak termasuk.

**Kriteria selesai** — untuk setiap temuan 🔴/🟠 di audit:
1. Aturan/perilaku legacy terpenuhi (atau tercatat sebagai deviasi sengaja di §4).
2. Ada tes unit untuk aturan yang bisa diekstrak jadi fungsi murni, atau item di `TESTING_PLAN.md` untuk yang hanya bisa diuji di device.
3. `./gradlew testDebugUnitTest lintDebug assembleDebug` hijau.
4. Upgrade 4.11.2 → 4.11.3 di device nyata tidak memaksa login ulang dan tidak mengubah pengaturan notifikasi driver (paket H).

## 2. Keputusan yang sudah final

| # | Topik | Keputusan |
|---|-------|-----------|
| 1 | Cancel setelah order diterima | Ikuti legacy: tombol cancel nonaktif saat `ASSIGNED`; status selain PENDING/ASSIGNED → dialog "Anda tidak bisa membatalkan job ini sekarang!"; hanya PENDING membuka dialog alasan |
| 2 | Teks | Teks legacy persis, **EN + ID** di `strings.xml` |
| 3 | Login Google, fragment update software | **Fase terpisah** (di luar spec ini) |
| 4 | `minSdk` | Tetap 23 (ikuti modern) |
| 5 | Restart service saat boot | Mulai bila token ada ∧ driver terakhir online ∧ izin lokasi foreground ∧ GPS aktif. **Tanpa** syarat izin background (tidak pernah diminta oleh kedua app) |
| 6 | Top Up | Ganti layar Top Up modern dengan katalog legacy (`DepositNewActivity`); endpoint `payment-methods` tidak dipanggil lagi |
| 7 | Rentang Driver Bid | Array tetap legacy; rentang dari server diabaikan |
| 8 | Terminal antrian | `Bandara Soetta`, `Pasar Senen` (default legacy; bila backend ternyata memakai nilai lain, hanya konstanta `QueueLocations` yang berubah) |

## 3. Prinsip desain

- **Legacy = acuan.** Penyimpangan hanya untuk bug legacy yang jelas (§4) dan harus disetujui di review ini.
- **Aturan sebagai fungsi murni.** Setiap aturan bisnis diekstrak ke `object ...Policy` tanpa dependensi Android
  (pola `QrisPaymentPolicy` yang sudah ada), supaya bisa dites tanpa device. Compose/ViewModel hanya memanggilnya.
- **Perubahan sekecil mungkin di arsitektur modern.** Clean architecture per fitur, Hilt, StateFlow dipertahankan. Tidak ada refactor di luar kebutuhan paket.
- **Teks selalu dari resource.** Tidak ada string UI baru yang di-hard-code.
- **Tidak menyentuh backend production.** Semua endpoint yang dipakai sudah dipakai legacy dengan payload yang sama.

## 4. Deviasi sengaja dari legacy (bug legacy yang **tidak** disalin)

| # | Perilaku legacy | Perilaku baru | Alasan |
|---|-----------------|---------------|--------|
| D1 | Memuat `order/pending-order` gagal dengan `order_not_available` → toast **lalu logout paksa** (`EXTRA_LOGOUT`) | Toast lalu ke Home, sesi tetap | Driver ter-logout hanya karena order sudah diambil orang lain |
| D2 | Alarm restart service tiap 30 menit **tidak pernah dibatalkan** (tidak di `ACTION_OFFLINE`, tidak di `onDestroy`) → kodenya bisa menghidupkan lagi service saat driver offline; efek nyata bergantung versi Android (Android 12+ sering menolak start FGS dari background) | Alarm dibatalkan saat stop/offline; saat alarm menyala service hanya melacak bila driver masih online | Pelacakan lokasi saat offline (privasi, baterai); perbaikannya tidak berisiko |
| D3 | Dialog peringatan sebelum online hanya informasi: toggle tetap jalan walau pilih "Tidak jadi" | Dialog menahan toggle: "Saya mengerti" melanjutkan, "Tidak jadi" membatalkan | Maksud jelas konfirmasi |
| D4 | Pesan memuat kode debug ("Error 104:", "Error (cancelledN1):", "(error: 100)") | Hanya teks pengguna dari `strings.xml` | Noise debug |
| D5 | Tolak order: pindah ke Home **sebelum** hasil `order/cancel` diketahui | Tunggu hasil; gagal → tetap di layar + opsi coba lagi | Hindari order tetap pending diam-diam |
| D6 | Kode legacy meminta `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` tetapi izinnya tidak dideklarasikan di manifest → dialog itu kemungkinan besar **tidak pernah tampil** bagi driver (perlu diuji di device; saya ~85% yakin) | **Tidak menambah** prompt baterai. Hanya autostart OEM (sekali) seperti legacy. Bila uji menunjukkan dialog legacy ternyata tampil, port persis dengan izin yang sama | Mengikuti perilaku yang benar-benar dialami driver; menghindari risiko kebijakan Play |
| D7 | Dialog konfirmasi Finish ada pada slider kedua yang tersembunyi (UI lama) | Tidak dibuat; satu slider seperti UI yang tampil | UI lama sudah tidak dipakai |
| D8 | Tombol **Batal** di Detail Deposit membuka dialog "Batalkan Transaksi" tetapi `cancelDeposit()` kosong (tidak ada yang dibatalkan) | Tombol tidak ditampilkan | Tombol yang tidak berfungsi menyesatkan driver; tidak ada endpoint pembatalan di legacy |
| D9 *(bersyarat)* | Blok skor disembunyikan hanya bila `score == null`; bila server mengirim objek kosong atau menghilangkan field, order yang belum dinilai tampil dengan **jempol turun** (`score != 2`). Belum bisa dipastikan bentuk respons server | Ikuti legacy persis (sembunyikan hanya bila `null`). Verifikasi bentuk respons order belum dinilai di staging; **bila** berupa objek kosong, tambah syarat `score > 0 \|\| review tidak kosong` | Tidak menyembunyikan penilaian buruk yang sah, tetapi menghindari jempol turun palsu bila terbukti |
| D10 | Waktu kedaluwarsa dibaca memakai zona waktu perangkat; kode unik/biaya admin berbeda aturan saat data awal vs setelah refresh; logo Mandiri di layar VA tidak pernah cocok (`"Mandiri" in "MANDIRI"`) | Zona `Asia/Jakarta` (sudah dipakai `TopUpExpiry`), aturan kode unik/biaya admin satu saja (§F1), logo dicocokkan tanpa membedakan huruf | Server memakai WIB; kesalahan perbandingan huruf |
| D11 | Daftar riwayat order memanggil `getOrders()` setiap `onResume` tanpa mengosongkan daftar: tiap kali **halaman berikutnya** ditambahkan (bukan duplikat) sehingga daftar tidak pernah disegarkan — order yang baru selesai tidak muncul sampai tarik-untuk-refresh | Muat ulang bersih dari halaman 1 saat layar kembali tampil | Daftar basi dan scroll melompat |

## 5. Rancangan per paket

Urutan dan dependensi ada di §6. Nomor `§x.y` merujuk ke temuan di PARITY_AUDIT.md.

### Paket A — Fondasi: error terstruktur & teks (audit §1)

**A1. Error terstruktur.** Perluas `ApiResponse.Error` dengan parameter berdefault (kompatibel dengan semua pemanggil sekarang):

```kotlin
data class Error(
    val code: Int,                         // HTTP/internal (-1 jaringan, -2 tak dikenal)
    val message: String,                   // sudah teks pengguna (lokal)
    val errorCode: String? = null,         // kode server mentah: "user_suspended", "ATTENDANCE_ALREADY_RECORDED", ...
    val meta: Map<String, String> = emptyMap() // mis. suspension_reason
)
```

- `ApiErrorCode`: konstanta dari `ErrorMessages.kt` legacy (50 kode) + kode absensi huruf besar.
- `ApiErrors` (di `core/network/model`): `fromServer(error, message, meta, fallback)`; kode dikenal → string legacy (tabel §1.1 audit),
  tak dikenal → `fallback` per pemanggil (mayoritas `error_unknown`; beberapa layar legacy menampilkan `message` server, mis. top-up).
  Resolver teks diinisialisasi di `OkeDriverApplication` dengan `Context`; tes mengganti resolver dengan fake.
- `safeApiCall`: `IOException` dan `HttpException` non-2xx → `error_connection` ("Tidak dapat terhubung"), seperti legacy
  (semua `onError` Rx → toast koneksi); exception lain → `error_unknown`. Kode HTTP tetap di `code` (401 tetap diurus `AuthInterceptor`).
- Semua `*Dto.errorMessage` (±16 DTO) diganti `toApiError()` yang memanggil `ApiErrors.fromServer`. ViewModel tetap menampilkan `result.message`.
- Aksi khusus per layar (logout, buka QRIS, tutup layar, Snackbar *indefinite*, dst.) memakai `result.errorCode` di ViewModel — **bukan** membandingkan teks.
  Tabel aksi per kode = §1.1 audit; ditaruh di paket pemilik layar (B/D/E/F/G).

**A2. Teks EN + ID.**
- Salin dari legacy: `values/strings.xml` (EN), `values-in/strings.xml` (ID), `chat_texts.xml`, `banks.xml` ke modern; nama kunci dipertahankan.
  Sesuaikan bila bentrok dengan kunci modern yang sudah ada.
- Ganti semua teks hard-code di Composable/ViewModel dalam cakupan dengan `stringResource`/resolver. Teks modern tanpa padanan legacy
  (mis. layar Order Payments, label VA) dipindah ke `strings.xml` dengan terjemahan EN+ID baru.
- `lint { error += "MissingTranslation" }` agar EN dan ID selalu sepadan.
- Pemangkasan resource legacy yang tak terpakai: lint `UnusedResources` di paket H.

**Tes A:** tes tabel `ApiErrorsTest` — setiap kode di `ApiErrorCode` menghasilkan teks non-kosong untuk EN dan ID, kode tak dikenal memakai fallback,
`meta.suspension_reason` terbawa; `SafeApiCallTest` (IOException/HttpException/exception lain).

### Paket B — Order masuk & notifikasi (audit §3, §4)

**B1. Channel.** `NotificationChannelUtils` memakai **ID dan atribut legacy persis**, supaya upgrade in-place mewarisi pengaturan driver:

| Fungsi | ID | Importance | Atribut |
|--------|----|-----------|---------|
| Order masuk | `order_reminder_channel_id` | HIGH | suara `onesignal_default_sound`, `USAGE_NOTIFICATION_RINGTONE`, getar, lampu, publik |
| Chat | `order_chat_channel_id` | HIGH | sama |
| Berita | `news_channel_id` | HIGH | sama |
| Bonus | `bonus_channel_id` | HIGH | sama |
| Pesan umum | `general_message` | DEFAULT | grup `messaging`, getar, lampu |
| Service online | `driver_online_channel_1` | LOW | — |

Channel dibuat idempoten saat `Application`/`FCMReceiverHandler`/service start. ID modern lama (`okedriver_*`) dihapus bila ada (hanya relevan di HP penguji).

**B2. Notifikasi order.** Dibangun seperti `FCMNotificationHandlerService.showOrderNotification`: judul `current_order_pending_warning`
("Order Pending"), isi = **alamat jemput** (di-parse dari `data["order"]` bila ada; jatuh ke `message_body`), `CATEGORY_CALL`, prioritas HIGH,
`setFullScreenIntent` ke `MainActivity` (extra kind/id yang sudah ada), aksi "Terima job" memakai intent yang sama, `VISIBILITY_PUBLIC`,
`FLAG_INSISTENT`. Notifikasi chat/news/bonus/umum memakai channel di tabel; pesan umum memakai suara `notification` dan `FLAG_INSISTENT` seperti legacy.
Android 14+: bila `canUseFullScreenIntent()` false, notifikasi tetap heads-up (sama dengan legacy); tidak menambah alur izin baru.

**B3. Layar di atas lock-screen.** `IncomingOrderScreen` memasang `LockScreenEffect` (`DisposableEffect` pada `Activity`):
`setShowWhenLocked(true)`, `setTurnScreenOn(true)`, `requestDismissKeyguard` (port `showActivityOnLock`), `FLAG_KEEP_SCREEN_ON`;
semuanya dikembalikan saat layar ditinggalkan. **Tidak** diatur permanen di manifest agar seluruh app tidak tampil di lock-screen.

**B4. Layar Order Masuk.**
- Hapus tombol "X". Back (`BackHandler`) dan tombol Tolak memunculkan dialog `confirm_pending_order` ("Anda yakin ingin menolak job ini?" Ya/Tidak).
- Ya → `order/cancel` `reason_code=98` → sukses: hapus notifikasi, ke Home; gagal: Snackbar *indefinite* `error_unable_to_cancel` + "Coba lagi" (D5).
  Gagal jaringan: tombol Terima/Tolak nonaktif dan Back boleh keluar (`canExit`), seperti legacy.
- Terima: `order/update-status`; sukses → hapus notifikasi → Order Aktif; `order_cancelled` → toast + Home; `order_not_available` (muat/cancel) → toast `error_order_not_available_or_cancelled` + Home (D1); lainnya `error_unknown`.
- Order berbeda dari yang diharapkan / tidak lagi PENDING → alert `error_missed_or_cancelled_job` ("Job ini sudah tidak tersedia untuk anda") → Home (D4).
- Tampilan: nominal = `fee` (bukan `totalPaid`), label promo bila `fee != originalFee`, baris total belanja untuk SHOPPING/FOOD (= Σ `qty×price` item), ikon per tipe, metode bayar.

**Tes B:** `NotificationChannelsTest` (ID/importance/atribut sesuai tabel); `OrderNotificationFactoryTest` (judul, isi alamat jemput, flag insisten, kategori, full-screen intent);
`IncomingOrderViewModelTest` (reject butuh konfirmasi, error→aksi, canExit, mismatch id); `IncomingOrderDisplayPolicyTest` (fee/promo/belanja). Manual: HP terkunci, foreground/background.

### Paket C — Service lokasi (audit §5)

Perubahan pada `OnlineDriverService` (interval 30/10/30 dtk dan payload tidak berubah):

- **WakeLock** `PARTIAL_WAKE_LOCK` ("okedriver::OnlineDriverService", timeout 2 jam) saat start tracking; dilepas saat stop.
- **Restart berkala**: `AlarmManager.setRepeating(ELAPSED_REALTIME_WAKEUP, +30 mnt, 30 mnt)` ke `ACTION_START` (request code 9914). Dibatalkan saat stop/offline (D2); `ForegroundServiceStartNotAllowedException`/`IllegalStateException` saat alarm menyala ditangkap dan dicatat.
- **`onTaskRemoved`**: bila sedang tracking, jadwalkan restart sekali 1 dtk kemudian (`FLAG_ONE_SHOT`, request code 9913).
- **`DriverLocationStore`** (`@Singleton`, `StateFlow<DriverLatLng?>`): diisi tiap fix lokasi dan disimpan di DataStore (`my_location`) sebagai nilai awal — mengganti `LocationUpdatedEvent` + prefs `MY_LOCATION` legacy. Dipakai peta Order Aktif (paket D).
- **`StartupHelper`** (port): `requestAutoStartIfNeeded` (komponen OEM Xiaomi/Oppo/Vivo/Asus/Huawei, sekali, flag `autostart_checked`); prompt baterai **tidak** ditambah (D6). Dipanggil sekali dari `InitScreen`/`InitViewModel` sebelum navigasi.
- **Boot**: `BootDecision.shouldStart(tokenPresent, driverOnline, fineOrCoarseGranted, gpsEnabled)` murni (keputusan #5). `BootCompletedReceiver` memakainya.
- Teks notifikasi service dari `online_driver_service_notification_title/content`; channel `driver_online_channel_1`.

**Tes C:** `BootDecisionTest` (tabel kombinasi); `ServiceRestartPolicyTest` (alarm dijadwalkan/dibatalkan sesuai state, dengan `AlarmScheduler` fake); `DriverLocationStoreTest`. Manual (TESTING_PLAN P0): Xiaomi/Oppo/Vivo — swipe dari recent, reboot, layar mati 30 menit.

### Paket D — Order Aktif & detail order (audit §2, §9.6–9.7)

**D-a. Komponen baru**
- `SwipeToConfirmButton` (Compose, `AnchoredDraggable`): teks "Mulai"/"FINISH" (`action_start`/`action_finish`), memicu `onConfirmed` saat digeser penuh, kembali ke awal setelah 3 dtk (D7).
- `CurrentOrderMap`: peta dengan marker driver (`icon_driver_motor` bila `type==RIDE`, selain itu `icon_vip_car`), marker tujuan (jemput bila PENDING/ASSIGNED, selain itu tujuan), **polyline rute** dari lokasi driver (`DriverLocationStore`), kamera zoom 14 ke tujuan, klik alamat memusatkan kamera.
- `DirectionsApiService` (Retrofit, `https://maps.googleapis.com/maps/api/directions/json`, `origin`, `destination`, `key`) + `DirectionParser.decode` (port polyline); kunci dari `BuildConfig.MAPS_API_KEY` yang diambil dari `local.properties` yang sama dengan `manifestPlaceholders`. Gagal diam-diam (tanpa rute), seperti legacy.
- Aset drawable legacy disalin ke modern: `icon_driver_motor`, `icon_vip_car`, `ic_service_*` (ride, delivery, shopping, food, car, rental_car, mart, trike, trike_courier).

**D-b. Aturan sebagai policy murni** (`OrderDisplayPolicy`, `CancelPolicy`) + tes:
- `CancelPolicy`: tombol aktif ⇔ status ≠ `ASSIGNED`; klik: status ∈ {PENDING, ASSIGNED} → dialog alasan, selain itu → alert `error_no_cancel`. Dialog: judul `label_cancel_reason_dialog`, 4 alasan legacy (`cancel_reason_*`), teks penalti `label_cancel_penalty_dialog`, tombol `action_never_mind`/`action_cancel_job`. Sukses → Home.
- `OrderDisplayPolicy`: baris belanja disembunyikan untuk RIDE; tombol edit belanja berlabel `action_edit_shopping_items` (SHOPPING/FOOD) atau `action_add_fee` (CAR), disembunyikan selain itu; kartu kurir untuk DELIVERY/EXPRESS dengan tombol telepon pengirim/penerima; **kode verifikasi + ongkos** untuk EXPRESS; kartu vendor (nama, alamat, telepon bila status ≠ ARRIVED); kartu addon untuk MASSAGE/RENTAL_CAR (durasi; lembur & biaya lembur saat ARRIVED; alamat tujuan disembunyikan); kartu "Bayar ke merchant" untuk FOOD/MART; label diskon bila `totalDiscount > 0`; biaya layanan bila `> 0`; "Menuju ke {nama}"; nomor telepon hanya bila `filterPhone(nomor)` tidak kosong.
- Label tipe layanan memakai `service_*` legacy; konstanta status `EXPRESS_FAILED=162/HOLD=161/RETURN=163` ditambahkan.

**D-c. Model.** Perluas `OrderDto`/`DriverOrder`: `driverId`, `score{score,review}`, `paidCash`, `paidBalance`, `foodVendor{id,name,address,phone}`, `foodVendorId`, `duration`, `overtime`, `overtimeFee`, `express.verificationCode`, `data.androidAppVersion` (untuk Chat), `courier.senderPhone/recipientPhone` (sudah ada di DTO).

**D-d. Alur & error.**
- Finish via slider → `finishOrShowQrisPayment` (logika QRIS yang sudah ter-port tetap). Sukses → toast `job_finished_thank` → Detail Order (dengan Home di back stack).
- Kode error: `order_action_departure_delayed`/`arrival_delayed` → Snackbar teks lokal + aksi "Sip" (merah); `order_progress_delayed` → Snackbar pesan server + "Sip"; `payment_failed` → buka QRIS **tanpa syarat provider**; sesi invalid → logout (sudah terpusat).
- `order.id <= 0` dari server → toast `error_order_cancelled`, hentikan polling, tutup ke Home.
- Gagal jaringan: Snackbar *indefinite* `error_connection` + "Coba lagi".
- `FLAG_KEEP_SCREEN_ON` selama layar tampil (efek yang sama dengan B3 tanpa show-when-locked).
- Auto-refresh 15 dtk (sudah sama).

**Tes D:** `CancelPolicyTest`, `OrderDisplayPolicyTest` (tabel tipe×status), `OrderDtoMappingTest` (field baru), `CurrentOrderViewModelTest` (kode error→event, order id ≤ 0, cancel), `DirectionParserTest`, `SwipeToConfirmStateTest` (logika ambang/reset tanpa UI).

### Paket E — Home (audit §6, §11 antrian)

- **Auto-buka order aktif.** Saat Home RESUMED dan `order/current-order` mengembalikan `id > 0` → navigasi ke Order Aktif menghapus Home dari back stack (Back keluar app, seperti `finish()` legacy). Setelah order selesai: Home dipulihkan lalu Detail Order, sehingga Back dari Detail ke Home.
- **Dialog online (D3).** Offline→online: dialog `location_tracking_warning_title/location_tracking_warning` (Saya mengerti / Tidak jadi); setelah sukses online: dialog `before_online_warning_*` + `app_should_open_warning_*`; sukses toggle → Snackbar `status_changed`; gagal → `error_unable_change_driver_status`; `user_has_unfinished_order` → Snackbar *indefinite* + OK.
- **OkeFast per kota.** `OkeFastPolicy.isAvailable(cityId) = cityId ∈ {444, 436, 469, 470}` (konstanta bisnis legacy; dicatat di sini agar mudah diubah). Entri OkeFast hanya tampil untuk kota itu, transparan 40% bila belum absen; ketuk saat belum absen → dialog "Belum Absen" ("Absen Sekarang" → Absensi / "Nanti"). Cek `check-absence-status` untuk kota tsb **atau** `enableDriverQueue`.
- **Antrian.** `QueueLocations.ALL = [Bandara Soetta, Pasar Senen]` (menggantikan `Terminal 1/2/3`); dialog pilih lokasi `choose_queue_location_title`; `QueueButtonPolicy`: tombol aktif ⇔ queue aktif ∧ sudah absen ∧ belum punya nomor; label `take_queue_button_label`/`queue_already_registered_button_label`; nomor `0` tampil "?" dengan rasio `0/total`; error `driver_balance_not_sufficient` & `outside_service_area` → dialog judul+hint legacy; lainnya dialog "Gagal" + pesan.
- **MOTD.** `MotdPolicy.shouldShow(now, lastClosed)`: pesan tidak kosong ∧ `now − lastClosed > 6 jam` (21.600 dtk); ketuk → dialog pesan dengan "Jangan tampilkan lagi" (simpan `last_read_motd` di DataStore) dan OK.
- **Heatmap.** `HeatmapTileProvider` (android-maps-utils, radius 35) dari `orders/recent-orders`; dilewati bila kosong; zoom maksimum 13 seperti legacy; marker driver mengikuti `driverType`.
- **Drawer.** Tambah Tutorial & Bantuan (`https://okejek.id/tutorial-oke-driver/`), label versi `"{app_name} v{versionName} ({versionCode})"`, tombol Edit Profil di header, nama menu dari string legacy. Entri modern tambahan (Order Terbaru) dipertahankan.

**Tes E:** `OkeFastPolicyTest`, `MotdPolicyTest`, `QueueButtonPolicyTest`, `HomeViewModelTest` (redirect order aktif, toggle + konfirmasi, error→aksi), `QueueLocationsTest`.

### Paket F — Dompet & Top Up (audit §8)

- **Katalog Top Up** (`TopUpCatalog`, domain murni): QRIS id 16; Transfer Bank BCA 7 / Mandiri 5 / BNI 6 / BRI 9 — aktif hanya jam **05.00–20.59 WIB** (`BankTransferWindow.isOpen(clock)`, zona `Asia/Jakarta`, jam ∈ 5..20), di luar jam tombol nonaktif + toast `Metode ini tidak tersedia saat ini.`; Virtual Account BCA 22 / Mandiri 23 / BNI 24 / BRI 25 dengan catatan "Dikenakan biaya layanan". Header grup & deskripsi dari string legacy. Default = bank pertama yang aktif, jika tidak ada metode aktif pertama, jika tidak ada sama sekali "Tidak ada metode tersedia".
- **Layar pilih nominal & metode** (`TopUpScreen` diganti): 6 chip nominal, input `Rp` dengan pemisah ribuan, validasi "Nominal tidak boleh kosong.", tombol "Bayar dengan {bank}", bottom sheet daftar metode. Request `topup-request` `{paymentMethod, amount}` (payload sudah sama). Sukses → QRIS (16) ke layar QRIS, selain itu ke detail; gagal → toast `Top-up gagal: {message}`.
- **Alur layar hasil** (dibaca baris-per-baris dari `DepositDetailActivity`, `VaResultBankTransfer`, `VaBankActivity`, `QrisPaymentActivity`). Setelah `topup-request` ada dua tahap: **Detail Deposit** lalu, khusus non-QRIS, **layar pembayaran** setelah "Lanjut Bayar".

  **F1. Detail Deposit** (non-QRIS; judul "Detail Deposit"; tarik-untuk-refresh memanggil `deposit-detail`; gagal → toast `Gagal memuat detail deposit` / `Terjadi kesalahan koneksi`):
  - Baris: `DEP-{trans_id}`, tanggal, jumlah (`harga`), kode unik, biaya admin, total, produk, detail produk, metode (`payment`), status (diwarnai `status_color`), catatan, batas waktu (`expired_date`), logo bank.
  - Kode unik **disembunyikan** bila `payment_method` > 10 (VA); biaya admin disembunyikan bila ≤ 10 (transfer manual) — aturan yang sama dipakai saat data awal dan setelah refresh (legacy tidak konsisten setelah refresh; lihat D10).
  - Status ∈ {`Berhasil`, `Canceled`, `Expired`, `Kadaluarsa`} → sembunyikan kartu pembayaran, konfirmasi, Batal, dan Lanjut Bayar.
  - Logo: metode 14/15 → `paychan_alfagroup`, 16 → `paychan_atm`, selain itu cocokkan nama (`VA Mandiri`, `Mandiri`, `BCA`, `BNI`, `BRI`; exact dulu lalu *contains*, tanpa membedakan huruf).
  - Tombol: **Bantuan** (WhatsApp 62817381500, teks `Halo Kak, saya butuh bantuan mengenai top-up saya.`); **Konfirmasi via WA** (teks `Halo Admin Oke Jack, saya ingin konfirmasi bukti pembayaran top up saldo.\n\nNama: {nama}\nNopol: {plat}\nNo. Order: DEP-{trans_id}\nNominal: {total Rp}\n\nBerikut saya lampirkan bukti transfernya.`); **Lanjut Bayar** → `payment-gateway/driver/topup` `{deposit_id}`; **Batal** (lihat D8). WhatsApp tak terpasang → toast `WhatsApp tidak terpasang di perangkat Anda`.
  - `createTopup`: respons sukses dengan `vadata.order_id` dan `vadata.va_number` terisi → buka layar pembayaran (`payment_method` < 10 → **Transfer Manual**, selain itu → **VA**); bila kosong ulangi hingga **3×** tiap 2 dtk, lalu toast `Gagal memproses top-up. Silakan coba lagi.`; `success=false` → toast `{message}` atau `Gagal membuat top-up`; error jaringan → `error_connection`, hitung ulang direset.

  **F2. Transfer Manual** (`VaResultBankTransfer`; judul `Pembayaran #{order_id}`):
  - Menampilkan nomor transaksi (`order_id`; ketuk = salin "Nomor Transaksi"), total (`amount` → Rp), nomor rekening (`va_number`), nama rekening (`account_name`), tenggat `dd MMM yyyy HH:mm WIB` dari `expired_at` (format `yyyy-MM-dd HH:mm:ss`), hitung mundur `HH : MM : SS`; `va_number`/`account_name` di-render sebagai HTML (`Html.fromHtml`).
  - `status` ∈ {`success`, `berhasil`, `16`} → tampilkan baris kode bank (`bank_code`, `bank_code_permata`) dan label langkah terakhir `3. Pembayaran Berhasil`; selain itu sembunyikan baris kode bank, label `4. Pembayaran Berhasil`, dan logo bank dari nama bank (BCA/BNI/BRI/Mandiri).
  - Salin: Jumlah (`amount` mentah), Nomor Rekening → toast `{label} berhasil disalin`.
  - **Konfirmasi**: dialog `Konfirmasi Transfer` — `Pastikan anda sudah melakukan transfer sebelum melanjutkan, apakah anda yakin?` (OK/Tidak); OK → buka WhatsApp `Halo Kak, saya sudah melakukan transfer topup driver.id {order_id} , Jumlah Rp{amount}` dan sembunyikan panel konfirmasi. **Bantuan**: `Halo Kak, saya butuh bantuan mengenai top-up saya.id {order_id}`.
  - Habis waktu → toast `Waktu transfer anda sudah habis` lalu tutup layar.
  - Teks statis dari string legacy: `transaction_id`, `payment_deadline`, `please_make_payment_before`, `remaining_time_to_pay`, `hour/minute/second`, `please_transfer_to_step_2`, `account_no`, `account_owner_name`, `okejek`, `total_amount_payment`, `how_to_make_payment`, `payment_confirmation_step_3`, `second_step`, `after_make_the_payment_please_confirm_by_clicking_button_below`, `payment_success`, `if_you_done_payment_transaction_will_process_by_system`.

  **F3. Virtual Account** (`VaBankActivity`; judul `Pembayaran #{order_id}`): sama dengan F2 untuk data, logo, tenggat, hitung mundur, dan label langkah, dengan perbedaan: nomor transaksi tampil `00000000{order_id}` (awalan tetap seperti legacy), nama pemilik rekening memakai `app_name`, **tanpa** tombol Konfirmasi/nama rekening dari server, salin berlabel `{label} berhasil di copy`, judul bagian "Pembayaran ke Virtual Account" dan "Batas Waktu Pembayaran". Bantuan: `Halo Kak, saya butuh bantuan mengenai top-up saya.`. Back mengembalikan `RESULT_CANCELED` (kembali ke Detail Deposit).

  **F4. QRIS Top Up** (`QrisPaymentActivity`; sudah ter-port sebagian): tambahkan toast `Top up berhasil, saldo kamu sudah bertambah.` (sukses) dan tutup otomatis 2,5 dtk, toast `Waktu pembayaran QRIS sudah habis` (kedaluwarsa, tanpa menutup layar), label `Pembayaran Berhasil` / `Waktu Pembayaran Habis`, unduh QR ke galeri (toast `QRIS berhasil disimpan ke galeri` / `Gagal menyimpan QRIS` / `QR code tidak tersedia` / `Izin penyimpanan diperlukan untuk mengunduh QRIS`), WhatsApp bantuan `Halo Kak, saya butuh bantuan mengenai top-up QRIS saya (order DEP-{trans_id}).`, data kosong → toast `Data pembayaran tidak ditemukan` + tutup. Status `Berhasil` → sukses; `Canceled`/`Expired`/`Kadaluarsa` → kedaluwarsa. Polling 5 dtk (sudah ada).
- **Riwayat top-up**: rentang tanggal (default hari ini–hari ini, `DatePicker` M3, tampil `dd MMM yyyy` Locale.US, dikirim `yyyy-MM-dd`), pencarian, paginasi `page`, empty state.
- **Withdraw / Bonus / Saldo**: error dipetakan lewat paket A (pesan legacy), tombol refresh; logika batas/pending yang sudah sama dipertahankan.
- `payment-methods` dihapus dari `WalletApiService` (keputusan #6). Aset: `ic_qris`, `logo_bca/mandiri/bni/bri`, `paychan_*` disalin.

**Tes F:** `TopUpCatalogTest` & `BankTransferWindowTest` (batas 04.59/05.00/20.59/21.00, zona waktu), `TopUpViewModelTest`, `CreateTopUpRetryTest`, `TopUpHistoryViewModelTest` (tanggal & paginasi), uji payload JSON (`paymentMethod`, `amount`, `deposit_id`, `date_from`, …).

### Paket G — Sisanya (audit §9, §10, §11)

| Area | Perubahan |
|------|-----------|
| Chat | 5 balasan cepat (`chat_texts`) lewat menu; judul `chat_title` + nama customer; Snackbar bila `data.androidAppVersion == "0"`; `no_access` → toast + keluar; ketuk pesan menampilkan waktu |
| Edit Belanja | Dialog `warning_edit_shopping_item` saat dibuka; opsi tambah per tipe order (SHOPPING/FOOD: Barang/Parkir/Tol/Dari merchant, CAR: Parkir/Tol, lainnya: tidak ada); **katalog merchant** via `vendors/{id}` (`food_vendor_id`); alert `error_max_shopping_edits` bila sudah diedit; Snackbar *indefinite* untuk `max_shopping_edit`/`unable_to_edit_shopping_items` |
| Riwayat | Daftar & detail — aturan lengkap di **G-Riwayat** di bawah |
| Profil | Salin & bagikan tautan `https://okejack.id/driver/apply/driver?ref={id}` (`referral_id_link_copied`, `format_referral_share_message`) |
| Edit Profil | Nama bank = dropdown 7 pilihan dari `banks.xml` (nilai yang dikirim = teks panjang) + placeholder `select_bank`; toast `profile_saved` |
| Driver Bid | Array tetap legacy (keputusan #7); abaikan rentang server |
| Login | Pesan `bad_auth`/`input_not_valid`/`user_not_activated`/`user_suspended` + `suspension_reason`; "Email dan password harus diisi!" |
| Init | Dialog Xiaomi "Notifikasi Tidak Berbunyi" bila channel order bisu → buka pengaturan channel `order_reminder_channel_id`; alur izin lokasi/dialog lokasi tidak aktif seperti `InitActivity` |
| Absensi | `maxUpdateAge` 5 dtk, jam & tanggal berjalan, sapaan `Selamat Datang, {nama}`, polling antrian 60 dtk, tutup 2 dtk setelah sukses, toast waktu |
| OkeFast | Kode 6 digit tampil `XXX XXX` |
| Order Terbaru | Heatmap radius 35, zoom 13 menggantikan marker |

**G-Riwayat — daftar** (`OrderListFragment` + `RVOrdersAdapter`):
- Dua tab berjudul **"In Progress"** (`in_progress=1`) dan **"Finished"** (`in_progress=0`) — judul tab hard-code Inggris di legacy dan dipertahankan.
- Paginasi `page` mulai 1, bertambah tiap halaman sukses, infinite scroll; tarik-untuk-refresh mengosongkan daftar dan kembali ke halaman 1; gagal → toast `error_connection`; kosong → `empty_order_message`.
- Item: label tipe (`OrderTypes.getOrderType`: Ride→`service_ride`, Delivery→`service_delivery`, Shopping→`service_shopping`, Food→`Oke Food`, OkeCar→`Oke Car` + `" - "` + `service_car`, Mart→`Oke Mart`, lainnya→`service_unknown`) berwarna per tipe (RIDE/OKECAR merah, DELIVERY oranye, SHOPPING hijau, FOOD biru tua, lainnya abu); jarak `"{distance}KM"` (tanpa spasi); alamat jemput; ongkos = `discountedFee`; bila `couponId > 0` baris `discount` ("Saldo Tunai") + `-{discount}`; ikon per tipe (OKECAR→`ic_service_car`; MART→ikon belanja; lainnya `serviceIcons`); ID `ID {tahun}/{bulan}/{tanggal}/#{id}` dari `createdAt`.
- **Hanya order `ARRIVED` yang bisa diketuk** → Detail Riwayat; status lain tidak bisa dibuka.
- `discountedFee`: `fee`, dikurangi diskon kupon bila `couponId>0` (persentase: `fee × discountPercentage / 100`; selain itu `discountFee`). Karena itu DTO daftar perlu `coupon_id` dan `coupon{use_discount_percentage, discount_fee, discount_percentage}`.
- Legacy memanggil `getOrders()` tiap `onResume` tanpa mengosongkan daftar (menambah halaman berikutnya, daftar basi) — modern memuat ulang bersih (D11).

**G-Riwayat — detail** (`ViewOrderActivity`; judul `title_activity_single_order`):
- Label **tidak tersedia** bila `driverId` ≠ id driver; label **selesai** bila status ARRIVED; label **dibatalkan** bila CANCELLED.
- `ID #{id}`, tipe + warna + ikon (courier/shopping/food/…); chip pembayaran: `isUseUserBalance` → "Oke Pay" (putih di atas warna utama), selain itu "Tunai" (abu); chip metode: provider `cash` → "Tunai" (hijau), selain itu `providerName` (warna utama).
- Ongkos: `totalDiscount > 0` → tampilkan `originalFee` dicoret + baris diskon; selain itu `fee`. Diskon tampil `-discount` memakai field **`discount`** (bukan `total_discount`); **total** = `total_shopping + (originalFee − discount)`; total belanja = Σ `qty×price` item. DTO perlu `discount`.
- Info driver atau `no_additional_information`. Grup "info tambahan" tampil bila `driverId` = driver ∧ tipe ∈ {SHOPPING, FOOD}. Panel "dimiliki" (rincian **dibayar saldo** `paid_balance` dan **dibayar tunai** `paid_cash`) tampil bila `driverId` = driver ∧ status ARRIVED.
- Kurir (tipe DELIVERY saja di riwayat): barang, nama pengirim, nama penerima (tanpa tombol telepon).
- Skor: ikon jempol naik bila `score.score == 2`, selain itu jempol turun; review tampil bila tidak kosong; blok skor disembunyikan hanya bila `score == null` (D9, bersyarat).
- Kartu "bayar ke merchant": nominal `original_total_shopping`, tombol bantuan → alert `info_original_shopping`; disembunyikan untuk RIDE. Vendor: nama + alamat; tombol telepon bila ada nomor ∧ status ≠ ARRIVED (membuka dialer).
- Addon (MASSAGE/RENTAL_CAR): sembunyikan alamat tujuan, tampilkan durasi (`duration_metric`); saat ARRIVED tampilkan lembur (`duration_metric`) dan biaya lembur (`cost`). Bukan addon: tampilkan alamat jemput/tujuan, biaya layanan bila > 0, daftar belanja bila SHOPPING/FOOD.

**Tes G:** policy/VM yang relevan (`ChatQuickReplyTest`, `EditShoppingOptionsPolicyTest`, `BankListTest`, `ReferralLinkTest`, `FormatCodeTest`), sisanya checklist manual.

### Paket H — Kesiapan production (audit §7)

- **Uji upgrade**: pasang APK legacy 4.11.2 → login → set online → pasang 4.11.3 di atasnya; verifikasi tetap login, FCM terdaftar, channel notifikasi dan pengaturan driver utuh, service lanjut. Dicatat sebagai item baru di `TESTING_PLAN.md`.
- **Signing**: pastikan keystore release modern = keystore yang terdaftar di Play Console (bukan keystore lokal berbeda).
- **Manifest**: bandingkan izin/komponen/intent-filter lengkap dengan legacy (selisih izin sudah diketahui: hanya `C2D_MESSAGE` yang lama).
- **Pembersihan**: hapus `ui/screens`, `ui/navigation`, `ui/model` (prototipe mati) dan `core/network/api/OrderApiService` (tak pernah di-inject) setelah build hijau; jalankan lint `UnusedResources`.
- **R8**: `isMinifyEnabled=false` di release — putuskan terpisah (di luar parity), tercatat sebagai risiko.
- Perbarui `MIGRATION_PLAN.md` & `TESTING_PLAN.md` agar sesuai kondisi akhir.

## 6. Urutan & rilis

```
A (fondasi) ──┬─> B (order masuk & notifikasi) ─┐
              ├─> C (service lokasi) ───────────┼─> D (order aktif) ─> E (home) ─> F (dompet) ─> G (sisanya) ─> H (rilis)
              └─────────────────────────────────┘
```

- A dikerjakan dulu (dipakai semua). B dan C independen satu sama lain; D membutuhkan `DriverLocationStore` dari C.
- Prioritas production: **A–F wajib**, G sebaiknya, H sebelum rilis. Bila waktu mepet, G dipecah dan item 🟡 ditunda.
- Tiap paket = satu branch/commit terpisah dengan tes hijau; tidak ada paket yang membutuhkan perubahan backend.

## 7. Strategi pengujian

- **Unit (JVM)**: semua `*Policy`, `ApiErrors`, katalog, DTO mapping, ViewModel dengan repository fake (`kotlinx-coroutines-test`). Pola tes yang sudah ada di `app/src/test` dipakai ulang.
- **Kontrak**: perluas `scripts/audit_parity.py` agar juga membandingkan **nama field body** (`@SerializedName`/properti) legacy vs modern, dan jalankan di tiap paket.
- **Manual di device** (tidak bisa diotomasi): notifikasi saat terkunci, service di Xiaomi/Oppo/Vivo, reboot, upgrade in-place, Directions & peta. Ditambahkan ke `TESTING_PLAN.md` per paket.
- Backend production **tidak** dipanggil dari tes; uji endpoint hanya di staging atau akun dummy sesuai `TESTING_PLAN.md` §0.

## 8. Risiko & mitigasi

| Risiko | Mitigasi |
|--------|----------|
| `FLAG_INSISTENT` + full-screen intent berperilaku beda per OEM/Android 14+ | Port persis legacy (targetSdk 35 sama); uji manual di ≥3 merek; degradasi ke heads-up bila izin full-screen tidak ada |
| Kunci Directions API terekspos di APK | Sama dengan legacy; batasi kunci di Google Console (paket + SHA-1); kunci diambil dari `local.properties`/CI secret |
| Alarm restart memulai FGS dari background ditolak di Android 12+ | Tangkap exception, catat; service yang masih hidup tidak terpengaruh; perilaku identik legacy |
| Mengganti layar Top Up memengaruhi alur uang | Port katalog & kode metode persis; tes batas jam; uji end-to-end dengan akun dummy sebelum rilis |
| Konstanta bisnis hard-code (kota OkeFast, terminal, kode metode) bisa usang | Dikumpulkan di satu tempat dengan komentar sumber; dicatat agar backend/produk bisa mengonfirmasi |
| Salin `strings.xml` legacy membawa teks tak terpakai | Lint `UnusedResources` di paket H |
| Tidak ada riwayat git | Commit kondisi sekarang sebelum paket A; tiap paket di commit terpisah |

## 9. Di luar cakupan

Login Google; fragment update software; styling visual; penggantian R8/minify; OneSignal (no-op di legacy); `OrderPaymentsActivity` (deprecated di legacy — layar modern dibiarkan);
endpoint `points/*` (tidak dipakai layar legacy); perubahan backend.
