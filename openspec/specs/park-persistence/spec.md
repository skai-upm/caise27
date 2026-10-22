# park-persistence Specification

## Purpose

Keeps groups, tickets (including historical fiscal records) and attractions between executions using plain text files in the data directory.

## Requirements

### Requirement: Park data storage
Groups with their members, individual tickets with their user, group tickets with their individual tickets and (while it exists) their group, and attractions SHALL be stored in text files in the data directory after every change, without Java serialization, databases or external libraries, and reconstructed at startup with the same relationships.

#### Scenario: State survives restart
- **WHEN** a group with members, an individual ticket, a group ticket and an attraction are created and the application is restarted
- **THEN** all of them exist again with the same members, users, composition, prices and dates

### Requirement: Attractions start empty
Attraction occupants SHALL NOT be persisted; at startup every attraction SHALL be empty.

#### Scenario: Restart empties attractions
- **WHEN** users are inside A01 and the application is restarted
- **THEN** `ATTRACTION LIST;A01` shows an empty result

### Requirement: Historical records of deleted users and groups
Tickets of a deleted user and group tickets of a deleted group SHALL keep enough stored information (identifier, date, price, composition and the holder's identity) to be reconstructed after a restart.

#### Scenario: Ticket of a deleted user after restart
- **WHEN** a user with only historical tickets is deleted and the application is restarted
- **THEN** those tickets still exist with their date, price and the holder's DNI
