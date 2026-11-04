# invoices Specification

## Purpose
Generates on-demand invoices for users and groups from the purchases recorded in the system, assembled step by step so that each invoice contains only the sections applicable to its recipient.

## Requirements

### Requirement: Invoice commands
`INVOICE CREATE;USER;<correo>` SHALL generate an invoice for the registered user with that email (case-insensitive), and `INVOICE CREATE;GROUP;<identificadorGrupo>` SHALL generate an invoice for the existing group with that identifier. An unknown user or group, an unknown recipient type, or a wrong parameter count SHALL be rejected with an `ERROR` message and no invoice.

#### Scenario: Unknown recipient
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com`, `INVOICE CREATE;GROUP;G99` or `INVOICE CREATE;COMPANY;X` is processed
- **THEN** an `ERROR` message is produced and no invoice is shown

#### Scenario: Wrong parameter count
- **WHEN** `INVOICE CREATE;USER` is processed
- **THEN** an `ERROR` message is produced

### Requirement: Invoice content and total
An invoice SHALL identify its recipient, list each included item with its price, and show a total equal to the sum of the included item prices, with amounts shown with two decimals. Sections with no applicable items SHALL be omitted; an invoice with no items SHALL show only the recipient and a total of `0.00`.

#### Scenario: Empty sections omitted
- **GIVEN** a user with no tickets
- **WHEN** `INVOICE CREATE;USER;<correo>` is processed
- **THEN** the invoice shows the user, no ticket section, and `TOTAL: 0.00`

### Requirement: User invoice
A user invoice SHALL include the individual tickets purchased directly for that user, each at the price captured at purchase. Constituent individual tickets of group tickets SHALL NOT be counted as individual purchases.

#### Scenario: No double counting
- **GIVEN** Ana bought `T001` at 40.00 and `T002` at 60.00, and is also a member covered by group ticket `TG01`
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice lists `T001` 40.00 and `T002` 60.00 once each, does not list any `TG01` ticket, and the total is 100.00

### Requirement: Group invoice
A group invoice SHALL include the group-ticket purchases of that group, each at its stored total, without adding the constituent individual tickets again.

#### Scenario: Group purchases
- **GIVEN** group `G01` with three members bought `TG01` (108.00) and later `TG02` (108.00)
- **WHEN** `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the invoice lists `TG01` and `TG02` with 108.00 each, no individual tickets, and the total is 216.00

#### Scenario: Members' own tickets not included
- **GIVEN** a member of `G01` also bought an individual ticket `T001`
- **WHEN** the group invoice is generated
- **THEN** `T001` does not appear on it

### Requirement: Builder-based construction
Invoices SHALL be constructed through a common builder abstraction whose distinct steps add the header and each kind of section, with one concrete builder per recipient type and a director that runs the steps; invoice variants SHALL NOT be handled with constructors taking numerous optional parameters. Invoices SHALL be generated on demand and SHALL NOT be persisted.

#### Scenario: Same steps for both recipients
- **WHEN** a user invoice and a group invoice are built
- **THEN** both are produced by the same director running the same builder steps, each concrete builder contributing only its applicable sections

#### Scenario: Not persisted
- **WHEN** an invoice is generated
- **THEN** no data file changes
