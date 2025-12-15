plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // 1. ADD THE KSP PLUGIN FOR ROOM DATABASE (Lab 05 requirement)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.medicine"

    // 2. USE YOUR CONFIRMED COMPILE SDK 36
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.medicine"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // ---------------------------------------------------------------------------------
    // Existing Dependencies
    // ---------------------------------------------------------------------------------
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
// --- Testing Dependencies ---
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.10.0")

    // Crucial for argument capturing and concise Mockito syntax in Kotlin
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")

    // For coroutine testing (already likely present)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // For assertion library (com.google.common.truth)
    testImplementation("com.google.truth:truth:1.1.5")
    // ---------------------------------------------------------------------------------
    // 5. NAVIGATION (NEW REQUIREMENT for multi-screen app)
    // ---------------------------------------------------------------------------------
    // Implementation for Compose Navigation: Required to switch between Home and Details screens.
    implementation("androidx.navigation:navigation-compose:2.7.7") // Using the latest stable version
    // ---------------------------------------------------------------------------------


    // ---------------------------------------------------------------------------------
    // 3. ROOM DATABASE (Lab 05 requirement)
    // ---------------------------------------------------------------------------------
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    // KSP annotation processor
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version") // Coroutines support

    // ---------------------------------------------------------------------------------
    // 4. TESTING (Lab 07 requirement)
    // ---------------------------------------------------------------------------------
    val coroutines_test_version = "1.8.0"

    val mockito_version = "5.12.0"
    testImplementation("org.mockito:mockito-core:$mockito_version")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")

    testImplementation(libs.junit) // Existing JUnit 4

    // Coroutine Test dependencies for testing Room/Flow (Lab 07 style testing)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines_test_version")
    testImplementation("com.google.truth:truth:1.1.5") // For clean assertions

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Additional Android Test dependencies
    androidTestImplementation("com.google.truth:truth:1.1.5")
}