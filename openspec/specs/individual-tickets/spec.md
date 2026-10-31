# individual-tickets Specification

## Purpose

Lets registered users buy dated individual park tickets at the current base price, keeps tickets as persistent historical records, and controls refunds against the system date.

## Requirements

### Requirement: Ticket model
Every ticket SHALL have an identifier that is unique among all tickets (individual, group and group-constituent), a date of use and a purchase price. An individual ticket SHALL reference the user it belongs to.

#### Scenario: Ticket attributes
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2026-10-15` succeeds with base price 40.00
- **THEN** ticket `T001` has date 2026-10-15, price 40.00 and belongs to Ana

### Requirement: Runtime base price
The base individual ticket price SHALL be initialised to `40.00` on every execution and SHALL NOT be persisted. `SYSTEM PRICE;<price>` SHALL change it to a non-negative decimal amount; a malformed or negative price SHALL be rejected without changing the current price.

#### Scenario: Change base price
- **WHEN** `SYSTEM PRICE;60.00` is executed and then a ticket is bought
- **THEN** the new ticket costs 60.00

#### Scenario: Invalid price
- **WHEN** `SYSTEM PRICE;abc` or `SYSTEM PRICE;-5` is executed
- **THEN** an error is reported and the base price is unchanged

#### Scenario: Price resets on restart
- **WHEN** `SYSTEM PRICE;60.00` is executed and the application restarts
- **THEN** the base price is 40.00

### Requirement: TICKET CREATE command
`TICKET CREATE;<id>;<email>;<yyyy-MM-dd>` SHALL create an individual ticket priced at the current base price for the registered user with that email and print `OK TICKET CREATED -> <Precio>` with the price formatted with two decimals. It SHALL be rejected if the user does not exist, the identifier is already used, the date is invalid, or the date is earlier than the system date.

#### Scenario: Successful purchase
- **WHEN** the system date is 2026-10-01 and `TICKET CREATE;T001;ana@correo.com;2026-10-15` is executed
- **THEN** the output is `OK TICKET CREATED -> 40.00`

#### Scenario: Unknown user or duplicate identifier
- **WHEN** `TICKET CREATE` is executed for an unregistered email or with an identifier already used by another ticket
- **THEN** an error is reported and no ticket is created

#### Scenario: Date before the system date
- **WHEN** the system date is 2026-10-01 and `TICKET CREATE;T002;ana@correo.com;2026-09-30` is executed
- **THEN** an error is reported and no ticket is created

### Requirement: Purchase price is fixed
The price recorded on a ticket at purchase time SHALL be kept with the ticket; later base-price changes SHALL NOT modify existing tickets.

#### Scenario: Price change is not retroactive
- **WHEN** `T001` is bought at 40.00 and then `SYSTEM PRICE;60.00` is executed
- **THEN** `T001` still has price 40.00, including after a restart

### Requirement: TICKET DELETE command
`TICKET DELETE;<id>` SHALL delete an individual ticket only when its date is strictly later than the system date, deleting together with it all its SpeedPasses and its Special Pass. Tickets dated on or before the system date, unknown identifiers, and tickets that are constituents of a group ticket SHALL be rejected leaving the ticket and its passes unchanged.

#### Scenario: Only future tickets are refunded
- **WHEN** the system date is 2026-10-15 and tickets dated 2026-10-14, 2026-10-15 and 2026-10-16 exist and `TICKET DELETE` is executed for each
- **THEN** only the ticket dated 2026-10-16 is deleted

#### Scenario: Constituent ticket cannot be deleted alone
- **WHEN** `TICKET DELETE` names a ticket that belongs to a group ticket
- **THEN** an error is reported and the group ticket is unchanged

#### Scenario: Cascade to passes
- **WHEN** future ticket `T001` with SpeedPasses and a Special Pass is deleted and the application restarts
- **THEN** neither `T001` nor any of its passes exists

### Requirement: Ticket persistence and history
Individual tickets SHALL be persisted after every change and restored at startup with their identifier, date, price and user. Past tickets SHALL be kept as historical records, including after their user has been deleted, retaining the holder's identifying data (DNI, email, name, surname).

#### Scenario: Tickets restored
- **WHEN** `T001` is created and the application restarts
- **THEN** `T001` exists with the same date, price and user

#### Scenario: History survives user deletion
- **WHEN** Ana's only ticket is in the past, Ana is deleted and the application restarts
- **THEN** the ticket record still exists with Ana's DNI, email and name, and Ana is not a registered user
