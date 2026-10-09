plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.nyx.themes"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nyx.themes"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "1.1.0"
    }

    // Personal sideload key, kept in the repo so every build (here or on GitHub) is signed the same way
    // and a new APK installs over the old one. Not a secret worth protecting.
    signingConfigs {
        create("release") {
            storeFile = file("nyx-release.jks")
            storePassword = "nyxnyx"
            keyAlias = "nyx"
            keyPassword = "nyxnyx"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    lint {
        abortOnError = true
        warningsAsErrors = false
        checkReleaseBuilds = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.systemProperty("robolectric.dependency.repo.url", "https://maven-central.storage-download.googleapis.com/maven2")
                it.systemProperty("robolectric.dependency.repo.id", "central-mirror")
                it.systemProperty("robolectric.graphicsMode", "NATIVE")
                it.maxHeapSize = "2g"
            }
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("androidx.test.ext:junit:1.2.1")
}
