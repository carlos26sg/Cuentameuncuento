// Fichero de build raíz: solo declara los plugins que usan los módulos.
buildscript {
    dependencies {
        // AGP 9 trae Kotlin integrado; aquí fijamos la versión del compilador
        classpath(libs.kotlin.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}
