# Copilot Instructions for surf-cloud

## Repository Overview

surf-cloud is a Minecraft cloud/networking system that facilitates communication between Minecraft servers (Paper/Bukkit, Velocity proxy) and a standalone server. It's a multi-module Gradle project written primarily in Kotlin with some Java, totaling ~50,000 lines of code. The system uses Netty for network communication, Spring Boot for the server components, and provides APIs for client-server packet-based communication.

**Project Type**: Multi-module Gradle project (Kotlin/Java)
**Primary Languages**: Kotlin (95%), Java (5%)
**Target Runtime**: JDK 21 (configured in CI), local development works with JDK 17+
**Gradle Version**: 8.14.3
**Key Frameworks**: Spring Boot 3.5.6, Netty 4.2.6, Ktor 3.3.0, Exposed ORM 0.61.0

## Build and Validation

### Prerequisites

- **Java 17 or higher** (JDK 21 preferred for CI compatibility)
- **Network access to `https://repo.slne.dev/repository/maven-public/`** - This is a **required** private Maven repository for the custom `surf-api-gradle-plugin`. Builds will fail without this access.

### Core Build Commands

**IMPORTANT**: Always run builds from the repository root (`/home/runner/work/surf-cloud/surf-cloud`).

#### Standard Build
```bash
./gradlew build
```
- **Expected duration**: 5-10 minutes on first run (downloads dependencies)
- **Expected duration**: 2-5 minutes on subsequent runs (with Gradle daemon)
- **What it does**: Compiles all modules, runs tests, generates artifacts
- **Timeout setting**: Use `timeout: 300` seconds minimum for bash tool
- **Cache location**: `.gradle/` and `build/` directories

#### Clean Build
```bash
./gradlew clean build
```
- **When to use**: After major dependency changes or when builds behave unexpectedly
- **Expected duration**: 5-10 minutes

#### Build Specific Module
```bash
./gradlew :surf-cloud-api:surf-cloud-api-common:build
```

#### Run Tests
```bash
./gradlew test
```
- **Expected duration**: 1-3 minutes
- **Test locations**: `*/src/test/` directories (limited test coverage exists)

#### Code Quality Check (Qodana)
- Runs automatically on PRs and master branch via GitHub Actions
- Uses JetBrains Qodana with profile `qodana.starter`
- Configuration: `qodana.yaml` in root
- Target JDK: 21 (as configured in qodana.yaml)

### Build Troubleshooting

**Network Dependency Issue**: If you see `Could not GET 'https://repo.slne.dev/repository/maven-public/...'`, the private Maven repository is inaccessible. This is a blocker for builds and cannot be worked around without repository access.

**Gradle Daemon Issues**: If builds hang or behave strangely:
```bash
./gradlew --stop
./gradlew clean build
```

**Out of Memory**: The project is configured with `org.gradle.jvmargs=-Xmx4G` in `gradle.properties`. If you encounter OOM errors, this may need to be increased.

## Project Architecture

### Module Structure

The project follows a layered architecture with clear separation between API, implementation, and platform-specific code:

```
surf-cloud/
├── surf-cloud-api/                    # Public APIs (client & server)
│   ├── surf-cloud-api-common/         # Common API (packets, networking, player)
│   ├── surf-cloud-api-server/         # Server-side API (commands, JPA, Ktor)
│   └── surf-cloud-api-client/         # Client-side API
│       ├── surf-cloud-api-client-common/
│       ├── surf-cloud-api-client-paper/    # Paper-specific API
│       └── surf-cloud-api-client-velocity/ # Velocity-specific API
├── surf-cloud-core/                   # Core implementations
│   ├── surf-cloud-core-common/        # Common core logic
│   └── surf-cloud-core-client/        # Client-side core
├── surf-cloud-bukkit/                 # Paper/Bukkit plugin implementation
├── surf-cloud-velocity/               # Velocity proxy plugin implementation
├── surf-cloud-standalone/             # Standalone server (Spring Boot app)
├── surf-cloud-standalone-launcher/    # Launcher for standalone (Java)
├── surf-cloud-bom/                    # Bill of Materials (dependency versions)
└── surf-cloud-test-plugin/            # Test plugins (excluded from CI)
```

### Key Configuration Files

- **Root `build.gradle.kts`**: Configures all subprojects, shadow JAR settings, API validation
- **`settings.gradle.kts`**: Defines module structure and CI-specific includes
- **`gradle.properties`**: Gradle daemon settings, JVM args, Kotlin config, project version
- **`buildSrc/`**: Custom Gradle convention plugins
  - `core-convention.gradle.kts`: Common dependencies (Spring Boot, Ktor, Kotlin wrappers)
  - `dokka-convention.gradle.kts`: Documentation generation
  - `exclude-kotlin.gradle.kts`: Kotlin stdlib exclusion
- **`gradle/libs.versions.toml`**: Version catalog for all dependencies
- **`.gitlab-ci.yml`**: CI pipeline (build → test → deploy → docs)
- **`.github/workflows/qodana_code_quality.yml`**: Code quality checks
- **`qodana.yaml`**: Qodana configuration (JDK 21, linter: jetbrains/qodana-jvm:2024.3)

### Entry Points

- **Bukkit/Paper Plugin**: `dev.slne.surf.cloud.bukkit.PaperMain` (main) and `PaperBootstrap` (bootstrapper)
- **Velocity Plugin**: `dev.slne.surf.cloud.velocity.VelocityMain`
- **Standalone Server**: `dev.slne.surf.cloud.standalone.Bootstrap` (Spring Boot application)
- **Standalone Launcher**: `dev.slne.surf.cloud.launcher.Main` (Java application)

### Dependencies Not Obvious from Structure

1. **Custom Gradle Plugin**: The project depends on a private Gradle plugin (`dev.slne.surf:surf-api-gradle-plugin:1.21.10+`) hosted at `https://repo.slne.dev/`. This plugin provides tasks for Paper/Velocity plugin configuration and standalone server setup.

2. **Generated Code**: `surf-cloud-api-common` generates `StreamCodecComposites.kt` during build (arity 2-20 composite codecs). This runs via the `generateStreamCodecComposites` task.

3. **Version Resource**: `surf-cloud-api-common` writes the project version to `src/main/resources/cloud.version` during `processResources`.

4. **Spring Boot Configuration Processor**: Used across modules but only as `compileOnly` dependency.

5. **LuckPerms API**: Required server dependency for the Bukkit plugin.

6. **Voicechat API**: Optional (soft) dependency for voice chat mute functionality.

## Coding Standards

From `AGENTS.md`:

- **Style**: Kotlin standard style with 4-space indentation, no trailing whitespace, files end with newline
- **Build Requirement**: **ALWAYS run `./gradlew build` after modifying code.** The build must succeed before committing.
- **Commit Messages**: Short (50 chars max), imperative mood
- **PR Description**: Summarize changes and mention build results

Additional conventions observed in codebase:

- **Package Structure**: `dev.slne.surf.cloud.{module}.{feature}`
- **Packet Classes**: Annotated with `@SurfNettyPacket`, extend `NettyPacket`, have packet codec
- **Packet Listeners**: Methods annotated with `@SurfPacketListener`, must be in Spring components
- **API Annotations**: Internal APIs marked with `@InternalApi` annotation
- **TODO Comments**: Many TODOs exist in code (see surf-cloud-standalone especially) - these are known and not errors

## Common Workflows

### Adding a New Packet

1. Create packet class in appropriate API module extending `NettyPacket`
2. Annotate with `@SurfNettyPacket`
3. Implement packet codec with `StreamCodec`
4. Add constructor with `SurfByteBuf` parameter
5. Add `write(buf: SurfByteBuf)` method
6. Build and test: `./gradlew build`

### Adding a New Module Dependency

1. Add dependency to module's `build.gradle.kts`
2. If it's a new library, add version to `gradle/libs.versions.toml` first
3. Run `./gradlew build` to verify dependency resolution
4. Check for dependency conflicts in output

### Running Locally

**Standalone Server**:
```bash
./gradlew :surf-cloud-standalone:bootJar
java -jar surf-cloud-standalone/build/libs/surf-cloud-standalone-*.jar
```

**Bukkit Plugin**:
```bash
./gradlew :surf-cloud-bukkit:runServer
```
(Configured with JVM arg: `-Dsurf.cloud.serverName=test-server01`)

### Certificate Generation

The system uses TLS certificates for secure communication. Initial setup requires:

1. Run standalone server to generate server keys
2. Run client (will fail) to generate folder structure
3. Copy `ca.pem` from standalone to client's `certificates/` folder
4. Restart client

Use `cert_generation/generate_keys.sh` for generating certificates.

## CI/CD Pipeline

**GitLab CI** (`.gitlab-ci.yml`):
- **Stages**: build → test → deploy → build_docs → test_docs → deploy_docs
- **Java Image**: `eclipse-temurin:21-jdk-alpine`
- **Build Cache**: Enabled for `gradle/wrapper/` and `cache/` directories
- **Test Stage**: Runs `./gradlew test`
- **Deploy Stage**: Runs `./gradlew publish` (publishes to Maven repository)
- **Docs**: Uses Writerside for documentation generation and deployment

**GitHub Actions**:
- **Qodana**: Runs on PRs and master branch pushes
- **JDK**: Uses JDK 21 for Qodana analysis
- **Environment**: Production environment with Qodana token

## Important Notes for Coding Agents

1. **Trust Build System**: The Gradle configuration is complex but complete. Don't add build workarounds unless absolutely necessary.

2. **Module Dependencies**: Respect the dependency hierarchy:
   - API modules should NOT depend on core/implementation modules
   - Client modules should NOT depend on server modules
   - BOM module defines platform dependencies

3. **Spring Boot Logging**: The project uses Log4j2, NOT Logback. Logback is explicitly excluded in multiple places.

4. **Kotlin Stdlib**: Some modules exclude kotlin-stdlib-default dependency via `exclude-kotlin` plugin.

5. **Binary Compatibility**: The project uses `kotlinx.binary-compatibility-validator` with specific modules ignored (see `apiValidation` block in root `build.gradle.kts`).

6. **Test Plugin Exclusion**: The `surf-cloud-test-plugin` is excluded from CI builds (see `settings.gradle.kts` CI check).

7. **Migration Generation**: There's a custom task `generateExposedMigrationScript` in surf-cloud-standalone that requires `migration.properties` file with database credentials.

## Root Directory File List

```
.gitattributes          - Git attributes configuration
.gitignore              - Ignores build/, .gradle/, .idea/, IDE files
.gitlab-ci.yml          - GitLab CI/CD pipeline
.github/                - GitHub configuration (workflows, copilot instructions)
AGENTS.md               - Coding guidelines for agents (referenced above)
LICENSE                 - Project license
README.md               - Basic project info and setup instructions
build.gradle.kts        - Root Gradle build configuration
buildSrc/               - Custom Gradle plugins and conventions
cert_generation/        - Certificate generation scripts
cert_new/               - Certificate documentation
gradle/                 - Gradle wrapper and version catalog
gradle.properties       - Gradle configuration (JVM args, versions)
gradlew / gradlew.bat   - Gradle wrapper scripts
logo-styles.css         - Logo styling for documentation
netlify.toml            - Netlify deployment configuration
qodana.yaml             - Qodana code quality configuration
settings.gradle.kts     - Gradle multi-module settings
Writerside/             - Documentation source files
[module directories]    - See Module Structure section
```

## Search Strategy

Before exploring the codebase with grep/find:

1. **Check this file first** - Most structural information is documented here
2. **Check `gradle/libs.versions.toml`** - For dependency versions
3. **Check module's `build.gradle.kts`** - For module-specific configuration
4. **Check `buildSrc/src/main/kotlin/`** - For convention plugin details

Only search if the above don't contain the needed information or if looking for specific code patterns.

## Final Reminders

- **Always build before committing**: `./gradlew build` must succeed
- **Repository access required**: Builds fail without access to `repo.slne.dev`
- **Use appropriate timeout**: Gradle tasks can take 2-10 minutes
- **Check CI configuration**: Both GitLab and GitHub workflows must pass
- **Respect module boundaries**: API vs. Core vs. Implementation separation
