# Spec Delta

## Purpose

Produces on-demand invoices that summarize what a user or a group has paid, without double-counting composed tickets.

## ADDED Requirements

### Requirement: User invoice
`INVOICE CREATE;USER;<correo>` SHALL display an invoice for the registered user with that email containing a section of the user's standalone individual tickets and a section of the personal passes attached to any of the user's tickets (including constituents of group tickets), each item with its price, and a total. Constituent tickets SHALL NOT be listed as ticket purchases.

#### Scenario: Tickets and passes
- **WHEN** Ana bought `T001` at 40.00 with one SpeedPass at 8.00 and holds constituent `TG01-1` with a Special Pass at 40.00, and `INVOICE CREATE;USER;ana@correo.com` runs
- **THEN** the invoice lists `T001` (40.00) as a ticket, both passes as passes, does not list `TG01-1` as a ticket, and shows total 88.00

#### Scenario: Unknown user
- **WHEN** the email is not registered
- **THEN** an error is shown and no invoice is produced

### Requirement: Group invoice
`INVOICE CREATE;GROUP;<idGrupo>` SHALL display an invoice for the existing group listing its group tickets with their prices and a total, without its constituent tickets or members' passes.

#### Scenario: Group tickets only
- **WHEN** group `G01` bought `TG01` at 108.00 and members hold passes, and `INVOICE CREATE;GROUP;G01` runs
- **THEN** the invoice lists `TG01` at 108.00, no constituent tickets or passes, and total 108.00

#### Scenario: Unknown group or type
- **WHEN** the group does not exist or the recipient type is neither `USER` nor `GROUP`
- **THEN** an error is shown

### Requirement: Applicable sections only
An invoice SHALL include only sections that apply to its recipient type and have items; a recipient with no items SHALL get an invoice with header and a total of 0.00. Invoices SHALL NOT be persisted.

#### Scenario: Empty section omitted
- **WHEN** a user with tickets but no passes requests an invoice
- **THEN** the invoice has no passes section

#### Scenario: Nothing purchased
- **WHEN** a user without tickets requests an invoice
- **THEN** the invoice shows the header and total 0.00 and no files change

### Requirement: Builder construction
Invoices SHALL be assembled through a Builder abstraction in distinct steps (recipient header, each applicable section, total); invoice variants SHALL NOT be handled by constructors with many optional parameters.

#### Scenario: Builder in use
- **WHEN** the invoice code is inspected
- **THEN** a builder interface with step methods and a concrete builder produce the invoice
