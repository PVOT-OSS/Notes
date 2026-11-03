// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

plugins {
    alias(libs.plugins.pvot.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android { namespace = "org.prauga.notes.core.data" }

room { schemaDirectory("$projectDir/schemas") }

dependencies {
    api(project(":core:model"))
    implementation(libs.room.runtime)
    implementation(libs.kotlinx.coroutines.android)
    ksp(libs.room.compiler)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
