# pass-persistence-and-uml Specification

## Purpose

Persists passes and attraction types in normal and `--fly` modes, cascades valid ticket deletions to their passes atomically, and keeps the PlantUML model aligned with the final Delivery 3 design (source F3-C09 and section 9.5).

## Requirements

### Requirement: Persist passes and attraction types
Each pass's type, identifier, associated individual ticket, stored price and consumed state, and each attraction's type, SHALL be stored in text files and reconstructed at start-up, using the same repositories in normal and `--fly` modes; occupancy SHALL remain unpersisted.

#### Scenario: Consumed pass restored
- **WHEN** a consumed SpeedPass and an unconsumed Special Pass exist and the application restarts
- **THEN** both are restored with the same ticket, price and consumed state, and attraction types are unchanged

### Requirement: Cascade deletion of passes
A valid `TICKET DELETE` SHALL also delete every pass of that ticket, and a valid `GROUPTICKET DELETE` SHALL delete the passes of all composed tickets, the composed tickets and the group ticket, all-or-nothing.

#### Scenario: Group ticket deletion cascade
- **WHEN** a future group ticket whose composed tickets have SpeedPasses and a Special Pass is deleted
- **THEN** the passes, composed tickets and group ticket no longer exist, also after a restart

#### Scenario: Ticket deletion cascade
- **WHEN** future ticket `T002` with passes bought in two operations is deleted
- **THEN** all of its passes are deleted

### Requirement: UML reflects Delivery 3
`uml/model.puml` SHALL show the access interfaces, the generic attraction types, the pass classes and their cardinalities towards individual tickets, the invoice builder abstractions, the singleton controllers and the injected dependencies, consistently with the code.

#### Scenario: Diagram matches code
- **WHEN** the diagram is parsed and compared with the source classes
- **THEN** it parses without errors and every class and field-based association in it exists in the code
