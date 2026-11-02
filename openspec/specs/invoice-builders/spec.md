# invoice-builders Specification

## Purpose

Defines on-demand user and group invoices assembled with the Builder pattern from the current park state, showing only applicable sections (F3-C02).

## Requirements

### Requirement: Builder-based invoice construction
Invoices SHALL be constructed through a common builder abstraction in clearly separated steps that add only the sections applicable to the recipient and the available information; sections with no items SHALL be omitted. Invoices SHALL be generated on demand from the current state and SHALL NOT be persisted.

#### Scenario: Empty sections omitted
- **WHEN** an invoice is requested for a user with tickets but no passes
- **THEN** the invoice contains no pass sections

### Requirement: User invoice
`INVOICE CREATE;USER;<email>` SHALL, for an existing active user, produce an invoice with the user's identifying information, the individual tickets purchased directly for that user, the SpeedPasses and Special Passes associated with any of the user's individual tickets (including tickets that compose group tickets), the price of each item and the total of the items shown. Individual tickets composing a group ticket SHALL NOT be listed or counted as direct purchases. An unknown user SHALL be reported as an error.

#### Scenario: Direct ticket, group-derived ticket and passes
- **WHEN** Ana has direct ticket T001 (40.00), a constituent ticket of group ticket TG01, one SpeedPass on T001 (8.00) and one Special Pass on her TG01 constituent (36.00)
- **THEN** her invoice lists T001, the SpeedPass and the Special Pass, does not list the TG01 constituent as a ticket, and totals 84.00

#### Scenario: Unknown user
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` is processed
- **THEN** an error is reported

### Requirement: Group invoice
`INVOICE CREATE;GROUP;<groupIdentifier>` SHALL, for an existing group, produce an invoice with the group identifier and name, each group ticket purchased by the group with its total price and its composition, and the total of the group tickets. Constituent tickets SHALL NOT be added separately to the total and members' passes SHALL NOT be included in it. An unknown group SHALL be reported as an error.

#### Scenario: Group total counted once
- **WHEN** group G01 with 3 members bought TG01 at base price 40.00 and members hold passes
- **THEN** the group invoice shows TG01 at 108.00 with its three constituents as details and a total of 108.00
