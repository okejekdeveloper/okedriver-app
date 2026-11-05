# Pembaruan UI dan UX Oke Driver — 2 November 2026

> Pembaruan lanjutan: aplikasi kini memiliki pilihan **UI Sederhana** dan **UI Dinamis**. Lihat [struktur dan validasi kedua UI](UI_VARIANTS.md) serta [galeri perbandingan](ui-variants/2026-11-02/README.md). Audit di bawah mencatat dasar revisi Sederhana.

Perubahan diarahkan untuk mitra yang kurang terbiasa memakai aplikasi: mudah menemukan tindakan berikutnya, membaca alamat dan nominal uang, serta kembali ke menu utama. Aturan bisnis dan kontrak pembayaran tetap menjadi acuan audit fitur sebelumnya.

## Referensi dan batas audit

Audit pembanding memakai panduan publik resmi, bukan pengujian akun atau seluruh versi aplikasi kompetitor. Materi lama tidak dianggap sebagai bukti bahwa tampilan kompetitor pada November 2026 masih sama. Aset dan identitas visual kompetitor tidak disalin.

| Referensi | Pola yang dipelajari | Penyesuaian Oke Driver |
| --- | --- | --- |
| [Panduan mitra Gojek, 13 Februari 2025](https://www.gojek.com/blog/gojek/panduan-mitra-baru) | Tahap online, jemput, pengantaran, dan tindakan yang mengikuti pekerjaan | Status kerja jelas, alamat tujuan kerja menonjol, petunjuk sebelum mulai/selesai |
| [Panduan menu Akun Grab Driver, 8 Mei 2023](https://www.grab.com/id/en/blog/driver/grab-indonesia-tampilan-baru-menu-akun-di-aplikasi-grab-driver/) | Pengelompokan fungsi akun dan jalur menemukan pengaturan | Akun berisi profil/rekening, panduan, pengaturan, info kantor, referral, dan akun pelanggan |
| [Grab Indonesia untuk mitra pengantaran](https://www.grab.com/id/driver/deliver/) | Akses ke pekerjaan, rute, dan penghasilan | Saldo dan penarikan memiliki bagian terpisah; total pembayaran pelanggan tidak disebut pendapatan bersih |
| [Panduan aplikasi Uber Driver](https://help.uber.com/en/driving-and-delivering/article/5---learn-how-to-use-the-driver-app?nodeId=d540b569-0e2c-4e39-b7ca-f2c2b9a81c8e) | Mulai online yang mudah ditemukan, navigasi perjalanan, bantuan dasar | Tombol mulai bekerja berlabel jelas, petunjuk jalan, dan panduan singkat di dalam aplikasi |
| [Aksesibilitas bawaan Compose](https://developer.android.com/develop/ui/compose/accessibility/api-defaults) | Target sentuh dan semantik aksesibilitas | Kontrol utama minimal 48–56 dp, status proses, label teks, dan aktivasi pembaca layar pada tombol geser |

## Temuan dan perbaikan

| Sebelumnya | Implementasi baru |
| --- | --- |
| Menu harian tersembunyi di drawer; pintasan peta berupa ikon | Navigasi Beranda, Saldo, Riwayat, Akun selalu terlihat pada halaman utama |
| Peta, panel mengambang, dan peringatan saling berebut ruang | Beranda memakai alur baca vertikal; peta memiliki bagian tersendiri dan tata letak dua kolom pada lebar ≥840 dp |
| Teks kecil dan tombol dengan tinggi tetap | Judul utama diperbesar; teks utama 14–18 sp, label pendukung minimal 12 sp; tombol utama tumbuh mengikuti isi |
| Ungu terang, gradasi, panel kaca, dan dekorasi berlebihan | Warna merek lama #3C2782, permukaan netral, garis batas, dan bentuk tombol yang konsisten |
| Ikon launcher bawaan Android Studio | Logo helm Oke Driver dari aplikasi lama, termasuk layer adaptive icon dan fallback Android 6–7 |
| “Go Online / Go Offline” | “Mulai terima order / Berhenti menerima order”, dengan konfirmasi berhenti |
| Absensi dan antrean tersembunyi di panel detail | Bagian absensi/antrean mengikuti flag akun; pilihan lokasi antrean dikonfirmasi sebelum dikirim |
| Tombol menerima order berada setelah detail yang panjang | Tombol terima/tolak tetap di bawah layar selama detail digulir |
| Memulai/menyelesaikan order harus dicari di bawah konten | Tindakan perjalanan tetap di bawah layar, disertai instruksi sesuai status; perlindungan tombol geser dipertahankan |
| Nama pelanggan dan dua tombol berdesakan dalam satu baris | Identitas pelanggan di atas, tombol Telepon dan Pesan pada baris tersendiri |
| Semua pesan harus diketik | Pesan cepat masuk ke kotak draf, kemudian driver memilih Kirim; tidak langsung mengirim saat dipilih |
| Nominal kosong dapat terlihat seperti saldo Rp0 | Saldo yang belum dimuat ditandai “Belum tersedia” |
| Uang operasional dan uang yang dapat ditarik tampil kurang jelas | Saldo operasional dan saldo yang dapat ditarik memiliki penjelasan dan tindakan masing-masing |
| Tindakan penarikan/konversi langsung mengirim permintaan | Konfirmasi menyebut nominal dan tujuan sebelum permintaan dikirim |
| “Akun terverifikasi” ditampilkan tanpa atribut verifikasi | Klaim dihapus; informasi profil memakai data yang tersedia |
| Tombol bantuan tidak menjalankan apa pun | Panduan lokal yang dapat dibaca dan pengaturan aplikasi yang dapat dibuka |
| Keluar dari akun langsung berjalan | Konfirmasi keluar dan pembersihan state tab yang disimpan untuk sesi lama |
| Tab riwayat memakai state lokal terpisah dari filter permintaan | Pemilihan tab mengikuti filter di ViewModel agar konsisten saat halaman dipulihkan |

## Cakupan

Halaman yang diubah langsung: beranda, navigasi utama, order masuk, order berjalan, isi/detail order bersama, riwayat, pesan, saldo, isi saldo, penarikan/konversi, akun, login, dan bagian pengaturan yang perlu digulir. Tema, tipografi, tombol, serta top bar bersama juga diperbarui.

Halaman sekunder lain masih memakai susunan sebelumnya dengan tema bersama. Ini bukan klaim bahwa seluruh layar sudah diaudit secara visual pada semua perangkat. Peta Google, keyboard, izin sistem, kamera/absensi, alur QRIS, dan integrasi backend tetap memerlukan pemeriksaan perangkat nyata.

## Validasi

Data di `DriverUiReviewActivity` sepenuhnya simulasi dan hanya tersedia pada build debug; activity tersebut tidak menginisialisasi ViewModel atau repository. Pengujian emulator ditujukan khusus ke `emulator-5580`, bukan ke ponsel yang terhubung. Activity pratinjau dipastikan tidak terdaftar di manifest rilis.

Pemeriksaan visual awal pada lebar 320 dp dan huruf 160% menemukan label navigasi yang pecah serta judul order yang mendesak tombol refresh. Label navigasi, judul, dan instruksi tombol geser diperbaiki. Tes navigasi juga memeriksa hasil layout teks agar setiap label tetap satu baris pada pengaturan tersebut.

Hasil akhir pemeriksaan kode dan build:

| Pemeriksaan | Hasil |
| --- | --- |
| Unit test debug | 74 tes, 26 suite, 0 kegagalan/error |
| Tes UI Android, API 35 | 5 tes lulus, termasuk pengukuran label navigasi pada 320 dp/huruf 160% |
| Lint release | 0 error, 62 warning, 2 hint; tanpa baseline baru atau penonaktifan pemeriksaan |
| APK debug dan APK/AAB release | Berhasil dibangun, versi 4.13.1 (4865) |
| Manifest release | Activity pratinjau debug tidak disertakan |
| Sertifikat APK | Sesuai APK legacy; SHA-256 `895bb014f520705a5a58805fbb2bdc994f13dbcd8e5883221591ef22d7383b6a` |
| Pemeriksaan visual | 12 tangkapan layar: 360 × 800 dp, 320 × 640 dp/huruf 160%, beranda 1280 × 800 dp, dan order aktif bertema gelap |
| Whitespace diff | `git diff --check` lulus |

Percobaan emulator awal sempat terhenti ketika System UI emulator mengalami ANR; tes diulang setelah proses build dipisahkan dan kelima tes selesai tanpa kegagalan. Data visual, ukuran layar, dan [log tes UI](ui-review/2026-11-02/usability-tests.txt) tersedia bersama [galeri pratinjau](ui-review/2026-11-02/README.md).

Sebelum rilis luas, lakukan uji kegunaan bersama driver: mulai online tanpa bantuan, membaca alamat jemput/tujuan, menerima order, mengirim pesan, membuka petunjuk jalan, menyelesaikan pembayaran/perjalanan, memeriksa saldo, dan kembali ke beranda. Catat langkah yang keliru dan bagian yang memerlukan bantuan; keberhasilan build tidak menggantikan pengamatan ini.
