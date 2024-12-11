pluginManagement {
    repositories {
        maven(url = "local-plugin-repo")
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MyCodeTreasure"
include(":app")
include(":ei-opengl")
include(":plugin")
include(":ei-datamapper")
include(":ei-datamapper:annotation")
include(":ei-datamapper:compiler")
include(":ei-datamapper:android")
include(":ei-mergeviewbinding:annotation")
include(":ei-mergeviewbinding:android")
include(":ei-mergeviewbinding:compiler")
