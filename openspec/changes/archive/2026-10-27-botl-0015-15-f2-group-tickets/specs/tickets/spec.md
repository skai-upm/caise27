# Spec Delta

## MODIFIED Requirements

### Requirement: Shared ticket identifier space
All ticket identifiers SHALL be unique across every ticket type — including the identifiers of individual tickets that compose group tickets — checked in a single common way.

#### Scenario: Duplicate identifier rejected
- **WHEN** ticket `T001` exists and `TICKET CREATE;T001;pedro@correo.com;2027-04-16` is processed
- **THEN** an error about the duplicate identifier is reported and no ticket is created

#### Scenario: Component identifier is taken
- **WHEN** group ticket `TG01` contains `TG01:12345678Z` and `TICKET CREATE;TG01:12345678Z;ana@correo.com;2027-04-16` is processed
- **THEN** an error about the duplicate identifier is reported

### Requirement: Individual ticket deletion
`TICKET DELETE;<identifier>` SHALL delete a ticket only when its date is strictly later than the current system date; tickets for the current day or earlier SHALL be kept as historical records. Individual tickets that compose a group ticket SHALL NOT be deleted with TICKET DELETE.

#### Scenario: Delete a future ticket
- **WHEN** the system date is 2027-04-10 and ticket `T001` is dated 2027-04-15
- **THEN** `TICKET DELETE;T001` responds `TICKET DELETED` and `T001` no longer exists

#### Scenario: Today's ticket kept
- **WHEN** the system date equals the ticket date
- **THEN** `TICKET DELETE` reports an error and the ticket remains

#### Scenario: Past ticket kept
- **WHEN** the system date is later than the ticket date
- **THEN** `TICKET DELETE` reports an error and the ticket remains

#### Scenario: Unknown ticket
- **WHEN** `TICKET DELETE;T999` is processed and no such ticket exists
- **THEN** an error is reported

#### Scenario: Component of a group ticket
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed for a future group ticket `TG01`
- **THEN** an error is reported and `TG01` keeps all its components
