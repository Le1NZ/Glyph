plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {

    androidLibrary {
        namespace = "ru.glyph.sync"
        compileSdk = 36
        minSdk = 26
    }

    val xcfName = "core:syncKit"

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
                implementation(libs.bundles.koin)
                implementation(libs.coroutines)
                implementation(libs.kotlinxSerializationCore)
                implementation(libs.ktor.client.core)

                implementation(projects.core.database)
                implementation(projects.core.network)
            }
        }
    }
}
