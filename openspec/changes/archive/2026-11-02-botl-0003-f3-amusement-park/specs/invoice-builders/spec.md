# Spec Delta

## Purpose

Generates invoices for users and groups on demand from the system state, built step by step with the Builder pattern so each invoice contains only the sections that apply to it.

## ADDED Requirements

### Requirement: Builder-based invoice construction
Invoices SHALL be constructed through a common builder abstraction in clearly differentiated steps (header, recipient, purchase sections), including only sections that have content, and showing each item's price and the total of the items shown; invoices SHALL be generated on demand and need not be persisted. (F3-C02)

#### Scenario: Empty sections omitted
- **WHEN** an invoice is generated for a recipient that has no passes
- **THEN** the invoice contains no pass sections

### Requirement: User invoice
`INVOICE CREATE;USER;<email>` SHALL, for an active user, produce an invoice with the user's identifying data, the individual tickets purchased directly for the user (not group constituents), the SpeedPasses and Special Passes associated with any of the user's individual tickets (including group constituents), each item's price and the total. (F3-C02)

#### Scenario: Direct ticket, group constituent and passes
- **WHEN** Ana has standalone ticket T001 (40.00), a constituent ticket of group ticket TG01, one SpeedPass on T001 (8.00) and a Special Pass on her TG01 constituent (40.00)
- **THEN** her invoice lists T001, the SpeedPass and the Special Pass, does not list the constituent ticket, and totals 88.00

#### Scenario: Unknown or deleted user
- **WHEN** the email does not belong to an active user
- **THEN** an error is reported

### Requirement: Group invoice
`INVOICE CREATE;GROUP;<groupIdentifier>` SHALL, for an existing group, produce an invoice with the group identifier and name, each group ticket bought by the group with its composition and total price, and the sum of those group-ticket totals; constituent tickets and members' passes SHALL NOT be added to the total. (F3-C02)

#### Scenario: Group total counted once
- **WHEN** G01 bought TG01 for 3 members at 40.00 base and one member bought a SpeedPass on their constituent
- **THEN** the group invoice shows TG01 with its three constituents and a total of 108.00

#### Scenario: Unknown group
- **WHEN** the group does not exist
- **THEN** an error is reported
