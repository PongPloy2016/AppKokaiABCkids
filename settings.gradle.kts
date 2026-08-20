pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "PaintApp"
include(":app")
// include(":videowatchfeature") // Legacy module
include(":videowatchfeatureSupabase") // Active Supabase module
include(":core")
include(":custom-ui")
include(":livefeature")
include(":kidswebviewfeature")


