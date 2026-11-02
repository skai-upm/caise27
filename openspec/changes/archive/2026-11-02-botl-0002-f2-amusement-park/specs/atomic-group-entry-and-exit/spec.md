# Spec Delta

## Purpose

Extends attraction access to indivisible group-ticket entry, individual exits that release capacity, and an attraction status report.

## ADDED Requirements

### Requirement: Indivisible group entry
`ATTRACTION ENTER` with a group ticket valid today SHALL admit together all users of the individual tickets in its composition, only if the attraction has enough free places for all of them; otherwise none SHALL enter.

#### Scenario: Insufficient capacity
- **WHEN** A01 has 3 free places and a group ticket for 4 users valid today is used
- **THEN** entry is rejected and no member enters

#### Scenario: Enough capacity
- **WHEN** A01 has at least 4 free places and the same group ticket is used
- **THEN** all 4 users are among A01's occupants

### Requirement: ATTRACTION EXIT
`ATTRACTION EXIT;AttractionIdentifier;Email` SHALL remove the specified user from the attraction's occupants and release one place. Users who entered with a group ticket SHALL leave individually with their own email. An unknown attraction or a user not inside SHALL be rejected.

#### Scenario: Group member leaves alone
- **WHEN** Ana and Pedro entered A01 with a group ticket and `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** Ana is no longer inside, Pedro still is, and one place is released

#### Scenario: User not inside
- **WHEN** `ATTRACTION EXIT;A01;nobody@correo.com` is processed
- **THEN** an error is reported and occupancy is unchanged

### Requirement: ATTRACTION STATUS
`ATTRACTION STATUS;AttractionIdentifier` SHALL display the attraction identifier, name, maximum capacity, number of users inside, number of available places and the users inside, derived from the attraction's own state and independent of how users entered.

#### Scenario: Status after mixed entries and an exit
- **WHEN** A01 (capacity 20) holds 3 users who entered with individual and group tickets and `ATTRACTION STATUS;A01` is processed
- **THEN** it shows A01, its name, capacity 20, 3 inside, 17 available and the 3 users' emails
