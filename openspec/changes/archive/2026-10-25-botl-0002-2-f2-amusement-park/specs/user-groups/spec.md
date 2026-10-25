# Spec Delta

## Purpose

Lets existing users be organised into named groups that reference those users and persist between executions.

## ADDED Requirements

### Requirement: Create group
`GROUP CREATE;Id;Nombre` SHALL create an empty group with the given non-empty identifier and name. Identifiers SHALL be unique.

#### Scenario: Create
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is executed
- **THEN** the reply starts with `OK` and group G01 exists with no members

#### Scenario: Duplicate identifier
- **WHEN** G01 exists and `GROUP CREATE;G01;Otro` is executed
- **THEN** an error is reported

### Requirement: Add member
`GROUP ADD;Id;Correo` SHALL add the existing user with that email to the group. Unknown users, unknown groups and users already in the group SHALL be rejected. The group SHALL reference the registered user itself, not an independent copy.

#### Scenario: Add existing user
- **WHEN** `GROUP ADD;G01;ana@correo.com` is executed for a registered user
- **THEN** Ana is a member of G01

#### Scenario: Unknown user
- **WHEN** `GROUP ADD;G01;nadie@correo.com` is executed
- **THEN** an error is reported and the group is unchanged

#### Scenario: Duplicate member
- **WHEN** Ana is already in G01 and `GROUP ADD;G01;ana@correo.com` is executed
- **THEN** an error is reported

#### Scenario: Shared user object
- **WHEN** Ana is in G01 and her name is changed through `USER UPDATE`
- **THEN** the group member reflects the new name

### Requirement: Remove member
`GROUP REMOVE;Id;Correo` SHALL remove the user from the group; it SHALL be rejected if the group or user does not exist or the user is not a member.

#### Scenario: Remove
- **WHEN** `GROUP REMOVE;G01;ana@correo.com` is executed for a member
- **THEN** Ana is no longer in G01

### Requirement: Delete group
`GROUP DELETE;Id` SHALL delete an existing group without deleting its users.

#### Scenario: Delete
- **WHEN** `GROUP DELETE;G01` is executed
- **THEN** G01 no longer exists and its former members remain registered users

### Requirement: Locked groups
While a group has any group ticket dated on or after the current date, `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` on it SHALL be rejected. When all its group tickets are in the past, it SHALL be modifiable again.

#### Scenario: Locked by future group ticket
- **WHEN** the date is 2026-10-01, G01 has a group ticket for 2026-10-15 and `GROUP ADD;G01;pedro@correo.com` is executed
- **THEN** an error is reported

#### Scenario: Unlocked after the date passes
- **WHEN** `SYSTEM DATE;2026-10-16` is then executed and `GROUP ADD;G01;pedro@correo.com` is executed
- **THEN** the user is added

### Requirement: Group persistence
Groups, their names and their members SHALL be stored in a text file and restored at startup, linking members to the restored users.

#### Scenario: Restart
- **WHEN** G01 with member Ana is created, the application exits and starts again
- **THEN** G01 exists with Ana as a member
