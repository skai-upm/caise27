# Spec Delta

## Purpose

Generates on-demand invoices for users and groups from the purchases recorded in the system, assembled step by step and showing each item's price and the total.

## ADDED Requirements

### Requirement: User invoice
`INVOICE CREATE;USER;<correo>` SHALL generate an invoice for the registered user with that email, listing the individual tickets bought directly for that user with their identifier, date and price, and the total. Individual tickets that belong to a group ticket SHALL NOT be listed as individual purchases.

#### Scenario: User with direct and group tickets
- **WHEN** Ana bought T001 (40.00) directly and is a member of a group that bought TG01, and `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice lists T001 with 40.00, does not list any ticket of TG01, and the total is 40.00

#### Scenario: Unknown user
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` is processed
- **THEN** an error is reported

### Requirement: Group invoice
`INVOICE CREATE;GROUP;<identificadorGrupo>` SHALL generate an invoice for the existing group listing each of its group-ticket purchases with identifier, date, number of tickets and price, and the total, without listing the individual tickets inside them.

#### Scenario: Group with purchases
- **WHEN** group `G01` bought TG01 (108.00) and TG02 (80.00) and `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the invoice lists TG01 and TG02 with their prices and the total is 188.00

#### Scenario: Unknown group
- **WHEN** `INVOICE CREATE;GROUP;G99` is processed and `G99` does not exist
- **THEN** an error is reported

### Requirement: Only applicable sections
An invoice SHALL include only the sections that apply to its recipient type and for which data exists; a recipient without purchases SHALL receive an invoice with its header and a total of 0.00.

#### Scenario: User without purchases
- **WHEN** Pedro has no tickets and `INVOICE CREATE;USER;pedro@correo.com` is processed
- **THEN** the invoice shows Pedro's header, no ticket section and `TOTAL: 0.00`

### Requirement: Invoices built in steps with a Builder
Invoices SHALL be assembled through a Builder abstraction in distinct steps (header, each applicable section, result) rather than through constructors with numerous optional parameters.

#### Scenario: Builder steps
- **WHEN** a user invoice is generated
- **THEN** it is produced by calling the builder's header step, one section step per applicable section, and the result step

### Requirement: Invoices are not persisted
Invoices SHALL be generated on demand from current data and SHALL NOT be stored.

#### Scenario: No invoice file
- **WHEN** an invoice is generated
- **THEN** no data file is created or modified

### Requirement: Invoice commands in HELP
`HELP` SHALL list `INVOICE CREATE;USER;Correo` and `INVOICE CREATE;GROUP;IdGrupo`.

#### Scenario: Help shows invoice commands
- **WHEN** `HELP` is processed
- **THEN** the output contains both syntaxes
