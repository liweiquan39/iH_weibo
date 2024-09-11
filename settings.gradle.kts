pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven ("https://www.jitpack.io")
        maven ("https://maven.aliyun.com/nexus/content/groups/public/")
        maven ("https://maven.aliyun.com/nexus/content/repositories/jcenter")
        maven ( "https://maven.aliyun.com/nexus/content/repositories/google")
        maven ( "https://maven.aliyun.com/nexus/content/repositories/gradle-plugin")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven ("https://www.jitpack.io")
        maven ("https://maven.aliyun.com/nexus/content/groups/public/")
        maven ("https://maven.aliyun.com/nexus/content/repositories/jcenter")
        maven ( "https://maven.aliyun.com/nexus/content/repositories/google")
        maven ( "https://maven.aliyun.com/nexus/content/repositories/gradle-plugin")
    }
}

rootProject.name = "weibo_liweiquan"
include(":app")
 