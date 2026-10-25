# Spec Delta

## Purpose

Allows registered users to be organised into named groups that reference the existing users, with file persistence across executions.

## ADDED Requirements

### Requirement: Group data
Each group SHALL have an identifier, a name and a collection of member users. Members SHALL be the same user objects registered in the system, not independent copies, so later user updates are visible through the group.

#### Scenario: Member reflects user update
- **WHEN** user `ana@correo.com` is a member of G01 and changes its first name with `USER UPDATE`
- **THEN** the member of G01 shows the new first name

### Requirement: Identifier format
Group, ticket and attraction identifiers SHALL be non-empty and contain only letters, digits, `-` or `_`.

#### Scenario: Invalid identifier
- **WHEN** `GROUP CREATE;G 01;Familia` is processed
- **THEN** an `ERROR` result is printed and no group is created

### Requirement: Create group
`GROUP CREATE;id;name` SHALL create an empty group when the identifier is valid and unused and the name is not empty.

#### Scenario: Create group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed and G01 does not exist
- **THEN** group G01 named `Familia Garcia` is created with no members and `OK GROUP CREATED` is printed

#### Scenario: Duplicate identifier
- **WHEN** `GROUP CREATE;G01;Otro` is processed and G01 already exists
- **THEN** an `ERROR` result is printed

### Requirement: Add member
`GROUP ADD;id;email` SHALL add the existing user with that email to the group. Unknown users SHALL NOT be added and a user SHALL NOT appear twice in the same group.

#### Scenario: Add existing user
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed and the user exists and is not in G01
- **THEN** the user becomes a member of G01 and `OK GROUP MEMBER ADDED` is printed

#### Scenario: Unknown user
- **WHEN** `GROUP ADD;G01;nadie@correo.com` is processed and no user has that email
- **THEN** an `ERROR` result is printed and the group is unchanged

#### Scenario: Duplicate member
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed and the user is already in G01
- **THEN** an `ERROR` result is printed and the user appears once

### Requirement: Remove member
`GROUP REMOVE;id;email` SHALL remove the user with that email from the group when it is a member.

#### Scenario: Remove member
- **WHEN** `GROUP REMOVE;G01;ana@correo.com` is processed and the user is in G01
- **THEN** the user is no longer a member and `OK GROUP MEMBER REMOVED` is printed

#### Scenario: Not a member
- **WHEN** `GROUP REMOVE;G01;pedro@correo.com` is processed and the user is not in G01
- **THEN** an `ERROR` result is printed

### Requirement: Delete group
`GROUP DELETE;id` SHALL delete an existing group. Its users SHALL remain registered.

#### Scenario: Delete group
- **WHEN** `GROUP DELETE;G01` is processed and G01 exists and is not locked
- **THEN** G01 no longer exists, its former members still exist, and `OK GROUP DELETED` is printed

#### Scenario: Unknown group
- **WHEN** `GROUP DELETE;G99` is processed and G99 does not exist
- **THEN** an `ERROR` result is printed

### Requirement: Group persistence
Groups and their memberships SHALL be stored in a semicolon-separated text file and restored at startup, linking members to the restored users. Membership changes SHALL be saved after every successful group command.

#### Scenario: Groups survive restart
- **WHEN** group G01 with member `ana@correo.com` is created, the application exits and starts again
- **THEN** G01 exists with that same user as member
