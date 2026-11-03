# Spec Delta

## Purpose

Organises registered users into groups with unique identifiers, and keeps group composition locked while an active group ticket exists.

## ADDED Requirements

### Requirement: Group creation
`GROUP CREATE;Identifier;Name` SHALL create an initially empty group with a unique identifier and a name.

#### Scenario: Create group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is executed
- **THEN** an empty group G01 named `Familia Garcia` exists

#### Scenario: Duplicate identifier
- **WHEN** `GROUP CREATE` uses an identifier that already exists
- **THEN** an error is reported and no group is created

### Requirement: Group membership
`GROUP ADD;Identifier;Email` SHALL add an existing user to an existing group, rejecting a user already in that group; `GROUP REMOVE;Identifier;Email` SHALL remove a member from the group.

#### Scenario: Add member once
- **WHEN** `GROUP ADD;G01;ana@correo.com` runs twice
- **THEN** Ana is a member once and the second addition is rejected

#### Scenario: Unknown user or group
- **WHEN** `GROUP ADD` names a nonexistent group or email
- **THEN** an error is reported and no membership changes

#### Scenario: Remove member
- **WHEN** `GROUP REMOVE;G01;ana@correo.com` runs for a member of a modifiable group
- **THEN** Ana is no longer a member

### Requirement: Group lock derived from active group tickets
A group SHALL be locked while at least one associated group ticket has a date equal to or later than the current system date; the lock SHALL be derived from tickets and the date, not stored. `GROUP ADD` and `GROUP REMOVE` SHALL be rejected on a locked group.

#### Scenario: Locked group
- **WHEN** a group has a group ticket for today or later and `GROUP ADD` or `GROUP REMOVE` is attempted
- **THEN** the operation is rejected

#### Scenario: Historical ticket does not lock
- **WHEN** the group's only group ticket is dated before the current system date
- **THEN** `GROUP ADD` and `GROUP REMOVE` are allowed

#### Scenario: Unlock after deleting the future ticket
- **WHEN** the group's only active group ticket is a future one and it is deleted with `GROUPTICKET DELETE`
- **THEN** the group can be modified again

### Requirement: Group deletion
`GROUP DELETE;Identifier` SHALL delete a group only when it has no associated group ticket dated on or after the current system date; historical group tickets SHALL be retained with their group reference cleared.

#### Scenario: Delete with active ticket
- **WHEN** a group with a group ticket for today is deleted
- **THEN** the deletion is rejected

#### Scenario: Delete with historical tickets
- **WHEN** a group whose group tickets are all historical is deleted
- **THEN** the group is removed and its historical group tickets remain with identifier, date, price and composition
