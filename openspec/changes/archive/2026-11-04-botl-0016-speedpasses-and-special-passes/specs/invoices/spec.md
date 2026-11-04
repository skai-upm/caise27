# Spec Delta

## MODIFIED Requirements

### Requirement: User invoice
A user invoice SHALL include the individual tickets purchased directly for that user, each at the price captured at purchase. Constituent individual tickets of group tickets SHALL NOT be counted as individual purchases. The invoice SHALL also include, at their stored prices, the personal passes attached to any individual ticket owned by the user, including passes attached to constituent tickets.

#### Scenario: No double counting
- **GIVEN** Ana bought `T001` at 40.00 and `T002` at 60.00, and is also a member covered by group ticket `TG01`
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice lists `T001` 40.00 and `T002` 60.00 once each, does not list any `TG01` ticket, and the total is 100.00

#### Scenario: Personal passes on the user invoice
- **GIVEN** Ana owns standalone ticket `T001` with SpeedPass `SP1` (8.00) and constituent ticket `TG01-1` of a group ticket with Special Pass `SX1` (40.00)
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** a passes section lists `SP1` 8.00 and `SX1` 40.00, `TG01-1` itself is not charged, and the total includes both passes

### Requirement: Group invoice
A group invoice SHALL include the group-ticket purchases of that group, each at its stored total, without adding the constituent individual tickets or the members' personal passes.

#### Scenario: Group purchases
- **GIVEN** group `G01` with three members bought `TG01` (108.00) and later `TG02` (108.00)
- **WHEN** `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the invoice lists `TG01` and `TG02` with 108.00 each, no individual tickets, and the total is 216.00

#### Scenario: Members' own tickets not included
- **GIVEN** a member of `G01` also bought an individual ticket `T001`
- **WHEN** the group invoice is generated
- **THEN** `T001` does not appear on it

#### Scenario: Members' passes not included
- **GIVEN** a member covered by `TG01` bought a SpeedPass for their constituent ticket
- **WHEN** `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the pass does not appear on the group invoice and the total is unchanged
