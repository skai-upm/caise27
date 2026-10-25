# user-groups Specification

## Purpose

Lets registered users be organised into named groups that can later buy group tickets, keeping the relationship with the existing user objects across runs.

## Requirements

### Requirement: Create group
`GROUP CREATE;Id;Name` SHALL create an empty group with a unique identifier and a non-empty name. Identifiers SHALL consist only of letters, digits, `-` and `_`. A duplicate or invalid identifier, or an empty name, SHALL be rejected.

#### Scenario: Group created
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is executed
- **THEN** group G01 named `Familia Garcia` exists with no members

#### Scenario: Duplicate identifier
- **WHEN** group G01 exists and `GROUP CREATE;G01;Otro` is executed
- **THEN** an error is shown and no group is created

### Requirement: Add member
`GROUP ADD;Id;Email` SHALL add the registered user with that email to the group. The group SHALL reference the existing user, not a copy. Unknown groups, unknown users and users already in the group SHALL be rejected.

#### Scenario: Member added
- **WHEN** user `ana@correo.com` exists and `GROUP ADD;G01;ana@correo.com` is executed
- **THEN** Ana is a member of G01

#### Scenario: Unknown user
- **WHEN** `GROUP ADD;G01;nadie@correo.com` is executed and no such user exists
- **THEN** an error is shown and the group is unchanged

#### Scenario: Duplicate member
- **WHEN** Ana is already in G01 and `GROUP ADD;G01;ana@correo.com` is executed
- **THEN** an error is shown and Ana appears only once

#### Scenario: Shared user object
- **WHEN** Ana is a member of G01 and her first name is changed with `USER UPDATE`
- **THEN** the group's member reflects the new first name

### Requirement: Remove member
`GROUP REMOVE;Id;Email` SHALL remove that user from the group. Unknown groups and users that are not members SHALL be rejected.

#### Scenario: Member removed
- **WHEN** Ana is in G01 and `GROUP REMOVE;G01;ana@correo.com` is executed
- **THEN** Ana is no longer a member of G01

### Requirement: Delete group
`GROUP DELETE;Id` SHALL delete the group. Unknown groups SHALL be rejected.

#### Scenario: Group deleted
- **WHEN** G01 exists, is not locked, and `GROUP DELETE;G01` is executed
- **THEN** G01 no longer exists

### Requirement: Group lock
While a group has any group ticket dated on or after the system date, `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` on it SHALL be rejected. When all its group tickets are dated before the system date the group SHALL be modifiable again.

#### Scenario: Locked group
- **WHEN** the system date is 2026-10-01, G01 has a group ticket for 2026-10-15, and `GROUP ADD`, `GROUP REMOVE` or `GROUP DELETE` is executed on G01
- **THEN** each operation is rejected and the group is unchanged

#### Scenario: Unlocked after the date passes
- **WHEN** `SYSTEM DATE;2026-10-16` is then executed
- **THEN** members can be added to or removed from G01 and G01 can be deleted

### Requirement: Group persistence
Groups, their names and their members SHALL be stored in a text file next to the users file and restored at startup, with members linked to the restored user objects.

#### Scenario: Groups restored
- **WHEN** G01 with member Ana was saved and the application restarts
- **THEN** G01 exists with Ana as a member
