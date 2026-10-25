# Spec Delta

## Purpose

Defines individual park tickets: their data and price, how they are bought and refunded, how they are persisted as history, and how the system date governs them.

## ADDED Requirements

### Requirement: Ticket data
Every individual ticket SHALL have a unique identifier, a date of use, a holder that is a reference to the registered user who owns it, and the price charged when it was bought.

#### Scenario: Ticket holds user and price
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2026-10-15` succeeds while the price is 40.00
- **THEN** ticket T001 has date 2026-10-15, holder Ana and price 40.00

### Requirement: Current ticket price
The application SHALL keep a current individual ticket price, initialised to 40.00 on every execution and not persisted. `SYSTEM PRICE;<price>` SHALL set it to a positive decimal amount and respond `OK SYSTEM PRICE -> <price>` with two decimals; other values are rejected with `ERROR INVALID PRICE`. Changing the price does not alter tickets already bought.

#### Scenario: Change price
- **WHEN** `SYSTEM PRICE;60.00` is processed and then a ticket is created
- **THEN** the response to the price command is `OK SYSTEM PRICE -> 60.00` and the ticket is created with price 60.00

#### Scenario: Invalid price
- **WHEN** `SYSTEM PRICE;gratis`, `SYSTEM PRICE;0` or `SYSTEM PRICE;-5` is processed
- **THEN** the response is `ERROR INVALID PRICE` and the price is unchanged

### Requirement: Buy individual ticket
`TICKET CREATE;<id>;<email>;<yyyy-MM-dd>` SHALL create a ticket for the registered user with that email at the current price and respond `OK TICKET CREATED -> <price>`. It SHALL reject an empty identifier (`ERROR EMPTY FIELD`), an identifier already used by another ticket (`ERROR DUPLICATE TICKET`), an unknown email (`ERROR USER NOT FOUND`), an invalid date (`ERROR INVALID DATE`) and a date earlier than the system date (`ERROR PAST DATE`).

#### Scenario: Successful purchase
- **WHEN** the system date is 2026-10-01, Ana exists and `TICKET CREATE;T001;ana@correo.com;2026-10-15` is processed
- **THEN** the response is `OK TICKET CREATED -> 40.00`

#### Scenario: Unknown user
- **WHEN** `TICKET CREATE;T002;nadie@correo.com;2026-10-15` is processed
- **THEN** the response is `ERROR USER NOT FOUND` and no ticket is created

#### Scenario: Date in the past
- **WHEN** the system date is 2026-10-01 and `TICKET CREATE;T003;ana@correo.com;2026-09-30` is processed
- **THEN** the response is `ERROR PAST DATE`

### Requirement: Refund individual ticket
`TICKET DELETE;<id>` SHALL delete the ticket and respond `OK TICKET DELETED` only when its date is strictly later than the system date; a ticket dated today or earlier SHALL be kept and the response is `ERROR TICKET NOT REFUNDABLE`. An unknown identifier is rejected with `ERROR TICKET NOT FOUND`.

#### Scenario: Future ticket deleted
- **WHEN** the system date is 2026-10-01 and `TICKET DELETE;T001` is processed for a ticket dated 2026-10-15
- **THEN** the response is `OK TICKET DELETED`

#### Scenario: Ticket for today
- **WHEN** the system date is 2026-10-15 and `TICKET DELETE;T001` is processed for a ticket dated 2026-10-15
- **THEN** the response is `ERROR TICKET NOT REFUNDABLE` and the ticket remains

### Requirement: Ticket persistence and history
Tickets SHALL be stored in a text file (`data/tickets.txt` by default), one per line as `<id>;<yyyy-MM-dd>;<holder DNI>;<price>`, rewritten after every change and restored at startup linked to the restored users. Past tickets SHALL be kept as historical records, also when their holder has since been deleted.

#### Scenario: Tickets restored after restart
- **WHEN** a ticket is created and the application is restarted
- **THEN** the ticket exists with the same date, holder and price

#### Scenario: Past tickets kept
- **WHEN** the system date moves past a ticket's date
- **THEN** the ticket is still stored and is not removed
