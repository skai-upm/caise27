# Spec Delta

## Purpose

Generates on-demand user and group invoices from the current park state, built step by step with the Builder pattern so each invoice contains only the sections relevant to its recipient.

## ADDED Requirements

### Requirement: Builder-based construction
Invoices SHALL be built through a common builder abstraction, step by step, with each step adding one part (header, item sections, total). They SHALL NOT be built through a single constructor with many optional parameters. Sections with no items SHALL be omitted. Generated invoices SHALL NOT be persisted.

#### Scenario: Empty sections omitted
- **WHEN** a user invoice is generated for a user with tickets but no passes
- **THEN** the invoice contains no SpeedPass or Special Pass section

### Requirement: User invoice
`INVOICE CREATE;USER;<email>` SHALL, for an active user, print an invoice with the user's identifying data, the individual tickets bought directly for the user, the SpeedPasses and Special Passes on any of the user's individual tickets (including group-composition tickets), each item's price, and the total of the items shown. Composition tickets SHALL NOT be listed as individual purchases.

#### Scenario: User with ticket and passes
- **WHEN** Ana has T001 (40.00) with SpeedPass SP01:1 (8.00), plus composition ticket TG01:12345678Z (36.00) with Special Pass PE01 (36.00), and `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice lists T001, SP01:1 and PE01, but not TG01:12345678Z as a ticket, with a total of 84.00

#### Scenario: Unknown user
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` is processed
- **THEN** an error is printed

### Requirement: Group invoice
`INVOICE CREATE;GROUP;<groupIdentifier>` SHALL, for an existing group, print an invoice with the group identifier and name, each group ticket bought by the group with its composition and total price, and the sum of those group-ticket totals. Composition tickets and members' passes SHALL NOT be added to the total.

#### Scenario: Group with one group ticket
- **WHEN** G01 bought TG01 (3 members × 36.00) and a member holds a Special Pass on their composition ticket, and `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the invoice lists TG01 with its three composition tickets and a total of 108.00

#### Scenario: Unknown recipient type or group
- **WHEN** `INVOICE CREATE;GROUP;G99` or `INVOICE CREATE;FOO;x` is processed
- **THEN** an error is printed
