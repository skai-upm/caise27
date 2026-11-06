# Spec Delta

## ADDED Requirements

### Requirement: Enter an attraction with a group ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` with an existing group ticket valid for the current system date SHALL admit together every user of the individual tickets in its composition, using the same access operation as individual tickets. Access MUST be indivisible: if the attraction lacks capacity for all of them, or any of them is already inside, nobody enters and an error is printed.

#### Scenario: Whole group enters
- **GIVEN** group ticket `TG01` valid today with Ana and Pedro, and attraction `A01` with capacity 20 and nobody inside
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** a confirmation naming `ana@correo.com` and `pedro@correo.com` is printed and both are inside `A01`

#### Scenario: Insufficient capacity for the whole group
- **GIVEN** group ticket `TG01` valid today with Ana and Pedro, and attraction `A03` with capacity 2 and one other user inside
- **WHEN** `ATTRACTION ENTER;A03;TG01` is processed
- **THEN** an error reporting insufficient capacity is printed and neither Ana nor Pedro is inside

#### Scenario: Group ticket for another date
- **GIVEN** group ticket `TG02` dated tomorrow
- **WHEN** `ATTRACTION ENTER;A01;TG02` is processed
- **THEN** an error reporting that the ticket is not valid today is printed and nobody enters
