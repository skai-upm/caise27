# Spec Delta

## MODIFIED Requirements

### Requirement: Delete ticket
`TICKET DELETE;<Id>` SHALL delete the ticket only when its date is strictly later than the system date and it does not belong to a group ticket; a ticket dated on or before the system date, a ticket that is part of a group ticket, or an unknown identifier SHALL be rejected.

#### Scenario: Future ticket deleted
- **GIVEN** system date `2026-10-01` and ticket `T001` dated `2026-10-15`
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** the ticket is deleted

#### Scenario: Ticket for today or the past
- **GIVEN** ticket `T001` dated `2026-10-15`
- **WHEN** the system date is `2026-10-15` or `2026-10-16` and `TICKET DELETE;T001` is processed
- **THEN** it is rejected and the ticket is kept

#### Scenario: Constituent of a group ticket
- **GIVEN** group ticket `TG01` with constituent ticket `TG01-12345678Z` dated in the future
- **WHEN** `TICKET DELETE;TG01-12345678Z` is processed
- **THEN** it is rejected and `TG01` keeps all its tickets

## ADDED Requirements

### Requirement: Identifiers unique across all tickets
A new individual ticket SHALL NOT reuse the identifier of any existing individual ticket, group ticket or constituent ticket, including historical ones.

#### Scenario: Identifier used by a group ticket
- **GIVEN** group ticket `TG01` exists
- **WHEN** `TICKET CREATE;TG01;ana@correo.com;2026-10-20` is processed
- **THEN** it is rejected
