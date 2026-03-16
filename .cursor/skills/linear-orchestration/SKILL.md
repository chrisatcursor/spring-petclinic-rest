# Linear Orchestration Skill

This skill teaches the orchestrating agent how to use Linear to track and manage the Java-to-Kotlin migration.

## Linear Project

- **Team**: FE Demos
- **Project**: "PetClinic Java-to-Kotlin Migration"
- **Owner**: Chris Diaz
- **MCP Server**: `plugin-linear-linear`
- **Project URL**: https://linear.app/anysphere/project/petclinic-java-to-kotlin-migration-f34d843f7fb7
- **Project ID**: `78deaefb-e0e2-496f-9366-3802318c4f27`

To find the project:
```
CallMcpTool: server=plugin-linear-linear, toolName=list_projects, arguments={query: "PetClinic Java-to-Kotlin"}
```

## Batch Issue Reference

| Batch | Linear ID | Title |
|-------|-----------|-------|
| 0 | FED-68 | Build system -- Add Kotlin to pom.xml |
| 1 | FED-69 | Migrate model/entity classes to Kotlin |
| 2 | FED-70 | Replace MapStruct mappers with Kotlin mapping functions |
| 3 | FED-71 | Migrate repository interfaces + Spring Data JPA impls |
| 4 | FED-72 | Migrate JPA repository implementations |
| 5 | FED-73 | Migrate JDBC repository implementations |
| 6 | FED-74 | Migrate service layer |
| 7 | FED-75 | Migrate REST controllers + exception advice |
| 8 | FED-76 | Migrate validation, security, config, entry point |
| 9 | FED-77 | Migrate test files to Kotlin |
| 10 | FED-78 | Cleanup and final verification |

## Workflow Per Batch

### 1. Pick the next batch issue
```
CallMcpTool: server=plugin-linear-linear, toolName=list_issues, arguments={project: "PetClinic Java-to-Kotlin Migration", state: "backlog"}
```
Pick the lowest-numbered batch that has no unresolved `blockedBy` dependencies.

### 2. Mark issue as in-progress
```
CallMcpTool: server=plugin-linear-linear, toolName=save_issue, arguments={id: "<issue-id>", state: "In Progress"}
```

### 3. Execute the batch
- Use the `migrate-batch` subagent to convert files
- Use the `verify-migration` subagent to confirm tests pass
- Use the `modernize-kotlin` subagent to review idioms
- Use the `security-scan` subagent for security-related batches (Batch 8)

### 4. Update progress
After the batch is complete:
- Update `MIGRATION_PROGRESS.md` with batch results
- Add a comment to the Linear issue with results summary:
```
CallMcpTool: server=plugin-linear-linear, toolName=save_comment, arguments={issueId: "<issue-id>", body: "Batch N complete. X files converted. ./mvnw verify: 222 tests passed."}
```

### 5. Open a PR
Follow the `.cursor/skills/pr-composition/SKILL.md` template. Link the PR to the Linear issue:
```
CallMcpTool: server=plugin-linear-linear, toolName=save_issue, arguments={id: "<issue-id>", links: [{url: "<pr-url>", title: "PR: Batch N"}]}
```

### 6. Mark issue as done
```
CallMcpTool: server=plugin-linear-linear, toolName=save_issue, arguments={id: "<issue-id>", state: "Done"}
```

### 7. Move to next batch
Repeat from step 1.

## Linear MCP Tools Reference

| Tool | Purpose |
|---|---|
| `list_projects` | Find the migration project |
| `list_issues` | List batch issues, filter by state |
| `get_issue` | Get details of a specific issue |
| `save_issue` | Create or update issues (status, links, comments) |
| `save_comment` | Add progress comments to issues |
| `list_issue_statuses` | See available workflow states |

## Error Handling

- If a batch fails verification, mark the Linear issue state as "In Review" (or equivalent blocked state) and add a comment describing the failure
- Flag the issue for human review by adding a comment starting with "BLOCKED:"
- Update `MIGRATION_PROGRESS.md` with the failure details
- Move to the next batch only if it doesn't depend on the blocked one
