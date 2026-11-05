# Spec Delta

## Purpose

Defines the invoices the application generates on demand for users and groups from the economic information in the system, their contents, and how they avoid counting group ticket components twice.

## ADDED Requirements

### Requirement: User invoice
`INVOICE CREATE;USER;<email>` SHALL display an invoice for the active user with that email containing the user's identifying data (name, surnames, DNI, email), a section with the individual tickets purchased directly for the user (identifier, date and price of each) and the total of the items shown. Individual tickets that belong to a group ticket composition MUST NOT be listed as individual purchases nor added to the total. A non-registered email is rejected with an error message starting with `ERROR`.

#### Scenario: Direct tickets invoiced
- **GIVEN** Ana bought `T001` at 40.00 and `T002` at 45.50
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice shows Ana's data, `T001` 40.00 and `T002` 45.50, and a total of 85.50

#### Scenario: Group ticket components not double-counted
- **GIVEN** Ana bought `T001` at 40.00 and is in the composition of group ticket `TG01`
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** `TG01:12345678Z` does not appear as an individual purchase and the total is 40.00

#### Scenario: Unknown user
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` is processed
- **THEN** an error message starting with `ERROR` is printed

### Requirement: Group invoice
`INVOICE CREATE;GROUP;<groupIdentifier>` SHALL display an invoice for the existing group containing its identifier and name, a section with the group tickets it purchased (identifier, date, total price) and, for each, its composition (composition ticket identifier and member) as descriptive detail not added to the total, and the total of the group tickets. A non-existent group is rejected with an error message starting with `ERROR`.

#### Scenario: Group tickets invoiced with composition
- **GIVEN** group `G01` with Ana and Pedro bought `TG01` (80.00) and `TG02` (80.00)
- **WHEN** `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the invoice shows `G01` and its name, `TG01` and `TG02` with 80.00 each and their compositions, and a total of 160.00

#### Scenario: Unknown group or recipient type
- **WHEN** `INVOICE CREATE;GROUP;G99` or `INVOICE CREATE;COMPANY;X` is processed
- **THEN** an error message starting with `ERROR` is printed

### Requirement: Conditional sections
Invoices SHALL include only the sections relevant to the recipient type and the available information; sections without information MUST be omitted rather than displayed empty, and the total SHALL be shown in every invoice.

#### Scenario: User without purchases
- **GIVEN** Pedro has no direct tickets
- **WHEN** `INVOICE CREATE;USER;pedro@correo.com` is processed
- **THEN** the invoice shows Pedro's data and a total of 0.00, without an individual tickets section

### Requirement: Invoices built with the Builder pattern
Invoices SHALL be constructed through a common builder abstraction in clearly differentiated steps (recipient header, recipient-specific item sections, final build computing the total), with specific builders for user and group invoices; invoice objects SHALL NOT be created through a single constructor with many optional parameters, and generated invoices are not persisted.

#### Scenario: Builders share the common abstraction
- **WHEN** the invoice construction types are inspected
- **THEN** the user and group invoice builders extend a common builder abstraction and the invoice is obtained from the builder's build step
