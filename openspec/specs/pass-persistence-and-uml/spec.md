# pass-persistence-and-uml Specification

## Purpose

Extends persistence to the Delivery 3 elements and keeps the PlantUML model aligned with the Delivery 3 design.

## Requirements

### Requirement: Pass and attraction-type persistence
Each attraction's type and each pass's kind, identifier, associated individual ticket, stored price and consumed state SHALL be stored in text files and reconstructed at start-up, in normal and `--fly` modes, through the same repositories; consumed passes SHALL remain persisted while their ticket exists, and occupants SHALL remain unpersisted. (F3-C09)

#### Scenario: Restart restores passes
- **WHEN** a consumed SpeedPass, an unused SpeedPass and a Special Pass exist and the application restarts
- **THEN** each is restored with its ticket, price and consumed state, and attractions keep their types

#### Scenario: Legacy attraction line
- **WHEN** `attractions.txt` contains a Delivery 2 line without a type
- **THEN** the attraction loads as GENERAL

### Requirement: Delivery 3 UML model
`uml/model.puml` SHALL represent the Delivery 3 design consistently with the code, including the ticket access interfaces, generic attraction types, pass classes and their cardinalities with individual tickets, the invoice builder abstractions, the singleton controllers and the relationships established by dependency injection. (F3-C09)

#### Scenario: Diagram parses and matches code
- **WHEN** the diagram is processed by PlantUML and compared with the code
- **THEN** it parses without errors and every element shown exists in the code
