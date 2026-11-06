# delivery-2-integration Specification

## Purpose

Captures the complete Delivery 2 behavior of the Amusement Park application as a whole: Delivery 1 and Delivery 2 commands coexisting, in normal and temporary persistence modes, under the delivery's technical restrictions.

## Requirements

### Requirement: Delivery 2 end-to-end flow
Built with `mvn package`, the JAR SHALL run a Delivery 2 command file — users, system date and price, groups, individual and group tickets, attractions with entry, exit, list and status, deletions and `HELP` — producing the expected results, both as `java -jar target/amusement-park.jar <file>` and as `java -jar target/amusement-park.jar --fly <file>`, with identical output in both modes from the same empty starting state.

#### Scenario: Same file in normal and fly mode
- **GIVEN** an empty working directory and a Delivery 2 command file
- **WHEN** it is run once with `--fly` and once without
- **THEN** both outputs are identical, the normal run leaves the data files, and the `--fly` run leaves no data on the system

#### Scenario: Delivery 1 commands still work
- **WHEN** the Delivery 1 example sequence is run with the Delivery 2 JAR
- **THEN** users are created, updated and deleted exactly as in Delivery 1

### Requirement: Domain relationships as object references
While loaded in memory, relationships between domain objects SHALL be object references and collections, not identifiers or strings, with these directions: attractions reference the users inside; individual tickets reference their user; group tickets are composed of individual tickets and reference their purchasing group while it exists; groups reference their member users; users and groups hold no references to tickets, and users hold no references to attractions or groups.

#### Scenario: Group members are user objects
- **GIVEN** Ana is a member of group `G01`
- **WHEN** Ana's email is changed with `USER UPDATE`
- **THEN** the group's member is the same user object and shows the new email, without any re-linking

#### Scenario: Members restored as objects
- **GIVEN** group `G01` with Ana was persisted
- **WHEN** the application restarts
- **THEN** `G01`'s member is the same user object that the user store loaded for Ana

### Requirement: Complete HELP for Delivery 2
`HELP` SHALL show the syntax of every Delivery 1 and Delivery 2 command: `USER CREATE`, `USER DELETE`, `USER UPDATE`, `SYSTEM DATE`, `SYSTEM PRICE`, `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`, `TICKET CREATE`, `TICKET DELETE`, `GROUPTICKET CREATE`, `GROUPTICKET DELETE`, `ATTRACTION CREATE`, `ATTRACTION ENTER`, `ATTRACTION EXIT`, `ATTRACTION LIST`, `ATTRACTION STATUS`, `HELP` and `EXIT`.

#### Scenario: Every command listed
- **WHEN** `HELP` is processed
- **THEN** each of the twenty commands above appears with its fields
