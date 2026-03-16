# PR Composition Skill

Use this skill when composing pull requests for migration batches. Follow this format for consistency across all PRs.

## PR Title Format

```
[Batch N] <brief description of what was migrated>
```

Examples:
- `[Batch 0] Add Kotlin build configuration to pom.xml`
- `[Batch 1] Migrate model/entity classes to Kotlin`
- `[Batch 7] Migrate REST controllers to Kotlin`

## PR Body Template

```markdown
## Summary

<1-3 sentences describing what was migrated in this batch>

## Files Changed

- **Converted**: <count> Java files -> Kotlin
- **Deleted**: <count> Java source files removed
- **Modified**: <list any non-migration changes, e.g., pom.xml>

## Migration Notes

- <any notable conversion decisions or patterns applied>
- <any deviations from the kotlin-migration skill>

## Modernization

- <Kotlin idioms applied beyond mechanical conversion>
- <e.g., replaced verbose null checks with safe calls, used when expressions>

## Test Results

- **Before**: 222 tests, 0 failures
- **After**: <test count> tests, <failure count> failures
- **JaCoCo**: <pass/fail>

## Human Review Flags

- [ ] <any areas that need human attention>
- [ ] <security-related changes if applicable>
- [ ] <complex logic that may have changed behavior>

If no human review flags, write: "No items flagged for human review."
```

## Rules

- Always target the integration branch (`migration/kotlin-linear`) for Linear-orchestrated work
- Always target `ChrisatCursor/spring-petclinic-rest` repo, never upstream
- Include the Linear issue link in the PR body if available
- If tests fail, note the failures explicitly and do NOT merge
- Use `gh pr create --repo ChrisatCursor/spring-petclinic-rest` for PR creation
