# invoices Specification

## Purpose
Produces on-demand invoices for users and groups from the purchases recorded in the system, built step by step so that each recipient type only shows the applicable sections and no purchase is counted twice.

## Requirements

### Requirement: Invoice commands
The system SHALL process `INVOICE CREATE;USER;<correo>` for an existing user and `INVOICE CREATE;GROUP;<identificadorGrupo>` for an existing group, answering with an `OK:` response that contains the invoice; a nonexistent recipient, an unknown recipient type or a wrong number of parameters SHALL be rejected with an `ERROR:` response and no invoice.

#### Scenario: Unknown user
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` is processed and no such user exists
- **THEN** an `ERROR:` response is printed

#### Scenario: Unknown group or type
- **WHEN** `INVOICE CREATE;GROUP;G99` (no such group), `INVOICE CREATE;CLIENT;ana@correo.com` or `INVOICE CREATE;USER` is processed
- **THEN** an `ERROR:` response is printed

### Requirement: User invoice contents
A user invoice SHALL identify the user and list the individual tickets purchased directly for that user, each with its identifier, date and price, followed by the total; individual tickets that are constituents of a group ticket SHALL NOT be listed or charged.

#### Scenario: Direct ticket charged once, constituent not charged
- **GIVEN** Ana bought `T001` at 40.00 and is a member of `G01`, which bought `TG01` (constituent `TG01-1` for Ana)
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice lists `T001` with 40.00, does not list `TG01-1`, and the total is 40.00

#### Scenario: Several tickets
- **GIVEN** Ana bought `T001` at 40.00 and, after `SYSTEM PRICE;60.00`, `T002` at 60.00
- **WHEN** her invoice is created
- **THEN** both tickets are listed with their own prices and the total is 100.00

### Requirement: Group invoice contents
A group invoice SHALL identify the group and list the group tickets purchased by that group, each with its identifier, date, number of members and total amount, followed by the overall total; the constituent individual tickets SHALL NOT be added again.

#### Scenario: Group ticket amount only
- **GIVEN** `G01` with 3 members bought `TG01` for 108.00
- **WHEN** `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the invoice lists `TG01` with 108.00 and the total is 108.00 (not 108.00 + 3 × 40.00)

### Requirement: Applicable sections only
An invoice SHALL include only the sections that apply to its recipient type and have data: a user invoice has no group tickets section, a group invoice has no individual tickets section, and a section without items is omitted; the total SHALL always be shown.

#### Scenario: Recipient without purchases
- **GIVEN** user Pedro has bought no individual tickets
- **WHEN** `INVOICE CREATE;USER;pedro@correo.com` is processed
- **THEN** the invoice shows Pedro, no tickets section, and total 0.00

### Requirement: Builder-based construction
Invoices SHALL be constructed through a common builder abstraction whose distinct steps (recipient, individual tickets, group tickets, total) are invoked in order by a director, with one concrete builder per recipient type; invoice variants SHALL NOT be created through constructors with many optional parameters.

#### Scenario: Same director for both types
- **WHEN** a user invoice and a group invoice are generated
- **THEN** both are produced by the same director running the same sequence of steps on different builders

### Requirement: On-demand, not persisted
Invoices SHALL be generated from the current data each time they are requested and SHALL NOT be stored.

#### Scenario: Reflects the latest purchases
- **GIVEN** Ana's invoice was created, then she bought another ticket
- **WHEN** her invoice is created again
- **THEN** it includes the new ticket, and no invoice file exists in the data directory

### Requirement: Invoice commands in HELP
The `HELP` response SHALL include `INVOICE CREATE;USER;Correo` and `INVOICE CREATE;GROUP;Identificador`.

#### Scenario: Help shows invoice commands
- **WHEN** `HELP` is processed
- **THEN** the response contains both lines
