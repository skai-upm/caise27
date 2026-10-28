# invoices Specification

## Purpose
Defines invoices generated on demand from the system state for users and groups: which sections and items each contains, how totals are computed, and that items already paid in a group ticket are not charged twice.

## Requirements

### Requirement: User invoice
`INVOICE CREATE;USER;<email>` SHALL print an invoice for the active user with that email, containing the user's identifying information (name, surnames, DNI, email), a section with each individual ticket bought directly for the user (identifier, date and price) and the total of the items shown. Individual tickets that belong to a group ticket composition SHALL NOT be charged on the user invoice. An email of no active user SHALL be rejected with an `ERROR:` message.

#### Scenario: User with direct tickets
- **WHEN** Ana has tickets `T001` (40.00) and `T002` (45.50) and `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice identifies Ana, lists `T001` with 40.00 and `T002` with 45.50, and shows a total of 85.50

#### Scenario: Group ticket items not charged to the user
- **WHEN** Ana's only tickets are part of group ticket `TG01`
- **THEN** her invoice shows her identification, no ticket section, and a total of 0.00

#### Scenario: Unknown user
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` is processed and no active user has that email
- **THEN** an `ERROR:` line is printed

### Requirement: Group invoice
`INVOICE CREATE;GROUP;<groupIdentifier>` SHALL print an invoice for the existing group containing its identifier and name, a section with each group ticket bought by the group (identifier, date, total price) and, under each, its composition (individual ticket identifiers and users) as description only, and the total of the group tickets. Composition tickets and members' personal passes SHALL NOT be added to the group total. An unknown group SHALL be rejected with an `ERROR:` message.

#### Scenario: Group with a group ticket
- **WHEN** `G01` bought `TG01` for Ana and Pedro at 40.00 each and `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the invoice shows `G01` and its name, `TG01` with 80.00, its composition `TG01:12345678Z` and `TG01:87654321X`, and a total of 80.00

### Requirement: Only relevant sections
An invoice SHALL contain only sections that apply to its recipient type and have information; empty sections SHALL be omitted rather than shown empty.

#### Scenario: Group without tickets
- **WHEN** `G02` has bought nothing and `INVOICE CREATE;GROUP;G02` is processed
- **THEN** the invoice shows the group identification and a total of 0.00 and no group ticket section

### Requirement: Invoice type syntax
The second element of `INVOICE CREATE` SHALL be `USER` or `GROUP`; any other value SHALL be rejected with an `ERROR:` message.

#### Scenario: Unknown invoice type
- **WHEN** `INVOICE CREATE;PARK;X` is processed
- **THEN** an `ERROR:` line is printed

### Requirement: Passes on user invoices
A user invoice SHALL include a SpeedPasses section and a Special Passes section listing the passes associated with the user's individual tickets — including tickets that belong to a group ticket composition — each with its price, and SHALL add them to the total. Group invoices SHALL NOT include members' passes. Sections without passes SHALL be omitted.

#### Scenario: Passes on a group composition ticket
- **WHEN** Ana's only ticket is `TG01:12345678Z` (36.00) and she bought SpeedPass `SP01:1` (7.20) for it
- **THEN** her invoice shows no ticket section, a SpeedPasses section with `SP01:1` at 7.20 and a total of 7.20, and the group invoice total does not include it
