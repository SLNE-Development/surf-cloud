package dev.slne.surf.cloud.standalone.event.server

import dev.slne.surf.cloud.api.common.event.CloudEvent
import dev.slne.surf.cloud.core.common.server.CommonCloudServerImpl

class CloudServerRegisteredEvent(source: Any, val server: CommonCloudServerImpl) :
    CloudEvent(source)