# user-groups Specification

## Purpose

Defines groups of registered users: their data, the commands to create, populate and delete them, their persistence, and how they stay consistent with the set of users.

## Requirements

### Requirement: Group data
A group SHALL have a non-empty identifier, a non-empty name and an ordered collection of members. Members SHALL be the registered users themselves, not independent copies, so a change to a user is visible through every group that contains it. Group identifiers are unique and compared exactly.

#### Scenario: Member reflects user updates
- **WHEN** Ana is added to group G01 and then her name is changed with `USER UPDATE`
- **THEN** the member of G01 shows the new name

### Requirement: Create group
`GROUP CREATE;<id>;<name>` SHALL create an empty group and respond `OK GROUP CREATED`. An empty identifier or name SHALL be rejected with `ERROR EMPTY FIELD`, and an identifier already in use with `ERROR DUPLICATE GROUP`.

#### Scenario: Group created
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed
- **THEN** the response is `OK GROUP CREATED` and group G01 exists with no members

#### Scenario: Duplicate identifier
- **WHEN** group G01 exists and `GROUP CREATE;G01;Otra` is processed
- **THEN** the response is `ERROR DUPLICATE GROUP`

### Requirement: Add member
`GROUP ADD;<id>;<email>` SHALL add the registered user with that email to the group and respond `OK USER ADDED TO GROUP`. It SHALL respond `ERROR GROUP NOT FOUND` for an unknown group, `ERROR USER NOT FOUND` when no user has that email, and `ERROR USER ALREADY IN GROUP` when the user is already a member.

#### Scenario: Existing user added
- **WHEN** user `ana@correo.com` exists and `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** the response is `OK USER ADDED TO GROUP` and Ana is a member of G01

#### Scenario: Nonexistent user
- **WHEN** `GROUP ADD;G01;nadie@correo.com` is processed and no user has that email
- **THEN** the response is `ERROR USER NOT FOUND` and the group is unchanged

#### Scenario: Same user twice
- **WHEN** Ana is a member of G01 and `GROUP ADD;G01;ana@correo.com` is processed again
- **THEN** the response is `ERROR USER ALREADY IN GROUP` and Ana appears only once

### Requirement: Remove member
`GROUP REMOVE;<id>;<email>` SHALL remove that user from the group and respond `OK USER REMOVED FROM GROUP`. It SHALL respond `ERROR GROUP NOT FOUND` for an unknown group and `ERROR USER NOT IN GROUP` when the email does not belong to a member.

#### Scenario: Member removed
- **WHEN** Ana is a member of G01 and `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** the response is `OK USER REMOVED FROM GROUP` and Ana is no longer a member, while her user still exists

### Requirement: Delete group
`GROUP DELETE;<id>` SHALL delete the group and respond `OK GROUP DELETED`, without deleting its users. An unknown group SHALL be rejected with `ERROR GROUP NOT FOUND`.

#### Scenario: Group deleted
- **WHEN** `GROUP DELETE;G01` is processed
- **THEN** the response is `OK GROUP DELETED`, G01 no longer exists and its former members still exist

### Requirement: Group persistence
Groups SHALL be stored in a text file (`data/groups.txt` by default), one group per line as `<id>;<name>;<member DNIs separated by ,>`, rewritten after every successful group change and loaded at startup after the users, linking each stored DNI to the restored user. Unknown DNIs and malformed lines are ignored.

#### Scenario: Groups restored after restart
- **WHEN** group G01 with Ana and Pedro is created, and the application is restarted
- **THEN** G01 exists with Ana and Pedro as members, which are the same objects as the restored users

### Requirement: Deleted users leave their groups
When a user is deleted with `USER DELETE`, the user SHALL be removed from every group that contains it, and the groups file SHALL be updated.

#### Scenario: Deleting a member
- **WHEN** Ana is a member of G01 and `USER DELETE;ana@correo.com;Clave1!` succeeds
- **THEN** G01 no longer contains Ana
