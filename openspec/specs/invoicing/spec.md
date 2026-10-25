# invoicing Specification

## Purpose

Generates on-demand invoices for users and groups from the purchases recorded in the system, assembled step by step with a Builder so that only applicable sections appear.

## Requirements

### Requirement: User invoice
`INVOICE CREATE;USER;<correo>` SHALL produce an invoice for the existing user with that email, containing the individual tickets bought directly for that user and the personal passes attached to any of the user's individual tickets (including tickets that belong to a group ticket). Individual tickets that belong to a group ticket SHALL NOT be listed as individual purchases.

#### Scenario: User with tickets and passes
- **WHEN** Ana bought T001 (40.00) with one SpeedPass (8.00), and is a member of group ticket TG01 whose internal ticket has a Special Pass (40.00)
- **THEN** her invoice lists T001 at 40.00, both passes with their prices, does not list TG01's internal ticket as a ticket, and shows a total of 88.00

#### Scenario: Unknown user
- **WHEN** the email does not belong to any user
- **THEN** an `ERROR` result is printed

### Requirement: Group invoice
`INVOICE CREATE;GROUP;<idGrupo>` SHALL produce an invoice for the existing group listing its group-ticket purchases and their total, without repeating the individual tickets inside them or the members' personal passes.

#### Scenario: Group with a group ticket
- **WHEN** G01 bought TG01 for 108.00 and a member has passes on TG01's internal ticket
- **THEN** the group invoice lists TG01 at 108.00 and shows a total of 108.00

#### Scenario: Unknown group
- **WHEN** the group does not exist
- **THEN** an `ERROR` result is printed

### Requirement: Applicable sections only
An invoice SHALL include only the sections that apply to its recipient type and for which data exists, show the price of each included item, and end with the total.

#### Scenario: User without purchases
- **WHEN** an invoice is requested for a user with no tickets or passes
- **THEN** the invoice shows the recipient and a total of 0.00 with no item sections

### Requirement: Builder construction
Invoices SHALL be assembled through a Builder abstraction in distinct steps (recipient, each applicable section, final build) rather than through constructors with many optional parameters.

#### Scenario: Step-wise assembly
- **WHEN** a user invoice is created
- **THEN** it is produced by invoking the builder's steps for the recipient and the applicable sections before building

### Requirement: Invoices are not persisted
Invoices SHALL be generated on demand and SHALL NOT be stored.

#### Scenario: No invoice file
- **WHEN** invoices are generated
- **THEN** no data file is created or modified
