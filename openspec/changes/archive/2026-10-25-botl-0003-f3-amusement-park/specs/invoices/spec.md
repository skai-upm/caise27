# Spec Delta

## Purpose

Generates on-demand invoices for users and groups from the purchase information in the system using a Builder.

## ADDED Requirements

### Requirement: Builder-based invoice construction
Invoices SHALL be assembled in distinct steps through a builder abstraction (header, then each applicable section, then the result), directed according to the recipient type. Invoice variants SHALL NOT be handled by constructors with numerous optional parameters.

#### Scenario: Assembled in steps
- **WHEN** a user invoice is generated
- **THEN** the builder receives a header step, one step per applicable section and a final build step

### Requirement: User invoice
`INVOICE CREATE;USER;<correo>` SHALL generate, for an existing user, an invoice listing the individual tickets bought directly for that user and the personal passes attached to any of the user's tickets (including internal tickets of group tickets). Internal tickets of a group ticket SHALL NOT be listed as individual purchases.

#### Scenario: Tickets and passes
- **WHEN** Ana bought T001 (40.00) and a SpeedPass for it (8.00), holds internal ticket TG01-12345678Z with a Special Pass (40.00), and `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice lists T001 at 40.00, the SpeedPass at 8.00 and the Special Pass at 40.00, does not list TG01-12345678Z as a ticket, and shows a total of 88.00

#### Scenario: Unknown user
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` is processed
- **THEN** an `ERROR` is reported

### Requirement: Group invoice
`INVOICE CREATE;GROUP;<idGrupo>` SHALL generate, for an existing group, an invoice listing its group tickets with their total amounts, without listing the internal individual tickets or the members' personal passes.

#### Scenario: Group tickets only
- **WHEN** G01 bought TG01 for 108.00 and a member bought a SpeedPass for an internal ticket, and `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the invoice lists TG01 at 108.00, no individual tickets or passes, and a total of 108.00

### Requirement: Only applicable sections
An invoice SHALL include only sections that apply to its recipient type and have data; each listed item SHALL show its price and the invoice SHALL show the total. Invoices SHALL NOT be persisted.

#### Scenario: No passes
- **WHEN** a user has tickets but no passes
- **THEN** the invoice has no passes section

#### Scenario: Invalid recipient type
- **WHEN** `INVOICE CREATE;COMPANY;X` is processed
- **THEN** an `ERROR` is reported
