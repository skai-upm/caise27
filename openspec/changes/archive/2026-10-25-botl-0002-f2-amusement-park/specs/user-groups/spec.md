# Spec Delta

## Purpose

Organises existing users into named groups so they can act together, for example when buying group tickets.

## ADDED Requirements

### Requirement: Create group
The system SHALL accept `GROUP CREATE;<id>;<nombre>` and create an empty group with that identifier and non-blank name. Group identifiers SHALL be unique.

#### Scenario: Create group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed
- **THEN** the system reports `OK GROUP CREATED` and group G01 exists with no members

#### Scenario: Duplicate identifier
- **WHEN** group G01 exists and `GROUP CREATE;G01;Otro` is processed
- **THEN** the system reports an error

### Requirement: Add member
The system SHALL accept `GROUP ADD;<id>;<correo>` and add the existing user with that email to the group. The group SHALL reference the registered user itself, not a copy. Unknown groups, unknown users and users already in the group SHALL be rejected.

#### Scenario: Add existing user
- **WHEN** user ana@correo.com exists and `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** the system reports `OK GROUP USER ADDED` and Ana is a member of G01

#### Scenario: Member reflects user changes
- **WHEN** Ana is a member of G01 and her phone is changed with `USER UPDATE`
- **THEN** the member of G01 shows the new phone

#### Scenario: Unknown user
- **WHEN** `GROUP ADD;G01;nadie@correo.com` is processed and no such user exists
- **THEN** the system reports an error and the group is unchanged

#### Scenario: Duplicate member
- **WHEN** Ana is already a member of G01 and `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** the system reports an error and Ana appears once in G01

### Requirement: Remove member
The system SHALL accept `GROUP REMOVE;<id>;<correo>` and remove that user from the group; it SHALL report an error if the group does not exist or the user is not a member.

#### Scenario: Remove member
- **WHEN** Ana is a member of G01 and `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** the system reports `OK GROUP USER REMOVED` and Ana is no longer a member

#### Scenario: Not a member
- **WHEN** Ana is not a member of G01 and `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** the system reports an error

### Requirement: Delete group
The system SHALL accept `GROUP DELETE;<id>` and delete the group; the users themselves SHALL NOT be deleted.

#### Scenario: Delete group
- **WHEN** group G01 exists without current or future group tickets and `GROUP DELETE;G01` is processed
- **THEN** the system reports `OK GROUP DELETED`, G01 no longer exists and its former members still exist

### Requirement: Locked group
While a group has any group ticket dated on or after the system date, `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` on that group SHALL be rejected. When all its group tickets are in the past, the group SHALL be modifiable again.

#### Scenario: Locked by future group ticket
- **WHEN** the system date is 2026-10-01 and G01 has a group ticket for 2026-10-15
- **THEN** adding, removing members and deleting G01 are rejected with an error

#### Scenario: Unlocked after date advances
- **WHEN** the system date is then set to 2026-10-16
- **THEN** members can be added to and removed from G01 and G01 can be deleted

### Requirement: Group persistence
Groups, their names and their members SHALL be stored in the data directory and restored at startup, linking members to the restored users.

#### Scenario: Groups survive restart
- **WHEN** G01 with members Ana and Pedro exists and the application restarts
- **THEN** G01 exists with the same name and members, and each member is the same user object as the registered user
