# delivery-3-integration Specification

## Purpose

Captures the complete Delivery 3 behavior of the Amusement Park application as a whole: commands of all three deliveries coexisting in normal and temporary persistence modes, under the Delivery 3 technical restrictions.

## Requirements

### Requirement: Delivery 3 end-to-end flow
Built with `mvn package`, the JAR SHALL run a Delivery 3 command file — users, groups, typed attractions, individual and group tickets, SpeedPass and Special Pass purchases, `SPEED` and special entries, refunds, user and group invoices, `HELP` and `HELP EXTENDED` — producing the expected results both as `java -jar target/amusement-park.jar <file>` and as `java -jar target/amusement-park.jar --fly <file>`, with identical output from the same empty starting state and no data left after the `--fly` run.

#### Scenario: Same file in normal and fly mode
- **GIVEN** an empty working directory and a Delivery 3 command file
- **WHEN** it is run once with `--fly` and once without
- **THEN** both outputs are identical, the normal run leaves the data files (including passes), and the `--fly` run leaves no data on the system

#### Scenario: Earlier deliveries still work
- **WHEN** the Delivery 1 and Delivery 2 example sequences are run with the Delivery 3 JAR
- **THEN** they produce the same results as in their deliveries

### Requirement: Delivery 3 technical restrictions
The project SHALL keep Java 21, Maven and an executable JAR built by `mvn package`, a layered package organization, invoices built with the Builder pattern, singleton controllers with replaceable repositories injected at initialization, BCrypt (jBCrypt) as the only runtime dependency, attractions restricted by bounded generics over access interfaces, consumed passes kept after use, and atomic group entry with SpeedPasses.

#### Scenario: Restrictions checked automatically
- **WHEN** the test suite runs
- **THEN** the architecture, UML consistency, singleton/injection, password-protection, attraction-type and pass-consumption tests pass, and the only non-test dependency is `org.mindrot:jbcrypt`

### Requirement: Complete HELP and HELP EXTENDED for Delivery 3
`HELP` SHALL list the syntax of every command of the three deliveries, including `ATTRACTION CREATE;<id>;<name>;<capacity>;<type>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, the pass commands, the invoice commands, `HELP`, `HELP EXTENDED` and `EXIT`; `HELP EXTENDED` SHALL give a clear purpose for each of them.

#### Scenario: Every syntax listed and explained
- **WHEN** `HELP` and `HELP EXTENDED` are processed
- **THEN** each listed syntax appears in both outputs and every entry of `HELP EXTENDED` has a purpose
