# Spec Delta

## MODIFIED Requirements

### Requirement: Create attraction
`ATTRACTION CREATE;Identifier;Name;Capacity[;Type]` SHALL create an attraction with that unique identifier, non-empty name, maximum capacity (a positive whole number of people that may be inside simultaneously) and type (default `GENERAL` when omitted), with no users inside. A used identifier, an empty name or a capacity that is not a positive whole number MUST be rejected with an error and create nothing.

#### Scenario: Attraction created empty
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** a confirmation is printed and attraction `A01` named `Dragon Rojo` with capacity `20` exists with no users inside

#### Scenario: Duplicate identifier rejected
- **GIVEN** attraction `A01` exists
- **WHEN** `ATTRACTION CREATE;A01;Otra;5` is processed
- **THEN** an error reporting that the attraction already exists is printed and `A01` is unchanged

#### Scenario: Invalid capacity rejected
- **WHEN** `ATTRACTION CREATE;A02;Torre del Cielo;0`, `...;-3` or `...;doce` is processed
- **THEN** an error reporting an invalid capacity is printed and no attraction is created

#### Scenario: Delivery 2 syntax creates a general attraction
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** attraction `A01` of type `GENERAL` is created
