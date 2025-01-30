import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot")
	id("io.spring.dependency-management")
	kotlin("jvm")
	kotlin("plugin.spring")
}

java.sourceCompatibility = JavaVersion.VERSION_17

val dgsVersion: String by project
val dgsTestVersion: String by project
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation(platform("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:$dgsVersion"))
    implementation("com.netflix.graphql.dgs:graphql-dgs-spring-graphql-starter")
    implementation("org.springframework.data:spring-data-commons")
    testImplementation("com.netflix.graphql.dgs:dgs-starter-test:$dgsTestVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.0")
    // Kotest 메인 의존성
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")

    // Spring Boot 통합 테스트를 위한 의존성
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.3")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
