# attraction-types Specification

## Purpose
Specialises attractions by the kind of ticket they admit — GENERAL, INDIVIDUAL, GROUP and SPECIAL — expressing ticket access capabilities with interfaces and restricting the accepted ticket kinds with bounded generics.

## Requirements

### Requirement: Four attraction types
The system SHALL support the attraction types `GENERAL`, `INDIVIDUAL`, `GROUP` and `SPECIAL`, each attraction having exactly one type.

#### Scenario: Explicit type
- **WHEN** `ATTRACTION CREATE;A02;Familiar;30;GROUP` is processed
- **THEN** an `OK:` response is printed and `A02` is a GROUP attraction

#### Scenario: Default type
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is processed
- **THEN** `A01` is a GENERAL attraction

#### Scenario: Unknown type
- **WHEN** `ATTRACTION CREATE;A03;X;10;VIP` or `ATTRACTION CREATE;A03;X;10;group` is processed
- **THEN** an `ERROR:` response is printed and no attraction is created

### Requirement: Accepted ticket kinds per type
GENERAL attractions SHALL admit individual and group tickets; INDIVIDUAL and SPECIAL attractions SHALL admit only individual tickets (SPECIAL then applying its additional Special Pass conditions); GROUP attractions SHALL admit only group tickets. A ticket of a kind the attraction does not admit SHALL be rejected with `ERROR:` and nobody enters; the other entry rules (date, capacity, already present) still apply to admitted kinds.

#### Scenario: GROUP rejects an individual ticket
- **GIVEN** GROUP attraction `A02` and individual ticket `T001` valid today
- **WHEN** `ATTRACTION ENTER;A02;T001` is processed
- **THEN** an `ERROR:` response is printed and nobody enters

#### Scenario: INDIVIDUAL rejects a group ticket
- **GIVEN** INDIVIDUAL attraction `A03` and group ticket `TG01` valid today
- **WHEN** `ATTRACTION ENTER;A03;TG01` is processed
- **THEN** an `ERROR:` response is printed and nobody enters

#### Scenario: Accepted kinds enter
- **WHEN** `T001` enters a GENERAL, INDIVIDUAL or SPECIAL attraction, `TG01` enters a GENERAL or GROUP attraction, or a constituent ticket `TG01-1` enters an INDIVIDUAL attraction
- **THEN** the visitors enter

### Requirement: Capability interfaces and bounded generics
Ticket access capabilities SHALL be represented by interfaces (a general admission capability refined into individual and group capabilities, implemented by the ticket classes), and attractions SHALL be generic in the capability they accept, bounded by the general admission capability, using that type parameter to decide admission and to receive the admitted ticket typed accordingly; SPECIAL SHALL be based on the individual-ticket capability without a separate ticket hierarchy.

#### Scenario: Design inspection
- **WHEN** the model classes are inspected
- **THEN** there is an interface for the general admission capability with individual and group sub-capabilities implemented by `IndividualTicket` and `GroupTicket`, `Attraction` declares a type parameter bounded by the admission interface, the GROUP attraction is parameterised with the group capability, and the SPECIAL attraction with the individual capability
