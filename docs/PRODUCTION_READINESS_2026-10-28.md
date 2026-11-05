# Audit kesiapan production OkeDriver Compose

Tanggal: 28 Oktober 2026 (Asia/Jakarta).

> Dokumen ini merupakan audit **sebelum perbaikan**. Implementasi dan hasil validasi sesudahnya dicatat di [FEATURE_PARITY_2026-10-28.md](FEATURE_PARITY_2026-10-28.md).

## Keputusan

**Belum layak menggantikan seluruh pengguna `okedriver` di production.** Fitur utama sudah banyak terhubung ke API nyata, tetapi masih ada penghambat upgrade, kompatibilitas perangkat, pelacakan lokasi, dan kesetaraan alur operasional. Kandidat ini lebih tepat untuk persiapan staging/UAT setelah penghambat utama diperbaiki.

Jika dibutuhkan angka, penilaian engineering saat ini **6/10 untuk kesiapan menggantikan legacy**. Ini penilaian kualitatif berdasarkan temuan, bukan persentase cakupan kode, hasil pengujian pengguna, atau peluang bebas bug.

Audit menggunakan isi working tree kedua project, termasuk perubahan yang belum di-commit. Tidak ada perubahan kode aplikasi atau transaksi backend production yang dilakukan dalam audit ini.

## Bukti pemeriksaan

| Pemeriksaan | Hasil |
|---|---|
| Unit test debug | 56 test, 19 suite, 0 failure, 0 error |
| `:app:assembleRelease` | APK release berhasil dibuat |
| `:app:lintRelease` | Gagal: 3 error, 61 warning, 2 hint; perintah Gradle gabungan berakhir exit code 1 |
| Konfigurasi release | Validasi keberadaan signing config, keystore, dan Maps key lolos; ini tidak membuktikan pembatasan API key di Google Cloud benar |
| Identitas APK | Application ID sama: `id.okejack.okedriver`; sertifikat SHA-256 APK lokal kedua project cocok |
| Native / packaging | Empat library native 64-bit lolos pemeriksaan ELF load alignment 16 KB; `zipalign -c -P 16 4` lolos. Ini tidak menggantikan uji runtime pada perangkat 16 KB. |
| Inventaris API statis | 60 kontrak legacy, 58 kontrak modern; deklarasi endpoint tidak membuktikan fitur bekerja |
| Perangkat terhubung | `adb devices` kosong; tidak ada uji perangkat dalam audit ini |
| Instrumentation test | Hanya contoh pemeriksaan package name; belum ada pengujian alur Compose end-to-end |

Perintah validasi:

```text
.\gradlew.bat :app:testDebugUnitTest :app:assembleRelease :app:lintRelease --console=plain
py scripts/audit_parity.py --output build/reports/parity-contracts.json
py scripts/check_native_alignment.py app/build/outputs/apk/release/app-release.apk
```

## Penghambat prioritas tinggi

### 1. Upgrade dari legacy terbaru terhalang versionCode

- Modern: `4.11.3 (4855)` di [app/build.gradle.kts](../app/build.gradle.kts#L48).
- Legacy saat audit: `4.13.0 (4864)` di [build.gradle.kts legacy](../../okedriver/app/build.gradle.kts#L50).
- Jika APK 4864 sudah terpasang, APK 4855 menjadi downgrade, bukan update normal. Naikkan code di atas versi tertinggi yang sudah didistribusikan; angka maksimum di Play Console belum diperiksa.
- Signing APK lokal cocok, tetapi status Play App Signing/sertifikat distribusi Play belum diperiksa.

Referensi: [Google Play: version codes harus meningkat](https://support.google.com/googleplay/android-developer/answer/17367361).

### 2. Berpotensi crash pada Android 6 / API 23

[OnlineDriverService.kt](../app/src/main/java/id/okejack/okedriver/core/service/OnlineDriverService.kt#L129) memakai `stopForeground(STOP_FOREGROUND_REMOVE)` tanpa guard API 24, sedangkan `minSdk` masih 23. Pemanggilan overload ini pada API 23 tidak tersedia. Jalur ini dipakai saat menghentikan layanan, termasuk `onDestroy()`.

Gunakan overload yang kompatibel atau percabangan versi, lalu uji start/stop/offline/logout pada API 23. Lint juga mendeteksi masalah ini sebagai `NewApi`.

Dua error lint lainnya adalah `MarkerState(...)` yang dibuat saat composition tanpa `remember`, pada [HomeScreen.kt:1212](../app/src/main/java/id/okejack/okedriver/feature/home/presentation/HomeScreen.kt#L1212) dan [OrderContent.kt:206](../app/src/main/java/id/okejack/okedriver/feature/order/presentation/components/OrderContent.kt#L206). Perbaiki lifecycle state marker dan pastikan posisi tetap ikut berubah; jangan menutup temuan dengan lint baseline/suppression saja. Laporan lengkap tersedia di [lint-results-release.html](../app/build/reports/lint-results-release.html).

### 3. Persetujuan pelacakan lokasi terbaru belum dipindahkan

Legacy kini memiliki [LocationAccess.kt](../../okedriver/app/src/main/java/id/okejack/okedriver/helpers/LocationAccess.kt) yang memisahkan consent, izin Android, dan permintaan tracking, termasuk pencabutan consent.

Di modern, [HomeScreen.kt](../app/src/main/java/id/okejack/okedriver/feature/home/presentation/HomeScreen.kt#L212) otomatis memulai layanan bila data driver online dan izin/GPS tersedia. [SessionDataStore.kt](../app/src/main/java/id/okejack/okedriver/core/datastore/SessionDataStore.kt#L20) memigrasikan status online dari legacy, tetapi tidak memigrasikan atau memeriksa consent. Tidak ditemukan alur disclosure/consent setara di kode modern yang aktif.

Skenario berisiko: driver upgrade dengan izin lokasi yang sudah diberikan dan status online, lalu tracking berjalan kembali tanpa memeriksa keputusan consent terbaru. Samakan alur persetujuan, penolakan, pencabutan, offline, dan logout sebelum distribusi.

Selain itu, [BootCompletedReceiver.kt](../app/src/main/java/id/okejack/okedriver/core/service/BootCompletedReceiver.kt#L25) memerlukan izin background, tetapi modern tidak menyediakan alur permintaannya. Pemulihan sesudah reboot karena itu tidak dapat diandalkan untuk instalasi yang hanya memiliki izin foreground. Perbaikan harus mengikuti aturan Android; jangan sekadar menghapus pemeriksaan izin.

Referensi: [Google Play: penggunaan lokasi background dan disclosure](https://support.google.com/googleplay/android-developer/answer/9799150), [Android: lokasi background](https://developer.android.com/develop/sensors-and-location/location/background).

### 4. Notifikasi order background/layar terkunci belum setara

- [FCMReceiverHandler.kt](../app/src/main/java/id/okejack/okedriver/core/service/FCMReceiverHandler.kt#L85) membuat notifikasi biasa dengan content intent; belum ada perilaku alarm berulang/setara notifier legacy.
- [MainActivity.kt](../app/src/main/java/id/okejack/okedriver/MainActivity.kt#L45) hanya mengonsumsi event order otomatis saat lifecycle `RESUMED`. Di background, pengguna perlu berinteraksi dengan notifikasi untuk membuka layar order.
- Channel modern (`okedriver_order`, `okedriver_chat`) berbeda dari channel legacy, sehingga pengaturan suara/channel driver lama tidak digunakan oleh channel baru.
- Belum ada bukti penerimaan, suara, dan respons order di Doze, layar terkunci, app mati, atau perangkat OEM.

Ini perbedaan implementasi yang terbukti; jumlah order yang akan terlewat belum diukur. Tetapkan pengalaman notifikasi yang sesuai Android modern lalu uji di perangkat. Menambahkan full-screen intent saja tidak menjamin kesetaraan dan perlu memperhatikan [pembatasan Android](https://developer.android.com/about/versions/14/behavior-changes-14#secure-fsi-notifications).

### 5. Nilai terminal antrean tidak mengikuti kontrak legacy

[HomeScreen.kt](../app/src/main/java/id/okejack/okedriver/feature/home/presentation/HomeScreen.kt#L1456) mengirim literal `Terminal 1`, `Terminal 2`, `Terminal 3`. Legacy menggunakan `QueueLocations.ALL`, yaitu `Bandara Soetta` dan `Pasar Senen`, untuk field backend `terminal`.

Ini bukan perbedaan label visual saja: payload request berbeda. Samakan nilai terminal, lalu verifikasi antrean untuk kedua lokasi di staging.

### 6. Jenis item belanja hilang saat membaca dan mengirim ulang order

Legacy [OrderItem.kt](../../okedriver/app/src/main/java/id/okejack/okedriver/models/OrderItem.kt) membawa `type` untuk item, pembayaran, dan parkir. Modern [OrderItemDto](../app/src/main/java/id/okejack/okedriver/feature/order/data/remote/dto/OrderResponseDto.kt#L81) dan `OrderLineItem` tidak membawa field itu. `toEditable()` membuat `EditableItem` dengan default `TYPE_ITEM`.

Akibatnya, item `type=1` atau `type=2` yang sudah ada di respons akan dikirim sebagai `type=0` ketika daftar disimpan ulang. Ini berpotensi mengubah pengelompokan biaya. Pertahankan jenis item sepanjang DTO → domain → editor → request, lalu uji round-trip dengan payload nyata.

Editor juga hanya dapat dibuka dari `ItemsCard` ketika daftar item tidak kosong. Alur menambahkan belanja pertama pada order kosong perlu disediakan dan diuji.

## Perbedaan fitur dan risiko tambahan

| Area | Kondisi saat audit |
|---|---|
| Top up | Modern mengambil metode dari `payment-methods`; legacy aktif memakai katalog QRIS/bank/VA dengan kode metode tetap. Batas bank transfer pukul 05.00–21.00 WIB belum diterapkan di modern. Kesetaraan QRIS 16 dan VA 22–25 belum terbukti dari respons backend. |
| Pembatalan order | `DriverOrder.canCancel` mengizinkan `ASSIGNED`; UI legacy menonaktifkan cancel pada status tersebut. Perlu menyamakan aturan agar driver tidak mendapat aksi yang ditolak server. |
| Peta order | Modern menampilkan marker target dan membuka Maps eksternal; rute/polyline in-app dari Directions belum setara dengan legacy. |
| Driver bid | Modern dapat memakai rentang dari server; legacy memakai rentang tetap. Periksa aturan final bisnis sebelum menyatakan sama. |
| Error server | Banyak DTO menampilkan kode `error` mentah. UI masih dapat menampilkan `payment_failed`, `user_suspended`, dan sejenisnya tanpa penjelasan/tindakan spesifik yang setara legacy. |
| FCM setelah login | Hasil registrasi FCM dipanggil tetapi tidak menentukan sukses login; belum ada mekanisme retry persisten yang terlihat bila registrasi gagal. Perlu uji putus jaringan sesaat setelah login dan setelah migrasi. |
| Riwayat/detail | Paging order tersedia, tetapi detail skor/review, informasi pembatalan, dan metadata layanan khusus belum selengkap model legacy. |
| Kompatibilitas | Modern minimum API 23; legacy API 21. Android 5.0/5.1 tidak dapat memakai pengganti ini. Jumlah driver terdampak belum diketahui. |
| Bahasa/tampilan | Banyak teks hard-code Indonesia dan layar prototype `ui/` masih ada. Belum ada bukti kesetaraan bahasa, font besar, layar kecil, atau landscape. |
| Backup sesi | Manifest masih `allowBackup=true` dengan aturan backup contoh. Penanganan token DataStore pada backup/restore perlu diaudit sebelum rilis. |
| Kandidat rilis | Banyak implementasi dan test masih modified/untracked. Perlu commit kandidat rilis yang dapat direproduksi dan di-review. Tidak ada commit/push dilakukan dalam audit ini. |

Inventaris statis juga menandai Directions `GET json`, merchant `GET vendors/{}`, dan deposit manual `POST payments/deposits`. Sebagian call site legacy berada di alur lama/prototipe; temuan ini **tidak** berarti tiga fitur produksi pasti hilang. Tentukan dulu alur yang masih digunakan.

## Yang sudah baik

- Compose, ViewModel, Hilt, repository, dan DataStore sudah dipakai pada jalur navigasi aktif.
- Login email/Google, migrasi sesi, dan penanganan sesi expired sudah diimplementasikan.
- Home, online/offline, absensi, flag attendance/OkeFast, chat, riwayat, profil, referral, berita, dan pengaturan mempunyai integrasi API.
- Saldo biasa dan saldo penarikan diambil terpisah; withdraw dan konversi saldo sudah tersedia.
- QRIS order memiliki polling, pemeriksaan pembayaran server, fallback cash, dan pembeda prepayment vs finish. Ada regression test untuk beberapa skenario ini.
- QRIS top up, pemeliharaan QR saat respons status parsial, countdown, dan unduh gambar sudah diimplementasikan.
- Target SDK 36; logging HTTP body dimatikan pada release; guard konfigurasi release tersedia.

Keberadaan implementasi tersebut belum menggantikan pengujian integrasi dengan backend dan perangkat.

## Syarat sebelum menggantikan legacy

1. Tutup penghambat versi, lint/API 23, consent/tracking, notifikasi order, payload antrean, serta jenis item belanja.
2. Tetapkan kontrak top up yang benar-benar dipakai dan samakan aturan cancel/bid/error dengan bisnis.
3. Uji upgrade **tanpa uninstall** dari APK legacy yang benar-benar didistribusikan: sesi, status online, consent, channel suara, saldo, dan order aktif.
4. Uji perjalanan lengkap di staging: masuk → terima/tolak → mulai → chat/edit belanja → QRIS/cash → selesai → saldo/riwayat. Tambahkan koneksi putus, pembayaran terlambat, klik ganda, app dibunuh, dan process recreation.
5. Uji perangkat API 23, Android modern, serta perangkat OEM representatif; mencakup background, layar terkunci, Doze, reboot, GPS mati, dan izin dicabut.
6. Buat kandidat rilis yang reproducible, verifikasi AAB/Play signing dan versi tertinggi pada semua track, lalu internal/closed testing sebelum rollout production bertahap dengan monitoring dan rencana pemulihan.

Belum diperiksa: respons backend nyata, Play Console, aturan API key Google Cloud, data perangkat driver, crash-free rate, latensi order/notifikasi, baterai, dan hasil UAT lapangan. Audit ini tidak memberikan jaminan kesetaraan 100%.
