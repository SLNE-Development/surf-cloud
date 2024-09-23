dependencies {
    api(project(":surf-cloud-core"))
    implementation(platform("org.springframework.shell:spring-shell-dependencies:3.3.2"))

    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.springframework.boot:spring-boot-starter-data-redis")
    api("org.springframework.shell:spring-shell-starter")
    api("dev.slne.surf:surf-api-standalone:1.21+")
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