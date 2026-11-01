# Spec Delta

## Purpose

Produces on-demand invoices for users and groups from the purchases recorded in the system, assembled step by step with a Builder.

## ADDED Requirements

### Requirement: Invoice commands
`INVOICE CREATE;USER;<correo>` SHALL print the invoice of the registered user with that email and `INVOICE CREATE;GROUP;<identificadorGrupo>` the invoice of that existing group. An unknown recipient type, user or group SHALL be rejected. Invoices SHALL NOT be persisted.

#### Scenario: Unknown recipient
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` or `INVOICE CREATE;CLIENT;x` is executed
- **THEN** the command is rejected

### Requirement: Builder-based assembly
Invoices SHALL be constructed through a Builder abstraction in distinct steps (recipient, each applicable section, total) without constructors taking numerous optional parameters, and SHALL include only sections that have items.

#### Scenario: Empty sections omitted
- **GIVEN** a registered user with no purchases
- **WHEN** the user invoice is created
- **THEN** it shows the recipient and a total of 0.00 and no item sections

### Requirement: User invoice contents
A user invoice SHALL list the individual tickets bought directly for the user and the personal passes attached to any of the user's tickets (including tickets owned by group tickets), each with its price, and the total. Tickets owned by a group ticket SHALL NOT be listed as individual purchases.

#### Scenario: No double counting of group-owned tickets
- **GIVEN** user Ana with ticket `T001` at 40.00, a constituent ticket of group ticket `TG01`, and a SpeedPass at 8.00 on that constituent ticket
- **WHEN** `INVOICE CREATE;USER;ana@correo.com` is executed
- **THEN** the invoice lists `T001` (40.00) and the SpeedPass (8.00) with total 48.00, and does not list the constituent ticket

### Requirement: Group invoice contents
A group invoice SHALL list the group tickets bought for the group with their prices and the total, without listing their constituent tickets or members' personal passes.

#### Scenario: Group invoice total
- **GIVEN** group `G01` with group tickets at 108.00 and 80.00
- **WHEN** `INVOICE CREATE;GROUP;G01` is executed
- **THEN** both group tickets are listed and the total is 188.00
