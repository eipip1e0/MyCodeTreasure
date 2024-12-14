import com.android.build.gradle.internal.tasks.databinding.DataBindingGenBaseClassesTask
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompileTool

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.ei.mergeviewbinding"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }

    androidComponents {
        onVariants(selector().all()) { variant ->
            afterEvaluate {
                // This is a workaround for https://issuetracker.google.com/301245705 which depends on internal
                // implementations of the android gradle plugin and the ksp gradle plugin which might change in the future
                // in an unpredictable way.
                val dataBindingTask =
                    project.tasks.named("dataBindingGenBaseClasses" + variant.name.capitalize())
                        .get() as DataBindingGenBaseClassesTask

                (project.tasks.getByName("ksp" + variant.name.capitalize() + "Kotlin") as AbstractKotlinCompileTool<*>).setSource(
                    dataBindingTask.sourceOutFolder
                )
            }
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation(project(":ei-mergeviewbinding:annotation"))
    ksp(project(":ei-mergeviewbinding:compiler"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}