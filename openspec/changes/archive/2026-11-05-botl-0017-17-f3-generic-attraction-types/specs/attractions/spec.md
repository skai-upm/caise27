# Spec Delta

## MODIFIED Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;<Id>;<Nombre>;<Capacidad>` SHALL create a GENERAL attraction and `ATTRACTION CREATE;<Id>;<Nombre>;<Capacidad>;<Tipo>` SHALL create an attraction of type `<Tipo>` (`GENERAL`, `INDIVIDUAL`, `GROUP` or `SPECIAL`), in both cases with a unique nonblank identifier, a nonblank name, a maximum capacity given as a positive whole number, and no users present; otherwise it SHALL be rejected.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is processed
- **THEN** attraction `A01` named `Tornado` of type GENERAL with capacity 20 and no users present exists

#### Scenario: Explicit type
- **WHEN** `ATTRACTION CREATE;A02;Familias;30;GROUP` is processed
- **THEN** attraction `A02` of type GROUP exists

#### Scenario: Invalid creation
- **WHEN** `ATTRACTION CREATE;A01;Otra;10` (duplicate id), `ATTRACTION CREATE;A02;Loop;0`, `ATTRACTION CREATE;A02;Loop;-3`, `ATTRACTION CREATE;A02;Loop;diez`, `ATTRACTION CREATE;A02;Loop`, `ATTRACTION CREATE;A02;Loop;5;VIP` or `ATTRACTION CREATE;A02;Loop;5;group` is processed
- **THEN** it is rejected and no attraction is created or changed

## ADDED Requirements

### Requirement: Ticket kinds admitted by attraction type
A GENERAL attraction SHALL admit individual and group tickets, an INDIVIDUAL attraction only individual tickets, a GROUP attraction only group tickets, and a SPECIAL attraction only individual tickets; `ATTRACTION ENTER` with a ticket kind the attraction does not admit SHALL be rejected without admitting anyone. The admitted ticket classes SHALL be expressed through an access-capability interface and the attraction's bounded generic type.

#### Scenario: GROUP rejects an individual ticket
- **GIVEN** GROUP attraction `A02` and individual ticket `T001` valid today
- **WHEN** `ATTRACTION ENTER;A02;T001` is processed
- **THEN** it is rejected and nobody is present in `A02`

#### Scenario: INDIVIDUAL rejects a group ticket
- **GIVEN** INDIVIDUAL attraction `A03` and group ticket `TG01` valid today
- **WHEN** `ATTRACTION ENTER;A03;TG01` is processed
- **THEN** it is rejected

#### Scenario: Accepted kinds
- **WHEN** a valid individual ticket enters a GENERAL, INDIVIDUAL or SPECIAL attraction, or a valid group ticket enters a GENERAL or GROUP attraction
- **THEN** its users are admitted under the existing date and capacity rules

#### Scenario: SPECIAL based on individual tickets
- **WHEN** the type of credential admitted by a SPECIAL attraction is inspected
- **THEN** it is the individual ticket class, with an extension point for the SPECIAL-specific conditions
