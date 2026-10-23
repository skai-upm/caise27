# Spec Delta

## MODIFIED Requirements

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

## ADDED Requirements

### Requirement: Pass persistence
Each pass SHALL be stored with its type, identifier, individual ticket, stored price and consumed state, and reconstructed attached to that ticket. Passes whose ticket is deleted SHALL be removed from storage.

#### Scenario: Consumed pass survives restart
- **WHEN** SpeedPass SP01:1 for T001 was consumed and the application restarts
- **THEN** SP01:1 exists for T001 with its price and is still consumed
