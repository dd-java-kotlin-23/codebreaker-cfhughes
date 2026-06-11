/*
 *  Copyright 2026 CNM Ingenuity, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka)
    jacoco
    alias(libs.plugins.openapi)
}

val javaVersion = libs.versions.java.get()
val targetDir = layout
    .buildDirectory
    .dir(properties["outputBaseDir"] as String)
    .get()
    .asFile
    .toString()

kotlin {
    jvmToolchain(javaVersion.toInt())
    sourceSets {
        main {
            kotlin.srcDir("$targetDir/src/main/kotlin")
        }
    }
}

dependencies {

    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.jdk8)

    implementation(libs.okhttp)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.logging.interceptor)

    implementation(libs.moshi.core)
    implementation(libs.moshi.kotlin)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.aggregator)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.junit.platform)
}

val commonOptions = mapOf(
    "collectionType" to properties["collectionType"] as String,
    "dateLibrary" to properties["dateLibrary"] as String,
    "serializationLibrary" to properties["serializationLibrary"] as String,
    "useCoroutines" to properties["useCoroutines"] as String,
    "enumPropertyNaming" to properties["enumPropertyNaming"] as String,
    "hideGenerationTimestamp" to "true",
    "openApiNullable" to "false",
    "skipIfSpecIsUnchanged" to "true"
)

val openApiGlobalProperties = mapOf(
    "apiDocs" to "false",
    "apiTests" to "false",
    "apis" to "false",
    "modelDocs" to "false",
    "modelTests" to "false",
    "models" to "false",
    "supportingFile" to "false"
)

tasks.register<GenerateTask>("openApiGenerateModels") {
    configOptions = commonOptions
    globalProperties = openApiGlobalProperties + mapOf(
        "models" to "",
        "modelDocs" to "",
    )
}

tasks.register<GenerateTask>("openApiGenerateApis") {
    configOptions = commonOptions + mapOf(
        "useTags" to "true"
    )
    globalProperties = openApiGlobalProperties + mapOf(
        "apis" to "",
        "apiDocs" to "",
        "supportingFiles" to "CollectionFormats.kt"
    )
}

tasks.withType<GenerateTask> {

    group = "openapi tools"

    generatorName = properties["generatorName"] as String
    inputSpec.set("$projectDir/spec/${properties["openApiSpec"]}")
    outputDir.set(targetDir)
    val basePackageName: String by project
    apiPackage = "$basePackageName.web"
    modelPackage = "$basePackageName.dto"
    library = properties["httpLibrary"] as String

}

tasks.openApiGenerate {
    enabled = false
}

tasks.compileKotlin {
    dependsOn(tasks.named("openApiGenerateModels"))
    dependsOn(tasks.named("openApiGenerateApis"))
    compilerOptions {
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events.addAll(setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED, TestLogEvent.PASSED))
    }
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}

tasks.javadoc {
    with(options as StandardJavadocDocletOptions) {
        links("https://docs.oracle.com/en/java/javase/${javaVersion}/docs/api/")
    }
}

