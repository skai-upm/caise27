# Spec Delta

## Purpose

Produces on-demand invoices for users and groups from the purchases recorded in the system, built step by step so each invoice contains only the sections that apply to its recipient and never charges an item twice.

## ADDED Requirements

### Requirement: Invoice commands
`INVOICE CREATE;USER;<correo>` SHALL produce an invoice for the registered user with that email (case-insensitive) and `INVOICE CREATE;GROUP;<identificadorGrupo>` SHALL produce an invoice for the existing group with that identifier; an unknown user or group, a recipient type other than `USER` or `GROUP`, or a wrong number of parameters SHALL be rejected with an error and no invoice.

#### Scenario: Unknown recipient
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` or `INVOICE CREATE;GROUP;G99` is processed
- **THEN** an error is printed and no invoice is shown

#### Scenario: Malformed command
- **WHEN** `INVOICE CREATE;CLIENT;ana@correo.com`, `INVOICE CREATE;USER` or `INVOICE CREATE;USER;ana@correo.com;x` is processed
- **THEN** it is rejected with a usage error

### Requirement: Builder-based construction
Invoices SHALL be assembled through a common builder abstraction whose distinct construction steps (header, directly purchased individual tickets, group-ticket purchases) are invoked in order by a director, with one concrete builder per recipient type; invoice variants SHALL NOT be created through constructors with optional parameters.

#### Scenario: Same steps, different builders
- **WHEN** the director constructs a user invoice and a group invoice
- **THEN** it invokes the same sequence of steps on a user builder and on a group builder, and each builder contributes only the sections applicable to its recipient

### Requirement: User invoice contents
A user invoice SHALL list, with their identifier, date and price, the individual tickets bought directly for that user, and SHALL NOT list or charge the individual tickets that belong to a group ticket; its total SHALL be the sum of the listed prices.

#### Scenario: Direct ticket charged once, group constituent not charged
- **GIVEN** Ana bought `T001` at `40.00` and is a member included in group ticket `TG01`
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice lists `T001` with `40.00`, does not list `TG01` or its constituent ticket, and the total is `40.00`

### Requirement: Group invoice contents
A group invoice SHALL list, with their identifier, date, number of tickets, discount and amount, the group-ticket purchases of that group, and SHALL NOT add their constituent individual tickets; its total SHALL be the sum of the group-ticket amounts.

#### Scenario: Group-ticket amount without constituents
- **GIVEN** group `G01` bought `TG01` for 3 members at `40.00` (amount `108.00`) and a member also bought `T005` directly
- **WHEN** `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the invoice lists `TG01` with `108.00`, does not list constituent tickets or `T005`, and the total is `108.00`

### Requirement: Only applicable sections
An invoice SHALL omit every section for which the recipient has no data and SHALL always show the recipient and the total.

#### Scenario: Recipient without purchases
- **GIVEN** user Pedro has no directly bought tickets
- **WHEN** `INVOICE CREATE;USER;pedro@correo.com` is processed
- **THEN** the invoice shows Pedro and a total of `0.00` and contains no ticket section

### Requirement: Invoices are not persisted
Invoices SHALL be generated on demand from current data and SHALL NOT be stored or change any stored data.

#### Scenario: No data change
- **WHEN** an invoice is generated
- **THEN** no data file is created or modified
