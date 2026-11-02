# attraction-management Specification

## Purpose

Models park attractions with limited capacity and lets visitors enter with individual or group tickets valid for the system date, leave, and inspect the current occupancy.

## Requirements

### Requirement: Create attraction
`ATTRACTION CREATE;<attractionId>;<name>;<capacity>` SHALL create an attraction with a unique non-empty identifier, a non-empty name, and a positive integer maximum capacity; invalid or duplicate values SHALL be rejected.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is executed and `A01` does not exist
- **THEN** attraction `A01` named `Tornado` with capacity 20 and no visitors exists

#### Scenario: Invalid capacity
- **WHEN** the capacity is `0`, negative, or not an integer
- **THEN** an error is shown and no attraction is created

### Requirement: Enter attraction with a ticket
`ATTRACTION ENTER;<attractionId>;<ticketId>` SHALL admit every user represented by the ticket — the holder of an individual ticket or the holders of all constituents of a group ticket — only if the ticket's date equals the system date, the free places suffice for all of them, and none of them is already present. Otherwise nobody SHALL enter.

#### Scenario: Individual entry
- **WHEN** ticket `T001` is dated on the system date and `ATTRACTION ENTER;A01;T001` is executed with free places
- **THEN** the ticket holder is present in `A01`

#### Scenario: Atomic capacity rejection
- **WHEN** `A01` has three free places and `ATTRACTION ENTER;A01;TG01` is executed for a group ticket with four constituents
- **THEN** an error is shown, nobody enters, and occupancy is unchanged

#### Scenario: Ticket not valid today
- **WHEN** the ticket's date differs from the system date
- **THEN** entry is rejected

### Requirement: Exit attraction
`ATTRACTION EXIT;<attractionId>;<ticketId|email>` SHALL remove the present users identified by the parameter — the holder(s) of the given individual or group ticket, or the user with the given email — and free their places, without checking any ticket date. If none of the identified users is present, it SHALL be rejected.

#### Scenario: Exit with ticket
- **WHEN** the holder of `T001` is in `A01` and `ATTRACTION EXIT;A01;T001` is executed
- **THEN** the holder is no longer present and one place is freed

#### Scenario: Exit with email
- **WHEN** Ana is in `A01` and `ATTRACTION EXIT;A01;ana@correo.com` is executed
- **THEN** Ana is no longer present

#### Scenario: Date changes after admission
- **WHEN** a user entered with a ticket valid on the entry date and the system date then changes
- **THEN** the user can still exit

### Requirement: Attraction status
`ATTRACTION STATUS;<attractionId>` SHALL display the identifier, name, maximum capacity, current number of users present, available places, and the users present, reflecting the current state regardless of whether users entered individually or with a group ticket.

#### Scenario: Status after entry and exit
- **WHEN** `T001` (1 user) and `TG01` (2 users) entered capacity-20 attraction `A01`, status is queried, then `ATTRACTION EXIT;A01;T001` is executed and status is queried again
- **THEN** the first status shows 3 present and 17 available listing all three users, and the second shows 2 present and 18 available

### Requirement: Attraction persistence
Attraction definitions (identifier, name, capacity) SHALL be persisted and restored at startup; the users present SHALL NOT be persisted, so every attraction starts empty.

#### Scenario: Restart empties attractions
- **WHEN** users are inside `A01` and the application restarts
- **THEN** `A01` exists with the same name and capacity and no users present
