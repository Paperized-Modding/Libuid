pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.momirealms.net/releases/")
    }
}

rootProject.name = "Libuid"
