plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {

    androidLibrary {
        namespace = "ru.glyph.share_bottom_sheet"
        compileSdk = 36
        minSdk = libs.versions.android.minSdk.get().toInt()
        experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true
    }

    val xcfName = "feature:share-bottom-sheetKit"

    iosX64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.bundles.compose)
                implementation(libs.bundles.koin)
                implementation(libs.coroutines)
                implementation(libs.viewmodel)

                implementation(projects.core.navigation)
                implementation(projects.core.design)
                implementation(projects.core.model)
                implementation(projects.core.database)
                implementation(projects.core.stringResources)
                implementation(projects.core.utils)
                implementation(projects.core.network)

                implementation(libs.kotlinxSerializationCore)
                implementation(libs.ktor.client.core)
            }
        }

        androidMain {
            dependencies {
            }
        }

        iosMain {
            dependencies {
            }
        }
    }

}