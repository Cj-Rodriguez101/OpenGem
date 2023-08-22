import java.util.Properties

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.hiltAndroid)
    //alias(libs.plugins.kspAndroid)
    id("com.google.devtools.ksp")
    kotlin("kapt")
}

android {
    namespace = "com.cjrodriguez.cjchatgpt"
    compileSdk = 34

    defaultConfig {

        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())

        //buildConfigField(type = "String", name = "API_KEY", value = properties.getProperty("API_KEY"))
        buildConfigField(type = "String", name = "API_KEY", value = "\"${properties.getProperty("API_KEY")}\"")
        applicationId = "com.cjrodriguez.cjchatgpt"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        //buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(libs.runtime.compose)
    implementation(libs.extended.icons)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.accompanist)
    implementation(libs.constraint)
    implementation(libs.nav.hilt)
    implementation(libs.navigation)
    implementation(libs.foundation)
    implementation(libs.immutable)

    //open ai & ktor
    implementation(platform(libs.client.bom))
    implementation(libs.ai.client)
    runtimeOnly(libs.ktor)
    testImplementation(libs.junit)

    //room
    implementation(libs.room.runtime)
    implementation(libs.room.coroutines)
    implementation(libs.room.paging)
    annotationProcessor(libs.room.compiler)
    ksp(libs.room.compiler)

    //paging
    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)

    //dataStore
    implementation(libs.dataStore)

    //hilt
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.compose)
    implementation(libs.dagger.hilt)

//    implementation(libs.html.text)

    //unit and instrumentation tests
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}