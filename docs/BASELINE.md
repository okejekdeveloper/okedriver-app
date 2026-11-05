# Baseline: okedriver (legacy) vs okedriver-app (Compose)

Diukur 5 November 2026 dari working tree lokal. Tujuannya satu: setiap perbaikan setelah ini
dibandingkan dengan angka di sini, bukan dengan perasaan. Angka yang belum diukur ditandai
**belum diukur** dan jangan dipakai sebagai klaim sebelum diisi.

## Terukur

| Metrik | okedriver (legacy) | okedriver-app (baru) | Catatan |
|---|---|---|---|
| UI toolkit | XML + ViewBinding | Jetpack Compose | |
| Versi | 4.13.0 (4864) | 5.0.0 (4866) | |
| minSdk / targetSdk | 21 / 36 | 23 / 36 | minSdk naik 2 level; perlu keputusan produk |
| File sumber (main) | 236 `.kt`/`.java` + 106 layout XML | 266 `.kt` | |
| Baris kode (main) | 23.074 | 21.651 | Kode baru lebih kecil walau fiturnya mengejar paritas |
| Direktif dependency di `build.gradle` | 56 | 36 | Hitungan kasar per baris `implementation/api/kapt/ksp` |
| Jumlah `@Test` | 19 | 99 | Baru: 35 file tes, hampir semuanya DTO/parser/policy |
| `isMinifyEnabled` (release) | false | false | Keduanya belum memakai R8 |
| Release APK (tanpa R8) | 17,3 MB (build 16 Okt 2026) | 17,7 MB (build 4 Nov 2026) | Bukan hasil perbandingan adil sampai R8 aktif di keduanya |
| Debug APK | 25,1 MB | 26,9 MB | |
| Jumlah commit | 10 (branch aktif) | 14 | Lihat catatan git di bawah |

## Belum diukur

| Metrik | Cara mengukur |
|---|---|
| Lint release (error/warning) | `./gradlew :app:lintRelease`; audit 28 Okt melaporkan 3 error, 61 warning |
| Cold start (TTID) | Macrobenchmark `StartupBenchmark` di perangkat nyata, kedua app, 10 iterasi |
| Waktu build bersih dan incremental | `./gradlew assembleDebug --profile`, 3 kali, ambil median |
| Ukuran APK dengan R8 + shrinkResources | Setelah R8 aktif di baru; legacy perlu diaktifkan di branch lepas untuk perbandingan |
| Crash-free users, ANR rate | Crashlytics / Play Console, 28 hari terakhir untuk legacy |
| Coverage | Kover atau JaCoCo; belum dikonfigurasi |

## Temuan hygiene git (perlu keputusan)

1. **351 file belum di-commit** di `okedriver-app` (97 diubah, 105 dihapus, 149 baru). Commit
   terakhir bertanggal 29 Juni 2026, sehingga hampir lima bulan pekerjaan hanya ada di satu
   working tree. Satu `git clean` atau disk bermasalah menghapus semuanya.
2. Branch aktif `wip/ui-merge-safe`. Riwayat commit yang ada memakai gaya campuran
   (`[FEAT]`, `feat:`, `first commit`).
3. Remote `okedriver-app` menunjuk ke `github.com/arvardy184/DocuMine.git`, repositori lain.
   Salah satu remote ini sebaiknya dihapus supaya tidak ada push ke tempat yang salah.
4. `app/google-services.json` ter-track dan ada di riwayat git. File ini bukan rahasia kuat, tetapi
   API key di dalamnya harus dibatasi (package name + SHA-1) di Google Cloud Console sebelum
   repositori dipublikasikan. Tidak ada keystore atau `local.properties` di riwayat.
5. Visibilitas repositori GitHub belum diperiksa (`gh` tidak terpasang di mesin ini).

## Hasil run

`./gradlew :app:testDebugUnitTest` pada 5 November 2026: **99 test di 31 suite, 0 gagal, 0 error,
0 dilewati**, BUILD SUCCESSFUL dalam 1m 28s. Sebagian besar task sudah `UP-TO-DATE`
(5 dari 35 dieksekusi), jadi durasi ini bukan waktu build bersih.
