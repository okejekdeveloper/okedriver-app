# OkeDriver Parity Audit — okedriver (legacy) vs okedriver-app (Compose)

> Pembaruan 2 November 2026: dokumen di bawah merupakan audit awal 6 Oktober. Implementasi fitur terbaru dicatat di [laporan parity](docs/FEATURE_PARITY_2026-10-28.md). Perubahan UI/UX yang kini diminta dan diterapkan dicatat terpisah di [audit UI/UX](docs/UI_UX_AUDIT_2026-11-02.md); penundaan styling pada audit awal tidak lagi menggambarkan cakupan pekerjaan sekarang.

> **Status: audit tahap 1 selesai, menunggu review & keputusan di §13. Belum ada kode yang diubah.**
> Acuan (source of truth): `okedriver` (legacy). Yang akan disesuaikan: `okedriver-app`.
> Cakupan yang disepakati: **perilaku & aturan bisnis, integrasi API, dan isi yang ditampilkan**
> (field, teks, kondisi tampil/sembunyi, state). Styling visual (warna, spacing, bentuk) **ditunda**.
> Dibuat 6 Okt 2026 dari pembacaan kode kedua project. Tidak ada riwayat git yang bisa dipakai, jadi
> "apa yang berubah" ditentukan dari membaca kode + tanggal modifikasi file.

Legenda keparahan: 🔴 P0 = risiko uang/order/akun, atau perilaku salah yang terlihat driver ·
🟠 P1 = perilaku beda tapi masih bisa dipulihkan · 🟡 P2 = teks/detail tampilan · ⚪ = butuh keputusan.

---

## 0. Ringkasan sementara

| Area | Kesimpulan | Gap utama |
|------|-----------|-----------|
| Kontrak endpoint (method + URL + nama field body) | ✅ Hampir sama | `payment-methods` dipanggil lewat base URL **berbeda** (§8.1); `vendors/{id}` (katalog merchant) tidak dipakai; `GET json` (Directions) tidak ada |
| QRIS order (perubahan 17 Sep & 5 Okt) | ✅ Sudah ter-port cukup setia | Teks beda, tidak ada jeda "Pembayaran Berhasil", fallback `payment_failed` hanya jika provider `qris` |
| Absensi (perubahan 17 Sep) | ✅ Sudah ter-port setia | Validasi lokasi segar & pemetaan kode error absensi sudah sama; polling antrian 60 dtk & jam digital belum |
| **Penanganan kode error server** | 🔴 Hanya absensi yang dipetakan | Legacy memetakan ~50 kode error → pesan/aksi per layar; selain absensi, modern menampilkan string mentah dari server |
| **Notifikasi order masuk & service lokasi** | 🔴 Beda besar | Tanpa full-screen intent, suara tidak berulang, tanpa wake-lock/restart berkala/autostart OEM, channel ID berbeda, layar tidak menyala di lock-screen |
| **Antrian bandara** | 🔴 Nilai terminal beda | Legacy kirim `Bandara Soetta`/`Pasar Senen`; modern kirim `Terminal 1/2/3` |
| **Top Up** | 🔴 Alur berbeda | Modern memakai alur lama (daftar metode dari API); legacy aktif memakai katalog hard-code QRIS/Bank/VA + jam transfer 05–21 WIB |
| Layar Order Aktif / Order Masuk | 🔴 Banyak beda | Tanpa rute/marker driver, tanpa konfirmasi Finish/Tolak, cancel boleh saat ASSIGNED (legacy menonaktifkan), tanpa kode verifikasi Express |
| Home | 🔴/🟠 | Tidak auto-buka order aktif; OkeFast tampil untuk semua kota; tanpa heatmap/MOTD cooldown/dialog peringatan online |
| Teks/lokalisasi | 🟠 Beda | Legacy punya `strings.xml` (EN + ID, ~600 baris); modern hard-code Indonesian, `strings.xml` hampir kosong |
| Jalur upgrade production | 🔴 Perlu dipastikan | Sesi dimigrasi ✅; channel notifikasi, signing key, `minSdk` 23 vs 21 perlu keputusan |

---

## 1. Lintas-fitur (cross-cutting)

### 1.1 🔴 Pemetaan kode error server → pesan & aksi

**Legacy:** setiap layar memetakan `response.error` ke aksi/pesan lokal (lihat `ErrorMessages.kt`, 50 kode).
**Modern:** `*Dto.errorMessage = error.ifBlank { message.ifBlank { "<teks default>" } }` → kalau server
mengirim `{"error":"order_action_departure_delayed"}`, driver akan melihat teks `order_action_departure_delayed`
mentah. Satu-satunya pemetaan yang ada: kode **absensi** (huruf besar, `HomeRepositoryImpl.attendanceErrorMessage`,
sudah sama dengan legacy), `session_not_valid` (terpusat di `AuthInterceptor`), dan `payment_failed`
(`CurrentOrderViewModel`). Semua kode lain di tabel bawah tidak dipetakan (pencarian `bad_auth`,
`user_suspended`, `driver_balance_not_sufficient`, dst. di `okedriver-app` tidak menemukan apa pun).

Aturan legacy yang harus dipertahankan (dikumpulkan dari seluruh Activity):

| Kode error | Layar legacy | Perilaku legacy |
|-----------|--------------|-----------------|
| `session_not_valid` | semua layar | Logout paksa → Login. *(modern: sudah ditangani terpusat di `AuthInterceptor` ✅, perlu diverifikasi alur redirect-nya)* |
| `bad_auth` | Login | "Email/Kata Sandi salah" |
| `input_not_valid` | Login | "Mohon isi semua kolom!" |
| `user_not_activated` | Login, Home, EditProfile, DriverBid | "Galat: Akun belum diaktifkan!" |
| `user_suspended` | Login, Home, EditProfile, DriverBid | "Galat: Akun telah disuspend!" — di Login **menyertakan `meta.suspension_reason`** |
| `bad_auth_google_not_verified` / `_not_registered` | Login Google | pesan khusus (Google login belum ada di modern) |
| `user_has_unfinished_order` | Home (toggle online), EditProfile, DriverBid, Withdraw | "Anda memiliki permintaan yang belum diseleseikan" (Home: Snackbar *indefinite* + tombol OK) |
| `order_action_departure_delayed` | Order Aktif | Snackbar "Harap tunggu beberapa saat" + aksi "Sip" (merah) |
| `order_action_arrival_delayed` | Order Aktif | idem |
| `order_progress_delayed` | Order Aktif | Snackbar dengan **pesan dari server** + aksi "Sip" |
| `payment_failed` | Order Aktif | Buka layar QRIS (apa pun provider-nya) |
| `order_cancelled` | Notifier (order masuk) | Toast "Permintaan telah dibatalkan" → Home |
| `order_not_available` | Notifier | Toast "Job tidak tersedia atau mungkin sudah dibatalkan" → Home |
| `no_access` | Chat | Toast "Anda tidak mempunyai akses disini!" → keluar layar |
| `max_shopping_edit` | Edit Belanja | Snackbar indefinite "Anda tidak bisa mengubah barang belanja lagi!" |
| `unable_to_edit_shopping_items` | Edit Belanja | Snackbar indefinite "Anda tidak bisa merubah barang belanja sekarang!" |
| `driver_balance_not_sufficient` | Withdraw, Queue | "Error: Saldo anda tidak cukup" (Queue: dialog dengan hint hubungi admin) |
| `pending_withdraw_request` | Withdraw | "Error: Anda memiliki permintaan tarik saldo" |
| `invalid_withdraw_amount` | Withdraw | "Error: Jumlah Penarikan Tidak Sesuai." |
| `bonus_box_not_found` / `bonus_box_redeemed` | Bonus Box | "Bonus tidak ditemukan" / "Bonus telah dibuka!" |
| `existing_unconfirmed_topup_confirmation` | Top Up | "Mohon tunggu hingga konfirmasi top up sebelumnya diterima" |
| `outside_service_area` | Queue | Dialog "Di Luar Area Layanan" + hint |
| *(lainnya)* | semua | `toast(error_unknown)` "Galat tidak diketahui penyebabnya" |

**Rencana perbaikan:** satu `ErrorMessages` + `ApiErrorMapper` terpusat di `core/network` (kode → string resource),
dipakai semua repository; aksi khusus (logout, buka QRIS, keluar layar) tetap di ViewModel via tipe error yang
terstruktur (bukan membandingkan string). Detail ada di tahap spec.

### 1.2 🟠 Teks & lokalisasi

- Legacy: `values/strings.xml` (EN, 687 baris) + `values-in/strings.xml` (ID, 608 baris).
- Modern: `values/strings.xml` 2 baris; semua teks hard-code Indonesian di Composable.
- Teks yang **berbeda** untuk hal yang sama (contoh dari Order Aktif):

| Konteks | Legacy (ID) | Modern |
|--------|-------------|--------|
| Alasan cancel 1 | "Tidak dapat menghubungi pelanggan" | "Customer tidak dapat dihubungi" |
| Alasan cancel 2 | "Restoran atau tempat belanja tutup" | "Toko tutup" |
| Alasan cancel 3 | "Customer meminta pembatalan" | "Customer membatalkan" |
| Alasan cancel 4 | "Tidak dapat mencapai lokasi (macet, lokasi tidak aman, dll)" | "Lokasi tidak dapat dijangkau" |
| Judul dialog cancel | "Mohon berikan alasan kenapa anda membatalkan job ini?" | "Batalkan order?" |
| Peringatan penalti | "Perlu diperhatikan, membatalkan job memungkinkan anda mendapatkan penalty." | **tidak ada** |
| Label QRIS lunas | "QRIS sudah lunas ✓ — geser untuk selesai" | "Pembayaran QRIS sudah lunas. Silakan selesaikan order." |
| Konfirmasi QRIS jauh | "Kamu masih sekitar %1$s dari tujuan. Tetap tampilkan QRIS ke customer?" (m / km) | "Anda masih N meter dari tujuan. Yakin ingin menampilkan QRIS sekarang?" |
| Tombol QRIS | "Tampilkan QRIS ke Customer" | "Tampilkan QRIS" |
| Konfirmasi aksi | "Apa anda yakin hendak melakukan hal ini?" | **tidak ada** |
| Selesai | "Terima kasih telah menyelesaikan job ini." (toast) | **tidak ada** |
| Label layanan | Ojek / Kurir / Belanja / Food / … (`service_*`) | OkeRide / OkeSend / OkeShop / OkeFood / … |

⚪ **Keputusan:** "sama persis" berarti kita **ambil teks legacy apa adanya**, termasuk istilah "job"/"Kurir"/"Ojek"?
Dan apakah locale EN (default) juga harus ada, atau cukup ID? *(rekomendasi: pindahkan semua teks ke `strings.xml`
(ID + EN) dengan isi dari legacy.)*

### 1.3 Duplikasi lapisan API di modern

`okedriver-app` punya **dua set** Retrofit service: `core/network/api/*ApiService.kt` (mengembalikan
`BaseResponse<JsonElement>`) dan `feature/*/data/remote/*ApiService.kt` (DTO bertipe). Terverifikasi:
`core/.../OrderApiService` hanya di-*provide* di `NetworkModule` dan **tidak pernah di-inject** → kode mati
(inilah yang dilaporkan `audit_parity.py` sebagai `vendors/{id}` "declaration only"). Sebaliknya
`core/.../DriverApiService`, `FcmApiService`, `CommonApiService` **masih dipakai** (`OnlineDriverService`,
`DeviceSessionReporter`, `AuthRepositoryImpl`, `InitViewModel`, `AttendanceViewModel`). Jadi tidak bisa dihapus
sembarangan — pembersihan dilakukan per-service setelah dicek call site-nya.

---

## 2. Order Aktif (`CurrentOrderActivity` ↔ `CurrentOrderScreen`/`CurrentOrderViewModel`/`OrderContent`)

Legacy: 926 baris, diubah 5 Okt. Modern: ±450 baris + 618 baris `OrderContent`.

| # | Aturan/perilaku legacy | Modern | Sev |
|---|-----------------------|--------|-----|
| 2.1 | Peta layar penuh: marker driver (ikon motor untuk RIDE, mobil untuk lainnya), marker tujuan, **rute polyline Google Directions** dari lokasi driver → tujuan, kamera ke tujuan, update tiap `LocationUpdatedEvent` | Peta 220dp berisi **satu marker tujuan** saja; tidak ada marker driver, tidak ada rute, tidak update lokasi | 🟠 |
| 2.2 | Start/Finish memakai **slider geser** (`SlidingButton`, `card_start_button`) yang memicu aksi tanpa dialog; slider kembali ke posisi awal setelah 3 dtk. *(Ada slider kedua `currentOrder_actionButton` dengan dialog "Apa anda yakin hendak melakukan hal ini?", tetapi container-nya `visibility="gone"` — UI lama yang tidak tampil.)* | Tombol biasa "Mulai"/"Selesaikan" langsung memanggil API; **tidak ada slider** (sengaja belum dibuat, tercatat ❌ di MIGRATION_PLAN) → salah-tap = trip selesai | 🔴 |
| 2.3 | Setelah finish: toast "Terima kasih…" lalu buka detail order | Langsung `onOrderFinished` (tanpa toast) | 🟡 |
| 2.4 | `onCancelButton` mengizinkan PENDING/ASSIGNED, di luar itu dialog "Anda tidak bisa membatalkan job ini sekarang!". **Tetapi** `CurrentOrderDetailUI` menonaktifkan tombol cancel saat status `ASSIGNED` → efektif driver **tidak bisa cancel setelah menerima order** (hanya `DEPARTED` bisa menekan tombol, lalu ditolak dialog tadi) | `canCancel = PENDING \|\| ASSIGNED` → tombol cancel **aktif** setelah order diterima | 🔴 ⚪ (aturan bisnis: perlu konfirmasi apakah legacy memang melarang cancel setelah accept, atau itu bug legacy) |
| 2.5 | Dialog cancel: spinner 4 alasan + **peringatan penalti** + tombol "Tidak jadi"/"Batalkan job" | Radio 4 alasan, **tanpa peringatan penalti**, teks beda | 🟠 |
| 2.6 | Cancel sukses → ke Home (clear) | `popBackStack()` → kembali ke Home bila dibuka dari Home/Incoming; **belum diverifikasi** bila dibuka dari notifikasi (back stack kosong) | 🟡 |
| 2.7 | `order.id <= 0` dari server → toast "cancelled", stop timer, **tutup layar** | Tetap di layar dengan empty state "Order aktif tidak ditemukan" + "Coba lagi" | 🟠 |
| 2.8 | Kode error `*_delayed` → Snackbar khusus (lihat §1.1) | Menampilkan kode mentah di banner | 🔴 |
| 2.9 | `payment_failed` → buka QRIS **tanpa syarat provider** | Hanya jika `paymentProvider == "qris"` | 🟡 |
| 2.10 | Gagal jaringan saat ambil order → Snackbar indefinite "Tidak dapat terhubung" + "Coba lagi" | Banner error (tanpa teks lokal), ada ikon refresh | 🟡 |
| 2.11 | Auto-refresh tiap 15 dtk selama layar terlihat (start `onResume`, stop `onStop`) | Sama (repeatOnLifecycle RESUMED, 15 dtk) ✅ | ✅ |
| 2.12 | `keepScreenOn = true` selama di layar | Belum ditemukan | 🟠 |
| 2.13 | Tampilan: "Menuju ke <nama customer>", ikon per tipe layanan, jarak (km), total = fee + belanja, badge diskon, nama provider bayar | Sebagian ada (Hero/Customer/Payment card); label & urutan beda | 🟡 |
| 2.14 | Panggilan telepon: hanya jika `filterPhone(nomor)` tidak kosong | `isNotBlank()` saja (tanpa `filterPhone`) | 🟡 |
| 2.15 | Konstanta status `EXPRESS_FAILED=162`, `HOLD=161`, `RETURN=163` | Hanya `EXPRESS_PROGRESS=160` → label "Status 161" dst. | 🟠 |
| 2.16 | `Order` membawa `overtime`, `overtimeFee`, `paid_cash`, `paid_balance` | Tidak ada di `DriverOrder` | 🟠 (perlu cek tampil di layar mana) |
| 2.17 | Crashlytics `setCustomKey(type, vendor_id, vendor)` saat buka detail | Tidak ada (Crashlytics belum diintegrasikan — Fase 7) | ⚪ |

### 2.A QRIS order (`QrisOrderPaymentActivity` ↔ `OrderQrisScreen`/`OrderQrisViewModel`)

Sudah ter-port setia: generate sekali → polling 5 dtk (berhenti saat tidak RESUMED) → `is_paid` = selesai;
switch-to-cash dengan konfirmasi; ambang 500 m; tombol prepay vs swipe-finish; label lunas.
Tes unit modern (`OrderQrisDtoTest`, `CurrentOrderPaymentTest`) ada.

| # | Beda | Sev |
|---|------|-----|
| A.1 | Legacy: setelah lunas tampil "Pembayaran Berhasil" + overlay centang + toast "Pembayaran QRIS diterima", **tunggu 1,5 dtk** baru tutup. Modern: langsung tutup | 🟡 |
| A.2 | Legacy: toast "Metode pembayaran diubah ke Tunai" setelah switch | 🟡 |
| A.3 | Legacy: tarik-untuk-refresh; Modern: tombol "Cek Pembayaran" + "Muat ulang QRIS" (tambahan, bukan kekurangan) | ⚪ |
| A.4 | Legacy: `order_id == 0` → toast "Data order tidak ditemukan" + tutup | 🟡 |
| A.5 | `QrisPaymentPolicy.isUnpaid/isPaidQrisInTransit` ada di legacy sebagai fungsi terpisah; modern menaruhnya sebagai properti `DriverOrder.requiresQrisPayment/isQrisPaidInTransit` — logika **sama** | ✅ |

---

## 3. Order Masuk (`OrderNotifierActivity` ↔ `IncomingOrderScreen`/`IncomingOrderViewModel`)

| # | Aturan/perilaku legacy | Modern | Sev |
|---|-----------------------|--------|-----|
| 3.1 | **Tolak** → dialog konfirmasi `confirm_pending_order`, lalu `order/cancel` dengan `reason_code=98` (REJECTED), lalu ke Home | Tombol "Tolak" **langsung** memanggil cancel tanpa konfirmasi | 🔴 |
| 3.2 | Tombol **Back tidak bisa keluar**: memunculkan dialog konfirmasi tolak (driver tidak bisa "mengabaikan" order dengan Back) | Ada tombol "X" (`onClose`) yang menutup layar **tanpa** menolak/menerima → order tetap pending di server | 🔴 |
| 3.3 | Nominal = `order.fee` (ongkir saja); label **promo** muncul bila `fee != originalFee`; untuk SHOPPING/FOOD ada baris **total belanja** terpisah | "Estimasi" = `totalPaid` (ongkir + belanja + …) satu angka; tanpa label promo | 🟠 |
| 3.4 | Terima = `order/update-status`; sukses → hapus notifikasi order → buka Order Aktif (clear task) | Sama (navigasi `popUpTo`) ✅ | ✅ |
| 3.5 | Error terima: `order_cancelled` → toast "Permintaan telah dibatalkan" + Home; lainnya "Galat tidak diketahui…" + tombol aktif lagi | Banner kode mentah, tetap di layar | 🔴 (via §1.1) |
| 3.6 | Error cancel: `order_not_available` → toast; lainnya → Snackbar indefinite "Tidak dapat membatalkan job" + **Coba lagi** | Banner kode mentah | 🟠 |
| 3.7 | Gagal jaringan saat cancel → tombol terima/tolak **dinonaktifkan** & `canExit = true` (boleh Back) | Tombol aktif lagi | 🟡 |
| 3.8 | Event `PendingOrderRetrieved` dengan id lain → alert "Error 104: Job ini sudah tidak tersedia untuk anda" → Home | `loadOrder` memvalidasi id & status PENDING, teks "Order ini sudah tidak tersedia…" (tidak otomatis ke Home) | 🟡 |
| 3.9 | `showActivityOnLock()` + `keepScreenOn` + peta: layar menyala di atas lock-screen | **Terverifikasi tidak ada** (`setShowWhenLocked`/`setTurnScreenOn`/`keepScreenOn` tidak ditemukan di modern) | 🔴 |
| 3.10 | Tidak ada countdown otomatis (kode countdown di legacy sudah di-comment) | Tidak ada ✅ | ✅ |
| 3.11 | Ikon tipe layanan per `order.type`; TRIKE memakai ikon mobil | Kartu tanpa ikon tipe; label teks | 🟡 |

## 4. Notifikasi FCM (`FCMReceiverHandler` + `FCMNotificationHandlerService` ↔ `core/service/FCMReceiverHandler`)

Payload yang ditangani sama: `__type` ∈ {`job_received`, `chat`, `news`, `bonus`, lainnya→general}.

| # | Legacy | Modern | Sev |
|---|--------|--------|-----|
| 4.1 | Notifikasi order: **`setFullScreenIntent`** (muncul penuh di atas lock-screen), kategori `CALL`, **`FLAG_INSISTENT`** (suara **berulang** sampai ditanggapi), aksi "Terima job", judul `current_order_pending_warning`, isi = **alamat jemput** | Heads-up biasa, kategori `EVENT`, **tanpa full-screen, tanpa insistent**, isi = `message_body` atau "Ada informasi baru untuk Anda" (alamat jemput tidak ditampilkan). Izin `USE_FULL_SCREEN_INTENT` sudah dideklarasi di manifest tapi tidak dipakai | 🔴 |
| 4.2 | Channel order: `order_reminder_channel_id`, audio attr `USAGE_NOTIFICATION_RINGTONE`, vibrate + lights + `VISIBILITY_PUBLIC` | `okedriver_order`, `USAGE_NOTIFICATION`, tanpa lights | 🔴 |
| 4.3 | Payload `order` (JSON) di-parse → `OrderParcel` → notifier langsung terisi tanpa API | Hanya `order_id`; layar order masuk memanggil `order/pending-order` | 🟠 |
| 4.4 | Dedup lewat `FCMAtomicOrder` | `notify(tag=ORDER, id=orderId)` menimpa notifikasi lama ✅ | ✅ |
| 4.5 | `AudioSystemInitializer.initializeAudioSystem()` di `onCreate`/`onMessageReceived` | Belum ditemukan padanannya (cek apa yang dilakukan sebelum diputuskan perlu atau tidak) | 🟠 |
| 4.6 | Chat: notifikasi + `LocalBroadcast ACTION_ORDER_CHAT` agar `ChatActivity` yang terbuka ikut refresh | `DriverNotificationEvents.emit` (SharedFlow) — setara bila `ChatScreen` mengoleksinya (belum dicek) | 🟡 |
| 4.7 | Channel bonus / chat / news / general terpisah | 4 channel (location/order/chat/general); news & bonus masuk `general` | 🟡 |
| 4.8 | Token baru → `FCMRegistrationService` | `onNewToken` → `RegisterFcmTokenUseCase` ✅ | ✅ |
| 4.9 | App di foreground: order masuk membuka notifier via full-screen/pending intent | App di foreground: `DriverNotificationEvents` → `MainActivity.notificationTarget` → langsung navigasi ke Incoming ✅ | ✅ |

⚠️ **Channel ID penting untuk upgrade in-place** (lihat §7): Android menyimpan pengaturan channel per ID dan atribut
channel (suara, usage) **tidak bisa diubah** setelah dibuat. Channel yang benar-benar dipakai legacy saat mengirim notifikasi:

| Fungsi | ID legacy (dipakai) | Importance / atribut | ID modern |
|--------|--------------------|----------------------|-----------|
| Order masuk | `order_reminder_channel_id` | HIGH, `USAGE_NOTIFICATION_RINGTONE`, suara `onesignal_default_sound`, getar, lampu, publik | `okedriver_order` |
| Chat | `order_chat_channel_id` | HIGH (sama seperti di atas) | `okedriver_chat` |
| Berita | `news_channel_id` | HIGH | *(gabung general)* |
| Bonus | `bonus_channel_id` | HIGH | *(gabung general)* |
| Pesan umum | `general_message` | (dibuat di `InitActivity`) | `okedriver_general` |
| Service online | `driver_online_channel_1` | LOW | `okedriver_location` |

(`InitActivity` juga membuat ID lain dari string resource — `channels.PENDING_ORDER.1001`, `order_chat`, `news`, `general`,
`channels.BONUS` — tetapi pengirim notifikasi memakai ID di tabel; kelompok ini hanya "tertinggal" di HP driver.)
Bila modern memakai ID baru, channel lama tertinggal, pengaturan driver (volume/override DND) tidak terbawa, dan channel baru
mulai dari default. Memakai **ID dan atribut legacy** otomatis mempertahankan semuanya.

## 5. Service Lokasi & Online (`OnlineDriverService` ↔ `core/service/OnlineDriverService`)

Legacy berubah 17 Sep. Interval sama (update 30 dtk / tercepat 10 dtk / push 30 dtk), payload sama
(`{"last_position":"lat,lng"}` ke `driver/update-location`). Perbedaan:

| # | Legacy | Modern | Sev |
|---|--------|--------|-----|
| 5.1 | `PARTIAL_WAKE_LOCK` (maks 2 jam) saat online (izin `WAKE_LOCK` ada di manifest modern) | Tidak ada | 🔴 |
| 5.2 | **Restart berkala** tiap 30 menit via `AlarmManager.setRepeating(ELAPSED_REALTIME_WAKEUP)` | Tidak ada | 🔴 |
| 5.3 | `onTaskRemoved` → jadwalkan restart service 1 dtk kemudian (app di-swipe dari recent) | Tidak ada → di OEM agresif service mati | 🔴 |
| 5.4 | `InitActivity`: `requestAutoStartIfNeeded` (buka pengaturan **autostart** OEM Xiaomi/Oppo/Vivo/Asus/Huawei, sekali) + `requestIgnoreBatteryOptimization` (dialog sistem tiap start bila belum dikecualikan) | Tidak ada (tidak ada `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` di manifest modern) | 🔴 |
| 5.5 | Boot: `restartOnlineService` **tanpa syarat** | Boot: hanya jika token ada **dan** `driverOnline` **dan** izin lokasi background **dan** GPS aktif | 🟠 (modern lebih ketat; perlu keputusan) |
| 5.6 | Tiap lokasi baru: `LocationUpdatedEvent` (dipakai peta Order Aktif) + simpan `MY_LOCATION` di prefs | Tidak ada event/penyimpanan → Order Aktif tidak punya posisi live | 🟠 |
| 5.7 | Notifikasi service: builder `PRIORITY_MAX`/`DEFAULT_ALL`/full-screen, **tetapi** channel-nya `driver_online_channel_1` berimportance `LOW` → di Android 8+ channel yang menang, jadi efektif **senyap** | `PRIORITY_LOW`, senyap — setara di Android 8+; judul/isi masih hard-code ("OkeDriver Online" vs `online_driver_service_notification_*`) | 🟡 (teks) |
| 5.8 | Ping/Pong `PingEvent`/`PongEvent` antara Home & service (start service bila tak menjawab dalam 3 dtk) | `LaunchedEffect` start service saat online + izin siap | ✅ setara |
| 5.9 | Cek izin termasuk `FOREGROUND_SERVICE_LOCATION` | Izin itu *normal permission* (install-time) dan sudah di manifest ✅ | ✅ |
| 5.10 | Service berhenti sendiri jika tak ada izin lokasi | Berhenti bila izin hilang/GPS mati **atau** `driverOnline` jadi false di DataStore ✅ | ✅ |

## 6. Home (`MainActivity` ↔ `HomeScreen`/`HomeViewModel`)

| # | Aturan/perilaku legacy | Modern | Sev |
|---|-----------------------|--------|-----|
| 6.1 | `onResume` → `order/current-order`; **bila ada order aktif, otomatis buka Order Aktif & tutup Home** | Hanya kartu/tombol "buka order aktif" (`activeOrderId`); tidak ada auto-redirect | 🔴 |
| 6.2 | Sebelum online (tap kartu on/off saat offline): dialog **`location_tracking_warning`** ("Saya mengerti"/"Tidak jadi") | Tidak ada dialog; langsung minta izin/toggle | 🟠 |
| 6.3 | Setelah berhasil online: dialog kustom `before_online_dialog` | Tidak ada | 🟠 |
| 6.4 | Toggle gagal `user_has_unfinished_order` → Snackbar *indefinite* + OK; sesi invalid → logout; lainnya "Galat tidak diketahui" | Banner teks mentah | 🔴 (via §1.1) |
| 6.5 | Kartu **OkeFast hanya untuk `city_id` ∈ {444, 436, 469, 470}**; transparan 40% bila belum absen; tap saat belum absen → dialog **"Belum Absen"** ("Absen Sekarang"/"Nanti") | Menu **OkeFast selalu tampil** di drawer untuk semua kota; tidak ada gating absen | 🔴 (aturan bisnis kota) |
| 6.6 | Cek status absen (`driver/check-absence-status`) **hanya untuk 4 kota itu** | Hanya jika `enableDriverQueue` (semantik beda) | 🟠 |
| 6.7 | Antrian (QueueNumberFragment) tampil bila `driver.enableDriverQueue`; error `driver_balance_not_sufficient` & `outside_service_area` → dialog + hint | Kartu antrian ada; pemetaan error tidak ada (§1.1) | 🟠 |
| 6.8 | MOTD: tampil bila ada pesan dan **>6 jam sejak ditutup**; tap → dialog pesan + "Sembunyikan sementara" (simpan waktu) / OK | `MotdCard` selalu tampil bila `motd` tidak kosong; tanpa cooldown/sembunyikan | 🟠 |
| 6.9 | **Heatmap** lokasi order terbaru (`orders/recent-orders`) di peta Home; marker driver ikon motor (driverType 0) / mobil | Peta Home tanpa heatmap (ada layar Recent Orders terpisah) | 🟠 |
| 6.10 | Peringatan versi usang (teks merah, klik → Play Store) bila `VERSION_CODE < APP_BUILD` | Banner "Update" via `hasAppUpdate` + `InitScreen` (force/optional update) ✅ | ✅ |
| 6.11 | Drawer: Tutorial & Bantuan (`okejek.id/tutorial-oke-driver/`), Pengumuman, Saldo, Riwayat, Pengaturan, Profil, Logout, **label versi `Nama vX (code)`**, tombol Edit Profil di header | Home, Saldo, Riwayat, Order Terbaru, OkeFast, Profil, Pengaturan, Berita, Logout; **tanpa Tutorial/Bantuan & label versi** | 🟠 |
| 6.12 | Logout: toast "Sesi anda telah berakhir", `Prefs.clear()`, matikan service, ke Login | `AuthRepositoryImpl.logout`: stop service + unregister FCM + clear DataStore ✅ (tanpa toast) | ✅ |
| 6.13 | Saldo ditampilkan di Home | Ada ✅ | ✅ |
| 6.14 | Tombol pintas: Riwayat, Saldo, Profil | Ada ✅ | ✅ |
| 6.15 | `driver/info` membawa `session_id` baru → disimpan sebagai token | `HomeRepositoryImpl.cacheDriverProfile(driver, sessionId.ifBlank { currentToken })` ✅ | ✅ |

## 7. Jalur upgrade production (in-place update)

Kedua app memakai **`applicationId = id.okejack.okedriver`**; modern `versionCode 4855 / 4.11.3` > legacy `4854 / 4.11.2`.
Artinya `okedriver-app` akan **menimpa** app yang terpasang di HP driver. Konsekuensi:

| # | Hal | Status | Sev |
|---|-----|--------|-----|
| 7.1 | Sesi login: `SessionDataStore` memigrasi `OKEDRIVER_SESSION_ID`, `OKEDRIVER_DRIVER`, `OKEDRIVER_DRIVER_ID`, `firebase_app_token` dari SharedPreferences EasyPrefs | ✅ Sudah ada (perlu diuji di device nyata: upgrade dari 4.11.2 tanpa login ulang) | ✅ (uji) |
| 7.2 | Channel notifikasi: ID berbeda dari legacy (lihat §4) | ❌ Disarankan **memakai ID & atribut audio legacy** agar pengaturan driver terbawa | 🔴 |
| 7.3 | `minSdk` 23 (modern) vs 21 (legacy) | Driver di Android 5.0/5.1 **tidak akan menerima update** dan tertahan di versi lama | ⚪ keputusan bisnis |
| 7.4 | Signing: modern memakai `signingConfig release` | Wajib **keystore yang sama** dengan yang dipakai Play Console, kalau tidak update ditolak | 🔴 (verifikasi sebelum rilis) |
| 7.5 | `isMinifyEnabled = false` di release modern | Bukan parity, tapi menyangkut kesiapan production (Fase 7 — ProGuard/R8) | ⚪ |
| 7.6 | Izin & komponen manifest (service/receiver/permission) | Belum dibandingkan secara penuh | 🟠 (TODO audit) |

---

## 8. Saldo & Dompet (`activities/balance/*` ↔ `feature/wallet/*`)

Payload withdraw (`amount`, `is_convert_to_balance`), batas (maks Rp 1.000.000 per tarik, minimum konversi Rp 100), pengecekan
permintaan pending, dan daftar nominal (50k–1jt) **sudah sama**. Perbedaan:

| # | Legacy | Modern | Sev |
|---|--------|--------|-----|
| 8.1 | `payment-methods` dipanggil lewat root **`https://api.okejack.id/api/payment-methods`** (`CommonApiService` memakai base `api/`, bukan `api/okedriver/v2/`) | `WalletApiService` memanggil `payment-methods` relatif ke `…/api/okedriver/v2/` → URL **berbeda** (`…/okedriver/v2/payment-methods`). Belum bisa dipastikan apakah backend meng-alias; **tidak boleh diuji ke production** — butuh staging/konfirmasi backend | 🔴 |
| 8.2 | Alur Top Up aktif = `DepositNewActivity`: **katalog hard-code** — QRIS (id 16), Transfer Bank BCA 7 / Mandiri 5 / BNI 6 / BRI 9 (**hanya 05.00–20.59 WIB**, `hour in 5..20`, di luar jam tombol nonaktif + toast "Metode ini tidak tersedia saat ini."), Virtual Account BCA 22 / Mandiri 23 / BNI 24 / BRI 25 ("Dikenakan biaya layanan"); default = bank pertama yang aktif; judul tombol "Bayar dengan {bank}"; validasi "Nominal tidak boleh kosong." | `TopUpScreen` menampilkan daftar dari API `payment-methods` (alur lama `TopUpActivity`, yang sudah tidak dipanggil dari `BalanceActivity`), tanpa jam operasional, tanpa grup QRIS/Bank/VA, tanpa catatan biaya | 🔴 |
| 8.3 | Setelah `topup-request`: QRIS (id 16) → `QrisPaymentActivity`; selainnya → `DepositDetailActivity`; gagal → toast "Top-up gagal: {message}" | Selalu ke `TopUpDetail` (QRIS dibedakan lewat `isQris`) — hasil mirip, tapi teks gagal beda | 🟡 |
| 8.4 | QRIS top-up: hitung mundur ke `expired_date`, polling `deposit-detail` tiap 5 dtk (stop saat tidak resumed), status "Berhasil" → toast "Top up berhasil, saldo kamu sudah bertambah." + tutup 2,5 dtk; "Canceled/Expired/Kadaluarsa" → "Waktu Pembayaran Habis"; unduh QR ke galeri; tombol WhatsApp bantuan (62817381500) | Sisa waktu + unduh QR + WhatsApp ada ✅; polling 5 dtk saat RESUMED & non-terminal ada ✅; yang belum: toast sukses ("saldo kamu sudah bertambah"), toast kedaluwarsa, dan penutupan layar otomatis setelah sukses | 🟡 |
| 8.5 | Deposit non-QRIS: `createTopup` **retry** s.d. `maxRetry` tiap 2 dtk bila `vadata` kosong, lalu toast "Gagal memproses top-up. Silakan coba lagi."; metode `<10` (transfer manual) → `VaResultBankTransfer` (kode unik, salin jumlah/rekening/no. transaksi), `>10` (VA) → `VaBankActivity`; **kode unik disembunyikan bila metode >10**; logo bank per metode | `createPayment` sekali tanpa retry; satu tampilan VA generik; belum ada layar transfer manual dengan kode unik/salin jumlah | 🟠 |
| 8.6 | Riwayat top-up: **rentang tanggal** (default hari ini–hari ini, date picker), pencarian, paginasi, empty state | `TopUpHistoryViewModel.load()` satu kali tanpa filter/tanggal/paginasi | 🟠 |
| 8.7 | Riwayat transaksi & riwayat tarik: paginasi, judul berbeda (`transaction_history` / `withdraw_history`) | Paginasi ada ✅ | ✅ |
| 8.8 | Bonus Box: error `bonus_box_not_found` / `bonus_box_redeemed` dipetakan | Tanpa pemetaan (§1.1) | 🟠 |
| 8.9 | Withdraw: kartu "ada permintaan pending" menyembunyikan form; error `pending_withdraw_request`/`invalid_withdraw_amount`/`driver_balance_not_sufficient` dipetakan; tombol refresh; event analytics `withdraw_balance` | Pending → ditolak di ViewModel ✅; error server tanpa pemetaan; analytics belum ada (Fase 7) | 🟠 |
| 8.10 | Endpoint poin (`points/*`) ada di `OkedriverAPI` tetapi **tidak dipakai layar manapun** | — | ⚪ tidak perlu |

## 9. Order — Chat, Edit Belanja, Riwayat, Detail

| # | Legacy | Modern | Sev |
|---|--------|--------|-----|
| 9.1 | **Edit Belanja**: dialog peringatan `warning_edit_shopping_item` saat layar dibuka | Tidak ada | 🟠 |
| 9.2 | Opsi tambah per tipe: SHOPPING/FOOD → {Barang, Parkir, Tol, **Dari merchant**}; CAR → {Parkir, Tol}; tipe lain tidak ada opsi. Tombol berlabel "Edit barang belanja" (SHOPPING/FOOD) atau "Tambah biaya" (CAR) | Opsi sama untuk semua order (Barang/Parkir/Tol); **tidak ada "Dari merchant"** (`vendors/{id}` tidak dipanggil); layar tidak tahu tipe order | 🟠 |
| 9.3 | Setelah pernah diedit (`shoppingEditedAt` ≠ kosong): tambah → alert `error_max_shopping_edits`; item tidak bisa diedit; banner "tidak bisa diedit" | `isAlreadyEdited` → read-only ✅ (tanpa alert/teks yang sama) | 🟡 |
| 9.4 | Qty minimal 1; daftar tidak boleh kosong (`error_shopping_items_empty`); error `max_shopping_edit`/`unable_to_edit_shopping_items` → Snackbar indefinite | Qty ≥ 1 & kosong ditolak ✅; error server tanpa pemetaan | 🟠 |
| 9.5 | **Chat**: judul "Chat {nama customer}"; **5 balasan cepat** (`chat_texts`: "Lokasinya sesuai titik?", "Mohon ditunggu", "Saya dalam perjalanan", "Saya sudah di lokasi", "Oke"); Snackbar bila app customer belum mendukung chat (`android_app_version == "0"`); `no_access` → toast + keluar; ketuk pesan = toast waktu | Polling 5 dtk ✅; tanpa balasan cepat, tanpa judul nama, tanpa peringatan versi customer, tanpa `no_access` | 🟠 |
| 9.6 | **Detail order** menampilkan: kartu **Express** (kode verifikasi + ongkos express), **kurir** (pengirim & penerima **dengan tombol telepon**), info **vendor** (nama, alamat, telepon bila belum selesai), **addon** Massage/Rental (durasi, lembur, biaya lembur saat ARRIVED; alamat tujuan disembunyikan), kartu "Bayar ke merchant" (FOOD/MART) + bantuan, baris belanja disembunyikan untuk RIDE, label provider bayar (cash vs lainnya) | `OrderContent`: kurir **tanpa telepon**, **tanpa kode verifikasi Express** (DTO tidak membaca `express.verification_code`), **tanpa vendor**, **tanpa addon/overtime**, "Bayar ke merchant" tanpa bantuan | 🔴 (Express) / 🟠 |
| 9.7 | **Riwayat detail** (`ViewOrderActivity`): label "tidak tersedia" bila `driver_id` ≠ driver ini, label Selesai/Dibatalkan, **skor & review customer**, rincian bayar **saldo vs tunai** (`paid_balance`/`paid_cash`), diskon, info driver / "tidak ada info tambahan", tipe layanan lokal (`OrderTypes`) | `OrderDetailScreen` memakai `OrderContent` yang sama; **DTO tidak membaca** `score`, `cancel`, `paid_cash`, `paid_balance`, `food_vendor`, `overtime*`, `duration`, `package*`, `payments`, `coupon`, `data.android_app_version` | 🟠 |
| 9.8 | **Daftar riwayat**: 2 tab (`in_progress` aktif / selesai), paginasi, empty state, item: tipe + warna per tipe, jarak km, alamat jemput, ongkos berdiskon, ID format `tahun/bulan/tanggal/id` | Ada riwayat (`OrderHistoryScreen`); detail tampilan item & ID **belum dibandingkan** | 🟡 (perlu audit rinci) |
| 9.9 | Order Aktif: tombol telepon/chat/navigasi di kartu ringkas, tombol detail membuka fragment detail | Padanan ada di `OrderContent` ✅ | ✅ |
| 9.10 | `OrderPaymentsActivity` ditandai `@Deprecated("Not being used")` | `OrderPaymentsScreen` ada (tambahan, tombol "Detail") | ⚪ tidak perlu diporting; boleh dipertahankan/dihapus |

## 10. Auth & Init

| # | Legacy | Modern | Sev |
|---|--------|--------|-----|
| 10.1 | Login: pesan `bad_auth`, `input_not_valid`, `user_not_activated`, `user_suspended` + **alasan suspend** dari `meta.suspension_reason`; "Email dan password harus diisi!" | Tanpa pemetaan (§1.1); alasan suspend belum dibaca | 🔴 |
| 10.2 | **Login Google** (`login/google`, error `bad_auth_google_*`) | Belum ada (sudah tercatat di MIGRATION_PLAN) | ⚪ (di luar cakupan "perilaku saat ini"? — butuh keputusan) |
| 10.3 | Peringatan versi usang di layar Login (klik → Play Store) | Cek update di `InitScreen` ✅ (bentuk beda) | ✅ |
| 10.4 | `InitActivity`: buat **semua channel notifikasi** (§4), minta izin notifikasi, **alur izin lokasi** (halaman penjelasan, dialog tolak, dialog "lokasi tidak aktif" → pengaturan), **dialog Xiaomi "Notifikasi Tidak Berbunyi"** (bila channel order bisu) → buka pengaturan channel, autostart OEM + battery optimization (§5.4) | Izin lokasi/notifikasi diminta di Home saat akan online; channel dibuat saat service/FCM start; tanpa dialog Xiaomi/autostart/baterai | 🟠 |
| 10.5 | Logout paksa pada `session_not_valid` di semua layar | `AuthInterceptor.expireSession` + redirect ✅ (perlu uji alurnya) | ✅ (uji) |
| 10.6 | Setelah login: register FCM + OneSignal PID + `DeviceReportIntentService` + `ReportBadAppIntentService` (kirim **seluruh daftar** `R.array.bad_apps`, tiap ≥ 360 menit) | `DeviceSessionReporter` mengirim info perangkat + daftar bad-apps dengan jeda 360 menit ✅ (sama, termasuk perilaku "kirim seluruh daftar"); OneSignal: `RegisterPid.handleActionRegisterPID()` di legacy adalah **fungsi kosong** (no-op) dan tidak ada panggilan `OneSignal.*` aktif, jadi tidak ada gap | ✅ |

## 11. Fitur lain

| Fitur | Perbedaan | Sev |
|-------|-----------|-----|
| **Antrian bandara** | Terminal legacy: `Bandara Soetta`, `Pasar Senen` (`QueueLocations.ALL`); modern kirim `Terminal 1/2/3` (**nilai tidak dikenal server**). Tombol "Ambil antrian" aktif hanya bila queue aktif ∧ sudah absen ∧ belum punya nomor; label berubah bila sudah terdaftar. Nomor `0` ditampilkan "?" dengan rasio `0/total` | 🔴 |
| **Absensi** | Logika lokasi segar & kode error sama ✅. Beda: `maxUpdateAge` 5 dtk (legacy) vs 30 dtk (modern); jam/tanggal digital berjalan; sapaan "Selamat Datang, {nama}"; polling nomor antrian tiap 60 dtk; toast waktu & tutup 2 dtk; tombol "SELESAI" setelah absen | 🟡 |
| **Profil** | Legacy: tautan referral `https://okejack.id/driver/apply/driver?ref={id}` — **salin** (toast `referral_id_link_copied`) dan **bagikan** (chooser, `format_referral_share_message`). Modern: tampil kode + daftar driver, **tanpa salin/bagikan tautan** | 🟠 |
| **Edit Profil** | Legacy: nama bank **dropdown 7 pilihan** (BCA, Mandiri, Maybank, BNI, Permata, BRI, CIMB — nilai yang dikirim = teks panjang, mis. "Bank Central Asia (BCA)") + placeholder `select_bank`. Modern: **teks bebas** → risiko nama bank tidak baku saat penarikan. Toast `profile_saved` | 🟠 |
| **Driver Bid** | Legacy memakai array **hard-code** (`50000000, 25000, 80000, 125000, 250000, 500000, 1000000, 1500000, 2000000` dan `1000, 3, 6, 10, 25`); modern memakai nilai yang sama **tetapi memprioritaskan rentang dari server** (`payment/distance_bidding_range` hasil `app/init`) bila ada → bisa beda dari legacy. Validasi "pilih minimal satu layanan" ✅ | 🟠 |
| **Connect akun customer** | Teks bantuan `user_connect_help_text`, konfirmasi putus (dialog), tampilan terhubung/tidak — modern ada (`ConnectAccountScreen`); detail teks belum dibandingkan | 🟡 |
| **Pengaturan** | Legacy hanya satu item ("Bid") + toast `setting_not_available` | ✅ |
| **Berita** | Daftar paginasi ✅; detail: judul fallback `error_unable_get_news_data`, gambar, tombol aksi membuka URL ("Opening webpage") — modern ✅ setara | ✅ |
| **OkeFast** | Legacy memformat kode 6 digit `XXX XXX`; modern menampilkan tanpa spasi; modern punya "Salin Kode" (tambahan). Tombol berbagi legacy sudah di-comment (tidak ada) | 🟡 |
| **Order Terbaru (peta)** | Legacy: **heatmap** (radius 35, zoom 13, titik awal = lokasi terakhir). Modern: **marker** maks 80, zoom 12 | 🟡 |
| **Update software (fragment)** | `SoftwareUpdateNoticeFragment` belum ada di modern (tercatat). Fungsinya sebagian digantikan banner/InitScreen | ⚪ |

## 12. Hal yang sengaja **tidak** dibandingkan (di luar cakupan "perilaku & isi")

Styling visual (warna, tipografi, jarak, bentuk, animasi), arsitektur internal, penggantian RxJava→Flow, dan komponen
yang di legacy sudah mati (`legacy/MainActivity`, `OrderPaymentsActivity`, kode countdown di-comment, `AutoStartHelper.scheduleAutoRestart` di-comment).

## 13. Keputusan

| # | Topik | Keputusan | Status |
|---|-------|-----------|--------|
| 1 | Cancel saat ASSIGNED (§2.4) | **Ikuti legacy**: tombol cancel nonaktif saat `ASSIGNED`; di status selain PENDING/ASSIGNED muncul dialog "Anda tidak bisa membatalkan job ini sekarang!"; hanya PENDING yang membuka dialog alasan | ✅ diputuskan |
| 2 | Teks (§1.2) | **Ambil teks legacy persis**, sediakan **EN + ID** di `strings.xml` | ✅ diputuskan |
| 3 | Login Google & fragment update software (§10.2, §11) | **Fase terpisah** (di luar cakupan sekarang) | ✅ diputuskan |
| 4 | `minSdk` (§7.3) | **Ikuti modern** (tetap 23) | ✅ diputuskan |
| 5 | Restart service saat boot (§5.5) | Rekomendasi: **opsi C** — mulai bila token ada ∧ driver terakhir *online* ∧ izin lokasi foreground ∧ GPS aktif; **tanpa** syarat izin background (legacy tidak pernah memintanya, modern juga tidak pernah memintanya sehingga syarat itu membuat restart-saat-boot **tidak pernah jalan**) | ⏳ menunggu konfirmasi |
| 6 | `payment-methods` (§8.1) | Rekomendasi: **ganti layar Top Up modern dengan katalog legacy** (`DepositNewActivity`) sehingga endpoint ini tidak dipanggil sama sekali; tidak perlu konfirmasi backend | ⏳ menunggu konfirmasi |
| 7 | Rentang Driver Bid (§11) | Rekomendasi: **pakai angka tetap legacy**, abaikan rentang dari server (persis legacy) | ⏳ menunggu konfirmasi |
| 8 | Terminal antrian (§11) | Pakai `Bandara Soetta` & `Pasar Senen` seperti legacy. Bila server kini memang mengenali `Terminal 1/2/3`, itu harus dikonfirmasi backend — default: ikuti legacy | ⏳ default legacy |

## 14. Usulan urutan perbaikan (setelah audit disetujui)

Dikerjakan per paket, masing-masing dengan tes unit/instrumentasi yang relevan dan commit terpisah:

| Paket | Isi | Prioritas |
|-------|-----|-----------|
| **A. Fondasi** | `ApiError` terstruktur + pemetaan kode error terpusat; pindah teks ke `strings.xml` (ID+EN dari legacy) | 🔴 (dipakai semua paket lain) |
| **B. Order masuk & notifikasi** | Channel ID/atribut legacy; full-screen intent + `FLAG_INSISTENT` + kategori CALL + isi alamat jemput; layar menyala di lock-screen; dialog konfirmasi Tolak + Back tidak bisa keluar; nominal fee/promo/belanja | 🔴 |
| **C. Service lokasi** | Wake-lock, restart 30 mnt, `onTaskRemoved`, autostart OEM, izin abaikan baterai, event/penyimpanan lokasi untuk Order Aktif | 🔴 |
| **D. Order Aktif** | Konfirmasi Finish, cancel (aturan + penalti + teks), keluar bila order hilang, keepScreenOn, peta + rute + marker driver, kode verifikasi Express, kurir bertelepon, vendor/addon | 🔴/🟠 |
| **E. Home** | Auto-buka order aktif, dialog peringatan online, gating OkeFast per kota + dialog absen, MOTD cooldown, heatmap, drawer (tutorial/bantuan/versi), terminal antrian | 🔴/🟠 |
| **F. Dompet** | URL `payment-methods`, katalog top-up legacy + jam transfer, layar transfer manual & VA (retry, kode unik), riwayat dengan tanggal/paginasi, polling QRIS top-up | 🔴/🟠 |
| **G. Sisanya** | Chat (balasan cepat dll.), edit belanja (peringatan, tipe, merchant), riwayat/detail (skor, paid cash/balance), profil (share, dropdown bank), driver bid, absensi minor, OkeFast, order terbaru | 🟠/🟡 |
| **H. Upgrade production** | Uji upgrade 4.11.2→4.11.3 dari APK nyata, verifikasi signing key & izin manifest, hapus kode mati (`ui/`, `core/.../OrderApiService`) | 🔴 sebelum rilis |

## 15. Cakupan audit & batasannya

- Dibaca penuh: `CurrentOrderActivity`, `QrisOrderPaymentActivity`, `OrderNotifierActivity`, `FCMReceiverHandler`, `FCMNotificationHandlerService`,
  `OnlineDriverService`, `MainActivity` (Home), `AirportDriverAttendanceActivity`, `QueueNumberFragment`, `WithdrawBalanceActivity`, `DepositNewActivity`,
  `QrisPaymentActivity`, semua `forms/*`, `api/*`, `ErrorMessages`, `Order`, `PaymentService`, `QrisPaymentPolicy`.
- Dipindai (pola aturan, bukan baris-per-baris): `EditShoppingItemActivity`, `ChatActivity`, `ViewOrderActivity`, `CurrentOrderDetailUI`, `OrderListFragment`,
  `LoginActivity`, `InitActivity`, `EditProfileActivity`, `DriverBidActivity`, `ConnectUserSettingActivity`, `DepositDetailActivity`, `VaBank*`, `TopupHistory`, `Balance*`, `Transactions`, `BonusBox`, News, Profile, OkeFast, RecentOrderLocation.
- Dibaca penuh kemudian (6 Okt, dipakai spec): `DepositDetailActivity`, `VaResultBankTransfer`, `VaBankActivity`, `QrisPaymentActivity`, `ViewOrderActivity`, `OrderListFragment`, `RVOrdersAdapter`, `OrderTypes`, `ReportBadAppIntentService`, `RegisterPid`, manifest (selisih izin).
- **Belum**: `strings.xml` per kunci (ditangani paket A saat penyalinan), `DeviceReportIntentService` baris-per-baris (modern sudah mengirim payload yang sama).
- Tidak ada pengujian runtime ke backend (production tidak disentuh); semua temuan berdasarkan membaca kode. Item bertanda "verifikasi" butuh uji di device/staging.
