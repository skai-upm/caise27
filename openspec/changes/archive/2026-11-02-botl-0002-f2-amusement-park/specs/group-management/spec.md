# Spec Delta

## Purpose

Defines groups of registered users, their membership commands, and the lock derived from active group tickets (F2-C02, F2-C04).

## ADDED Requirements

### Requirement: Group creation
`GROUP CREATE;Identifier;Name` SHALL create an empty group with a unique identifier and a name. A duplicate identifier SHALL be rejected.

#### Scenario: Create group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed and no group G01 exists
- **THEN** group G01 named `Familia Garcia` exists with no members

#### Scenario: Duplicate identifier
- **WHEN** `GROUP CREATE;G01;Otro` is processed and G01 already exists
- **THEN** an error is reported and the existing group is unchanged

### Requirement: Group membership
`GROUP ADD;Identifier;Email` SHALL add an existing user to an existing group; the same user SHALL NOT appear more than once in a group. `GROUP REMOVE;Identifier;Email` SHALL remove a member from the group. Unknown groups, unknown users, duplicate additions and removal of non-members SHALL be rejected without change.

#### Scenario: Add member once
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed twice for an unlocked group
- **THEN** the first succeeds, the second is rejected, and Ana appears once in G01

#### Scenario: Add unknown user
- **WHEN** `GROUP ADD;G01;nadie@correo.com` is processed and no user has that email
- **THEN** an error is reported and the group is unchanged

#### Scenario: Remove member
- **WHEN** `GROUP REMOVE;G01;ana@correo.com` is processed for an unlocked group containing Ana
- **THEN** Ana is no longer a member of G01

### Requirement: Group lock derived from active group tickets
A group SHALL be locked while at least one associated group ticket has a date equal to or later than the current system date. While locked, `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` SHALL be rejected. Group tickets with earlier dates SHALL NOT lock the group. The lock SHALL be derived from existing tickets and the current date, never stored as an independent attribute.

#### Scenario: Locked by ticket for today or later
- **WHEN** G01 has a group ticket dated today or later and `GROUP ADD`, `GROUP REMOVE` or `GROUP DELETE` is processed for G01
- **THEN** the operation is rejected and G01 is unchanged

#### Scenario: Unlocked when only historical tickets remain
- **WHEN** the system date moves past the date of every group ticket of G01
- **THEN** `GROUP ADD` and `GROUP REMOVE` on G01 are accepted again

#### Scenario: Unlocked after deleting the only future ticket
- **WHEN** the only active group ticket of G01 is future and is deleted with `GROUPTICKET DELETE`
- **THEN** G01 can be modified again

### Requirement: Group deletion
`GROUP DELETE;Identifier` SHALL delete an unlocked group. Historical group tickets of a deleted group SHALL be retained, with their group reference becoming empty while their identifier, date, price and composition are preserved.

#### Scenario: Delete group with historical ticket
- **WHEN** G01 has only a group ticket dated before the current date and `GROUP DELETE;G01` is processed
- **THEN** G01 no longer exists and the historical group ticket is retained without a group reference
