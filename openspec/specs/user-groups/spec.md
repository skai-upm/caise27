# user-groups Specification

## Purpose

Lets registered users be organised into identified, named groups that can buy group tickets. Group composition is locked while an active group ticket exists.

## Requirements

### Requirement: Group creation
`GROUP CREATE;Identifier;Name` SHALL create an empty group with a unique, non-empty identifier and a non-empty name.

#### Scenario: Create group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed
- **THEN** group G01 exists with no members

#### Scenario: Duplicate identifier
- **WHEN** `GROUP CREATE;G01;Other` is processed and G01 already exists
- **THEN** an error is printed and no group is created

### Requirement: Adding members
`GROUP ADD;Identifier;Email` SHALL add an existing registered user to an existing group. It SHALL reject a user who already belongs to that group, an unknown group or user, and a locked group.

#### Scenario: Add member
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed for a registered user and an unlocked group
- **THEN** the user is a member of G01

#### Scenario: Duplicate member
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed and the user is already a member of G01
- **THEN** an error is printed and membership is unchanged

### Requirement: Removing members
`GROUP REMOVE;Identifier;Email` SHALL remove a member from an existing, unlocked group. It SHALL reject a user who is not a member.

#### Scenario: Remove member
- **WHEN** `GROUP REMOVE;G01;ana@correo.com` is processed for a member of unlocked G01
- **THEN** the user is no longer a member

### Requirement: Derived group lock
A group SHALL be locked while at least one associated group ticket has a date on or after the current system date. The lock SHALL be derived from the existing tickets and the current date, not stored. Historical (earlier-dated) group tickets SHALL NOT lock the group.

#### Scenario: Locked by active ticket
- **WHEN** G01 has a group ticket dated today or later and `GROUP ADD` or `GROUP REMOVE` is processed for G01
- **THEN** an error is printed and membership is unchanged

#### Scenario: Unlocked after date passes
- **WHEN** the only group ticket of G01 is dated 2027-04-15 and the system date is set to 2027-04-16
- **THEN** `GROUP ADD` and `GROUP REMOVE` on G01 are accepted

#### Scenario: Unlocked after future ticket deleted
- **WHEN** the only active group ticket of G01 is a future ticket that is deleted with `GROUPTICKET DELETE`
- **THEN** G01 can be modified again

### Requirement: Group deletion
`GROUP DELETE;Identifier` SHALL delete a group only when it has no associated group ticket dated on or after the current system date. Historical group tickets of a deleted group SHALL be retained with their identifier, date, price and composition, and their group reference SHALL become null.

#### Scenario: Delete group without active tickets
- **WHEN** `GROUP DELETE;G01` is processed and G01 has only historical group tickets
- **THEN** G01 no longer exists and its historical group tickets remain

#### Scenario: Delete rejected by active ticket
- **WHEN** `GROUP DELETE;G01` is processed and G01 has a group ticket dated today
- **THEN** an error is printed and G01 still exists
