plugins {
    val kotlinVersion = "1.7.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.14.0"
}

group = "top.mrxiaom"
version = "0.1.6"

repositories {
    maven("https://repo.huaweicloud.com/repository/maven")
    mavenCentral()
}
