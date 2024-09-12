dependencies {
    api(project(":surf-data-api"))
    implementation("org.mariadb.jdbc:mariadb-java-client")
    // https://mvnrepository.com/artifact/org.aspectj/aspectjweaver
    runtimeOnly("org.aspectj:aspectjweaver:1.9.22.1")
}