# Spec Delta

## Purpose

Lets registered users buy dated individual tickets at a configurable price, cancel future tickets, and keeps past tickets as historical records, with every date check based on the system date.

## ADDED Requirements

### Requirement: Ticket data
Each ticket SHALL have an identifier, a date of use, a reference to the existing user who owns it, and the price charged when it was created. Ticket identifiers SHALL be unique among all stored tickets, including past ones.

#### Scenario: Ticket fields
- **GIVEN** the system date is 2026-10-01 and the price is 40.00
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2026-10-15` succeeds
- **THEN** ticket `T001` has date 2026-10-15, price 40.00 and refers to the same user object as `ana@correo.com`

### Requirement: Create ticket
The system SHALL process `TICKET CREATE;Id;Correo;yyyy-MM-dd`. It SHALL create the ticket only if the identifier is non-blank and unused, a user with that email exists, and the date is a valid `yyyy-MM-dd` date on or after the system date. On success it SHALL print exactly `OK TICKET CREATED -> <Precio>`, with the price formatted with two decimals and `.` as separator. Otherwise it SHALL print a line starting with `ERROR` and create nothing.

#### Scenario: Create for today or later
- **GIVEN** the system date is 2026-10-01 and the price is 40.00
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2026-10-15` or `TICKET CREATE;T002;ana@correo.com;2026-10-01` is processed
- **THEN** each prints `OK TICKET CREATED -> 40.00` and the ticket exists

#### Scenario: Nonexistent user
- **WHEN** `TICKET CREATE;T003;nadie@correo.com;2026-10-15` is processed
- **THEN** the result starts with `ERROR` and no ticket is created

#### Scenario: Past date
- **GIVEN** the system date is 2026-10-01
- **WHEN** `TICKET CREATE;T003;ana@correo.com;2026-09-30` is processed
- **THEN** the result starts with `ERROR` and no ticket is created

#### Scenario: Invalid input
- **WHEN** the identifier is already used or blank, the date is not a valid `yyyy-MM-dd` date (for example `2026-10-32` or `15-10-2026`), or the command has fewer or more than three parameters
- **THEN** the result starts with `ERROR` and no ticket is created

### Requirement: Ticket price configuration
The ticket price SHALL be `40.00` at the start of every run and SHALL NOT be persisted. The system SHALL process `SYSTEM PRICE;Precio`, where `Precio` is a positive decimal number using `.` as separator with at most two decimals. On success it SHALL set the price used by later ticket creations and print a line starting with `OK`. Tickets created earlier SHALL keep their price. An invalid price SHALL be rejected with `ERROR` and leave the price unchanged.

#### Scenario: Price applies to later tickets only
- **GIVEN** ticket `T001` was created at 40.00
- **WHEN** `SYSTEM PRICE;60.00` is processed and then `TICKET CREATE;T002;ana@correo.com;2026-10-20`
- **THEN** the creation prints `OK TICKET CREATED -> 60.00` and `T001` still has price 40.00

#### Scenario: Invalid price
- **WHEN** any of `SYSTEM PRICE;0`, `SYSTEM PRICE;-5.00`, `SYSTEM PRICE;abc`, `SYSTEM PRICE;60,00`, `SYSTEM PRICE;60.001` or `SYSTEM PRICE` is processed
- **THEN** the result starts with `ERROR` and the price is unchanged

#### Scenario: Price resets on restart
- **GIVEN** `SYSTEM PRICE;60.00` was processed in a previous run
- **WHEN** the application starts again and a ticket is created
- **THEN** it is created at 40.00

### Requirement: Delete ticket
The system SHALL process `TICKET DELETE;Id`, deleting the ticket only if it exists and its date is later than the system date (the system date itself does not count). Otherwise it SHALL print a line starting with `ERROR` and keep the ticket.

#### Scenario: Delete a future ticket
- **GIVEN** the system date is 2026-10-01 and ticket `T001` is dated 2026-10-15
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** the result starts with `OK` and `T001` no longer exists

#### Scenario: Ticket for today or the past
- **GIVEN** ticket `T001` is dated 2026-10-15
- **WHEN** the system date is 2026-10-15 or 2026-10-16 and `TICKET DELETE;T001` is processed
- **THEN** the result starts with `ERROR` and `T001` is kept

#### Scenario: Unknown ticket
- **WHEN** `TICKET DELETE;T999` is processed and no such ticket exists
- **THEN** the result starts with `ERROR`

### Requirement: Past tickets are historical records
Tickets SHALL NOT be removed automatically when their date passes, and SHALL be kept when their owner is deleted, keeping the owner's DNI as historical reference.

#### Scenario: History after user deletion
- **GIVEN** ticket `T001` dated 2026-10-15 belongs to Ana and the system date is 2026-10-16
- **WHEN** Ana is deleted and the application restarts
- **THEN** ticket `T001` still exists with its date, price and Ana's DNI
