plugins {
    // 给 buildSrc 添加 kotlin-dsl 支持
    `kotlin-dsl`
    // groovy 支持
    // id 'groovy-gradle-plugin'id 'groovy-gradle-plugin'
}

group = "com.github.hanlyjiang.gradle"
version = "1.0.0"

repositories {
    maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
    maven { setUrl("https://maven.aliyun.com/repository/google") }
    maven { setUrl("https://repo.huaweicloud.com/repository/maven") }
}

dependencies {
    // 添加gradleApi
    implementation(gradleApi())
    // 添加 Android Gradle Api
    implementation("com.android.tools.build:gradle:7.0.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}