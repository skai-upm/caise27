# Spec Delta

## Purpose

Defines groups of registered users: their creation, membership changes and deletion, and the locking derived from active group tickets.

## ADDED Requirements

### Requirement: Group creation
`GROUP CREATE;Identifier;Name` SHALL create an empty group with a unique identifier and a name. A duplicate or empty identifier MUST be rejected.

#### Scenario: Create empty group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed
- **THEN** group G01 named `Familia Garcia` exists with no members

#### Scenario: Duplicate identifier
- **WHEN** `GROUP CREATE;G01;Otro` is processed while G01 exists
- **THEN** an error is shown and no group is created

### Requirement: Adding members
`GROUP ADD;Identifier;Email` SHALL add an existing user to an existing, modifiable group. It MUST reject unknown groups, unknown users, a user already in that group, and a locked group.

#### Scenario: Add users
- **WHEN** `GROUP ADD;G01;ana@correo.com` and `GROUP ADD;G01;pedro@correo.com` are processed
- **THEN** G01 contains Ana and Pedro

#### Scenario: Duplicate member rejected
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed while Ana is already in G01
- **THEN** an error is shown and Ana appears only once in G01

#### Scenario: Unknown user rejected
- **WHEN** `GROUP ADD;G01;nadie@correo.com` is processed
- **THEN** an error is shown

### Requirement: Removing members
`GROUP REMOVE;Identifier;Email` SHALL remove a member from an existing, modifiable group. It MUST reject unknown groups, users who are not members and locked groups.

#### Scenario: Remove member
- **WHEN** `GROUP REMOVE;G01;pedro@correo.com` is processed and G01 is modifiable
- **THEN** Pedro is no longer in G01

### Requirement: Derived group lock
A group SHALL be locked while at least one group ticket associated with it has a date equal to or later than the current system date. The locked state MUST be derived from the existing tickets and the current date, not stored. GROUP ADD and GROUP REMOVE MUST be rejected on a locked group; historical group tickets do not lock it.

#### Scenario: Active group ticket locks
- **WHEN** G01 has a group ticket for 2027-04-15 and the date is 2027-04-15
- **THEN** GROUP ADD and GROUP REMOVE on G01 are rejected

#### Scenario: Historical ticket does not lock
- **WHEN** the date is moved to 2027-04-16
- **THEN** GROUP ADD and GROUP REMOVE on G01 are allowed again

#### Scenario: Unlock after deleting future ticket
- **WHEN** G01's only active group ticket is in the future and is deleted with GROUPTICKET DELETE
- **THEN** G01 can be modified again

### Requirement: Group deletion
`GROUP DELETE;Identifier` SHALL delete a group only when it has no associated group ticket dated on or after the current system date. Historical group tickets of a deleted group are retained and their group reference becomes empty.

#### Scenario: Delete group with only historical tickets
- **WHEN** G01 has only group tickets dated before the current date and `GROUP DELETE;G01` is processed
- **THEN** G01 is deleted and its historical group tickets remain with their identifier, date, price and composition

#### Scenario: Delete locked group rejected
- **WHEN** G01 has a group ticket for today or a future date
- **THEN** `GROUP DELETE;G01` is rejected
