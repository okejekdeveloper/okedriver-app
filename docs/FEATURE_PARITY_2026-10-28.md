# Penyesuaian fitur OkeDriver Compose

Tanggal: 28 Oktober 2026. Acuan: working tree `okedriver` 4.13.0 (4864), termasuk perubahan lokal, dibandingkan dengan `okedriver-app` 4.13.1 (4865).

Dokumen ini mencatat implementasi setelah perbaikan. [Audit awal](PRODUCTION_READINESS_2026-10-28.md) tetap disimpan sebagai riwayat kondisi sebelum perbaikan. Status implementasi di bawah tidak berarti seluruh alur sudah diuji pada backend dan perangkat production.

## Cakupan fitur aktif

| Kelompok fitur | Kondisi implementasi setelah penyesuaian |
|---|---|
| Login email dan Google | Ada, memakai kontrak login legacy termasuk `token_id` untuk Google |
| Sesi dan upgrade | Migrasi SharedPreferences legacy ke DataStore; session expired ditangani; version code 4865 lebih tinggi dari acuan lokal 4864 |
| Registrasi FCM | Dijadwalkan lewat WorkManager dengan syarat jaringan dan exponential backoff; job dibatasi pada sesi yang menjadwalkannya dan dibatalkan saat logout |
| Home dan status driver | Online/offline, saldo, order aktif, MOTD, flag attendance dan OkeFast tersedia |
| Persetujuan lokasi | Disclosure EN/ID sebelum akses; consent eksplisit legacy yang masih sesuai versi dapat digunakan; pencabutan di Pengaturan menghentikan tracking |
| Pelacakan online | Izin, consent, permintaan tracking, online, dan token harus valid; berhenti saat offline/logout; lokasi terlalu lama tidak dikirim ulang |
| Reboot | Pemulihan hanya jika otorisasi Android mencukupi; bila belum mencukupi, notifikasi mengajak driver membuka app |
| Order masuk | Terima/tolak, konfirmasi penolakan, suara berulang dengan batas waktu, deep link notifikasi, dan tampilan layar terkunci saat layar order dibuka |
| Channel notifikasi | Menggunakan channel legacy yang sudah ada; jika instalasi sebelumnya memakai channel Compose, konfigurasi channel tersebut dipertahankan |
| Order berjalan | Mulai/selesai, refresh status, chat, rincian pembayaran, QRIS sebelum selesai atau saat finish, fallback cash, dan aturan cancel pending |
| Belanja dan biaya | Jenis item 0/1/2 dipertahankan saat round-trip; edit order kosong tersedia; perubahan setelah pernah diedit atau saat request berjalan dicegah |
| Peta dan navigasi | Marker disimpan dengan `remember`; polyline server atau Directions tersedia; panggilan Google memakai client tersendiri tanpa bearer token driver; navigasi eksternal mempunyai fallback |
| Detail order | Merchant dan telepon, pengirim/penerima, penilaian/review, alasan/tanggal pembatalan, kode verifikasi OkeFast, paket, durasi/overtime, dan rincian biaya tambahan |
| Riwayat order | Tab aktif/riwayat dan paging; refresh saat kembali ke layar |
| Absensi | Lokasi terbaru diperiksa dan dikirim sebelum mencatat absensi; akses memerlukan consent dan izin lokasi |
| Antrean | Pilihan dan payload disamakan dengan `Bandara Soetta` dan `Pasar Senen`; nilai lain ditolak sebelum API |
| OkeFast | Generate kode tersedia; akses menu mengikuti flag driver |
| Wallet | Saldo biasa dan saldo penarikan diambil dari endpoint terpisah; refresh saat kembali dari layar lain |
| Withdraw/konversi | Batas nominal dan pending request diperiksa sebelum pengiriman; klik ganda ditahan |
| Top up | Katalog aktif legacy: QRIS 16; transfer BCA 7, Mandiri 5, BNI 6, BRI 9; VA BCA 22, Mandiri 23, BNI 24, BRI 25 |
| Jam transfer bank | 05.00 sampai sebelum 21.00 WIB; divalidasi pada pemilihan dan sebelum submit. QRIS/VA tetap tersedia 24 jam |
| Detail QRIS/VA top up | Status, QR, polling, masa berlaku, unduh QR, nomor VA, salin nomor, dan bantuan pembayaran tersedia |
| Riwayat top up | Paging, pencarian, dan rentang tanggal dikirim memakai field kontrak legacy |
| Bonus | Daftar dan pembukaan bonus tersedia |
| Profil/bank dan referral | Baca/edit profil, bank, dan daftar referral tersedia; profil disegarkan setelah kembali dari edit |
| Pengaturan | Preferensi bid mengikuti rentang tetap legacy; connect/disconnect akun tersedia |
| Berita dan pembaruan versi | Daftar/detail berita, pemeriksaan versi minimum, dan update opsional tersedia |
| Error backend | Kode yang dikenal diberi pesan pengguna; kode tetap tersedia terpisah untuk aksi seperti membuka QRIS |

## Perbaikan teknis pendukung

- Memperbaiki overload `stopForeground` untuk API 23 dan lifecycle state marker Compose.
- Menyamakan label app menjadi `Oke Driver` dan versi menjadi `4.13.1 (4865)`.
- Release memerlukan signing config, Maps key, dan Directions key. Konfigurasi lokal memakai keystore legacy; tidak ada credential ditulis ke source control.
- DataStore sesi dan SharedPreferences dikecualikan dari cloud backup serta transfer perangkat.
- Rute order tidak memakai HTTP client driver untuk request Google, sehingga token driver tidak ikut dikirim ke layanan Directions.
- Menjaga registrasi FCM tetap mencoba ulang tanpa menyimpan token sesi mentah dalam input job WorkManager.

## Validasi

Validasi akhir menggunakan:

```text
.\gradlew.bat :app:testDebugUnitTest :app:assembleRelease :app:lintRelease --console=plain --max-workers=2
py scripts/check_native_alignment.py app/build/outputs/apk/release/app-release.apk
```

Hasil akhir:

| Pemeriksaan | Hasil |
|---|---|
| Unit test | **72 test / 25 suite lulus**, 0 failure dan 0 error |
| Release APK | **Berhasil**, `4.13.1 (4865)` |
| Release AAB | **Berhasil**, `:app:bundleRelease` termasuk signing dan lint vital |
| Release lint | **Lulus**, 0 error, 69 warning, 2 hint; tidak memakai baseline atau suppression untuk menutup tiga error awal |
| Signing APK | Sertifikat SHA-256 cocok dengan APK legacy lokal |
| Native 16 KB | Empat library 64-bit lolos pemeriksaan ELF; zipalign 16 KB exit code 0 |
| Whitespace | `git diff --check` lulus |
| Device | Tidak ada device terhubung; uji end-to-end belum dijalankan |

APK berada di `app/build/outputs/apk/release/app-release.apk`; AAB di `app/build/outputs/bundle/release/app-release.aab`. Laporan test ada di `app/build/reports/tests/testDebugUnitTest/index.html`; lint di `app/build/reports/lint-results-release.html`. Warning lint yang tersisa tetap perlu diperhatikan, tetapi bukan error yang menggagalkan validasi ini.

Regression test mencakup aturan consent/tracking, jenis item dan metadata order, cancel, QRIS finish/prepayment, klik ganda, jam transfer bank WIB, paging/filter top up, konversi saldo, pesan error, dan decoding polyline rusak.

## Batas cakupan dan verifikasi yang masih diperlukan

- **Belum ada uji perangkat/staging end-to-end.** Tidak ada transaksi order, pembayaran, penarikan, atau perubahan akun production yang dijalankan selama pekerjaan ini.
- Full-screen notification bergantung pada izin dan kebijakan Android. Jika izin tersebut tidak diberikan, app menggunakan notifikasi prioritas tinggi. Suara/channel tetap mengikuti pengaturan pengguna. Lihat [pembatasan full-screen intent Android](https://developer.android.com/about/versions/14/behavior-changes-14#secure-fsi-notifications).
- Reboot/background recovery tetap mengikuti izin lokasi Android; persetujuan aplikasi tidak menggantikan izin runtime. Lihat [ketentuan foreground service lokasi](https://developer.android.com/develop/background-work/services/fgs/service-types#location).
- Perlu uji in-place upgrade tanpa uninstall, consent ditolak/dicabut, sesi expired, Doze, layar terkunci, app dibunuh, OEM, jaringan buruk, dan pembayaran terlambat.
- Perlu verifikasi pembatasan Maps/Directions key dan OAuth signing SHA di Google Cloud/Play Console. Keberadaan key dan sertifikat lokal yang cocok belum membuktikan konfigurasi console benar.
- Minimum Android modern tetap API 23; legacy mendukung API 21. Android 5.0/5.1 tidak tercakup oleh pengganti ini.
- API/model lama yang tidak mempunyai alur aktif yang setara tidak dijadikan ukuran kelengkapan fitur: points, daftar massage khusus, katalog merchant pada fragment prototipe, dan form konfirmasi deposit manual lama. Top up aktif mengikuti `DepositNewActivity`; tombol batal deposit legacy yang tidak memanggil endpoint juga tidak ditiru.
- Tampilan Compose dipertahankan. Kesetaraan pixel, seluruh teks EN/ID, serta kinerja/baterai dan tingkat crash lapangan belum dinyatakan sama.

Dengan batas tersebut, fitur inti dan perbedaan yang ditemukan telah ditangani pada implementasi. Klaim kesetaraan 100% masih memerlukan hasil UAT dan pengujian perangkat.
