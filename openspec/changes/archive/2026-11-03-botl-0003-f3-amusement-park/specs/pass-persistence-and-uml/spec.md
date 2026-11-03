# Spec Delta

## Purpose

Persists the Delivery-3 elements (attraction types and passes with their state) in all execution modes and documents the final Delivery-3 design in PlantUML.

## ADDED Requirements

### Requirement: Pass and type persistence
The application SHALL persist and reconstruct each attraction's type and each pass with its associated individual ticket, stored price and consumed state, in normal and `--fly` modes through the same repositories, without serialization or databases; consumed passes SHALL remain stored while their ticket exists.

#### Scenario: Restart restores passes
- **WHEN** a pass is bought, consumed and the application restarts
- **THEN** the pass is restored with its ticket, price and consumed state

#### Scenario: Restart restores types
- **WHEN** a SPECIAL attraction exists and the application restarts
- **THEN** it is still SPECIAL with no occupants

### Requirement: Atomic cascading deletion
Cascading deletions of passes with their tickets SHALL be all or nothing, including in persistence.

#### Scenario: Group ticket cascade
- **WHEN** a future group ticket whose composed tickets have passes is deleted
- **THEN** the passes, composed tickets and group ticket are removed and none remain after a restart

### Requirement: Delivery-3 UML model
`uml/model.puml` SHALL represent the final Delivery-3 design consistently with the code, including the access interfaces, generic attraction types, ticket-pass relationships with their multiplicities, the invoice builders, injection relationships and the singleton controllers.

#### Scenario: Diagram parses and matches
- **WHEN** `uml/model.puml` is processed by PlantUML and compared with the code
- **THEN** it parses without errors and its elements and relationships exist in the code
