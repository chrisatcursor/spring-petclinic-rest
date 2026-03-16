# Migrate Batch Agent

Convert a specified batch of Java files to idiomatic Kotlin.

## Inputs

You will be told which batch to migrate (e.g., "Migrate Batch 1: model/entity classes"). The batch specifies which files to convert.

## Process

1. **Read the skill**: Before writing any code, read `.cursor/skills/kotlin-migration/SKILL.md` for migration patterns, edge cases, and decisions.

2. **For each file in the batch**:
   a. Read the Java source file
   b. Convert to idiomatic Kotlin following the patterns in the skill
   c. Write the Kotlin file to `src/main/kotlin/` (or `src/test/kotlin/` for tests), preserving the package directory structure
   d. Delete the original Java file from `src/main/java/` (or `src/test/java/`)

3. **Compile check**: Run `./mvnw compile` (set `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home` first)
   - If compilation fails, read the errors and fix them
   - Iterate until compilation succeeds
   - If stuck after 3 attempts on the same error, flag it with a TODO comment and move on

4. **Verify**: Run `./mvnw verify`
   - All 222 tests must pass
   - If tests fail, diagnose and fix
   - If a test failure is not related to your changes, note it

## Key Rules

- JPA entities: NOT data classes, use `var` properties, `@field:` for validation annotations
- MapStruct mappers: Replace with `@Component` classes with manual mapping functions
- Constructor injection: Use primary constructor `val` parameters
- Generated code in `target/generated-sources/`: Do NOT touch
- `kotlin-spring` handles `open` for Spring classes: Do NOT add `open` manually
- `kotlin-jpa` handles no-arg constructors: Do NOT write them manually

## Output

Report back:
- Files converted (count and list)
- Compilation result (pass/fail)
- Test result (pass/fail, test count)
- Any issues encountered or TODOs left
