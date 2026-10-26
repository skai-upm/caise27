# invoicing Specification

## Purpose

Defines on-demand invoices for users and groups: which purchases each recipient type includes, how invoices are assembled step by step with a Builder, which sections appear, how prices and totals are shown, and that invoices are not stored.

## Requirements

### Requirement: User invoice
The command `INVOICE CREATE;USER;<correo>` SHALL produce an invoice for the registered user with that email listing the individual tickets bought directly for that user (identifier, date and price). Individual tickets that belong to a group ticket SHALL NOT be listed as individual purchases. Personal passes attached to any of the user's tickets, including internal tickets of group tickets, MAY appear as their own items. An unknown email SHALL be rejected with an `ERROR` line.

#### Scenario: Direct tickets listed
- **WHEN** Ana bought `T001` (40.00) and `T002` (60.00) and `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice lists `T001` and `T002` with their prices and a total of 100.00

#### Scenario: Group ticket members not double counted
- **WHEN** Ana is covered by group ticket `TG01` through internal ticket `TG01-1` and has no direct tickets
- **THEN** her invoice lists no individual tickets and its total is 0.00

#### Scenario: Unknown user
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` is processed
- **THEN** an `ERROR` line is printed

### Requirement: Group invoice
The command `INVOICE CREATE;GROUP;<identificadorGrupo>` SHALL produce an invoice for the existing group listing its group ticket purchases (identifier, date, number of members covered and total price of each), without listing the internal individual tickets or the members' personal passes again. An unknown group SHALL be rejected with an `ERROR` line.

#### Scenario: Group tickets listed
- **WHEN** group `G01` bought `TG01` (108.00) and `TG02` (80.00) and `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the invoice lists `TG01` and `TG02` with their totals and an invoice total of 188.00, and no individual ticket lines

#### Scenario: Unknown group
- **WHEN** `INVOICE CREATE;GROUP;G99` is processed and `G99` does not exist
- **THEN** an `ERROR` line is printed

#### Scenario: Invalid recipient type
- **WHEN** `INVOICE CREATE;COMPANY;X` is processed
- **THEN** an `ERROR` line states that the recipient type must be `USER` or `GROUP`

### Requirement: Step-by-step assembly with a Builder
Invoices SHALL be assembled through a Builder abstraction in distinct steps — setting the recipient, adding each applicable section with its items, and building the final invoice with its total — and invoice variants SHALL NOT be handled with constructors taking numerous optional parameters.

#### Scenario: Builder used for both recipient types
- **WHEN** a user invoice and a group invoice are produced
- **THEN** both are built by the same Builder abstraction with different sequences of steps

### Requirement: Applicable sections only
An invoice SHALL include only the sections that apply to its recipient type and for which data exists; an empty section SHALL NOT appear.

#### Scenario: User without purchases
- **WHEN** a registered user with no tickets requests an invoice
- **THEN** the invoice shows the recipient and a total of 0.00 with no sections

### Requirement: Prices and total
Every invoice SHALL show the price of each included item with two decimals and a total equal to the sum of the included items. The output SHALL start with an `OK` line identifying the invoice.

#### Scenario: Output format
- **WHEN** Ana's invoice with `T001` at 40.00 is printed
- **THEN** it starts with `OK INVOICE USER ana@correo.com`, contains a line for `T001` showing `40.00`, and ends with `Total: 40.00`

### Requirement: Invoices not persisted
Invoices SHALL be generated on demand from the current data and SHALL NOT be stored.

#### Scenario: No invoice file
- **WHEN** an invoice is generated
- **THEN** no data file is created or modified
