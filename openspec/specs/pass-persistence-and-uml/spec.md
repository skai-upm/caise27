# pass-persistence-and-uml Specification

## Purpose

Persists the Delivery 3 state (attraction types and passes), applies cascading deletions of passes with their tickets, and keeps the PlantUML model aligned with the Delivery 3 design.

## Requirements

### Requirement: Pass and attraction type persistence
Each attraction's type, and each SpeedPass and Special Pass with its individual ticket, stored price and consumed state, SHALL be persisted in text files and reconstructed at start-up in normal and `--fly` mode, using the same repositories configured with different paths. Attraction data without a type SHALL be read as GENERAL. Occupancy SHALL remain unpersisted.

#### Scenario: Restart restores passes
- **WHEN** a consumed SpeedPass and an unused Special Pass exist and the application restarts
- **THEN** both exist with the same ticket, price and consumed state

#### Scenario: Legacy attraction line
- **WHEN** the attractions file contains `A01;Dragon Rojo;20`
- **THEN** A01 is loaded as a GENERAL attraction

### Requirement: Cascading ticket deletion
A valid `TICKET DELETE` SHALL also delete every SpeedPass and the Special Pass of that ticket, even if bought in different operations. A valid `GROUPTICKET DELETE` SHALL delete the passes of all composition tickets, the composition tickets and the group ticket. The deletion SHALL be all-or-nothing.

#### Scenario: Future group ticket with passes
- **WHEN** future group ticket TG02 has passes on its composition tickets and `GROUPTICKET DELETE;TG02` is processed
- **THEN** the passes, composition tickets and TG02 no longer exist

### Requirement: Delivery 3 UML model
`uml/model.puml` SHALL represent the final Delivery 3 design consistently with the code, including the ticket access interfaces, the generic attraction types, the relationships and cardinalities between individual tickets and SpeedPasses / Special Passes, the invoice Builder abstractions, the dependency-injection relationships and the singleton controllers.

#### Scenario: Diagram renders and matches
- **WHEN** `uml/model.puml` is processed by PlantUML and compared with the code
- **THEN** it parses and every shown element and relationship exists in the code
