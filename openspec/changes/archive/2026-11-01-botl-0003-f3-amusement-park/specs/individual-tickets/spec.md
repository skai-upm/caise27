# Spec Delta

## MODIFIED Requirements

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
