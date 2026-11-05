package id.okejack.okedriver.core.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf

enum class DriverUiStyle(val id: String, val label: String, val description: String) {
    SIMPLE("simple", "Sederhana", "Warna tenang dengan tampilan yang ringkas."),
    DYNAMIC("dynamic", "Dinamis", "Warna lebih cerah dan tombol yang merespons sentuhan.");

    companion object {
        val default = DYNAMIC
        fun fromStored(value: String?): DriverUiStyle = entries.firstOrNull { it.id == value } ?: default
    }
}

// Standalone composables and existing previews retain their original simple appearance.
val LocalDriverUiStyle = staticCompositionLocalOf { DriverUiStyle.SIMPLE }
