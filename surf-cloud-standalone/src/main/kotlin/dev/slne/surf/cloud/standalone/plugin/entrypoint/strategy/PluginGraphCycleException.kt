package dev.slne.surf.cloud.standalone.plugin.entrypoint.strategy

import it.unimi.dsi.fastutil.objects.ObjectList

class PluginGraphCycleException(val cycles: List<List<String>>): RuntimeException()