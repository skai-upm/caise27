# park-persistence Specification

## Purpose

Preserves groups, tickets and attractions between runs in plain text files alongside the users file, so the park state can be reconstructed at start-up.

## Requirements

### Requirement: Park data files
Groups, tickets, passes and attractions SHALL be stored in text files (`groups.txt`, `tickets.txt`, `passes.txt`, `attractions.txt`) in the same data directory as `users.txt`, written after every successful change and loaded at start-up. Attractions SHALL be stored with their type; stored attractions without a type SHALL load as `GENERAL`. Databases, persistence libraries and Java object serialization SHALL NOT be used.

#### Scenario: Restart restores the park
- **WHEN** users, a group with members, an individual ticket, a group ticket and an attraction are created and the application is restarted
- **THEN** the same groups with their members, the tickets with their users, dates and prices, the group ticket with its member tickets and group, and the attraction with its name and capacity exist

#### Scenario: Attraction type restored
- **WHEN** a SPECIAL attraction is created and the application restarts
- **THEN** the attraction is still SPECIAL

#### Scenario: Legacy attraction line
- **WHEN** `attractions.txt` contains `A01;Dragon Rojo;20`
- **THEN** A01 loads as a GENERAL attraction

### Requirement: Occupants are not persisted
Attraction occupancy SHALL NOT be stored; every attraction SHALL be empty when the application starts.

#### Scenario: Empty after restart
- **WHEN** users are inside A01 when the application exits and it is started again
- **THEN** `ATTRACTION LIST;A01` reports that A01 is empty

### Requirement: Historical records survive deletions
Tickets SHALL keep enough information about their user (DNI, email, names) to be reconstructed after that user has been deleted, and a group ticket whose group was deleted SHALL be reconstructed without a group reference.

#### Scenario: Ticket of a deleted user
- **WHEN** Marta has only a historical ticket, Marta is deleted and the application restarts
- **THEN** the historical ticket still exists and refers to Marta's DNI and email, while Marta is not an active user

#### Scenario: Group ticket of a deleted group
- **WHEN** group G01 with a historical group ticket TG01 is deleted and the application restarts
- **THEN** TG01 exists with its member tickets and has no group

### Requirement: Malformed park data
Malformed lines in the park data files SHALL be skipped with a warning instead of aborting start-up.

#### Scenario: Corrupt tickets line
- **WHEN** `tickets.txt` contains an unreadable line
- **THEN** a warning is printed, the remaining data is loaded and the application keeps running

### Requirement: Pass persistence
Each pass SHALL be stored with its type, identifier, individual ticket, stored price and consumed state, and reconstructed attached to that ticket. Passes whose ticket is deleted SHALL be removed from storage.

#### Scenario: Consumed pass survives restart
- **WHEN** SpeedPass SP01:1 for T001 was consumed and the application restarts
- **THEN** SP01:1 exists for T001 with its price and is still consumed
