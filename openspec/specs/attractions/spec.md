# attractions Specification

## Purpose

Manages park attractions and the presence of users in them, admitting visitors with individual or group tickets valid for the current date.

## Requirements

### Requirement: Create attraction
The system SHALL accept `ATTRACTION CREATE;<id>;<nombre>;<capacidad>` and create an empty attraction with a unique identifier, non-blank name and a positive integer maximum capacity.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is processed
- **THEN** the system reports `OK ATTRACTION CREATED` and A01 exists with capacity 20 and nobody inside

#### Scenario: Invalid capacity or duplicate
- **WHEN** `ATTRACTION CREATE;A02;Rio;0`, `ATTRACTION CREATE;A02;Rio;diez` or a second `ATTRACTION CREATE;A01;...` is processed
- **THEN** the system reports an error and no attraction is created

### Requirement: Delete attraction
The system SHALL accept `ATTRACTION DELETE;<id>` and delete the attraction when nobody is inside it.

#### Scenario: Delete empty attraction
- **WHEN** A01 is empty and `ATTRACTION DELETE;A01` is processed
- **THEN** the system reports `OK ATTRACTION DELETED` and A01 no longer exists

#### Scenario: Occupied attraction
- **WHEN** someone is inside A01 and `ATTRACTION DELETE;A01` is processed
- **THEN** the system reports an error

### Requirement: Enter attraction
The system SHALL accept `ATTRACTION ENTER;<atraccion>;<entrada>` with an individual or group ticket. An individual ticket admits its user; a group ticket admits the users of its individual tickets. The ticket date SHALL equal the system date. Entry SHALL be atomic: if capacity is insufficient for all the ticket's users or any of them is already inside, nobody enters.

#### Scenario: Individual entry
- **WHEN** the system date is 2026-10-15 and `ATTRACTION ENTER;A01;T001` is processed for T001 dated 2026-10-15
- **THEN** the system reports `OK ATTRACTION ENTER -> 1` and Ana is inside A01

#### Scenario: Group entry
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed for TG01 with 3 tickets dated today
- **THEN** the system reports `OK ATTRACTION ENTER -> 3` and the 3 users are inside A01

#### Scenario: Ticket for another date
- **WHEN** the system date is 2026-10-14 and `ATTRACTION ENTER;A01;T001` is processed for T001 dated 2026-10-15
- **THEN** the system reports an error and nobody enters

#### Scenario: Insufficient capacity for a group
- **WHEN** 3 places remain in A01 and `ATTRACTION ENTER;A01;TG01` is processed for a group ticket of 4 users
- **THEN** the system reports an error and none of the 4 users enters

#### Scenario: Already inside
- **WHEN** Ana is inside A01 and `ATTRACTION ENTER;A01;T001` is processed again
- **THEN** the system reports an error and occupancy is unchanged

### Requirement: Exit attraction
The system SHALL accept `ATTRACTION EXIT;<atraccion>;<entrada>` and remove from the attraction the ticket's users that are inside, freeing their places; the date SHALL NOT be checked on exit. If none of the ticket's users is inside, it SHALL report an error.

#### Scenario: Exit frees capacity
- **WHEN** Ana entered A01 with T001 and `ATTRACTION EXIT;A01;T001` is processed
- **THEN** the system reports `OK ATTRACTION EXIT -> 1` and A01 has one more available place

### Requirement: Attraction status
The system SHALL accept `ATTRACTION STATUS;<atraccion>` and display the identifier, name, maximum capacity, number of users present, available places and the users present, reflecting the current state regardless of whether users entered individually or with a group ticket.

#### Scenario: Status after entries and exit
- **WHEN** T001 (Ana) and TG01 (3 users) entered A01 with capacity 20, and then T001 exited
- **THEN** `ATTRACTION STATUS;A01` shows A01, its name, capacity 20, 3 present, 17 available and the 3 group users

### Requirement: Attraction persistence
Attractions SHALL be stored with identifier, name and capacity and restored at startup; they SHALL always start empty.

#### Scenario: Restart empties attractions
- **WHEN** users are inside A01 and the application restarts
- **THEN** A01 exists with the same name and capacity and nobody inside
