@file:OptIn(InternalPluginApi::class)

package dev.slne.surf.cloud.api.server.server.plugin.loader.library.impl

import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.api.server.server.plugin.InternalPluginApi
import dev.slne.surf.cloud.api.server.server.plugin.loader.library.ClassPathLibrary
import dev.slne.surf.cloud.api.server.server.plugin.loader.library.LibraryLoadingException
import dev.slne.surf.cloud.api.server.server.plugin.loader.library.LibraryStore
import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.repository.RepositoryPolicy
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.resolution.DependencyResolutionException
import org.eclipse.aether.spi.connector.transport.http.HttpTransporterFactory
import org.eclipse.aether.supplier.RepositorySystemSupplier
import org.eclipse.aether.transfer.AbstractTransferListener
import org.eclipse.aether.transfer.TransferEvent

class MavenLibraryResolver : ClassPathLibrary {
    private val log = logger()

    private val repository: RepositorySystem = RepositorySystemSupplier().get()
//    private val session = repository.createSessionBuilder().apply {
//        setSystemProperties(System.getProperties())
//        setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_FAIL)
//        withLocalRepositories(LocalRepository("libraries"))
//        setTransferListener(object : AbstractTransferListener() {
//            override fun transferInitiated(event: TransferEvent) {
//                log.atInfo()
//                    .log(
//                        "Downloading %s",
//                        event.resource.repositoryUrl + event.resource.resourceName
//                    )
//            }
//
//            override fun transferFailed(event: TransferEvent) {
//                log.atSevere().log(
//                    "Failed to download %s from %s",
//                    event.resource.resourceName,
//                    event.resource.repositoryUrl
//                )
//            }
//        })
//    }.build()

    private val session = MavenRepositorySystemUtils.newSession().apply {
        setSystemProperties(System.getProperties())
        setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_FAIL)
        setLocalRepositoryManager(repository.newLocalRepositoryManager(this, LocalRepository("libraries")))
        setTransferListener(object : AbstractTransferListener() {
            override fun transferInitiated(event: TransferEvent) {
                log.atInfo()
                    .log(
                        "Downloading %s",
                        event.resource.repositoryUrl + event.resource.resourceName
                    )
            }

            override fun transferFailed(event: TransferEvent) {
                log.atSevere().log(
                    "Failed to download %s from %s",
                    event.resource.resourceName,
                    event.resource.repositoryUrl
                )
            }
        })
        setReadOnly()
    }

    private val repositories = mutableObjectListOf<RemoteRepository>()
    private val dependencies = mutableObjectListOf<Dependency>()

    fun addDependency(dependency: Dependency) {
        dependencies.add(dependency)
    }

    fun addDependency(dependency: String) {
        val splitted = dependency.split(':')

        require(splitted.size == 3) { "Invalid dependency format: $dependency" }

        val (groupId, artifactId, version) = splitted
        addDependency(groupId, artifactId, version)
    }

    fun addDependency(groupId: String, artifactId: String, version: String) {
        addDependency(
            Dependency(
                DefaultArtifact("$groupId:$artifactId:$version"),
                null
            )
        )
    }

    fun addRepository(repository: RemoteRepository) {
        repositories.add(repository)
    }

    fun addMavenCentral() {
        repositories.add(
            RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/")
                .build()
        )
    }

    fun addJitpack() {
        repositories.add(
            RemoteRepository.Builder("jitpack", "default", "https://jitpack.io")
                .build()
        )
    }

    fun addJCenter() {
        repositories.add(
            RemoteRepository.Builder("jcenter", "default", "https://jcenter.bintray.com/")
                .build()
        )
    }

    fun addGoogle() {
        repositories.add(
            RemoteRepository.Builder("google", "default", "https://maven.google.com/")
                .build()
        )
    }

    fun addGradlePluginPortal() {
        repositories.add(
            RemoteRepository.Builder(
                "gradle-plugin-portal",
                "default",
                "https://plugins.gradle.org/m2/"
            )
                .build()
        )
    }

    fun addDefaultRepositories() {
        addMavenCentral()
        addJitpack()
        addJCenter()
        addGoogle()
        addGradlePluginPortal()
    }

    override fun register(store: LibraryStore) {
        val repos = repository.newResolutionRepositories(session, repositories)

        val result = try {
            repository.resolveDependencies(
                session,
                DependencyRequest(CollectRequest(null as Dependency?, dependencies, repos), null)
            )
        } catch (e: DependencyResolutionException) {
            println("Error resolving libraries")
            println(e.result.collectExceptions)
            println(e.result.artifactResults)
            println(e.result.request)

            throw LibraryLoadingException("Error resolving libraries", e)
        }

        for (result in result.artifactResults) {
            store.addLibrary(result.artifact.path)
        }
    }
}