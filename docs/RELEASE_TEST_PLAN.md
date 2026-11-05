# Rencana uji rilis OkeDriver Compose: dari internal testing ke production

Disusun: 4 November 2026. Target: `okedriver-app` 4.13.x menggantikan `okedriver` (legacy) dengan `applicationId` yang sama.

Dokumen ini mengatur **tahapan, perangkat, kriteria lolos, dan fitur yang diuji pada tiap tahap**. Detail langkah per fitur untuk uji paritas ada di [TESTING_PLAN.md](../TESTING_PLAN.md); jangan diduplikasi di sini. Status kode saat ini ada di [FEATURE_PARITY_2026-10-28.md](FEATURE_PARITY_2026-10-28.md).

Aturan umum: item yang gagal ditulis di bawah item itu sebagai `> BUG: ...` beserta perangkat, versi Android, dan versi app. Item yang tidak diuji **tidak dicentang**.

---

## Alur tahapan

| Tahap | Siapa | Lingkungan | Durasi minimal | Lanjut jika |
|---|---|---|---|---|
| 0. Gerbang sebelum upload | Developer | Mesin build | Setiap build | Semua centang di §0 |
| 1. Internal testing | Tim internal, 3–5 perangkat | Staging / akun driver uji | 2–3 hari | Tidak ada bug P0/P1 terbuka, uji upgrade lulus (§2) |
| 2. Closed testing | 10–30 driver nyata yang bersedia | Production, order nyata | 1–2 minggu | Metrik §4 sehat, tidak ada order terlewat yang disebabkan app |
| 3. Production bertahap | Semua driver, bertahap | Production | 5% → 20% → 50% → 100% | Metrik §5 sehat di tiap langkah |

Jangan lompat tahap. Jika terjadi masalah di tahap mana pun, **hentikan rollout** (Play Console → Halt rollout) sebelum menyelidiki.

---

## 0. Gerbang sebelum upload (otomatis)

- [ ] `.\gradlew.bat :app:testDebugUnitTest :app:assembleRelease :app:lintRelease` hijau: 0 test gagal, 0 error lint
- [ ] `:app:bundleRelease` menghasilkan AAB **baru** (bukan AAB lama di folder output)
- [ ] `versionCode` lebih tinggi dari angka tertinggi di Play Console (App bundle explorer)
- [ ] Sertifikat upload cocok dengan yang terdaftar di Play (App signing)
- [ ] Perubahan sudah di-commit dan diberi tag, supaya build yang diuji bisa direproduksi
- [ ] Hasil **Pre-launch report** Play Console dibaca (crash, ANR, aksesibilitas di perangkat uji Google)

---

## 1. Persiapan uji

### Perangkat (minimal)
| Kebutuhan | Alasan |
|---|---|
| Android 6 (API 23) | `minSdk`; sebelumnya ada risiko crash `stopForeground` |
| Android 10 | Alur izin background lewat dialog sistem |
| Android 11–12 | Izin background lewat halaman pengaturan; batas foreground service |
| Android 13–14 | Izin notifikasi; full-screen intent perlu izin |
| Android 15/16 | Perilaku terbaru, 16 KB page size |
| 1 Xiaomi/Redmi, 1 Oppo/Vivo, 1 Samsung | Penghemat baterai dan autostart OEM memengaruhi tracking dan notifikasi |
| 1 layar kecil + font sistem terbesar | Tata letak, slider, dialog panjang |

### Akun dan data
- [ ] Minimal 3 akun driver uji: kota biasa, kota dengan antrean/absensi aktif, akun dengan flag OkeFast aktif
- [ ] Akun customer uji untuk membuat order (RIDE, kurir, belanja/food, minimal satu pembayaran QRIS)
- [ ] Saldo uji cukup untuk withdraw dan konversi
- [ ] **Backend staging bila ada.** Jika hanya production, transaksi uang (withdraw, top up, QRIS) memakai nominal terkecil dan akun uji milik tim, bukan driver nyata

### Perangkat pembanding
- [ ] Satu perangkat tetap memakai legacy sebagai pembanding perilaku (kecuali untuk uji upgrade, §2)

---

## 2. Uji upgrade in-place (wajib, paling berisiko)

Jalankan **tanpa uninstall**, memakai APK/AAB legacy yang benar-benar beredar di production.

- [ ] Pasang legacy dari Play, login, aktifkan Online, biarkan ada riwayat order
- [ ] Update ke build baru lewat internal testing
- [ ] Tetap login (tidak diminta login ulang)
- [ ] Status Online, saldo, dan order aktif (jika ada) terbawa
- [ ] Persetujuan lokasi lama terbaca (tidak diminta ulang bila versinya sama); jika dicabut/belum ada, disclosure tampil
- [ ] Suara notifikasi order sama seperti sebelumnya (channel lama dipakai ulang), tidak jadi senyap
- [ ] FCM token terdaftar ulang setelah update (order masuk setelah update)
- [ ] Update saat layanan Online sedang berjalan: layanan pulih dan lokasi tetap terkirim
- [ ] Kebalikannya tidak diharapkan: jangan uji downgrade, tidak didukung

---

## 3. Uji fitur (tahap internal; ulangi bagian yang berubah di tahap berikutnya)

Prioritas: **P0** risiko uang/order/akun, **P1** perilaku penting, **P2** pelengkap.

### 3.1 Login, sesi, persetujuan lokasi (P0)
- [ ] Login email benar/salah; akun belum aktif; akun suspend (pesan jelas, ada alasan bila server mengirim)
- [ ] Login Google: akun terdaftar, belum terdaftar, belum terverifikasi
- [ ] Tutup app lalu buka lagi: tetap login. Logout: tidak auto-login
- [ ] Sesi kedaluwarsa (token dibuat invalid dari backend): kembali ke Login, tidak crash
- [ ] Disclosure lokasi muncul sebelum akses; **Tolak** → tidak ada tracking, tidak ada izin diminta
- [ ] Pengaturan → matikan "Penggunaan lokasi": tracking berhenti, status tidak mengirim lokasi
- [ ] Teks disclosure tampil benar dalam bahasa Indonesia dan Inggris (ganti bahasa perangkat)

### 3.2 Online, tracking, layanan latar (P0)
- [ ] Online → notifikasi layanan tampil, lokasi terkirim ±30 detik (cek log server)
- [ ] Offline → layanan berhenti, notifikasi hilang, lokasi tidak terkirim
- [ ] GPS dimatikan saat Online: layanan berhenti rapi, ada indikasi ke driver
- [ ] Izin lokasi dicabut dari pengaturan sistem saat Online: layanan berhenti, tidak crash
- [ ] App di-swipe dari recent: layanan tetap berjalan (uji di Xiaomi, Oppo/Vivo, Samsung)
- [ ] Layar mati 30+ menit (Doze): lokasi tetap terkirim, order masih masuk
- [ ] Logout saat Online: layanan berhenti
- [ ] Android 6: Online → Offline → Logout tanpa crash
- [ ] **Izin background (disclosure baru):** Pengaturan → "Izin lokasi untuk pemulihan setelah restart" → dialog disclosure tampil penuh → Setuju → Android 10: dialog sistem; Android 11+: halaman pengaturan → pilih "Selalu izinkan". **Tolak** menutup dialog tanpa meminta izin
- [ ] Restart perangkat saat Online **dengan** izin background: layanan pulih
- [ ] Restart perangkat saat Online **tanpa** izin background: muncul notifikasi untuk membuka app, tidak crash

### 3.3 Order masuk (P0)
- [ ] Order masuk saat app foreground, background, app dimatikan, dan **layar terkunci**
- [ ] Notifikasi: suara berulang, getar, layar menyala di atas lock-screen, notifikasi hilang ±60 detik
- [ ] Android 14+: tanpa izin full-screen intent, tetap muncul sebagai notifikasi prioritas tinggi
- [ ] Ketuk notifikasi → layar order masuk terbuka (dari berbagai kondisi app)
- [ ] Setelah layar order selesai, layar terkunci/menyala normal lagi (tidak terus menyala)
- [ ] **Terima** → masuk order aktif. **Tolak** → ada konfirmasi, lalu order dibatalkan dengan alasan "ditolak"
- [ ] Tombol **Back** di order masuk memunculkan konfirmasi tolak, tidak bisa keluar begitu saja
- [ ] Order dibatalkan customer saat layar terbuka: pesan jelas, tidak nyangkut
- [ ] Bandingkan jeda kemunculan notifikasi dengan legacy pada order yang sama

### 3.4 Order berjalan (P0)
- [ ] **Slider Mulai:** geser penuh → status berubah; lepas sebelum 85% → kembali ke awal, tidak ada request
- [ ] **Slider Selesaikan:** sama; **tap biasa tidak memicu aksi**
- [ ] Slider tidak aktif saat request berjalan; tidak bisa dipicu dua kali
- [ ] Slider dapat dioperasikan dengan TalkBack (aktifkan dua ketukan)
- [ ] Layar kecil dan font besar: slider tetap terbaca dan bisa digeser
- [ ] Batalkan: hanya aktif saat PENDING; alasan terkirim dengan kode yang benar
- [ ] Error server tampil sebagai pesan yang dapat dibaca (departure/arrival delayed, progress delayed)
- [ ] Peta: marker tujuan, polyline rute, navigasi eksternal terbuka; fallback bila Maps tidak ada
- [ ] Telepon customer, pengirim, penerima, merchant
- [ ] Chat: kirim/terima, notifikasi chat, `no_access`
- [ ] Edit belanja: tambah/ubah/hapus, jenis item (barang/parkir/tol) bertahan setelah simpan ulang, order kosong, batas edit
- [ ] Detail: kode verifikasi Express, paket, durasi/overtime, rincian biaya, diskon
- [ ] Refresh otomatis 15 detik; kembali dari background memuat ulang
- [ ] Selesai → riwayat dan saldo terbarui

### 3.5 Pembayaran order (P0)
- [ ] Order tunai: selesai normal, saldo/komisi sesuai
- [ ] Order QRIS: tombol QRIS tampil; jarak jauh (>500 m) meminta konfirmasi
- [ ] QRIS dibayar sebelum selesai, dibayar saat selesai, dibayar terlambat
- [ ] Ganti ke tunai dengan konfirmasi
- [ ] `payment_failed` membuka QRIS; polling berhenti saat layar tidak terlihat
- [ ] Jaringan putus saat polling: pulih tanpa bayar ganda atau status salah

### 3.6 Home, absensi, antrean, OkeFast (P1)
- [ ] Saldo, MOTD, status order aktif tampil; kartu order aktif membuka order
- [ ] Absensi: lokasi terbaru diperiksa; kode error absensi (di luar area, di luar jam, sudah absen) jelas
- [ ] Antrean: pilihan **Bandara Soetta** dan **Pasar Senen** terkirim persis itu (cek payload); saldo kurang dan di luar area punya pesan
- [ ] OkeFast: menu hanya untuk akun dengan flag aktif; generate kode berhasil
- [ ] Update app: opsional dan wajib (versi minimum) tampil sesuai

### 3.7 Dompet dan uang (P0)
- [ ] Saldo biasa dan saldo penarikan sama dengan legacy
- [ ] Withdraw: batas maksimum, permintaan pending menolak yang baru, klik ganda tidak mengirim dua kali
- [ ] Konversi saldo: batas minimum, hasil benar
- [ ] Top up QRIS (metode 16): QR tampil, hitung mundur, polling status, unduh QR, kedaluwarsa
- [ ] Top up transfer bank BCA/Mandiri/BNI/BRI: **hanya 05.00–<21.00 WIB** (uji sebelum dan sesudah jam, dan saat submit tepat di batas)
- [ ] Top up VA BCA/Mandiri/BNI/BRI: tersedia 24 jam, nomor VA bisa disalin
- [ ] Riwayat top up: paging, pencarian, rentang tanggal
- [ ] Riwayat transaksi dan penarikan: paging, nominal sama dengan legacy
- [ ] Bonus box: daftar, buka, sudah dibuka, tidak ditemukan

### 3.8 Profil, pengaturan, lainnya (P1–P2)
- [ ] Profil dan referral tampil; edit profil dan bank tersimpan, profil terbarui setelah kembali
- [ ] Nama bank: pastikan format yang tersimpan dapat diproses penarikan (di app baru berupa teks bebas)
- [ ] Preferensi order (bid): rentang tetap legacy, tersimpan
- [ ] Akun terhubung: hubungkan dan putus
- [ ] Berita: daftar, detail, buka tautan
- [ ] Riwayat order: tab aktif/selesai, paging, detail lengkap (skor, review, pembatalan)
- [ ] Order terbaru di peta

### 3.9 Ketahanan (P1)
- [ ] Mode pesawat dan jaringan lemah di tiap layar utama: pesan wajar, bisa dicoba lagi
- [ ] Putar layar dan proses dimatikan sistem (Developer options → Don't keep activities): state pulih
- [ ] Penyimpanan penuh, mode hemat baterai, mode hemat data
- [ ] Bahasa perangkat Indonesia dan Inggris
- [ ] Mode gelap

---

## 4. Closed testing: ukur di lapangan

Cakup 10–30 driver nyata dari kota dan jenis HP yang beragam, termasuk HP kelas bawah dan OEM agresif. Beri tahu mereka cara melapor dan siapkan jalur cepat (grup khusus).

- [ ] Crash-free users ≥ 99%, tanpa ANR berulang (Play Console → Android vitals)
- [ ] Tidak ada laporan order masuk yang tidak berbunyi/terlewat akibat app
- [ ] Jumlah driver yang tiba-tiba Offline (lokasi berhenti terkirim) tidak lebih tinggi dari baseline legacy; bandingkan di backend
- [ ] Tidak ada selisih saldo atau transaksi ganda
- [ ] Tingkat keberhasilan pembayaran QRIS dan top up sama atau lebih baik dari legacy
- [ ] Setiap bug P0/P1 dari lapangan sudah diperbaiki dan diverifikasi ulang pada build berikutnya

Kumpulkan juga umpan balik tentang slider (apakah mudah dipakai dengan sarung tangan atau saat berkendara di lokasi aman) dan tentang teks yang hanya berbahasa Indonesia.

---

## 5. Production bertahap

Mulai 5%, naik ke 20%, 50%, lalu 100%. Tunggu minimal 24–48 jam per langkah dan setidaknya satu jam sibuk (pagi dan sore).

Pantau di tiap langkah:
- [ ] Crash-free users dan ANR (Android vitals), serta crash per versi Android/perangkat
- [ ] Rasio order diterima/ditolak/timeout dibanding sebelum rollout
- [ ] Jumlah driver Online dan frekuensi update lokasi di backend
- [ ] Laporan masalah dari driver dan tim lapangan
- [ ] Error rate API (khususnya `update-status`, `current-order`, `topup`, `withdraw`)
- [ ] Ulasan Play Store

**Hentikan rollout** bila salah satu terjadi: crash-free < 98%, order terlewat yang terbukti karena app, selisih saldo, atau lonjakan driver Offline. Siapkan rencana pemulihan: build perbaikan dengan `versionCode` lebih tinggi; downgrade ke legacy tidak mungkin karena `versionCode` tidak bisa turun.

---

## 6. Perbedaan yang diketahui (jangan dilaporkan sebagai bug baru)

Lihat [FEATURE_PARITY_2026-10-28.md](FEATURE_PARITY_2026-10-28.md) untuk batas cakupan. Perbedaan yang sengaja atau belum dikerjakan per audit 2 November:
- Home tidak otomatis membuka order aktif (ada kartu); tidak ada cooldown MOTD, heatmap, dan menu tutorial/bantuan
- Teks hanya Indonesia (kecuali disclosure lokasi); label layanan dan teks dialog cancel berbeda dari legacy, tanpa peringatan penalti
- Nama bank berupa teks bebas, bukan pilihan
- Tidak ada salin/bagikan tautan referral, balasan cepat chat, atau pilihan "Dari merchant" saat edit belanja
- `minSdk` 23 (legacy 21): driver Android 5.x tidak mendapat update

Bila salah satu dianggap wajib untuk rilis, kerjakan sebelum closed testing.

---

## Yang belum bisa diverifikasi dari kode

Perilaku di perangkat nyata (Doze, OEM, lock-screen saat app mati), respons backend, pembatasan Maps/Directions key di Google Cloud, Play App Signing, dan hasil review izin background location. Semua hanya bisa dibuktikan lewat tahap di atas.
