# Spec Delta

## Purpose

Defines invoices generated on demand for users and groups from the park's economic information, their sections and totals, and the rule that each purchase is counted once.

## ADDED Requirements

### Requirement: User invoice
The command `INVOICE CREATE;USER;<email>` SHALL produce, for an existing user, an invoice with the user's identifying information, each individual ticket purchased directly for that user with its price, and the total of the items shown. Individual tickets belonging to a group ticket composition SHALL NOT be included as individual purchases. An unknown user SHALL be rejected with an error message.

#### Scenario: User with direct tickets
- **WHEN** `ana@correo.com` has individual tickets `T001` (40.00) and `T002` (45.50) and `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice shows the user's email and DNI, both tickets with their prices, and a total of 85.50

#### Scenario: Group-ticket components not counted on the user invoice
- **WHEN** `ana@correo.com` is only in the composition of group ticket `TG01`
- **THEN** her invoice shows no ticket section and a total of 0.00

#### Scenario: Unknown user
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` is processed and no such user exists
- **THEN** an error message is shown

### Requirement: Group invoice
The command `INVOICE CREATE;GROUP;<groupIdentifier>` SHALL produce, for an existing group, an invoice with the group identifier and name, each group ticket purchased by the group with its composition and total price, and the total of the group tickets. Composed individual tickets SHALL NOT be added separately to the total, and members' personal purchases SHALL NOT be added. An unknown group SHALL be rejected with an error message.

#### Scenario: Group with a group ticket
- **WHEN** group `G01` (6 members) bought `TG02` at 192.00 and `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the invoice shows `G01` and its name, `TG02` with its six individual tickets and 192.00, and a total of 192.00

#### Scenario: Members' own tickets excluded
- **WHEN** a member of `G01` also has an individual ticket of their own
- **THEN** that ticket does not appear on the group invoice

### Requirement: Invoices built with a Builder
Invoices SHALL be constructed step by step through a common builder abstraction, applying only the construction steps relevant to the recipient type and the available information; sections without information SHALL be omitted rather than shown empty, and invoices SHALL NOT be persisted.

#### Scenario: Empty sections omitted
- **WHEN** a group has bought no group tickets and its invoice is requested
- **THEN** the invoice shows the group data and a total of 0.00 without an empty group-ticket section

#### Scenario: Wrong invoice target
- **WHEN** `INVOICE CREATE;COMPANY;X` or `INVOICE CREATE;USER` is processed
- **THEN** an error message is shown
