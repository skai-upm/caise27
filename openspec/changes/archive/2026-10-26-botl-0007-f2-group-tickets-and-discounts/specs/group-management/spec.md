# Spec Delta

## ADDED Requirements

### Requirement: Group lock
While a group has any group ticket dated on or after the system date, the group SHALL be locked: `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` on it SHALL be rejected with an `ERROR` line and change nothing. When all of its group tickets are dated before the system date, the group SHALL accept these commands again.

#### Scenario: Locked by a future group ticket
- **WHEN** the system date is 2026-10-01 and group `G01` has group ticket `TG01` dated 2026-10-15
- **THEN** `GROUP ADD;G01;luis@correo.com`, `GROUP REMOVE;G01;ana@correo.com` and `GROUP DELETE;G01` are each rejected with an `ERROR` line

#### Scenario: Locked on the ticket date
- **WHEN** the system date is 2026-10-15 and `TG01` is dated 2026-10-15
- **THEN** `G01` is still locked

#### Scenario: Unlocked when tickets are past
- **WHEN** the system date is moved to 2026-10-16 and `TG01` is the only group ticket of `G01`
- **THEN** `GROUP ADD;G01;luis@correo.com` succeeds

### Requirement: Deleted groups remove their past group tickets
When an unlocked group is deleted, its group tickets (all of them past) and their internal individual tickets SHALL also be removed, so no group ticket refers to a non-existent group.

#### Scenario: Group with past group ticket deleted
- **WHEN** `G01` only has past group ticket `TG01` and `GROUP DELETE;G01` is processed
- **THEN** `G01` and `TG01` no longer exist
