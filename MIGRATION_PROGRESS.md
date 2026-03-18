# Migration Progress

Tracking the Java-to-Kotlin migration of Spring PetClinic REST API.

## Summary

| Batch | Description | Status | Start | End | Files | Tests | Notes |
|-------|-------------|--------|-------|-----|-------|-------|-------|
| 0 | Build system (pom.xml + Kotlin config) | Done | 2026-03-18 | 2026-03-18 | pom | 222 | Kotlin `sourceDirs`, compile in `process-sources` before javac |
| 1 | Model/entity classes | Done | 2026-03-18 | 2026-03-18 | 11/11 | 222 pass | See Batch 1 log |
| 2 | Mappers (replace MapStruct) | Pending | - | - | 0/7 | - | |
| 3 | Repository interfaces + Spring Data JPA | Pending | - | - | 0/22 | - | |
| 4 | JPA repository implementations | Pending | - | - | 0/7 | - | |
| 5 | JDBC repository implementations | Pending | - | - | 0/11 | - | |
| 6 | Service layer | Pending | - | - | 0/4 | - | |
| 7 | REST controllers + exception advice | Pending | - | - | 0/10 | - | |
| 8 | Validation, security, config, entry point | Pending | - | - | 0/~8 | - | |
| 9 | Test files | Pending | - | - | 0/~20 | - | |
| 10 | Cleanup + final verification | Pending | - | - | - | - | |

**Baseline**: 222 tests, 0 failures, 54.3s build time, 85 Java source files

## Batch Logs

### Batch 1: Model/entity classes
- **Completed**: 2026-03-18
- **Files converted**: 11 Kotlin classes under `src/main/kotlin/.../model/`; removed 12 Java files (11 entities + `package-info.java`)
- **Compile result**: PASS
- **Verify result**: 222 tests, 0 failures; JaCoCo PASS
- **Build fixes (Batch 0 alignment)**:
  - `kotlin-maven-plugin`: explicit `sourceDirs` for main/test Kotlin
  - Kotlin compile bound to `process-sources` so Java sees Kotlin classes on classpath
- **Entity notes**:
  - `@field:Access(AccessType.FIELD)` on `Owner.pets`, `Pet.visits`, `Vet.specialties` so JPQL `owner.pets` resolves and JVM has single `getPets()`/`getVisits()`/`getSpecialties()` returning sorted lists
  - `open class Pet` for `JdbcPet` subclass
  - Nullable `String?` where Java allowed null (mappers/controllers call setters with null); `NamedEntity.name`, `Person` names, `Owner` address fields, `Visit.description`
- **PR**: https://github.com/chrisatcursor/spring-petclinic-rest/pull/2

## Decision Log

_Changes to migration decisions made during execution._

| Date | Decision | Reason |
|------|----------|--------|
| 2026-03-16 | Keep generated OpenAPI code as Java | Kotlin interops seamlessly, avoids generator switch risk |
| 2026-03-16 | Replace MapStruct with manual Kotlin mappers | kapt is deprecated-track, Kotlin is concise enough |
| 2026-03-16 | Split repository layer into 3 sub-batches | JDBC/JPA/Spring Data JPA are different complexity levels |
| 2026-03-18 | Kotlin compile in `process-sources` | Ensures mixed module: Kotlin entities before javac |
| 2026-03-18 | FIELD access for collection fields with custom list getters | Hibernate attribute names `pets`/`visits`/`specialties` + no duplicate JVM getters |

## Blockers and Human Review

_Items flagged for human attention._

## Running Metrics

- **Total files migrated**: 11 / 85 (main)
- **Total batches complete**: 2 / 11 (Batch 0 tooling + Batch 1 models)
- **Cumulative build time**: verify ~18s local (varies by machine)
