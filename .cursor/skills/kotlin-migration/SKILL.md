# Kotlin Migration Skill

This skill provides the complete reference for migrating the Spring PetClinic REST API from Java to idiomatic Kotlin. Read this before converting any file.

## Key Decisions

- **Generated code stays as Java.** The OpenAPI-generated DTOs (`rest/dto/`) and API interfaces (`rest/api/`) are produced by `openapi-generator-maven-plugin` into `target/generated-sources/`. Do NOT convert them. Kotlin interops with Java seamlessly.
- **Replace MapStruct with manual Kotlin mapping functions.** MapStruct requires `kapt` for Kotlin, which is deprecated-track. Kotlin is concise enough that manual mapping functions are cleaner and avoid the build complexity.
- **JPA entities are NOT data classes.** Use regular Kotlin classes with `var` properties. Data classes break Hibernate proxies, lazy loading, and generate problematic `equals`/`hashCode`.
- **The `kotlin-jpa` plugin** generates required no-arg constructors for `@Entity` classes.
- **The `kotlin-spring` plugin** auto-opens classes annotated with `@Service`, `@Component`, `@Configuration`, `@Controller`, `@RestController`, etc. Do NOT manually add `open` to these.

## Build System Changes (pom.xml)

These changes must be applied before any Kotlin code is added (Batch 0):

### Add Kotlin properties
```xml
<properties>
    <kotlin.version>2.1.10</kotlin.version>
</properties>
```

### Add dependencies
```xml
<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib</artifactId>
    <version>${kotlin.version}</version>
</dependency>
<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-reflect</artifactId>
    <version>${kotlin.version}</version>
</dependency>
```

### Add Kotlin Maven plugin (must run BEFORE maven-compiler-plugin)
```xml
<plugin>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-maven-plugin</artifactId>
    <version>${kotlin.version}</version>
    <configuration>
        <compilerPlugins>
            <plugin>spring</plugin>
            <plugin>jpa</plugin>
        </compilerPlugins>
        <jvmTarget>${java.version}</jvmTarget>
    </configuration>
    <executions>
        <execution>
            <id>compile</id>
            <goals><goal>compile</goal></goals>
            <configuration>
                <sourceDirs>
                    <sourceDir>${project.basedir}/src/main/kotlin</sourceDir>
                    <sourceDir>${project.basedir}/src/main/java</sourceDir>
                    <sourceDir>${project.basedir}/target/generated-sources/openapi/src/main/java</sourceDir>
                </sourceDirs>
            </configuration>
        </execution>
        <execution>
            <id>test-compile</id>
            <goals><goal>test-compile</goal></goals>
            <configuration>
                <sourceDirs>
                    <sourceDir>${project.basedir}/src/test/kotlin</sourceDir>
                    <sourceDir>${project.basedir}/src/test/java</sourceDir>
                </sourceDirs>
            </configuration>
        </execution>
    </executions>
    <dependencies>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-maven-allopen</artifactId>
            <version>${kotlin.version}</version>
        </dependency>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-maven-noarg</artifactId>
            <version>${kotlin.version}</version>
        </dependency>
    </dependencies>
</plugin>
```

### Adjust maven-compiler-plugin
The `maven-compiler-plugin` default-compile and default-testCompile executions must have `<phase>none</phase>` so Kotlin compiles first. Add new `java-compile` and `java-test-compile` executions that run in the `compile` and `test-compile` phases respectively, after the Kotlin plugin.

## Before/After Patterns

### JPA Entity (NOT a data class)
```java
// Java
@Entity
@Table(name = "owners")
public class Owner extends Person {
    @Column(name = "address")
    @NotEmpty
    private String address;

    public String getAddress() { return this.address; }
    public void setAddress(String address) { this.address = address; }
}
```
```kotlin
// Kotlin
@Entity
@Table(name = "owners")
class Owner : Person() {
    @Column(name = "address")
    @field:NotEmpty
    var address: String = ""
}
```

### Constructor Injection
```java
// Java
@Service
public class ClinicServiceImpl implements ClinicService {
    private final PetRepository petRepository;
    private final VetRepository vetRepository;

    public ClinicServiceImpl(PetRepository petRepository, VetRepository vetRepository) {
        this.petRepository = petRepository;
        this.vetRepository = vetRepository;
    }
}
```
```kotlin
// Kotlin
@Service
class ClinicServiceImpl(
    private val petRepository: PetRepository,
    private val vetRepository: VetRepository
) : ClinicService {
}
```

### MapStruct Mapper -> Manual Kotlin Mapping
```java
// Java (MapStruct)
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {
    OwnerDto toOwnerDto(Owner owner);
    Owner toOwner(OwnerDto ownerDto);
    List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);
}
```
```kotlin
// Kotlin (manual mapping, Spring component)
@Component
class OwnerMapper(private val petMapper: PetMapper) {
    fun toOwnerDto(owner: Owner): OwnerDto = OwnerDto(
        id = owner.id,
        firstName = owner.firstName,
        lastName = owner.lastName,
        address = owner.address,
        city = owner.city,
        telephone = owner.telephone,
        pets = owner.pets.map { petMapper.toPetDto(it) }
    )

    fun toOwner(ownerDto: OwnerDto): Owner = Owner().apply {
        id = ownerDto.id
        firstName = ownerDto.firstName
        lastName = ownerDto.lastName
        address = ownerDto.address
        city = ownerDto.city
        telephone = ownerDto.telephone
    }

    fun toOwnerDtoCollection(owners: Collection<Owner>): List<OwnerDto> =
        owners.map { toOwnerDto(it) }
}
```

### Optional to Nullable
```java
// Java
Optional<Pet> findById(int id);
pet.orElse(null);
```
```kotlin
// Kotlin
fun findById(id: Int): Pet?
```

### Streams to Collection Operations
```java
// Java
getPetsInternal().stream().filter(p -> p.getId().equals(petId)).findFirst().orElse(null);
```
```kotlin
// Kotlin
petsInternal.firstOrNull { it.id == petId }
```

### Switch to When
```java
// Java
switch (status) {
    case "ACTIVE": return true;
    case "INACTIVE": return false;
    default: throw new IllegalArgumentException();
}
```
```kotlin
// Kotlin
when (status) {
    "ACTIVE" -> true
    "INACTIVE" -> false
    else -> throw IllegalArgumentException()
}
```

### String Concatenation to Templates
```java
// Java
"Owner " + owner.getId() + " not found"
```
```kotlin
// Kotlin
"Owner ${owner.id} not found"
```

### JUnit 5 Tests
```java
// Java
@Test
void testGetOwner() {
    // ...
}

@Autowired
private ClinicService clinicService;
```
```kotlin
// Kotlin
@Test
fun testGetOwner() {
    // ...
}

@Autowired
lateinit var clinicService: ClinicService
```

## Edge Cases

- **`@Value("\${property}")`**: In Kotlin string literals, `$` triggers string interpolation. Escape it: `@Value("\${property}")` stays the same in Kotlin because the annotation value is a plain string, but be careful in string templates.
- **`@field:` target for validation annotations**: Jakarta validation annotations on Kotlin properties need `@field:NotEmpty` instead of `@NotEmpty` to target the backing field (which is what JPA reads).
- **`lateinit var` for test injection**: Use `lateinit var` for `@Autowired` fields in tests. Do NOT use `lateinit` for nullable types.
- **`mockito-kotlin`**: Prefer `mockito-kotlin` library for better null safety and final class support in tests. Use `whenever` instead of `when` (which is a Kotlin keyword).
- **`kotlin-spring` auto-open**: Do NOT add `open` modifier to `@Service`, `@Component`, `@Configuration`, `@RestController` classes. The `kotlin-spring` compiler plugin handles this.
- **`kotlin-jpa` no-arg**: Do NOT manually write no-arg constructors for `@Entity` classes. The `kotlin-jpa` plugin generates them.
- **Companion objects for constants**: Java `static final` fields become `companion object` members or top-level `const val`.
- **`Unit` vs `void`**: Kotlin functions that return nothing return `Unit` implicitly. Do NOT write `: Unit` explicitly.
- **`@Throws`**: Add `@Throws(DataAccessException::class)` to Kotlin methods that declare checked exceptions for Java interop (only needed if Java code calls them).

## Verification Checklist

After each batch:
1. `./mvnw compile` succeeds
2. `./mvnw verify` passes -- 222 tests, 0 failures
3. No regressions in JaCoCo coverage

After all batches:
1. Application boots: `./mvnw spring-boot:run` responds on port 9966
2. Swagger UI loads at `http://localhost:9966/petclinic/swagger-ui.html`
3. No `.java` files remain in `src/main/kotlin/` or `src/test/kotlin/`
4. All hand-written `.java` files in `src/main/java/` and `src/test/java/` have been replaced

## File Placement

- Kotlin source files go in `src/main/kotlin/` mirroring the package structure
- Kotlin test files go in `src/test/kotlin/` mirroring the package structure
- When a Java file is converted, delete the original `.java` file from `src/main/java/`
- `package-info.java` files can be deleted (Kotlin doesn't use them)
