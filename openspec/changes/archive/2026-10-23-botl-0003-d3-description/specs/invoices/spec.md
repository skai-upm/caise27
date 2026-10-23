# Spec Delta

## Purpose

Defines on-demand invoices for users and groups, built from the stored park state, showing each charged item with its amount and a total, and only the sections that apply.

## ADDED Requirements

### Requirement: User invoice
`INVOICE CREATE;USER;<email>` SHALL, for an active user, print an invoice with the user's identifying data (name, DNI, email), the standalone individual tickets bought for the user, the SpeedPasses and Special Passes attached to any of the user's individual tickets (standalone or composing a group ticket), the price of each item, and the total of the items shown; tickets composing group tickets SHALL NOT be charged on the user's invoice.

#### Scenario: Ana's invoice
- **WHEN** Ana has ticket T001 (40.00), ten SpeedPasses on T001 (8.00 each), Special Pass PE01 (40.00), a SpeedPass on `TG01:12345678Z` (7.20), and `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice lists T001, the eleven SpeedPasses and PE01 with their prices and a total of 167.20, and does not charge `TG01:12345678Z`

#### Scenario: Unknown user
- **WHEN** `INVOICE CREATE;USER;nobody@correo.com` is processed
- **THEN** an error is printed

### Requirement: Group invoice
`INVOICE CREATE;GROUP;<groupIdentifier>` SHALL, for an existing group, print an invoice with the group identifier and name, each group ticket bought by the group with its date, composition and total price, and the total of the group tickets; composing tickets and members' passes SHALL NOT be added to the total.

#### Scenario: G01's invoice
- **WHEN** G01 bought TG01 (3 members, 108.00) and members bought SpeedPasses, and `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the invoice lists TG01 with its three composing tickets, a TG01 price of 108.00 and a total of 108.00

#### Scenario: Unknown group
- **WHEN** `INVOICE CREATE;GROUP;G99` is processed
- **THEN** an error is printed

### Requirement: Only applicable sections
An invoice SHALL omit any section that has no items instead of showing it empty; a recipient with no purchases SHALL get an invoice with its identifying data and a total of 0.00.

#### Scenario: User without passes
- **WHEN** a user with only standalone tickets requests an invoice
- **THEN** the invoice contains no SpeedPass or Special Pass section

#### Scenario: Invalid invoice target
- **WHEN** `INVOICE CREATE;SHOP;X` is processed
- **THEN** an error states that the target must be USER or GROUP
