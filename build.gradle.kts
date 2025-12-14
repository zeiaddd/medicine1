// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // ADD THIS: KSP Plugin declaration for Room (required for Lab 05)
    id("com.google.devtools.ksp") version "2.0.21-1.0.26" apply false
}