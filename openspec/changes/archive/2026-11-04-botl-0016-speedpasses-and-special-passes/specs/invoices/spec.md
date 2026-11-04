## MODIFIED Requirements

### Requirement: User invoice contents
A user invoice SHALL identify the user and list the individual tickets purchased directly for that user, each with its identifier, date and price, then, in a personal passes section, the passes attached to any of the user's tickets (including constituent tickets of group tickets) with their price, followed by the total; individual tickets that are constituents of a group ticket SHALL NOT be listed or charged as tickets.

#### Scenario: Direct ticket charged once, constituent not charged
- **GIVEN** Ana bought `T001` at 40.00 and is a member of `G01`, which bought `TG01` (constituent `TG01-1` for Ana)
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice lists `T001` with 40.00, does not list `TG01-1`, and the total is 40.00

#### Scenario: Several tickets
- **GIVEN** Ana bought `T001` at 40.00 and, after `SYSTEM PRICE;60.00`, `T002` at 60.00
- **WHEN** her invoice is created
- **THEN** both tickets are listed with their own prices and the total is 100.00

#### Scenario: Personal pass on a constituent ticket
- **GIVEN** Ana bought `T001` at 40.00 and SpeedPass `P01-1` (7.20) is attached to her constituent ticket `TG01-1`
- **WHEN** her invoice is created
- **THEN** it lists `T001` (40.00) under tickets and `P01-1` (7.20) under personal passes, not `TG01-1`, and the total is 47.20

### Requirement: Group invoice contents
A group invoice SHALL identify the group and list the group tickets purchased by that group, each with its identifier, date, number of members and total amount, followed by the overall total; the constituent individual tickets and the members' personal passes SHALL NOT be added.

#### Scenario: Group ticket amount only
- **GIVEN** `G01` with 3 members bought `TG01` for 108.00
- **WHEN** `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the invoice lists `TG01` with 108.00 and the total is 108.00 (not 108.00 + 3 × 40.00)

#### Scenario: Members' passes not charged to the group
- **GIVEN** a member of `G01` bought a pass on her constituent ticket of `TG01`
- **WHEN** `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the total is still the group ticket amount

### Requirement: Builder-based construction
Invoices SHALL be constructed through a common builder abstraction whose distinct steps (recipient, individual tickets, group tickets, personal passes, total) are invoked in order by a director, with one concrete builder per recipient type; invoice variants SHALL NOT be created through constructors with many optional parameters.

#### Scenario: Same director for both types
- **WHEN** a user invoice and a group invoice are generated
- **THEN** both are produced by the same director running the same sequence of steps on different builders
