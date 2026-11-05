# OkeDriver Testing Plan
## Parity Testing: okedriver-app (Compose) vs okedriver (legacy)

> Dokumen ini adalah checklist untuk memverifikasi bahwa `okedriver-app` (Compose) berjalan 100%
> dan **berperilaku sama** dengan `okedriver` (legacy, Activity/XML) yang sudah dipakai driver
> sekarang. Berdasarkan inventaris fitur di [MIGRATION_PLAN.md](MIGRATION_PLAN.md), diurutkan dari
> risiko paling tinggi ke paling rendah.
>
> **Cara pakai:** centang `[x]` tiap item yang sudah dites dan hasilnya sesuai. Kalau ada yang beda
> dari app lama atau nemuin bug, JANGAN dicentang — tulis di kolom catatan di bawah item itu
> (format: `> BUG: ...`) supaya gampang di-grep nanti.

---

## 0. Persiapan Sebelum Mulai Testing

- [ ] Siapkan **2 device/emulator terpisah** — satu install `okedriver` (legacy), satu `okedriver-app` (Compose). Kalau cuma ada 1 device, install 2 app ini sekaligus (application ID beda) dan buka bergantian.
- [ ] Login dengan **akun driver yang sama** di kedua app, idealnya akun yang sudah punya data representatif: riwayat order, saldo > 0, riwayat transaksi, ada referral, dll — supaya kelihatan bedanya kalau ada data yang salah tampil.
- [ ] Kalau tersedia **backend staging**, pakai itu untuk testing yang sifatnya destruktif (accept/cancel order, withdraw, topup) supaya nggak ganggu data production/akun driver asli. Kalau cuma ada production, tandai test seperti itu dengan hati-hati dan idealnya lakukan di jam sepi / pakai akun driver dummy.
- [ ] Nyalakan logging/Logcat filter untuk package `id.okejack.okedriver` di kedua app supaya gampang lihat error/exception yang nggak kelihatan di UI.
- [ ] Siapkan tempat catat bug (bisa langsung di file ini di bawah tiap item, atau bikin GitHub issue per bug kalau mau di-assign/dilacak terpisah).
- [ ] Kalau memungkinkan, uji di **lebih dari 1 merk HP** (minimal 1 Xiaomi/Oppo/Vivo selain Samsung/Pixel) — khusus untuk bagian location tracking & notifikasi, karena battery optimization tiap OEM beda dan ini risiko yang sudah diketahui (lihat §6 MIGRATION_PLAN.md).

---

## P0 — Kritis: Auth & Session

> Kalau ini bocor, semua fitur lain nggak kepakai. Tes ini duluan.

- [ ] Login dengan email/password valid → masuk ke Home, data driver (nama, foto, saldo) tampil benar
- [ ] Login dengan password salah → pesan error muncul, tidak crash
- [ ] Login dengan akun yang di-suspend/nonaktif (kalau ada test case-nya) → pesan sesuai, bukan crash generik
- [ ] Tutup app tanpa logout, buka lagi → tetap login (session persist), langsung ke Home tanpa harus login ulang
- [ ] Logout → kembali ke Login screen, buka app lagi tidak auto-login
- [ ] Token expired (kalau bisa disimulasikan / tunggu sampai expired) → auto redirect ke Login, bukan stuck/crash
- [ ] FCM token ke-register setelah login (bisa dicek dari sisi backend/log kalau ada akses) — penting supaya push notification order masuk

---

## P0 — Kritis: Order Flow (Real-Time)

> Paling kritis karena ini sumber penghasilan driver. Uji end-to-end, termasuk kondisi app di background / HP terkunci.

- [ ] **Incoming order**: order baru masuk → notifikasi FCM muncul + suara/getar sama seperti app lama, dengan app dalam kondisi: (a) foreground, (b) background, (c) HP terkunci/layar mati
- [ ] Waktu respons notifikasi masuk — bandingkan jeda antara order masuk di app lama vs app baru (harus nggak lebih lambat)
- [ ] **Accept order** → masuk ke Current Order screen, detail order (alamat, harga, customer) sesuai dengan yang di app lama
- [ ] **Decline/timeout order** → order hilang dari layar, tidak nyangkut
- [ ] **Update status order** (picked up → on the way → delivered/selesai) tiap tahap → status ke-update di backend, tercermin juga kalau dicek dari app lama/dashboard admin (kalau ada aksesnya)
- [ ] **Chat dengan customer** → kirim & terima pesan, riwayat chat persist kalau keluar-masuk screen
- [ ] **Edit shopping items** (untuk order OkeShop/OkeFood) → tambah/kurang/edit item, total harga ke-update benar
- [ ] **Order payment confirmation** → nominal & metode pembayaran sesuai
- [ ] **Cancel order** → alasan cancel tersimpan, order pindah status dengan benar, saldo/penalti (kalau ada aturan) sesuai app lama
- [ ] **Order history** (tab aktif & riwayat) → data & paginasi sama dengan app lama
- [ ] **Order detail view** dari history → semua field lengkap dan sama dengan app lama
- [ ] **Recent order locations** (peta) → marker & rute sesuai
- [ ] **OkeFast generate code** → kode ter-generate dan valid dipakai

---

## P0 — Kritis: Location Tracking & Foreground Service

> Cuma bisa dites di device fisik, tidak bisa lewat automated test.

- [ ] Toggle online → foreground service jalan (ada notifikasi persisten sesuai kebijakan Android), lokasi ke-update ke server secara periodik — bandingkan interval/akurasi dengan app lama
- [ ] Toggle offline → service berhenti, driver hilang dari radar order baru
- [ ] App di-*kill* dari recent apps saat online → service tetap jalan (atau restart otomatis) sama seperti app lama, tidak diam-diam berhenti tracking
- [ ] HP di-restart saat driver online → service auto-start lagi (uji `BootCompletedReceiver`) — cek apakah driver perlu login ulang atau langsung online lagi seperti app lama
- [ ] Battery optimization / app di-*background restrict* oleh sistem (khususnya di Xiaomi/Oppo/Vivo) → cek apakah tracking & notifikasi order tetap jalan, bandingkan dengan app lama di HP yang sama
- [ ] Queue number (nomor antrian) tampil & ke-update benar di Home

---

## P1 — Penting: Balance & Wallet

> Ini duit asli driver — paling nggak boleh salah hitung atau salah tampil.

- [ ] Saldo (balance) & saldo yang bisa ditarik (withdrawable) — angka **persis sama** dengan app lama
- [ ] Riwayat transaksi — daftar, jumlah, tanggal, dan detail tiap transaksi sama dengan app lama
- [ ] **Top up**: pilih nominal → pilih metode/bank → dapat VA number → detail top up tampil benar
- [ ] Top up history — daftar & status (pending/success/failed) sesuai
- [ ] **Withdraw**: request withdraw, saldo terpotong dengan benar, muncul di riwayat withdraw
- [ ] Withdraw history — status dan nominal sesuai
- [ ] **Bonus box**: daftar bonus, buka bonus, saldo bertambah sesuai
- [ ] Payment methods list — daftar metode & logo/nama sama dengan app lama
- [ ] ⚠️ Alur **VA Bank transfer** (legacy `VaBankActivity`/`VaResultBankTransfer`) — cek dulu apakah ini flow yang masih dipakai backend atau sudah lama nggak aktif; kalau masih dipakai, cek juga apakah sudah ke-cover di app baru atau memang ada gap

---

## P2 — Profile, Settings, News, Misc

- [ ] Profile view — foto, nama, nomor HP, kendaraan, ringkasan bank sesuai
- [ ] Edit profile — ganti password & info bank tersimpan dengan benar
- [ ] Referral — kode referral tampil, daftar driver yang direferensikan sesuai
- [ ] Settings screen — semua opsi ada & berfungsi
- [ ] Driver bid settings — submit bid tersimpan
- [ ] Connect/disconnect akun — behavior sesuai app lama
- [ ] Airport attendance — absen & status antrian bandara sesuai
- [ ] News list — daftar & paginasi sesuai
- [ ] News detail — konten tampil lengkap (termasuk gambar/HTML kalau ada)
- [ ] ⚠️ Software update notice dialog — **belum ada implementasinya di app baru** (lihat MIGRATION_PLAN.md §Fase 6), jadi ini akan gagal sampai dibangun. Catat sebagai known gap, bukan bug baru.

---

## P3 — Hal-hal yang Memang Belum Dikerjakan (Jangan Dilaporkan sebagai Bug)

Berdasarkan MIGRATION_PLAN.md §Fase 7, hal-hal berikut **memang belum diimplementasikan**, jadi kalau
ketemu saat testing, catat sebagai "known gap" bukan bug baru:

- Loading/shimmer state yang konsisten di semua list screen
- Error state & tombol retry yang seragam
- Empty state illustration
- Pull-to-refresh di semua list
- Mode offline / caching saat nggak ada internet
- Deep link dari notifikasi ke screen tertentu (selain incoming order)
- Analytics & Crashlytics
- Dark mode (kalaupun tema-nya ada, belum tentu semua screen sudah dites di dark mode)

---

## Cara Melaporkan Bug yang Ditemukan

Untuk tiap bug, tulis langsung di bawah item checklist terkait dengan format:

```
> BUG: [deskripsi singkat apa yang beda/salah]
> Expected (app lama): ...
> Actual (app baru): ...
> Device/OS: ...
> Repro steps: ...
```

Kalau bug-nya cukup besar/butuh di-assign ke orang lain, angkat jadi GitHub issue terpisah dan taruh
link issue-nya di sini.

---

*Dokumen ini dibuat 14 Sep 2026, berdasarkan audit fitur di [MIGRATION_PLAN.md](MIGRATION_PLAN.md).
Update checklist ini seiring fitur baru selesai atau prioritas berubah.*
