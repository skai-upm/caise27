# Spec Delta

## Purpose

Defines on-demand invoices for users and groups, built step by step with the Builder pattern from the stored purchases, including only the sections relevant to each recipient.

## ADDED Requirements

### Requirement: User invoice
`INVOICE CREATE;USER;<email>` SHALL print, for an existing active user, the user's identifying data, a section for individual tickets bought directly for the user, a section for SpeedPasses and one for Special Passes of any of the user's individual tickets (including composed ones), each item with its price, and the total of the items shown. Composed tickets SHALL NOT be listed as purchases. Empty sections SHALL be omitted.

#### Scenario: Passes on composed tickets counted, composed ticket not
- **WHEN** ana has T001 (40.00), one SpeedPass on T001 (8.00) and a Special Pass on composed ticket TG01:12345678Z (32.00)
- **THEN** the invoice lists T001, the SpeedPass and the Special Pass, not TG01:12345678Z, and the total is 80.00

#### Scenario: Empty sections omitted
- **WHEN** a user has tickets but no passes
- **THEN** the invoice shows no SpeedPass or Special Pass section

#### Scenario: Unknown user
- **WHEN** the email does not belong to an active user
- **THEN** an `ERROR:` message is printed

### Requirement: Group invoice
`INVOICE CREATE;GROUP;<groupIdentifier>` SHALL print, for an existing group, its identifier and name, each group ticket it bought with its composition and total price, and the sum of those group ticket prices. Composed tickets and members' passes SHALL NOT be added to the total.

#### Scenario: Group total
- **WHEN** G01 bought TG01 (192.00, 6 members) and members own SpeedPasses
- **THEN** the invoice lists TG01 with its 6 composed tickets and the total is 192.00

### Requirement: Builder-based construction
Invoices SHALL be built through a common builder abstraction in distinct steps, with concrete builders adding only the parts relevant to the invoice type, rather than through a constructor with many optional parameters.

#### Scenario: Group invoice has no pass sections
- **WHEN** a group invoice is built
- **THEN** it contains no SpeedPass or Special Pass section
