# personal-passes Specification

## Purpose

Lets visitors add SpeedPasses and a Special Pass to their individual tickets, each pass being an independent priced object whose consumption is tracked and which can be refunded while unused.

## Requirements

### Requirement: Pass model
A pass SHALL belong to exactly one individual ticket (bought directly or as a constituent of a group ticket) and SHALL have its own identifier, purchase price, and consumed state, initially unused. An individual ticket MAY hold any number of SpeedPasses and at most one Special Pass during its whole lifetime. Consumed passes SHALL remain recorded.

#### Scenario: Pass belongs to constituent ticket
- **GIVEN** group ticket `TG01` with constituent ticket `TG01-1`
- **WHEN** `SPEEDPASS CREATE;TG01-1;1` is processed
- **THEN** a SpeedPass attached to `TG01-1` exists

### Requirement: SPEEDPASS CREATE command
`SPEEDPASS CREATE;<IdTicket>;<Cantidad>` SHALL accept quantities `1` and `10` only and create that many independent SpeedPasses with identifiers `<IdTicket>-SP<n>` (n increasing per ticket, never reused). Each SHALL cost 20% of the ticket's stored price rounded to two decimals. The output SHALL be `OK SPEEDPASS CREATED -> <Precio>` with the total charged, followed by the created identifiers. It SHALL be rejected for unknown individual tickets, other quantities, and tickets dated before the system date.

#### Scenario: Pack of ten
- **GIVEN** ticket `T001` priced `40.00`, dated on or after the system date
- **WHEN** `SPEEDPASS CREATE;T001;10` is processed
- **THEN** the output starts with `OK SPEEDPASS CREATED -> 80.00` and ten distinct unused SpeedPasses of `8.00` each exist for `T001`

#### Scenario: Invalid quantity
- **WHEN** `SPEEDPASS CREATE;T001;5` is processed
- **THEN** an error message is reported and no pass is created

### Requirement: SPECIALPASS CREATE command
`SPECIALPASS CREATE;<IdTicket>` SHALL create the Special Pass `<IdTicket>-SPECIAL` priced at the ticket's stored price and output `OK SPECIALPASS CREATED -> <Precio>` followed by its identifier. It SHALL be rejected if the ticket has ever had a Special Pass, even if it was consumed or refunded, for unknown individual tickets, and for tickets dated before the system date.

#### Scenario: One Special Pass per lifetime
- **GIVEN** ticket `T001` priced `40.00` whose Special Pass was consumed
- **WHEN** `SPECIALPASS CREATE;T001` is processed
- **THEN** an error message is reported

#### Scenario: Special Pass price
- **GIVEN** constituent ticket `TG01-1` priced `36.00`
- **WHEN** `SPECIALPASS CREATE;TG01-1` is processed
- **THEN** the output starts with `OK SPECIALPASS CREATED -> 36.00`

### Requirement: Pass refunds
`SPEEDPASS DELETE;<IdPase>` and `SPECIALPASS DELETE;<IdPase>` SHALL remove an unused pass of the corresponding kind only when its ticket's date is strictly later than the system date (the E2 refund rule). Consumed passes, passes of today's or past tickets, and unknown identifiers SHALL be rejected without changes.

#### Scenario: Only unused refundable passes
- **GIVEN** system date 2026-10-01, ticket `T001` dated 2026-10-15 with unused SpeedPass `T001-SP1` and consumed SpeedPass `T001-SP2`
- **WHEN** `SPEEDPASS DELETE;T001-SP1` and `SPEEDPASS DELETE;T001-SP2` are processed
- **THEN** `T001-SP1` is removed and the second request is rejected leaving `T001-SP2` recorded

#### Scenario: Ticket not refundable
- **GIVEN** system date 2026-10-15 and an unused SpeedPass of a ticket dated 2026-10-15
- **WHEN** it is submitted to `SPEEDPASS DELETE`
- **THEN** an error message is reported and the pass remains
