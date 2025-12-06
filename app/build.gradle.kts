plugins {
    // Standard Android and Kotlin plugins
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Helper to use Jetpack Compose (Modern UI)
    alias(libs.plugins.kotlin.compose)
    // Required for Firebase to work
    id("com.google.gms.google-services")
    // REQUIRED: Kotlin Annotation Processing Tool.
    // This allows Room to generate the SQL code for your database automatically.
    id("kotlin-kapt")
}

android {
    // Unique ID for your app on the Play Store/Phone
    namespace = "com.example.bloomix"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.bloomix"
        minSdk = 24 // Android 7.0 (Nougat) - covers ~96% of devices
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Shrinks code size for the final store version
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Java Version Compatibility
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    // Enable Jetpack Compose features
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}

dependencies {
    // --- CORE ANDROID UI ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // UPDATED: Ensures AppCompatActivity works correctly
    implementation("androidx.appcompat:appcompat:1.7.0")

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // --- JETPACK COMPOSE (Modern UI Toolkit) ---
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // --- REQUIRED FOR YOUR XML LAYOUTS ---
    // RecyclerView: For the Calendar Grid and History List
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    // CardView: For the rounded cards in History and Results
    implementation("androidx.cardview:cardview:1.0.0")
    // Coroutines: For running Database/ML tasks in the background without freezing UI
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Activity Compose integration
    implementation("androidx.activity:activity-compose:1.9.0")

    // --- FIREBASE (Authentication) ---
    // The BOM (Bill of Materials) ensures all Firebase versions match automatically
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-auth")

    // Material Design Components (Buttons, TextFields styling)
    implementation(libs.material)

    // --- UNIT TESTING ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // --- ROOM DATABASE (CRITICAL) ---
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
}