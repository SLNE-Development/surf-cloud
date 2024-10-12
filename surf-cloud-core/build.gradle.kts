dependencies {
    api(project(":surf-cloud-api")) {
        exclude(group = "ch.qos.logback", module = "logback-classic")
    }
    // https://mvnrepository.com/artifact/org.aspectj/aspectjweaver
    runtimeOnly("org.aspectj:aspectjweaver:1.9.22.1")
}