plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

android {
    // ... your existing android block
}

dependencies {
    // ... your existing dependencies

    // ---------------------------------------------------------------------------------
    // 4. TESTING (Lab 07 requirement)
    // ---------------------------------------------------------------------------------
    val coroutines_test_version = "1.8.0"

    // START: ADD THESE DEPENDENCIES
    val mockito_version = "5.12.0"
    testImplementation("org.mockito:mockito-core:$mockito_version")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    // END: ADD THESE DEPENDENCIES

    testImplementation(libs.junit) // Existing JUnit 4

    // Coroutine Test dependencies for testing Room/Flow (Lab 07 style testing)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines_test_version")
    testImplementation("com.google.truth:truth:1.1.5") // For clean assertions

    // ... rest of your dependencies
}
