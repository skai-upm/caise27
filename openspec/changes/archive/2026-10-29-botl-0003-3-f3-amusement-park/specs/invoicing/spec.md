# Spec Delta

## Purpose

Defines user and group invoices generated on demand from the stored economic information and constructed step by step with the Builder pattern.

## ADDED Requirements

### Requirement: Builder-based construction
Invoices SHALL be built with the Builder pattern through a common builder abstraction and clearly differentiated construction steps; a single constructor with many optional parameters MUST NOT be used. Invoices are generated on demand and not persisted.

#### Scenario: Only relevant sections
- **WHEN** an invoice is built for a user with tickets but no passes
- **THEN** the invoice contains no pass sections

### Requirement: User invoice
`INVOICE CREATE;USER;<email>` SHALL produce, for an active user, an invoice with the user's identifying data, the individual tickets bought directly for the user, the SpeedPasses and Special Passes of any of the user's individual tickets (including composition tickets), each item's price and the total of the items shown. Composition tickets MUST NOT be listed as individual purchases. Empty sections MUST be omitted. Unknown or inactive users MUST be rejected.

#### Scenario: Tickets and passes
- **WHEN** Ana has ticket T001 (40.00), a SpeedPass on it (8.00) and a Special Pass on her group composition ticket TG01:12345678Z (32.00)
- **THEN** her invoice lists T001, the SpeedPass and the Special Pass with total 80.00 and does not list TG01:12345678Z as a ticket

#### Scenario: Unknown user
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` is processed
- **THEN** an error is shown

### Requirement: Group invoice
`INVOICE CREATE;GROUP;<groupIdentifier>` SHALL produce, for an existing group, an invoice with the group identifier and name, each group ticket bought by the group with its composition and total price, and the total of those group tickets. Composition tickets MUST NOT be added separately and members' passes MUST NOT be added to the total. Unknown groups MUST be rejected.

#### Scenario: Group tickets
- **WHEN** G01 bought TG01 (2 members, 80.00) and a member bought a SpeedPass on a composition ticket
- **THEN** the group invoice shows TG01 with its two composition tickets and total 80.00

#### Scenario: Invalid request
- **WHEN** `INVOICE CREATE;GROUP;G99` or `INVOICE CREATE;COMPANY;X` is processed
- **THEN** an error is shown
