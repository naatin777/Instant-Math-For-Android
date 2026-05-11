// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
}

tasks.register("printKotlinVersion") {
    doLast {
        error("Kotlin version: ${org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION}")
    }
}