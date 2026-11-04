# user-groups Specification

## Purpose

Lets registered users be organised into named groups whose members are the existing users of the system, managed through text commands.

## Requirements

### Requirement: Group data
Each group SHALL have an identifier, a name and a collection of member users. Members SHALL be the same user entities managed by the system, not independent copies, so changes to a user (for example a new email) are visible through every group that contains them.

#### Scenario: Member reflects user changes
- **GIVEN** group `G01` contains the user with email `ana@correo.com`
- **WHEN** that user's email is updated to `ana.nueva@correo.com`
- **THEN** `GROUP REMOVE;G01;ana.nueva@correo.com` succeeds, because the group refers to the same user

### Requirement: Create group
The system SHALL process `GROUP CREATE;Id;Nombre`, creating an empty group when the identifier and name are non-blank and no group with that identifier exists. Identifiers are compared exactly (case-sensitive).

#### Scenario: Create a group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed and no group `G01` exists
- **THEN** the result starts with `OK` and group `G01` named `Familia Garcia` exists with no members

#### Scenario: Duplicate identifier
- **GIVEN** group `G01` exists
- **WHEN** `GROUP CREATE;G01;Otra` is processed
- **THEN** the result starts with `ERROR` and the existing group is unchanged

#### Scenario: Blank or missing values
- **WHEN** `GROUP CREATE;;Familia`, `GROUP CREATE;G02;` or `GROUP CREATE;G02` is processed
- **THEN** the result starts with `ERROR` and no group is created

### Requirement: Add member
The system SHALL process `GROUP ADD;Id;Correo`, adding the registered user with that email (case-insensitive, as for user commands) to the group. It SHALL reject the command if the group does not exist, no user has that email, or the user is already a member of that group.

#### Scenario: Add an existing user
- **GIVEN** group `G01` and user `ana@correo.com` exist
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** the result starts with `OK` and `G01` contains that user

#### Scenario: Same user twice
- **GIVEN** `ana@correo.com` is already in `G01`
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed again
- **THEN** the result starts with `ERROR` and the user appears only once in `G01`

#### Scenario: Nonexistent user or group
- **WHEN** `GROUP ADD;G01;nadie@correo.com` is processed with no such user, or `GROUP ADD;G99;ana@correo.com` with no group `G99`
- **THEN** the result starts with `ERROR` and no membership is created

#### Scenario: Same user in two groups
- **GIVEN** groups `G01` and `G02` exist
- **WHEN** `ana@correo.com` is added to both
- **THEN** both additions succeed

### Requirement: Remove member
The system SHALL process `GROUP REMOVE;Id;Correo`, removing that user from the group. It SHALL reject the command if the group does not exist or the user with that email is not a member of it. Removing a member SHALL NOT delete the user.

#### Scenario: Remove a member
- **GIVEN** `ana@correo.com` is in `G01`
- **WHEN** `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** the result starts with `OK`, `G01` no longer contains her, and the user still exists

#### Scenario: Remove a non-member
- **WHEN** `GROUP REMOVE;G01;pedro@correo.com` is processed and Pedro is not in `G01`
- **THEN** the result starts with `ERROR` and `G01` is unchanged

### Requirement: Delete group
The system SHALL process `GROUP DELETE;Id`, removing the group and its memberships but not its member users. It SHALL reject the command if the group does not exist.

#### Scenario: Delete a group
- **GIVEN** group `G01` with member `ana@correo.com`
- **WHEN** `GROUP DELETE;G01` is processed
- **THEN** the result starts with `OK`, `G01` no longer exists and the user still exists

#### Scenario: Delete a nonexistent group
- **WHEN** `GROUP DELETE;G99` is processed and no group `G99` exists
- **THEN** the result starts with `ERROR`

### Requirement: Deleted users leave their groups
When a user is deleted with `USER DELETE`, the system SHALL remove that user from every group, so that no group contains a user who no longer exists in the system.

#### Scenario: User deleted while in a group
- **GIVEN** `ana@correo.com` is a member of `G01`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` succeeds
- **THEN** `G01` no longer contains her, also after a restart

### Requirement: Group command parameters
Each `GROUP` command SHALL be rejected with `ERROR`, without changes, when it has fewer or more parameters than its format (`GROUP CREATE;Id;Nombre`, `GROUP ADD;Id;Correo`, `GROUP REMOVE;Id;Correo`, `GROUP DELETE;Id`).

#### Scenario: Wrong parameter count
- **WHEN** `GROUP DELETE;G01;extra` or `GROUP ADD;G01` is processed
- **THEN** the result starts with `ERROR` and no group data changes
