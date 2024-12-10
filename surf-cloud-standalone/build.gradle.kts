plugins {
//    kotlin("plugin.jpa") version "2.0.21"
    `core-convention`
}

dependencies {
    api(project(":surf-cloud-core:surf-cloud-core-common"))
    api(project(":surf-cloud-api:surf-cloud-api-server"))

    implementation(platform("org.springframework.shell:spring-shell-dependencies:3.3.3"))

    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.springframework.boot:spring-boot-starter-data-redis")
//    api("org.springframework.shell:spring-shell-starter")
    api("dev.slne.surf:surf-api-standalone:1.21+")
    api("org.reactivestreams:reactive-streams:1.0.4")
    api("io.lettuce:lettuce-core")
    api(libs.velocity.native)
}

//dependencyManagement {
//    imports {
//        mavenBom("org.springframework.shell:spring-shell-dependencies:3.3.2")
//    }
//}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "dev.slne.surf.cloud.standalone.launcher.Launcher"
            attributes["Real-Main-Class"] = "dev.slne.surf.cloud.standalone.Bootstrap"
        }
    }
}