plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "com.kaii.lavender_snackbars"
    compileSdk = 34

    defaultConfig {
        minSdk = 30

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    buildFeatures {
        compose = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    publishing {
    	singleVariant("release") {
    		withSourcesJar()
    		withJavadocJar()
    	}

    	singleVariant("debug") {
    		withSourcesJar()
    		withJavadocJar()
    	}
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.runtime.android)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.ui.android)
    implementation(libs.androidx.animation.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

val androidSourceJar by tasks.registering(Jar::class) {
	archiveClassifier.set("sources")
	from(android.sourceSets.getByName("main").java.srcDirs)
}

publishing {
	publications {
		create<MavenPublication>("release") {
			groupId = "com.kaii.lavender"
			artifactId = "lavender_snackbars"
			version = "0.1.1"

			afterEvaluate {
				from(components["release"])
			}
		}
	}
}
