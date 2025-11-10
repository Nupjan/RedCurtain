import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.kapt")
}

fun getLocalProperty(key: String, defaultValue: String = ""): String {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        val localProperties = Properties()
        FileInputStream(localPropertiesFile).use {
            localProperties.load(it)
        }
        return localProperties.getProperty(key, defaultValue)
    }
    return defaultValue
}

android {
    namespace = "com.example.redcurtainapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.redcurtainapp"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Expose TMDB API key via BuildConfig so we don't hard-code in source
        // First try local.properties (gitignored), then fallback to gradle.properties
        val tmdbKey = getLocalProperty("TMDB_API_KEY")
            .takeIf { it.isNotBlank() }
            ?: providers.gradleProperty("TMDB_API_KEY").getOrElse("")
        buildConfigField("String", "TMDB_API_KEY", "\"$tmdbKey\"")
        // Optional TMDB v4 bearer token support
        val tmdbV4 = getLocalProperty("TMDB_V4_TOKEN")
            .takeIf { it.isNotBlank() }
            ?: providers.gradleProperty("TMDB_V4_TOKEN").getOrElse("")
        buildConfigField("String", "TMDB_V4_TOKEN", "\"$tmdbV4\"")
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
        viewBinding = true
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
}

dependencies {
    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.activity:activity-ktx:1.8.0")

    // Compose BOM and core
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    // Room (with kapt)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Tooling/debug
    debugImplementation(libs.androidx.ui.tooling)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
}