# Spec Delta

## ADDED Requirements

### Requirement: Groups locked by current group tickets
While a group has any group ticket dated on or after the system date, `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` on that group SHALL be rejected with an error and the group SHALL remain unchanged. Once all its group tickets are dated before the system date, the group SHALL be modifiable again.

#### Scenario: Locked group
- **WHEN** the system date is 2026-10-01, group `G01` has group ticket `TG01` for 2026-10-15 and `GROUP ADD;G01;luis@correo.com`, `GROUP REMOVE;G01;ana@correo.com` or `GROUP DELETE;G01` is received
- **THEN** each command is rejected with an error and `G01` is unchanged

#### Scenario: Ticket for today still locks
- **WHEN** the system date is 2026-10-15 and `GROUP DELETE;G01` is received
- **THEN** the command is rejected

#### Scenario: Unlocked after the ticket date
- **WHEN** the system date is 2026-10-16 and `GROUP ADD;G01;luis@correo.com` is received
- **THEN** Luis is added to `G01`
