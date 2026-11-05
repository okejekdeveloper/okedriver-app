# OkeDriver Migration Plan
## Kotlin XML → Kotlin Jetpack Compose

> Dokumen ini memetakan **semua** fitur di codebase legacy `okedriver` (Activity/Fragment/XML)
> dan merencanakan migrasinya ke `okedriver-app` (Compose + Hilt + Clean Architecture).

> **🔄 Update 10 Sep 2026:** Status di dokumen ini diverifikasi ulang langsung terhadap kode
> (`Route.kt`, `NavGraph.kt`, dan keberadaan `ViewModel`/`Repository`/DTO per fitur), bukan cuma
> ditulis manual. Banyak item yang sebelumnya ditandai ❌/🟡 ternyata sudah ✅ di kode — dokumen versi
> lama (12 Mei 2026) sempat jauh tertinggal dari progres aktual. **Overall progress saat ini: ~85%**
> (navigasi & integrasi API fitur inti ~90-95% selesai, tapi Fase 7 "Polish & Parity" praktis belum
> disentuh, dan masih ada kode prototipe mati `ui/screens/*` + `ui/navigation/*` yang perlu dihapus).

---

## 1. Arsitektur Target

```
okedriver-app/
├── app/
│   └── src/main/java/id/okejack/okedriver/
│       ├── MainActivity.kt                    ← Single Activity host
│       ├── OkeDriverApplication.kt            ← @HiltAndroidApp
│       ├── app/navigation/
│       │   ├── Route.kt                       ← Sealed class semua route
│       │   └── NavGraph.kt                    ← NavHost wiring
│       ├── core/
│       │   ├── network/                       ← Retrofit, OkHttp, Interceptor, DI
│       │   ├── database/                      ← Room DB, DAO, Entity, DI
│       │   ├── datastore/                     ← DataStore Preferences (session)
│       │   ├── designsystem/                  ← Theme, Color, Typography, Shape, Spacing
│       │   ├── common/                        ← Util helpers (currency, date, geo, string)
│       │   └── service/                       ← Foreground service (location, FCM)
│       └── feature/
│           ├── auth/     {data, domain, presentation}
│           ├── home/     {data, domain, presentation}
│           ├── order/    {data, domain, presentation}
│           ├── balance/  {data, domain, presentation}
│           ├── profile/  {data, domain, presentation}
│           ├── news/     {data, domain, presentation}
│           └── settings/ {data, domain, presentation}
```

**Prinsip:**
- **Single Activity** — semua screen adalah Composable, navigasi via Navigation Compose.
- **Clean Architecture per feature** — data (remote/local) → domain (model/usecase/repo interface) → presentation (ViewModel + Screen).
- **Hilt DI** — satu module per layer (`NetworkModule`, `DatabaseModule`, `DataStoreModule`) + per-feature module.
- **Unidirectional Data Flow** — ViewModel expose `StateFlow`, Screen observe via `collectAsStateWithLifecycle()`.

---

## 2. Inventaris Legacy (okedriver) vs Progress Modern (okedriver-app)

### 2.1 Screens & Activities

| # | Legacy (Activity/Fragment) | Route di Compose | Status Compose |
|---|---------------------------|------------------|----------------|
| 1 | `InitActivity` | `Route.Init` → `InitScreen` | ✅ Done |
| 2 | `LoginActivity` | `Route.Login` → `LoginScreen` | ✅ Done (feature/auth) |
| 3 | `MainActivity` (home + drawer + map) | `Route.Home` → `HomeScreen` | ✅ Wired + `HomeViewModel` (API driver info, toggle status, map, queue number, MOTD) |
| 4 | `CurrentOrderActivity` + fragments | `Route.CurrentOrder` → `CurrentOrderScreen` | ✅ Basic flow done |
| 5 | `OrderNotifierActivity` (incoming order popup) | `Route.IncomingOrder` → `IncomingOrderScreen` | ✅ Basic FCM + accept/decline done |
| 6 | `OrdersActivity` + `OrderListFragment` + tabs | `Route.OrderHistory` → `OrderHistoryScreen` | ✅ Basic backend list done |
| 7 | `ViewOrderActivity` | `Route.OrderDetail` → `OrderDetailScreen` | ✅ Basic backend detail done |
| 8 | `ChatActivity` | `Route.Chat` → `ChatScreen` | ✅ Basic chat done |
| 9 | `EditShoppingItemActivity` + fragments | `Route.EditShoppingItems` → `EditShoppingItemsScreen` | ✅ Basic edit flow done |
| 10 | `OrderPaymentsActivity` | `Route.OrderPayments` → `OrderPaymentsScreen` | ✅ Basic payment detail done |
| 11 | `RecentOrderLocationActivity` | `Route.RecentOrderLocations` → `RecentOrderLocationsScreen` | ✅ Basic map markers done |
| 12 | `BalanceActivity` + `BalanceFragment` | `Route.Balance` → `WalletScreen` | ✅ Wired + `WalletViewModel`/`WalletRepositoryImpl` (balance, withdrawable) |
| 13 | `TopUpActivity` / `DepositActivity` / `DepositNewActivity` | `Route.TopUp` → `TopUpScreen` | ✅ Wired + `TopUpViewModel` (nominal → bank → VA) |
| 14 | `DepositDetailActivity` | `Route.TopUpDetail` → `TopUpDetailScreen` | ✅ Wired + `TopUpDetailViewModel` |
| 15 | `VaBankActivity` / `VaResultBankTransfer` | (belum ada route terpisah) | ❌ Not started — kemungkinan tercakup di flow `TopUpScreen`, perlu dicek manual apakah masih ada gap |
| 16 | `TopupHistory` | `Route.TopUpHistory` → `TopUpHistoryScreen` | ✅ Wired + `TopUpHistoryViewModel` |
| 17 | `TransactionsActivity` | `Route.Transactions` → `TransactionHistoryScreen` | ✅ Basic backend list done |
| 18 | `WithdrawBalanceActivity` | `Route.Withdraw` → `WithdrawScreen` | ✅ Basic backend flow done |
| 19 | `BonusBoxActivity` + fragments | `Route.BonusBox` → `BonusBoxScreen` | ✅ Wired + `BonusBoxViewModel`/`BonusBoxDto` |
| 20 | `ProfileActivity` | `Route.Profile` → `ProfileScreen` | ✅ Basic backend view done |
| 21 | `EditProfileActivity` | `Route.EditProfile` → `EditProfileScreen` | ✅ Basic profile/bank edit done |
| 22 | `NewsActivity` | `Route.News` → `NewsListScreen` | ✅ Wired + `NewsListViewModel`/`NewsRepositoryImpl` |
| 23 | `ViewNewsActivity` | `Route.NewsDetail` → `NewsDetailScreen` | ✅ Wired + `NewsDetailViewModel` |
| 24 | `SettingsActivity` | `Route.Settings` → `SettingsScreen` | ✅ Wired + `SettingsRepositoryImpl` |
| 25 | `DriverBidActivity` | `Route.DriverBid` → `DriverBidScreen` | ✅ Wired + `DriverBidViewModel` |
| 26 | `ConnectUserSettingActivity` | `Route.ConnectAccount` → `ConnectAccountScreen` | ✅ Wired + `ConnectAccountViewModel` |
| 27 | `AirportDriverAttendanceActivity` | `Route.AirportAttendance` → `AttendanceScreen` | ✅ Basic attendance + queue status done |
| 28 | `DriverOkeFastActivity` + `DriverOkeNowFragment` | `Route.OkeFast` → `OkeFastScreen` | ✅ Basic generate code done |
| 29 | `QueueNumberFragment` | (bukan route terpisah — digabung ke `HomeScreen`/`HomeViewModel`) | ✅ Tergabung di Home |
| 30 | `ReferralFragment` | `Route.Referral` → `ReferralScreen` | ✅ Basic code + referred drivers done |
| 31 | `SoftwareUpdateNoticeFragment` | (dialog/bottom sheet) | ❌ Not started |

**Ringkasan:** 28/31 ✅ done atau wired dengan ViewModel+API, 1 tergabung ke fitur lain, 2 (`VaBank` flow, software update dialog) belum dikonfirmasi/belum ada. "Wired" di sini berarti route terdaftar di `NavGraph.kt` dan ViewModel-nya memanggil Repository/API asli (bukan data statis) — bukan jaminan *feature parity* penuh dengan legacy (edge case, error handling, dsb belum diaudit satu-satu).

### 2.2 API Endpoints (Legacy → Perlu dimigrasikan)

| Domain | Endpoint | Method | Feature Module Target |
|--------|----------|--------|-----------------------|
| **Auth** | `login` | POST | `feature/auth` ✅ |
| | `login/google` | POST | `feature/auth` ❌ |
| **FCM** | `fcm/register` | POST | `feature/auth` (post-login) |
| | `fcm/unregister` | POST | `feature/auth` (logout) |
| **OneSignal** | `onesignal/register` | POST | `feature/auth` (evaluate) |
| **Driver** | `driver/info` | GET | `feature/profile` |
| | `driver/update-location` | POST | `core/service` |
| | `toggle-status` | GET | `feature/home` |
| | `driver/check-absence-status` | GET | `feature/home` |
| | `driver/submit-status` | POST | `feature/home` |
| | `edit-profile` | POST | `feature/profile` |
| | `submit-driver-bid` | POST | `feature/settings` |
| | `session/queue/number` | GET | `feature/home` |
| | `session/queue/register` | POST | `feature/home` |
| | `referral/code` | GET | `feature/profile` |
| | `referral/drivers` | GET | `feature/profile` |
| **Order** | `orders` | GET | `feature/order` |
| | `order/view/{id}` | GET | `feature/order` |
| | `order/current-order` | GET | `feature/order` |
| | `order/pending-order` | GET | `feature/order` |
| | `order/update-status` | GET | `feature/order` |
| | `order/cancel` | GET+POST | `feature/order` |
| | `orders/recent-orders` | GET | `feature/order` |
| | `order/shopping/edit/{id}` | POST | `feature/order` |
| | `order/chats/{id}/messages` | GET | `feature/order` |
| | `order/chats/{id}/submit` | POST | `feature/order` |
| | `order/okefast/generate-code` | GET | `feature/order` |
| **Balance** | `driver/balance` | GET | `feature/balance` |
| | `driver/withdrawable-balance` | GET | `feature/balance` |
| | `driver/transactions` | GET | `feature/balance` |
| | `driver/transactions/withdrawable` | GET | `feature/balance` |
| | `submit-withdraw` | POST | `feature/balance` |
| | `withdraw-requests` | GET | `feature/balance` |
| | `bonus` | GET | `feature/balance` |
| | `bonus/open/{id}` | POST | `feature/balance` |
| | `payments/deposits` | POST | `feature/balance` |
| | `payment-gateway/driver/topup-request` | POST | `feature/balance` |
| | `payment-gateway/driver/topup` | POST | `feature/balance` |
| | `payment-gateway/driver/deposit-detail` | POST | `feature/balance` |
| | `payment-gateway/driver/deposit-list` | POST | `feature/balance` |
| **Common** | `payment-methods` | GET | `core/network` |
| | `app/init/android` | GET | `core/common` |
| | `news` | GET | `feature/news` |
| | `news/{id}` | GET | `feature/news` |
| | `motd` | GET | `feature/home` |
| **Points** | `points/points` | GET | `feature/balance` |
| | `points/transactions` | GET | `feature/balance` |
| | `points/redeem` | POST | `feature/balance` |
| **Massage** | `massage/orders` | GET | `feature/order` |
| **Device** | `session/device` | POST | `core/service` |
| | `auto-report/bad-apps` | POST | `core/service` |
| **User Connect** | `user-connect` | POST | `feature/settings` |
| | `user-disconnect` | GET | `feature/settings` |
| **Merchant** | `vendors/{id}` | GET | `feature/order` |

### 2.3 Background Services (Kritis)

| Legacy Service | Fungsi | Target di Compose App |
|---------------|--------|----------------------|
| `OnlineDriverService` | Foreground service location tracking saat online | `core/service/OnlineDriverService.kt` — **tetap Service**, bukan Composable |
| `UpdateLocationIntentService` | Push lokasi ke server periodik | Merge ke `OnlineDriverService` atau WorkManager |
| `FCMReceiverHandler` | Handle FCM push notification | `core/service/FCMReceiverHandler.kt` |
| `FCMNotificationHandlerService` | Process order notification data | Merge ke handler utama |
| `FCMRegistrationService` | Register FCM token ke backend | Pindah ke UseCase di `feature/auth` |
| `RegisterFCMToken` | Job service register token | Merge ke coroutine-based |
| `RegisterPid` (OneSignal) | Register player ID | Evaluasi: masih pakai OneSignal? |
| `BootCompletedReceiver` | Auto-start service setelah reboot | `core/service/BootCompletedReceiver.kt` |
| `LocationAlarmReceiver` | Alarm-based location update | Ganti dengan WorkManager periodic |
| `AutoStartWorker` | WorkManager auto-start | Pindah ke `core/service/` |
| `ReportBadAppIntentService` | Report anomaly | Ganti Crashlytics custom event |
| `DeviceReportIntentService` | Device info reporting | Ganti Crashlytics/Analytics |
| `AudioSystemInitializer` | Init audio untuk notif order | `core/common/AudioInitializer.kt` |

### 2.4 Data Models (52 model di legacy)

| Kategori | Models | Target |
|----------|--------|--------|
| **Auth** | `Driver`, `User`, `DriverParcelable`, `UserParcel` | `feature/auth/domain/model/` ✅ partial (`AuthDriver`) |
| **Order** | `Order`, `OrderData`, `OrderItem`, `OrderPackage`, `OrderParcel`, `MyOrderParcel`, `OrderCancel`, `OrderScore`, `OrderPayment`, `PaymentOrder`, `Express`, `MassageOrder`, `MassagePackage`, `MassagePackageDuration` | `feature/order/domain/model/` |
| **Balance** | `Topup` (×2), `TopupList`, `TopupMethod`, `Transaction`, `SingleTransaction`, `BonusBox`, `VaData`, `DriverBank`, `PointTransaction` | `feature/balance/domain/model/` |
| **Merchant** | `MerchantItem`, `Vendor`, `Coupon`, `PackagePricing`, `ShoppingItem` | `feature/order/domain/model/` |
| **News** | `NewsItem` (×2) | `feature/news/domain/model/` |
| **Chat** | `ChatMessage` | `feature/order/domain/model/` |
| **Maps** | `Routes`, `MapDirectionDataModel`, `Overview_polyline` | `core/common/model/` |
| **Settings** | `SettingItem`, `DriverBid`, `InitSettings` | `feature/settings/domain/model/` |
| **Common** | `ResponseDataModel`, `ResponseModel`, `ResourceError`, `Pagination`, `CurrencyModel`, `Score` | `core/network/model/` |
| **Internal/UI** | `OrderRVObject`, `OrderPaymentObject`, `CurrentOrderFragmentObject`, `PaymentItem` | Tidak perlu di-migrate (UI adapter specific) |

### 2.5 Helpers / Utils (25 file)

| Helper | Fungsi | Target |
|--------|--------|--------|
| `SessionHelper` | SharedPreferences auth token | ✅ Sudah diganti `SessionDataStore` |
| `CurrencyHelper` | Format Rupiah | `core/common/CurrencyFormatter.kt` |
| `DateTimeHelper` | Format tanggal/jam | `core/common/DateTimeFormatter.kt` |
| `GeoHelper` | Geocoding, distance | `core/common/GeoHelper.kt` |
| `StringHelpers` | String manipulation | `core/common/StringExt.kt` |
| `OkeHelpers` | General utilities | Pecah ke extension functions |
| `OrderHelper` | Order-specific logic | `feature/order/domain/util/` |
| `OrderDiscountHelper` | Kalkulasi diskon | `feature/order/domain/util/` |
| `LocationServiceHelper` | Location permission & service | `core/service/LocationHelper.kt` |
| `Api` | Base URL & config | ✅ Sudah di `RetrofitClient` |
| `OkeData` | Static data/constants | `core/common/Constants.kt` |
| `DirectionParser` | Parse Google Directions API | `core/common/DirectionParser.kt` |
| `ViewHelper` | View utility (XML specific) | ❌ Tidak perlu (Compose native) |
| `ClipboardHelper` | Copy to clipboard | `core/common/ClipboardHelper.kt` |
| `SimpleNotify` | Notification builder | `core/service/NotificationHelper.kt` |
| `NotificationChannelUtils` | Create notification channels | `core/service/NotificationChannelUtils.kt` |
| `ShowActivityOnLock` | Wake screen on lock | `core/common/WakeScreenHelper.kt` |
| `PackageHelper` | App version info | `core/common/PackageHelper.kt` |
| `Logger` | Custom logging | Ganti Timber atau Logger library |
| `RandomHelper` | Random generation | Hapus, pakai Kotlin stdlib |
| `StartupHelper` | App startup check | `core/common/StartupHelper.kt` |

### 2.6 Adapters (20 adapter) → Compose LazyList/LazyColumn

Semua RecyclerView adapter di legacy **tidak perlu di-migrate** satu-per-satu.
Compose pakai `LazyColumn` / `LazyRow` dengan item composable langsung.

| Legacy Adapter | Composable Pengganti |
|----------------|---------------------|
| `RVOrdersAdapter` | `OrderListItem` composable di `feature/order` |
| `OrdersAdapter` | Sama ↑ |
| `OrderItemAdapter` | `OrderItemRow` |
| `OrderPaymentAdapter` / `OrderPaymentRVAdapter` | `PaymentRow` |
| `OrderChatAdapter` | `ChatBubble` |
| `EditShoppingItemAdapter` | `ShoppingItemEditor` |
| `MerchantItemAdapter` | `MerchantItemCard` |
| `PaymentAdapter` / `BankAdapter` | `PaymentMethodCard` / `BankCard` ✅ sudah ada |
| `TopupAdapter` / `NominalAdapter` | `AmountChip` ✅ sudah ada |
| `TransactionAdapter` / `RVBalanceAdapter` | `TransactionRow` |
| `BonusBoxAdapter` | `BonusBoxCard` |
| `DriverNewsItemsAdapter` | `NewsItemCard` |
| `ReferredDriverAdapter` | `ReferralDriverRow` |
| `SettingItemsAdapter` | `SettingItemRow` |
| `OrderListFragmentAdapter` (ViewPager) | `HorizontalPager` + tabs |

### 2.7 Catatan Penting: Dua Jalur UI di okedriver-app

> **🔄 Verifikasi 10 Sep 2026:** dikonfirmasi `MainActivity.kt` hanya memanggil
> `app/navigation/OkeDriverNavGraph`. Folder `ui/screens/*`, `ui/navigation/*`, dan `ui/model/`
> **masih ada di repo tapi sudah terkonfirmasi dead code** — tidak direferensikan dari `MainActivity`
> maupun `NavGraph.kt` manapun. Aman secara fungsional, tapi berisiko membingungkan kontributor baru
> (misal ada 2 `LoginScreen.kt`, 2 `WalletScreen.kt`, 2 `WithdrawScreen.kt`, 2 `ProfileScreen.kt`,
> 2 `TopUpScreen.kt`, 2 `IncomingOrderScreen.kt`, 2 `OrderDetailScreen.kt` di codebase). Rekomendasi:
> hapus seluruh folder `ui/` (bukan lagi "pindahkan", karena semua screen yang relevan sudah ada
> versi barunya yang lebih lengkap di `feature/*/presentation/`) — cek dulu `ui/components/` apakah
> ada composable reusable yang belum ada padanannya di `core/designsystem/component/` sebelum dihapus.
>
> | Jalur | Package | Dipakai oleh | Karakteristik |
> |-------|---------|-------------|---------------|
> | **Feature layer** (aktif) | `feature/{auth,home,order,balance/wallet,profile,news,settings,debug}` | `app/navigation/NavGraph.kt` → `MainActivity` | Clean arch (ViewModel + UseCase + Repo), integrasi API nyata |
> | **UI prototype** (mati, belum dihapus) | `ui/screens/*` + `ui/navigation/*` + `ui/model/` | Tidak dipanggil dari manapun | UI kaya + data mock/statis, tanpa ViewModel/Hilt |
>
> **Strategi konsolidasi (update):**
> - ~~Pindahkan UI dari `ui/screens/*`~~ → sudah tidak relevan, feature layer sudah unggul cakupannya
> - [ ] Audit `ui/components/` untuk composable yang belum punya padanan di `core/designsystem/component/`
> - [ ] Hapus `ui/screens/`, `ui/navigation/`, `ui/model/` (dan `ui/components/` setelah diaudit)
> - [ ] Pastikan tidak ada referensi tersisa (build clean setelah hapus)

### 2.8 Design System

| Komponen | Status |
|----------|--------|
| Color palette | ✅ Done (80 warna) |
| Typography | ✅ Done (14 text styles) |
| Shape | ✅ Done |
| Spacing | ✅ Done |
| `OkedriverTheme` (light + dark) | ✅ Done |
| `OkeButton` / `GradientButton` | ✅ Done |
| `OkeTextField` | ✅ Done |
| `OkeTopBar` | ✅ Done |
| `BalanceCard` | ✅ Done |
| `BankCard` | ✅ Done |
| `AmountChip` | ✅ Done |
| `CountdownTimer` | ✅ Done |
| `DrawerMenuItem` | ✅ Done |
| `OkeDriverMap` | ✅ Done |
| `OrderHistoryCard` | ✅ Done |
| `OrderRouteCard` | ✅ Done |
| `ProfileDrawerHeader` | ✅ Done |
| `QuickActionButton` | ✅ Done |
| Loading/shimmer component | ❌ Belum |
| Error/empty state component | ❌ Belum |
| Dialog/BottomSheet component | ❌ Belum |
| Badge/chip component | ❌ Belum |
| SlidingButton (swipe to accept) | ❌ Belum |

---

## 3. Fase Migrasi (Roadmap)

### Fase 0: Foundation (Estimasi: 1 minggu)
> Pastikan infrastruktur siap sebelum fitur apapun.

- [ ] Finalisasi `core/network` — semua API service interface + interceptor + error handling
- [ ] Finalisasi `core/database` — Room DB schema final (orders, transactions)
- [ ] Finalisasi `core/datastore` — SessionDataStore (token, driver profile cache)
- [ ] Buat `core/common` — migrate semua helper/utility yang reusable
- [ ] Setup `core/service` — foreground service skeleton (location, FCM)
- [ ] Setup error handling pattern — `Result<T>` atau sealed `ApiResponse`
- [ ] Setup testing infrastructure — unit test untuk UseCase, Repository
- [ ] Pastikan `.gitignore` bersih dan CI/CD siap

### Fase 1: Auth & Session (Estimasi: 3-5 hari)
> Gerbang masuk app. Harus 100% sebelum lanjut.

- [ ] ✅ Login (email/password) — sudah done
- [ ] Tambah Google Sign-In flow
- [x] FCM token registration setelah login berhasil
- [ ] Session check on app start (init screen) — sudah done
- [x] Logout + clear session + unregister FCM
- [x] Handle token expired → redirect ke login

### Fase 2: Home / Dashboard (Estimasi: 1-2 minggu)
> Layar utama driver. Paling kompleks karena gabungan map + status + drawer.

- [x] Driver info API integration (`driver/info`)
- [x] Online/offline toggle (`toggle-status`)
- [x] Map integration (lokasi real-time driver)
- [x] `OnlineDriverService` foreground service migration
- [x] Location update periodik ke server
- [x] Navigation drawer (profil, balance, history, settings, news, logout)
- [x] Queue number display (`session/queue/number`)
- [x] Queue number registration (`session/queue/register`)
- [x] MOTD (message of the day) display
- [x] App init settings (`app/init/android`) - version check, force update dialog

### Fase 3: Order Management (Estimasi: 2-3 minggu)
> Fitur inti driver — terima, proses, selesaikan order.

- [x] Incoming order notification (FCM → IncomingOrderScreen)
- [x] Accept/decline order flow
- [x] Current order detail screen (route map, customer info, order items)
- [x] Order status update flow (picked up → on the way → delivered)
- [x] Swipe-to-confirm button component
- [x] Chat with customer
- [x] Edit shopping items (OkeShop/OkeFood)
- [x] Order payment confirmation
- [x] Cancel order flow
- [x] Order history list (tabs: aktif, riwayat)
- [x] Order detail view
- [x] Recent order locations (map view)
- [x] OkeFast generate code

### Fase 4: Balance & Wallet (Estimasi: 1-2 minggu)
> Keuangan driver — saldo, top up, withdraw, transaksi.

- [x] Balance overview (`driver/balance`, `driver/withdrawable-balance`)
- [x] Transaction history list
- [x] Top up request flow (pilih nominal → pilih bank → VA number)
- [x] Top up detail & status ✅ (verifikasi 10 Sep 2026: `TopUpDetailScreen` + `TopUpDetailViewModel`)
- [x] Top up history ✅ (verifikasi 10 Sep 2026: `TopUpHistoryScreen` + `TopUpHistoryViewModel`)
- [x] Withdraw request flow
- [x] Withdraw request history
- [x] Bonus box list & open ✅ (verifikasi 10 Sep 2026: `BonusBoxScreen` + `BonusBoxViewModel`)
- [x] Payment methods list
- [ ] VA Bank transfer flow (`VaBankActivity`/`VaResultBankTransfer` legacy) — belum dikonfirmasi ada padanannya, cek manual

### Fase 5: Profile & Settings (Estimasi: 1 minggu)
> Identitas dan preferensi driver.

- [x] Profile view (foto, nama, nomor, kendaraan, bank summary)
- [x] Edit profile (password + bank info sesuai legacy endpoint)
- [x] Referral code & referred drivers list
- [x] Settings screen ✅ (verifikasi 10 Sep 2026: wired + `SettingsRepositoryImpl`)
- [x] Driver bid settings ✅ (verifikasi 10 Sep 2026: wired + `DriverBidViewModel`)
- [x] Connect/disconnect user account ✅ (verifikasi 10 Sep 2026: wired + `ConnectAccountViewModel`)
- [x] Airport attendance (fitur spesifik)

### Fase 6: News & Misc (Estimasi: 3-5 hari)
> Informasi dan fitur pelengkap.

- [x] News list (paginated) ✅ (verifikasi 10 Sep 2026: wired + `NewsListViewModel`/`NewsRepositoryImpl`)
- [x] News detail view ✅ (verifikasi 10 Sep 2026: wired + `NewsDetailViewModel`)
- [ ] Software update notice dialog
- [ ] Notification channel setup — util `NotificationChannelUtils.kt` sudah ada di `core/service/`, tapi belum dikonfirmasi dipanggil saat app startup

### Fase 7: Polish & Parity (Estimasi: 1-2 minggu)
> Pastikan semua edge case, UX, dan performa setara atau lebih baik dari legacy.
>
> **🔄 Status 10 Sep 2026:** ini fase yang paling tertinggal — belum ada bukti implementasi untuk
> item manapun di bawah ini di kode. Sementara Fase 1-6 (screen + API integration) sudah ~90%+,
> Fase 7 ini kemungkinan besar **~0-10%**. Jadi ini prioritas berikutnya setelah bersih-bersih
> `ui/` dead code di §2.7.

- [ ] Loading/shimmer states untuk semua list screen
- [ ] Error states & retry mechanism
- [ ] Empty state illustrations
- [ ] Pull-to-refresh di list screens
- [ ] Offline mode / caching strategy
- [ ] Deep link handling (dari notification ke screen tertentu)
- [ ] Analytics event tracking (Firebase Analytics)
- [ ] Crashlytics integration
- [ ] ProGuard / R8 rules
- [ ] Performance profiling (compose recomposition, memory)
- [ ] Accessibility (content descriptions, touch targets)
- [ ] Dark mode testing menyeluruh

---

## 4. Dependency Migration

| Legacy | Modern Replacement | Status |
|--------|--------------------|--------|
| RxJava 2/3 | Kotlin Coroutines + Flow | ✅ Sudah di okedriver-app |
| Retrofit + RxJava adapter | Retrofit + suspend functions | ✅ |
| SharedPreferences | DataStore Preferences | ✅ |
| Activity/Fragment/XML | Single Activity + Compose | ✅ |
| ViewBinding | Compose | ✅ |
| RecyclerView + Adapter | LazyColumn / LazyRow | ✅ |
| Navigation Component (XML) | Navigation Compose | ✅ |
| Manual DI / no DI | Hilt | ✅ |
| Glide / Picasso | Coil Compose | ✅ |
| Google Maps (XML fragment) | Maps Compose | ✅ |
| OneSignal | Firebase Messaging (evaluate) | 🟡 Perlu keputusan |
| JobIntentService | WorkManager + Coroutines | 🟡 Perlu migrasi |
| AlarmManager | WorkManager periodic | 🟡 Perlu migrasi |
| LiveData | StateFlow | ✅ |

---

## 5. Legacy Dependency yang Harus Di-drop

| Legacy Dependency | Kenapa Ada | Pengganti di Compose App |
|-------------------|-----------|--------------------------|
| RxJava 2 + RxKotlin + RxAndroid | Async & reactive streams | Kotlin Coroutines + Flow |
| Anko (Splitties) | `startActivity<T>()`, toast shortcut | Navigation Compose + Snackbar |
| EasyPrefs | SharedPreferences wrapper | DataStore Preferences ✅ |
| Picasso | Image loading | Coil Compose ✅ |
| Dexter | Runtime permission | Accompanist Permissions / manual |
| SmartLocation | Location helper | Play Services Location langsung |
| FastAdapter + Iconics | RecyclerView adapter framework | LazyColumn + Compose items |
| FancyButtons | Custom styled buttons | `OkeButton` / `GradientButton` ✅ |
| MaterialEditText | Styled input | `OkeTextField` ✅ |
| CircleImageView | Circular avatar | Coil + `Modifier.clip(CircleShape)` |
| Joda-Time | Date/time | `java.time` (desugaring) atau kotlinx-datetime |
| materialdatetimepicker | Date picker dialog | Material3 `DatePicker` / `TimePicker` |
| OkHttp 3.12.x | HTTP client (sangat lama) | OkHttp 4.12.x ✅ |

---

## 6. Risiko & Mitigasi

| Risiko | Dampak | Mitigasi |
|--------|--------|----------|
| Dual codebase maintenance | Drift behavior, double bug | Freeze legacy, semua fitur baru di Compose |
| Background service complexity | Location tracking reliability | Test extensif di berbagai OEM (Xiaomi, Samsung, Oppo) |
| FCM notification handling | Order missed kalau salah handle | End-to-end test notification flow |
| Token management race condition | Random logout | Implement token refresh dengan mutex/semaphore |
| `runBlocking` di AuthInterceptor | ANR di main thread | Ganti ke `runBlocking(Dispatchers.IO)` atau Authenticator pattern |
| Google Maps API key exposure | Security | Restrict key di Google Console + pakai `secrets-gradle-plugin` |
| Large state di ViewModel | Memory leak, slow recomposition | Pecah state, pakai `derivedStateOf`, profile recomposition |
| Missing error handling | Crash di production | Consistent `Result<T>` wrapper + global error handler |

---

## 7. Prioritas Migrasi (Quick Reference)

```
🔴 Kritis (harus selesai dulu):
   Fase 0 (Foundation) → Fase 1 (Auth) → Fase 2 (Home) → Fase 3 (Order)

🟡 Penting (setelah core flow jalan):
   Fase 4 (Balance) → Fase 5 (Profile)

🟢 Nice-to-have (bisa paralel):
   Fase 6 (News) → Fase 7 (Polish)
```

---

## 8. Checklist Kesiapan per Feature Module

Setiap feature module harus punya struktur lengkap sebelum dianggap "done":

```
feature/{name}/
├── data/
│   ├── remote/
│   │   ├── {Name}ApiService.kt        ← Retrofit interface
│   │   └── dto/                        ← Response/Request DTOs
│   ├── local/                          ← Room DAO (kalau perlu cache)
│   └── repository/
│       └── {Name}RepositoryImpl.kt     ← Implementasi repository
├── domain/
│   ├── model/                          ← Domain models (clean, no annotation)
│   ├── repository/
│   │   └── {Name}Repository.kt         ← Interface repository
│   └── usecase/                        ← Business logic units
├── di/
│   └── {Name}Module.kt                ← Hilt module
└── presentation/
    ├── {screen}/
    │   ├── {Screen}Screen.kt           ← Composable UI
    │   └── {Screen}ViewModel.kt        ← State management
    └── components/                     ← Feature-specific composables
```

**Definition of Done per screen:**
- [ ] API service endpoint terdefinisi
- [ ] DTO ↔ Domain model mapping ada
- [ ] Repository interface + implementation
- [ ] UseCase (minimal 1 per aksi utama)
- [ ] ViewModel dengan StateFlow
- [ ] Screen composable terintegrasi dengan ViewModel
- [ ] Loading, error, empty state ter-handle
- [ ] Route terdaftar di NavGraph
- [ ] Navigation action berfungsi (masuk & keluar)

---

*Dokumen ini di-generate pada 12 Mei 2026, diverifikasi ulang & diupdate terhadap kode aktual pada 10 Sep 2026. Update sesuai progress.*
