plugins {
    kotlin("jvm")
    id("java-library")
}

// Versions are declared in 'gradle.properties' file
val kspVersion: String by project

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
}