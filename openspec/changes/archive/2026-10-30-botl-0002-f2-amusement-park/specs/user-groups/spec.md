# Spec Delta

## Purpose

Lets registered users be organised into identified, named groups whose composition can change only while no active group ticket locks it.

## ADDED Requirements

### Requirement: Create group
`GROUP CREATE;Identifier;Name` SHALL create an empty group with a unique identifier and a name, printing `GROUP CREATED`. A duplicate identifier SHALL be rejected.

#### Scenario: Create empty group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed
- **THEN** group G01 exists with no members

#### Scenario: Duplicate identifier
- **WHEN** `GROUP CREATE;G01;Otro` is processed and G01 exists
- **THEN** an error is printed and the existing group is unchanged

### Requirement: Add user to group
`GROUP ADD;Identifier;Email` SHALL add an existing user to an existing, unlocked group. Unknown group or user, a user already in the group, or a locked group SHALL be rejected.

#### Scenario: Add members
- **WHEN** `GROUP ADD;G01;ana@correo.com` and `GROUP ADD;G01;pedro@correo.com` are processed
- **THEN** G01 contains both users

#### Scenario: Duplicate member
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed and Ana already belongs to G01
- **THEN** an error is printed and Ana appears once in G01

#### Scenario: Unknown user or group
- **WHEN** the email or group identifier does not exist
- **THEN** an error is printed and no group changes

### Requirement: Remove user from group
`GROUP REMOVE;Identifier;Email` SHALL remove a member from an unlocked group. A user that is not a member, an unknown group, or a locked group SHALL be rejected.

#### Scenario: Remove member
- **WHEN** `GROUP REMOVE;G01;pedro@correo.com` is processed and G01 is unlocked
- **THEN** Pedro is no longer in G01

### Requirement: Group lock derived from tickets
A group SHALL be locked while at least one of its group tickets has a date on or after the current system date. The lock SHALL be derived from tickets and the system date, never stored. Locked groups SHALL reject `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE`.

#### Scenario: Locked by future ticket
- **WHEN** G01 has a group ticket dated after today and `GROUP ADD` or `GROUP REMOVE` is processed for G01
- **THEN** an error is printed and G01 is unchanged

#### Scenario: Unlocked after date passes
- **WHEN** the system date moves past the date of every group ticket of G01
- **THEN** `GROUP ADD`/`GROUP REMOVE` on G01 succeed again

#### Scenario: Unlocked after future ticket deleted
- **WHEN** the only future group ticket of G01 is deleted with `GROUPTICKET DELETE`
- **THEN** G01 can be modified again

### Requirement: Delete group
`GROUP DELETE;Identifier` SHALL delete an existing group with no active group ticket, printing `GROUP DELETED`. Historical group tickets SHALL be kept and lose their reference to the group.

#### Scenario: Delete group with only historical tickets
- **WHEN** G01 has only group tickets dated before today and `GROUP DELETE;G01` is processed
- **THEN** G01 no longer exists and its historical tickets keep identifier, date, price and composition

#### Scenario: Ticket for today blocks deletion
- **WHEN** G01 has a group ticket dated today and `GROUP DELETE;G01` is processed
- **THEN** an error is printed and G01 still exists
