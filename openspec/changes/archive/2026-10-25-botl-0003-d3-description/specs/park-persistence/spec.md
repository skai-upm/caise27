# Spec Delta

## MODIFIED Requirements

### Requirement: Park state survives restarts
Groups with their members, individual tickets with their user, group tickets with their individual tickets and (while it exists) their group, attractions with their type, and SpeedPasses and Special Passes with their individual ticket, stored price and consumed state SHALL be stored in text files, without Java serialization or databases, and reconstructed when the application starts. Every successful state-changing command SHALL be persisted before the next command.

#### Scenario: Reload park
- **WHEN** a group, an individual ticket, a group ticket and an attraction are created, the application exits and starts again
- **THEN** the same group members, tickets (identifiers, dates, prices, composition) and attractions are available

#### Scenario: Reload passes and types
- **WHEN** a SPECIAL attraction exists, T001 has a consumed SpeedPass, an unconsumed SpeedPass and a Special Pass, and the application restarts
- **THEN** the attraction is still SPECIAL and T001's passes keep their identifiers, prices and consumed states

## ADDED Requirements

### Requirement: Untyped attraction records
Attraction records without a type, as written by Delivery 2, SHALL be loaded as `GENERAL` attractions.

#### Scenario: Delivery 2 data
- **WHEN** the attraction file contains `A01;Dragon Rojo;20`
- **THEN** A01 is loaded as a GENERAL attraction with capacity 20
