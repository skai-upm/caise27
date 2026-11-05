# Spec Delta

## ADDED Requirements

### Requirement: Delivery 2 technical restrictions
Delivery 2 SHALL keep the Delivery 1 restrictions and the executable-JAR behavior. While loaded in memory, park relationships SHALL be held as objects and collections in these directions only: attractions know their occupants; tickets know their user; group tickets are composed of their individual tickets and reference their purchasing group (possibly empty); groups, users and attractions hold no ticket references. All ticket types SHALL share one abstraction, there SHALL be a single central-management instance, and `--fly` SHALL use the same repositories as normal mode, configured only with a different directory.

#### Scenario: Relationship directions
- **GIVEN** the domain classes
- **WHEN** their fields are inspected
- **THEN** `Group` and `Attraction` hold no ticket references, `Ticket` types hold no attraction references, and `GroupTicket` holds a collection of `IndividualTicket` and a `Group` reference

#### Scenario: Phase-2 command file in both modes
- **GIVEN** a phase-2 command file
- **WHEN** it is run normally and with `--fly`
- **THEN** both runs process all commands, the normal run persists its data, and the `--fly` run leaves no data behind
