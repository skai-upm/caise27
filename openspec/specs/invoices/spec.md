# invoices Specification

## Purpose
Defines on-demand invoices for users and groups: which purchases each one includes, how their totals are computed without double-counting, and how they are built from the system's economic information.

## Requirements

### Requirement: User invoice
The command `INVOICE CREATE;USER;<email>` SHALL show an invoice for an existing active user. It SHALL contain the user's identifying information, a section listing each individual ticket bought directly for that user (identifier, date and price), a section listing the SpeedPasses and Special Passes associated with that user's individual tickets (including tickets that make up a group ticket) with their prices, and a total equal to the sum of the listed items. Sections with no items SHALL be omitted. Individual tickets that make up a group ticket SHALL NOT be listed or counted on the user invoice. An unknown user SHALL be rejected with an error.

#### Scenario: Direct tickets invoiced
- **GIVEN** `ana@correo.com` with direct tickets `T001` (40.00) and `T002` (45.50), and a component ticket `TG01:12345678Z` (36.00) of a group ticket
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice shows the user's e-mail and DNI, lists `T001` and `T002` with their prices, does not list `TG01:12345678Z`, and shows total 85.50

#### Scenario: Unknown user
- **WHEN** `INVOICE CREATE;USER;nobody@correo.com` is processed
- **THEN** an error is shown and no invoice is displayed

#### Scenario: Personal passes invoiced
- **GIVEN** `ana@correo.com` with direct ticket `T001` (40.00) with SpeedPass `SP01:1` (8.00), and group component `TG01:12345678Z` (36.00) with Special Pass `PE02` (36.00)
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice lists `T001`, `SP01:1` and `PE02`, does not list `TG01:12345678Z`, and shows total 84.00

### Requirement: Group invoice
The command `INVOICE CREATE;GROUP;<groupIdentifier>` SHALL show an invoice for an existing group. It SHALL contain the group identifier and name, a section listing each group ticket the group bought with its total price and, as detail, its component tickets, and a total equal to the sum of the group-ticket totals. Component tickets SHALL NOT be added again to the total, and members' personal purchases SHALL NOT be included. An unknown group SHALL be rejected with an error.

#### Scenario: Group tickets invoiced once
- **GIVEN** group `G01` "Familia" that bought `TG01` (3 members, total 108.00) and `TG02` (total 120.00)
- **WHEN** `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the invoice shows `G01` and `Familia`, lists `TG01` and `TG02` with their totals and their component tickets as detail, and shows total 228.00

#### Scenario: Members' direct tickets excluded
- **GIVEN** a member of `G01` also has direct ticket `T001`
- **WHEN** the group invoice is generated
- **THEN** `T001` does not appear and does not affect the total

#### Scenario: Unknown group
- **WHEN** `INVOICE CREATE;GROUP;G99` is processed
- **THEN** an error is shown

### Requirement: Only applicable sections
An invoice SHALL include only the sections that apply to its recipient type and have information. Sections with nothing to show SHALL be omitted, not displayed empty, and the total SHALL then be 0.00. A malformed command (wrong number of fields or an invoice type other than `USER` or `GROUP`) SHALL be rejected with an error.

#### Scenario: User without purchases
- **GIVEN** an existing user with no direct tickets
- **WHEN** their invoice is generated
- **THEN** it shows the identifying information and total 0.00, with no ticket section

#### Scenario: Invalid invoice type
- **WHEN** `INVOICE CREATE;COMPANY;X` is processed
- **THEN** an error is shown

### Requirement: Invoices built step by step
Invoices SHALL be built through a common builder abstraction with clearly separate construction steps, and recipient-specific steps SHALL be offered only by the builder for that recipient type. Invoices SHALL be generated on demand from the current system state and SHALL NOT be persisted.

#### Scenario: Invoice reflects current state
- **GIVEN** a user invoice was generated, and then a new direct ticket was bought for that user
- **WHEN** the invoice is generated again
- **THEN** it includes the new ticket and the updated total
