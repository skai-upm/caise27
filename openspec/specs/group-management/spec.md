# group-management Specification

## Purpose

Defines groups of registered park users: how they are created with a unique identifier and a name, how members are added and removed, how groups are deleted, and when a group may be modified or deleted.

## Requirements

### Requirement: Group creation
`GROUP CREATE;Identifier;Name` SHALL create an empty group with the given identifier and name. The identifier SHALL be unique among existing groups.

#### Scenario: Create a group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed and no group `G01` exists
- **THEN** a confirmation is printed and group `G01` named `Familia Garcia` exists with no members

#### Scenario: Duplicate identifier
- **WHEN** group `G01` exists and `GROUP CREATE;G01;Otro` is processed
- **THEN** an error is printed and the existing group is unchanged

#### Scenario: Wrong number of elements
- **WHEN** `GROUP CREATE;G01` is processed
- **THEN** an error describing the expected syntax is printed

### Requirement: Adding users to a group
`GROUP ADD;Identifier;Email` SHALL add the existing user with that email to the existing group. The same user SHALL NOT appear more than once in the same group.

#### Scenario: Add members progressively
- **WHEN** users `ana@correo.com` and `pedro@correo.com` exist, group `G01` exists, and `GROUP ADD;G01;ana@correo.com` then `GROUP ADD;G01;pedro@correo.com` are processed
- **THEN** both additions are confirmed and `G01` contains both users

#### Scenario: Duplicate member rejected
- **WHEN** `ana@correo.com` already belongs to `G01` and `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** an error is printed and Ana appears only once in `G01`

#### Scenario: Unknown user or group
- **WHEN** `GROUP ADD;G01;nadie@correo.com` (no such user) or `GROUP ADD;G99;ana@correo.com` (no such group) is processed
- **THEN** an error is printed and no group changes

### Requirement: Removing users from a group
`GROUP REMOVE;Identifier;Email` SHALL remove the user with that email from the group.

#### Scenario: Remove a member
- **WHEN** `ana@correo.com` belongs to `G01` and `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** a confirmation is printed and Ana no longer belongs to `G01`

#### Scenario: User not in group
- **WHEN** `pedro@correo.com` does not belong to `G01` and `GROUP REMOVE;G01;pedro@correo.com` is processed
- **THEN** an error is printed

### Requirement: Group deletion
`GROUP DELETE;Identifier` SHALL delete the existing group.

#### Scenario: Delete a group
- **WHEN** group `G01` exists with no group tickets and `GROUP DELETE;G01` is processed
- **THEN** a confirmation is printed and `G01` no longer exists

#### Scenario: Unknown group
- **WHEN** `GROUP DELETE;G99` is processed and no group `G99` exists
- **THEN** an error is printed

### Requirement: Group modifiability derived from tickets
A group SHALL be modifiable (GROUP ADD, GROUP REMOVE) and deletable (GROUP DELETE) only when it has no associated group ticket dated on or after the current system date. This locked state SHALL be computed from the existing tickets and the current system date and SHALL NOT be stored as a group attribute.

#### Scenario: Group without tickets is modifiable
- **WHEN** group `G01` has no associated group tickets
- **THEN** GROUP ADD, GROUP REMOVE and GROUP DELETE are allowed

### Requirement: Groups contain only existing users
A group SHALL consist only of users that exist in the system; when a user is deleted, the user SHALL no longer be a member of any group.

#### Scenario: Deleted user leaves groups
- **WHEN** `ana@correo.com` belongs to `G01` and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** `G01` no longer contains Ana
