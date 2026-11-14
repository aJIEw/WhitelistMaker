import java.util.Properties
import org.gradle.api.tasks.Sync

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

val appApplicationId = "com.ajiew.whitelistmaker"
val appVersionCode = 3
val appVersionName = "1.0.3"
val releaseArtifactName = "${appApplicationId}_v${appVersionName}_${appVersionCode}.apk"

val releaseSigningProperties = Properties().apply {
    val propertiesFile = rootProject.file("keystore.properties")
    if (propertiesFile.isFile) {
        propertiesFile.inputStream().use(::load)
    }
}

fun releaseSigningValue(name: String): String? =
    System.getenv(name)?.takeIf(String::isNotBlank)
        ?: releaseSigningProperties.getProperty(name)?.takeIf(String::isNotBlank)

val releaseSigningPropertyNames = listOf(
    "RELEASE_KEYSTORE_FILE",
    "RELEASE_STORE_PASSWORD",
    "RELEASE_KEY_ALIAS",
    "RELEASE_KEY_PASSWORD",
    "RELEASE_STORE_TYPE",
)
val missingReleaseSigningProperties = releaseSigningPropertyNames.filter {
    releaseSigningValue(it) == null
}
val releaseKeystoreFile = releaseSigningValue("RELEASE_KEYSTORE_FILE")
    ?.let(rootProject::file)
val releaseSigningError = when {
    missingReleaseSigningProperties.isNotEmpty() ->
        "Missing release signing properties: ${missingReleaseSigningProperties.joinToString()}"

    releaseKeystoreFile?.isFile != true ->
        "RELEASE_KEYSTORE_FILE does not point to a readable file: $releaseKeystoreFile"

    else -> null
}

android {
    namespace = "com.ajiew.whitelistmaker"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = appApplicationId
        minSdk = 26
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (releaseSigningError == null) {
            create("release") {
                storeFile = requireNotNull(releaseKeystoreFile)
                storePassword = requireNotNull(releaseSigningValue("RELEASE_STORE_PASSWORD"))
                keyAlias = requireNotNull(releaseSigningValue("RELEASE_KEY_ALIAS"))
                keyPassword = requireNotNull(releaseSigningValue("RELEASE_KEY_PASSWORD"))
                storeType = requireNotNull(releaseSigningValue("RELEASE_STORE_TYPE"))
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (releaseSigningError == null) {
                signingConfig = signingConfigs.getByName("release")
            }
            ndk {
                abiFilters += "arm64-v8a"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

val releaseApk = layout.buildDirectory.file("outputs/apk/release/app-release.apk")
val distributionDirectory = layout.buildDirectory.dir("outputs/distribution")

val packageReleaseApk = tasks.register<Sync>("packageReleaseApk") {
    group = "build"
    description = "Builds and packages the signed arm64-v8a release APK for distribution."
    dependsOn("assembleRelease")
    outputs.upToDateWhen { false }

    from(releaseApk) {
        rename { releaseArtifactName }
    }
    into(distributionDirectory)

    doFirst {
        check(releaseSigningError == null) {
            releaseSigningError ?: "Release signing is unavailable."
        }
        check(releaseApk.get().asFile.isFile) {
            "Expected signed release APK was not produced: ${releaseApk.get().asFile}"
        }
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
