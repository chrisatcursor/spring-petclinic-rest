# Grind Mode: Java-to-Kotlin Migration

You are performing a complete Java-to-Kotlin migration of the Spring PetClinic REST API in a single pass. This file is fully self-contained -- everything you need is here.

## Environment

- **JDK**: Set `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home` and add `$JAVA_HOME/bin` to PATH before running Maven
- **Build**: `./mvnw compile` (compilation only), `./mvnw verify` (full test suite)
- **Baseline**: 222 tests, 0 failures, 54s build time
- **Server**: port 9966, context path `/petclinic/`, Swagger at `http://localhost:9966/petclinic/swagger-ui.html`
- **Git**: Only push to `ChrisatCursor/spring-petclinic-rest`, NEVER to upstream `spring-petclinic`

## Critical Decisions

1. **Generated code stays as Java.** The OpenAPI generator produces DTOs (`rest/dto/`) and API interfaces (`rest/api/`) in `target/generated-sources/`. Do NOT touch them. Kotlin interops with Java.
2. **JPA entities are NOT data classes.** Use regular Kotlin classes with `var` properties. Data classes break Hibernate proxies and lazy loading. The `kotlin-jpa` plugin generates no-arg constructors.
3. **Replace MapStruct mappers with manual Kotlin mapping functions.** MapStruct requires `kapt` (deprecated). Convert each `@Mapper` interface to a `@Component` class with manual mapping methods.
4. **The `kotlin-spring` plugin auto-opens** Spring-annotated classes. Do NOT add `open` manually.
5. **Validation annotations need `@field:` target** on Kotlin properties: `@field:NotEmpty var address: String = ""`

## Migration Order

Execute in this exact order. Run `./mvnw compile` after each phase. Run `./mvnw verify` after phases 1, 4, 6, 8, and 9.

### Phase 0: Build System
Update `pom.xml`:
- Add `kotlin.version` property (use 2.1.10)
- Add `kotlin-stdlib` and `kotlin-reflect` dependencies
- Add `kotlin-maven-plugin` with `spring` and `jpa` compiler plugins, configured to compile both `src/main/kotlin` and `src/main/java` (and generated sources)
- Adjust `maven-compiler-plugin` so Kotlin compiles first (set default-compile phase to none, add java-compile execution after Kotlin)
- Create `src/main/kotlin/` and `src/test/kotlin/` directories

Verify: `./mvnw compile` succeeds with no Kotlin files yet.

### Phase 1: Model/Entity Classes (11 files in `model/`)
Convert: `BaseEntity`, `NamedEntity`, `Person`, `Owner`, `Pet`, `PetType`, `Vet`, `Visit`, `Specialty`, `Role`, `User`

Rules:
- NOT data classes -- regular Kotlin classes with `var` properties
- Use `@field:` target for validation annotations (`@field:NotEmpty`, `@field:Digits`, etc.)
- Inheritance: `Owner : Person()`, `Person : BaseEntity()`, etc.
- `Set<Pet>` collections: use `var pets: MutableSet<Pet> = mutableSetOf()`
- Delete `package-info.java` files (Kotlin doesn't use them)
- Place in `src/main/kotlin/org/springframework/samples/petclinic/model/`
- Delete original `.java` files from `src/main/java/`

Verify: `./mvnw verify` -- 222 tests pass.

### Phase 2: Mappers (7 files in `mapper/`)
Convert: `OwnerMapper`, `PetMapper`, `PetTypeMapper`, `SpecialtyMapper`, `UserMapper`, `VetMapper`, `VisitMapper`

Rules:
- Replace MapStruct `@Mapper` interfaces with `@Component` classes
- Write manual mapping functions (the DTOs are generated Java classes, so use their getters/setters or constructors)
- Inject dependent mappers via constructor (e.g., `OwnerMapper` depends on `PetMapper`)
- Remove MapStruct from `pom.xml` dependencies and `maven-compiler-plugin` annotation processor config after all mappers are converted

Verify: `./mvnw compile` succeeds.

### Phase 3: Repository Interfaces (7 files in `repository/`)
Convert: `OwnerRepository`, `PetRepository`, `PetTypeRepository`, `SpecialtyRepository`, `UserRepository`, `VetRepository`, `VisitRepository`

These are interfaces -- straightforward conversion. Keep `@Throws(DataAccessException::class)` for Java interop with implementations that haven't been converted yet.

### Phase 4: Repository Implementations

**Spring Data JPA** (15 files in `repository/springdatajpa/`):
Mostly interfaces extending Spring Data -- straightforward.

**JPA** (7 files in `repository/jpa/`):
Classes with `@PersistenceContext` and `EntityManager`. Convert to Kotlin classes with constructor/property injection.

**JDBC** (11 files in `repository/jdbc/`):
Most complex -- `JdbcTemplate`, `RowMapper`, `ResultSetExtractor`. Convert carefully. `RowMapper` lambdas in Kotlin: `RowMapper { rs, _ -> ... }`.

Verify: `./mvnw verify` -- 222 tests pass.

### Phase 5: Service Layer (4 files in `service/`)
Convert: `ClinicService` (interface), `ClinicServiceImpl`, `UserService` (interface), `UserServiceImpl`

Rules:
- `ClinicServiceImpl` has `@Transactional` methods -- `kotlin-spring` handles the `open` requirement
- Constructor injection with `val` parameters
- `Supplier<T>` lambda: convert to Kotlin `() -> T`

### Phase 6: REST Controllers (9 files in `rest/controller/`) + Exception Advice (1 file in `rest/advice/`)
Convert: `OwnerRestController`, `PetRestController`, `PetTypeRestController`, `SpecialtyRestController`, `UserRestController`, `VetRestController`, `VisitRestController`, `RootRestController`, `BindingErrorsResponse`, `ExceptionControllerAdvice`

Rules:
- Controllers implement generated Java API interfaces (e.g., `OwnersApi`) -- keep implementing them
- `@PreAuthorize` annotations stay as-is
- `ResponseEntity<T>` usage stays the same
- Mapper injection via constructor

Verify: `./mvnw verify` -- 222 tests pass.

### Phase 7: Validation, Security, Config, Entry Point (~6 files)
Convert: `PetAgeValidation`, `PetAgeValidator`, `BasicAuthenticationConfig`, `DisableSecurityConfig`, `Roles`, `SwaggerConfig`, `PetClinicApplication`

Rules:
- **Security config needs careful review**: `BasicAuthenticationConfig` uses `@Autowired` field injection for `DataSource` -- convert to constructor injection
- `Roles` class: constants become `const val` in a companion object or top-level
- `PetClinicApplication`: `@SpringBootApplication` class with `main` function as a top-level function
- `CallMonitoringAspect` and `EntityUtils` in `util/` package

Verify: `./mvnw verify` -- 222 tests pass.

### Phase 8: Test Files (~20 files)
Convert all test classes in `src/test/java/` to Kotlin in `src/test/kotlin/`.

Rules:
- `@Autowired` fields become `@Autowired lateinit var`
- Mockito: use `whenever` instead of `when` (Kotlin keyword). Consider adding `mockito-kotlin` dependency.
- `@Test void testFoo()` becomes `@Test fun testFoo()`
- Abstract test classes: keep as `abstract class`
- Test config classes: `@TestConfiguration` stays

Verify: `./mvnw verify` -- 222 tests pass.

### Phase 9: Cleanup
- Remove empty `src/main/java/` directories (or leave if generated sources need them)
- Remove MapStruct dependencies from `pom.xml` if not already done
- Delete all `package-info.java` files
- Verify no `.java` files remain in `src/main/kotlin/` or `src/test/kotlin/`
- Final `./mvnw verify`

## If You Get Stuck

- If a file won't compile after 3 attempts, add a `// TODO: Migration issue - <description>` comment and move on
- If tests fail after a phase, check if it's a single test or widespread. Fix single failures; flag widespread ones.
- Do NOT loop endlessly on the same error. Skip and document.

## Commit Strategy

Commit after each phase with a message like:
```
Phase N: Migrate <description> to Kotlin

- Converted X files from Java to Kotlin
- <any notable changes>
```

When complete, open a PR against `migration/kotlin-grind` on `ChrisatCursor/spring-petclinic-rest`.
