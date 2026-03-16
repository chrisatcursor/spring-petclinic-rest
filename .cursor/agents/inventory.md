# Inventory Agent

Scan the Java source tree and produce a structured migration manifest.

## Task

1. List all `.java` files under `src/main/java/` (exclude `package-info.java`)
2. For each file, catalog:
   - **Package**: e.g., `org.springframework.samples.petclinic.model`
   - **Class type**: entity, DTO, mapper, repository-interface, repository-impl, service-interface, service-impl, controller, config, security, util, test
   - **Spring annotations**: `@Entity`, `@Service`, `@Repository`, `@RestController`, `@Configuration`, `@Component`, etc.
   - **Dependencies**: which other project classes does it import/use?
   - **Classification**: leaf (no project deps), simple-container (few deps), complex-workflow (business logic, transactions), shared-infra (everything depends on it)
   - **Size**: S (<50 LOC), M (50-200 LOC), L (>200 LOC)
3. Produce a dependency-ordered migration manifest as a markdown table
4. Group files into recommended migration batches

## Output

Write the manifest to `MIGRATION_MANIFEST.md` in the repo root. Format:

```markdown
# Migration Manifest

## File Inventory

| File | Package | Type | Classification | Size | Batch |
|------|---------|------|----------------|------|-------|
| BaseEntity.java | model | entity | shared-infra | S | 1 |
| ... | ... | ... | ... | ... | ... |

## Dependency Graph

<describe key dependency chains>

## Recommended Batch Order

<list batches with rationale>
```

## Notes

- Ignore files in `target/generated-sources/` (these are generated and stay as Java)
- Ignore `package-info.java` files (will be deleted)
- Count test files separately
