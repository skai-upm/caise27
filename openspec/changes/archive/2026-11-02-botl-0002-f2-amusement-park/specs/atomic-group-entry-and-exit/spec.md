# Spec Delta

## Purpose

Extends attraction access to group tickets as an indivisible operation, and lets occupants leave individually and attraction status be checked.

## ADDED Requirements

### Requirement: Indivisible group entry
When `ATTRACTION ENTER` uses a group ticket valid today, all users of its constituent tickets SHALL enter together, and if the attraction lacks capacity for all of them none SHALL enter. (F2-C07)

#### Scenario: Insufficient capacity
- **WHEN** A01 has 3 free places and a group ticket for 4 users is used
- **THEN** no member enters and occupancy is unchanged

#### Scenario: Enough capacity
- **WHEN** A01 has 4 free places and a group ticket for 4 users is used
- **THEN** all four users are inside A01

### Requirement: ATTRACTION EXIT
`ATTRACTION EXIT;AttractionIdentifier;Email` SHALL remove the occupant with that email from the attraction and release one place; users who entered with a group ticket SHALL leave individually. (F2-C07)

#### Scenario: One group member leaves
- **WHEN** users admitted through a group ticket are inside and `ATTRACTION EXIT;A01;ana@correo.com` runs
- **THEN** only Ana leaves and exactly one place is released

#### Scenario: Not inside
- **WHEN** ATTRACTION EXIT names an email not inside the attraction, or an unknown attraction
- **THEN** an error is reported and occupancy is unchanged

### Requirement: ATTRACTION STATUS
`ATTRACTION STATUS;AttractionIdentifier` SHALL display the attraction identifier, name, maximum capacity, number of users inside, number of available places and the users inside, taken from the attraction's own state and independent of how users entered. (F2-C07)

#### Scenario: Status after entry and exit
- **WHEN** four users entered A01 (capacity 20) by group ticket and one left
- **THEN** STATUS shows identifier A01, its name, capacity 20, 3 inside, 17 available and the three remaining emails
