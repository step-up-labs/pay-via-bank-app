import co.touchlab.skie.configuration.SealedInterop
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.skie)
    alias(libs.plugins.mavenDeployer)
}

group = "io.stepuplabs.pvba"
version = System.getenv("GITHUB_RELEASE_VERSION") ?: "SNAPSHOT"

kotlin {
    androidTarget {
    }

    val xcf = XCFramework("pvba")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "pvba"
            isStatic = true

            xcf.add(this)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.okio)
            implementation(libs.ktor.http)
        }
        androidMain.dependencies {
            implementation(libs.qr.code)
            implementation(libs.androidx.core)
        }
    }
}

android {
    namespace = "io.stepuplabs.pvba"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    publishing {
        singleVariant("release")
    }
}

skie {
    features {
        group {
            SealedInterop.Enabled(true)
        }
    }
}

deployer {
    content {
        androidComponents("release") {
            kotlinSources()
            emptyDocs()
        }
    }
    projectInfo {
        artifactId = "pvba"
        description = "Kotlin Multiplatform library for paying directly via bank app."
        url = "https://github.com/step-up-labs/pay-via-bank-app"
        scm.fromGithub("step-up-labs", "pay-via-bank-app")
        license(MIT)
        developer("David Vávra", "david@stepuplabs.io", "Step Up Labs", "https://stepuplabs.io")
        developer("Radovan Paška", "radovan@stepuplabs.io", "Step Up Labs", "https://stepuplabs.io")
    }
    centralPortalSpec {
        signing.key = secret("MAVEN_CENTRAL_SIGNING_KEY")
        signing.password = secret("MAVEN_CENTRAL_SIGNING_PASSPHRASE")
        auth.user = secret("MAVEN_CENTRAL_UPLOAD_USERNAME")
        auth.password = secret("MAVEN_CENTRAL_UPLOAD_PASSWORD")
        allowMavenCentralSync = false
    }
}