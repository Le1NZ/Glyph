import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
val yandexClientId: String = localProperties.getProperty("YANDEX_CLIENT_ID")
    ?: System.getenv("YANDEX_CLIENT_ID")
    ?: ""

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.roomPlugin)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koinAndroid)
            implementation(libs.yandexAuthSdk)
        }
        commonMain.dependencies {
            implementation(libs.bundles.compose)
            implementation(libs.viewmodel)
            implementation(libs.bundles.koin)
            implementation(libs.kotlinxSerializationCore)

            implementation(projects.core.navigation)
            implementation(projects.core.design)
            implementation(projects.core.datastore)
            implementation(projects.core.network)
            implementation(projects.core.auth)
            implementation(projects.feature.screen.home)
            implementation(projects.feature.screen.auth)
            implementation(projects.feature.screen.profile)
            implementation(projects.feature.screen.note)
            implementation(projects.feature.confirmBottomSheet)
            implementation(projects.core.database)
            implementation(projects.core.sync)
        }
    }
}

android {
    namespace = "ru.glyph"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "ru.glyph"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        manifestPlaceholders["YANDEX_CLIENT_ID"] = yandexClientId
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    sourceSets {
        getByName("androidTest") {
            manifest.srcFile("src/androidTest/AndroidManifest.xml")
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
    // KSP for Room (composeApp uses database module, which is compiled separately — no KSP needed here)

    // Tests
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.ktor.client.mock)
}
