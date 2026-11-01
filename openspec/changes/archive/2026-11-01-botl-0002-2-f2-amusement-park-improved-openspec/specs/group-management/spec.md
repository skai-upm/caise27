# Spec Delta

## Purpose

Organizes previously registered users into named groups that reference those users, with membership rules and persistence across executions.

## ADDED Requirements

### Requirement: Group structure
A group SHALL have a unique identifier, a nonempty name and a collection of references to existing registered users, without creating copies of those users.

#### Scenario: Group references the registered user
- **GIVEN** user `ana@correo.com` is registered and added to group `G01`
- **WHEN** the user's name is updated with `USER UPDATE`
- **THEN** group `G01` reflects the updated user

### Requirement: Create group command
`GROUP CREATE;IdGrupo;Nombre` SHALL create an empty group. An identifier already in use or an empty name SHALL be rejected.

#### Scenario: Create group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is executed
- **THEN** group `G01` named `Familia Garcia` exists with no members

#### Scenario: Duplicate group identifier
- **GIVEN** group `G01` exists
- **WHEN** `GROUP CREATE;G01;Otro` is executed
- **THEN** the command is rejected

### Requirement: Add member command
`GROUP ADD;IdGrupo;Correo` SHALL add the registered user with that email to the group. An unknown group, an unregistered email, a user already in the group, or a locked group SHALL be rejected without changing membership.

#### Scenario: Add existing user
- **GIVEN** group `G01` and registered user `ana@correo.com`
- **WHEN** `GROUP ADD;G01;ana@correo.com` is executed
- **THEN** the user is a member of `G01`

#### Scenario: Invalid membership
- **GIVEN** group `G01` already containing `ana@correo.com`
- **WHEN** `GROUP ADD;G01;ana@correo.com` or `GROUP ADD;G01;nadie@correo.com` is executed
- **THEN** the command is rejected and membership is unchanged

### Requirement: Remove member command
`GROUP REMOVE;IdGrupo;Correo` SHALL remove that user from the group. An unknown group, a user who is not a member, or a locked group SHALL be rejected without changing membership.

#### Scenario: Remove member
- **GIVEN** `ana@correo.com` is a member of unlocked group `G01`
- **WHEN** `GROUP REMOVE;G01;ana@correo.com` is executed
- **THEN** the user is no longer a member and remains registered

#### Scenario: Remove non-member
- **WHEN** `GROUP REMOVE` names a user who is not in the group
- **THEN** the command is rejected

### Requirement: Delete group command
`GROUP DELETE;IdGrupo` SHALL delete the group without deleting its users. An unknown or locked group SHALL be rejected.

#### Scenario: Delete group
- **GIVEN** unlocked group `G01` with members
- **WHEN** `GROUP DELETE;G01` is executed
- **THEN** the group no longer exists and its former members remain registered

### Requirement: Group persistence
Groups and their membership SHALL be persisted after every successful change and restored at startup with references to the restored users, without duplicating users.

#### Scenario: Restore membership
- **GIVEN** existing users have been added to a group
- **WHEN** the application restarts in normal persistence mode
- **THEN** the group contains references to the restored registered users and no duplicate user objects
