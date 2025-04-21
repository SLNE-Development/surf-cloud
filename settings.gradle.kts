plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}
rootProject.name = "surf-cloud"

val ci = System.getenv("CI")?.toBoolean() == true

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

include("surf-cloud-api:surf-cloud-api-client:surf-cloud-api-client-common")
findProject(":surf-cloud-api:surf-cloud-api-client:surf-cloud-api-client-common")?.name = "surf-cloud-api-client-common"

include("surf-cloud-api:surf-cloud-api-client:surf-cloud-api-client-paper")
findProject(":surf-cloud-api:surf-cloud-api-client:surf-cloud-api-client-paper")?.name = "surf-cloud-api-client-paper"

include("surf-cloud-api:surf-cloud-api-client:surf-cloud-api-client-velocity")
findProject(":surf-cloud-api:surf-cloud-api-client:surf-cloud-api-client-velocity")?.name = "surf-cloud-api-client-velocity"

if (!ci) {
    include(":surf-cloud-test-plugin:surf-cloud-test-standalone")
}

