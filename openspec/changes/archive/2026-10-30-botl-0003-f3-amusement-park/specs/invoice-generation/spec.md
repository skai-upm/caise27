# Spec Delta

## Purpose

Generates on-demand invoices for users and groups from the purchases recorded in the system, assembled step by step with a Builder.

## ADDED Requirements

### Requirement: Invoice builder
Invoices SHALL be assembled through a Builder abstraction in distinct steps (header, one step per section, build), adding only sections that apply to the recipient and have items. Invoice variants SHALL NOT be handled with constructors taking many optional parameters. Invoices SHALL NOT be persisted.

#### Scenario: Empty sections omitted
- **GIVEN** a user with tickets but no passes
- **WHEN** the user's invoice is generated
- **THEN** it contains a ticket section and no pass section

### Requirement: User invoice
`INVOICE CREATE;USER;<correo>` SHALL print an invoice for the registered user listing each individual ticket bought directly for that user (identifier, date, price) and each personal pass attached to any of the user's individual tickets, including tickets inside group tickets (identifier, type, price), followed by the total. Individual tickets that compose a group ticket SHALL NOT be listed as ticket purchases. Unknown users SHALL be rejected.

#### Scenario: User invoice without double counting
- **GIVEN** Ana bought ticket `T001` for 40.00 and belongs to group ticket `TG01` whose constituent `TG01-1` has a SpeedPass of 7.20
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is executed
- **THEN** the invoice lists `T001` 40.00 and the SpeedPass 7.20, does not list `TG01-1` as a ticket, and shows total 47.20

#### Scenario: Unknown user
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` is executed
- **THEN** an error is reported

### Requirement: Group invoice
`INVOICE CREATE;GROUP;<idGrupo>` SHALL print an invoice for an existing group listing each group ticket bought for it (identifier, date, number of members, price) and the total. It SHALL NOT list the constituent tickets nor the members' passes. Unknown groups and an invoice type other than `USER` or `GROUP` SHALL be rejected.

#### Scenario: Group invoice
- **GIVEN** group `G01` bought `TG01` for 108.00 and `TG02` for 80.00
- **WHEN** `INVOICE CREATE;GROUP;G01` is executed
- **THEN** the invoice lists both group tickets and shows total 188.00
