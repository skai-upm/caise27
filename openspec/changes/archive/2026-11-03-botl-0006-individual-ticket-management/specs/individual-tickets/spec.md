# Spec Delta

## Purpose

Lets registered users buy individual tickets for a date of use, and controls when such tickets may be deleted, always relative to the configured system date.

## ADDED Requirements

### Requirement: Ticket data
Every ticket SHALL have an identifier, a date of use, a reference to the registered user it belongs to, and the price captured when it was created.

#### Scenario: Ticket fields
- **GIVEN** system date 2026-10-01, price 40.00 and registered user `ana@correo.com`
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2026-10-15` is processed
- **THEN** ticket `T001` exists with date 2026-10-15, price 40.00 and Ana's user as owner

### Requirement: Ticket purchase
The command `TICKET CREATE;Id;Correo;yyyy-MM-dd` SHALL create a ticket when the identifier is non-blank and unused, the email belongs to a registered user, and the date is a valid `yyyy-MM-dd` date on or after the current system date. On success it SHALL respond exactly `OK TICKET CREATED -> <Precio>` with the captured price formatted with two decimals.

#### Scenario: Successful purchase
- **GIVEN** system date 2026-10-01 and the default price
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2026-10-15` is processed
- **THEN** the response is `OK TICKET CREATED -> 40.00`

#### Scenario: Ticket for today
- **GIVEN** system date 2026-10-15
- **WHEN** `TICKET CREATE;T002;ana@correo.com;2026-10-15` is processed
- **THEN** the ticket is created

#### Scenario: Nonexistent user
- **WHEN** `TICKET CREATE;T003;nadie@correo.com;2026-10-15` is processed
- **THEN** an error is reported and no ticket is created

#### Scenario: Past date
- **GIVEN** system date 2026-10-01
- **WHEN** `TICKET CREATE;T004;ana@correo.com;2026-09-30` is processed
- **THEN** an error is reported and no ticket is created

#### Scenario: Invalid date, duplicate id or wrong parameters
- **WHEN** a TICKET CREATE has date `2026-02-30`, an identifier already used, or a missing parameter
- **THEN** an error is reported and no ticket is created

### Requirement: Ticket deletion
The command `TICKET DELETE;Id` SHALL delete the ticket only if its date is strictly later than the current system date; a ticket dated on the system date or earlier, or a nonexistent ticket, SHALL be rejected.

#### Scenario: Future ticket
- **GIVEN** system date 2026-10-01 and ticket `T001` dated 2026-10-15
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** the ticket is deleted

#### Scenario: Ticket for today cannot be deleted
- **GIVEN** system date 2026-10-15 and ticket `T001` dated 2026-10-15
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** an error is reported and the ticket remains

#### Scenario: Past ticket cannot be deleted
- **GIVEN** system date 2026-10-16 and ticket `T001` dated 2026-10-15
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** an error is reported and the ticket is kept as history

### Requirement: Historical tickets
Tickets whose date is earlier than the system date SHALL be retained as historical records, including after their user has been deleted.

#### Scenario: History survives user deletion
- **GIVEN** system date 2026-10-16, Ana's ticket `T001` dated 2026-10-15 and no other tickets of Ana
- **WHEN** Ana is deleted
- **THEN** ticket `T001` is still stored
