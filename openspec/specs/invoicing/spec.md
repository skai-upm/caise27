# invoicing Specification

## Purpose

Produces on-demand invoices for users and groups from the tickets they bought, listing each item's price and a total without counting any purchase twice.

## Requirements

### Requirement: Invoice command
The command `INVOICE CREATE;USER;<correo>` SHALL print the invoice of the registered user with that email and `INVOICE CREATE;GROUP;<identificadorGrupo>` the invoice of the existing group with that identifier. An unknown recipient type, user or group SHALL be reported with an error. Invoices SHALL NOT be persisted.

#### Scenario: User invoice
- **WHEN** user Ana exists and `INVOICE CREATE;USER;ana@correo.com` is received
- **THEN** Ana's invoice is printed

#### Scenario: Unknown recipient
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com`, `INVOICE CREATE;GROUP;G99` or `INVOICE CREATE;COMPANY;X` is received
- **THEN** an error is printed

#### Scenario: Not persisted
- **WHEN** an invoice is generated
- **THEN** no persistence file is created or modified

### Requirement: Invoice content
Every invoice SHALL show the recipient, the issue date (the system date), one section per applicable kind of item that has at least one item, each item with its price, and the total equal to the sum of the item prices formatted with two decimals. A recipient without items SHALL get an invoice with no sections and total `0.00`.

#### Scenario: Prices and total
- **WHEN** Ana bought tickets `T001` at 40.00 and `T002` at 60.00
- **THEN** her invoice lists both tickets with their prices and shows total `100.00`

#### Scenario: Nothing bought
- **WHEN** Pedro bought nothing and his invoice is generated
- **THEN** it shows no sections and total `0.00`

### Requirement: User invoice items
A user invoice SHALL include the individual tickets bought directly for that user and SHALL NOT include the internal tickets generated for the user by a group ticket.

#### Scenario: Group ticket not counted for the member
- **WHEN** Ana has individual ticket `T001` at 40.00 and is covered by internal ticket `TG01-1` of group ticket `TG01`
- **THEN** Ana's invoice lists only `T001` and its total is `40.00`

### Requirement: Group invoice items
A group invoice SHALL include the group tickets bought for that group, each with its discounted total, and SHALL NOT list their internal tickets nor any individual ticket of the members.

#### Scenario: Group invoice
- **WHEN** group `G01` bought `TG01` (80.00) and `TG02` (108.00) and member Ana also has individual ticket `T001`
- **THEN** the invoice of `G01` lists `TG01` and `TG02` only and its total is `188.00`

### Requirement: Step-wise invoice construction
Invoices SHALL be assembled by a builder in distinct steps (header, then each applicable section, then the result), with one builder per recipient type implementing only its applicable steps; invoice variants SHALL NOT be handled with constructors taking many optional parameters.

#### Scenario: Builder steps
- **WHEN** a group invoice is built
- **THEN** the header and group-ticket steps contribute content and the individual-ticket step contributes nothing
