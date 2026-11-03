# Spec Delta

## Purpose

Generates on-demand invoices that show what a user or a group has purchased, with item prices and a total, and only the sections that apply to the recipient.

## ADDED Requirements

### Requirement: Invoice recipients
The command `INVOICE CREATE;USER;<correo>` SHALL generate an invoice for the registered user with that email, and `INVOICE CREATE;GROUP;<identificadorGrupo>` for the existing group with that identifier. An unknown user or group, another recipient type, or a wrong number of parameters SHALL be rejected with an error and no invoice.

#### Scenario: Unknown recipient
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` or `INVOICE CREATE;GROUP;G99` is processed
- **THEN** an error is reported and no invoice is shown

#### Scenario: Unknown type
- **WHEN** `INVOICE CREATE;PERSON;ana@correo.com` is processed
- **THEN** an error is reported

### Requirement: Invoice content
An invoice SHALL show the recipient, the issue date (the current system date), each applicable section with its items and their prices, and the total of all items. Item prices SHALL be the prices captured at purchase time.

#### Scenario: User invoice
- **GIVEN** Ana bought `T001` at 40.00 and, after `SYSTEM PRICE;60.00`, `T002` at 60.00
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice lists `T001` 40.00 and `T002` 60.00 under individual tickets and a total of 100.00

#### Scenario: Group invoice
- **GIVEN** group `G01` with 3 members bought `TG01` for 108.00
- **WHEN** `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the invoice lists `TG01` 108.00 under group tickets and a total of 108.00

### Requirement: No double counting
A user invoice SHALL include only individual tickets purchased directly for that user and SHALL NOT include the constituent tickets of group tickets; a group invoice SHALL include the group's group tickets with their totals and SHALL NOT add their constituent tickets separately.

#### Scenario: Member of a group ticket
- **GIVEN** Ana bought `T001` (40.00) and belongs to `G01`, which bought `TG01`
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** only `T001` is charged and the total is 40.00

#### Scenario: Group total not inflated
- **GIVEN** `G01` with 2 members bought `TG01` for 80.00
- **WHEN** `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the total is 80.00 and no individual ticket line appears

### Requirement: Only applicable sections
Sections for which the recipient has no data SHALL be omitted; a recipient without purchases SHALL get an invoice with no sections and a total of 0.00. Invoices SHALL be generated on demand and SHALL NOT be persisted.

#### Scenario: Empty sections omitted
- **GIVEN** Pedro has no tickets
- **WHEN** `INVOICE CREATE;USER;pedro@correo.com` is processed
- **THEN** the invoice has no individual tickets section and the total is 0.00

#### Scenario: Not persisted
- **WHEN** an invoice is generated
- **THEN** no data file is created or changed
