# Spec Delta

## Purpose

Lets registered users be organized into persistent groups that reference the existing user records.

## ADDED Requirements

### Requirement: Create group
`GROUP CREATE;id;name` SHALL create an empty group with a unique non-empty identifier and a non-empty name.

#### Scenario: Create group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed and no group G01 exists
- **THEN** `OK` is reported and group G01 named `Familia Garcia` exists with no members

#### Scenario: Duplicate identifier
- **WHEN** `GROUP CREATE;G01;Otro` is processed and G01 already exists
- **THEN** an `ERROR` is reported

### Requirement: Add existing users
`GROUP ADD;id;email` SHALL add the existing user with that email to the group. Nonexistent users SHALL be rejected, and a user SHALL NOT appear twice in the same group. Groups SHALL reference the same user records as the rest of the system, not copies.

#### Scenario: Add user
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed and Ana exists
- **THEN** Ana is a member of G01

#### Scenario: Nonexistent user
- **WHEN** `GROUP ADD;G01;nadie@correo.com` is processed
- **THEN** an `ERROR` is reported and the group is unchanged

#### Scenario: Duplicate member
- **WHEN** Ana is already in G01 and `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** an `ERROR` is reported and Ana appears once

#### Scenario: Member data stays current
- **WHEN** Ana is in G01 and her name is updated with `USER UPDATE`
- **THEN** the group member shows the updated name

### Requirement: Remove members
`GROUP REMOVE;id;email` SHALL remove that user from the group, reporting an `ERROR` when the group or the membership does not exist.

#### Scenario: Remove member
- **WHEN** Ana is in G01 and `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** Ana is no longer a member of G01

### Requirement: Delete group
`GROUP DELETE;id` SHALL delete the group (its users are not deleted).

#### Scenario: Delete group
- **WHEN** `GROUP DELETE;G01` is processed and G01 is not locked
- **THEN** G01 no longer exists and its former members still exist

### Requirement: Locked groups
A group with any group ticket dated on or after the system date SHALL be locked: `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` SHALL be rejected. Once all its group tickets are in the past, it SHALL be modifiable again.

#### Scenario: Locked group
- **WHEN** the system date is 2026-10-01, G01 has a group ticket for 2026-10-15 and `GROUP ADD;G01;pedro@correo.com` is processed
- **THEN** an `ERROR` reports that the group is locked

#### Scenario: Unlocked after date advances
- **WHEN** the system date becomes 2026-10-16 and `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** the member is removed

### Requirement: Groups persist
Groups and their memberships SHALL be stored in a `;`-separated text file and restored, linked to the loaded users, when the application starts again.

#### Scenario: Restart restores groups
- **WHEN** G01 with Ana and Pedro exists and the application restarts
- **THEN** G01 exists with Ana and Pedro as members
