# user-groups Specification

## Purpose
Lets registered users be organised into named groups that reference the existing users, with commands to create and delete groups and to add and remove members.

## Requirements

### Requirement: Group creation
`GROUP CREATE;Id;Nombre` SHALL create an empty group with the given identifier and name. The identifier and name SHALL be nonblank and the identifier SHALL NOT already be used by another group (compared exactly).

#### Scenario: Create group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed
- **THEN** an `OK` message is produced and group `G01` named `Familia Garcia` exists with no members

#### Scenario: Duplicate identifier
- **GIVEN** group `G01` exists
- **WHEN** `GROUP CREATE;G01;Otro` is processed
- **THEN** an `ERROR` message is produced and the existing group is unchanged

#### Scenario: Invalid parameters
- **WHEN** `GROUP CREATE;;Familia`, `GROUP CREATE;G02;` or `GROUP CREATE;G02` is processed
- **THEN** an `ERROR` message is produced and no group is created

### Requirement: Group deletion
`GROUP DELETE;Id` SHALL delete an existing group without deleting its member users; deleting an unknown group SHALL be rejected.

#### Scenario: Delete group
- **GIVEN** group `G01` with member `ana@correo.com`
- **WHEN** `GROUP DELETE;G01` is processed
- **THEN** the group no longer exists and user `ana@correo.com` still exists

#### Scenario: Unknown group
- **WHEN** `GROUP DELETE;G99` is processed
- **THEN** an `ERROR` message is produced

### Requirement: Add member
`GROUP ADD;Id;Correo` SHALL add the existing registered user with that email (case-insensitive) to the group, referring to that same user rather than a copy. Unknown groups, unknown users and users already in the group SHALL be rejected.

#### Scenario: Add registered user
- **GIVEN** group `G01` and registered user `ana@correo.com`
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** an `OK` message is produced and `G01` contains that user

#### Scenario: Nonexistent user
- **WHEN** `GROUP ADD;G01;nadie@correo.com` is processed
- **THEN** an `ERROR` message is produced and no membership is created

#### Scenario: Duplicate member
- **GIVEN** `ana@correo.com` is already in `G01`
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed again
- **THEN** an `ERROR` message is produced and the user appears once in `G01`

#### Scenario: Same user in several groups
- **GIVEN** groups `G01` and `G02`
- **WHEN** `ana@correo.com` is added to both
- **THEN** both additions succeed

#### Scenario: Member reflects user updates
- **GIVEN** `ana@correo.com` is in `G01`
- **WHEN** her first name is updated with `USER UPDATE`
- **THEN** the member of `G01` shows the updated first name

### Requirement: Remove member
`GROUP REMOVE;Id;Correo` SHALL remove that user from the group without deleting the user. Unknown groups and users that are not members SHALL be rejected.

#### Scenario: Remove member
- **GIVEN** `ana@correo.com` is in `G01`
- **WHEN** `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** `G01` no longer contains her and the user still exists

#### Scenario: Not a member
- **WHEN** `GROUP REMOVE;G01;pedro@correo.com` is processed and Pedro is not in `G01`
- **THEN** an `ERROR` message is produced

### Requirement: Deleted users leave groups
When a user is deleted with `USER DELETE`, the user SHALL be removed from every group it belonged to.

#### Scenario: Delete member user
- **GIVEN** `ana@correo.com` is in `G01`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` succeeds
- **THEN** `G01` still exists and has no member with that email, also after a restart
