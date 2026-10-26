# Spec Delta

## Purpose

Organises registered users into named groups, keeping references to the existing users and persisting groups and memberships between executions.

## ADDED Requirements

### Requirement: Create group
The system SHALL create an empty group with `GROUP CREATE;Id;Nombre` when the identifier is not empty and not already used and the name is not empty.

#### Scenario: New group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed
- **THEN** group `G01` named `Familia Garcia` exists with no members

#### Scenario: Duplicate identifier
- **WHEN** `GROUP CREATE;G01;Otro` is processed and `G01` already exists
- **THEN** an error is reported and the existing group is unchanged

### Requirement: Delete group
The system SHALL delete a group with `GROUP DELETE;Id`; the member users SHALL NOT be deleted.

#### Scenario: Delete existing group
- **WHEN** `GROUP DELETE;G01` is processed and `G01` exists
- **THEN** the group no longer exists and its former members remain registered

#### Scenario: Unknown group
- **WHEN** `GROUP DELETE;G99` is processed and `G99` does not exist
- **THEN** an error is reported

### Requirement: Add member
The system SHALL add the registered user identified by the email to the group with `GROUP ADD;Id;Correo`. Users that do not exist SHALL NOT be added and a user SHALL NOT appear twice in the same group.

#### Scenario: Add existing user
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed and Ana is registered
- **THEN** Ana is a member of `G01`

#### Scenario: Nonexistent user
- **WHEN** `GROUP ADD;G01;nadie@correo.com` is processed and no such user exists
- **THEN** an error is reported and the group is unchanged

#### Scenario: Duplicate member
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed and Ana is already a member
- **THEN** an error is reported and Ana appears only once

### Requirement: Remove member
The system SHALL remove a member from a group with `GROUP REMOVE;Id;Correo`; removing a user that is not a member SHALL be reported as an error.

#### Scenario: Remove member
- **WHEN** `GROUP REMOVE;G01;ana@correo.com` is processed and Ana is a member
- **THEN** Ana is no longer a member and remains registered

### Requirement: Groups reference existing users
Group members SHALL be the same user objects managed by the system, not independent copies, so changes to a user are visible through every group containing it.

#### Scenario: Updated user seen through group
- **WHEN** Ana is a member of `G01` and her first name is updated
- **THEN** the member of `G01` shows the new first name

### Requirement: Group persistence
Groups, their names and their members SHALL be stored in a text file and restored when the application starts, linking members to the restored users.

#### Scenario: Restart
- **WHEN** `G01` with members Ana and Pedro is saved and the application restarts
- **THEN** `G01` exists with Ana and Pedro as members

### Requirement: Group commands in HELP
`HELP` SHALL list the syntax of `GROUP CREATE;Id;Nombre`, `GROUP ADD;Id;Correo`, `GROUP REMOVE;Id;Correo` and `GROUP DELETE;Id`.

#### Scenario: Help shows group commands
- **WHEN** `HELP` is processed
- **THEN** the output contains the four group command syntaxes
