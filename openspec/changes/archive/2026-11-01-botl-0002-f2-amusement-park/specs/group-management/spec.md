# Spec Delta

## Purpose

Organises existing registered users into named groups that reference those users, persist across executions, and are locked while they hold current or future group tickets.

## ADDED Requirements

### Requirement: Group model
A group SHALL have a unique identifier, a name, and a collection of references to registered users. A group SHALL NOT contain independent copies of users: a member is the same user that the user commands manage.

#### Scenario: Member reflects user updates
- **GIVEN** user `ana@correo.com` is a member of group `G01`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed
- **THEN** group `G01`'s member shows the name `Ana Maria`

### Requirement: GROUP CREATE command
`GROUP CREATE;<Id>;<Nombre>` SHALL create an empty group. Creation SHALL be rejected with an error message when the identifier is already used by another group or a parameter is missing or empty.

#### Scenario: Create group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed
- **THEN** group `G01` named `Familia Garcia` exists with no members and a success message is reported

#### Scenario: Duplicate group identifier
- **GIVEN** group `G01` exists
- **WHEN** `GROUP CREATE;G01;Otro` is processed
- **THEN** an error message is reported and the existing group is unchanged

### Requirement: GROUP ADD command
`GROUP ADD;<Id>;<Correo>` SHALL add the registered user with that email to the group. It SHALL be rejected without changing membership when the group does not exist, no registered user has that email, the user is already a member, or the group is locked.

#### Scenario: Add existing user
- **GIVEN** group `G01` and registered user `ana@correo.com`
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** Ana is a member of `G01`

#### Scenario: Invalid membership
- **GIVEN** group `G01` with member `ana@correo.com`
- **WHEN** `GROUP ADD;G01;nadie@correo.com` or `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** an error message is reported and membership is unchanged

### Requirement: GROUP REMOVE command
`GROUP REMOVE;<Id>;<Correo>` SHALL remove the user with that email from the group. It SHALL be rejected without changes when the group does not exist, the user is not a member, or the group is locked.

#### Scenario: Remove member
- **GIVEN** group `G01` with member `ana@correo.com` and no current or future group ticket
- **WHEN** `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** Ana is no longer a member and remains a registered user

### Requirement: GROUP DELETE command
`GROUP DELETE;<Id>` SHALL delete the group without deleting its users. It SHALL be rejected when the group does not exist or is locked.

#### Scenario: Delete group
- **GIVEN** group `G01` with members and no current or future group ticket
- **WHEN** `GROUP DELETE;G01` is processed
- **THEN** the group no longer exists and its former members remain registered users

### Requirement: Group locking by active group tickets
A group SHALL be locked while it has at least one group ticket dated on or after the current system date. While locked, `GROUP ADD`, `GROUP REMOVE`, and `GROUP DELETE` SHALL be rejected. The lock SHALL be evaluated against the current system date at each request and SHALL NOT be stored as an independent flag.

#### Scenario: Locked and unlocked group
- **GIVEN** system date 2026-10-01 and a group ticket for `G01` dated 2026-10-15
- **WHEN** `GROUP ADD`, `GROUP REMOVE`, or `GROUP DELETE` is requested for `G01`
- **THEN** the request is rejected
- **AND WHEN** `SYSTEM DATE;2026-10-16` is executed and the request is repeated
- **THEN** the request is accepted

#### Scenario: Ticket dated today locks the group
- **GIVEN** system date 2026-10-15 and a group ticket for `G01` dated 2026-10-15
- **WHEN** `GROUP DELETE;G01` is requested
- **THEN** the request is rejected

### Requirement: Group persistence
Groups and their membership SHALL be persisted to a text file after every successful change and restored at startup, with members resolved to the restored registered users.

#### Scenario: Restore membership
- **GIVEN** existing users have been added to a group
- **WHEN** the application restarts in normal persistence mode
- **THEN** the group contains references to the correctly restored users, with no duplicated users
