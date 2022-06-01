
rootProject.name = "agp-source-read"

pluginManagement {
    repositories {
        maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { setUrl("https://repo.huaweicloud.com/repository/maven") }
        gradlePluginPortal()
    }
}

buildscript {
    repositories {
        maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { setUrl("https://repo.huaweicloud.com/repository/maven") }
    }
}

