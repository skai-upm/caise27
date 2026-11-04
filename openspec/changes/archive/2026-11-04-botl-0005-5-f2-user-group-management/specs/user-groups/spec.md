# Spec Delta

## Purpose

Lets registered users be organized into named groups, managed by text commands, where each group refers to the existing user records rather than copies of them.

## ADDED Requirements

### Requirement: Group data
A group SHALL have an identifier, a name and a collection of member users; identifiers SHALL be unique among existing groups (case-sensitive) and identifier and name SHALL be nonblank.

#### Scenario: Group created
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed and no group `G01` exists
- **THEN** group `G01` named `Familia Garcia` exists with no members and a success message is printed

#### Scenario: Duplicate identifier
- **GIVEN** group `G01` exists
- **WHEN** `GROUP CREATE;G01;Otra Familia` is processed
- **THEN** it is rejected and group `G01` keeps its name and members

#### Scenario: Malformed create
- **WHEN** `GROUP CREATE;G01`, `GROUP CREATE;;Familia` or `GROUP CREATE;G01;Familia;x` is processed
- **THEN** it is rejected with a usage error and no group is created

### Requirement: Add existing user to group
`GROUP ADD;<groupId>;<email>` SHALL add the registered user with that email (case-insensitive) to the group; it SHALL be rejected when the group does not exist, no user has that email, or the user is already a member of that group.

#### Scenario: Add registered user
- **GIVEN** user `ana@correo.com` and group `G01` exist
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** `G01` contains that user

#### Scenario: Nonexistent user
- **WHEN** `GROUP ADD;G01;nadie@correo.com` is processed
- **THEN** it is rejected and `G01`'s members are unchanged

#### Scenario: User already in group
- **GIVEN** `ana@correo.com` is a member of `G01`
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed again
- **THEN** it is rejected and Ana appears once in `G01`

#### Scenario: Same user in different groups
- **GIVEN** `ana@correo.com` is a member of `G01` and group `G02` exists
- **WHEN** `GROUP ADD;G02;ana@correo.com` is processed
- **THEN** Ana is a member of both groups

#### Scenario: Unknown group
- **WHEN** `GROUP ADD;G99;ana@correo.com` is processed and `G99` does not exist
- **THEN** it is rejected

### Requirement: Remove user from group
`GROUP REMOVE;<groupId>;<email>` SHALL remove that user from the group; it SHALL be rejected when the group does not exist or the user is not a member; the user itself SHALL remain registered.

#### Scenario: Remove member
- **GIVEN** Ana is a member of `G01`
- **WHEN** `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** `G01` no longer contains Ana and Ana is still a registered user

#### Scenario: Not a member
- **WHEN** `GROUP REMOVE;G01;pedro@correo.com` is processed and Pedro is not in `G01`
- **THEN** it is rejected

### Requirement: Delete group
`GROUP DELETE;<groupId>` SHALL delete the group and its memberships without deleting any user; it SHALL be rejected when the group does not exist.

#### Scenario: Delete group with members
- **GIVEN** `G01` has members Ana and Pedro
- **WHEN** `GROUP DELETE;G01` is processed
- **THEN** `G01` no longer exists and Ana and Pedro remain registered

#### Scenario: Unknown group
- **WHEN** `GROUP DELETE;G99` is processed
- **THEN** it is rejected

### Requirement: Members are the registered users
Group members SHALL be the same user records held by user management, not independent copies: changes to a user are visible through every group containing it, and deleting a user SHALL remove it from every group.

#### Scenario: Updated user seen through group
- **GIVEN** Ana is a member of `G01`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com` is processed
- **THEN** the member of `G01` has email `ana.nueva@correo.com`, and `GROUP REMOVE;G01;ana.nueva@correo.com` succeeds

#### Scenario: Deleted user leaves groups
- **GIVEN** Ana is a member of `G01` and `G02`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` succeeds
- **THEN** neither group contains Ana, now or after restart
