# AGENTS.md

## Cursor Cloud specific instructions

This is a **Spring Boot REST API** (Spring PetClinic REST, v4.0.2) with no UI. It uses Java 21, Maven 3.9.9 (via `./mvnw` wrapper), and an in-memory H2 database by default (no external services required).

### Key commands

| Action | Command |
|--------|---------|
| Build + test | `./mvnw clean install` |
| Run (dev) | `./mvnw spring-boot:run` |
| Run tests only | `./mvnw test` |

See `readme.md` for full API endpoint list and database switching instructions.

### Service details

- **Port:** `9966`, context path `/petclinic/`
- **Health check:** `http://localhost:9966/petclinic/actuator/health`
- **Swagger UI:** `http://localhost:9966/petclinic/swagger-ui/index.html` (redirects from `/swagger-ui.html`)
- **H2 Console:** `http://localhost:9966/petclinic/h2-console` (JDBC URL: `jdbc:h2:mem:petclinic`, user: `sa`, no password)

### Non-obvious caveats

- **Code generation:** DTOs and API interfaces are generated at build time from `src/main/resources/openapi.yml` by the OpenAPI Generator Maven plugin. You must run `./mvnw compile` (or `clean install`) before the project will compile in an IDE or if you modify the OpenAPI spec. Generated sources go to `target/generated-sources/openapi/`.
- **MapStruct mappers** are also generated at compile time via annotation processing. If mapper-related compilation errors appear, run `./mvnw compile` first.
- **Adding a pet** via `POST /api/owners/{id}/pets` requires the `type` field to include both `id` and `name` (e.g., `{"id": 2, "name": "dog"}`); omitting `name` returns a 400 validation error.
- **Security** is disabled by default. Set `petclinic.security.enable=true` in `application.properties` to enable basic auth (default user: `admin`/`admin`).
- **Database profiles:** Default is `h2,spring-data-jpa`. To switch, change `spring.profiles.active` in `src/main/resources/application.properties`. MySQL and PostgreSQL require Docker containers (see `docker-compose.yml`).
