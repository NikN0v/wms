
# WMS — Development Instructions

## Project
Warehouse Management System (WMS).

## Stack
- Kotlin
- Spring Boot
- PostgreSQL
- Spring Data JPA
- Flyway
- Gradle

## Development approach
- Work as a senior backend developer.
- Keep explanations concise and technical.
- Inspect existing code before proposing or implementing changes.
- Follow the current project architecture and naming conventions.
- Implement proper validation and error handling.
- Do not introduce Docker unless explicitly requested.

## Multitenancy
- Preserve tenant isolation.
- Verify that tenant-specific operations cannot access another tenant's data.
- Do not trust tenant identifiers from requests without checking authorization and ownership.

## Database
- Use Flyway for schema changes.
- Never edit migrations already applied to a database.
- Create a new migration for each schema change.
- Preserve existing data and constraints.

## Workflow
1. Read PROJECT_CONTEXT.md.
2. Inspect the actual code and database migrations.
3. Compare implementation with the project context.
4. Propose the next step before making changes.
5. Run relevant tests after implementation.
6. Report modified files, test results and remaining issues.
