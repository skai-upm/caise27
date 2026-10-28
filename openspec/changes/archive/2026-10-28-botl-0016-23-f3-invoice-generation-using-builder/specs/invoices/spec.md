# Spec Delta

## Purpose

Defines on-demand invoices for users and groups, built from the economic information stored in the system and containing only the sections relevant to the recipient.

## ADDED Requirements

### Requirement: User invoice
The system SHALL accept `INVOICE CREATE;USER;<email>` for an active user and display an invoice with the user's identifying information, a section with the individual tickets purchased directly for that user and the price of each, and the total of the items shown. Individual tickets that are part of a group ticket composition SHALL NOT be included as individual purchases. An unknown or deleted user SHALL be rejected with an error message.

#### Scenario: User with direct tickets
- **WHEN** user `ana@correo.com` has individual tickets `T001` (40.00) and `T002` (45.50) and `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice identifies the user, lists `T001` with 40.00 and `T002` with 45.50, and shows a total of 85.50

#### Scenario: Group ticket items not counted again
- **WHEN** `ana@correo.com` only appears in the composition of group ticket `TG01` and her user invoice is requested
- **THEN** no ticket item is listed and the total is 0.00

#### Scenario: Unknown user
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` is processed
- **THEN** an error message is displayed

### Requirement: Group invoice
The system SHALL accept `INVOICE CREATE;GROUP;<groupIdentifier>` for an existing group and display an invoice with the group identifier and name, its group tickets with their composition and the total price of each, and the invoice total equal to the sum of those group ticket totals. Composed individual tickets SHALL NOT be added separately to the total. An unknown group SHALL be rejected with an error message.

#### Scenario: Group with a group ticket
- **WHEN** group `G01` (Familia Garcia) bought group ticket `TG01` for 3 members at 36.00 each and `INVOICE CREATE;GROUP;G01` is processed
- **THEN** the invoice shows `G01` and `Familia Garcia`, `TG01` with its three composed tickets and a price of 108.00, and a total of 108.00

#### Scenario: Unknown group
- **WHEN** `INVOICE CREATE;GROUP;G99` is processed
- **THEN** an error message is displayed

### Requirement: Only relevant sections
Invoices SHALL include only sections relevant to the recipient type and with available information; sections without information SHALL be omitted rather than shown empty, and user and group invoices SHALL NOT carry meaningless fields of the other type.

#### Scenario: User without purchases
- **WHEN** an active user without tickets requests an invoice
- **THEN** the invoice shows the user identification and a total of 0.00 with no ticket section

#### Scenario: Group without tickets
- **WHEN** a group without group tickets requests an invoice
- **THEN** the invoice shows the group identification and a total of 0.00 with no group ticket section

### Requirement: Invalid invoice request
An `INVOICE CREATE` whose recipient type is neither `USER` nor `GROUP`, or with a wrong number of fields, SHALL be rejected with an error message.

#### Scenario: Unknown recipient type
- **WHEN** `INVOICE CREATE;COMPANY;X` is processed
- **THEN** an error message is displayed
