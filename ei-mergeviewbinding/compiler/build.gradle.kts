plugins {
    alias(libs.plugins.jetbrainsKotlinJvm)
}

dependencies {
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
    implementation(libs.ksp)
    implementation(project(":ei-mergeviewbinding:annotation"))

    testImplementation("com.bennyhuo.kotlin:kotlin-compile-testing-extensions:1.9.20-1.3.0")
    testImplementation(project(":ei-mergeviewbinding:annotation"))
    testImplementation(project(":ei-mergeviewbinding:compiler"))
    testImplementation(kotlin("test-annotations-common"))
    testImplementation(kotlin("test-junit"))
}