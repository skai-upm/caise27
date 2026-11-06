# Spec Delta

## Purpose

Produces on-demand invoices for Amusement Park users and groups from the purchases recorded in the system, built step by step with only the sections that apply.

## ADDED Requirements

### Requirement: User invoice
`INVOICE CREATE;USER;<email>` SHALL print an invoice for the active user with that email containing the user's identifying information (name, surnames, DNI, email), a section with every individual ticket purchased directly for the user with its identifier, date and price, and the total of the items shown. Individual tickets that belong to a group ticket's composition MUST NOT appear as individual purchases. An email of no active user MUST be rejected with an error.

#### Scenario: Direct tickets only
- **GIVEN** Ana bought individual tickets `T001` (40.00) and `T002` (45.50) and also has constituent ticket `TG01:12345678Z` of a group ticket
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice shows Ana's identification, `T001` and `T002` with their prices, does not list `TG01:12345678Z`, and the total is `85.50`

#### Scenario: Unknown or deleted user
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` is processed
- **THEN** an error reporting that no user has that email is printed

### Requirement: Group invoice
`INVOICE CREATE;GROUP;<group>` SHALL print an invoice for the existing group containing its identifier and name, a section with each group ticket purchased by the group with its identifier, date and total price, the composition of each group ticket as non-priced detail, and the total of the group tickets. Constituent tickets MUST NOT be added separately to the total, and members' personal passes MUST NOT be added. An unknown group MUST be rejected with an error.

#### Scenario: Group tickets without double counting
- **GIVEN** group `G01` bought `TG01` with 3 members at 40.00 base price (total 108.00)
- **WHEN** `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the invoice shows `G01`, its name, `TG01` with `108.00`, the three constituent tickets as detail, and the total `108.00`

#### Scenario: Unknown group
- **WHEN** `INVOICE CREATE;GROUP;G99` is processed
- **THEN** an error reporting that there is no group `G99` is printed

### Requirement: Only relevant sections
Each invoice SHALL contain only the sections relevant to its recipient type and to the available information; a section without items MUST be omitted rather than printed empty, and the total MUST equal the sum of the priced items shown.

#### Scenario: User without purchases
- **GIVEN** user Pedro has no direct tickets and no passes
- **WHEN** `INVOICE CREATE;USER;pedro@correo.com` is processed
- **THEN** the invoice shows Pedro's identification and a total of `0.00`, with no tickets or passes section

### Requirement: Builder-based construction
Invoices SHALL be constructed through a common builder abstraction by clearly separated steps (recipient, sections, priced items, non-priced details, result), so each invoice type uses only the steps it needs, without constructors with many optional parameters or placeholder values. Invoices are generated from the current system state on demand and are not persisted.

#### Scenario: Conditional steps
- **WHEN** a user invoice and a group invoice are built
- **THEN** both are produced by the same builder abstraction, the user invoice never contains a composition section and the group invoice never contains a pass section

### Requirement: Invoice command format
`INVOICE CREATE` SHALL take exactly two fields, the recipient type `USER` or `GROUP` and its identifier; any other type MUST be rejected with an error.

#### Scenario: Unknown recipient type
- **WHEN** `INVOICE CREATE;COMPANY;X` is processed
- **THEN** an error reporting an unknown invoice type is printed
