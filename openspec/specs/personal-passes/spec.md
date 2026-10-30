# personal-passes Specification

## Purpose

Lets users buy personal passes attached to individual tickets — SpeedPasses for priority entry and one Special Pass for special attractions — with stored prices, consumption state, refunds and persistence.

## Requirements

### Requirement: Pass model
A pass SHALL belong to exactly one individual ticket (standalone or constituent of a group ticket) and SHALL have its own identifier, purchase price and consumption status. Consumed passes SHALL remain recorded with status consumed.

#### Scenario: Consumed pass kept
- **GIVEN** a SpeedPass that has been used to enter an attraction
- **WHEN** the ticket's passes are inspected
- **THEN** the pass still exists and is marked consumed

### Requirement: Buy SpeedPasses
`SPEEDPASS CREATE;<idTicket>;<cantidad>` SHALL accept quantities `1` and `10` only, for an individual ticket dated on or after the system date, creating that many independent SpeedPasses identified `<idTicket>-SP<n>` (n increasing per ticket), each priced at 20 % of the ticket's stored price, and SHALL print `OK SPEEDPASS CREATED -> <importe>` with the total charged and the pass identifiers.

#### Scenario: Pack of ten
- **GIVEN** ticket `T001` priced 40.00
- **WHEN** `SPEEDPASS CREATE;T001;10` is executed
- **THEN** ten unused SpeedPasses priced 8.00 exist and the output starts with `OK SPEEDPASS CREATED -> 80.00`

#### Scenario: Invalid quantity or ticket
- **WHEN** the quantity is `5`, the ticket does not exist, the ticket is a group ticket, or its date is before the system date
- **THEN** an error is reported and no pass is created

### Requirement: Buy Special Pass
`SPECIALPASS CREATE;<idTicket>` SHALL create one Special Pass identified `<idTicket>-SPECIAL` priced at the ticket's stored price, for an individual ticket dated on or after the system date that has no Special Pass (unused or consumed), and SHALL print `OK SPECIALPASS CREATED -> <importe>`.

#### Scenario: Only one Special Pass per ticket
- **GIVEN** ticket `T001` whose Special Pass has been consumed
- **WHEN** `SPECIALPASS CREATE;T001` is executed
- **THEN** an error is reported

### Requirement: Refund passes
`SPEEDPASS DELETE;<idPase>` and `SPECIALPASS DELETE;<idPase>` SHALL delete an unused pass of the given kind only when its ticket's date is strictly after the system date. Consumed passes, passes of tickets dated today or earlier, unknown identifiers and identifiers of the other pass kind SHALL be rejected.

#### Scenario: Only unused future passes refunded
- **GIVEN** system date 2026-10-01, ticket `T001` dated 2026-10-15 with SpeedPasses `T001-SP1` (consumed) and `T001-SP2` (unused)
- **WHEN** both are submitted to `SPEEDPASS DELETE`
- **THEN** only `T001-SP2` is deleted

### Requirement: Pass persistence and cascade deletion
Passes SHALL be stored in `passes.txt` with kind, identifier, ticket identifier, price and consumption status and restored at startup. Deleting an individual ticket SHALL delete its passes; deleting a group ticket SHALL delete the passes of its constituent tickets, then those tickets, then the group ticket. A failed deletion SHALL leave tickets and passes unchanged, in memory and on disk.

#### Scenario: Passes survive restart
- **GIVEN** `T001` with a consumed SpeedPass and an unused Special Pass
- **WHEN** the application restarts
- **THEN** both passes exist with the same identifiers, prices, ticket and status

#### Scenario: Cascade on ticket deletion
- **GIVEN** a future ticket `T001` with passes
- **WHEN** `TICKET DELETE;T001` succeeds
- **THEN** none of its passes exist, before or after restart
