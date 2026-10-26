# Spec Delta

## Purpose

Lets registered users buy individual park tickets for a given date, keeps them as persistent records, and manages the ticket price and ticket cancellation according to the system date.

## ADDED Requirements

### Requirement: Buy individual ticket
`TICKET CREATE;<id>;<email>;<yyyy-MM-dd>` SHALL create a ticket with that identifier for the registered user with that email, for that date, at the current ticket price, and print `OK TICKET CREATED -> <price>` with the price formatted with two decimals. It SHALL fail with an `ERROR` line if the identifier is empty or already used, no user has that email, the date is invalid, or the date is before the system date.

#### Scenario: Buy ticket
- **WHEN** the system date is 2026-10-01 and `TICKET CREATE;T001;ana@correo.com;2026-10-15` is processed for registered user Ana
- **THEN** ticket `T001` for Ana on 2026-10-15 with price 40.00 exists and `OK TICKET CREATED -> 40.00` is printed

#### Scenario: Unknown user
- **WHEN** `TICKET CREATE;T002;nadie@correo.com;2026-10-15` is processed
- **THEN** no ticket is created and an `ERROR` line is printed

#### Scenario: Duplicate identifier
- **WHEN** ticket `T001` exists and `TICKET CREATE;T001;ana@correo.com;2026-10-20` is processed
- **THEN** no ticket is created and an `ERROR` line is printed

#### Scenario: Date in the past
- **WHEN** the system date is 2026-10-01 and `TICKET CREATE;T003;ana@correo.com;2026-09-30` is processed
- **THEN** no ticket is created and an `ERROR` line is printed

### Requirement: Ticket data
Every ticket SHALL have an identifier, a date of use, a reference to the user it belongs to and the price it was sold at.

#### Scenario: Ticket refers to its user
- **WHEN** Ana buys ticket `T001` and later changes her first name
- **THEN** ticket `T001` refers to Ana with the new first name

### Requirement: Ticket price setting
The current ticket price SHALL be 40.00 at the start of every execution and SHALL be changed with `SYSTEM PRICE;<amount>`, printing `OK SYSTEM PRICE <amount>`. The amount SHALL be a positive decimal number with at most two decimals; otherwise an `ERROR` line SHALL be printed and the price kept. The setting SHALL NOT be persisted; already sold tickets keep their own price.

#### Scenario: Change price
- **WHEN** `SYSTEM PRICE;60.00` is processed and then `TICKET CREATE;T002;ana@correo.com;2026-10-20`
- **THEN** `OK TICKET CREATED -> 60.00` is printed and earlier tickets keep their price

#### Scenario: Invalid price
- **WHEN** `SYSTEM PRICE;-5` or `SYSTEM PRICE;abc` is processed
- **THEN** an `ERROR` line is printed and the price does not change

#### Scenario: Price resets on restart
- **WHEN** the price was changed to 60.00 and the application is restarted
- **THEN** new tickets cost 40.00

### Requirement: Delete ticket
`TICKET DELETE;<id>` SHALL delete the ticket and print `OK TICKET DELETED` only if its date is strictly after the system date; a ticket dated on or before the system date SHALL NOT be deleted and an `ERROR` line SHALL be printed. An unknown identifier SHALL produce an `ERROR` line.

#### Scenario: Future ticket
- **WHEN** the system date is 2026-10-01 and `TICKET DELETE;T001` is processed for a ticket dated 2026-10-15
- **THEN** the ticket is deleted

#### Scenario: Ticket for today
- **WHEN** the system date is 2026-10-15 and `TICKET DELETE;T001` is processed for a ticket dated 2026-10-15
- **THEN** the ticket is kept and an `ERROR` line is printed

### Requirement: Ticket persistence and history
Tickets SHALL be stored in a text file after every change and restored at startup, linked to their users. Tickets whose date has passed SHALL be kept as historical records, including when their user has been deleted.

#### Scenario: Restart restores tickets
- **WHEN** ticket `T001` exists and the application is restarted
- **THEN** ticket `T001` exists with the same date, user and price

#### Scenario: History of a deleted user
- **WHEN** Ana had a past ticket and was deleted, and the application is restarted
- **THEN** the past ticket still exists with Ana's DNI as its holder
