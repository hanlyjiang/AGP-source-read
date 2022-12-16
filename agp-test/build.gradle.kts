plugins {
    id("java")
    kotlin("jvm") // version "1.6.20" // 加入classpath 之后，不再需要version
}

group = "com.github.hanlyjiang"
version = "1.0.0"

repositories {
    maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
    maven { setUrl("https://maven.aliyun.com/repository/google") }
    maven { setUrl("https://repo.huaweicloud.com/repository/maven") }
}

dependencies {
    implementation(gradleApi())
    implementation("com.android.tools.build:gradle:7.0.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

logger.lifecycle("hasAndroidPlugin: ${pluginManager.hasPlugin("com.android.base")}")