# invoicing Specification

## Purpose

Defines how the application generates invoices on demand for users and groups from the park's persisted economic information, including only the items applicable to each recipient and never counting the same purchase twice.

## Requirements

### Requirement: User invoice
`INVOICE CREATE;USER;<email>` SHALL generate an invoice for the active user with that email showing the user's identifying information (name, surnames, DNI, email), each individual ticket purchased directly for the user with its price, and the total of the items shown. Individual tickets that belong to a group ticket SHALL NOT be included as individual purchases.

#### Scenario: Invoice with individual tickets
- **WHEN** Ana has tickets `T001` (40.00) and `T002` (45.50) and `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice shows Ana's identification, `T001` with `40.00`, `T002` with `45.50` and a total of `85.50`

#### Scenario: Group ticket items not repeated
- **WHEN** Ana's only tickets are `T001` (40.00) and the composed ticket `TG01:12345678Z` of a group ticket
- **THEN** the user invoice includes `T001` only and its total is `40.00`

#### Scenario: Unknown or deleted user
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` is processed and no active user has that email
- **THEN** an error is printed and no invoice is shown

### Requirement: Group invoice
`INVOICE CREATE;GROUP;<groupIdentifier>` SHALL generate an invoice for the existing group showing its identifier and name, each group ticket it purchased with its total price and composition (composed ticket identifiers and their users), and the total of the group ticket prices. Composed individual tickets and members' personal passes SHALL NOT be added to the total.

#### Scenario: Group invoice
- **WHEN** group `G01` (Familia Garcia) bought `TG01` for Ana and Pedro at 80.00 and `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the invoice shows `G01`, `Familia Garcia`, `TG01` with `80.00`, the composed tickets `TG01:12345678Z` and `TG01:87654321X`, and a total of `80.00`

#### Scenario: Unknown group
- **WHEN** `INVOICE CREATE;GROUP;G99` is processed and no such group exists
- **THEN** an error is printed

#### Scenario: Wrong invoice type or syntax
- **WHEN** `INVOICE CREATE;COMPANY;X` or `INVOICE CREATE;USER` is processed
- **THEN** an error is printed

### Requirement: Sections only when relevant
An invoice SHALL include only the sections that apply to its recipient and have information; sections without items SHALL be omitted rather than shown empty.

#### Scenario: User without tickets
- **WHEN** Pedro has no individual tickets and `INVOICE CREATE;USER;pedro@correo.com` is processed
- **THEN** the invoice shows Pedro's identification and a total of `0.00` and no tickets section

### Requirement: Builder-based construction
Invoices SHALL be built step by step through a common builder abstraction (with concrete builders for user and group invoices), not through a constructor with many optional parameters, and SHALL be generated on demand without being persisted.

#### Scenario: Builders share an abstraction
- **WHEN** the invoice classes are inspected
- **THEN** the user and group invoice builders extend a common builder type and the invoice is obtained from the builder after its construction steps
