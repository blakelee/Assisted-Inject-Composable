plugins {
    kotlin("jvm")
    id("java-library")
}

// Versions are declared in 'gradle.properties' file
val kspVersion: String by project

dependencies {
    api(project(":annotations"))
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
}