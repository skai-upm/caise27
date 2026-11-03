# invoice-builders Specification

## Purpose

Generates user and group invoices on demand from the current park state using the Builder pattern, including only applicable sections and never counting an amount twice (source F3-C02, section 6).

## Requirements

### Requirement: Invoices built with the Builder pattern
Invoices SHALL be constructed step by step through a common builder abstraction, adding only the sections relevant to the recipient and the available information, without constructors with many optional parameters; generated invoices SHALL NOT be persisted.

#### Scenario: Builder steps
- **WHEN** the invoice code is inspected
- **THEN** a builder abstraction with separate steps for header, sections, items and the final build exists, and both invoice types are produced through it

### Requirement: User invoice
`INVOICE CREATE;USER;<email>` SHALL, for an active user, display the user's identifying data, the individual tickets purchased directly for that user, the SpeedPasses and Special Passes associated with any of the user's individual tickets (including tickets inside group tickets), each item's price and the total of the items shown; tickets inside group tickets SHALL NOT be listed as purchases; empty sections SHALL be omitted.

#### Scenario: Direct ticket, group-derived ticket and passes
- **WHEN** Ana has standalone ticket `T001` (40.00), a ticket inside group ticket `TG01`, and a SpeedPass (8.00) on the composed ticket, and `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice lists `T001` and the SpeedPass but not the composed ticket, and the total is 48.00

#### Scenario: Sections without data omitted
- **WHEN** a user with tickets but no passes is invoiced
- **THEN** the invoice has no SpeedPass or Special Pass section

#### Scenario: Unknown user
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` is processed
- **THEN** an error is reported

### Requirement: Group invoice
`INVOICE CREATE;GROUP;<groupIdentifier>` SHALL, for an existing group, display its identifier and name, the group tickets it purchased with their composition and total price, and the total of the group tickets; composed tickets SHALL NOT be added separately and members' passes SHALL NOT be included in the total.

#### Scenario: Group ticket counted once
- **WHEN** group `G01` bought `TG01` (3 members, total 108.00) and a member owns a SpeedPass, and `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the invoice shows `TG01` with its three composed tickets and a total of 108.00

#### Scenario: Invalid invoice type or unknown group
- **WHEN** `INVOICE CREATE;GROUP;G99` or `INVOICE CREATE;OTHER;x` is processed
- **THEN** an error is reported
