# user-groups Specification

## Purpose

Defines groups of registered users — unique identifier, name and members — and the commands that create, modify and delete them.

## Requirements

### Requirement: Group creation
`GROUP CREATE;Identifier;Name` SHALL create an empty group with that identifier and name when both are non-empty and no group with that identifier exists.

#### Scenario: Create a group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed
- **THEN** the response is `GROUP CREATED` and group `G01` named `Familia Garcia` exists with no members

#### Scenario: Duplicate identifier
- **WHEN** group `G01` exists and `GROUP CREATE;G01;Otro` is processed
- **THEN** an error is reported and the existing group is unchanged

#### Scenario: Bad format
- **WHEN** `GROUP CREATE;G02` is processed
- **THEN** a format error is reported and no group is created

### Requirement: Adding members
`GROUP ADD;Identifier;Email` SHALL add the registered user with that email to the group; it SHALL be rejected when the group or the user does not exist or the user already belongs to the group.

#### Scenario: Add two users
- **WHEN** users `ana@correo.com` and `pedro@correo.com` exist and `GROUP ADD;G01;ana@correo.com` and `GROUP ADD;G01;pedro@correo.com` are processed
- **THEN** both respond `GROUP USER ADDED` and the group has two members

#### Scenario: Same user twice
- **WHEN** `ana@correo.com` is already in `G01` and `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** an error is reported and Ana appears only once in the group

#### Scenario: Unknown user or group
- **WHEN** `GROUP ADD;G01;nadie@correo.com` or `GROUP ADD;G99;ana@correo.com` is processed
- **THEN** an error is reported

### Requirement: Removing members
`GROUP REMOVE;Identifier;Email` SHALL remove that user from the group; it SHALL be rejected when the group does not exist or the user is not a member.

#### Scenario: Remove a member
- **WHEN** Ana is in `G01` and `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** the response is `GROUP USER REMOVED` and Ana is no longer a member

#### Scenario: Remove a non-member
- **WHEN** Pedro is not in `G01` and `GROUP REMOVE;G01;pedro@correo.com` is processed
- **THEN** an error is reported

### Requirement: Group deletion
`GROUP DELETE;Identifier` SHALL delete an existing group only when it has no associated group ticket dated on or after the current system date; an unknown identifier SHALL be rejected. Historical group tickets of a deleted group SHALL be kept, with their group reference cleared.

#### Scenario: Delete a group
- **WHEN** group `G01` exists and `GROUP DELETE;G01` is processed
- **THEN** the response is `GROUP DELETED` and `G01` no longer exists, while its former members remain registered users

#### Scenario: Unknown group
- **WHEN** `GROUP DELETE;G99` is processed
- **THEN** an error is reported

#### Scenario: Group with an active group ticket
- **WHEN** `G01` has a group ticket dated today or later
- **THEN** `GROUP DELETE;G01` reports an error and the group remains

#### Scenario: Group with only historical tickets
- **WHEN** `G01`'s only group ticket `TG01` is dated before the current date and `GROUP DELETE;G01` is processed
- **THEN** the group is deleted and `TG01` still exists with no group reference, keeping its identifier, date, price and composition

### Requirement: Groups contain only existing users
Group members SHALL be existing registered users; deleting a user with USER DELETE SHALL also remove that user from every group.

#### Scenario: Deleted user leaves its groups
- **WHEN** Ana belongs to `G01` and `USER DELETE;ana@correo.com;Clave1!` succeeds
- **THEN** `G01` no longer contains Ana

### Requirement: Locked groups cannot change composition
GROUP ADD and GROUP REMOVE SHALL be rejected while the group has an associated group ticket dated on or after the current system date.

#### Scenario: Add to a locked group
- **WHEN** `G01` has an active group ticket and `GROUP ADD;G01;luis@correo.com` is processed
- **THEN** an error is reported and the membership is unchanged

#### Scenario: Remove from a locked group
- **WHEN** `G01` has an active group ticket and `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** an error is reported and Ana remains a member
