plugins {
    id("java")
}

group = "com.github.hanlyjiang"
version = "1.0.0"

repositories {
    maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
    maven { setUrl("https://maven.aliyun.com/repository/google") }
    maven { setUrl("https://repo.huaweicloud.com/repository/maven") }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    implementation(gradleApi())
    implementation("com.android.tools.build:gradle:7.0.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}