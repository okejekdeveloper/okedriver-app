# Beranda OkeDriver

Perbaikan beranda mengikuti prinsip app screen dari [Hallmark](https://github.com/Nutlope/hallmark/blob/main/skills/hallmark/references/flow.md#app-screens), diterapkan pada Android Compose. Ini bukan keluaran HTML/CSS atau hasil kelulusan sloplint web.

## Masalah dan perubahan

Beranda Sederhana dan Dinamis sebelumnya memakai `MapFirstScaffold`: peta layar penuh dengan bottom sheet berstatus `PartiallyExpanded`. Menu kerja dan saldo berada dalam bagian `details`, sehingga pengemudi harus menarik panel ke atas untuk membukanya.

Kedua beranda kini memakai `HomeDashboardLayout`:

- Header tetap dengan nama aplikasi, sapaan dan tombol muat ulang.
- Status kerja serta tombol online/offline mendahului informasi tambahan.
- Saldo ditampilkan sekali, dengan akses ke halaman saldo.
- Menu kerja di dalam halaman yang digulir biasa. Tidak ada pegangan atau status expand/collapse.
- Peta posisi berada di bagian tersendiri setinggi 200 dp setelah informasi kerja.
- Tampilan Dinamis memakai daftar menu dan tombol tema, menggantikan hero bergradasi serta susunan tile dekoratif di beranda.

Warna, tipografi, lebar konten 680 dp dan komponen berasal dari sistem desain aplikasi. Navigasi, callback, konfirmasi offline/antrean, izin dan data backend tetap menggunakan implementasi yang sudah ada. `MapFirstScaffold` pada order aktif tetap digunakan.

## Berkas

- Tambah `feature/home/presentation/HomeDashboardLayout.kt`.
- Sesuaikan `simple/SimpleHomeContent.kt` dan `dynamic/DynamicHomeContent.kt`.
- Perbarui komentar peta pada `MapHomeShared.kt`.
- Tambah `HomeDashboardTest.kt` untuk kedua gaya pada lebar 320 dp dan huruf 160%: tombol kerja, saldo, absensi, pengaturan, peta, header dan ketiadaan aksi expand/collapse.

## Batas verifikasi

`./gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain` berhasil. APK aplikasi dan APK tes, termasuk `HomeDashboardTest`, berhasil dikompilasi. Pemeriksaan whitespace pada berkas perubahan tidak menemukan masalah.

Pemeriksaan visual dan eksekusi tes instrumentasi belum dilakukan: emulator `flutter_emulator` menolak menyala karena ruang disk tidak mencukupi. Perangkat fisik yang terhubung tidak dipakai untuk memasang build debug.
