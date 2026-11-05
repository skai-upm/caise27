# attraction-types Specification

## Purpose
Defines the attraction types GENERAL, INDIVIDUAL, GROUP and SPECIAL, which ticket types each accepts, the Special Pass condition of special attractions, and the design with ticket access-capability interfaces and bounded generic attraction types that enforces those restrictions.

## Requirements

### Requirement: Ticket types accepted by each attraction type
Each attraction SHALL have exactly one type. `GENERAL` attractions SHALL accept individual and group tickets; `INDIVIDUAL` attractions SHALL accept individual tickets only; `GROUP` attractions SHALL accept group tickets only; `SPECIAL` attractions SHALL accept individual tickets only. `ATTRACTION ENTER` with a ticket type not accepted by the attraction SHALL be rejected with an `ERROR` response that names the attraction type, and nobody SHALL enter.

#### Scenario: General attraction accepts both ticket types
- **GIVEN** `GENERAL` attraction `A01`, individual ticket `T001` and group ticket `TG01`, both valid today
- **WHEN** `ATTRACTION ENTER;A01;T001` and `ATTRACTION ENTER;A01;TG01` are processed
- **THEN** both responses start with `OK`

#### Scenario: Individual attraction rejects a group ticket
- **WHEN** `ATTRACTION ENTER;A02;TG01` is processed for `INDIVIDUAL` attraction `A02`
- **THEN** an `ERROR` response mentions that `A02` (`INDIVIDUAL`) does not accept group tickets and nobody enters

#### Scenario: Group attraction rejects an individual ticket
- **WHEN** `ATTRACTION ENTER;A03;T001` is processed for `GROUP` attraction `A03`
- **THEN** an `ERROR` response is printed and nobody enters

### Requirement: Special attractions require an unconsumed Special Pass
Entry to a `SPECIAL` attraction with an individual ticket SHALL additionally require that the ticket has a Special Pass that has not been consumed; the user SHALL NOT specify any additional option for this. When the ticket has no Special Pass, or its Special Pass is consumed, the entry SHALL be rejected with an `ERROR` response and nobody enters. (Consumption of the Special Pass on entry and the SpeedPass entry option are defined by the entry and pass consumption milestone.)

#### Scenario: Ticket with an unconsumed Special Pass enters
- **GIVEN** `SPECIAL` attraction `A04` and ticket `T001`, valid today, with unconsumed Special Pass `PE01`
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed
- **THEN** a response starting with `OK` is printed and the ticket's user is inside `A04`

#### Scenario: Ticket without a Special Pass rejected
- **WHEN** `ATTRACTION ENTER;A04;T002` is processed and `T002` has no Special Pass
- **THEN** an `ERROR` response mentions the missing Special Pass and nobody enters

#### Scenario: Consumed Special Pass rejected
- **GIVEN** the Special Pass of `T001` has been consumed
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed
- **THEN** an `ERROR` response is printed and nobody enters

### Requirement: Access capabilities as interfaces and bounded generic attractions
Ticket access capabilities SHALL be represented by interfaces: a general access capability implemented by every ticket, an individual access capability implemented by individual tickets, and a group access capability implemented by group tickets. Attractions SHALL be generic types whose type parameter is bounded by the general access capability, and each attraction type SHALL bind it to the capability it accepts (general for `GENERAL`, individual for `INDIVIDUAL` and `SPECIAL`, group for `GROUP`). The type parameter SHALL have an actual effect: an attraction SHALL only obtain, and run its entry rules on, tickets of its bound capability. `INDIVIDUAL` and `SPECIAL` attractions SHALL both use the individual capability; the Special Pass condition SHALL be behaviour of the special attraction, not a separate ticket hierarchy.

#### Scenario: Capabilities implemented by the ticket types
- **WHEN** the ticket classes are inspected
- **THEN** individual tickets implement the individual access capability, group tickets implement the group access capability, and both are general access capabilities

#### Scenario: Attraction accepts only tickets of its bound capability
- **WHEN** an `INDIVIDUAL` attraction is asked to accept a group ticket and an individual ticket
- **THEN** it accepts only the individual ticket, typed as its individual access capability
