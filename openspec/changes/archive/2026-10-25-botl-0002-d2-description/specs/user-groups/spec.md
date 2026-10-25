# Spec Delta

## Purpose

Lets registered users be organised into named groups that can purchase group tickets, with group composition protected while an active group ticket exists.

## ADDED Requirements

### Requirement: Group creation
`GROUP CREATE;Identifier;Name` SHALL create an empty group with a unique identifier and a non-blank name. A duplicate identifier SHALL be rejected.

#### Scenario: Create group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed
- **THEN** group G01 exists with no members

#### Scenario: Duplicate identifier
- **WHEN** `GROUP CREATE;G01;Otro` is processed and G01 already exists
- **THEN** an error is printed

### Requirement: Group membership
`GROUP ADD;Identifier;Email` SHALL add an existing user to an existing group; a user already in the group SHALL be rejected. `GROUP REMOVE;Identifier;Email` SHALL remove a member from the group; a user who is not a member SHALL be rejected.

#### Scenario: Add members
- **WHEN** `GROUP ADD;G01;ana@correo.com` and `GROUP ADD;G01;pedro@correo.com` are processed for existing users
- **THEN** G01 contains Ana and Pedro

#### Scenario: Duplicate member
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed and Ana already belongs to G01
- **THEN** an error is printed and G01 is unchanged

#### Scenario: Unknown user
- **WHEN** `GROUP ADD;G01;nadie@correo.com` is processed
- **THEN** an error is printed

#### Scenario: Remove member
- **WHEN** `GROUP REMOVE;G02;laura@correo.com` is processed and G02 is unlocked
- **THEN** Laura is no longer a member of G02

### Requirement: Group lock
A group SHALL be locked while at least one of its group tickets has a date on or after the current system date. While locked, `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` SHALL be rejected. The locked state SHALL be derived from existing tickets and the current date, not stored.

#### Scenario: Locked by future group ticket
- **WHEN** G02 has group ticket TG02 dated after the current date and `GROUP REMOVE;G02;laura@correo.com` is processed
- **THEN** an error is printed and Laura remains in G02

#### Scenario: Unlocked after deleting the ticket
- **WHEN** `GROUPTICKET DELETE;TG02` succeeds and G02 has no other active group ticket
- **THEN** `GROUP REMOVE;G02;laura@correo.com` succeeds

#### Scenario: Historical tickets do not lock
- **WHEN** all of G01's group tickets are dated before the current date
- **THEN** G01 can be modified and deleted

### Requirement: Group deletion
`GROUP DELETE;Identifier` SHALL delete an unlocked group. Historical group tickets of the deleted group SHALL be retained, with their reference to the group cleared.

#### Scenario: Delete unlocked group
- **WHEN** `GROUP DELETE;G02` is processed and G02 has no active group ticket
- **THEN** G02 no longer exists

#### Scenario: Ticket for today blocks deletion
- **WHEN** G01 has a group ticket dated the current system date and `GROUP DELETE;G01` is processed
- **THEN** an error is printed and G01 still exists
