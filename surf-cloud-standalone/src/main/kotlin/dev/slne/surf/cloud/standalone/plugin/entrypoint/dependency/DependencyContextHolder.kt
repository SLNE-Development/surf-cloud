package dev.slne.surf.cloud.standalone.plugin.entrypoint.dependency

import dev.slne.surf.cloud.api.server.server.plugin.dependency.DependencyContext

interface DependencyContextHolder {

    fun setContext(context: DependencyContext)
}