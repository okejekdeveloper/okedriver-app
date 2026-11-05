# Brand and control review

The changes use Hallmark's app-screen principles within native Android Compose: preserve product behavior, use the existing brand and theme tokens, keep controls predictable, and show loading, failure and content as separate states. Hallmark's HTML/CSS linter does not validate Compose; native build, tests and screenshots are the checks for this work.

## Findings and fixes

| Finding | Result |
| --- | --- |
| Orange helmet logo differed from legacy | `oke_driver_mark.png` is an exact copy of legacy `drawable/new_logo_okejek1.png`. Density-specific legacy launcher icons are copied too; adaptive icon backgrounds match the purple in the logo. |
| Launcher icon used as a notification small icon | All notification builders use the legacy monochrome notification resource, purple accent, and purple legacy logo as the large icon. Firebase fallback metadata uses the same resources. Android controls status-bar icon tint. |
| Typography still used `FontFamily.Default` | All 15 typography roles use bundled Inter static files, weights 400, 500, 600 and 700. Static instances also support Android 6/7. License and source notices are in `assets/licenses/`. |
| Inconsistent pill defaults and large rounded surfaces | Shared filled, outlined, tonal and text-button wrappers use 6 dp corners and at least 48 dp touch targets. Cards use 8 dp; detail panels use 12 dp top corners. The wrappers are applied across production screens. |
| Decorative controls dominated content | Dynamic actions use compact icon/text rows and standard ripples. Colored icon tiles, press scaling, floating navigation, heavy shadows and hero gradients are removed from the shared components. |
| Loading replaced a button's label | Primary actions retain their label beside a small spinner and reject duplicate submissions. |
| Google login was still offered | The Google button, launcher and Google-specific dependencies in the login ViewModel are removed. Password login and the separate account-linking functionality remain. |
| Rejected credentials appeared briefly in a snackbar | Credential errors persist under the password field until email/password is edited. Empty fields have their own messages. Network and suspended-account errors retain their actual reason. |
| Initial order composition looked like a missing order | The state records completion of the first load. Until then, the screen displays only loading UI. Later refreshes retain the loaded order; actual failures offer retry. |
| Inter navigation labels wrapped at 160% text on 320 dp screens | Removed unnecessary horizontal padding and used semibold selection labels. Text scale is preserved. |
| Active-order actions scrolled out of view and the sheet could obscure the address | The order has a bounded map, a separate scrolling detail area and a fixed action footer. There is no draggable sheet. Map controls retain their own space, and the address is visible at the start of the details. |

## Verification

- Font files inspected: Inter family, static outlines, expected weights.
- Brand asset hash compared with legacy: exact match.
- Unit regressions cover login validation, rejected credentials, network failure, suspended accounts, successful login, duplicate requests, first order load and failed refresh.
- Native UI regressions cover the persistent credential error, absence of Google login, and loading without empty-state/retry content.
- Debug review fixtures include `login`, `login-error` and `current-loading` for repeatable screenshot capture.

`testDebugUnitTest`, `assembleDebug`, and `assembleDebugAndroidTest` succeeded. All **99 unit tests** and **20 Android instrumentation tests** passed. After separating map, details and actions, all **12 focused usability tests** passed again on the compact emulator, including visible back controls and order actions while scrolling with 160% text. The final screenshots were visually reviewed at normal and 160% text sizes.

Screenshots of the native presentation components are in [the review gallery](ui-review/2026-11-04-brand/README.md). Login and loading fixtures render the actual production composables with local test data. The screenshots do not represent a live account or a backend login test.
