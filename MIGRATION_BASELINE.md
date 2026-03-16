# Migration Baseline

Recorded on: 2026-03-16, branch `master`

## Build Verification

- **Command**: `./mvnw verify`
- **Result**: BUILD SUCCESS
- **Total build time**: 54.3s (wall clock ~56s)
- **JaCoCo coverage**: All checks passed (85% line, 66% branch thresholds)
- **Classes analyzed by JaCoCo**: 64

## Test Results

- **Total tests**: 222
- **Failures**: 0
- **Errors**: 0
- **Skipped**: 0

### Test Classes (18 classes, 222 tests)

| Test Class | Tests | Time |
|---|---|---|
| `ValidatorTests` | 1 | 0.377s |
| `SpringConfigTests` | 1 | 4.309s |
| `ClinicServiceHsqlJdbcTests` | 34 | 0.671s |
| `ClinicServiceH2JdbcTests` | 34 | 0.729s |
| `ClinicServiceJpaTests` | 34 | 0.632s |
| `ClinicServiceSpringDataJpaTests` | 34 | 0.503s |
| `UserServiceHsqlJdbcTests` | 1 | 0.313s |
| `UserServiceSpringDataJpaTests` | 1 | 0.009s |
| `UserServiceJpaTests` | 1 | 0.006s |
| `UserServiceH2JdbcTests` | 1 | 0.005s |
| `PetRestControllerTests` | 8 | 1.719s |
| `PetTypeRestControllerTests` | 12 | 0.156s |
| `VisitRestControllerTests` | 10 | 0.138s |
| `VetRestControllerTests` | 10 | 0.148s |
| `SpecialtyRestControllerTests` | 10 | 0.116s |
| `UserRestControllerTests` | 2 | 0.953s |
| `OwnerRestControllerTests` | 24 | 0.372s |
| `PetAgeValidatorTest` | 4 | 0.080s |

## Source File Counts

- **Java source files** (`src/main/java/`): 85 (includes 3 `package-info.java`)
- **Java test files** (`src/test/java/`): ~20
- **Hand-written source files** (excluding `package-info.java`): 82

## Application Configuration

- **Server port**: 9966
- **Context path**: `/petclinic/`
- **Swagger UI**: `http://localhost:9966/petclinic/swagger-ui.html`
- **Default profiles**: `h2,spring-data-jpa`
- **Security**: Disabled by default (`petclinic.security.enable=false`)
- **Test profiles**: `hsqldb,spring-data-jpa` with basic auth enabled

## Environment

- **Spring Boot**: 4.0.3
- **JDK**: OpenJDK 17.0.18 (Homebrew)
- **Build tool**: Maven via wrapper (`./mvnw`)
- **JAVA_HOME**: `/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home`
