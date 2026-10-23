# Spec Delta

## Purpose

Defines how registered users are organised into identified, named groups that can buy group tickets, and when a group's composition may change.

## ADDED Requirements

### Requirement: Group creation
The command `GROUP CREATE;Identifier;Name` SHALL create an empty group when the identifier is not used by another group and both values are non-empty; on success it SHALL print `GROUP CREATED`.

#### Scenario: Create group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed
- **THEN** group G01 exists with no members

#### Scenario: Duplicate identifier
- **WHEN** `GROUP CREATE;G01;Otro` is processed while G01 exists
- **THEN** an error is printed

### Requirement: Group membership
`GROUP ADD;Identifier;Email` SHALL add an existing user to an existing group and `GROUP REMOVE;Identifier;Email` SHALL remove a member; a user SHALL NOT appear twice in the same group, and removing a non-member SHALL be rejected.

#### Scenario: Add members
- **WHEN** `GROUP ADD;G01;ana@correo.com` and `GROUP ADD;G01;pedro@correo.com` are processed
- **THEN** G01 contains both users

#### Scenario: Duplicate member
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed while Ana already belongs to G01
- **THEN** an error is printed and the group is unchanged

#### Scenario: Unknown user or group
- **WHEN** `GROUP ADD` names a group or email that does not exist
- **THEN** an error is printed

### Requirement: Group locking
A group SHALL be locked while at least one group ticket bought by it has a date on or after the current system date; locking SHALL be derived from tickets and the current date, not stored; `GROUP ADD` and `GROUP REMOVE` SHALL be rejected on a locked group.

#### Scenario: Locked by active ticket
- **WHEN** G01 has a group ticket for today and `GROUP ADD;G01;laura@correo.com` is processed
- **THEN** an error is printed and the group is unchanged

#### Scenario: Unlocked by passing time
- **WHEN** G01's only group ticket is dated 2027-04-15 and `SYSTEM DATE;2027-04-16` is processed
- **THEN** `GROUP ADD` and `GROUP REMOVE` on G01 succeed again

#### Scenario: Unlocked by deleting the future ticket
- **WHEN** G02's only active group ticket is dated after today and it is deleted with `GROUPTICKET DELETE`
- **THEN** G02 can be modified again

### Requirement: Group deletion
`GROUP DELETE;Identifier` SHALL delete a group only when it is not locked; historical group tickets of a deleted group SHALL be kept, losing only their reference to the group.

#### Scenario: Delete unlocked group
- **WHEN** `GROUP DELETE;G02` is processed and G02 has no active group tickets
- **THEN** G02 no longer exists and `GROUP DELETED` is printed

#### Scenario: Delete locked group
- **WHEN** `GROUP DELETE;G01` is processed while G01 has a group ticket for today
- **THEN** an error is printed and G01 still exists
