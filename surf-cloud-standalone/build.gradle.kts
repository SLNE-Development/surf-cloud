dependencies {
    api(project(":surf-cloud-core"))

    implementation(platform("org.springframework.shell:spring-shell-dependencies:3.3.2"))
    implementation("org.springframework.shell:spring-shell-starter")
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