# Spec Delta

## Purpose

Generates on-demand invoices for users and groups from the purchases recorded in the system, assembled step by step with a Builder.

## ADDED Requirements

### Requirement: Invoice commands
`INVOICE CREATE;USER;<correo>` SHALL display the invoice of the registered user with that email and `INVOICE CREATE;GROUP;<idGrupo>` the invoice of the existing group. Unknown recipients or recipient kinds other than `USER` and `GROUP` SHALL be rejected with an error message. Invoices SHALL NOT be persisted.

#### Scenario: Invoice for an existing user
- **GIVEN** user `ana@correo.com` bought ticket `T001` for `40.00`
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** an invoice for Ana listing `T001` with `40.00` and a total of `40.00` is displayed

#### Scenario: Unknown recipient
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com`, `INVOICE CREATE;GROUP;G99`, or `INVOICE CREATE;SHOP;X` is processed
- **THEN** an error message is reported

### Requirement: User invoice content
A user invoice SHALL include a section of the individual tickets bought directly for that user and a section of the personal passes attached to any of the user's individual tickets, including tickets that belong to a group ticket. Individual tickets that belong to a group ticket SHALL NOT be listed as individual purchases. Each item SHALL show its price and the invoice SHALL show the total of all listed items.

#### Scenario: Group constituents are not double-counted
- **GIVEN** Ana bought ticket `T001` for `40.00` and is in group `G01` whose group ticket `TG01` includes a constituent ticket for her with a SpeedPass of `7.20`
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice lists `T001` and the SpeedPass, does not list `TG01` or its constituent ticket as a purchase, and its total is `47.20`

### Requirement: Group invoice content
A group invoice SHALL include a section of the group tickets bought by the group with their prices and the total, without listing their constituent tickets or the members' personal passes.

#### Scenario: Group invoice total
- **GIVEN** group `G01` bought `TG01` for `108.00` and `TG02` for `108.00`, and a member bought passes
- **WHEN** `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the invoice lists `TG01` and `TG02` and the total is `216.00`, with no passes listed

### Requirement: Builder-based assembly
Invoices SHALL be assembled through a Builder abstraction in distinct steps (recipient, each applicable section, total), adding a section only when it has items, without constructors taking many optional parameters.

#### Scenario: Empty sections omitted
- **GIVEN** a user with tickets but no passes
- **WHEN** the user invoice is generated
- **THEN** it contains a tickets section and no passes section

#### Scenario: Recipient without purchases
- **GIVEN** a registered user with no purchases
- **WHEN** the user invoice is generated
- **THEN** it shows the recipient, no item sections, and a total of `0.00`
