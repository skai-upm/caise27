# Spec Delta

## Purpose

Defines persistence of passes and attraction types, cascading pass deletion with ticket deletions, and the updated Delivery 3 PlantUML model (F3-C09).

## ADDED Requirements

### Requirement: Pass and attraction-type persistence
The type of each attraction and every pass with its individual ticket, stored price and consumed state SHALL be persisted in text files and reconstructed at start-up, in normal and `--fly` modes through the same repositories. Attraction data without a type SHALL be read as GENERAL. Occupants SHALL remain unpersisted. Consumed passes SHALL remain stored while their ticket exists.

#### Scenario: Consumed pass survives restart
- **WHEN** a pass is consumed and the application restarts
- **THEN** the pass exists with the same ticket, price and consumed state

#### Scenario: Legacy attraction line
- **WHEN** the attractions file contains a line without a type
- **THEN** that attraction is loaded as GENERAL

### Requirement: Cascading pass deletion
A valid `TICKET DELETE` SHALL also delete every pass of that ticket, and a valid `GROUPTICKET DELETE` SHALL delete every pass of its constituent tickets, the constituents and the group ticket, all as one operation that leaves no partial deletion if it cannot be completed.

#### Scenario: Future group ticket with passes deleted
- **WHEN** a future group ticket whose constituents have SpeedPasses and a Special Pass is deleted
- **THEN** the passes, the constituent tickets and the group ticket no longer exist, also after restart

### Requirement: Delivery 3 PlantUML model
`uml/model.puml` SHALL be updated to the implemented Delivery 3 design, showing the ticket access interfaces, the generic attraction types, the ticket–pass relationships with their cardinalities, the invoice builder abstractions, the relevant dependency-injection relationships and the singleton controllers, and SHALL be syntactically valid PlantUML consistent with the code.

#### Scenario: Diagram parses and matches
- **WHEN** `uml/model.puml` is checked with PlantUML and compared with the code
- **THEN** it is valid and every element and relationship shown exists in the code
