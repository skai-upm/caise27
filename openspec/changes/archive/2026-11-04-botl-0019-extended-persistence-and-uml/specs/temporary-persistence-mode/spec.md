# Spec Delta

## MODIFIED Requirements

### Requirement: Temporary data directory
When the first argument is `--fly`, the application SHALL create at startup a new directory `tmp/<yyyyMMdd_HHmmss>/` next to the permanent data files, named with the start timestamp and distinct from any existing directory, and SHALL make every repository (users, groups, tickets, group tickets, attractions, passes) read and write only files inside it. The temporary directory SHALL start without data files.

#### Scenario: All repositories use the temporary directory
- **GIVEN** permanent data files exist with users, groups, tickets, attractions and passes
- **WHEN** the application starts with `--fly` and processes commands that create a user, a group, a ticket, a group ticket, a typed attraction and passes, and consumes a pass
- **THEN** all the resulting data files, including the passes file, are written inside `tmp/<timestamp>/` and the permanent data files are byte-for-byte unchanged

#### Scenario: Independent start
- **GIVEN** a permanent users file containing Ana
- **WHEN** the application starts with `--fly` and processes `USER CREATE` for Ana with the same DNI and email
- **THEN** the creation succeeds, because the temporary run has no stored users

#### Scenario: Directory name
- **WHEN** the application starts with `--fly`
- **THEN** the directory created under `tmp/` has a name of the form `yyyyMMdd_HHmmss` (with a numeric suffix only if that name already exists)
