# Deklarasi `ACCESS_BACKGROUND_LOCATION` untuk Google Play

Disiapkan: 2 November 2026. Aplikasi: Oke Driver (`id.okejack.okedriver`) 4.13.1 (4865).

Dokumen ini adalah bahan untuk diisi di **Play Console → App content → Sensitive app permissions → Location permissions**. Pengajuan harus dilakukan manusia pemilik akun Play Console; tidak ada yang diajukan oleh dokumen ini. Teks formulir Play berbahasa Inggris, jadi jawaban di bawah ditulis dalam bahasa Inggris.

## Status yang perlu dipahami dulu

- Legacy (`okedriver`) **tidak** mendeklarasikan izin ini. Versi baru mendeklarasikannya, sehingga update akan masuk review izin sensitif. Review dapat menolak atau menunda seluruh update, bukan hanya fitur ini.
- Satu-satunya fungsi izin ini di aplikasi: memulihkan layanan lokasi status **Online** setelah perangkat restart ([BootCompletedReceiver.kt](../app/src/main/java/id/okejack/okedriver/core/service/BootCompletedReceiver.kt)). Pelacakan saat aplikasi dipakai dan saat layanan foreground berjalan tidak membutuhkannya.
- Izin bersifat **opsional** untuk pengguna. Tanpa izin, driver tetap dapat Online dan menerima order; setelah restart mereka mendapat notifikasi untuk membuka aplikasi.
- Risiko review: reviewer dapat menilai pemulihan setelah restart sebagai kenyamanan, bukan fungsi inti. Jika ditolak, jalan keluarnya adalah menghapus `ACCESS_BACKGROUND_LOCATION` dari [AndroidManifest.xml](../app/src/main/AndroidManifest.xml) dan baris pengaturannya di [SettingsScreen.kt](../app/src/main/java/id/okejack/okedriver/feature/settings/presentation/SettingsScreen.kt). Perilaku restart lalu menjadi hanya notifikasi; tidak ada kode lain yang bergantung pada izin ini.

## Prominent disclosure di aplikasi (sudah diimplementasikan)

Alur: **Pengaturan → "Izin lokasi untuk pemulihan setelah restart"** → (jika belum menyetujui pengumpulan lokasi, dialog persetujuan umum tampil lebih dulu) → dialog disclosure khusus → tombol **Setuju** → Android 10: dialog izin sistem; Android 11+: halaman pengaturan aplikasi tempat driver memilih "Selalu izinkan". Tombol **Tolak** menutup dialog tanpa meminta izin.

Teks dialog ada di `res/values/location_consent.xml` (EN) dan `res/values-in/location_consent.xml` (ID), kunci `background_location_disclosure_title` dan `background_location_disclosure`. Disclosure menyebut: data yang diakses (lokasi), bahwa akses terjadi saat aplikasi ditutup atau tidak digunakan, tujuan (melanjutkan Online setelah restart, mencocokkan order terdekat, memantau perjalanan), kapan data dikirim (hanya saat Online), bahwa izin opsional, dan cara mencabutnya.

## Jawaban formulir

**Which feature in your app requires background location access?**

> The "Online" status of the driver app. When a driver turns Online, the app starts a foreground location service (with a persistent notification) that shares the driver's position so the platform can assign nearby orders. Background location lets this Online session resume automatically after the device restarts, so a driver is not silently offline and missing orders after a reboot.

**Why does this feature require background access, and why can't it work in the foreground?**

> After a device restart no app screen is visible, and Android (12+) does not allow a location foreground service to access location when started from the background unless the app holds background location access. Without it the driver would appear Online in the app but send no location until they manually reopen the app.

**Is the permission optional?**

> Yes. Drivers can go Online and receive orders without granting it. After a restart without the permission, we show a notification asking the driver to open the app to resume. The permission is requested only from Settings, after a dedicated in-app disclosure, never at first launch.

**Data use**

> Location is collected only while the driver status is Online (and during attendance or queue registration for location verification). It is sent to our servers to match the driver with nearby orders and monitor order progress, and shared with the customer during an active order. Tracking stops when the driver goes Offline, logs out, or revokes consent.

## Video untuk review (maks. ±30 detik, tanpa suara tidak masalah)

Rekam di perangkat Android 11+ dengan build release terbaru:

1. Buka aplikasi dan login sebagai driver uji.
2. Buka **Pengaturan** (di drawer Home).
3. Ketuk **Izin lokasi untuk pemulihan setelah restart**.
4. Tunjukkan dialog disclosure sampai habis (gulir bila perlu), lalu ketuk **Setuju**.
5. Tunjukkan halaman izin aplikasi dan pilih **Selalu izinkan**.
6. Kembali ke aplikasi, tunjukkan status **Online** dengan notifikasi layanan lokasi tampil.
7. Opsional: tunjukkan restart perangkat dan layanan Online pulih.

Unggah ke YouTube (tidak publik/unlisted) dan tempel tautannya di formulir.

## Checklist sebelum mengajukan

- [ ] Kebijakan privasi di Play Console memuat pengumpulan lokasi termasuk saat aplikasi tidak digunakan, tujuan, dan pembagian ke pelanggan.
- [ ] Formulir **Data safety**: lokasi (perkiraan dan tepat) ditandai dikumpulkan dan dibagikan (ke pelanggan saat order), dengan tujuan fungsionalitas aplikasi.
- [ ] Deklarasi **foreground service**: tipe `location`. Manifest juga mendeklarasikan `FOREGROUND_SERVICE_SPECIAL_USE` padahal tidak ada service bertipe itu (legacy sama). Izin yang tidak dipakai dapat memicu pertanyaan tambahan; pertimbangkan menghapusnya.
- [ ] Video sudah diunggah dan tautannya benar.
- [ ] Build yang diunggah adalah AAB release 4865 dan disclosure sudah diverifikasi muncul di perangkat nyata (belum diuji di perangkat).
