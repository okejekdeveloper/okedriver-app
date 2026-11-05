import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

val localProps = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

fun localProperty(name: String): String? =
    localProps.getProperty(name)?.takeIf { it.isNotBlank() }
        ?: System.getenv(name)?.takeIf { it.isNotBlank() }

val hasReleaseSigningConfig = listOf(
    "KEYSTORE_FILE",
    "KEYSTORE_PASSWORD",
    "KEY_ALIAS",
    "KEY_PASSWORD"
).all { localProperty(it) != null }

android {
    namespace = "id.okejack.okedriver"
    compileSdk = 36

    signingConfigs {
        if (hasReleaseSigningConfig) {
            create("release") {
                storeFile = rootProject.file(localProperty("KEYSTORE_FILE")!!).canonicalFile
                storePassword = localProperty("KEYSTORE_PASSWORD")!!
                keyAlias = localProperty("KEY_ALIAS")!!
                keyPassword = localProperty("KEY_PASSWORD")!!
            }
        }
    }

    defaultConfig {
        applicationId = "id.okejack.okedriver"
        minSdk = 23
        targetSdk = 36
        versionCode = 4866
        versionName = "5.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["googleMapsApiKey"] = localProperty("MAPS_API_KEY") ?: ""
        buildConfigField("String", "DIRECTIONS_API_KEY", "\"${localProperty("DIRECTIONS_API_KEY").orEmpty()}\"")
        multiDexEnabled = true
    }

    buildTypes {
        release {
            if (hasReleaseSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = false
            isShrinkResources = false
            buildConfigField("String", "GCS_STORAGE_BUCKET", "\"okejackiddev\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            buildConfigField("String", "GCS_STORAGE_BUCKET", "\"okejackiddev\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

val validateProductionConfig by tasks.registering {
    doLast {
        check(hasReleaseSigningConfig) { "Release membutuhkan KEYSTORE_FILE, KEYSTORE_PASSWORD, KEY_ALIAS, dan KEY_PASSWORD." }
        check(rootProject.file(localProperty("KEYSTORE_FILE")!!).isFile) { "Release keystore tidak ditemukan." }
        check(!localProperty("MAPS_API_KEY").isNullOrBlank()) { "Release membutuhkan MAPS_API_KEY." }
        check(!localProperty("DIRECTIONS_API_KEY").isNullOrBlank()) { "Release membutuhkan DIRECTIONS_API_KEY untuk rute order." }
    }
}
tasks.matching { it.name == "preReleaseBuild" }.configureEach { dependsOn(validateProductionConfig) }

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM + UI
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Lifecycle + ViewModel
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    // Navigation
    implementation(libs.navigation.compose)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore
    implementation(libs.datastore.preferences)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    // Maps
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.play.services.auth)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging)

    // Image loading
    implementation(libs.coil.compose)


    implementation(libs.androidx.navigation.compose)
    implementation(libs.osmdroid)

    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.navigation.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
