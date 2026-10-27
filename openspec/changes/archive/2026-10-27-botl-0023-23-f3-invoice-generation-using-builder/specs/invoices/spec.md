# Spec Delta

## Purpose

Defines invoices for users and groups, built on demand from the park state with the Builder pattern, including only the sections relevant to each recipient.

## ADDED Requirements

### Requirement: Builder-based invoice construction
Invoices SHALL be constructed through a common builder abstraction with differentiated steps (recipient information, sections, items with amounts, item details) instead of constructors with many optional parameters, and the resulting invoice's total SHALL be the sum of the amounts of the items shown.

#### Scenario: Step-by-step construction
- **WHEN** a builder receives a recipient line, a section with two items of 40.00 and 36.00 and a detail line, and is asked to build
- **THEN** the invoice has that section with both items, the detail is not counted, and the total is 76.00

### Requirement: User invoice
`INVOICE CREATE;USER;<email>` SHALL produce, for an active user, an invoice identifying the user and listing the individual tickets purchased directly for that user with their prices and the total; individual tickets that belong to a group ticket SHALL NOT be listed as purchases.

#### Scenario: User with direct and group tickets
- **WHEN** Ana bought `T001` (40.00) and `T002` (45.50) and belongs to group ticket `TG01`
- **THEN** her invoice shows her name, DNI and email, an individual-tickets section with `T001` and `T002`, no line for `TG01:12345678Z`, and total 85.50

#### Scenario: Unknown or deleted user
- **WHEN** the email does not belong to an active user
- **THEN** an error is reported

### Requirement: Group invoice
`INVOICE CREATE;GROUP;<groupIdentifier>` SHALL produce, for an existing group, an invoice with the group identifier and name, each group ticket bought by the group with its total price and its composition as detail, and the total; component tickets SHALL NOT be added separately to the total.

#### Scenario: Group with one group ticket
- **WHEN** group `G01` (Familia Garcia) bought `TG01` for 3 members at 40.00
- **THEN** the invoice shows `G01`, `Familia Garcia`, `TG01` at 108.00 with its three component tickets as detail, and total 108.00

#### Scenario: Unknown group
- **WHEN** `INVOICE CREATE;GROUP;G99` is processed
- **THEN** an error is reported

### Requirement: Empty sections omitted
Sections without information SHALL be omitted from the invoice rather than shown empty.

#### Scenario: User without purchases
- **WHEN** a user who bought nothing requests an invoice
- **THEN** the invoice shows only the user identification and a total of 0.00, with no ticket section

#### Scenario: Invalid invoice type
- **WHEN** `INVOICE CREATE;COMPANY;X` or `INVOICE CREATE;USER` is processed
- **THEN** a format error is reported
