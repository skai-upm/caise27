# group-management Specification

## Purpose

Defines how registered users are organised into groups: creating and deleting groups, adding and removing members, the membership rules, and persistence of groups and memberships between executions.

## Requirements

### Requirement: Group data
Each group SHALL have an identifier, a name and a collection of member users. Identifiers are unique and compared exactly; identifier and name are required and cannot be blank.

#### Scenario: Group fields
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed
- **THEN** a group with identifier `G01`, name `Familia Garcia` and no members exists

### Requirement: Create group
The command `GROUP CREATE;<id>;<name>` SHALL create an empty group and print `OK GROUP CREATED`. If a group with that identifier already exists, or a value is blank, no group is created and an `ERROR` line is printed.

#### Scenario: Group created
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed and no group `G01` exists
- **THEN** the group is created and `OK GROUP CREATED` is printed

#### Scenario: Duplicate identifier
- **WHEN** `GROUP CREATE;G01;Otro` is processed and group `G01` already exists
- **THEN** no group is created and an `ERROR` line is printed

### Requirement: Delete group
The command `GROUP DELETE;<id>` SHALL delete an existing group and print `OK GROUP DELETED`; its members remain registered users. Deleting a non-existent group SHALL print an `ERROR` line.

#### Scenario: Group deleted
- **WHEN** `GROUP DELETE;G01` is processed and `G01` exists with members
- **THEN** the group no longer exists and its former members are still registered

#### Scenario: Unknown group
- **WHEN** `GROUP DELETE;G99` is processed and `G99` does not exist
- **THEN** an `ERROR` line is printed

### Requirement: Add member
The command `GROUP ADD;<id>;<email>` SHALL add the registered user with that email to the group and print `OK USER ADDED TO GROUP`. The group SHALL hold a reference to the existing user, not a copy. The command SHALL fail with an `ERROR` line, changing nothing, if the group does not exist, no user has that email, or the user is already a member.

#### Scenario: Existing user added
- **WHEN** user `ana@correo.com` exists and `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** Ana is a member of `G01` and `OK USER ADDED TO GROUP` is printed

#### Scenario: Non-existent user
- **WHEN** `GROUP ADD;G01;nadie@correo.com` is processed and no user has that email
- **THEN** no member is added and an `ERROR` line is printed

#### Scenario: Duplicate member
- **WHEN** Ana is already a member of `G01` and `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** Ana appears only once in `G01` and an `ERROR` line is printed

#### Scenario: Same user object
- **WHEN** Ana is a member of `G01` and Ana's name is changed with `USER UPDATE`
- **THEN** the member of `G01` shows the new name

### Requirement: Remove member
The command `GROUP REMOVE;<id>;<email>` SHALL remove that user from the group and print `OK USER REMOVED FROM GROUP`. It SHALL fail with an `ERROR` line if the group does not exist or the user is not a member.

#### Scenario: Member removed
- **WHEN** Ana is a member of `G01` and `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** Ana is no longer a member and remains a registered user

#### Scenario: Not a member
- **WHEN** `GROUP REMOVE;G01;pedro@correo.com` is processed and Pedro is not a member of `G01`
- **THEN** an `ERROR` line is printed

### Requirement: Deleted users leave their groups
When a user is deleted, that user SHALL be removed from every group it belonged to, and the stored groups SHALL be updated.

#### Scenario: User deleted while in a group
- **WHEN** Ana is a member of `G01` and `USER DELETE;ana@correo.com;Clave1!` succeeds
- **THEN** `G01` no longer contains Ana

### Requirement: Group persistence
Groups and their memberships SHALL be stored in a text file, one group per line, and restored when the application starts, linking each group to the already loaded user objects. Members SHALL be identified in the file by DNI, which never changes. After every successful group change the file SHALL reflect the current groups.

#### Scenario: Groups survive restart
- **WHEN** group `G01` with members Ana and Pedro is created, the application terminates, and it is started again
- **THEN** `G01` exists with Ana and Pedro as members, referring to the same user objects as the user list

#### Scenario: Member email changed before restart
- **WHEN** Ana is a member of `G01`, Ana's email is changed, and the application restarts
- **THEN** `G01` still contains Ana
