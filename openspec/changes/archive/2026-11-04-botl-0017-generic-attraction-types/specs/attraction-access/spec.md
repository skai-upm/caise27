# Spec Delta

## ADDED Requirements

### Requirement: Ticket kind accepted by attraction type
`ATTRACTION ENTER` SHALL reject, without admitting anyone, a ticket whose kind the attraction type does not accept: a group ticket at an `INDIVIDUAL` or `SPECIAL` attraction, and an individual ticket at a `GROUP` attraction. `GENERAL` attractions SHALL accept both kinds. All other entry rules (date, capacity, atomic group entry, already present) SHALL continue to apply.

#### Scenario: GROUP rejects an individual ticket
- **GIVEN** `GROUP` attraction `A02` and individual ticket `T001` valid today
- **WHEN** `ATTRACTION ENTER;A02;T001` is processed
- **THEN** an `ERROR` message is produced and nobody enters

#### Scenario: INDIVIDUAL rejects a group ticket
- **GIVEN** `INDIVIDUAL` attraction `A03` and group ticket `TG01` valid today
- **WHEN** `ATTRACTION ENTER;A03;TG01` is processed
- **THEN** an `ERROR` message is produced and nobody enters

#### Scenario: Accepted kinds
- **WHEN** `T001` enters an `INDIVIDUAL` or `SPECIAL` attraction, `TG01` enters a `GROUP` attraction, and both enter a `GENERAL` attraction
- **THEN** every entry succeeds
