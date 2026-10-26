# Spec Delta

## Purpose

Lets registered users be organised into named groups whose members are the existing users, with the groups persisted across executions.

## ADDED Requirements

### Requirement: Create group
The command `GROUP CREATE;Id;Nombre` SHALL create an empty group with the given identifier and name. The identifier and name SHALL be non-empty and the identifier SHALL NOT already be used by another group; otherwise an error SHALL be reported and nothing created.

#### Scenario: Group created
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is received and no group `G01` exists
- **THEN** group `G01` named `Familia Garcia` with no members exists and a confirmation is printed

#### Scenario: Duplicate identifier
- **WHEN** group `G01` exists and `GROUP CREATE;G01;Otra` is received
- **THEN** an error is printed and group `G01` keeps its original name

### Requirement: Delete group
The command `GROUP DELETE;Id` SHALL delete an existing group; the users that were members SHALL remain registered. Deleting a non-existent group SHALL report an error.

#### Scenario: Group deleted
- **WHEN** group `G01` with member `ana@correo.com` exists and `GROUP DELETE;G01` is received
- **THEN** group `G01` no longer exists and user `ana@correo.com` is still registered

#### Scenario: Unknown group
- **WHEN** `GROUP DELETE;G99` is received and no group `G99` exists
- **THEN** an error is printed

### Requirement: Add member
The command `GROUP ADD;Id;Correo` SHALL add the registered user with that email to the group. The group SHALL exist, the user SHALL be registered, and the user SHALL NOT already be a member of that group; otherwise an error SHALL be reported and the group unchanged. The member SHALL be the registered user itself, not a copy.

#### Scenario: Existing user added
- **WHEN** group `G01` and user `ana@correo.com` exist and `GROUP ADD;G01;ana@correo.com` is received
- **THEN** Ana is a member of `G01`

#### Scenario: Nonexistent user
- **WHEN** `GROUP ADD;G01;nadie@correo.com` is received and no user has that email
- **THEN** an error is printed and `G01` is unchanged

#### Scenario: Duplicate member
- **WHEN** Ana is already in `G01` and `GROUP ADD;G01;ana@correo.com` is received
- **THEN** an error is printed and Ana appears only once in `G01`

#### Scenario: Member reflects user changes
- **WHEN** Ana is in `G01` and her name is updated with `USER UPDATE`
- **THEN** the member of `G01` shows the updated name

### Requirement: Remove member
The command `GROUP REMOVE;Id;Correo` SHALL remove the user with that email from the group. If the group does not exist or the user is not a member, an error SHALL be reported. The user SHALL remain registered.

#### Scenario: Member removed
- **WHEN** Ana is in `G01` and `GROUP REMOVE;G01;ana@correo.com` is received
- **THEN** Ana is no longer in `G01` and is still registered

#### Scenario: Not a member
- **WHEN** Ana is not in `G01` and `GROUP REMOVE;G01;ana@correo.com` is received
- **THEN** an error is printed

### Requirement: Group persistence
Groups, their names and their members SHALL be stored in a text file using only the Java standard library and restored when the application starts, with each member linked to the corresponding restored user. The file SHALL reflect every successful group change.

#### Scenario: Restart restores groups
- **WHEN** group `G01` with members Ana and Pedro is created, the application is closed and started again
- **THEN** group `G01` exists with members Ana and Pedro, who are the same users loaded from the user file
