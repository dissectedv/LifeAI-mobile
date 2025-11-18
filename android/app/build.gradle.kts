plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.lifeai_mobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.lifeai_mobile"
        minSdk = 27
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
}

dependencies {
    // --- Compose Core ---
    val composeVersion = "1.7.4"
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$composeVersion")

    implementation("com.patrykandpatrick.vico:compose-m3:1.14.0")
// Ou a versão mais recente
    implementation("com.patrykandpatrick.vico:core:1.14.0")
    implementation("androidx.compose.foundation:foundation:1.6.8")
    // --- Material 3 (para o Compose) ---
    implementation("androidx.compose.material3:material3:1.3.0")

    // --- Material Components (para temas XML) ---
    implementation("com.google.android.material:material:1.12.0")

    // --- Activity & Lifecycle ---
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")

    // --- Navigation Compose ---
    implementation("androidx.navigation:navigation-compose:2.8.3")

    // --- Icons ---
    implementation("androidx.compose.material:material-icons-core:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")

    // --- ConstraintLayout Compose ---
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.1")

    // --- DataStore ---
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // --- Retrofit + Gson + OkHttp ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // --- Coroutines ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // --- Splash Screen ---
    implementation("androidx.core:core-splashscreen:1.0.1")

    // --- Testes ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
    implementation("io.coil-kt:coil-compose:2.6.0")
}