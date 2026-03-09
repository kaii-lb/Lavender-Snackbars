import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.compose.multiplatform)
}

group = "io.github.kaii-lb.lavender"
version = "0.3.0"

kotlin {
    androidLibrary {
        namespace = "io.github.kaii_lb.lavender.snackbars"
        compileSdk = 36
        minSdk = 24
        androidResources.enable = true

        withJava()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.appcompat)
            implementation(libs.material)
            implementation(libs.androidx.foundation)
            implementation(libs.androidx.material3)
            implementation(libs.androidx.runtime)
            implementation(libs.androidx.foundation.layout)
            implementation(libs.androidx.ui)
            implementation(libs.androidx.animation)
            implementation(libs.androidx.ui.tooling.preview)
            implementation(libs.androidx.ui.tooling)
            implementation(libs.compose.components.resources)
            implementation(libs.aakira.napier)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "snackbars", version.toString())

    pom {
        name = "Lavender Immich Integration"
        description = "A cross platform library for Immich integration"
        inceptionYear = "2025"
        url = "https://github.com/kaii-lb/Lavender-Snackbars"

        licenses {
            license {
                name = "GNU GENERAL PUBLIC LICENSE, Version 3.0"
                url = "https://www.gnu.org/licenses/lgpl-3.0.txt"
                distribution = "https://www.gnu.org/licenses/lgpl-3.0.txt"
            }
        }

        developers {
            developer {
                id = "kaii-lb"
                name = "kaii-lb"
                url = "https://github.com/kaii-lb"
                email = "kaiilbbusiness@gmail.com"
                organization = "kaii-lb"
                organizationUrl = "https://github.com/kaii-lb"
            }
        }

        scm {
            url = "https://github.com/kaii-lb/Lavender-Snackbars"
            connection = "scm:git:git://github.com/kaii-lb/Lavender-Snackbars.git"
            developerConnection = "scm:git:ssh://git@github.com/kaii-lb/Lavender-Snackbars.git"
        }
    }
}