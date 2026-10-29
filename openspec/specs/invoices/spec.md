# invoices Specification

## Purpose

Generates on-demand invoices for users and groups from the stored economic information, built step by step with the Builder pattern and containing only the sections relevant to each recipient.

## Requirements

### Requirement: Builder-based construction
Invoices SHALL be constructed through a common builder abstraction with clearly differentiated construction steps (header, items, passes, total), implemented by concrete builders for user and group invoices and driven by a director; no constructor with many optional parameters SHALL be used. Invoices SHALL NOT be persisted.

#### Scenario: Builders share an abstraction
- **WHEN** the invoice classes are inspected
- **THEN** the user and group builders implement one builder interface whose steps are invoked by a director to produce an invoice

### Requirement: User invoice
`INVOICE CREATE;USER;<email>` SHALL, for an active user, show the user's identifying data; the individual tickets bought directly for the user (not composition tickets) with prices; the SpeedPasses and Special Passes of all the user's individual tickets (including composition tickets) with prices; and the total of the items shown. Sections without items SHALL be omitted.

#### Scenario: Tickets and passes
- **WHEN** Ana has ticket `T001` (40.00) with SpeedPass `SP01:1` (8.00) and Special Pass `PE01` (40.00), and is in group ticket `TG01` whose composition ticket `TG01:12345678Z` has SpeedPass `SP02:1` (8.00)
- **THEN** `INVOICE CREATE;USER;ana@correo.com` lists `T001` but not `TG01:12345678Z` as a ticket, lists `SP01:1`, `SP02:1` and `PE01`, and shows a total of 96.00

#### Scenario: Empty sections omitted
- **WHEN** Ana has one individual ticket and no passes
- **THEN** the invoice has a tickets section and no SpeedPass or Special Pass section

#### Scenario: Unknown or inactive user
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` is processed for an email with no active user
- **THEN** an error is reported

### Requirement: Group invoice
`INVOICE CREATE;GROUP;<groupIdentifier>` SHALL, for an existing group, show its identifier and name, each group ticket bought by it with its composition and total price, and the total of the group tickets. Composition tickets SHALL NOT be added separately and members' passes SHALL NOT be included.

#### Scenario: Group tickets with composition
- **WHEN** group `G01` (6 members) bought `TG02` at base price 40.00 and a member has a SpeedPass on their composition ticket
- **THEN** the group invoice shows `G01`, its name, `TG02` with its 6 composition tickets at 32.00 each, and a total of 192.00 that excludes the SpeedPass

#### Scenario: Unknown group
- **WHEN** `INVOICE CREATE;GROUP;G99` is processed and `G99` does not exist
- **THEN** an error is reported

#### Scenario: Invalid invoice kind
- **WHEN** `INVOICE CREATE;COMPANY;X` is processed
- **THEN** an error is reported
