# Spec Delta

## Purpose

Defines on-demand invoices that summarise the economic information of a user or a group, including only the sections that apply to the invoiced party.

## ADDED Requirements

### Requirement: User invoice
`INVOICE CREATE;USER;<email>` SHALL, for an existing active user, display an invoice with the user's identifying data, the individual tickets bought directly for the user, the SpeedPasses and the Special Passes of any of the user's individual tickets (including those inside group tickets), the price of each item and the total of the items shown. Individual tickets inside group tickets SHALL NOT be listed as purchases.

#### Scenario: Ana's invoice
- **WHEN** Ana has ticket T001 (40.00), ten SpeedPasses on T001 (8.00 each), Special Pass PE01 (40.00), one SpeedPass on `TG01:12345678Z` (7.20) and belongs to group ticket TG01, and `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice lists T001, the eleven SpeedPasses and PE01, does not list TG01 or `TG01:12345678Z` as a ticket purchase, and shows a total of 167.20

#### Scenario: Unknown user
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` is processed
- **THEN** an error is printed

### Requirement: Group invoice
`INVOICE CREATE;GROUP;<groupIdentifier>` SHALL, for an existing group, display an invoice with the group identifier and name, each group ticket bought by the group with its composition and total price, and the total of the group tickets. Component tickets SHALL NOT be added separately to the total and members' passes SHALL NOT be included.

#### Scenario: G01 invoice
- **WHEN** G01 bought TG01 for 3 members at 108.00 and members hold SpeedPasses on its individual tickets, and `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the invoice shows G01, Familia Garcia, TG01 with its three individual tickets, and a total of 108.00

#### Scenario: Unknown group
- **WHEN** `INVOICE CREATE;GROUP;G99` is processed
- **THEN** an error is printed

#### Scenario: Unknown invoice type
- **WHEN** `INVOICE CREATE;COMPANY;X` is processed
- **THEN** an error is printed

### Requirement: Invoices omit empty sections
An invoice SHALL include only the sections that have content for the invoiced party; sections without items SHALL be omitted rather than shown empty.

#### Scenario: User without passes
- **WHEN** a user with one ticket and no passes requests an invoice
- **THEN** the invoice contains no SpeedPass or Special Pass section
