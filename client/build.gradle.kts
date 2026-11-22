plugins {
    java
    id("net.weavemc.gradle") version "1.3.3"
}

group = "dev.yaclient"
version = "1.0.0"

weave {
    configure {
        name = "YA Client"
        modId = "yaclient"
        entryPoints = listOf("dev.yaclient.YAClient")
        mcpMappings()
    }
    version("1.8.9")
}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven/")
    maven("https://gitlab.com/api/v4/projects/80566527/packages/maven")
}

dependencies {
    implementation("net.weavemc:loader:1.3.3")
    implementation("net.weavemc:internals:1.3.3")
    implementation("net.weavemc.api:api:1.3.3")
    implementation("net.weavemc.api:api-v1_8:1.3.3")
    compileOnly("org.spongepowered:mixin:0.8.5")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.jar {
    archiveFileName.set("YAClient.jar")
}
