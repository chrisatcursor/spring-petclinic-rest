# Linear Orchestration Skill

This skill teaches the orchestrating agent how to use Linear to track and manage the Java-to-Kotlin migration.

## Linear Project

- **Team**: FE Demos
- **Project**: "PetClinic Java-to-Kotlin Migration"
- **Owner**: Chris Diaz
- **MCP Server**: `dashboard-team-1-Linear`

To find the project:
```
CallMcpTool: server=dashboard-team-1-Linear, toolName=list_projects, arguments={query: "PetClinic Java-to-Kotlin"}
```

## Workflow Per Batch

### 1. Pick the next batch issue
```
CallMcpTool: server=dashboard-team-1-Linear, toolName=list_issues, arguments={project: "<project-id>", state: "backlog"}
```
Pick the lowest-numbered batch that has no unresolved `blockedBy` dependencies.

### 2. Mark issue as in-progress
```
CallMcpTool: server=dashboard-team-1-Linear, toolName=save_issue, arguments={id: "<issue-id>", state: "In Progress"}
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
CallMcpTool: server=dashboard-team-1-Linear, toolName=save_comment, arguments={issueId: "<issue-id>", body: "Batch N complete. X files converted. ./mvnw verify: 222 tests passed."}
```

### 5. Open a PR
Follow the `.cursor/skills/pr-composition/SKILL.md` template. Link the PR to the Linear issue:
```
CallMcpTool: server=dashboard-team-1-Linear, toolName=save_issue, arguments={id: "<issue-id>", links: [{url: "<pr-url>", title: "PR: Batch N"}]}
```

### 6. Mark issue as done
```
CallMcpTool: server=dashboard-team-1-Linear, toolName=save_issue, arguments={id: "<issue-id>", state: "Done"}
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
