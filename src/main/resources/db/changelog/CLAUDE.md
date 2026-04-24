# Database Migrations (Liquibase)

- Changelogs organized by bounded context: `identity/`, `group/`, etc.
- Master changelog: `db.changelog-master.yaml`
- **Each migration file is listed explicitly** in the master changelog (no `includeAll`) — controls execution order
- Naming convention: `NNN-description.yaml` (e.g. `001-create-users-table.yaml`)
