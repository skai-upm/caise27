# Spec Delta

## Purpose

Generates user and group invoices on demand from the system state, constructed step by step through the Builder pattern with only the sections relevant to the recipient.

## ADDED Requirements

### Requirement: Builder-based invoice construction
Invoices SHALL be built through a common builder abstraction in distinct steps, including only sections that have content; empty sections SHALL be omitted. Each item SHALL show its price and the invoice SHALL show the total of the items shown. Invoices SHALL NOT be persisted.

#### Scenario: Omitted section
- **WHEN** a user has individual tickets but no passes
- **THEN** the user invoice contains no pass section

### Requirement: User invoice
`INVOICE CREATE;USER;<email>` SHALL require an active registered user and SHALL include the user's identifying data, the individual tickets purchased directly for the user, and the SpeedPasses and Special Passes associated with the user's individual tickets, including tickets that compose group tickets. Composition tickets SHALL NOT be listed or counted as individual purchases.

#### Scenario: Direct ticket, group ticket and passes
- **WHEN** Ana has T001 (40.00), a composition ticket in a group ticket, and a SpeedPass (7.20) on that composition ticket
- **THEN** Ana's invoice lists T001 and the SpeedPass and totals 47.20

#### Scenario: Unknown user
- **WHEN** the email is not of an active registered user
- **THEN** an error is reported

### Requirement: Group invoice
`INVOICE CREATE;GROUP;<groupIdentifier>` SHALL require an existing group and SHALL include the group identifier and name, its group tickets with their composition and total price, and the sum of those totals. Composition tickets SHALL NOT be added separately and members' passes SHALL NOT be included.

#### Scenario: Group ticket counted once
- **WHEN** group G01 bought TG01 (108.00 for 3 members) and a member bought a SpeedPass on their composition ticket
- **THEN** the group invoice lists TG01 with its 3 composition tickets and totals 108.00

#### Scenario: Unknown group
- **WHEN** the group does not exist
- **THEN** an error is reported
