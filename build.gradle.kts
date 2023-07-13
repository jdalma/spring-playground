import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val resilience4jVersion = "1.7.0"

plugins {
	id("org.springframework.boot") version "3.0.3"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("jvm") version "1.7.22"
	kotlin("plugin.spring") version "1.7.22"
	kotlin("plugin.jpa") version "1.7.22"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
//	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.1")
//	implementation("org.mybatis:mybatis:3.5.11")
//	implementation("org.mybatis.dynamic-sql:mybatis-dynamic-sql:1.4.1")

	// resilience4j
	implementation("io.github.resilience4j:resilience4j-circuitbreaker:${resilience4jVersion}")
	implementation("io.github.resilience4j:resilience4j-ratelimiter:${resilience4jVersion}")
	implementation("io.github.resilience4j:resilience4j-retry:${resilience4jVersion}")
	implementation("io.github.resilience4j:resilience4j-bulkhead:${resilience4jVersion}")
	implementation("io.github.resilience4j:resilience4j-cache:${resilience4jVersion}")
	implementation("io.github.resilience4j:resilience4j-timelimiter:${resilience4jVersion}")

	runtimeOnly("com.mysql:mysql-connector-j")


	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter-test:3.0.1")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

ext["jakarta-servlet.version"] = "5.0.0"

tasks.withType<Test> {
	useJUnitPlatform()
}
