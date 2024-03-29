import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.50"
}

allprojects {
    group = "no.nav.helse"
    version = properties["version"] ?: "local-build"
}

val junitJupiterVersion = "5.4.0"
val jacksonVersion = "2.10.0"

val githubUser: String by project
val githubPassword: String by project

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/navikt/inntektsmelding-kontrakt")
            credentials {
                username = githubUser
                password = githubPassword
            }
        }
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))

        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
        implementation("commons-codec:commons-codec:1.11")

        testImplementation("no.nav.sykepenger.kontrakter:inntektsmelding-kontrakt:2019.11.08-09-49-c3234")
        testImplementation("no.nav.syfo.kafka:sykepengesoknad:191a7a91c115dab0038a7063be52dfae34f76c3a")

        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
        testImplementation("org.assertj:assertj-core:3.11.1")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
dependencies {
    implementation(kotlin("stdlib-jdk8"))
}
