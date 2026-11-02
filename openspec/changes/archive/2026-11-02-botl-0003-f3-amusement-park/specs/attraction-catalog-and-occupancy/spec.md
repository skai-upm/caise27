# Spec Delta

## MODIFIED Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;Identifier;Name;Capacity` and `ATTRACTION CREATE;Identifier;Name;Capacity;Type` SHALL create an empty attraction with a unique identifier, a name, a maximum capacity that is a positive whole number and a type among GENERAL, INDIVIDUAL, GROUP and SPECIAL; the form without type SHALL create a GENERAL attraction. A duplicate identifier, an invalid capacity or an unknown type SHALL be rejected.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** attraction A01 `Dragon Rojo` with capacity 20 and no occupants exists

#### Scenario: Invalid capacity or duplicate
- **WHEN** `ATTRACTION CREATE;A03;X;0`, `ATTRACTION CREATE;A03;X;abc` or a second `ATTRACTION CREATE;A01;...` is processed
- **THEN** an error is reported and no attraction is created or changed

#### Scenario: Typed attraction
- **WHEN** `ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL` is processed
- **THEN** A04 is a SPECIAL attraction with capacity 15

#### Scenario: Default and unknown type
- **WHEN** an attraction is created without type, or `ATTRACTION CREATE;A05;X;5;VIP` is processed
- **THEN** the first is GENERAL and the second is rejected
