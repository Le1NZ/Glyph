plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {

    androidLibrary {
        namespace = "ru.glyph.screen.note"
        experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true
        compileSdk = 36
        minSdk = 26

        withHostTestBuilder {
        }
    }

    val xcfName = "feature:screen:noteKit"

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
                implementation(projects.core.database)
                implementation(projects.core.model)
                implementation(projects.core.utils)
                implementation(projects.core.stringResources)

                implementation(libs.markdownRenderer)
                implementation(libs.markdownRendererM3)
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
