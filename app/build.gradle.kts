plugins {
    id ("com.android.application")
    id ("org.jetbrains.kotlin.android")
    id ("kotlin-kapt")
    id ("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.tiktok"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.tiktok"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation ("androidx.core:core-ktx:1.12.0")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.google.android.material:material:1.11.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.google.firebase:firebase-config-ktx:21.6.0")
    implementation ("com.google.android.play:review-ktx:2.0.1")
    implementation("androidx.activity:activity:1.9.1")
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")

    // Navigation component
    val nav_version = "2.7.6"
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

    //Lifecycle
    val lifecycle_version = "2.5.1"
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation ("androidx.lifecycle:lifecycle-service:$lifecycle_version")
    implementation ("androidx.lifecycle:lifecycle-process:$lifecycle_version")

    // Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.7.2")
    implementation ("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    //Hilt
    implementation ("com.google.dagger:hilt-android:2.44")
    kapt ("com.google.dagger:hilt-compiler:2.44")

    //Exoplayer
    implementation("androidx.media3:media3-exoplayer:1.4.0")
    implementation("androidx.media3:media3-exoplayer-dash:1.4.0")
    implementation("androidx.media3:media3-ui:1.4.0")

    //Paging
    val pagingVersion = "3.3.2"
    implementation("androidx.paging:paging-runtime-ktx:$pagingVersion")

    //Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
}