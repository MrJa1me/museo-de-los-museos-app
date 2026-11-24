import java.util.Properties

plugins {
    jacoco
    alias(libs.plugins.legacy.kapt)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.museo_de_los_museos_app"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.museo_de_los_museos_app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Dirección de los microservicios. 10.0.2.2 es el "localhost" del PC visto desde el emulador.
        // En un teléfono físico se usa la IP del PC en la red Wi-Fi:  ./gradlew assembleDebug -PmsHost=192.168.1.20
        // (o se agrega msHost=192.168.1.20 en gradle.properties)
        val msHost = (project.findProperty("msHost") as String?) ?: "10.0.2.2"
        buildConfigField("String", "URL_MS_MUSEOS", "\"http://$msHost:8081/\"")
        buildConfigField("String", "URL_MS_PEDIDOS", "\"http://$msHost:8082/\"")
        // API externa pública (sin clave): Art Institute of Chicago
        buildConfigField("String", "URL_API_OBRAS", "\"https://api.artic.edu/\"")
    }

    // Firma del APK de release con la llave del proyecto (app/keystore/museo-release.jks).
    // Los datos se leen de app/keystore/keystore.properties para no escribir contraseñas en este archivo.
    signingConfigs {
        create("release") {
            val propiedades = Properties()
            val archivo = file("keystore/keystore.properties")
            if (archivo.exists()) archivo.inputStream().use { propiedades.load(it) }
            storeFile = file(propiedades.getProperty("storeFile", "keystore/museo-release.jks"))
            storePassword = propiedades.getProperty("storePassword")
            keyAlias = propiedades.getProperty("keyAlias")
            keyPassword = propiedades.getProperty("keyPassword")
        }
    }

    buildTypes {
        debug {
            // JaCoCo mide qué líneas ejecutan las pruebas unitarias (reporte: ./gradlew coberturaLogica)
            enableUnitTestCoverage = true
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Persistencia SQLite con Room (Guía SQLitePersonas)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.ui.graphics)
    kapt(libs.androidx.room.compiler)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Base del equipo: navegación, MVVM, persistencia, adaptabilidad
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.compose.material3.window.size)
    implementation(libs.androidx.compose.material.icons.extended)

    // Evaluación 4: consumo de microservicios propios y API externa (Retrofit + Gson), imágenes remotas (Coil)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Recursos nativos (P3): mapa (osmdroid) y lectura de QR (Google Code Scanner)
    implementation(libs.osmdroid.android)
    implementation(libs.play.services.code.scanner)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    // MockK: simula las interfaces Retrofit y los DAO de Room en las pruebas unitarias
    testImplementation(libs.mockk)
    // org.json real para pruebas unitarias en el PC (en Android ya viene incluido)
    testImplementation(libs.org.json)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}


// ---------------------------------------------------------------------------------------------
// Cobertura de la lógica (Evaluación 4: pruebas que cubran al menos el 80 % de la lógica).
//   ./gradlew coberturaLogica   →  app/build/reports/jacoco/coberturaLogica/html/index.html
// Mide ViewModels, repositorios, capa remota (Retrofit), dominio y modelos.
// No mide la UI en Compose (se prueba en el emulador) ni el código declarativo de Room
// (entidades, DAO y las clases que genera Room), que no tiene lógica propia.
// ---------------------------------------------------------------------------------------------
val excluidosDeCobertura = listOf(
    "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
    "**/repository/db/**",          // Room: entidades, DAO y código generado (*_Impl)
    "**/remote/ClienteHttp*",       // solo construye los clientes Retrofit/OkHttp
    "**/repository/Repositorios*",  // localizador de servicios: arma las dependencias al iniciar
    "**/repository/AppDataStore*",  // declaración del archivo de DataStore
    "**/*\$\$serializer*", "**/*ComposableSingletons*"
)
val paquetesDeLogica = listOf("domain", "viewmodel", "repository", "remote", "model")
    .map { "com/example/museo_de_los_museos_app/$it/**" }

tasks.register<JacocoReport>("coberturaLogica") {
    group = "verification"
    description = "Ejecuta las pruebas unitarias y genera el reporte de cobertura de la lógica de la app."
    dependsOn("testDebugUnitTest")
    val clasesKotlin = layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")
    classDirectories.setFrom(clasesKotlin.map { dir ->
        fileTree(dir) {
            include(paquetesDeLogica)
            exclude(excluidosDeCobertura)
        }
    })
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(layout.buildDirectory.file("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"))
    reports {
        html.required.set(true)
        xml.required.set(true)
        csv.required.set(true)
    }
}
