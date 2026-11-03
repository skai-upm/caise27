# invoice-builders Specification

## Purpose

Generates on-demand user and group invoices from the system's economic information, constructed step by step with the Builder pattern.

## Requirements

### Requirement: Builder-based invoice construction
Invoices SHALL be built with the Builder pattern through a common construction abstraction and clearly differentiated steps, including only the sections relevant to the recipient and the available data; empty sections SHALL be omitted. Invoices SHALL NOT be persisted.

#### Scenario: Only applicable sections
- **WHEN** an invoice is generated for a user with tickets but no passes
- **THEN** it shows the user and ticket sections and no pass sections

### Requirement: User invoice
`INVOICE CREATE;USER;<email>` SHALL, for an active user, show the user's identifying data, the individual tickets purchased directly for that user, the SpeedPasses and Special Passes associated with that user's individual tickets (including composed tickets), each item's price and the total; composed tickets SHALL NOT be counted as individual purchases.

#### Scenario: Direct ticket, composed ticket and passes
- **WHEN** a user has a direct ticket at 40.00, a composed group ticket, and a SpeedPass at 8.00 on the composed ticket
- **THEN** the user invoice lists the direct ticket and the SpeedPass and totals 48.00

#### Scenario: Unknown or inactive user
- **WHEN** `INVOICE CREATE;USER` names an email of no active user
- **THEN** an error is reported

### Requirement: Group invoice
`INVOICE CREATE;GROUP;<groupIdentifier>` SHALL, for an existing group, show the group identifier and name, its group tickets with their composition and total price, and the overall total; composed tickets SHALL NOT be added separately and members' passes SHALL NOT be added.

#### Scenario: Group total counted once
- **WHEN** a group of two bought one group ticket at 80.00 and a member bought a pass
- **THEN** the group invoice total is 80.00 and the composition is shown without separate amounts

#### Scenario: Unknown group
- **WHEN** `INVOICE CREATE;GROUP` names a nonexistent group
- **THEN** an error is reported

#### Scenario: Unknown invoice type
- **WHEN** `INVOICE CREATE;COMPANY;X` is executed
- **THEN** an error is reported
