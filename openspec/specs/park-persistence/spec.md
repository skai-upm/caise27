# park-persistence Specification

## Purpose

Keeps groups, tickets (including historical fiscal records) and attractions between executions using plain text files in the data directory.

## Requirements

### Requirement: Park data storage
Groups with their members, individual tickets with their user, group tickets with their individual tickets and (while it exists) their group, attractions with their type, and SpeedPasses and Special Passes with their individual ticket, stored price and consumed state SHALL be stored in text files in the data directory after every change, without Java serialization or databases, and reconstructed at startup with the same relationships. Attraction records without a type SHALL be read as `GENERAL`.

#### Scenario: State survives restart
- **WHEN** a group with members, an individual ticket, a group ticket and an attraction are created and the application is restarted
- **THEN** all of them exist again with the same members, users, composition, prices and dates

#### Scenario: Passes and types survive restart
- **WHEN** a SPECIAL attraction, a consumed Special Pass and an unconsumed SpeedPass exist and the application is restarted
- **THEN** the attraction is still SPECIAL and both passes exist with their ticket, price and consumed state

#### Scenario: Delivery 2 attraction data
- **WHEN** `attractions.txt` contains the line `A01;Dragon Rojo;20`
- **THEN** A01 is loaded as a GENERAL attraction

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
