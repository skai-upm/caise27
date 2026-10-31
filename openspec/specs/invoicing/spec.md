# invoicing Specification

## Purpose

Produces on-demand invoices for users and groups from the purchases recorded in the system, assembled step by step so that only applicable sections appear.

## Requirements

### Requirement: Invoice commands
`INVOICE CREATE;USER;<correo>` SHALL print an invoice for the registered user with that email and `INVOICE CREATE;GROUP;<idGrupo>` SHALL print an invoice for that group. An unknown user or group, or a recipient type other than `USER` or `GROUP`, SHALL be rejected. Invoices SHALL NOT be persisted.

#### Scenario: Unknown recipient
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` or `INVOICE CREATE;GROUP;G99` or `INVOICE CREATE;OTHER;x` is executed
- **THEN** an error is reported and no invoice is printed

#### Scenario: Not persisted
- **WHEN** an invoice is generated
- **THEN** no data file is created or changed

### Requirement: Builder-based assembly
Invoices SHALL be constructed through a Builder abstraction in distinct steps (recipient, each applicable section, total) rather than through constructors with many optional parameters. A section with no items SHALL be omitted.

#### Scenario: Empty section omitted
- **WHEN** a user has individual tickets but no personal passes and requests an invoice
- **THEN** the invoice contains the individual-ticket section and no personal-pass section

### Requirement: User invoice contents
A user invoice SHALL list the individual tickets bought directly for that user and the personal passes attached to any of the user's individual tickets (including constituents of group tickets), each with its price, and their total. Constituent tickets of group tickets SHALL NOT be listed as individual purchases.

#### Scenario: No double counting
- **WHEN** Ana bought `T001` (40.00), is a member of group ticket `TG01` with constituent `TG01-1` (36.00) and bought one SpeedPass (7.20) for `TG01-1`
- **THEN** her invoice lists `T001` 40.00 and the SpeedPass 7.20, does not list `TG01-1`, and totals 47.20

### Requirement: Group invoice contents
A group invoice SHALL list the group's group-ticket purchases with their prices and their total, and SHALL NOT list constituent tickets or members' personal passes.

#### Scenario: Group invoice total
- **WHEN** group `G01` bought `TG01` for 108.00 and `TG02` for 108.00 and members hold personal passes
- **THEN** the group invoice lists `TG01` and `TG02` and totals 216.00 without any pass
