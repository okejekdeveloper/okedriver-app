# Dua tampilan Oke Driver

Satu aplikasi memiliki dua pilihan UI. Pilihan awal adalah **UI Dinamis**, sesuai arah desain yang diinginkan. **UI Sederhana** mempertahankan susunan bersih dari revisi sebelumnya. Pilihan dapat diganti melalui **Akun → Tampilan aplikasi**, **Pengaturan → Tampilan aplikasi**, atau **Pilih tampilan aplikasi** pada layar masuk.

Pilihan tersimpan per perangkat dan tetap tersimpan setelah keluar dari akun. Pergantian tampilan memperbarui komponen Compose tanpa membuat ulang Activity atau mengganti NavController.

| Bagian | UI Sederhana | UI Dinamis |
| --- | --- | --- |
| Beranda | Kartu berbingkai, status kerja langsung, daftar menu | Hero ungu bergradasi, kartu saldo, menu bergambar yang merespons sentuhan |
| Saldo | Pemisahan saldo dan uang yang bisa ditarik | Ringkasan saldo bergradasi dan pintasan berwarna |
| Navigasi | Bilah datar dengan empat menu | Bilah mengambang dengan animasi pilihan dan ikon |
| Akun | Identitas dan rekening dalam kartu biasa | Header identitas berwarna dengan foto dan status akun sebenarnya |
| Order | Ringkasan netral | Ringkasan layanan, tarif, dan alamat dalam hero berwarna |
| Interaksi | Ripple standar Android | Ripple, respons skala sentuhan, dan transisi pilihan singkat |

Teks, ukuran target sentuh, konfirmasi offline/antrean/penarikan, pemblokiran klik saat diproses, serta tindakan order yang tetap terlihat dipakai oleh kedua tampilan. Tidak ada animasi berulang yang terus berjalan pada beranda.

## Struktur kode

- `core/designsystem/theme/simple/` dan `theme/dynamic/`: palet terang/gelap yang terpisah.
- `feature/home/presentation/simple/` dan `dynamic/`: dua susunan beranda yang terpisah.
- `feature/wallet/presentation/simple/` dan `dynamic/`: dua susunan saldo yang terpisah.
- `app/navigation/simple/` dan `dynamic/`: dua komponen navigasi yang terpisah.
- `feature/profile/presentation/dynamic/`, `feature/order/presentation/dynamic/`, dan `core/designsystem/component/dynamic/`: komponen presentasi Dinamis.
- `AppearancePreferences`: pilihan UI per perangkat, terpisah dari penyimpanan sesi.
- `DriverAppearanceHost`: menerapkan pilihan ke seluruh aplikasi.

ViewModel, repository, model order, kontrak API, izin lokasi, notifikasi, dan aturan pembayaran tetap menggunakan implementasi bersama. Komponen detail order, formulir, dan halaman sekunder memakai struktur fitur yang sama dengan palet serta komponen sesuai pilihan UI. Direktori `ui/` yang berisi prototipe lama tidak diaktifkan sebagai alur produksi karena masih memiliki state contoh; nuansa visualnya diterapkan pada komponen yang terhubung ke data sebenarnya.

## Pemeriksaan

Pengujian meliputi kedua pilihan tampilan pada lebar 320 dp dan huruf 160%, tindakan order yang tetap terlihat setelah menggulir, pemblokiran tindakan ganda, pergantian UI dari menu Akun, status driver yang dipertahankan saat berganti UI, serta preferensi yang tetap tersimpan setelah sesi dihapus.

Pratinjau debug menerima `--es ui_style simple` atau `--es ui_style dynamic`. Skrip tangkapan layar menerima `--ui-style simple|dynamic`; pilihan bawaan skrip tetap `simple` agar galeri sebelumnya dapat direproduksi.

### Hasil verifikasi 2 November 2026

- Build debug, APK pengujian, APK release, dan AAB release berhasil.
- **76 unit test**, 27 suite, tanpa kegagalan atau tes yang dilewati.
- **14 tes instrumentasi** lulus pada emulator Android 15: enam skenario untuk masing-masing UI, satu tes penyimpanan preferensi, dan satu tes pergantian kedua UI sambil mempertahankan status driver.
- Lint release: **0 error, 63 warning, 2 hint**. Warning tambahan pada penyimpanan preferensi merupakan saran memakai ekstensi KTX `SharedPreferences.edit`.
- Manifest release tidak menyertakan `DriverUiReviewActivity`. Fixture dan pintasan review hanya berada dalam build debug.
- Release tetap memakai application ID `id.okejack.okedriver`, versi **4.13.1 (4865)**. Verifikasi tanda tangan APK berhasil dengan SHA-256 sertifikat `895bb014f520705a5a58805fbb2bdc994f13dbcd8e5883221591ef22d7383b6a`.

Dua percobaan instrumentasi pertama berhenti sebelum tes dimulai akibat batas waktu startup proses saat emulator baru selesai boot dan masih melakukan pekerjaan awal. Setelah daemon Gradle dihentikan, jaringan emulator dimatikan, dan paket debug dikompilasi lebih dahulu, seluruh 14 tes lulus dalam satu eksekusi. Tidak ada perubahan kode aplikasi di antara percobaan tersebut. Hasil ini memverifikasi presentasi dan interaksi lokal; pengujian operasional dengan akun driver dan backend tetap mengikuti rencana pengujian produksi.

Lihat [galeri perbandingan kedua tampilan](ui-variants/2026-11-02/README.md) dan [log instrumentasi lengkap](ui-variants/2026-11-02/instrumentation-tests.txt).

Artefak build tersedia di `app/build/outputs/apk/release/app-release.apk` dan `app/build/outputs/bundle/release/app-release.aab`.

| Artefak | SHA-256 |
| --- | --- |
| APK | `d13b367eae9207e379f84fdb5ce60afc40c8f4571efad569af57758fd703149d` |
| AAB | `35ab3e3f823ab9835c34a953c0907fc6cfc043bbf6c18dfc704e83ed983f123c` |
