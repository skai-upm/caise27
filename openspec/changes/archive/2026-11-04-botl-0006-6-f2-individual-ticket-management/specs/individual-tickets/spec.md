# Spec Delta

## Purpose

Lets registered users buy individual tickets for a date of use, each carrying its identifier, date, owner and captured price, with deletion governed by the configured system date.

## ADDED Requirements

### Requirement: Ticket data
Every ticket SHALL have a unique identifier, a date of use, a reference to its registered user (the same user record held by user management) and the price captured when it was created.

#### Scenario: Ticket fields
- **GIVEN** system date `2026-10-01`, price `40.00` and registered user `ana@correo.com`
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2026-10-15` succeeds
- **THEN** ticket `T001` has date `2026-10-15`, price `40.00` and refers to Ana's user record

### Requirement: Create individual ticket
`TICKET CREATE;<Id>;<Correo>;<yyyy-MM-dd>` SHALL create the ticket when the identifier is nonblank and unused, the email belongs to a registered user, and the date is a valid `yyyy-MM-dd` date on or after the system date; on success the output SHALL be exactly `OK TICKET CREATED -> <Precio>` with the captured price in two-decimal format. Otherwise it SHALL be rejected and nothing created.

#### Scenario: Successful purchase
- **GIVEN** system date `2026-10-01` and the default price
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2026-10-15` is processed
- **THEN** the output is `OK TICKET CREATED -> 40.00`

#### Scenario: Ticket for the system date itself
- **GIVEN** system date `2026-10-15`
- **WHEN** `TICKET CREATE;T002;ana@correo.com;2026-10-15` is processed
- **THEN** the ticket is created

#### Scenario: Nonexistent user or past date
- **GIVEN** system date `2026-10-01`
- **WHEN** `TICKET CREATE;T003;nadie@correo.com;2026-10-15` or `TICKET CREATE;T004;ana@correo.com;2026-09-30` is processed
- **THEN** each is rejected and no ticket is created

#### Scenario: Duplicate identifier or malformed command
- **GIVEN** ticket `T001` exists
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2026-10-20`, `TICKET CREATE;T005;ana@correo.com;2026-02-30` or `TICKET CREATE;T005;ana@correo.com` is processed
- **THEN** each is rejected and no ticket is created or changed

### Requirement: Delete ticket
`TICKET DELETE;<Id>` SHALL delete the ticket only when its date is strictly later than the system date; a ticket dated on or before the system date, or an unknown identifier, SHALL be rejected.

#### Scenario: Future ticket deleted
- **GIVEN** system date `2026-10-01` and ticket `T001` dated `2026-10-15`
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** the ticket is deleted

#### Scenario: Ticket for today or the past
- **GIVEN** ticket `T001` dated `2026-10-15`
- **WHEN** the system date is `2026-10-15` or `2026-10-16` and `TICKET DELETE;T001` is processed
- **THEN** it is rejected and the ticket is kept

### Requirement: Past tickets kept as history
Tickets whose date is before the system date SHALL remain stored as historical records; no operation SHALL remove them automatically.

#### Scenario: History retained after date moves on
- **GIVEN** ticket `T001` dated `2026-10-15`
- **WHEN** `SYSTEM DATE;2026-12-01` is processed and the application restarts
- **THEN** ticket `T001` is still stored with its date, user and price
