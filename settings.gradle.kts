pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal(); maven("https://jitpack.io") } }
dependencyResolutionManagement { repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS); repositories { google(); mavenCentral(); maven("https://jitpack.io") } }
rootProject.name = "HAM"
include(":app", ":Bcore", ":black-reflection", ":compiler")
project(":Bcore").projectDir = file("engine/blackbox/Bcore")
project(":black-reflection").projectDir = file("engine/blackbox/black-reflection")
project(":compiler").projectDir = file("engine/blackbox/compiler")
