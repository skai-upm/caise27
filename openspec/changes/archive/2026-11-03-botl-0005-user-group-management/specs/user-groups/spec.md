# Spec Delta

## Purpose

Lets registered users be organised into named groups that refer to the existing users, as the basis for group-level park operations.

## ADDED Requirements

### Requirement: Group creation
The command `GROUP CREATE;Id;Nombre` SHALL create an empty group with the given non-blank identifier and non-blank name. An identifier already used by another group SHALL be rejected.

#### Scenario: Create a group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed
- **THEN** a success response is returned and group `G01` named `Familia Garcia` exists with no members

#### Scenario: Duplicate identifier
- **GIVEN** group `G01` exists
- **WHEN** `GROUP CREATE;G01;Otra` is processed
- **THEN** an error is reported and group `G01` keeps its name and members

#### Scenario: Missing data
- **WHEN** `GROUP CREATE;G02` or `GROUP CREATE;;Nombre` is processed
- **THEN** an error is reported and no group is created

### Requirement: Add a member
The command `GROUP ADD;Id;Correo` SHALL add the registered user with that email to the group. It SHALL be rejected if the group does not exist, if no registered user has that email, or if the user already belongs to that group.

#### Scenario: Add an existing user
- **GIVEN** group `G01` and registered user `ana@correo.com`
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** the group contains that registered user

#### Scenario: Nonexistent user
- **WHEN** `GROUP ADD;G01;nadie@correo.com` is processed
- **THEN** an error is reported and no membership is created

#### Scenario: Duplicate member
- **GIVEN** `ana@correo.com` already belongs to `G01`
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed again
- **THEN** an error is reported and the user appears only once in the group

#### Scenario: Nonexistent group
- **WHEN** `GROUP ADD;G99;ana@correo.com` is processed
- **THEN** an error is reported

### Requirement: Members are the registered users
A group SHALL refer to the registered user objects themselves, not to independent copies, so that changes to a user (e.g. a new email or phone) are seen through every group containing it.

#### Scenario: Updated user seen through the group
- **GIVEN** `ana@correo.com` belongs to `G01`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com` is processed
- **THEN** `GROUP REMOVE;G01;ana.nueva@correo.com` succeeds

### Requirement: Remove a member
The command `GROUP REMOVE;Id;Correo` SHALL remove the user with that email from the group; it SHALL be rejected if the group does not exist or the user is not a member. The user itself SHALL remain registered.

#### Scenario: Remove a member
- **GIVEN** `ana@correo.com` belongs to `G01`
- **WHEN** `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** `G01` no longer contains Ana and Ana is still a registered user

#### Scenario: User not in group
- **WHEN** `GROUP REMOVE;G01;pedro@correo.com` is processed and Pedro is not a member
- **THEN** an error is reported and the group is unchanged

### Requirement: Group deletion
The command `GROUP DELETE;Id` SHALL delete the group without deleting its member users; it SHALL be rejected if the group does not exist.

#### Scenario: Delete a group
- **GIVEN** group `G01` with member Ana
- **WHEN** `GROUP DELETE;G01` is processed
- **THEN** `G01` no longer exists and Ana is still registered

#### Scenario: Nonexistent group
- **WHEN** `GROUP DELETE;G99` is processed
- **THEN** an error is reported

### Requirement: Deleted users leave their groups
When a user is deleted with `USER DELETE`, the system SHALL remove that user from every group it belonged to.

#### Scenario: Cascade on user deletion
- **GIVEN** Ana belongs to `G01`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` succeeds
- **THEN** `G01` exists and no longer contains Ana

### Requirement: Malformed GROUP commands
GROUP commands with a wrong number of parameters SHALL be reported as errors without changing any group and without terminating the application.

#### Scenario: Wrong parameter count
- **WHEN** `GROUP ADD;G01` or `GROUP DELETE` is processed
- **THEN** an error is reported and no group changes
