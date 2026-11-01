# individual-tickets Specification

## Purpose

Lets registered users buy dated individual tickets at a configurable base price, keeps purchased tickets as persistent history, and lets future tickets be refunded.

## Requirements

### Requirement: Runtime base ticket price
The application SHALL hold a base individual-ticket price initialised to `40.00` at every startup and not persisted. `SYSTEM PRICE;<Precio>` SHALL set it to a positive decimal amount; an invalid, zero, or negative amount SHALL be rejected with an error message leaving the price unchanged.

#### Scenario: Change price
- **WHEN** `SYSTEM PRICE;60.00` is processed
- **THEN** tickets bought afterwards cost `60.00`

#### Scenario: Invalid price
- **WHEN** `SYSTEM PRICE;abc` or `SYSTEM PRICE;-5` is processed
- **THEN** an error message is reported and the base price is unchanged

#### Scenario: Price resets on restart
- **GIVEN** `SYSTEM PRICE;60.00` was executed in a previous run
- **WHEN** the application starts again
- **THEN** the base price is `40.00`

### Requirement: Individual ticket model
Each individual ticket SHALL have a unique identifier, a date of use, a reference to its holder user, and the price paid at purchase.

#### Scenario: Ticket attributes
- **GIVEN** base price `40.00` and system date 2026-10-01
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2026-10-15` succeeds
- **THEN** ticket `T001` has date 2026-10-15, holder Ana, and price `40.00`

### Requirement: TICKET CREATE command
`TICKET CREATE;<Id>;<Correo>;<yyyy-MM-dd>` SHALL create an individual ticket for the registered user with that email at the current base price and report exactly `OK TICKET CREATED -> <Precio>` with the price formatted with two decimals and `.` as separator. It SHALL be rejected when the user does not exist, the identifier is already used by any ticket, the date is invalid, or the date is earlier than the system date.

#### Scenario: Create ticket
- **GIVEN** system date 2026-10-01, base price `40.00`, and registered user `ana@correo.com`
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2026-10-15` is processed
- **THEN** the output is `OK TICKET CREATED -> 40.00`

#### Scenario: Unknown user or past date
- **GIVEN** system date 2026-10-01
- **WHEN** a ticket is requested for a nonexistent email or for date 2026-09-30
- **THEN** an error message is reported and no ticket is created

#### Scenario: Duplicate identifier
- **GIVEN** ticket `T001` exists
- **WHEN** `TICKET CREATE;T001;pedro@correo.com;2026-10-20` is processed
- **THEN** an error message is reported and no ticket is created

### Requirement: Purchase price is not retroactive
Changing the base price SHALL NOT modify the price stored on tickets already purchased.

#### Scenario: Price change after purchase
- **GIVEN** ticket `T001` bought at `40.00`
- **WHEN** `SYSTEM PRICE;60.00` is processed and the application restarts
- **THEN** ticket `T001` still has price `40.00`

### Requirement: TICKET DELETE command
`TICKET DELETE;<Id>` SHALL delete an individual ticket, together with all its SpeedPasses and its Special Pass, only when its date is strictly later than the system date. It SHALL be rejected for tickets dated today or in the past, for unknown identifiers, and for tickets that belong to a group ticket. A rejected or failed deletion SHALL leave the ticket and all its passes unchanged in memory and in storage.

#### Scenario: Future-only ticket refund
- **GIVEN** system date 2026-10-15 and tickets dated 2026-10-14, 2026-10-15, and 2026-10-16
- **WHEN** each is submitted to `TICKET DELETE`
- **THEN** only the ticket dated 2026-10-16 is deleted

#### Scenario: Cascade to passes
- **GIVEN** a future ticket `T001` with SpeedPasses and a Special Pass
- **WHEN** `TICKET DELETE;T001` succeeds and the application restarts
- **THEN** neither `T001` nor any of its passes exist

### Requirement: Ticket persistence and history
Individual tickets SHALL be persisted after every change and restored at startup with their holder, date, purchase price, passes (identifier, kind, price, consumed state), and whether a Special Pass was ever issued. Past tickets SHALL be kept as historical records, including when their holder has since been deleted. Data files SHALL be replaced atomically so a failed write never leaves a partially written file.

#### Scenario: Tickets survive restart
- **GIVEN** ticket `T001` for Ana
- **WHEN** the application restarts
- **THEN** `T001` exists with the same date, holder, and price

#### Scenario: History survives holder deletion
- **GIVEN** a past ticket `T001` whose holder Ana was deleted
- **WHEN** the application restarts
- **THEN** `T001` still exists with Ana's DNI, name, and email as historical holder data

#### Scenario: Passes survive restart
- **GIVEN** ticket `T001` with an unused SpeedPass, a consumed SpeedPass, and a Special Pass
- **WHEN** the application restarts
- **THEN** the three passes exist with the same identifiers, prices, and consumed states, attached to `T001`
