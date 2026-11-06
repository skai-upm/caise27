# invoices Specification

## Purpose
Generates on-demand invoices for users and groups from the tickets kept by the system. Each invoice contains only the sections relevant to its recipient and never counts the same purchase twice.

## Requirements

### Requirement: User invoice
The system SHALL, for `INVOICE CREATE;USER;<email>` naming an active registered user, display an invoice with the user's identifying information (email and DNI), a section listing each individual ticket purchased directly for that user with its identifier, date and price, a section with the SpeedPasses and a section with the Special Passes associated with any of the user's individual tickets (including tickets that compose a group ticket), each with its price, and the total of the listed items. Individual tickets that are part of a group ticket SHALL NOT be listed or counted. A user who does not exist SHALL produce an error message.

#### Scenario: User with direct and group tickets
- **GIVEN** `ana@correo.com` (DNI `12345678Z`) has direct tickets `T001` (40.00) and `T002` (45.50) and holds `TG01:12345678Z` in group ticket `TG01`
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice shows `ana@correo.com` and `12345678Z`
- **AND** lists `T001` and `T002` with their prices
- **AND** does not list `TG01:12345678Z`
- **AND** shows the total 85.50

#### Scenario: Unknown user
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` is processed and no such user exists
- **THEN** an error message is displayed

#### Scenario: Passes on the user invoice
- **GIVEN** `ana@correo.com` has direct ticket `T001` (40.00) with SpeedPass `SP01:1` (8.00), and composed ticket `TG01:12345678Z` (40.00) with Special Pass `PE01` (40.00)
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice lists `T001`, `SP01:1` and `PE01` but not `TG01:12345678Z`
- **AND** shows the total 88.00
- **AND** `INVOICE CREATE;GROUP` for the group of `TG01` does not include `PE01`

### Requirement: Group invoice
The system SHALL, for `INVOICE CREATE;GROUP;<groupIdentifier>` naming an existing group, display an invoice with the group identifier and name, a section listing each group ticket bought by that group with its identifier, date and total price, the composition of each group ticket (ticket identifiers and users) as descriptive detail, and the sum of the group ticket totals. Composed individual tickets SHALL NOT be added to the total, and members' personal purchases SHALL NOT be included. An unknown group SHALL produce an error message.

#### Scenario: Group with two group tickets
- **GIVEN** group `G01` bought `TG01` (2 members, 80.00) and `TG02` (2 members, 91.00)
- **WHEN** `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the invoice shows `G01` and its name, lists `TG01` and `TG02` with their totals and their composed tickets
- **AND** shows the total 171.00

#### Scenario: Unknown group
- **WHEN** `INVOICE CREATE;GROUP;G99` is processed and `G99` does not exist
- **THEN** an error message is displayed

### Requirement: Only relevant sections are shown
An invoice SHALL contain only sections that have items. A section without items SHALL be omitted instead of shown empty, and the total SHALL be the sum of the items shown.

#### Scenario: User without purchases
- **GIVEN** registered user `pedro@correo.com` with no direct tickets
- **WHEN** `INVOICE CREATE;USER;pedro@correo.com` is processed
- **THEN** the invoice shows the user's identification and a total of 0.00
- **AND** shows no ticket section

### Requirement: Invoices built step by step
Invoices SHALL be built through a common builder abstraction whose steps add the parts of the invoice one at a time. User and group invoices SHALL use builders that offer only the steps meaningful for them, without constructors with many optional parameters or placeholder values.

#### Scenario: Builder steps
- **WHEN** a group invoice is built
- **THEN** it is produced through distinct steps (recipient, group tickets with composition, build) of a builder implementing the common abstraction

### Requirement: Invalid invoice requests
The system SHALL reject an `INVOICE CREATE` whose type is not `USER` or `GROUP`, or that has missing arguments, with an error message.

#### Scenario: Unknown invoice type
- **WHEN** `INVOICE CREATE;COMPANY;X` or `INVOICE CREATE;USER` is processed
- **THEN** an error message is displayed

### Requirement: INVOICE commands listed in HELP
The system SHALL include `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>` in the output of `HELP`.

#### Scenario: HELP shows INVOICE commands
- **WHEN** `HELP` is processed
- **THEN** the output contains both INVOICE command syntaxes
