object Versions {
    // Plugins
    const val gradleVersionsPlugin = "0.39.0"
    const val googleServicesVersion = "4.3.8"
    const val firebaseCrashlyticsPlugin = "2.7.1"
    const val firebasePerformancePlugin = "1.4.0"

    // Platforms
    const val firebasePlatform = "28.2.1"

    // Libs
    const val aboutLibraries = "8.9.0"
    const val androidXAppCompat = "1.4.0-alpha03"
    const val androidXBrowser = "1.3.0"
    const val androidXCompose = "1.0.0-rc02"
    const val androidXConstraintLayout = "2.1.0-beta02"
    const val androidXCore = "1.7.0-alpha01"
    const val androidXFragment = "1.4.0-alpha04"
    const val androidXHilt = "1.0.0"
    const val androidXLifecycle = "2.4.0-alpha02"
    const val androidXNavigation = "2.4.0-alpha04"
    const val androidXPreference = "1.1.1"
    const val androidXRecyclerView = "1.2.1"
    const val androidXRecyclerViewSelection = "1.2.0-alpha01"
    const val androidXRoom = "2.4.0-alpha03"
    const val androidXStartup = "1.1.0-beta01"
    const val androidXWorkManager = "2.7.0-alpha04"
    const val daggerHilt = "2.37"
    const val flowBinding = "1.2.0"
    const val googlePlayCore = "1.10.0"
    const val googlePlayCoreKtx = "1.8.1"
    const val kotlin = "1.5.10"
    const val kotlinCoroutines = "1.5.1"
    const val kotlinLogging = "2.0.10"
    const val leakCanary = "2.7"
    const val materialComponents = "1.4.0"
    const val okhttp = "4.9.1"
    const val okio = "2.10.0"
    const val slf4jAndroidLogger = "1.0.5"

    // Testing
    const val junit = "4.13.2"
    const val robolectric = "4.6.1"
    const val mockk = "1.12.0"
}

object Config {
    const val min_sdk = 21
    const val target_sdk = 31
    const val compile_sdk = 31
    const val build_tools = "30.0.3"
}

object Plugins {
    const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val navigation_safeargs = "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.androidXNavigation}"
    const val google_services_gradle = "com.google.gms:google-services:${Versions.googleServicesVersion}"
    const val versions_gradle = "com.github.ben-manes:gradle-versions-plugin:${Versions.gradleVersionsPlugin}"
    const val firebaseCrashlyticsPlugin =
        "com.google.firebase:firebase-crashlytics-gradle:${Versions.firebaseCrashlyticsPlugin}"
    const val firebasePerformancePlugin = "com.google.firebase:perf-plugin:${Versions.firebasePerformancePlugin}"
    const val daggerHiltPlugin = "com.google.dagger:hilt-android-gradle-plugin:${Versions.daggerHilt}"
    const val aboutLibrariesPlugin = "com.mikepenz.aboutlibraries.plugin:aboutlibraries-plugin:${Versions.aboutLibraries}"
}

object Libs {
    // AboutLibraries
    const val about_libraries = "com.mikepenz:aboutlibraries:${Versions.aboutLibraries}"

    // AndroidX
    const val androidx_appcompat = "androidx.appcompat:appcompat:${Versions.androidXAppCompat}"
    const val androidx_browser = "androidx.browser:browser:${Versions.androidXBrowser}"
    const val androidx_preference = "androidx.preference:preference:${Versions.androidXPreference}"
    const val androidx_constraint_layout =
        "androidx.constraintlayout:constraintlayout:${Versions.androidXConstraintLayout}"
    const val androidx_fragment = "androidx.fragment:fragment-ktx:${Versions.androidXFragment}"
    const val androidx_hilt_work = "androidx.hilt:hilt-work:${Versions.androidXHilt}"
    const val androidx_hilt_compiler = "androidx.hilt:hilt-compiler:${Versions.androidXHilt}"
    const val androidx_core = "androidx.core:core-ktx:${Versions.androidXCore}"

    const val androidx_lifecycle_viewmodel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.androidXLifecycle}"
    const val androidx_lifecycle_livedata = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.androidXLifecycle}"
    const val androidx_lifecycle_java8 = "androidx.lifecycle:lifecycle-common-java8:${Versions.androidXLifecycle}"
    const val androidx_lifecycle_process = "androidx.lifecycle:lifecycle-process:${Versions.androidXLifecycle}"
    const val androidx_lifecycle_runtime = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.androidXLifecycle}"

    const val androidx_navigation_fragment = "androidx.navigation:navigation-fragment-ktx:${Versions.androidXNavigation}"
    const val androidx_navigation_ui = "androidx.navigation:navigation-ui-ktx:${Versions.androidXNavigation}"
    const val androidx_recyclerview = "androidx.recyclerview:recyclerview:${Versions.androidXRecyclerView}"
    const val androidx_recyclerview_selection =
        "androidx.recyclerview:recyclerview-selection:${Versions.androidXRecyclerViewSelection}"
    const val androidx_room_runtime = "androidx.room:room-runtime:${Versions.androidXRoom}"
    const val androidx_room_ktx = "androidx.room:room-ktx:${Versions.androidXRoom}"
    const val androidx_room_compiler = "androidx.room:room-compiler:${Versions.androidXRoom}"
    const val androidx_startup = "androidx.startup:startup-runtime:${Versions.androidXStartup}"
    const val androidx_work_runtime = "androidx.work:work-runtime-ktx:${Versions.androidXWorkManager}"
    const val androidx_work_gcm = "androidx.work:work-gcm:${Versions.androidXWorkManager}"

    // Android Material Components
    const val material_components = "com.google.android.material:material:${Versions.materialComponents}"

    // Dagger
    const val daggerHiltAndroid = "com.google.dagger:hilt-android:${Versions.daggerHilt}"
    const val daggerHiltAndroidCompiler = "com.google.dagger:hilt-android-compiler:${Versions.daggerHilt}"

    // Firebase
    const val firebaseAnalytics = "com.google.firebase:firebase-analytics"
    const val firebaseCrashlytics = "com.google.firebase:firebase-crashlytics"

    // FlowBinding
    const val flowBindingAndroid = "io.github.reactivecircus.flowbinding:flowbinding-android:${Versions.flowBinding}"
    const val flowBindingCommon = "io.github.reactivecircus.flowbinding:flowbinding-common:${Versions.flowBinding}"
    const val flowBindingMaterial = "io.github.reactivecircus.flowbinding:flowbinding-material:${Versions.flowBinding}"

    // Google Play
    const val googlePlayCore = "com.google.android.play:core:${Versions.googlePlayCore}"
    const val googlePlayCoreKtx = "com.google.android.play:core-ktx:${Versions.googlePlayCoreKtx}"

    // Kotlin
    const val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib"

    // Kotlin Coroutines
    const val kotlinCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinCoroutines}"

    // LeakCanary
    const val leakCanary = "com.squareup.leakcanary:leakcanary-android:${Versions.leakCanary}"
    const val leakCanaryPlumberAndroid = "com.squareup.leakcanary:plumber-android:${Versions.leakCanary}"

    // Logging
    const val slf4jAndroidLogger = "de.psdev.slf4j-android-logger:slf4j-android-logger:${Versions.slf4jAndroidLogger}"
    const val kotlinLogging = "io.github.microutils:kotlin-logging:${Versions.kotlinLogging}"

    // OkHttp
    const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"

    // Okio
    const val okio = "com.squareup.okio:okio:${Versions.okio}"

    // Testing
    const val junit = "junit:junit:${Versions.junit}"
    const val robolectric = "org.robolectric:robolectric:${Versions.robolectric}"
    const val mockk = "io.mockk:mockk:${Versions.mockk}"
}

object Platforms {
    const val firebase = "com.google.firebase:firebase-bom:${Versions.firebasePlatform}"
    const val kotlin = "org.jetbrains.kotlin:kotlin-bom:${Versions.kotlin}"
}