rootProject.name = "Glyph"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":composeApp")
include(":core:navigation")
include(":core:design")
include(":core:utils")
include(":core:model")
include(":core:string-resources")
include(":feature:screen:home")
include(":core:datastore")
include(":core:network")
include(":core:auth")
include(":feature:screen:auth")
include(":feature:screen:profile")
