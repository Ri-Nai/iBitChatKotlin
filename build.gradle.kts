plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    application
}

group = "com.ibit.chat"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin标准库
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    
    // 协程
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // HTTP客户端 - OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // JSON序列化
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    
    // 日志 - 使用与Java 8兼容的版本
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")
    implementation("ch.qos.logback:logback-classic:1.2.11") // 降级到1.2.x版本，兼容Java 8
    
    // 测试
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("com.ibit.chat.MainKt")
} 
