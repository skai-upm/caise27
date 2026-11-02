# Spec Delta

## Purpose

Defines indivisible attraction entry with group tickets through the same entry command, individual exit, and attraction status reporting (F2-C07).

## ADDED Requirements

### Requirement: Indivisible group entry
`ATTRACTION ENTER` with a group ticket valid for the current date SHALL admit together all users of the individual tickets in its composition, through the same mechanism used for individual tickets. If the free places are fewer than the users to admit, or any of them is already inside, none SHALL enter.

#### Scenario: Whole group enters
- **WHEN** a group ticket for today has 3 constituents and the attraction has at least 3 free places
- **THEN** all 3 users are inside the attraction

#### Scenario: Insufficient capacity
- **WHEN** a group ticket for today has 4 constituents and the attraction has 3 free places
- **THEN** an error is reported and no member enters

### Requirement: Attraction exit
`ATTRACTION EXIT;AttractionIdentifier;Email` SHALL remove the user with that email from the attraction's occupants and release one place. Users who entered with a group ticket SHALL leave individually. An unknown attraction or a user not inside SHALL be reported as an error without change.

#### Scenario: One group member leaves
- **WHEN** three users entered A01 with a group ticket and `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** only Ana leaves and one place is released

#### Scenario: User not inside
- **WHEN** `ATTRACTION EXIT;A01;pedro@correo.com` is processed and Pedro is not inside A01
- **THEN** an error is reported and occupancy is unchanged

### Requirement: Attraction status
`ATTRACTION STATUS;AttractionIdentifier` SHALL display the attraction identifier, name, maximum capacity, number of users inside, number of available places and the users inside, from the attraction's own state, regardless of how users entered.

#### Scenario: Status after group entry and one exit
- **WHEN** three users entered A01 (capacity 20) with a group ticket and one left
- **THEN** STATUS shows A01, its name, capacity 20, 2 inside, 18 available and the two remaining emails
