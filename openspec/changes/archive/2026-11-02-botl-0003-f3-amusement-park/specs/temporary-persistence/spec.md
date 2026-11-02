# Spec Delta

## MODIFIED Requirements

### Requirement: Fly mode uses an isolated directory
When the first argument is `--fly`, the application SHALL create at startup a new directory `tmp/<yyyyMMdd_HHmmss>/` (made unique if it already exists) and SHALL store and load the files of every repository — users, groups, tickets, group tickets, passes, and attractions — only there. The same repository code SHALL be used as in normal mode; only the paths differ.

#### Scenario: Isolated run and cleanup
- **WHEN** permanent data exists and a `--fly` session creates users, groups, tickets, passes, and attractions and then exits
- **THEN** a subsequent normal run observes exactly the original permanent data

#### Scenario: Shared functionality
- **WHEN** the same commands run once normally and once with `--fly` from equivalent empty state
- **THEN** the command outputs are identical
