# AGENTS.md

## Cursor Cloud specific instructions

### Overview

Spring PetClinic REST is a backend-only REST API for a veterinary clinic management system. It exposes CRUD endpoints for owners, pets, vets, visits, specialties, pet types, and users. There is no UI — it is designed to be consumed by a separate Angular frontend.

The codebase is being migrated from Java to Kotlin. See `.cursor/rules/no-java.mdc` and `.cursor/skills/kotlin-migration/SKILL.md` for migration conventions.

### Build & Run

Standard commands are documented in `readme.md`. Key reference:

| Task | Command |
|------|---------|
| Full build (compile + codegen + test) | `./mvnw clean install` |
| Run tests only | `./mvnw test` |
| Start app (dev, H2 in-memory DB) | `./mvnw spring-boot:run` |
| Generate code only (no tests) | `./mvnw generate-sources` |

- **Port**: `9966`, context path `/petclinic/`
- **Health check**: `http://localhost:9966/petclinic/actuator/health`
- **Swagger UI**: `http://localhost:9966/petclinic/swagger-ui/index.html`

### Non-obvious caveats

- **Code generation is required before IDE support works.** DTOs (`rest.dto` package) and MapStruct mapper implementations are generated into `target/generated-sources/` during `./mvnw compile` or `./mvnw install`. If you see missing class errors, run a build first.
- **Default profile is `h2,spring-data-jpa`** (in-memory H2). No external database is needed for development or testing.
- **Test profile uses HSQLDB** (`hsqldb,spring-data-jpa`) — also in-memory, no external services needed.
- **JaCoCo coverage checks run during `install`.** If you only need test results, `./mvnw test` is faster. To skip JaCoCo: `./mvnw test -Djacoco.skip=true`.
- **JDK 17+ is required** (`maven.compiler.release=17`). JDK 21 works fine and is pre-installed on the VM.
- **Maven wrapper** (`./mvnw`) is the canonical way to build. It auto-downloads Maven 3.9.9.
- **Security is disabled by default** (`petclinic.security.enable=false`). No auth is needed for API calls during development.
- **This is a fork of `spring-petclinic/spring-petclinic-rest`.** All pushes and PRs must target `ChrisatCursor/spring-petclinic-rest`. See `.cursor/rules/upstream-protection.mdc`.
