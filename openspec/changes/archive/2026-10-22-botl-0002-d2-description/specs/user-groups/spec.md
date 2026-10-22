# Spec Delta

## Purpose

Lets registered users be organized into named groups that can buy group tickets, and protects a group's composition while it has active group tickets.

## ADDED Requirements

### Requirement: Group creation
`GROUP CREATE;<Identifier>;<Name>` SHALL create an empty group when the identifier is not used by another group and both values are non-empty.

#### Scenario: Create a group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed and G01 does not exist
- **THEN** an empty group G01 named "Familia Garcia" exists and a confirmation is printed

#### Scenario: Duplicate identifier
- **WHEN** `GROUP CREATE;G01;Otro` is processed and G01 already exists
- **THEN** an error is printed

### Requirement: Group membership changes
`GROUP ADD;<Identifier>;<Email>` SHALL add an existing user to an existing group, rejecting a user who already belongs to it. `GROUP REMOVE;<Identifier>;<Email>` SHALL remove a member from the group, rejecting a user who is not a member. Both SHALL be rejected while the group is locked.

#### Scenario: Add members
- **WHEN** `GROUP ADD;G01;ana@correo.com` and `GROUP ADD;G01;pedro@correo.com` are processed for existing users
- **THEN** both users are members of G01

#### Scenario: Duplicate member
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed and Ana already belongs to G01
- **THEN** an error is printed and the group is unchanged

#### Scenario: Unknown user or group
- **WHEN** `GROUP ADD` names a group or an email that does not exist
- **THEN** an error is printed

#### Scenario: Remove a member
- **WHEN** `GROUP REMOVE;G02;laura@correo.com` is processed, Laura belongs to G02 and G02 is not locked
- **THEN** Laura is no longer a member of G02

### Requirement: Group lock derived from tickets
A group SHALL be locked while at least one of its group tickets has a date equal to or later than the system date. Group tickets with earlier dates are historical and SHALL NOT lock the group. The lock SHALL be derived from existing tickets and the current system date, not stored.

#### Scenario: Locked by an active group ticket
- **WHEN** G01 has a group ticket dated on or after the system date and `GROUP ADD` or `GROUP REMOVE` is processed for G01
- **THEN** an error is printed and the membership is unchanged

#### Scenario: Unlocked after the ticket becomes historical
- **WHEN** the system date is moved past the date of G01's only group ticket
- **THEN** `GROUP ADD` and `GROUP REMOVE` on G01 are accepted again

#### Scenario: Unlocked after deleting the future ticket
- **WHEN** G02's only active group ticket is a future one and it is deleted with `GROUPTICKET DELETE`
- **THEN** G02 can be modified again

### Requirement: Group deletion
`GROUP DELETE;<Identifier>` SHALL delete the group only when it has no group ticket dated on or after the system date. Historical group tickets of a deleted group SHALL be kept, with their reference to the group cleared.

#### Scenario: Delete an unlocked group
- **WHEN** `GROUP DELETE;G02` is processed and G02 has no active group ticket
- **THEN** G02 no longer exists

#### Scenario: Delete a locked group
- **WHEN** `GROUP DELETE;G01` is processed and G01 has a group ticket for the current system date
- **THEN** an error is printed and G01 still exists

#### Scenario: Historical tickets survive
- **WHEN** a group with only past group tickets is deleted
- **THEN** those group tickets still exist with their identifier, date, price and composition, and no group
