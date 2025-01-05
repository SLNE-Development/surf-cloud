plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "surf-cloud"

val ci = System.getenv("CI")?.toBoolean() == true

include(":docs")

include("surf-cloud-core")
include("surf-cloud-bukkit")
include("surf-cloud-velocity")
include("surf-cloud-standalone")
include("surf-cloud-standalone-launcher")

include("surf-cloud-api:surf-cloud-api-common")
findProject(":surf-cloud-api:surf-cloud-api-common")?.name = "surf-cloud-api-common"

include("surf-cloud-api:surf-cloud-api-client")
findProject(":surf-cloud-api:surf-cloud-api-client")?.name = "surf-cloud-api-client"

include("surf-cloud-api:surf-cloud-api-server")
findProject(":surf-cloud-api:surf-cloud-api-server")?.name = "surf-cloud-api-server"

include("surf-cloud-core:surf-cloud-core-common")
findProject(":surf-cloud-core:surf-cloud-core-common")?.name = "surf-cloud-core-common"

include("surf-cloud-core:surf-cloud-core-client")
findProject(":surf-cloud-core:surf-cloud-core-client")?.name = "surf-cloud-core-client"
